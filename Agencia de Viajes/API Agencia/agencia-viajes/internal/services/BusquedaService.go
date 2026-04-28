// # Package services
//
// Servicios de negocio de la agencia de viajes. Este paquete contiene la logica
// central para reservaciones, busquedas, autenticacion, catalogos y comunicacion
// con proveedores externos (aerolineas y hoteleras).
package services

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/helpers"
	"agencia-viajes/internal/repositories"
	"bytes"
	"context"
	"database/sql"
	"encoding/json"
	"fmt"
	"log"
	"math"
	"net/http"
	"time"

	"github.com/gin-gonic/gin"
)

// TipoBusquedaVuelos es el ID del tipo de busqueda de vuelos en la tabla TipoBusqueda.
const TipoBusquedaVuelos = 1

// TipoBusquedaHoteles es el ID del tipo de busqueda de hoteles en la tabla TipoBusqueda.
const TipoBusquedaHoteles = 2

// timeoutProveedor es el tiempo maximo que se espera la respuesta de un proveedor
// externo antes de cancelar la peticion y continuar con el siguiente.
const timeoutProveedor = 10 * time.Second

// BusquedaService
//
// Servicio encargado de realizar busquedas de vuelos y hoteles consultando
// los proveedores externos registrados en el catalogo. Aplica el margen de
// ganancia configurado por proveedor sobre los precios retornados y registra
// un historico de cada busqueda, ya sea de un usuario autenticado o anonimo.
//
// Si uno o mas proveedores fallan o no responden a tiempo, sus errores se
// incluyen en la respuesta como resultados parciales sin interrumpir las
// consultas a los demas proveedores.
type BusquedaService struct {
	repo      *repositories.BusquedaRepository
	client    *http.Client
	logSesion *LogSesionService
}

// NewBusquedaService
//
// Crea e inicializa una nueva instancia de BusquedaService con su repositorio
// de busqueda y un cliente HTTP con timeout configurado por proveedor.
//
// Parametros:
//   - db:        conexion activa a la base de datos SQL
//   - logSesion: servicio de auditoria para registrar eventos REST salientes
//
// Retorna:
//   - *BusquedaService: instancia lista para usar
func NewBusquedaService(db *sql.DB, logSesion *LogSesionService) *BusquedaService {
	return &BusquedaService{
		repo:      repositories.NewBusquedaRepository(db),
		logSesion: logSesion,
		client: &http.Client{
			Timeout: timeoutProveedor,
		},
	}
}

// registrarBusqueda
//
// Serializa los parametros de busqueda a JSON y llama al repositorio para
// insertar el registro historico. Si el INSERT falla, imprime el error en
// el log del servidor pero NO interrumpe el flujo principal: la busqueda
// sigue retornando resultados aunque el historico no se haya podido guardar.
//
// Parametros:
//   - tipoBusquedaID: ID del tipo (TipoBusquedaVuelos o TipoBusquedaHoteles)
//   - usuarioID: puntero al ID del usuario autenticado; nil para busquedas anonimas
//   - params: struct con los parametros de busqueda a serializar como JSON
//   - ciudadOrigenID: puntero al ID de la ciudad origen; nil para hoteles
//   - ciudadDestinoID: ID de la ciudad destino
func (s *BusquedaService) registrarBusqueda(
	tipoBusquedaID int,
	usuarioID *int,
	params interface{},
	ciudadOrigenID *int,
	ciudadDestinoID int,
) {
	parametrosJSON, err := json.Marshal(params)
	if err != nil {
		log.Printf("[BusquedaService] ERROR al serializar parametros (tipo=%d): %v", tipoBusquedaID, err)
		return
	}

	if err := s.repo.RegistrarBusqueda(
		tipoBusquedaID,
		usuarioID,
		string(parametrosJSON),
		ciudadOrigenID,
		ciudadDestinoID,
	); err != nil {
		log.Printf("[BusquedaService] ERROR al guardar busqueda en BD (tipo=%d, usuarioID=%v): %v",
			tipoBusquedaID, usuarioID, err)
	}
}

