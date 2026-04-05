// # Package controllers
//
// Controladores HTTP de la API de Movent. Cada controlador agrupa los handlers
// relacionados a un recurso o dominio especifico de la aplicacion.
package controllers

import (
	"net/http"

	"github.com/gin-gonic/gin"
)

// SesionController
//
// Controlador que maneja los endpoints relacionados a la sesion del usuario autenticado.
type SesionController struct{}

// NewSesionController
//
// Constructor que retorna una nueva instancia de SesionController.
//
// Retorna:
//   - *SesionController: puntero a la nueva instancia
func NewSesionController() *SesionController {
	return &SesionController{}
}

// ObtenerSesion
//
// Retorna los datos de la sesion del usuario actualmente autenticado,
// extrayendo el ID, nombre de usuario y rol desde el contexto de Gin
// inyectado por el middleware de autenticacion.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200 OK: JSON con campos usuario_id, username y rol_id
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
