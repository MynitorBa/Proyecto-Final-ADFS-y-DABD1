// # Package services
//
// Contiene los servicios de negocio de la agencia de viajes,
// incluyendo procesamiento de pagos, reservaciones, proveedores y usuarios.
package services

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/helpers"
	"agencia-viajes/internal/repositories"
	"database/sql"
	"errors"
)

// ProveedorService
//
// Servicio encargado de la logica de negocio relacionada con la creacion
// y gestion de proveedores externos registrados en el sistema.
type ProveedorService struct {
	repo *repositories.ProveedorRepository
}

// NewProveedorService
//
// Crea e inicializa una nueva instancia de ProveedorService con su repositorio.
//
// Parametros:
//   - db: conexion activa a la base de datos SQL
//
// Retorna:
//   - *ProveedorService: instancia inicializada del servicio de proveedores
func NewProveedorService(db *sql.DB) *ProveedorService {
	return &ProveedorService{
		repo: repositories.NewProveedorRepository(db),
	}
}

// CrearProveedor
//
// Ejecuta las validaciones de negocio necesarias antes de registrar un nuevo proveedor:
// verifica que el usuario exista y tenga rol webservice (rol 3),
// que dicho usuario no tenga ya un proveedor asignado,
// y que el tipo de proveedor indicado sea valido.
// Si todas las validaciones pasan, delega la creacion al repositorio.
//
// Parametros:
//   - req: datos del proveedor a crear, incluyendo el ID de usuario y tipo de proveedor
//
// Retorna:
//   - dto.CrearProveedorResponse: datos del proveedor creado incluyendo token generado
//   - error: error si el usuario no existe, no tiene el rol correcto,
//     ya tiene un proveedor asignado o el tipo de proveedor no existe
func (s *ProveedorService) CrearProveedor(req dto.CrearProveedorRequest) (dto.CrearProveedorResponse, error) {

	// 1. El usuario debe existir y ser rol 3 (webservice)
	rolID, err := s.repo.ObtenerRolUsuario(req.UsuarioID)
	if err != nil {
		return dto.CrearProveedorResponse{}, err
	}
	if rolID == 0 {
		return dto.CrearProveedorResponse{}, errors.New("el usuario no existe")
	}
	if rolID != helpers.RolWebService {
		return dto.CrearProveedorResponse{}, errors.New("el usuario debe tener rol webservice (rol 3)")
	}

	// 2. El usuario webservice no puede tener más de un proveedor
	yaExiste, err := s.repo.UsuarioYaTieneProveedor(req.UsuarioID)
	if err != nil {
		return dto.CrearProveedorResponse{}, err
	}
	if yaExiste {
		return dto.CrearProveedorResponse{}, errors.New("el usuario webservice ya tiene un proveedor asignado")
	}

	// 3. El tipo de proveedor debe existir
	tipoValido, err := s.repo.ExisteTipoProveedor(req.TipoProveedorID)
	if err != nil {
		return dto.CrearProveedorResponse{}, err
	}
	if !tipoValido {
		return dto.CrearProveedorResponse{}, errors.New("el tipo de proveedor no existe")
	}

	// 4. Crear el proveedor
	return s.repo.CrearProveedor(req)
}