// BuscarVuelos
//
// Busca vuelos disponibles entre dos ciudades consultando todos los proveedores
// aerolineas registrados para esa ruta en el catalogo. Resuelve los IDs de
// ciudad para origen y destino, obtiene la lista de proveedores activos y
// llama a cada uno de forma individual.
//
// Si un proveedor falla o no responde dentro de timeoutProveedor, su error
// queda registrado en la respuesta y la busqueda continua con los demas
// proveedores, garantizando resultados parciales en lugar de un fallo total.
//
// Parametros:
//   - req: datos de busqueda incluyendo ciudad/pais de origen, destino y demas filtros
//   - usuarioID: puntero al ID del usuario autenticado; nil si es anonimo
//
// Retorna:
//   - []dto.BusquedaVuelosResponse: lista de respuestas por proveedor, con datos o error
//   - error: solo si falla la resolucion de ciudades o la consulta de proveedores en BD
func (s *BusquedaService) BuscarVuelos(c *gin.Context, req dto.BusquedaVuelosRequest, usuarioID *int) ([]dto.BusquedaVuelosResponse, error) {
	origenID, err := s.repo.BuscarCiudadID(req.Origen, req.OrigenPais)
	if err != nil {
		return nil, err
	}
	if origenID == nil {
		return nil, fmt.Errorf("ciudad origen '%s, %s' no encontrada en catálogo", req.Origen, req.OrigenPais)
	}

	destinoID, err := s.repo.BuscarCiudadID(req.Destino, req.DestinoPais)
	if err != nil {
		return nil, err
	}
	if destinoID == nil {
		return nil, fmt.Errorf("ciudad destino '%s, %s' no encontrada en catálogo", req.Destino, req.DestinoPais)
	}

	// Registro sincrono: si falla, el error aparece en el log del servidor
	// pero NO impide que el usuario reciba sus resultados de busqueda.
	s.registrarBusqueda(TipoBusquedaVuelos, usuarioID, req, origenID, *destinoID)

	proveedores, err := s.repo.ObtenerAerolineasPorRuta(*origenID, *destinoID)
	if err != nil {
		return nil, err
	}
	if len(proveedores) == 0 {
		return []dto.BusquedaVuelosResponse{}, nil
	}

	var resultados []dto.BusquedaVuelosResponse
	for _, p := range proveedores {
		datos, err := s.llamarVuelos(c, usuarioID, p, req)
		if err != nil {
			log.Printf("[BusquedaService] Proveedor '%s' (ID=%d) no disponible: %v", p.Nombre, p.ProveedorID, err)
			resultados = append(resultados, dto.BusquedaVuelosResponse{
				ProveedorID:     p.ProveedorID,
				Proveedor:       p.Nombre,
				ProveedorImagen: p.ImagenBase64,
				Error:           err.Error(),
			})
			continue
		}
		resultados = append(resultados, dto.BusquedaVuelosResponse{
			ProveedorID:     p.ProveedorID,
			Proveedor:       p.Nombre,
			ProveedorImagen: p.ImagenBase64,
			Datos:           datos,
		})
	}
	return resultados, nil
}

