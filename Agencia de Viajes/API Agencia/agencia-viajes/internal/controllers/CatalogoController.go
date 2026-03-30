package controllers

import (
	"agencia-viajes/internal/services"
	"net/http"

	"github.com/gin-gonic/gin"
)

type CatalogoController struct {
	service *services.CatalogoService
}

func NewCatalogoController(service *services.CatalogoService) *CatalogoController {
	return &CatalogoController{service: service}
}

func (ctrl *CatalogoController) ActualizarCatalogo(c *gin.Context) {
	resultados, err := ctrl.service.ActualizarCatalogo()
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"mensaje":    "proceso de actualización completado",
		"resultados": resultados,
	})
}
