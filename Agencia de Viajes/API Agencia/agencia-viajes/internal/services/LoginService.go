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
	"database/sql"
	"errors"
)

// LoginService
//
// Servicio encargado de gestionar la autenticacion de usuarios en la agencia
// de viajes. Valida las credenciales ingresadas contra la base de datos y
// retorna la informacion del usuario autenticado para generar la sesion.
type LoginService struct {
	repo *repositories.LoginRepository
}

// NewLoginService
//
// Crea e inicializa una nueva instancia de LoginService con su repositorio
// de login.
//
// Parametros:
//   - db: conexion activa a la base de datos SQL
//
// Retorna:
//   - *LoginService: instancia lista para usar
func NewLoginService(db *sql.DB) *LoginService {
	return &LoginService{
		repo: repositories.NewLoginRepository(db),
	}
}

var (
	ErrUsuarioNoEncontrado = errors.New("usuario no encontrado")
	ErrContrasenaInvalida  = errors.New("contraseña inválida")
)

var ErrCredencialesInvalidas = errors.New("credenciales inválidas")

// Login
//
// Autentica a un usuario verificando sus credenciales contra la base de datos.
// Busca al usuario por username o correo electronico y verifica la contrasena
// usando hashing seguro. Si las credenciales son invalidas retorna un error
// generico para evitar revelar si el usuario existe o no.
//
// Parametros:
//   - req: datos de login con campo Login (username o correo) y Contrasena
//
// Retorna:
//   - dto.LoginResponse: datos del usuario autenticado (ID, nombre, apellido, correo, username, rol)
//   - error: ErrCredencialesInvalidas si el usuario no existe o la contrasena es incorrecta
func (s *LoginService) Login(req dto.LoginRequest) (dto.LoginResponse, error) {
	usuario, err := s.repo.ObtenerPorUsernameOCorreo(req.Login)
	if err != nil {
		return dto.LoginResponse{}, err
	}

	if usuario.ID == 0 || !helpers.CheckPassword(req.Contrasena, usuario.Contrasena) {
		return dto.LoginResponse{}, ErrCredencialesInvalidas
	}

	return dto.LoginResponse{
		ID:       usuario.ID,
		Nombre:   usuario.Nombre,
		Apellido: usuario.Apellido,
		Correo:   usuario.Correo,
		Username: usuario.Username,
		RolID:    usuario.RolID,
	}, nil
}
