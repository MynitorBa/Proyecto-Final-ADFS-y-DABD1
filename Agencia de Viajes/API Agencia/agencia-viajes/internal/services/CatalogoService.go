// # Package services
//
// Servicios de negocio de la agencia de viajes. Este paquete contiene la logica
// central para reservaciones, busquedas, autenticacion, catalogos y comunicacion
// con proveedores externos (aerolineas y hoteleras).
package services

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/repositories"
	"database/sql"
	"encoding/json"
	"fmt"
	"net/http"
)

const (
	TipoAerolinea = 1
	TipoHotelera  = 2
)

// CatalogoService
//
// Servicio encargado de sincronizar el catalogo local de rutas y ubicaciones
// con la informacion provista por los proveedores externos registrados.
// Soporta proveedores de tipo aerolinea (rutas origen-destino) y hotelera
// (hoteles por ciudad), gestionando la creacion de ubicaciones nuevas
// mediante el UbicacionService.
type CatalogoService struct {
	repo             *repositories.CatalogoRepository
	ubicacionService *UbicacionService
}

// NewCatalogoService
//
// Crea e inicializa una nueva instancia de CatalogoService con su repositorio
// y el servicio de ubicaciones requerido.
//
// Parametros:
//   - db: conexion activa a la base de datos SQL
//   - ubicacionService: servicio para obtener o crear ciudades/paises en BD
//
// Retorna:
//   - *CatalogoService: instancia lista para usar
func NewCatalogoService(db *sql.DB, ubicacionService *UbicacionService) *CatalogoService {
	return &CatalogoService{
		repo:             repositories.NewCatalogoRepository(db),
		ubicacionService: ubicacionService,
	}
}

// ActualizarCatalogo
//
// Actualiza el catalogo completo de la agencia iterando sobre todos los
// proveedores activos registrados en BD. Por cada proveedor llama a
// actualizarProveedor y acumula el resultado o el error en la respuesta.
// Un fallo en un proveedor no detiene el proceso para los demas.
//
// Retorna:
//   - []dto.ActualizarCatalogoResponse: lista de resultados por proveedor con conteo de insertados
//   - error: si falla la consulta de proveedores activos en BD
func (s *CatalogoService) ActualizarCatalogo() ([]dto.ActualizarCatalogoResponse, error) {
	// 1. Obtener todos los proveedores activos
	proveedorIDs, err := s.repo.ObtenerProveedoresActivos()
	if err != nil {
		return nil, err
	}

	var resultados []dto.ActualizarCatalogoResponse

	for _, proveedorID := range proveedorIDs {
		resultado, err := s.actualizarProveedor(proveedorID)
		if err != nil {
			resultados = append(resultados, dto.ActualizarCatalogoResponse{
				Proveedor:  fmt.Sprintf("Proveedor ID %d", proveedorID),
				Insertados: 0,
				Mensaje:    "Error: " + err.Error(),
			})
			continue
		}
		resultados = append(resultados, resultado)
	}

	return resultados, nil
}

// actualizarProveedor
//
// Actualiza el catalogo de un proveedor especifico. Determina su tipo
// (aerolinea o hotelera), obtiene sus datos de conexion y delega el
// procesamiento al metodo correspondiente segun el tipo.
//
// Parametros:
//   - proveedorID: identificador del proveedor a actualizar
//
// Retorna:
//   - dto.ActualizarCatalogoResponse: resultado con nombre del proveedor y cantidad de entradas insertadas
//   - error: si falla la consulta del tipo, los datos de conexion o el procesamiento
func (s *CatalogoService) actualizarProveedor(proveedorID int) (dto.ActualizarCatalogoResponse, error) {
	// 1. Obtener tipo (aerolinea=1 / hotelera=2)
	tipoID, err := s.repo.ObtenerTipoProveedor(proveedorID)
	if err != nil {
		return dto.ActualizarCatalogoResponse{}, err
	}

	// 2. Obtener datos de conexión
	urlAPI, tokenEntrada, err := s.repo.ObtenerDatosConexion(proveedorID)
	if err != nil {
		return dto.ActualizarCatalogoResponse{}, err
	}

	// 3. Llamar al proveedor según su tipo
	var insertados int
	switch tipoID {
	case TipoAerolinea:
		insertados, err = s.procesarAerolinea(proveedorID, urlAPI, tokenEntrada)
	case TipoHotelera:
		insertados, err = s.procesarHotelera(proveedorID, urlAPI, tokenEntrada)
	default:
		return dto.ActualizarCatalogoResponse{}, fmt.Errorf("tipo de proveedor desconocido: %d", tipoID)
	}

	if err != nil {
		return dto.ActualizarCatalogoResponse{}, err
	}

	return dto.ActualizarCatalogoResponse{
		Proveedor:  fmt.Sprintf("Proveedor ID %d", proveedorID),
		Insertados: insertados,
		Mensaje:    "Catálogo actualizado exitosamente",
	}, nil
}

