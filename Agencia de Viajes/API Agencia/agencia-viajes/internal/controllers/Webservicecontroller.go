package controllers

import (
	"context"
	"database/sql"
	"net/http"

	"github.com/gin-gonic/gin"
)

type WebserviceController struct {
	db *sql.DB
}

func NewWebserviceController(db *sql.DB) *WebserviceController {
	return &WebserviceController{db: db}
}

// POST /api/webservice/notificacion
// Header: X-Proveedor-Token: <token>
// Autenticado por middleware ProveedorRequerido
//
// Body:
// {
//   "reservacionProveedorId": "123",   // ID de la reserva en el sistema del proveedor
//   "nuevoEstado": "cancelada",         // cancelada | confirmada | completada | en curso
//   "motivo": "El vuelo fue cancelado"  // requerido si nuevoEstado = cancelada
// }
func (ctrl *WebserviceController) RecibirNotificacion(c *gin.Context) {
	// ── Identidad del proveedor (inyectada por ProveedorRequerido) ────────
	proveedorID := c.GetInt("proveedor_id")

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

	// ── Si es cancelación, guardar motivo en parametros_json del detalle ─
	if req.NuevoEstado == "cancelada" && req.Motivo != "" {
		conn.ExecContext(context.Background(), `
			UPDATE detalles_reservacion
			SET Parametros_JSON = JSON_SET(
				COALESCE(Parametros_JSON, '{}'),
				'$.motivoCancelacion', ?
			)
			WHERE ID_Reserva_Proveedor = ? AND Proveedor_ID = ?
		`, req.Motivo, req.ReservacionProveedorID, proveedorID)
	}

	c.JSON(http.StatusOK, gin.H{
		"mensaje":        "notificación procesada correctamente",
		"reservacion_id": reservacionID,
		"nuevo_estado":   req.NuevoEstado,
	})
}