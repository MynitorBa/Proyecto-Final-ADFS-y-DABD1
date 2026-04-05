// # Package services
//
// Servicios de negocio de la agencia de viajes. Este paquete contiene la logica
// central para reservaciones, busquedas, autenticacion, catalogos y comunicacion
// con proveedores externos (aerolineas y hoteleras).
package services

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/repositories"
	"bytes"
	"database/sql"
	"encoding/json"
	"fmt"
	"math"
	"net/http"
)

// BusquedaService
//
// Servicio encargado de realizar busquedas de vuelos y hoteles consultando
// los proveedores externos registrados en el catalogo. Aplica el margen de
// ganancia configurado por proveedor sobre los precios retornados.
type BusquedaService struct {
	repo *repositories.BusquedaRepository
}

// NewBusquedaService
//
// Crea e inicializa una nueva instancia de BusquedaService con su repositorio
// de busqueda.
//
// Parametros:
//   - db: conexion activa a la base de datos SQL
//
// Retorna:
//   - *BusquedaService: instancia lista para usar
func NewBusquedaService(db *sql.DB) *BusquedaService {
	return &BusquedaService{
		repo: repositories.NewBusquedaRepository(db),
	}
}

// BuscarVuelos
//
// Busca vuelos disponibles entre dos ciudades consultando todos los proveedores
// aerolineas registrados para esa ruta en el catalogo. Resuelve los IDs de
// ciudad para origen y destino, obtiene la lista de proveedores activos y
// llama a cada uno de forma individual. Si un proveedor falla, se incluye
// su error en la respuesta sin interrumpir las demas consultas.
//
// Parametros:
//   - req: datos de busqueda incluyendo ciudad/pais de origen, destino y demas filtros
//
// Retorna:
//   - []dto.BusquedaVuelosResponse: lista de respuestas por proveedor, con datos o error
//   - error: si falla la resolucion de ciudades o la consulta de proveedores en BD
func (s *BusquedaService) BuscarVuelos(req dto.BusquedaVuelosRequest) ([]dto.BusquedaVuelosResponse, error) {
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

	proveedores, err := s.repo.ObtenerAerolineasPorRuta(*origenID, *destinoID)
	if err != nil {
		return nil, err
	}
	if len(proveedores) == 0 {
		return []dto.BusquedaVuelosResponse{}, nil
	}

	var resultados []dto.BusquedaVuelosResponse
	for _, p := range proveedores {
		datos, err := s.llamarVuelos(p, req)
		if err != nil {
			resultados = append(resultados, dto.BusquedaVuelosResponse{
				ProveedorID: p.ProveedorID,
				Proveedor:   p.Nombre,
				Error:       err.Error(),
			})
			continue
		}
		resultados = append(resultados, dto.BusquedaVuelosResponse{
			ProveedorID: p.ProveedorID,
			Proveedor:   p.Nombre,
			Datos:       datos,
		})
	}
	return resultados, nil
}

// llamarVuelos
//
// Realiza la llamada HTTP POST al endpoint de busqueda de vuelos de un proveedor
// aerolinea especifico y aplica el porcentaje de ganancia configurado sobre
// los precios retornados.
//
// Parametros:
//   - p: datos del proveedor incluyendo URL, token y porcentaje de ganancia
//   - req: parametros de busqueda a enviar al proveedor
//
// Retorna:
//   - interface{}: datos de vuelos con precios ajustados por margen de ganancia
//   - error: si la peticion HTTP falla o el proveedor retorna un estado no exitoso
func (s *BusquedaService) llamarVuelos(p dto.ProveedorCatalogo, req dto.BusquedaVuelosRequest) (interface{}, error) {
	body, _ := json.Marshal(req)

	httpReq, err := http.NewRequest(http.MethodPost, p.URLApi+"/api/vuelos-agencia/buscar", bytes.NewBuffer(body))
	if err != nil {
		return nil, err
	}
	httpReq.Header.Set("Content-Type", "application/json")
	httpReq.Header.Set("X-Agencia-Token", p.TokenEntrada)

	resp, err := http.DefaultClient.Do(httpReq)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("aerolinea respondió con status %d", resp.StatusCode)
	}

	var datos interface{}
	if err := json.NewDecoder(resp.Body).Decode(&datos); err != nil {
		return nil, err
	}

	datos = aplicarGanancia(datos, p.PorcentajeGanancia)
	return datos, nil
}

