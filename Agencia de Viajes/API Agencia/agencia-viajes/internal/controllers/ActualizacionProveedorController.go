package controllers

import (
	"agencia-viajes/internal/helpers"
	"agencia-viajes/internal/services"
	"context"
	"database/sql"
	"fmt"
	"net/http"

	"github.com/gin-gonic/gin"
)

// ActualizacionProveedorController
//
// Controlador que expone el endpoint para que los proveedores externos
// notifiquen una actualizacion sobre un componente de una reservacion.
// No modifica estados: solo registra la notificacion, dispara el correo
// y registra el evento ACTUALIZACION_PROVEEDOR (ID 32) en log_sesion.
// Requiere autenticacion via middleware ProveedorAuthRequerido.
type ActualizacionProveedorController struct {
	service   *services.ActualizacionProveedorService
	logSesion *services.LogSesionService
	db        *sql.DB
}

// NewActualizacionProveedorController
//
// Crea e inicializa una nueva instancia del controller.
//
// Parametros:
//   - service:   servicio de actualizacion por proveedor ya inicializado
//   - logSesion: servicio de auditoria de sesion
//   - db:        conexion a la base de datos para la consulta de auditoria
//
// Retorna:
//   - *ActualizacionProveedorController: instancia lista para registrar rutas
func NewActualizacionProveedorController(
	service *services.ActualizacionProveedorService,
	logSesion *services.LogSesionService,
	db *sql.DB,
) *ActualizacionProveedorController {
	return &ActualizacionProveedorController{service: service, logSesion: logSesion, db: db}
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
// solo registra la notificacion en BD, envia un correo al usuario y
// registra el evento ACTUALIZACION_PROVEEDOR (ID 32) en log_sesion.
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

	// 3. Recuperar proveedor_id y proveedor_nombre inyectados por ProveedorAuthRequerido
	proveedorIDRaw, exists := c.Get("proveedor_id")
	if !exists {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Error interno: proveedor no identificado"})
		return
	}
	proveedorID := proveedorIDRaw.(int)

	proveedorNombreRaw, _ := c.Get("proveedor_nombre")
	provStr, _ := proveedorNombreRaw.(string)

	// 4. Delegar al servicio
	if err := ctrl.service.NotificarActualizacion(idReservaProveedor, proveedorID, req.Mensaje); err != nil {
		if err.Error() == "detalle no encontrado o no pertenece al proveedor" {
			c.JSON(http.StatusUnauthorized, gin.H{"error": err.Error()})
		} else {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "Error al registrar la notificacion"})
		}
		return
	}

	// 5. Obtener Usuario_ID y No_Reservacion para el log de auditoria.
	//    El detalle sigue existiendo con el mismo Reservacion_ID.
	var usuarioID int
	var noReservacion string
	_ = ctrl.db.QueryRowContext(context.Background(), `
		SELECT r.Usuario_ID, r.No_Reservacion
		FROM Reservacion r
		JOIN detalles_reservacion d ON d.Reservacion_ID = r.ID
		WHERE d.ID_Reserva_Proveedor = ? AND d.Proveedor_ID = ?
		LIMIT 1
	`, idReservaProveedor, proveedorID).Scan(&usuarioID, &noReservacion)

	// 6. Registrar evento ACTUALIZACION_PROVEEDOR (ID 32) en log_sesion
	uid := usuarioID
	mensaje := fmt.Sprintf("Proveedor %s actualizó detalle de reserva %s", provStr, noReservacion)
	if req.Mensaje != "" {
		mensaje += fmt.Sprintf(" — %s", req.Mensaje)
	}
	ctrl.logSesion.RegistrarSistema(
		helpers.TipoActualizacionProveedor,
		&uid,
		noReservacion,
		mensaje,
		fmt.Sprintf("ActualizacionProveedorController:%s", provStr),
	)

	c.JSON(http.StatusOK, gin.H{"mensaje": "Notificacion registrada correctamente"})
}
