package controllers

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/services"
	"net/http"

	"github.com/gin-gonic/gin"
)

type BusquedaController struct {
	service *services.BusquedaService
}

func NewBusquedaController(service *services.BusquedaService) *BusquedaController {
	return &BusquedaController{service: service}
}

// POST /busqueda/vuelos
func (ctrl *BusquedaController) BuscarVuelos(c *gin.Context) {
	var req dto.BusquedaVuelosRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Body inválido"})
		return
	}

	resultados, err := ctrl.service.BuscarVuelos(req)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, resultados)
}

// POST /busqueda/hoteles
func (ctrl *BusquedaController) BuscarHoteles(c *gin.Context) {
	var req dto.BusquedaHotelesRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Body inválido"})
		return
	}

	resultados, err := ctrl.service.BuscarHoteles(req)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, resultados)
}
