package controllers

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/services"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
)

type CancelacionController struct {
	service *services.CancelacionService
}

func NewCancelacionController(s *services.CancelacionService) *CancelacionController {
	return &CancelacionController{service: s}
}

// GET /api/reservaciones/:id/cancelar/verificar
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

// POST /api/reservaciones/:id/cancelar
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
