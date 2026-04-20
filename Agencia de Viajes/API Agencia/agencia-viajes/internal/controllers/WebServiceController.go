// # Package controllers
//
// Controladores HTTP de la API de Movent. Cada controlador agrupa los handlers
// relacionados a un recurso o dominio especifico de la aplicacion.
package controllers

import (
	"agencia-viajes/internal/services"
	"net/http"

	"github.com/gin-gonic/gin"
)

// WebServiceController
//
// Controlador que expone los endpoints del panel operacional del WebService.
// Accesible por administradores (rol 2) y usuarios WebService (rol 3).
type WebServiceController struct {
	service *services.WebServiceService
}

// NewWebServiceController
//
// Crea e inicializa un nuevo WebServiceController.
//
// Parametros:
//   - service: instancia del servicio WebService
//
// Retorna:
//   - *WebServiceController: puntero al controlador creado
func NewWebServiceController(service *services.WebServiceService) *WebServiceController {
	return &WebServiceController{service: service}
}

// ObtenerEstado
//
// Retorna el estado operacional del sistema WebService: lista de proveedores
// con flag handshake_configurado y conteo de eventos recientes de handshake
// y actualizacion de catalogo registrados en log_sesion.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200 OK: JSON con campos "proveedores" y "eventos"
//   - HTTP 500 Internal Server Error: si alguna consulta de BD falla
func (ctrl *WebServiceController) ObtenerEstado(c *gin.Context) {
	proveedores, eventos, err := ctrl.service.ObtenerEstado()
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "error consultando estado"})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"proveedores": proveedores,
		"eventos":     eventos,
	})
}

// ObtenerNotificaciones
//
// Retorna las ultimas 50 notificaciones generadas por proveedores en
// todas las reservaciones del sistema, sin filtrar por usuario.
// Solo accesible por administradores y usuarios WebService.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200 OK: JSON con arreglo de notificaciones ordenadas por fecha DESC
//   - HTTP 500 Internal Server Error: si la consulta de BD falla
func (ctrl *WebServiceController) ObtenerNotificaciones(c *gin.Context) {
	notificaciones, err := ctrl.service.ObtenerNotificaciones()
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "error consultando notificaciones"})
		return
	}

	c.JSON(http.StatusOK, notificaciones)
}
