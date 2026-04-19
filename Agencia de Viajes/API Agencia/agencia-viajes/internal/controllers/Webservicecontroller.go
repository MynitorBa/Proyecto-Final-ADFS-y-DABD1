// # Package controllers
//
// Controladores HTTP de la API de Movent. Cada controlador agrupa los handlers
// relacionados a un recurso o dominio especifico de la aplicacion.
package controllers

import (
	"agencia-viajes/internal/helpers"
	"agencia-viajes/internal/services"
	"context"
	"database/sql"
	"fmt"
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
)

// WebserviceController
//
// Controlador que maneja los endpoints del webservice externo,
// permitiendo a proveedores autenticados notificar cambios de estado
// sobre reservaciones mediante un token de proveedor.
type WebserviceController struct {
	db        *sql.DB
	logSesion *services.LogSesionService
}

// NewWebserviceController
//
// Constructor que retorna una nueva instancia de WebserviceController
// con la conexion a la base de datos y el servicio de auditoria inyectados.
//
// Parametros:
//   - db: puntero a la conexion de base de datos SQL
//   - logSesion: instancia del servicio de auditoria de sesion
//
// Retorna:
//   - *WebserviceController: puntero a la nueva instancia
func NewWebserviceController(db *sql.DB, logSesion *services.LogSesionService) *WebserviceController {
	return &WebserviceController{db: db, logSesion: logSesion}
}

// RecibirNotificacion
//
// Recibe una notificacion de cambio de estado desde un proveedor externo
// autenticado via header X-Proveedor-Token. Busca la reservacion interna
// usando el ID de reserva del proveedor y actualiza su estado en la base de datos.
// Si el nuevo estado es cancelada, guarda el motivo en el campo parametros_json
// del detalle de la reservacion y registra el evento CANCELACION_PROVEEDOR (30).
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200 OK: JSON con mensaje de confirmacion, reservacion_id y nuevo_estado
//   - HTTP 400 Bad Request: si el body es invalido, el estado no es reconocido,
//     o falta el motivo en una cancelacion
//   - HTTP 404 Not Found: si no se encuentra la reservacion para el proveedor indicado
//   - HTTP 500 Internal Server Error: si ocurre un error de conexion o al actualizar
//
// Notas:
//   - La identidad del proveedor (proveedor_id, proveedor_nombre) es inyectada por el middleware ProveedorRequerido
//   - Los estados validos son: cancelada, confirmada, completada, en curso
//   - El motivo es obligatorio cuando nuevoEstado es cancelada
func (ctrl *WebserviceController) RecibirNotificacion(c *gin.Context) {
	// ── Identidad del proveedor (inyectada por ProveedorRequerido) ────────
	proveedorID := c.GetInt("proveedor_id")
	proveedorNombre, _ := c.Get("proveedor_nombre")
	provStr, _ := proveedorNombre.(string)

	// ── Body ─────────────────────────────────────────────────────────────
	var req struct {
		ReservacionProveedorID string `json:"reservacionProveedorId" binding:"required"`
		NuevoEstado            string `json:"nuevoEstado"            binding:"required"`
		Motivo                 string `json:"motivo"`
	}
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "datos inválidos: " + err.Error()})
		return
	}

	// ── Validar estado recibido ───────────────────────────────────────────
	estadoMap := map[string]int{
		"cancelada":  3,
		"confirmada": 2,
		"completada": 5,
		"en curso":   6,
	}
	nuevoEstadoID, ok := estadoMap[req.NuevoEstado]
	if !ok {
		c.JSON(http.StatusBadRequest, gin.H{
			"error": "nuevoEstado inválido. Valores aceptados: cancelada, confirmada, completada, en curso",
		})
		return
	}
	if req.NuevoEstado == "cancelada" && req.Motivo == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "motivo es requerido para cancelaciones"})
		return
	}

	conn, err := ctrl.db.Conn(context.Background())
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "error de conexión"})
		return
	}
	defer conn.Close()

	// ── Buscar la reservación en detalles_reservacion ─────────────────────
	// El proveedor envía su propio ID de reserva (guardado en ID_Reserva_Proveedor)
	var reservacionID int
	err = conn.QueryRowContext(context.Background(), `
		SELECT Reservacion_ID
		FROM detalles_reservacion
		WHERE ID_Reserva_Proveedor = ? AND Proveedor_ID = ?
		LIMIT 1
	`, req.ReservacionProveedorID, proveedorID).Scan(&reservacionID)

	if err == sql.ErrNoRows {
		c.JSON(http.StatusNotFound, gin.H{
			"error": "reservación no encontrada para este proveedor",
		})
		return
	}
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "error consultando reservación"})
		return
	}

	// ── Actualizar estado en Reservacion ──────────────────────────────────
	_, err = conn.ExecContext(context.Background(),
		"UPDATE Reservacion SET EstadoID = ? WHERE ID = ?",
		nuevoEstadoID, reservacionID,
	)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "error actualizando estado"})
		return
	}

	// ── Si es cancelación, guardar motivo y registrar log de auditoria ────
	if strings.ToLower(req.NuevoEstado) == "cancelada" {
		// Guardar motivo en parametros_json del detalle
		if req.Motivo != "" {
			conn.ExecContext(context.Background(), `
				UPDATE detalles_reservacion
				SET Parametros_JSON = JSON_SET(
					COALESCE(Parametros_JSON, '{}'),
					'$.motivoCancelacion', ?
				)
				WHERE ID_Reserva_Proveedor = ? AND Proveedor_ID = ?
			`, req.Motivo, req.ReservacionProveedorID, proveedorID)
		}

		// Obtener usuario_id y no_reservacion para el log de auditoria
		var usuarioID int
		var noReservacion string
		_ = ctrl.db.QueryRow(
			`SELECT Usuario_ID, No_Reservacion FROM Reservacion WHERE ID = ?`,
			reservacionID,
		).Scan(&usuarioID, &noReservacion)

		// Log ID 30: CANCELACION_PROVEEDOR
		uid := usuarioID
		mensaje := fmt.Sprintf("Proveedor %s canceló reserva %s", provStr, noReservacion)
		if req.Motivo != "" {
			mensaje += fmt.Sprintf(" — motivo: %s", req.Motivo)
		}
		ctrl.logSesion.RegistrarSistema(
			helpers.TipoCancelacionProveedor,
			&uid,
			noReservacion,
			mensaje,
			fmt.Sprintf("WebserviceController:%s", provStr),
		)
	}

	c.JSON(http.StatusOK, gin.H{
		"mensaje":        "notificación procesada correctamente",
		"reservacion_id": reservacionID,
		"nuevo_estado":   req.NuevoEstado,
	})
}
