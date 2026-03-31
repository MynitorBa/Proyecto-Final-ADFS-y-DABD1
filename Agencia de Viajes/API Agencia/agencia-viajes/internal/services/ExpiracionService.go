package services

import (
	"agencia-viajes/internal/repositories"
	"database/sql"
	"log"
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
				if err := s.repo.ExpirarReservacionesPendientes(); err != nil {
					log.Println("[EXPIRACION] Error expirando reservaciones:", err)
				} else {
					log.Println("[EXPIRACION] Revisión de expiración completada")
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
