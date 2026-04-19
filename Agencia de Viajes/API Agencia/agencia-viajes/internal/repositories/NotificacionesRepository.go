package repositories

import (
	"context"
	"database/sql"
	"fmt"
	"time"
)

// NotificacionesRepository
//
// Repositorio que gestiona la lectura y actualizacion de notificaciones
// del usuario. No tiene relacion con los repositorios de proveedor:
// estos son endpoints del lado del usuario, no del proveedor.
type NotificacionesRepository struct {
	db *sql.DB
}

// NewNotificacionesRepository
//
// Crea e inicializa una nueva instancia del repositorio.
//
// Parametros:
//   - db: conexion activa a la base de datos
//
// Retorna:
//   - *NotificacionesRepository: instancia lista para usar
func NewNotificacionesRepository(db *sql.DB) *NotificacionesRepository {
	return &NotificacionesRepository{db: db}
}

// NotificacionDTO
//
// Representa una notificacion aplanada lista para serializar a JSON.
// Une datos de notificaciones, tipo_notificacion y reservacion en
// una sola estructura para no exponer joins al controller.
type NotificacionDTO struct {
	ID                   int     `json:"id"`
	ReservacionID        int     `json:"reservacion_id"`
	NoReservacion        string  `json:"no_reservacion"`
	DetalleReservacionID *int    `json:"detalle_reservacion_id"` // nullable
	MensajeProveedor     *string `json:"mensaje_proveedor"`      // nullable
	BoleanoLeido         bool    `json:"leido"`
	FechaEmision         string  `json:"fecha_emision"`
	TipoNotificacionID   int     `json:"tipo_notificacion_id"`
	TipoNotificacion     string  `json:"tipo_notificacion"`
}

// ObtenerNotificacionesDeUsuario
//
// Retorna todas las notificaciones de todas las reservaciones que
// pertenecen al usuario indicado, ordenadas de mas reciente a mas antigua.
//
// El JOIN con reservacion garantiza que el usuario solo vea notificaciones
// de sus propias reservaciones, sin necesidad de filtrar por usuario
// directamente en la tabla notificaciones.
//
// Parametros:
//   - usuarioID: ID del usuario autenticado
//
// Retorna:
//   - []NotificacionDTO: slice con todas las notificaciones del usuario
//   - error: si falla la consulta de BD
func (r *NotificacionesRepository) ObtenerNotificacionesDeUsuario(
	usuarioID int,
) ([]NotificacionDTO, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return nil, err
	}
	defer conn.Close()

	rows, err := conn.QueryContext(context.Background(), `
		SELECT
			n.ID,
			n.Reservacion_ID,
			res.No_Reservacion,
			n.Detalle_Reservacion_ID,
			n.Mensaje_Proveedor,
			n.Boleano_Leido,
			n.Fecha_Emision,
			n.Tipo_Notificacion_ID,
			tn.tipo_Notificacion
		FROM notificaciones n
		JOIN reservacion   res ON n.Reservacion_ID       = res.ID
		JOIN tipo_notificacion tn ON n.Tipo_Notificacion_ID = tn.ID
		WHERE res.Usuario_ID = ?
		ORDER BY n.Fecha_Emision DESC
	`, usuarioID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	notificaciones := []NotificacionDTO{}
	for rows.Next() {
		var n NotificacionDTO
		var fechaRaw time.Time
		if err := rows.Scan(
			&n.ID,
			&n.ReservacionID,
			&n.NoReservacion,
			&n.DetalleReservacionID,
			&n.MensajeProveedor,
			&n.BoleanoLeido,
			&fechaRaw,
			&n.TipoNotificacionID,
			&n.TipoNotificacion,
		); err != nil {
			return nil, err
		}
		n.FechaEmision = fechaRaw.Format("2006-01-02 15:04:05")
		notificaciones = append(notificaciones, n)
	}
	return notificaciones, nil
}

// MarcarComoLeida
//
// Cambia el estado Boleano_Leido de una notificacion de false a true.
// Verifica que la notificacion pertenezca al usuario autenticado antes
// de actualizar, para evitar que un usuario marque notificaciones ajenas.
//
// Parametros:
//   - notificacionID: ID de la notificacion a marcar como leida
//   - usuarioID:      ID del usuario autenticado
//
// Retorna:
//   - error: si la notificacion no existe, no pertenece al usuario o falla la BD
func (r *NotificacionesRepository) MarcarComoLeida(
	notificacionID, usuarioID int,
) error {
	result, err := r.db.ExecContext(context.Background(), `
		UPDATE notificaciones n
		JOIN reservacion res ON n.Reservacion_ID = res.ID
		SET n.Boleano_Leido = 1
		WHERE n.ID = ?
		  AND res.Usuario_ID = ?
		  AND n.Boleano_Leido = 0
	`, notificacionID, usuarioID)
	if err != nil {
		return err
	}

	// Verificar que realmente se actualizo una fila:
	// si rowsAffected = 0 puede ser que no exista, no pertenezca al usuario
	// o ya estaba marcada como leida
	rows, err := result.RowsAffected()
	if err != nil {
		return err
	}
	if rows == 0 {
		return fmt.Errorf("notificacion no encontrada, no pertenece al usuario o ya estaba leida")
	}
	return nil
}