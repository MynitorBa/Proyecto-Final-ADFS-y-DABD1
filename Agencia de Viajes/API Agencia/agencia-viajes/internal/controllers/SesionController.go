package controllers

import (
	"net/http"

	"github.com/gin-gonic/gin"
)

type SesionController struct{}

func NewSesionController() *SesionController {
	return &SesionController{}
}

func (ctrl *SesionController) ObtenerSesion(c *gin.Context) {
	usuarioID := c.MustGet("usuario_id").(int)
	username := c.MustGet("username").(string)
	rolID := c.MustGet("rol_id").(int)

	c.JSON(http.StatusOK, gin.H{
		"usuario_id": usuarioID,
		"username":   username,
		"rol_id":     rolID,
	})
}
