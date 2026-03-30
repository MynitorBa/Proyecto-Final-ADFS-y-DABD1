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

type CatalogoService struct {
	repo             *repositories.CatalogoRepository
	ubicacionService *UbicacionService
}

func NewCatalogoService(db *sql.DB, ubicacionService *UbicacionService) *CatalogoService {
	return &CatalogoService{
		repo:             repositories.NewCatalogoRepository(db),
		ubicacionService: ubicacionService,
	}
}

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