// llamarVuelos
//
// Realiza la llamada HTTP POST al endpoint de busqueda de vuelos de un proveedor
// aerolinea especifico y aplica el porcentaje de ganancia configurado sobre
// los precios retornados. Usa un contexto con timeout para que el proveedor
// no bloquee la busqueda mas alla de timeoutProveedor segundos.
//
// Parametros:
//   - p: datos del proveedor incluyendo URL, token y porcentaje de ganancia
//   - req: parametros de busqueda a enviar al proveedor
//
// Retorna:
//   - interface{}: datos de vuelos con precios ajustados por margen de ganancia
//   - error: si la peticion HTTP falla, expira el timeout o el proveedor retorna un estado no exitoso
func (s *BusquedaService) llamarVuelos(c *gin.Context, usuarioID *int, p dto.ProveedorCatalogo, req dto.BusquedaVuelosRequest) (interface{}, error) {
	body, _ := json.Marshal(req)

	ctx, cancel := context.WithTimeout(context.Background(), timeoutProveedor)
	defer cancel()

	httpReq, err := http.NewRequestWithContext(ctx, http.MethodPost, p.URLApi+"/api/vuelos-agencia/buscar", bytes.NewBuffer(body))
	if err != nil {
		return nil, err
	}
	httpReq.Header.Set("Content-Type", "application/json")
	httpReq.Header.Set("X-Agencia-Token", p.TokenEntrada)

	resp, err := s.client.Do(httpReq)
	if err != nil {
		s.logSesion.Registrar(c, helpers.TipoOutBusquedaVuelosFallida, usuarioID, "busqueda-vuelos",
			fmt.Sprintf("%s status=ERR msg='%s'", p.Nombre, err.Error()))
		return nil, fmt.Errorf("proveedor no disponible: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		msg := fmt.Sprintf("%s status=%d msg='%s'", p.Nombre, resp.StatusCode, helpers.ParseErrorProveedor(resp))
		s.logSesion.Registrar(c, helpers.TipoOutBusquedaVuelosFallida, usuarioID, "busqueda-vuelos", msg)
		return nil, fmt.Errorf("aerolinea respondió con status %d", resp.StatusCode)
	}

	var datos interface{}
	if err := json.NewDecoder(resp.Body).Decode(&datos); err != nil {
		s.logSesion.Registrar(c, helpers.TipoOutBusquedaVuelosFallida, usuarioID, "busqueda-vuelos",
			fmt.Sprintf("%s status=%d decode_error='%s'", p.Nombre, resp.StatusCode, err.Error()))
		return nil, err
	}

	// NO APLICAR DESCUENTO: Los precios vienen SIN descuento del proveedor.
	// El descuento es a nivel lógico/negocio (para calcular ganancia en reservación)
	// pero el usuario VE SIEMPRE el precio original.
	// datos = aplicarGanancia(datos, p.PorcentajeGanancia)

	count := 0
	switch v := datos.(type) {
	case map[string]interface{}:
		// Broom devuelve { directos: [...], conEscala: [...] }
		if directos, ok := v["directos"].([]interface{}); ok {
			count += len(directos)
		}
		if conEscala, ok := v["conEscala"].([]interface{}); ok {
			count += len(conEscala)
		}
	case []interface{}:
		// Fallback por si algún proveedor futuro devuelve array plano
		count = len(v)
	}

	if count > 0 {
		s.logSesion.Registrar(c, helpers.TipoOutBusquedaVuelosExitosa, usuarioID, "busqueda-vuelos",
			fmt.Sprintf("%s: %d resultado(s) para %s→%s fecha=%s", p.Nombre, count, req.Origen, req.Destino, req.Fecha))
	} else {
		s.logSesion.Registrar(c, helpers.TipoOutBusquedaVuelosSinResultados, usuarioID, "busqueda-vuelos",
			fmt.Sprintf("%s: 0 resultados para %s→%s fecha=%s", p.Nombre, req.Origen, req.Destino, req.Fecha))
	}

	return datos, nil
}

// BuscarHoteles
//
// Busca hoteles disponibles en una ciudad consultando todos los proveedores
// hoteleras registrados para esa ubicacion en el catalogo. Resuelve el ID
// de ciudad, obtiene la lista de proveedores de tipo hotelera y llama a
// cada uno de forma individual.
//
// Si un proveedor falla o no responde dentro de timeoutProveedor, su error
// queda registrado en la respuesta y la busqueda continua con los demas
// proveedores, garantizando resultados parciales en lugar de un fallo total.
//
// Parametros:
//   - req: datos de busqueda incluyendo ciudad, pais y demas filtros de hospedaje
//   - usuarioID: puntero al ID del usuario autenticado; nil si es anonimo
//
// Retorna:
//   - []dto.BusquedaHotelesResponse: lista de respuestas por proveedor, con datos o error
//   - error: solo si falla la resolucion de ciudad o la consulta de proveedores en BD
func (s *BusquedaService) BuscarHoteles(c *gin.Context, req dto.BusquedaHotelesRequest, usuarioID *int) ([]dto.BusquedaHotelesResponse, error) {
	ciudadID, err := s.repo.BuscarCiudadID(req.Ciudad, req.Pais)
	if err != nil {
		return nil, err
	}
	if ciudadID == nil {
		return nil, fmt.Errorf("ciudad '%s, %s' no encontrada en catálogo", req.Ciudad, req.Pais)
	}

	// Registro sincrono: si falla, el error aparece en el log del servidor
	// pero NO impide que el usuario reciba sus resultados de busqueda.
	s.registrarBusqueda(TipoBusquedaHoteles, usuarioID, req, nil, *ciudadID)

	proveedores, err := s.repo.ObtenerProveedoresPorOrigenYTipo(*ciudadID, 2)
	if err != nil {
		return nil, err
	}
	if len(proveedores) == 0 {
		return []dto.BusquedaHotelesResponse{}, nil
	}

	var resultados []dto.BusquedaHotelesResponse
	for _, p := range proveedores {
		datos, err := s.llamarHoteles(c, usuarioID, p, req)
		if err != nil {
			log.Printf("[BusquedaService] Proveedor '%s' (ID=%d) no disponible: %v", p.Nombre, p.ProveedorID, err)
			resultados = append(resultados, dto.BusquedaHotelesResponse{
				ProveedorID:     p.ProveedorID,
				Proveedor:       p.Nombre,
				ProveedorImagen: p.ImagenBase64,
				Error:           err.Error(),
			})
			continue
		}
		resultados = append(resultados, dto.BusquedaHotelesResponse{
			ProveedorID:     p.ProveedorID,
			Proveedor:       p.Nombre,
			ProveedorImagen: p.ImagenBase64,
			Datos:           datos,
		})
	}
	return resultados, nil
}

// llamarHoteles
//
// Realiza la llamada HTTP POST al endpoint de busqueda de hoteles de un proveedor
// hotelera especifico y aplica el porcentaje de ganancia configurado sobre
// los precios retornados. Usa un contexto con timeout para que el proveedor
// no bloquee la busqueda mas alla de timeoutProveedor segundos.
//
// Parametros:
//   - p: datos del proveedor incluyendo URL, token y porcentaje de ganancia
//   - req: parametros de busqueda a enviar al proveedor
//
// Retorna:
//   - interface{}: datos de hoteles con precios ajustados por margen de ganancia
//   - error: si la peticion HTTP falla, expira el timeout o el proveedor retorna un estado no exitoso
func (s *BusquedaService) llamarHoteles(c *gin.Context, usuarioID *int, p dto.ProveedorCatalogo, req dto.BusquedaHotelesRequest) (interface{}, error) {
	body, _ := json.Marshal(req)

	ctx, cancel := context.WithTimeout(context.Background(), timeoutProveedor)
	defer cancel()

	httpReq, err := http.NewRequestWithContext(ctx, http.MethodPost, p.URLApi+"/agencia/busqueda", bytes.NewBuffer(body))
	if err != nil {
		return nil, err
	}
	httpReq.Header.Set("Content-Type", "application/json")
	httpReq.Header.Set("X-Agencia-Token", p.TokenEntrada)

	resp, err := s.client.Do(httpReq)
	if err != nil {
		s.logSesion.Registrar(c, helpers.TipoOutBusquedaHotelesFallida, usuarioID, "busqueda-hoteles",
			fmt.Sprintf("%s status=ERR msg='%s'", p.Nombre, err.Error()))
		return nil, fmt.Errorf("proveedor no disponible: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		msg := fmt.Sprintf("%s status=%d msg='%s'", p.Nombre, resp.StatusCode, helpers.ParseErrorProveedor(resp))
		s.logSesion.Registrar(c, helpers.TipoOutBusquedaHotelesFallida, usuarioID, "busqueda-hoteles", msg)
		return nil, fmt.Errorf("hotelera respondió con status %d", resp.StatusCode)
	}

	var datos interface{}
	if err := json.NewDecoder(resp.Body).Decode(&datos); err != nil {
		s.logSesion.Registrar(c, helpers.TipoOutBusquedaHotelesFallida, usuarioID, "busqueda-hoteles",
			fmt.Sprintf("%s status=%d decode_error='%s'", p.Nombre, resp.StatusCode, err.Error()))
		return nil, err
	}

	// NO APLICAR DESCUENTO: Los precios vienen SIN descuento del proveedor.
	// El descuento es a nivel lógico/negocio (para calcular ganancia en reservación)
	// pero el usuario VE SIEMPRE el precio original.
	// datos = aplicarGanancia(datos, p.PorcentajeGanancia)

	count := 0
	if arr, ok := datos.([]interface{}); ok {
		count = len(arr)
	}

	if count > 0 {
		s.logSesion.Registrar(c, helpers.TipoOutBusquedaHotelesExitosa, usuarioID, "busqueda-hoteles",
			fmt.Sprintf("%s: %d resultado(s) para %s check-in=%s", p.Nombre, count, req.Ciudad, req.FechaCheckIn))
	} else {
		s.logSesion.Registrar(c, helpers.TipoOutBusquedaHotelesSinResultados, usuarioID, "busqueda-hoteles",
			fmt.Sprintf("%s: 0 resultados para %s check-in=%s", p.Nombre, req.Ciudad, req.FechaCheckIn))
	}

	return datos, nil
}

// aplicarGanancia
//
// Recorre recursivamente una estructura de datos JSON (mapas y slices) y
// aplica el factor de descuento (porcentaje de ganancia del proveedor) reduciendo
// los precios. Los campos de precio recibidos del proveedor son multiplicados por
// (1 - porcentaje) para obtener el precio con descuento que se mostrará al cliente.
// Los campos afectados son: precioTurista, precioEjecutiva, precioPorPersona y precioPorNoche.
//
// Parametros:
//   - data: estructura de datos generica (map[string]interface{} o []interface{})
//   - porcentaje: porcentaje de descuento del proveedor (ej: 55 para 55% descuento)
//
// Retorna:
//   - interface{}: la misma estructura con los precios reducidos por el descuento, redondeados a 2 decimales
func aplicarGanancia(data interface{}, porcentaje float64) interface{} {
	// Aplicar descuento: precio_con_descuento = precio_original * (1 - porcentaje%)
	log.Printf("[aplicarGanancia] DEBUG: Porcentaje_Descuento=%.2f%%, aplicando reducción de precios", porcentaje)

	factor := 1 - (porcentaje / 100)
	if factor <= 0 {
		factor = 1 // Evitar factor negativo o cero
		log.Printf("[aplicarGanancia] WARN: factor <= 0, usando factor=1 (sin descuento)")
	}

	switch v := data.(type) {
	case map[string]interface{}:
		for key, val := range v {
			switch key {
			case "precioTurista", "precioEjecutiva", "precioTuristaTotal", "precioEjecutivaTotal", "precioPorPersona", "precioPorNoche", "precio", "total":
				if precio, ok := val.(float64); ok {
					precioConDescuento := math.Round((precio*factor)*100) / 100
					if factor != 1 && (key == "precioPorNoche" || key == "precioPorPersona" || key == "precio") {
						log.Printf("[aplicarGanancia] %s: %.2f → %.2f (* %.4f)", key, precio, precioConDescuento, factor)
					}
					v[key] = precioConDescuento
				}
			default:
				v[key] = aplicarGanancia(val, porcentaje)
			}
		}
		return v

	case []interface{}:
		for i, item := range v {
			v[i] = aplicarGanancia(item, porcentaje)
		}
		return v
	}

	return data
}