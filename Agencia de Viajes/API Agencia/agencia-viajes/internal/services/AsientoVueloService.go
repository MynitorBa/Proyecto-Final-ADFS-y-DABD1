package services

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/repositories"
	"bytes"
	"database/sql"
	"encoding/json"
	"errors"
	"fmt"
	"net/http"
)

type AsientoVueloService struct {
	repo *repositories.DetalleReservacionRepository
}

func NewAsientoVueloService(db *sql.DB) *AsientoVueloService {
	return &AsientoVueloService{
		repo: repositories.NewDetalleReservacionRepository(db),
	}
}

func (s *AsientoVueloService) ObtenerAsientosVuelo(
	usuarioID int,
	req dto.ObtenerAsientosVueloRequest,
) (*dto.AsientosVueloResponse, error) {

	idReservaAerolinea, urlAPI, token, err := s.repo.ObtenerDetalleAerolineaPorProveedor(
		req.ReservacionID, usuarioID, req.ProveedorID,
	)
	if err != nil {
		return nil, err
	}

	return s.llamarGetAsientos(urlAPI, token, idReservaAerolinea)
}

func (s *AsientoVueloService) CambiarAsientoVuelo(
	usuarioID int,
	req dto.CambiarAsientoVueloRequest,
) error {

	idReservaAerolinea, urlAPI, token, err := s.repo.ObtenerDetalleAerolineaPorProveedor(
		req.ReservacionID, usuarioID, req.ProveedorID,
	)
	if err != nil {
		return err
	}

	asientos, err := s.llamarGetAsientos(urlAPI, token, idReservaAerolinea)
	if err != nil {
		return err
	}

	boletoValido := false
	for _, vuelo := range *asientos {
		for _, b := range vuelo.BoletosAgencia {
			if b.BoletoID == req.BoletoID {
				boletoValido = true
				break
			}
		}
		if boletoValido {
			break
		}
	}

	if !boletoValido {
		return errors.New("el boleto no pertenece a esta reservación")
	}

	return s.llamarCambiarAsiento(urlAPI, token, req.BoletoID, req.NuevoAsiento)
}

func (s *AsientoVueloService) llamarGetAsientos(
	urlAPI, token, idReservaProveedor string,
) (*dto.AsientosVueloResponse, error) {

	url := fmt.Sprintf("%s/api/asientos-agencia/reservacion/%s", urlAPI, idReservaProveedor)

	req, err := http.NewRequest(http.MethodGet, url, nil)
	if err != nil {
		return nil, err
	}
	req.Header.Set("X-Agencia-Token", token)

	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("error contactando aerolínea: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("aerolínea respondió con status %d", resp.StatusCode)
	}

	var resultado dto.AsientosVueloResponse
	if err := json.NewDecoder(resp.Body).Decode(&resultado); err != nil {
		return nil, errors.New("el formato de respuesta de la aerolínea es incompatible")
	}
	return &resultado, nil
}

func (s *AsientoVueloService) llamarCambiarAsiento(
	urlAPI, token string,
	boletoID int,
	nuevoAsiento string,
) error {

	bodyReq := dto.CambiarAsientoAerolineaBody{NuevoAsiento: nuevoAsiento}
	bodyBytes, err := json.Marshal(bodyReq)
	if err != nil {
		return fmt.Errorf("error serializando request: %w", err)
	}

	url := fmt.Sprintf("%s/api/asientos-agencia/%d", urlAPI, boletoID)

	req, err := http.NewRequest(http.MethodPut, url, bytes.NewBuffer(bodyBytes))
	if err != nil {
		return err
	}
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("X-Agencia-Token", token)

	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		return fmt.Errorf("error contactando aerolínea: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK && resp.StatusCode != http.StatusNoContent {
		var errResp map[string]interface{}
		json.NewDecoder(resp.Body).Decode(&errResp)
		if msg, ok := errResp["message"].(string); ok {
			return fmt.Errorf("aerolínea rechazó el cambio: %s", msg)
		}
		return fmt.Errorf("aerolínea respondió con status %d", resp.StatusCode)
	}

	return nil
}
