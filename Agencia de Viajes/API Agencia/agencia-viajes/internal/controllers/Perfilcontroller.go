package controllers

import (
	"agencia-viajes/internal/helpers"
	"agencia-viajes/internal/services"
	"net/http"

	"github.com/gin-gonic/gin"
)

type PerfilController struct {
	service *services.PerfilService
}

func NewPerfilController(service *services.PerfilService) *PerfilController {
	return &PerfilController{service: service}
}

// GET /api/perfil
func (ctrl *PerfilController) ObtenerPerfil(c *gin.Context) {
	usuarioID := c.GetInt("usuario_id")
	perfil, err := ctrl.service.ObtenerPerfil(usuarioID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "No se pudo obtener el perfil"})
		return
	}
	c.JSON(http.StatusOK, perfil)
}

// PUT /api/perfil/telefono
func (ctrl *PerfilController) ActualizarTelefono(c *gin.Context) {
	usuarioID := c.GetInt("usuario_id")
	var req struct {
		Telefono string `json:"telefono" binding:"required"`
	}
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Teléfono requerido"})
		return
	}
	if err := ctrl.service.ActualizarTelefono(usuarioID, req.Telefono); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "No se pudo actualizar el teléfono"})
		return
	}
	c.JSON(http.StatusOK, gin.H{"mensaje": "Teléfono actualizado correctamente"})
}

// PUT /api/perfil/contrasena
func (ctrl *PerfilController) CambiarContrasena(c *gin.Context) {
	usuarioID := c.GetInt("usuario_id")
	var req struct {
		Actual   string `json:"actual"   binding:"required"`
		Nueva    string `json:"nueva"    binding:"required"`
		Confirma string `json:"confirma" binding:"required"`
	}
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Todos los campos son requeridos"})
		return
	}
	if req.Nueva != req.Confirma {
		c.JSON(http.StatusBadRequest, gin.H{"error": "La nueva contraseña y la confirmación no coinciden"})
		return
	}
	if len(req.Nueva) < 8 {
		c.JSON(http.StatusBadRequest, gin.H{"error": "La nueva contraseña debe tener al menos 8 caracteres"})
		return
	}

	// Obtener hash actual
	hashActual, err := ctrl.service.ObtenerHash(usuarioID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Error al verificar la contraseña"})
		return
	}

	// CheckPassword(plaintext, hash) — orden correcto según helpers/password.go
	if !helpers.CheckPassword(req.Actual, hashActual) {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "La contraseña actual es incorrecta"})
		return
	}

	if err := ctrl.service.CambiarContrasena(usuarioID, req.Nueva); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "No se pudo cambiar la contraseña"})
		return
	}
	c.JSON(http.StatusOK, gin.H{"mensaje": "Contraseña actualizada correctamente"})
}