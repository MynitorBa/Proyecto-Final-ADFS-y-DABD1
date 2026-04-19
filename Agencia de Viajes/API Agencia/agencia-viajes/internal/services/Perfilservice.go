// # Package services
//
// Contiene los servicios de negocio de la agencia de viajes,
// incluyendo procesamiento de pagos, reservaciones, proveedores y usuarios.
package services

import (
	"agencia-viajes/internal/helpers"
	"agencia-viajes/internal/repositories"
	"database/sql"
)

// PerfilService
//
// Servicio encargado de gestionar las operaciones sobre el perfil del usuario,
// incluyendo la consulta de datos, actualizacion de telefono y cambio de contrasena.
type PerfilService struct {
	repo *repositories.PerfilRepository
}

// NewPerfilService
//
// Crea e inicializa una nueva instancia de PerfilService con su repositorio.
//
// Parametros:
//   - db: conexion activa a la base de datos SQL
//
// Retorna:
//   - *PerfilService: instancia inicializada del servicio de perfil
func NewPerfilService(db *sql.DB) *PerfilService {
	return &PerfilService{repo: repositories.NewPerfilRepository(db)}
}

// ObtenerPerfil
//
// Recupera los datos del perfil del usuario identificado por su ID.
//
// Parametros:
//   - usuarioID: identificador del usuario cuyo perfil se desea obtener
//
// Retorna:
//   - map[string]interface{}: mapa con los campos del perfil del usuario
//   - error: error si el usuario no existe o falla la consulta a la base de datos
func (s *PerfilService) ObtenerPerfil(usuarioID int) (map[string]interface{}, error) {
	return s.repo.ObtenerPerfil(usuarioID)
}

// ActualizarTelefono
//
// Actualiza el numero de telefono del usuario identificado por su ID.
//
// Parametros:
//   - usuarioID: identificador del usuario a actualizar
//   - telefono: nuevo numero de telefono a asignar
//
// Retorna:
//   - error: error si falla la actualizacion en la base de datos
func (s *PerfilService) ActualizarTelefono(usuarioID int, telefono string) error {
	return s.repo.ActualizarTelefono(usuarioID, telefono)
}

// ObtenerHash
//
// Obtiene el hash de la contrasena actual del usuario para verificacion previa
// antes de permitir el cambio de contrasena.
//
// Parametros:
//   - usuarioID: identificador del usuario cuyo hash se desea obtener
//
// Retorna:
//   - string: hash bcrypt de la contrasena actual del usuario
//   - error: error si el usuario no existe o falla la consulta a la base de datos
func (s *PerfilService) ObtenerHash(usuarioID int) (string, error) {
	return s.repo.ObtenerHash(usuarioID)
}

// ObtenerTelefonoYPais
//
// Recupera el telefono actual y el nombre del pais del usuario para validar
// que el nuevo numero sea distinto al actual y tenga los digitos correctos
// segun el pais antes de persistir el cambio.
//
// Parametros:
//   - usuarioID: identificador del usuario
//
// Retorna:
//   - telefono: numero de telefono actual (cadena vacia si no tiene)
//   - pais: nombre del pais (cadena vacia si no tiene ciudad asignada)
//   - error: error si falla la consulta a la base de datos
func (s *PerfilService) ObtenerTelefonoYPais(usuarioID int) (telefono, pais string, err error) {
	return s.repo.ObtenerTelefonoYPais(usuarioID)
}

// CambiarContrasena
//
// Genera el hash bcrypt de la nueva contrasena y lo persiste en la base de datos
// para el usuario indicado.
//
// Parametros:
//   - usuarioID: identificador del usuario que cambia su contrasena
//   - nueva: nueva contrasena en texto plano que sera hasheada antes de guardarse
//
// Retorna:
//   - error: error si falla la generacion del hash o la actualizacion en la base de datos
func (s *PerfilService) CambiarContrasena(usuarioID int, nueva string) error {
	hash, err := helpers.HashPassword(nueva)
	if err != nil {
		return err
	}
	return s.repo.ActualizarContrasena(usuarioID, hash)
}
