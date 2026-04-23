// # Package services
//
// Servicios de negocio de la agencia de viajes. Este paquete contiene la logica
// central para reservaciones, busquedas, autenticacion, catalogos y comunicacion
// con proveedores externos (aerolineas y hoteleras).
package services

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/helpers"
	"agencia-viajes/internal/models"
	"agencia-viajes/internal/repositories"
	"database/sql"
	"errors"
	"strings"
)

// LoginService
//
// Servicio encargado de gestionar la autenticacion de usuarios en la agencia
// de viajes. Valida las credenciales ingresadas contra la base de datos y
// retorna la informacion del usuario autenticado para generar la sesion.
// ILoginRepository define las operaciones de base de datos necesarias para autenticacion.
type ILoginRepository interface {
	ObtenerPorUsernameOCorreo(login string) (models.Usuario, error)
}

type LoginService struct {
	repo            ILoginRepository
	captchaVerifier func(string) (bool, string)
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
		repo:            repositories.NewLoginRepository(db),
		captchaVerifier: helpers.VerificarCaptcha,
	}
}

// NewLoginServiceConRepo crea un LoginService con repositorio e inyector de captcha para pruebas.
func NewLoginServiceConRepo(repo ILoginRepository, captchaV func(string) (bool, string)) *LoginService {
	return &LoginService{repo: repo, captchaVerifier: captchaV}
}

var (
	ErrUsuarioNoEncontrado  = errors.New("usuario no encontrado")
	ErrContrasenaInvalida   = errors.New("contraseña inválida")
	ErrUsuarioDeshabilitado = errors.New("usuario deshabilitado")
	ErrCamposVacios         = errors.New("login o contraseña vacíos")
	ErrCaptchaAusente       = errors.New("token de captcha no proporcionado")
	ErrCaptchaInvalido      = errors.New("token de captcha rechazado por google")
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
	// 1. Validar que login y contraseña no vengan vacíos
	if strings.TrimSpace(req.Login) == "" || req.Contrasena == "" {
		return dto.LoginResponse{}, ErrCamposVacios
	}

	// 2. Validar que venga el token de captcha
	if strings.TrimSpace(req.Captcha) == "" {
		return dto.LoginResponse{}, ErrCaptchaAusente
	}

	// 3. Verificar el captcha con Google
	if valido, _ := s.captchaVerifier(req.Captcha); !valido {
		return dto.LoginResponse{}, ErrCaptchaInvalido
	}

	usuario, err := s.repo.ObtenerPorUsernameOCorreo(req.Login)
	if err != nil {
		return dto.LoginResponse{}, err
	}

	if usuario.ID == 0 {
		return dto.LoginResponse{}, ErrCredencialesInvalidas
	}

	if usuario.EstadoID != 1 {
		return dto.LoginResponse{}, ErrUsuarioDeshabilitado
	}

	if !helpers.CheckPassword(req.Contrasena, usuario.Contrasena) {
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
