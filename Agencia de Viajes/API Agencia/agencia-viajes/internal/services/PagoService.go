package services

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/repositories"
	"bytes"
	"encoding/json"
	"errors"
	"fmt"
	"net/http"
)

type PagoService struct {
	repo        *repositories.PagoRepository
	reservaRepo *repositories.ReservacionRepository
}

func NewPagoService(repo *repositories.PagoRepository, rr *repositories.ReservacionRepository) *PagoService {
	return &PagoService{repo: repo, reservaRepo: rr}
}

func (s *PagoService) ProcesarPago(usuarioID int, req dto.PagoReservacionRequest) error {
	// 1. Validar Tarjeta 
	if len(req.TarjetaNumero) < 16 {
		return errors.New("número de tarjeta inválido")
	}
	if len(req.TarjetaCVV) != 3 {
		return errors.New("CVV inválido")
	}

	// 2. Verificar existencia, dueño y estado pendiente
	tipoReserva, total, err := s.repo.ObtenerReservaParaPago(req.ReservacionID, usuarioID)
	if err != nil {
		return errors.New("reservación no encontrada, ya pagada o no le pertenece")
	}

	// 3. Validar integridad de detalles (1=Vuelo, 2=Hotel, 3=Paquete)
	vuelos, hoteles, _ := s.repo.ContarDetallesPorTipo(req.ReservacionID)

	switch tipoReserva {
	case 1: // Aerolínea
		if vuelos != 1 || hoteles != 0 {
			return errors.New("la reserva debe tener exactamente 1 vuelo")
		}
	case 2: // Hotelera
		if hoteles != 1 || vuelos != 0 {
			return errors.New("la reserva debe tener exactamente 1 hotel")
		}
	case 3: // Paquete
		if vuelos != 1 || hoteles != 1 {
			return errors.New("el paquete debe tener exactamente 1 vuelo y 1 hotel")
		}
	}

	// 4. Notificar a Proveedores
	detalles, _ := s.reservaRepo.ObtenerDetallesDeReservacion(req.ReservacionID)
	for _, d := range detalles {
		err := s.notificarProveedor(d, req.Nit, req.CodigoPostal)
		if err != nil {
			return fmt.Errorf("error al confirmar con proveedor: %w", err)
		}
	}

	// 5. Finalizar en BD Agencia (Estado 2 y Factura)
	return s.repo.ConfirmarReservaYFacturar(req.ReservacionID, total, req.Nit, req.CodigoPostal)
}

func (s *PagoService) notificarProveedor(d dto.DetalleProveedor, nit, cp string) error {
	body, _ := json.Marshal(dto.PagoProveedorBody{Nit: nit, CodigoPostal: cp})

	var url string
	if d.TipoDetalleID == 1 { // Aerolínea
		url = fmt.Sprintf("%s/api/reservaciones-agencia/%s/confirmar", d.URLAPI, d.IDReservaProveedor)
	} else { // Hotelera
		url = fmt.Sprintf("%s/agencia/reservaciones/%s/pago", d.URLAPI, d.IDReservaProveedor)
	}

	req, _ := http.NewRequest("POST", url, bytes.NewBuffer(body))
	req.Header.Set("X-Agencia-Token", d.TokenEntrada)
	req.Header.Set("Content-Type", "application/json")

	resp, err := http.DefaultClient.Do(req)
	if err != nil || resp.StatusCode >= 400 {
		return errors.New("el proveedor rechazó el pago")
	}
	return nil
}
