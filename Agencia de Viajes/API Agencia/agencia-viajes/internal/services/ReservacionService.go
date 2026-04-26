// # Package services
//
// Contiene los servicios de negocio de la agencia de viajes,
// incluyendo procesamiento de pagos, reservaciones, proveedores y usuarios.
package services

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/repositories"
	"database/sql"
	"fmt"
	"time"
)

// ReservacionService
//
// Servicio responsable de la creacion de nuevas reservaciones de viaje.
// Coordina la expiracion de reservaciones pendientes anteriores del usuario
// y la generacion de numeros de reservacion unicos con fecha de expiracion.
type ReservacionService struct {
	repo              *repositories.ReservacionRepository
	expiracionService *ExpiracionService
}

// NewReservacionService
//
// Crea e inicializa una nueva instancia de ReservacionService con sus dependencias.
//
// Parametros:
//   - db: conexion activa a la base de datos SQL
//   - expiracionService: servicio que gestiona la expiracion de reservaciones pendientes
//
// Retorna:
//   - *ReservacionService: instancia inicializada del servicio de reservaciones
func NewReservacionService(db *sql.DB, expiracionService *ExpiracionService) *ReservacionService {
	return &ReservacionService{
		repo:              repositories.NewReservacionRepository(db),
		expiracionService: expiracionService,
	}
}

// CrearReservacion
//
// Crea una nueva reservacion para el usuario especificado. Primero expira cualquier
// reservacion pendiente anterior del mismo usuario, luego genera un numero de reservacion
// unico de 8 caracteres en mayusculas y calcula una fecha de expiracion de 10 minutos
// desde el momento de la creacion.
//
// Parametros:
//   - usuarioID: identificador del usuario que crea la reservacion
//   - tipoReservaID: tipo de reserva (1=Aerolinea, 2=Hotel, 3=Paquete)
//
// Retorna:
//   - dto.CrearReservacionResponse: datos de la reservacion creada incluyendo numero, estado y fechas
//   - error: error si falla la expiracion de reservaciones anteriores o la creacion en base de datos
func (s *ReservacionService) CrearReservacion(usuarioID, tipoReservaID int) (dto.CrearReservacionResponse, error) {

	// 1. Expirar reservaciones pendientes anteriores del usuario
	if err := s.expiracionService.ExpirarReservacionesDeUsuario(usuarioID); err != nil {
		return dto.CrearReservacionResponse{}, fmt.Errorf("error expirando reservaciones anteriores: %w", err)
	}

	// 2. El SP genera el código, calcula expiración e inserta
	id, noReservacion, fechaExpiracion, err := s.repo.CrearReservacion(usuarioID, tipoReservaID)
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
