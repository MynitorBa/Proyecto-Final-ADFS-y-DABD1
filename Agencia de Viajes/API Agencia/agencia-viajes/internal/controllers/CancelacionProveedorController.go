package controllers

import (
	"agencia-viajes/internal/services"
	"net/http"

	"github.com/gin-gonic/gin"
)

// CancelacionProveedorController
//
// Controlador que expone el endpoint para que los proveedores externos
// notifiquen la cancelacion de un detalle de reservacion especifico.
// Requiere autenticacion via middleware ProveedorAuthRequerido, que ya
// valida el token y deposita proveedor_id y proveedor_tipo en el contexto.
type CancelacionProveedorController struct {
	service *services.CancelacionProveedorService
}

// NewCancelacionProveedorController
//
// Crea e inicializa una nueva instancia del controller.
//
// Parametros:
//   - service: servicio de cancelacion por proveedor ya inicializado
//
// Retorna:
//   - *CancelacionProveedorController: instancia lista para registrar rutas
func NewCancelacionProveedorController(service *services.CancelacionProveedorService) *CancelacionProveedorController {
	return &CancelacionProveedorController{service: service}
}

// cancelacionProveedorRequest
//
// Cuerpo JSON esperado en el endpoint de cancelacion por proveedor.
// El mensaje es requerido para registrar la razon en la notificacion.
type cancelacionProveedorRequest struct {
	Mensaje string `json:"mensaje" binding:"required"`
}

// CancelarDetalle
//
// Endpoint: POST /api/proveedores-ext/detalles/:idReservaProveedor/cancelar
//
// Recibe la notificacion de cancelacion de un proveedor externo. El proveedor
// identifica su reserva por su propio ID (ID_Reserva_Proveedor), no por el
// ID interno de la agencia, que desconoce por completo.
//
// El proveedor ya cancelo en su sistema y este endpoint sincroniza el estado:
//   - Localiza el detalle por ID_Reserva_Proveedor + Proveedor_ID (del token)
//   - Marca el detalle como Cancelado (estado 3)
//   - Pone la reservacion padre en Retenido (estado 7)
//   - Registra una notificacion con el mensaje del proveedor
//
// Headers requeridos:
//   - X-Agencia-Token: token de entrada del proveedor (validado por middleware)
//
// Path params:
//   - idReservaProveedor: ID de la reserva en el sistema del proveedor (string)
//
// Body JSON:
//
//	{ "mensaje": "razon de la cancelacion" }
//
// Respuestas:
//   - 200: cancelacion procesada correctamente
//   - 400: body malformado o mensaje ausente
//   - 401: token ausente/invalido (middleware) o detalle no pertenece al proveedor
//   - 422: el detalle no puede cancelarse en su estado actual
//   - 500: error interno de base de datos
func (ctrl *CancelacionProveedorController) CancelarDetalle(c *gin.Context) {
	// 1. Leer el idReservaProveedor del path — es string, no requiere conversion
	idReservaProveedor := c.Param("idReservaProveedor")
	if idReservaProveedor == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "idReservaProveedor es requerido"})
		return
	}

	// 2. Leer y validar el body JSON
	var req cancelacionProveedorRequest
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

	// 4. Delegar la logica al servicio
	if err := ctrl.service.CancelarDetallePorProveedor(idReservaProveedor, proveedorID, req.Mensaje); err != nil {
		switch err.Error() {
		case "detalle no encontrado o no pertenece al proveedor":
			c.JSON(http.StatusUnauthorized, gin.H{"error": err.Error()})
		default:
			if len(err.Error()) > 30 && err.Error()[:30] == "el detalle no puede cancelarse" {
				c.JSON(http.StatusUnprocessableEntity, gin.H{"error": err.Error()})
			} else {
				c.JSON(http.StatusInternalServerError, gin.H{"error": "Error al procesar la cancelación"})
			}
		}
		return
	}

	c.JSON(http.StatusOK, gin.H{"mensaje": "Detalle cancelado y reservación retenida correctamente"})
}
