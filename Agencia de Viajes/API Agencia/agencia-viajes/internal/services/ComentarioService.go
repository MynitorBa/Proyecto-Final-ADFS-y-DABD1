package services

import (
	"agencia-viajes/internal/repositories"
	"crypto/tls"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
)

type ComentarioService struct {
	proveedorRepo *repositories.ProveedorRepository
}

func NewComentarioService(proveedorRepo *repositories.ProveedorRepository) *ComentarioService {
	return &ComentarioService{proveedorRepo: proveedorRepo}
}

// ObtenerComentariosVuelo — llama al proveedor específico por su ID
func (s *ComentarioService) ObtenerComentariosVuelo(proveedorID, rutaID int) (interface{}, error) {
	proveedor, err := s.proveedorRepo.ObtenerProveedorPorID(proveedorID)
	if err != nil {
		return nil, fmt.Errorf("proveedor no encontrado: %w", err)
	}

	url := fmt.Sprintf("%s/api/comentarios/agencia/ruta/%d", proveedor.URLAPI, rutaID)
	return s.llamarProveedor(url, proveedor.TokenEntrada)
}

// ObtenerComentariosHotel — llama al proveedor específico por su ID
func (s *ComentarioService) ObtenerComentariosHotel(proveedorID, hotelID int) (interface{}, error) {
	proveedor, err := s.proveedorRepo.ObtenerProveedorPorID(proveedorID)
	if err != nil {
		return nil, fmt.Errorf("proveedor no encontrado: %w", err)
	}

	url := fmt.Sprintf("%s/agencia/comentarios/hotel/%d", proveedor.URLAPI, hotelID)
	return s.llamarProveedor(url, proveedor.TokenEntrada)
}

func (s *ComentarioService) llamarProveedor(url, token string) (interface{}, error) {
	req, err := http.NewRequest("GET", url, nil)
	if err != nil {
		return nil, err
	}
	req.Header.Set("X-Agencia-Token", token)

	client := &http.Client{
		Transport: &http.Transport{
			TLSClientConfig: &tls.Config{InsecureSkipVerify: true},
		},
	}

	resp, err := client.Do(req)
	if err != nil {
		return nil, fmt.Errorf("error contactando proveedor: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode >= 400 {
		return nil, fmt.Errorf("proveedor respondió con status %d", resp.StatusCode)
	}

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, err
	}

	var resultado interface{}
	if err := json.Unmarshal(body, &resultado); err != nil {
		return nil, err
	}
	return resultado, nil
}
