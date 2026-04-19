package controllers

import (
	"agencia-viajes/internal/services"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
)

// NotificacionesController
//
// Controlador que expone los endpoints de notificaciones para el usuario
// autenticado. Ambas rutas requieren el middleware AuthRequerido.
type NotificacionesController struct {
	service *services.NotificacionesService
}

// NewNotificacionesController
//
// Crea e inicializa una nueva instancia del controller.
//
// Parametros:
//   - service: servicio de notificaciones ya inicializado
//
// Retorna:
//   - *NotificacionesController: instancia lista para registrar rutas
func NewNotificacionesController(service *services.NotificacionesService) *NotificacionesController {
	return &NotificacionesController{service: service}
}

// ObtenerTodas
//
// Endpoint: GET /api/notificaciones
//
// Retorna todas las notificaciones del usuario autenticado, de todas sus
// reservaciones, ordenadas de mas reciente a mas antigua. Incluye tanto
// leidas como no leidas.
//
// Headers requeridos:
//   - Cookie session: JWT valido (validado por middleware AuthRequerido)
//
// Respuestas:
//   - 200: lista de notificaciones (puede ser array vacio si no hay ninguna)
//   - 500: error interno de base de datos
func (ctrl *NotificacionesController) ObtenerTodas(c *gin.Context) {
	usuarioID := c.GetInt("usuario_id")

	notificaciones, err := ctrl.service.ObtenerTodas(usuarioID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Error al obtener notificaciones"})
		return
	}

	c.JSON(http.StatusOK, gin.H{"notificaciones": notificaciones})
}

// MarcarComoLeida
//
// Endpoint: PATCH /api/notificaciones/:id/leida
//
// Marca una notificacion especifica como leida. Solo funciona si la
// notificacion pertenece al usuario autenticado y no estaba ya leida.
//
// Headers requeridos:
//   - Cookie session: JWT valido (validado por middleware AuthRequerido)
//
// Path params:
//   - id: ID de la notificacion a marcar como leida
//
// Respuestas:
//   - 200: notificacion marcada como leida correctamente
//   - 400: id invalido
//   - 404: notificacion no encontrada, no pertenece al usuario o ya estaba leida
//   - 500: error interno de base de datos
func (ctrl *NotificacionesController) MarcarComoLeida(c *gin.Context) {
	// 1. Parsear y validar el id del path
	notificacionID, err := strconv.Atoi(c.Param("id"))
	if err != nil || notificacionID <= 0 {
		c.JSON(http.StatusBadRequest, gin.H{"error": "id de notificacion invalido"})
		return
	}

	usuarioID := c.GetInt("usuario_id")

	// 2. Delegar al servicio
	if err := ctrl.service.MarcarComoLeida(notificacionID, usuarioID); err != nil {
		// El repositorio retorna un error descriptivo si no se afecto ninguna fila
		if err.Error() == "notificacion no encontrada, no pertenece al usuario o ya estaba leida" {
			c.JSON(http.StatusNotFound, gin.H{"error": err.Error()})
		} else {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "Error al actualizar notificacion"})
		}
		return
	}

	c.JSON(http.StatusOK, gin.H{"mensaje": "Notificacion marcada como leida"})
}