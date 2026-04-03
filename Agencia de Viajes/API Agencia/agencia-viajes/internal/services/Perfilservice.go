package services

import (
	"agencia-viajes/internal/helpers"
	"agencia-viajes/internal/repositories"
	"database/sql"
)

type PerfilService struct {
	repo *repositories.PerfilRepository
}

func NewPerfilService(db *sql.DB) *PerfilService {
	return &PerfilService{repo: repositories.NewPerfilRepository(db)}
}

func (s *PerfilService) ObtenerPerfil(usuarioID int) (map[string]interface{}, error) {
	return s.repo.ObtenerPerfil(usuarioID)
}

func (s *PerfilService) ActualizarTelefono(usuarioID int, telefono string) error {
	return s.repo.ActualizarTelefono(usuarioID, telefono)
}

func (s *PerfilService) ObtenerHash(usuarioID int) (string, error) {
	return s.repo.ObtenerHash(usuarioID)
}

func (s *PerfilService) CambiarContrasena(usuarioID int, nueva string) error {
	hash, err := helpers.HashPassword(nueva)
	if err != nil {
		return err
	}
	return s.repo.ActualizarContrasena(usuarioID, hash)
}