// BuscarHoteles
//
// Busca hoteles disponibles en una ciudad consultando todos los proveedores
// hoteleras registrados para esa ubicacion en el catalogo. Resuelve el ID
// de ciudad, obtiene la lista de proveedores de tipo hotelera y llama a
// cada uno de forma individual. Si un proveedor falla, se incluye su error
// en la respuesta sin interrumpir las demas consultas.
//
// Parametros:
//   - req: datos de busqueda incluyendo ciudad, pais y demas filtros de hospedaje
//
// Retorna:
//   - []dto.BusquedaHotelesResponse: lista de respuestas por proveedor, con datos o error
//   - error: si falla la resolucion de ciudad o la consulta de proveedores en BD
func (s *BusquedaService) BuscarHoteles(req dto.BusquedaHotelesRequest) ([]dto.BusquedaHotelesResponse, error) {
	ciudadID, err := s.repo.BuscarCiudadID(req.Ciudad, req.Pais)
	if err != nil {
		return nil, err
	}
	if ciudadID == nil {
		return nil, fmt.Errorf("ciudad '%s, %s' no encontrada en catálogo", req.Ciudad, req.Pais)
	}

	proveedores, err := s.repo.ObtenerProveedoresPorOrigenYTipo(*ciudadID, 2)
	if err != nil {
		return nil, err
	}
	if len(proveedores) == 0 {
		return []dto.BusquedaHotelesResponse{}, nil
	}

	var resultados []dto.BusquedaHotelesResponse
	for _, p := range proveedores {
		datos, err := s.llamarHoteles(p, req)
		if err != nil {
			resultados = append(resultados, dto.BusquedaHotelesResponse{
				ProveedorID: p.ProveedorID,
				Proveedor:   p.Nombre,
				Error:       err.Error(),
			})
			continue
		}
		resultados = append(resultados, dto.BusquedaHotelesResponse{
			ProveedorID: p.ProveedorID,
			Proveedor:   p.Nombre,
			Datos:       datos,
		})
	}
	return resultados, nil
}

// llamarHoteles
//
// Realiza la llamada HTTP POST al endpoint de busqueda de hoteles de un proveedor
// hotelera especifico y aplica el porcentaje de ganancia configurado sobre
// los precios retornados.
//
// Parametros:
//   - p: datos del proveedor incluyendo URL, token y porcentaje de ganancia
//   - req: parametros de busqueda a enviar al proveedor
//
// Retorna:
//   - interface{}: datos de hoteles con precios ajustados por margen de ganancia
//   - error: si la peticion HTTP falla o el proveedor retorna un estado no exitoso
func (s *BusquedaService) llamarHoteles(p dto.ProveedorCatalogo, req dto.BusquedaHotelesRequest) (interface{}, error) {
	body, _ := json.Marshal(req)

	httpReq, err := http.NewRequest(http.MethodPost, p.URLApi+"/agencia/busqueda", bytes.NewBuffer(body))
	if err != nil {
		return nil, err
	}
	httpReq.Header.Set("Content-Type", "application/json")
	httpReq.Header.Set("X-Agencia-Token", p.TokenEntrada)

	resp, err := http.DefaultClient.Do(httpReq)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("hotelera respondió con status %d", resp.StatusCode)
	}

	var datos interface{}
	if err := json.NewDecoder(resp.Body).Decode(&datos); err != nil {
		return nil, err
	}

	datos = aplicarGanancia(datos, p.PorcentajeGanancia)
	return datos, nil
}

// aplicarGanancia
//
// Recorre recursivamente una estructura de datos JSON (mapas y slices) y
// multiplica los campos de precio conocidos por el factor de ganancia calculado
// a partir del porcentaje indicado. Los campos afectados son: precioTurista,
// precioEjecutiva, precioPorPersona y precioPorNoche.
//
// Parametros:
//   - data: estructura de datos generica (map[string]interface{} o []interface{})
//   - porcentaje: porcentaje de ganancia a aplicar (ej: 15 para 15%)
//
// Retorna:
//   - interface{}: la misma estructura con los precios ajustados, redondeados a 2 decimales
func aplicarGanancia(data interface{}, porcentaje float64) interface{} {
	multiplicador := 1 + (porcentaje / 100)

	switch v := data.(type) {
	case map[string]interface{}:
		for key, val := range v {
			switch key {
			case "precioTurista", "precioEjecutiva", "precioPorPersona", "precioPorNoche":
				if precio, ok := val.(float64); ok {
					v[key] = math.Round(precio*multiplicador*100) / 100
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
