package services

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/helpers"
	"agencia-viajes/internal/repositories"
	"database/sql"
	"errors"
)

type LoginService struct {
	repo *repositories.LoginRepository
}

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
