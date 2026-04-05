// # Package controllers
//
// Controladores HTTP de la agencia de viajes. Cada controlador recibe
// solicitudes de Gin, delega la logica de negocio al servicio correspondiente
// y devuelve la respuesta JSON al cliente.
package controllers

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/services"
	"net/http"

	"github.com/gin-gonic/gin"
)

// ProveedorController
//
// Controlador encargado de gestionar el alta de proveedores externos
// (aerolineas y hoteles) en el sistema de la agencia.
type ProveedorController struct {
	service *services.ProveedorService
}

// NewProveedorController
//
// Crea e inicializa un nuevo ProveedorController con el servicio recibido.
//
// Parametros:
//   - service: instancia del servicio de proveedor
//
// Retorna:
//   - *ProveedorController: puntero al controlador creado
func NewProveedorController(service *services.ProveedorService) *ProveedorController {
	return &ProveedorController{service: service}
}

// CrearProveedor
//
// Handler HTTP que registra un nuevo proveedor externo en el sistema a partir
// de los datos enviados en el body de la solicitud.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 201: mensaje de confirmacion junto con los datos del proveedor creado
//   - HTTP 400: error si el body JSON es invalido o el servicio retorna un error de validacion
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

	c.JSON(http.StatusCreated, gin.H{
		"mensaje":   "proveedor creado exitosamente",
		"proveedor": proveedor,
	})
}
