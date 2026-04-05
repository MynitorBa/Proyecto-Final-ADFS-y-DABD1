// # Package controllers
//
// Controladores HTTP de la API de Movent. Cada controlador agrupa los handlers
// relacionados a un recurso o dominio especifico de la aplicacion.
package controllers

import (
	"agencia-viajes/internal/helpers"
	"agencia-viajes/internal/services"
	"net/http"

	"github.com/gin-gonic/gin"
)

// PerfilController
//
// Controlador que maneja los endpoints de consulta y actualizacion del
// perfil del usuario autenticado, incluyendo telefono y contrasena.
type PerfilController struct {
	service *services.PerfilService
}

// NewPerfilController
//
// Constructor que retorna una nueva instancia de PerfilController
// con el servicio de perfil inyectado.
//
// Parametros:
//   - service: puntero al servicio de perfil
//
// Retorna:
//   - *PerfilController: puntero a la nueva instancia
func NewPerfilController(service *services.PerfilService) *PerfilController {
	return &PerfilController{service: service}
}

// ObtenerPerfil
//
// Retorna los datos del perfil del usuario actualmente autenticado.
// El ID del usuario se extrae del contexto de Gin inyectado por el middleware.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200 OK: JSON con los datos del perfil del usuario
//   - HTTP 500 Internal Server Error: si ocurre un error al obtener el perfil
func (ctrl *PerfilController) ObtenerPerfil(c *gin.Context) {
	usuarioID := c.GetInt("usuario_id")
	perfil, err := ctrl.service.ObtenerPerfil(usuarioID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "No se pudo obtener el perfil"})
		return
	}
	c.JSON(http.StatusOK, perfil)
}

// ActualizarTelefono
//
// Actualiza el numero de telefono del usuario autenticado. El nuevo numero
// se lee del campo telefono en el body JSON de la solicitud.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200 OK: JSON con mensaje de confirmacion
//   - HTTP 400 Bad Request: si el campo telefono no esta presente en el body
//   - HTTP 500 Internal Server Error: si ocurre un error al actualizar el telefono
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

// CambiarContrasena
//
// Cambia la contrasena del usuario autenticado. Verifica que la contrasena
// actual sea correcta, que la nueva y su confirmacion coincidan, y que la
// nueva tenga al menos 8 caracteres antes de persistir el cambio.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200 OK: JSON con mensaje de confirmacion
//   - HTTP 400 Bad Request: si faltan campos, las contrasenas no coinciden o la nueva es muy corta
//   - HTTP 401 Unauthorized: si la contrasena actual proporcionada es incorrecta
//   - HTTP 500 Internal Server Error: si ocurre un error al verificar o cambiar la contrasena
//
// Notas:
//   - La verificacion de la contrasena actual se realiza comparando el hash almacenado
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
