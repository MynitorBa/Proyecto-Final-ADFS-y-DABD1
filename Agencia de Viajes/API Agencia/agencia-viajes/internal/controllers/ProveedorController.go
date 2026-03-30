package controllers

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/services"
	"net/http"

	"github.com/gin-gonic/gin"
)

type ProveedorController struct {
	service *services.ProveedorService
}

func NewProveedorController(service *services.ProveedorService) *ProveedorController {
	return &ProveedorController{service: service}
}

func (ctrl *ProveedorController) CrearProveedor(c *gin.Context) {
	var req dto.CrearProveedorRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "datos inválidos: " + err.Error()})
		return
	}

	proveedor, err := ctrl.service.CrearProveedor(req)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusCreated, gin.H{
		"mensaje":   "proveedor creado exitosamente",
		"proveedor": proveedor,
	})
}
