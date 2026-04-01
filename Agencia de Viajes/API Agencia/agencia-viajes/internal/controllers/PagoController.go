package controllers

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/services"
	"net/http"

	"github.com/gin-gonic/gin"
)

type PagoController struct {
	service *services.PagoService
}

func NewPagoController(s *services.PagoService) *PagoController {
	return &PagoController{service: s}
}

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
