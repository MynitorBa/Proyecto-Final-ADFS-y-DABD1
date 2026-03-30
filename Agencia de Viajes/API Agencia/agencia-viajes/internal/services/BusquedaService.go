package services

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/repositories"
	"bytes"
	"database/sql"
	"encoding/json"
	"fmt"
	"net/http"
)

type BusquedaService struct {
	repo *repositories.BusquedaRepository
}

func NewBusquedaService(db *sql.DB) *BusquedaService {
	return &BusquedaService{
		repo: repositories.NewBusquedaRepository(db),
	}
}

func (s *BusquedaService) BuscarVuelos(req dto.BusquedaVuelosRequest) ([]dto.BusquedaVuelosResponse, error) {
	origenID, err := s.repo.BuscarCiudadID(req.Origen, req.OrigenPais)
	if err != nil {
		return nil, err
	}
	if origenID == nil {
		return nil, fmt.Errorf("ciudad origen '%s, %s' no encontrada en catálogo", req.Origen, req.OrigenPais)
	}

	destinoID, err := s.repo.BuscarCiudadID(req.Destino, req.DestinoPais)
	if err != nil {
		return nil, err
	}
	if destinoID == nil {
		return nil, fmt.Errorf("ciudad destino '%s, %s' no encontrada en catálogo", req.Destino, req.DestinoPais)
	}

	proveedores, err := s.repo.ObtenerAerolineasPorRuta(*origenID, *destinoID)
	if err != nil {
		return nil, err
	}
	if len(proveedores) == 0 {
		return []dto.BusquedaVuelosResponse{}, nil
	}

	var resultados []dto.BusquedaVuelosResponse
	for _, p := range proveedores {
		datos, err := s.llamarVuelos(p, req)
		if err != nil {
			resultados = append(resultados, dto.BusquedaVuelosResponse{
				ProveedorID: p.ProveedorID,
				Proveedor:   p.Nombre,
				Error:       err.Error(),
			})
			continue
		}
		resultados = append(resultados, dto.BusquedaVuelosResponse{
			ProveedorID: p.ProveedorID,
			Proveedor:   p.Nombre,
			Datos:       datos,
		})
	}
	return resultados, nil
}

func (s *BusquedaService) llamarVuelos(p dto.ProveedorCatalogo, req dto.BusquedaVuelosRequest) (interface{}, error) {
	body, _ := json.Marshal(req)

	httpReq, err := http.NewRequest(http.MethodPost, p.URLApi+"/api/vuelos-agencia/buscar", bytes.NewBuffer(body))
	if err != nil {
		return nil, err
	}
	httpReq.Header.Set("Content-Type", "application/json")
	httpReq.Header.Set("X-Agencia-Token", p.TokenEntrada)

	resp, err := http.DefaultClient.Do(httpReq)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("aerolinea respondió con status %d", resp.StatusCode)
	}

	var datos interface{}
	if err := json.NewDecoder(resp.Body).Decode(&datos); err != nil {
		return nil, err
	}
	return datos, nil
}

func (s *BusquedaService) BuscarHoteles(req dto.BusquedaHotelesRequest) ([]dto.BusquedaHotelesResponse, error) {
	ciudadID, err := s.repo.BuscarCiudadID(req.Ciudad, req.Pais)
	if err != nil {
		return nil, err
	}
	if ciudadID == nil {
		return nil, fmt.Errorf("ciudad '%s, %s' no encontrada en catálogo", req.Ciudad, req.Pais)
	}

	proveedores, err := s.repo.ObtenerProveedoresPorOrigenYTipo(*ciudadID, 2)
	if err != nil {
		return nil, err
	}
	if len(proveedores) == 0 {
		return []dto.BusquedaHotelesResponse{}, nil
	}

	var resultados []dto.BusquedaHotelesResponse
	for _, p := range proveedores {
		datos, err := s.llamarHoteles(p, req)
		if err != nil {
			resultados = append(resultados, dto.BusquedaHotelesResponse{
				ProveedorID: p.ProveedorID,
				Proveedor:   p.Nombre,
				Error:       err.Error(),
			})
			continue
		}
		resultados = append(resultados, dto.BusquedaHotelesResponse{
			ProveedorID: p.ProveedorID,
			Proveedor:   p.Nombre,
			Datos:       datos,
		})
	}
	return resultados, nil
}

func (s *BusquedaService) llamarHoteles(p dto.ProveedorCatalogo, req dto.BusquedaHotelesRequest) (interface{}, error) {
	body, _ := json.Marshal(req)

	httpReq, err := http.NewRequest(http.MethodPost, p.URLApi+"/agencia/busqueda", bytes.NewBuffer(body))
	if err != nil {
		return nil, err
	}
	httpReq.Header.Set("Content-Type", "application/json")
	httpReq.Header.Set("X-Agencia-Token", p.TokenEntrada)

	resp, err := http.DefaultClient.Do(httpReq)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("hotelera respondió con status %d", resp.StatusCode)
	}

	var datos interface{}
	if err := json.NewDecoder(resp.Body).Decode(&datos); err != nil {
		return nil, err
	}
	return datos, nil
}
