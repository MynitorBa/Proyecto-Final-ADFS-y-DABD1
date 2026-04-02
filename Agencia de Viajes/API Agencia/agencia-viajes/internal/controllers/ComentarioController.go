package controllers

import (
	"agencia-viajes/internal/services"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
)

type ComentarioController struct {
	service *services.ComentarioService
}

func NewComentarioController(s *services.ComentarioService) *ComentarioController {
	return &ComentarioController{service: s}
}

// GET /api/comentarios/vuelo/:rutaId
func (ctrl *ComentarioController) ObtenerComentariosVuelo(c *gin.Context) {
	rutaID, err := strconv.Atoi(c.Param("rutaId"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "ID de ruta inválido"})
		return
	}

	comentarios, err := ctrl.service.ObtenerComentariosVuelo(rutaID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, comentarios)
}

// GET /api/comentarios/hotel/:hotelId
func (ctrl *ComentarioController) ObtenerComentariosHotel(c *gin.Context) {
	hotelID, err := strconv.Atoi(c.Param("hotelId"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "ID de hotel inválido"})
		return
	}

	comentarios, err := ctrl.service.ObtenerComentariosHotel(hotelID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, comentarios)
}
