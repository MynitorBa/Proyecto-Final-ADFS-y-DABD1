// # Package controllers
//
// Controladores HTTP de la agencia de viajes. Cada controlador recibe
// solicitudes de Gin, delega la logica de negocio al servicio correspondiente
// y devuelve la respuesta JSON al cliente.
package controllers

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/helpers"
	"agencia-viajes/internal/services"
	"fmt"
	"net/http"

	"github.com/gin-gonic/gin"
)

// ProveedorController
//
// Controlador encargado de gestionar el alta de proveedores externos
// (aerolineas y hoteles) en el sistema de la agencia.
type ProveedorController struct {
	service   *services.ProveedorService
	logSesion *services.LogSesionService
}

// NewProveedorController
//
// Crea e inicializa un nuevo ProveedorController con los servicios recibidos.
//
// Parametros:
//   - service:   instancia del servicio de proveedor
//   - logSesion: instancia del servicio de auditoria de sesion
//
// Retorna:
//   - *ProveedorController: puntero al controlador creado
func NewProveedorController(service *services.ProveedorService, logSesion *services.LogSesionService) *ProveedorController {
	return &ProveedorController{service: service, logSesion: logSesion}
}

// CrearProveedor
//
// Handler HTTP que registra un nuevo proveedor externo en el sistema a partir
// de los datos enviados en el body de la solicitud. Registra PROVEEDOR_CREADO
// (ID 40) en log_sesion si la creacion es exitosa.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 201: mensaje de confirmacion junto con los datos del proveedor creado
//   - HTTP 400: error si el body JSON es invalido o el servicio retorna un error de validacion
//
// Notas:
//   - Solo accesible por administradores (RolRequerido(2))
func (ctrl *ProveedorController) CrearProveedor(c *gin.Context) {
	var req dto.CrearProveedorRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "datos inválidos: " + err.Error()})
		return
	}

	proveedor, err := ctrl.service.CrearProveedor(req)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	// Obtener usuario_id del admin que ejecuta la acción (inyectado por AuthRequerido)
	adminIDRaw, _ := c.Get("usuario_id")
	adminID, _ := adminIDRaw.(int)

	// Log de creación exitosa de proveedor (ID 40)
	ctrl.logSesion.Registrar(c, helpers.TipoProveedorCreado,
		&adminID, fmt.Sprintf("proveedor_nombre=%s", req.Nombre),
		fmt.Sprintf("Admin creó proveedor '%s' (ID=%d, tipo_id=%d, usuario_ws_id=%d, ganancia=%.2f%%)",
			req.Nombre, proveedor.ID, req.TipoProveedorID, req.UsuarioID, req.PorcentajeGanancia))

	c.JSON(http.StatusCreated, gin.H{
		"mensaje":   "proveedor creado exitosamente",
		"proveedor": proveedor,
	})
}
