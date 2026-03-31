package controllers

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/services"
	"net/http"

	"github.com/gin-gonic/gin"
)

type DetalleReservacionController struct {
	service *services.DetalleReservacionService
}

func NewDetalleReservacionController(service *services.DetalleReservacionService) *DetalleReservacionController {
	return &DetalleReservacionController{service: service}
}

func (ctrl *DetalleReservacionController) AgregarDetalleVuelo(c *gin.Context) {
	usuarioID, exists := c.Get("usuario_id")
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "usuario no autenticado"})
		return
	}

	var req dto.AgregarDetalleVueloRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "datos inválidos"})
		return
	}

	resp, err := ctrl.service.AgregarDetalleVuelo(usuarioID.(int), req)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, resp)
}

func (ctrl *DetalleReservacionController) AgregarDetalleHotel(c *gin.Context) {
	usuarioID, _ := c.Get("usuario_id")

	var req dto.AgregarDetalleHotelRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "datos inválidos en la solicitud de hotel"})
		return
	}

	resp, err := ctrl.service.AgregarDetalleHotel(usuarioID.(int), req)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, resp)
}

func (ctrl *DetalleReservacionController) AgregarPasajerosVuelo(c *gin.Context) {
	usuarioID, exists := c.Get("usuario_id")
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "usuario no autenticado"})
		return
	}

	var req dto.AgregarPasajerosVueloRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "datos inválidos"})
		return
	}

	err := ctrl.service.AgregarPasajerosVuelo(usuarioID.(int), req)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"mensaje": "datos de pasajeros guardados correctamente"})
}
