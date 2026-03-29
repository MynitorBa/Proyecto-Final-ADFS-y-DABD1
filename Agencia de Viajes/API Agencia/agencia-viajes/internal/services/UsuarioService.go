package services

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/repositories"
	"database/sql"
)

type UsuarioService struct {
	repo             *repositories.UsuarioRepository
	ubicacionService *UbicacionService // <- agregar esto
}

func NewUsuarioService(db *sql.DB, ubicacionService *UbicacionService) *UsuarioService {
	return &UsuarioService{
		repo:             repositories.NewUsuarioRepository(db),
		ubicacionService: ubicacionService,
	}
}

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

	response.Correo = existeCorreo
	response.Pasaporte = existePasaporte
	response.Username = existeUsername

	return response, nil
}

func (s *UsuarioService) Registrar(req dto.RegistroUsuarioRequest) (dto.ValidacionUsuarioResponse, error) {
	validacion, err := s.ValidarDatosUnicos(req)
	if err != nil {
		return validacion, err
	}

	// Si hay duplicados, devuelve la validación sin registrar
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
