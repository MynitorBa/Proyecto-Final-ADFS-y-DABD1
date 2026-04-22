// # Package controllers
//
// Controladores HTTP de la agencia de viajes. Cada controlador recibe
// solicitudes de Gin, delega la logica de negocio al servicio correspondiente
// y devuelve la respuesta JSON al cliente.
package controllers

import (
	"agencia-viajes/internal/helpers"
	"agencia-viajes/internal/services"
	"fmt"
	"net/http"
	"strconv"
	"strings"

	"github.com/gin-gonic/gin"
)

// AdminCancelacionController
//
// Controlador encargado de gestionar la cancelacion de reservaciones
// iniciada por un administrador desde el panel de administracion.
// A diferencia del CancelacionController, no requiere que la reservacion
// pertenezca al usuario autenticado: el administrador puede cancelar
// cualquier reservacion activa del sistema.
type AdminCancelacionController struct {
	service   *services.AdminCancelacionService
	logSesion *services.LogSesionService
}

// NewAdminCancelacionController
//
// Crea e inicializa un nuevo AdminCancelacionController con los servicios recibidos.
//
// Parametros:
//   - s:         instancia del servicio de cancelacion administrativa
//   - logSesion: instancia del servicio de auditoria de sesion
//
// Retorna:
//   - *AdminCancelacionController: puntero al controlador creado
func NewAdminCancelacionController(s *services.AdminCancelacionService, logSesion *services.LogSesionService) *AdminCancelacionController {
	return &AdminCancelacionController{service: s, logSesion: logSesion}
}

// Cancelar
//
// Handler HTTP que permite a un administrador cancelar cualquier reservacion
// activa del sistema. El motivo es obligatorio. Se registra un evento de
// auditoria con TipoCancelacionAdmin (62) si fue exitosa, o
// TipoCancelacionFallida (31) si falla.
//
// El correo de notificacion al usuario se envia de forma asincrona (goroutine)
// desde el servicio, por lo que no bloquea la respuesta HTTP.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200: JSON con mensaje de confirmacion y numero de reservacion cancelada
//   - HTTP 400: ID invalido o motivo vacio
//   - HTTP 401: administrador no autenticado
//   - HTTP 404: reservacion no encontrada
//   - HTTP 409: reservacion ya no es cancelable (estado incorrecto)
//   - HTTP 500: error interno al comunicarse con proveedores o BD
//
// Notas:
//   - Ruta esperada: POST /api/admin/reservaciones/:id/cancelar
//   - Requiere rol administrador (RolRequerido(helpers.RolAdmin) aplicado en el router)
func (ctrl *AdminCancelacionController) Cancelar(c *gin.Context) {
	adminIDRaw, exists := c.Get("usuario_id")
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "administrador no autenticado"})
		return
	}
	adminID := adminIDRaw.(int)

	reservacionID, err := strconv.Atoi(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "ID de reservacion invalido"})
		return
	}

	var req struct {
		Motivo string `json:"motivo"`
	}
	if err := c.ShouldBindJSON(&req); err != nil || strings.TrimSpace(req.Motivo) == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "el campo motivo es obligatorio"})
		return
	}

	noReservacion, err := ctrl.service.CancelarReservacion(reservacionID, req.Motivo)
	if err != nil {
		errMsg := err.Error()

		// Distinguir entre "no encontrada" y "estado incorrecto" para devolver
		// el codigo HTTP adecuado
		if strings.Contains(errMsg, "no encontrada") {
			c.JSON(http.StatusNotFound, gin.H{"error": errMsg})
			return
		}
		if strings.Contains(errMsg, "no puede cancelarse") {
			c.JSON(http.StatusConflict, gin.H{"error": errMsg})
			return
		}

		// Log de cancelacion fallida (ID 31)
		ctrl.logSesion.Registrar(c, helpers.TipoCancelacionFallida,
			&adminID,
			fmt.Sprintf("reserva_id=%d", reservacionID),
			fmt.Sprintf("Admin (ID=%d) intento cancelar reserva %d y fallo: %s", adminID, reservacionID, errMsg),
		)
		c.JSON(http.StatusInternalServerError, gin.H{"error": errMsg})
		return
	}

	// Log de cancelacion administrativa exitosa (ID 62)
	ctrl.logSesion.Registrar(c, helpers.TipoCancelacionAdmin,
		&adminID,
		noReservacion,
		fmt.Sprintf("Admin (ID=%d) cancelo reserva %s — motivo: %s", adminID, noReservacion, req.Motivo),
	)

	c.JSON(http.StatusOK, gin.H{
		"mensaje":       "Reservacion cancelada por administrador",
		"noReservacion": noReservacion,
	})
}
