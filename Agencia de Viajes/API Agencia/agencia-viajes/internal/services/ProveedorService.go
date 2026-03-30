package services

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/repositories"
	"database/sql"
	"errors"
)

type ProveedorService struct {
	repo *repositories.ProveedorRepository
}

func NewProveedorService(db *sql.DB) *ProveedorService {
	return &ProveedorService{
		repo: repositories.NewProveedorRepository(db),
	}
}

func (s *ProveedorService) CrearProveedor(req dto.CrearProveedorRequest) (dto.CrearProveedorResponse, error) {

	// 1. El usuario debe existir y ser rol 3 (webservice)
	rolID, err := s.repo.ObtenerRolUsuario(req.UsuarioID)
	if err != nil {
		return dto.CrearProveedorResponse{}, err
	}
	if rolID == 0 {
		return dto.CrearProveedorResponse{}, errors.New("el usuario no existe")
	}
	if rolID != 3 {
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
