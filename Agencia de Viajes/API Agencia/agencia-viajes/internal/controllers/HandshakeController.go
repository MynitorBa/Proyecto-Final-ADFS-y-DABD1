// internal/controllers/handshake_controller.go
package controllers

import (
	"agencia-viajes/internal/services"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
)

type HandshakeController struct {
	service *services.HandshakeService
}

func NewHandshakeController(service *services.HandshakeService) *HandshakeController {
	return &HandshakeController{service: service}
}

func (ctrl *HandshakeController) IniciarHandshake(c *gin.Context) {
	proveedorID, err := strconv.Atoi(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "ID de proveedor inválido"})
		return
	}

	tokenSalida, err := ctrl.service.IniciarHandshake(proveedorID)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"mensaje":      "handshake completado exitosamente",
		"token_salida": tokenSalida,
	})
}
