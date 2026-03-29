package services

import (
	"agencia-viajes/internal/models"
	"agencia-viajes/internal/repositories"
	"database/sql"
	"strings"
)

type UbicacionService struct {
	repo *repositories.UbicacionRepository
}

func NewUbicacionService(db *sql.DB) *UbicacionService {
	return &UbicacionService{
		repo: repositories.NewUbicacionRepository(db),
	}
}

type UbicacionResult struct {
	Pais   models.Pais   `json:"pais"`
	Ciudad models.Ciudad `json:"ciudad"`
}

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

// Función separada que recibe un slice porque el usuario puede tener múltiples
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
