// # Package controllers
//
// Controladores HTTP de la API de Movent. Cada controlador agrupa los handlers
// relacionados a un recurso o dominio especifico de la aplicacion.
package controllers

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/services"
	"net/http"

	"github.com/gin-gonic/gin"
)

// PagoController
//
// Controlador que maneja los endpoints relacionados al procesamiento de pagos
// de reservaciones existentes en la plataforma.
type PagoController struct {
	service *services.PagoService
}

// NewPagoController
//
// Constructor que retorna una nueva instancia de PagoController
// con el servicio de pagos inyectado.
//
// Parametros:
//   - s: puntero al servicio de pagos
//
// Retorna:
//   - *PagoController: puntero a la nueva instancia
func NewPagoController(s *services.PagoService) *PagoController {
	return &PagoController{service: s}
}

// Pagar
//
// Procesa el pago de una reservacion existente para el usuario autenticado.
// Lee el ID del usuario desde el contexto de Gin, valida el body del request
// y delega el procesamiento al servicio de pagos.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200 OK: JSON con mensaje de confirmacion si el pago fue exitoso
//   - HTTP 400 Bad Request: si los datos de pago en el body estan incompletos o son invalidos
//   - HTTP 422 Unprocessable Entity: si el servicio rechaza el pago (fondos insuficientes,
//     reservacion no encontrada, etc.)
//
// Notas:
//   - El ID del usuario autenticado se obtiene del contexto sin verificacion de existencia
func (ctrl *PagoController) Pagar(c *gin.Context) {
	usuarioID, _ := c.Get("usuario_id")
	var req dto.PagoReservacionRequest

	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "datos de pago incompletos"})
		return
	}

	err := ctrl.service.ProcesarPago(usuarioID.(int), req)
	if err != nil {
		c.JSON(http.StatusUnprocessableEntity, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"mensaje": "Pago procesado y reservación confirmada exitosamente"})
}
