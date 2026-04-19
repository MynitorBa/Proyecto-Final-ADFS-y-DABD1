package controllers

import (
	"agencia-viajes/internal/services"
	"net/http"

	"github.com/gin-gonic/gin"
)

// ActualizacionProveedorController
//
// Controlador que expone el endpoint para que los proveedores externos
// notifiquen una actualizacion sobre un componente de una reservacion.
// No modifica estados: solo registra la notificacion y dispara el correo.
// Requiere autenticacion via middleware ProveedorAuthRequerido.
type ActualizacionProveedorController struct {
	service *services.ActualizacionProveedorService
}

// NewActualizacionProveedorController
//
// Crea e inicializa una nueva instancia del controller.
//
// Parametros:
//   - service: servicio de actualizacion por proveedor ya inicializado
//
// Retorna:
//   - *ActualizacionProveedorController: instancia lista para registrar rutas
func NewActualizacionProveedorController(service *services.ActualizacionProveedorService) *ActualizacionProveedorController {
	return &ActualizacionProveedorController{service: service}
}

// actualizacionProveedorRequest
//
// Cuerpo JSON esperado en el endpoint de actualizacion por proveedor.
// El mensaje es requerido para registrar la notificacion y enviarlo al usuario.
type actualizacionProveedorRequest struct {
	Mensaje string `json:"mensaje" binding:"required"`
}

// NotificarActualizacion
//
// Endpoint: POST /api/proveedores-ext/detalles/:idReservaProveedor/actualizar
//
// Recibe la notificacion de actualizacion de un proveedor externo sobre
// un componente especifico de una reservacion. No cambia ningun estado:
// solo registra la notificacion en BD y envia un correo al usuario.
//
// Headers requeridos:
//   - X-Agencia-Token: token de entrada del proveedor (validado por middleware)
//
// Path params:
//   - idReservaProveedor: ID de la reserva en el sistema del proveedor (string)
//
// Body JSON:
//
//	{ "mensaje": "descripcion de la actualizacion" }
//
// Respuestas:
//   - 200: notificacion registrada y correo en camino
//   - 400: body malformado o mensaje ausente
//   - 401: token ausente/invalido (middleware) o detalle no pertenece al proveedor
//   - 500: error interno de base de datos
func (ctrl *ActualizacionProveedorController) NotificarActualizacion(c *gin.Context) {
	// 1. Leer el idReservaProveedor del path
	idReservaProveedor := c.Param("idReservaProveedor")
	if idReservaProveedor == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "idReservaProveedor es requerido"})
		return
	}

	// 2. Leer y validar el body JSON
	var req actualizacionProveedorRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Se requiere el campo 'mensaje'"})
		return
	}

	// 3. Recuperar proveedor_id inyectado por el middleware ProveedorAuthRequerido
	proveedorIDRaw, exists := c.Get("proveedor_id")
	if !exists {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Error interno: proveedor no identificado"})
		return
	}
	proveedorID := proveedorIDRaw.(int)

	// 4. Delegar al servicio
	if err := ctrl.service.NotificarActualizacion(idReservaProveedor, proveedorID, req.Mensaje); err != nil {
		if err.Error() == "detalle no encontrado o no pertenece al proveedor" {
			c.JSON(http.StatusUnauthorized, gin.H{"error": err.Error()})
		} else {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "Error al registrar la notificacion"})
		}
		return
	}

	c.JSON(http.StatusOK, gin.H{"mensaje": "Notificacion registrada correctamente"})
}
