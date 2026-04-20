// # Package controllers
//
// Controladores HTTP de la agencia de viajes. Cada controlador recibe
// solicitudes de Gin, delega la logica de negocio al servicio correspondiente
// y devuelve la respuesta JSON al cliente.
package controllers

import (
	"agencia-viajes/internal/helpers"
	"agencia-viajes/internal/services"
	"fmt"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
)

// HandshakeController
//
// Controlador encargado de iniciar el proceso de handshake con proveedores
// de aerolineas, obteniendo el token de sesion necesario para consumir
// sus servicios. Registra el resultado en log_sesion para trazabilidad
// de configuraciones de proveedores.
type HandshakeController struct {
	service   *services.HandshakeService
	logSesion *services.LogSesionService
}

// NewHandshakeController
//
// Crea e inicializa un nuevo HandshakeController con el servicio recibido.
//
// Parametros:
//   - service:   instancia del servicio de handshake de aerolineas
//   - logSesion: instancia del servicio de auditoria de sesion
//
// Retorna:
//   - *HandshakeController: puntero al controlador creado
func NewHandshakeController(service *services.HandshakeService, logSesion *services.LogSesionService) *HandshakeController {
	return &HandshakeController{service: service, logSesion: logSesion}
}

// IniciarHandshake
//
// Handler HTTP que inicia el proceso de handshake con un proveedor de aerolinea
// identificado por su ID en la ruta. Si el proceso es exitoso retorna el token
// de salida generado por el proveedor y registra HANDSHAKE_PROVEEDOR_EXITOSO (ID 35).
// Si falla, registra HANDSHAKE_PROVEEDOR_FALLIDO (ID 36).
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200: mensaje de exito junto con el token de salida del proveedor
//   - HTTP 400: error si el parametro de ruta ID no es un entero valido o el servicio retorna un error
//
// Notas:
//   - El parametro de ruta :id corresponde al ID del proveedor de aerolinea
//   - Solo accesible por administradores (RolRequerido(2))
func (ctrl *HandshakeController) IniciarHandshake(c *gin.Context) {
	proveedorID, err := strconv.Atoi(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "ID de proveedor inválido"})
		return
	}

	// Obtener usuario_id del admin para el log (inyectado por AuthRequerido)
	uidRaw, _ := c.Get("usuario_id")
	uidInt := 0
	if u, ok := uidRaw.(int); ok {
		uidInt = u
	}

	_, err = ctrl.service.IniciarHandshake(proveedorID)
	if err != nil {
		// Log de handshake fallido (ID 36)
		ctrl.logSesion.Registrar(c, helpers.TipoHandshakeProveedorFallido,
			&uidInt, fmt.Sprintf("proveedor_id=%d;tipo=vuelos", proveedorID),
			fmt.Sprintf("Handshake con proveedor ID %d falló: %s", proveedorID, err.Error()))

		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	// Log de handshake exitoso (ID 35)
	ctrl.logSesion.Registrar(c, helpers.TipoHandshakeProveedorExitoso,
		&uidInt, fmt.Sprintf("proveedor_id=%d;tipo=vuelos", proveedorID),
		fmt.Sprintf("Admin completó handshake con proveedor ID %d (vuelos)", proveedorID))

	c.JSON(http.StatusOK, gin.H{
		"mensaje": "handshake completado exitosamente",
	})
}
