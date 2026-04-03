package controllers

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/helpers"
	"agencia-viajes/internal/services"
	"log"
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

	// ── Correo de bienvenida (fire-and-forget) ────────────────────────────
	// Se envía en background para no bloquear la respuesta al cliente.
	go func() {
		if err := helpers.EnviarBienvenida(
			req.Correo,
			req.Nombre,
			req.Apellido,
			req.Username,
			req.Telefono,
			req.FechaNacimiento,
			req.Ciudad,
			req.Pais,
			req.Nacionalidades,
		); err != nil {
			log.Printf("[BIENVENIDA] Error enviando correo a %s: %v", req.Correo, err)
		}
	}()

	c.JSON(http.StatusCreated, gin.H{"mensaje": "Usuario registrado exitosamente"})
}