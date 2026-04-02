package controllers

import (
	"agencia-viajes/internal/services"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
)

type MisReservacionesController struct {
	service *services.MisReservacionesService
}

func NewMisReservacionesController(s *services.MisReservacionesService) *MisReservacionesController {
	return &MisReservacionesController{service: s}
}

// GET /api/reservaciones/mias
// Devuelve todas las reservaciones del usuario con datos locales
func (ctrl *MisReservacionesController) Listar(c *gin.Context) {
	usuarioID, _ := c.Get("usuario_id")

	reservaciones, err := ctrl.service.ListarReservaciones(usuarioID.(int))
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, reservaciones)
}

// GET /api/reservaciones/mias/:id
// Devuelve el detalle completo de una reservación llamando a los proveedores
func (ctrl *MisReservacionesController) Detalle(c *gin.Context) {
	usuarioID, _ := c.Get("usuario_id")

	reservacionID, err := strconv.Atoi(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "ID de reservación inválido"})
		return
	}

	detalle, err := ctrl.service.ObtenerDetalle(reservacionID, usuarioID.(int))
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, detalle)
}
