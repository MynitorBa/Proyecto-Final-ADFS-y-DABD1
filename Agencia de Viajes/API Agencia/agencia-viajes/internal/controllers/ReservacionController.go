// internal/controllers/reservacion_controller.go
package controllers

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/services"
	"net/http"

	"github.com/gin-gonic/gin"
)

type ReservacionController struct {
	service *services.ReservacionService
}

func NewReservacionController(service *services.ReservacionService) *ReservacionController {
	return &ReservacionController{service: service}
}

func (ctrl *ReservacionController) CrearReservacion(c *gin.Context) {
	usuarioID, exists := c.Get("usuario_id")
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "usuario no autenticado"})
		return
	}

	var req dto.CrearReservacionRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "datos inválidos"})
		return
	}

	if req.TipoReservaID < 1 || req.TipoReservaID > 3 {
		c.JSON(http.StatusBadRequest, gin.H{"error": "tipo_reserva_id inválido. Use 1=Aerolinea, 2=Hotelera, 3=Paquete"})
		return
	}

	resp, err := ctrl.service.CrearReservacion(usuarioID.(int), req.TipoReservaID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusCreated, resp)
}
