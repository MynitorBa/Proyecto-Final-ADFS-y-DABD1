// # Package controllers
//
// Controladores HTTP de la agencia de viajes. Cada controlador recibe
// solicitudes de Gin, delega la logica de negocio al servicio correspondiente
// y devuelve la respuesta JSON al cliente.
package controllers

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/services"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
)

// CancelacionController
//
// Controlador encargado de gestionar la verificacion y ejecucion de
// cancelaciones de reservaciones realizadas por el usuario.
type CancelacionController struct {
	service *services.CancelacionService
}

// NewCancelacionController
//
// Crea e inicializa un nuevo CancelacionController con el servicio recibido.
//
// Parametros:
//   - s: instancia del servicio de cancelacion
//
// Retorna:
//   - *CancelacionController: puntero al controlador creado
func NewCancelacionController(s *services.CancelacionService) *CancelacionController {
	return &CancelacionController{service: s}
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
//   - HTTP 404: error si la reservacion no existe o no pertenece al usuario
//
// Notas:
//   - Ruta esperada: GET /api/reservaciones/:id/cancelar/verificar
func (ctrl *CancelacionController) Verificar(c *gin.Context) {
	usuarioID, _ := c.Get("usuario_id")

	reservacionID, err := strconv.Atoi(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "ID de reservación inválido"})
		return
	}

	resultado, err := ctrl.service.VerificarCancelacion(reservacionID, usuarioID.(int))
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, resultado)
}

// Cancelar
//
// Handler HTTP que ejecuta la cancelacion de una reservacion existente.
// El motivo de cancelacion es opcional; si no se proporciona se usa una
// cadena vacia.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200: mensaje confirmando que la reservacion fue cancelada exitosamente
//   - HTTP 400: error si el parametro de ruta ID no es un entero valido
//   - HTTP 422: error si el servicio no puede procesar la cancelacion
//
// Notas:
//   - Ruta esperada: POST /api/reservaciones/:id/cancelar
func (ctrl *CancelacionController) Cancelar(c *gin.Context) {
	usuarioID, _ := c.Get("usuario_id")

	reservacionID, err := strconv.Atoi(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "ID de reservación inválido"})
		return
	}

	var req dto.CancelarReservacionRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		req.Motivo = "" // motivo es opcional
	}

	if err := ctrl.service.CancelarReservacion(reservacionID, usuarioID.(int), req.Motivo); err != nil {
		c.JSON(http.StatusUnprocessableEntity, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"mensaje": "Reservación cancelada exitosamente"})
}
