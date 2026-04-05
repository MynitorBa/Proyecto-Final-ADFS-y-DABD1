// # Package services
//
// Contiene los servicios de negocio de la agencia de viajes,
// incluyendo procesamiento de pagos, reservaciones, proveedores y usuarios.
package services

import (
	"agencia-viajes/internal/models"
	"agencia-viajes/internal/repositories"
	"database/sql"
	"strings"
)

// UbicacionService
//
// Servicio encargado de resolver ubicaciones geograficas (paises, ciudades y nacionalidades),
// creandolas en la base de datos si no existen previamente.
type UbicacionService struct {
	repo *repositories.UbicacionRepository
}

// NewUbicacionService
//
// Crea e inicializa una nueva instancia de UbicacionService con su repositorio.
//
// Parametros:
//   - db: conexion activa a la base de datos SQL
//
// Retorna:
//   - *UbicacionService: instancia inicializada del servicio de ubicaciones
func NewUbicacionService(db *sql.DB) *UbicacionService {
	return &UbicacionService{
		repo: repositories.NewUbicacionRepository(db),
	}
}

// UbicacionResult
//
// Estructura que agrupa el resultado de una busqueda o creacion de ubicacion,
// conteniendo el pais y la ciudad resueltos.
type UbicacionResult struct {
	Pais   models.Pais   `json:"pais"`
	Ciudad models.Ciudad `json:"ciudad"`
}

// ObtenerOCrearUbicacion
//
// Busca en la base de datos el pais y la ciudad indicados por nombre.
// Si alguno no existe, lo crea automaticamente. Limpia espacios en blanco
// de los nombres antes de procesarlos.
//
// Parametros:
//   - nombreCiudad: nombre de la ciudad a buscar o crear
//   - nombrePais: nombre del pais al que pertenece la ciudad
//
// Retorna:
//   - UbicacionResult: struct con el pais y la ciudad resueltos
//   - error: error si falla la consulta o insercion en base de datos
func (s *UbicacionService) ObtenerOCrearUbicacion(nombreCiudad, nombrePais string) (UbicacionResult, error) {
	nombreCiudad = strings.TrimSpace(nombreCiudad)
	nombrePais = strings.TrimSpace(nombrePais)

	pais, err := s.repo.BuscarOCrearPais(nombrePais)
	if err != nil {
		return UbicacionResult{}, err
	}

	ciudad, err := s.repo.BuscarOCrearCiudad(nombreCiudad, pais.ID)
	if err != nil {
		return UbicacionResult{}, err
	}

	return UbicacionResult{
		Pais:   pais,
		Ciudad: ciudad,
	}, nil
}

// ObtenerOCrearNacionalidades
//
// Procesa un slice de nombres de nacionalidades, buscando cada una en la base de datos
// y creandola si no existe. Ignora entradas vacias o que solo contengan espacios.
// Recibe un slice porque el usuario puede tener multiples nacionalidades.
//
// Parametros:
//   - nombres: slice de nombres de nacionalidades a resolver
//
// Retorna:
//   - []models.Nacionalidad: slice con las nacionalidades resueltas
//   - error: error si falla la consulta o insercion de alguna nacionalidad en base de datos
func (s *UbicacionService) ObtenerOCrearNacionalidades(nombres []string) ([]models.Nacionalidad, error) {
	var nacionalidades []models.Nacionalidad

	for _, nombre := range nombres {
		nombre = strings.TrimSpace(nombre)
		if nombre == "" {
			continue
		}

		nacionalidad, err := s.repo.BuscarOCrearNacionalidad(nombre)
		if err != nil {
			return nil, err
		}

		nacionalidades = append(nacionalidades, nacionalidad)
	}

	return nacionalidades, nil
}
