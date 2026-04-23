// # Package services
//
// Contiene los servicios de negocio de la agencia de viajes,
// incluyendo procesamiento de pagos, reservaciones, proveedores y usuarios.
package services

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/repositories"
	"database/sql"
)

// UsuarioService
//
// Servicio encargado de la logica de negocio para el registro y validacion
// de usuarios, incluyendo la resolucion de ubicaciones y nacionalidades
// mediante el servicio de ubicacion.
// IUsuarioRepository define las operaciones de base de datos necesarias para el servicio de usuarios.
type IUsuarioRepository interface {
	ExisteCorreo(correo string) (bool, error)
	ExistePasaporte(pasaporte string) (bool, error)
	ExisteUsername(username string) (bool, error)
	CrearUsuario(req dto.RegistroUsuarioRequest, ciudadID, rolID, estadoID int) (int, error)
	AsignarNacionalidades(usuarioID int, nacionalidadIDs []int) error
	ObtenerTodos() ([]dto.UsuarioResumen, error)
}

type UsuarioService struct {
	repo             IUsuarioRepository
	ubicacionService *UbicacionService
}

// NewUsuarioService
//
// Crea e inicializa una nueva instancia de UsuarioService con sus dependencias.
//
// Parametros:
//   - db: conexion activa a la base de datos SQL
//   - ubicacionService: servicio de ubicaciones para resolver pais, ciudad y nacionalidades
//
// Retorna:
//   - *UsuarioService: instancia inicializada del servicio de usuarios
func NewUsuarioService(db *sql.DB, ubicacionService *UbicacionService) *UsuarioService {
	return &UsuarioService{
		repo:             repositories.NewUsuarioRepository(db),
		ubicacionService: ubicacionService,
	}
}

// NewUsuarioServiceConRepo crea un UsuarioService con repositorio inyectado para pruebas.
func NewUsuarioServiceConRepo(repo IUsuarioRepository, ubicacionService *UbicacionService) *UsuarioService {
	return &UsuarioService{repo: repo, ubicacionService: ubicacionService}
}

// ValidarDatosUnicos
//
// Verifica si el correo, pasaporte o username del request ya existen
// en la base de datos. Devuelve un struct de validacion indicando cuales
// campos ya estan en uso.
//
// Parametros:
//   - req: datos del usuario a registrar, incluyendo correo, pasaporte y username
//
// Retorna:
//   - dto.ValidacionUsuarioResponse: flags indicando que campos ya existen en la base de datos
//   - error: error si falla alguna consulta a la base de datos
func (s *UsuarioService) ValidarDatosUnicos(req dto.RegistroUsuarioRequest) (dto.ValidacionUsuarioResponse, error) {
	response := dto.ValidacionUsuarioResponse{}

	existeCorreo, err := s.repo.ExisteCorreo(req.Correo)
	if err != nil {
		return response, err
	}

	existePasaporte, err := s.repo.ExistePasaporte(req.Pasaporte)
	if err != nil {
		return response, err
	}

	existeUsername, err := s.repo.ExisteUsername(req.Username)
	if err != nil {
		return response, err
	}

	response.Correo    = existeCorreo
	response.Pasaporte = existePasaporte
	response.Username  = existeUsername

	return response, nil
}

// Registrar
//
// Ejecuta el flujo completo de registro de un nuevo usuario: valida que correo,
// pasaporte y username no esten duplicados, resuelve la ubicacion geografica
// (pais y ciudad), resuelve las nacionalidades, crea el usuario en la base de datos
// con estado y rol por defecto (1), y le asigna sus nacionalidades.
// Si hay duplicados en los datos unicos, retorna la validacion sin registrar al usuario.
//
// Parametros:
//   - req: datos completos del usuario a registrar
//
// Retorna:
//   - dto.ValidacionUsuarioResponse: resultado de la validacion de datos unicos
//   - error: error si falla la validacion, la resolucion de ubicacion o la insercion en base de datos
func (s *UsuarioService) Registrar(req dto.RegistroUsuarioRequest) (dto.ValidacionUsuarioResponse, error) {
	validacion, err := s.ValidarDatosUnicos(req)
	if err != nil {
		return validacion, err
	}

	if validacion.Correo || validacion.Pasaporte || validacion.Username {
		return validacion, nil
	}

	ubicacion, err := s.ubicacionService.ObtenerOCrearUbicacion(req.Ciudad, req.Pais)
	if err != nil {
		return validacion, err
	}

	nacionalidades, err := s.ubicacionService.ObtenerOCrearNacionalidades(req.Nacionalidades)
	if err != nil {
		return validacion, err
	}

	usuarioID, err := s.repo.CrearUsuario(req, ubicacion.Ciudad.ID, 1, 1)
	if err != nil {
		return validacion, err
	}

	nacionalidadIDs := make([]int, len(nacionalidades))
	for i, n := range nacionalidades {
		nacionalidadIDs[i] = n.ID
	}

	return validacion, s.repo.AsignarNacionalidades(usuarioID, nacionalidadIDs)
}

// ObtenerTodos
//
// Retorna la lista completa de usuarios registrados en el sistema
// con sus datos basicos y rol asignado. Usado por el panel de
// administracion para gestion de roles y asignacion de WebService.
//
// Retorna:
//   - []dto.UsuarioResumen: lista de usuarios con id, nombre, apellido, correo y rol
//   - error: error si falla la consulta a la base de datos
func (s *UsuarioService) ObtenerTodos() ([]dto.UsuarioResumen, error) {
	return s.repo.ObtenerTodos()
}