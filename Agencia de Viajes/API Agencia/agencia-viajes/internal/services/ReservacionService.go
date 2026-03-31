package services

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/repositories"
	"database/sql"
	"fmt"
	"strings"
	"time"

	"github.com/google/uuid"
)

type ReservacionService struct {
	repo              *repositories.ReservacionRepository
	expiracionService *ExpiracionService
}

func NewReservacionService(db *sql.DB, expiracionService *ExpiracionService) *ReservacionService {
	return &ReservacionService{
		repo:              repositories.NewReservacionRepository(db),
		expiracionService: expiracionService,
	}
}

func (s *ReservacionService) CrearReservacion(usuarioID, tipoReservaID int) (dto.CrearReservacionResponse, error) {

	// 1. Expirar reservaciones pendientes anteriores del usuario
	if err := s.expiracionService.ExpirarReservacionesDeUsuario(usuarioID); err != nil {
		return dto.CrearReservacionResponse{}, fmt.Errorf("error expirando reservaciones anteriores: %w", err)
	}

	// 2. Crear nueva reservación
	noReservacion := strings.ToUpper(strings.ReplaceAll(uuid.New().String(), "-", "")[:8])
	fechaExpiracion := time.Now().Add(10 * time.Minute).Format("2006-01-02 15:04:05")

	id, err := s.repo.CrearReservacion(usuarioID, tipoReservaID, noReservacion, fechaExpiracion)
	if err != nil {
		return dto.CrearReservacionResponse{}, fmt.Errorf("error creando reservación: %w", err)
	}

	return dto.CrearReservacionResponse{
		ID:              id,
		NoReservacion:   noReservacion,
		EstadoID:        1,
		Estado:          "Pendiente",
		TipoReservaID:   tipoReservaID,
		FechaExpiracion: fechaExpiracion,
		FechaCreacion:   time.Now().Format("2006-01-02 15:04:05"),
	}, nil
}
