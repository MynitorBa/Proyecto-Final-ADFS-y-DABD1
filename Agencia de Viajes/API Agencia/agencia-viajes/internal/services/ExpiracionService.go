package services

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/repositories"
	"bytes"
	"database/sql"
	"fmt"
	"log"
	"net/http"
	"time"
)

type ExpiracionService struct {
	repo   *repositories.ReservacionRepository
	stopCh chan struct{}
}

func NewExpiracionService(db *sql.DB) *ExpiracionService {
	return &ExpiracionService{
		repo:   repositories.NewReservacionRepository(db),
		stopCh: make(chan struct{}),
	}
}

func (s *ExpiracionService) Iniciar() {
	go func() {
		ticker := time.NewTicker(1 * time.Minute)
		defer ticker.Stop()
		for {
			select {
			case <-ticker.C:
				if err := s.expirarPendientes(); err != nil {
					log.Println("[EXPIRACION] Error:", err)
				} else {
					log.Println("[EXPIRACION] Revisión completada")
				}
			case <-s.stopCh:
				log.Println("[EXPIRACION] Servicio detenido")
				return
			}
		}
	}()
}

func (s *ExpiracionService) Detener() {
	close(s.stopCh)
}

func (s *ExpiracionService) ExpirarReservacionesDeUsuario(usuarioID int) error {
	pendientes, err := s.repo.ObtenerPendientesConDetalles(usuarioID)
	if err != nil {
		return err
	}
	for _, res := range pendientes {
		if err := s.expirarUna(res.ID, res.Detalles); err != nil {
			log.Printf("[EXPIRACION] Error expirando reservacion %d del usuario %d: %v", res.ID, usuarioID, err)
		}
	}
	return nil
}

func (s *ExpiracionService) expirarPendientes() error {
	ids, err := s.repo.ObtenerIDsPendientesExpirados()
	if err != nil {
		return err
	}
	for _, id := range ids {
		detalles, err := s.repo.ObtenerDetallesDeReservacion(id)
		if err != nil {
			log.Printf("[EXPIRACION] Error obteniendo detalles de reservacion %d: %v", id, err)
			continue
		}
		if err := s.expirarUna(id, detalles); err != nil {
			log.Printf("[EXPIRACION] Error expirando reservacion %d: %v", id, err)
		}
	}
	return nil
}

func (s *ExpiracionService) expirarUna(reservacionID int, detalles []dto.DetalleProveedor) error {
	for _, d := range detalles {
		if err := s.llamarExpirarProveedor(d.URLAPI, d.TokenEntrada, d.IDReservaProveedor, d.TipoDetalleID); err != nil {
			log.Printf("[EXPIRACION] Error expirando en proveedor %d reserva %s: %v", d.ProveedorID, d.IDReservaProveedor, err)
		}
	}

	if err := s.repo.ExpirarDetalles(reservacionID); err != nil {
		log.Printf("[EXPIRACION] Error expirando detalles de reservacion %d: %v", reservacionID, err)
		return err
	}

	if err := s.repo.ExpirarReservacion(reservacionID); err != nil {
		log.Printf("[EXPIRACION] Error expirando reservacion %d: %v", reservacionID, err)
		return err
	}

	return nil
}

func (s *ExpiracionService) llamarExpirarProveedor(urlAPI, token, idReservaProveedor string, tipoDetalleID int) error {
	var url string
	switch tipoDetalleID {
	case TipoDetalleVuelo:
		url = fmt.Sprintf("%s/api/reservaciones-agencia/%s/expirar", urlAPI, idReservaProveedor)
	case TipoDetalleHotel:
		url = fmt.Sprintf("%s/agencia/reservaciones/%s/expirar", urlAPI, idReservaProveedor)
	default:
		return fmt.Errorf("tipo de detalle desconocido: %d", tipoDetalleID)
	}

	req, err := http.NewRequest(http.MethodPost, url, bytes.NewBuffer(nil))
	if err != nil {
		return err
	}
	req.Header.Set("X-Agencia-Token", token)

	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		return err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("proveedor respondió con status %d", resp.StatusCode)
	}
	return nil
}
