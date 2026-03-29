package controllers

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/services"
	"net/http"

	"github.com/gin-gonic/gin"
)

type UsuarioController struct {
	service *services.UsuarioService
}

func NewUsuarioController(service *services.UsuarioService) *UsuarioController {
	return &UsuarioController{service: service}
}

func (ctrl *UsuarioController) Registrar(c *gin.Context) {
	var req dto.RegistroUsuarioRequest

	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Datos inválidos"})
		return
	}

	validacion, err := ctrl.service.Registrar(req)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Error al registrar usuario"})
		return
	}

	// Si hubo duplicados devuelve la validación
	if validacion.Correo || validacion.Pasaporte || validacion.Username {
		c.JSON(http.StatusConflict, validacion)
		return
	}

	c.JSON(http.StatusCreated, gin.H{"mensaje": "Usuario registrado exitosamente"})
}