// procesarAerolinea
//
// Sincroniza el catalogo de rutas de una aerolinea. Obtiene las rutas desde
// el API externo, limpia las entradas previas de ese proveedor en BD y luego
// inserta cada ruta resolviendo o creando las ciudades de origen y destino.
// Los errores por ruta individual se omiten y el conteo refleja solo las insertadas.
//
// Parametros:
//   - proveedorID: identificador del proveedor aerolinea
//   - urlAPI: URL base del API del proveedor
//   - tokenEntrada: token de autenticacion para el proveedor
//
// Retorna:
//   - int: numero de rutas insertadas exitosamente
//   - error: si falla la llamada al API o la limpieza del catalogo en BD
func (s *CatalogoService) procesarAerolinea(proveedorID int, urlAPI, tokenEntrada string) (int, error) {
	// 1. Llamar a la aerolínea
	rutas, err := s.obtenerRutasAerolinea(urlAPI, tokenEntrada)
	if err != nil {
		return 0, err
	}

	// 2. Limpiar catálogo actual de este proveedor
	if err := s.repo.EliminarCatalogoPorProveedor(proveedorID); err != nil {
		return 0, err
	}

	// 3. Insertar cada ruta
	insertados := 0
	for _, ruta := range rutas {
		// Buscar o crear ciudad origen
		origenResult, err := s.ubicacionService.ObtenerOCrearUbicacion(
			ruta.CiudadOrigen, ruta.PaisOrigen,
		)
		if err != nil {
			continue
		}

		// Buscar o crear ciudad destino
		destinoResult, err := s.ubicacionService.ObtenerOCrearUbicacion(
			ruta.CiudadDestino, ruta.PaisDestino,
		)
		if err != nil {
			continue
		}

		destinoID := destinoResult.Ciudad.ID
		err = s.repo.InsertarEntrada(
			origenResult.Ciudad.ID,
			&destinoID, // aerolinea siempre tiene destino
			TipoAerolinea,
			proveedorID,
		)
		if err != nil {
			continue
		}
		insertados++
	}

	return insertados, nil
}

// obtenerRutasAerolinea
//
// Llama al endpoint de rutas del API de una aerolinea proveedora y retorna
// la lista de rutas disponibles en formato DTO.
//
// Parametros:
//   - urlAPI: URL base del API del proveedor aerolinea
//   - token: token de autenticacion de agencia (X-Agencia-Token)
//
// Retorna:
//   - []dto.RutaProveedorDTO: lista de rutas con ciudades y paises de origen y destino
//   - error: si la peticion HTTP falla, el proveedor retorna error o el JSON es invalido
func (s *CatalogoService) obtenerRutasAerolinea(urlAPI, token string) ([]dto.RutaProveedorDTO, error) {
	req, err := http.NewRequest(http.MethodGet, urlAPI+"/api/rutas-agencia", nil)
	if err != nil {
		return nil, err
	}
	req.Header.Set("X-Agencia-Token", token)

	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("la aerolínea respondió con status %d", resp.StatusCode)
	}

	var rutas []dto.RutaProveedorDTO
	if err := json.NewDecoder(resp.Body).Decode(&rutas); err != nil {
		return nil, err
	}

	return rutas, nil
}

// procesarHotelera
//
// Sincroniza el catalogo de hoteles de una hotelera. Obtiene los hoteles desde
// el API externo, limpia las entradas previas de ese proveedor en BD y luego
// inserta cada hotel resolviendo o creando la ciudad correspondiente.
// A diferencia de las aerolineas, las hoteleras no tienen ciudad destino.
// Los errores por hotel individual se omiten y el conteo refleja solo los insertados.
//
// Parametros:
//   - proveedorID: identificador del proveedor hotelera
//   - urlAPI: URL base del API del proveedor
//   - tokenEntrada: token de autenticacion para el proveedor
//
// Retorna:
//   - int: numero de hoteles insertados exitosamente
//   - error: si falla la llamada al API o la limpieza del catalogo en BD
func (s *CatalogoService) procesarHotelera(proveedorID int, urlAPI, tokenEntrada string) (int, error) {
	// 1. Llamar a la hotelera
	hoteles, err := s.obtenerHotelesHotelera(urlAPI, tokenEntrada)
	if err != nil {
		return 0, err
	}

	// 2. Limpiar catálogo actual de este proveedor
	if err := s.repo.EliminarCatalogoPorProveedor(proveedorID); err != nil {
		return 0, err
	}

	// 3. Insertar cada hotel — solo origen, sin destino
	insertados := 0
	for _, hotel := range hoteles {
		origenResult, err := s.ubicacionService.ObtenerOCrearUbicacion(
			hotel.Ciudad, hotel.Pais,
		)
		if err != nil {
			continue
		}

		err = s.repo.InsertarEntrada(
			origenResult.Ciudad.ID,
			nil, // hotelera no tiene destino
			TipoHotelera,
			proveedorID,
		)
		if err != nil {
			continue
		}
		insertados++
	}

	return insertados, nil
}

// obtenerHotelesHotelera
//
// Llama al endpoint de hoteles del API de una hotelera proveedora y retorna
// la lista de hoteles disponibles en formato DTO.
//
// Parametros:
//   - urlAPI: URL base del API del proveedor hotelera
//   - token: token de autenticacion de agencia (X-Agencia-Token)
//
// Retorna:
//   - []dto.HotelProveedorDTO: lista de hoteles con ciudad y pais
//   - error: si la peticion HTTP falla, el proveedor retorna error o el JSON es invalido
func (s *CatalogoService) obtenerHotelesHotelera(urlAPI, token string) ([]dto.HotelProveedorDTO, error) {
	req, err := http.NewRequest(http.MethodGet, urlAPI+"/api/hoteles-agencia", nil)
	if err != nil {
		return nil, err
	}
	req.Header.Set("X-Agencia-Token", token)

	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("la hotelera respondió con status %d", resp.StatusCode)
	}

	var hoteles []dto.HotelProveedorDTO
	if err := json.NewDecoder(resp.Body).Decode(&hoteles); err != nil {
		return nil, err
	}

	return hoteles, nil
}
