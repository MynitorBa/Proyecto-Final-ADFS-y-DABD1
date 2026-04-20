// # Package repositories
//
// Repositorios de acceso a datos para la agencia de viajes.
// Este paquete centraliza todas las consultas a la base de datos
// utilizadas por los servicios de la aplicacion.
package repositories

import (
	"context"
	"database/sql"
	"time"
)

// WebServiceRepository
//
// Repositorio encargado de las consultas necesarias para el panel
// operacional del WebService: estado de proveedores con flag de
// handshake, conteo de eventos recientes y listado de notificaciones
// generadas por proveedores en todas las reservaciones del sistema.
type WebServiceRepository struct {
	db *sql.DB
}

// NewWebServiceRepository
//
// Crea e inicializa una nueva instancia de WebServiceRepository.
//
// Parametros:
//   - db: conexion activa a la base de datos
//
// Retorna:
//   - *WebServiceRepository: instancia lista para usar
func NewWebServiceRepository(db *sql.DB) *WebServiceRepository {
	return &WebServiceRepository{db: db}
}

// ProveedorWS
//
// Datos de un proveedor enriquecidos con el flag handshake_configurado,
// derivado de si Token_HASH_Salida esta poblado. Solo se usa en el
// panel operacional del WebService, no en el panel de administracion general.
type ProveedorWS struct {
	ID                   int     `json:"id"`
	Nombre               string  `json:"nombre"`
	TipoProveedorID      int     `json:"tipo_proveedor_id"`
	TipoNombre           string  `json:"tipo_nombre"`
	URL                  string  `json:"url"`
	Activo               bool    `json:"activo"`
	HandshakeConfigurado bool    `json:"handshake_configurado"`
	PorcentajeGanancia   float64 `json:"porcentaje_ganancia"`
}

// EventosWS
//
// Conteo acumulado de eventos de handshake y actualizacion de catalogo
// registrados en log_sesion. Se usa en las tarjetas de resumen del panel.
type EventosWS struct {
	HandshakeExitosos int `json:"handshake_exitosos"`
	HandshakeFallidos int `json:"handshake_fallidos"`
	CatalogoExitosos  int `json:"catalogo_exitosos"`
	CatalogoFallidos  int `json:"catalogo_fallidos"`
}

// ObtenerProveedores
//
// Retorna todos los proveedores registrados incluyendo el flag
// handshake_configurado derivado de si Token_HASH_Salida esta poblado.
//
// Retorna:
//   - []ProveedorWS: slice con todos los proveedores
//   - error: si falla la consulta de BD
func (r *WebServiceRepository) ObtenerProveedores() ([]ProveedorWS, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return nil, err
	}
	defer conn.Close()

	rows, err := conn.QueryContext(context.Background(), `
		SELECT
			ID,
			Nombre,
			Tipo_Proveedor_ID,
			URL_API,
			EstadoID,
			(Token_HASH_Salida != '') AS handshake_configurado,
			Porcentaje_Ganancia
		FROM Proveedor
		ORDER BY ID
	`)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	tipoNombre := func(id int) string {
		if id == 1 {
			return "Aerolínea"
		}
		return "Hotel"
	}

	lista := []ProveedorWS{}
	for rows.Next() {
		var p ProveedorWS
		var estadoID, handshakeInt int
		if err := rows.Scan(
			&p.ID, &p.Nombre, &p.TipoProveedorID,
			&p.URL, &estadoID, &handshakeInt, &p.PorcentajeGanancia,
		); err != nil {
			continue
		}
		p.Activo = estadoID == 1
		p.HandshakeConfigurado = handshakeInt == 1
		p.TipoNombre = tipoNombre(p.TipoProveedorID)
		lista = append(lista, p)
	}
	return lista, nil
}

// ObtenerEventosRecientes
//
// Cuenta los eventos de handshake (IDs 35 y 36) y actualizacion de
// catalogo (IDs 37 y 38) registrados en log_sesion, para mostrar
// en las tarjetas de resumen del panel WebService.
//
// Retorna:
//   - EventosWS: conteo agrupado por tipo de evento
//   - error: si falla la consulta de BD
func (r *WebServiceRepository) ObtenerEventosRecientes() (EventosWS, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return EventosWS{}, err
	}
	defer conn.Close()

	rows, err := conn.QueryContext(context.Background(), `
		SELECT Tipo_Evento_ID, COUNT(*) AS total
		FROM log_sesion
		WHERE Tipo_Evento_ID IN (35, 36, 37, 38)
		GROUP BY Tipo_Evento_ID
	`)
	if err != nil {
		return EventosWS{}, err
	}
	defer rows.Close()

	var ev EventosWS
	for rows.Next() {
		var tipoID, total int
		if err := rows.Scan(&tipoID, &total); err != nil {
			continue
		}
		switch tipoID {
		case 35:
			ev.HandshakeExitosos = total
		case 36:
			ev.HandshakeFallidos = total
		case 37:
			ev.CatalogoExitosos = total
		case 38:
			ev.CatalogoFallidos = total
		}
	}
	return ev, nil
}

// ObtenerNotificaciones
//
// Retorna las ultimas 50 notificaciones generadas por proveedores en
// todas las reservaciones del sistema, sin filtrar por usuario. Se usa
// en el feed de actividad del panel operacional del WebService.
//
// Retorna:
//   - []NotificacionDTO: slice con las notificaciones mas recientes
//   - error: si falla la consulta de BD
func (r *WebServiceRepository) ObtenerNotificaciones() ([]NotificacionDTO, error) {
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
		JOIN reservacion      res ON n.Reservacion_ID        = res.ID
		JOIN tipo_notificacion tn ON n.Tipo_Notificacion_ID  = tn.ID
		ORDER BY n.Fecha_Emision DESC
		LIMIT 50
	`)
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
			continue
		}
		n.FechaEmision = fechaRaw.Format("2006-01-02 15:04:05")
		notificaciones = append(notificaciones, n)
	}
	return notificaciones, nil
}
