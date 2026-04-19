// # Package controllers
//
// Controladores HTTP de la agencia de viajes. Cada controlador recibe
// solicitudes de Gin, delega la logica de negocio al servicio correspondiente
// y devuelve la respuesta JSON al cliente.
package controllers

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/helpers"
	"agencia-viajes/internal/services"
	"fmt"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
)

// CancelacionController
//
// Controlador encargado de gestionar la verificacion y ejecucion de
// cancelaciones de reservaciones realizadas por el usuario.
type CancelacionController struct {
	service   *services.CancelacionService
	logSesion *services.LogSesionService
}

// NewCancelacionController
//
// Crea e inicializa un nuevo CancelacionController con los servicios recibidos.
//
// Parametros:
//   - s: instancia del servicio de cancelacion
//   - logSesion: instancia del servicio de auditoria de sesion
//
// Retorna:
//   - *CancelacionController: puntero al controlador creado
func NewCancelacionController(s *services.CancelacionService, logSesion *services.LogSesionService) *CancelacionController {
	return &CancelacionController{service: s, logSesion: logSesion}
}

// Verificar
//
// Handler HTTP que verifica si una reservacion puede ser cancelada, devolviendo
// las condiciones y penalizaciones aplicables antes de confirmar la cancelacion.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200: resultado de la verificacion con condiciones de cancelacion
//   - HTTP 400: error si el parametro de ruta ID no es un entero valido
//   - HTTP 401: error si el middleware no inyecto usuario_id
//   - HTTP 404: error si la reservacion no existe o no pertenece al usuario
//
// Notas:
//   - Ruta esperada: GET /api/reservaciones/:id/cancelar/verificar
func (ctrl *CancelacionController) Verificar(c *gin.Context) {
	usuarioIDRaw, exists := c.Get("usuario_id")
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{"mensaje": "No autenticado"})
		return
	}
	usuarioID := usuarioIDRaw.(int)

	reservacionID, err := strconv.Atoi(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "ID de reservación inválido"})
		return
	}

	resultado, err := ctrl.service.VerificarCancelacion(reservacionID, usuarioID)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, resultado)
}

// Cancelar
//
// Handler HTTP que ejecuta la cancelacion de una reservacion existente.
// Registra un evento de auditoria segun el resultado: CANCELACION_USUARIO (29)
// si fue exitosa, CANCELACION_FALLIDA (31) si el servicio la rechaza.
// El motivo de cancelacion es opcional.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200: mensaje confirmando que la reservacion fue cancelada exitosamente
//   - HTTP 400: error si el parametro de ruta ID no es un entero valido
//   - HTTP 401: error si el middleware no inyecto usuario_id
//   - HTTP 422: error si el servicio no puede procesar la cancelacion
//
// Notas:
//   - Ruta esperada: POST /api/reservaciones/:id/cancelar
func (ctrl *CancelacionController) Cancelar(c *gin.Context) {
	idStr := c.Param("id")
	reservacionID, err := strconv.Atoi(idStr)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"mensaje": "ID inválido"})
		return
	}

	// FIX: verificar existencia del usuario_id (antes causaba panic)
	usuarioIDRaw, exists := c.Get("usuario_id")
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{"mensaje": "No autenticado"})
		return
	}
	usuarioID := usuarioIDRaw.(int)

	var req dto.CancelarReservacionRequest
	_ = c.ShouldBindJSON(&req) // motivo es opcional

	noReservacion, err := ctrl.service.CancelarReservacion(reservacionID, usuarioID, req.Motivo)
	if err != nil {
		// Log de cancelacion fallida (ID 31)
		uid := usuarioID
		loginInt := fmt.Sprintf("reserva_id=%d", reservacionID)
		ctrl.logSesion.Registrar(c, helpers.TipoCancelacionFallida, &uid, loginInt, err.Error())
		c.JSON(http.StatusUnprocessableEntity, gin.H{"error": err.Error()})
		return
	}

	// Log de cancelacion exitosa por usuario (ID 29)
	uid := usuarioID
	mensaje := fmt.Sprintf("Usuario canceló reserva %s", noReservacion)
	if req.Motivo != "" {
		mensaje += fmt.Sprintf(" — motivo: %s", req.Motivo)
	}
	ctrl.logSesion.Registrar(c, helpers.TipoCancelacionUsuario, &uid, noReservacion, mensaje)

	c.JSON(http.StatusOK, gin.H{
		"mensaje": "Reservación cancelada exitosamente",
	})
}
