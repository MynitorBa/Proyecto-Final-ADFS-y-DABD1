// # Package controllers
//
// Controladores HTTP de la API de Movent. Cada controlador agrupa los handlers
// relacionados a un recurso o dominio especifico de la aplicacion.
package controllers

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/helpers"
	"agencia-viajes/internal/services"
	"fmt"
	"net/http"

	"github.com/gin-gonic/gin"
)

// PagoController
//
// Controlador que maneja los endpoints relacionados al procesamiento de pagos
// de reservaciones existentes en la plataforma.
type PagoController struct {
	service   *services.PagoService
	logSesion *services.LogSesionService
}

// NewPagoController
//
// Constructor que retorna una nueva instancia de PagoController
// con el servicio de pagos y el servicio de log inyectados.
//
// Parametros:
//   - service: puntero al servicio de pagos
//   - logSesion: puntero al servicio de log de sesion para auditoria
//
// Retorna:
//   - *PagoController: puntero a la nueva instancia
func NewPagoController(service *services.PagoService, logSesion *services.LogSesionService) *PagoController {
	return &PagoController{service: service, logSesion: logSesion}
}

// Pagar
//
// Procesa el pago de una reservacion existente para el usuario autenticado.
// Lee el ID del usuario desde el contexto de Gin verificando su existencia,
// valida el body del request y delega el procesamiento al servicio de pagos.
// Registra en log_sesion el resultado del intento de pago (exitoso o fallido).
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200 OK: JSON con mensaje de confirmacion si el pago fue exitoso
//   - HTTP 400 Bad Request: si los datos de pago en el body estan incompletos o son invalidos
//   - HTTP 401 Unauthorized: si el usuario no esta autenticado (usuario_id ausente en contexto)
//   - HTTP 422 Unprocessable Entity: si el servicio rechaza el pago (fondos insuficientes,
//     reservacion no encontrada, etc.)
//
// Notas:
//   - ID 25 (COMPRA_EXITOSA) se registra con el no_reservacion como loginIntentado
//   - ID 26 (COMPRA_FALLIDA_PAGO) se registra con "reserva_id=N" como loginIntentado
func (ctrl *PagoController) Pagar(c *gin.Context) {
	var req dto.PagoReservacionRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"mensaje": "Datos inválidos"})
		return
	}

	// FIX: verificar existencia del usuario_id (antes causaba panic con type assertion sin check)
	usuarioIDRaw, exists := c.Get("usuario_id")
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{"mensaje": "No autenticado"})
		return
	}
	usuarioID := usuarioIDRaw.(int)

	noReservacion, err := ctrl.service.ProcesarPago(usuarioID, req)
	if err != nil {
		uid := usuarioID
		loginInt := fmt.Sprintf("reserva_id=%d", req.ReservacionID)
		ctrl.logSesion.Registrar(c, helpers.TipoCompraFallidaPago, &uid, loginInt, err.Error())
		c.JSON(http.StatusUnprocessableEntity, gin.H{"mensaje": err.Error()})
		return
	}

	uid := usuarioID
	ctrl.logSesion.Registrar(c, helpers.TipoCompraExitosa,
		&uid, noReservacion,
		fmt.Sprintf("Compra procesada para reserva %s", noReservacion))

	c.JSON(http.StatusOK, gin.H{
		"mensaje": "Pago procesado y reservación confirmada exitosamente",
	})
}
