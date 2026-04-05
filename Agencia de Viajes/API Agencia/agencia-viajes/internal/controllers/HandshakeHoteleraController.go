// # Package controllers
//
// Controladores HTTP de la agencia de viajes. Cada controlador recibe
// solicitudes de Gin, delega la logica de negocio al servicio correspondiente
// y devuelve la respuesta JSON al cliente.
package controllers

import (
	"agencia-viajes/internal/services"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
)

// HandshakeHoteleraController
//
// Controlador encargado de iniciar el proceso de handshake con proveedores
// hoteleros, obteniendo el token de sesion necesario para consumir sus servicios.
type HandshakeHoteleraController struct {
	service *services.HandshakeHoteleraService
}

// NewHandshakeHoteleraController
//
// Crea e inicializa un nuevo HandshakeHoteleraController con el servicio recibido.
//
// Parametros:
//   - service: instancia del servicio de handshake hotelero
//
// Retorna:
//   - *HandshakeHoteleraController: puntero al controlador creado
func NewHandshakeHoteleraController(service *services.HandshakeHoteleraService) *HandshakeHoteleraController {
	return &HandshakeHoteleraController{service: service}
}

// IniciarHandshake
//
// Handler HTTP que inicia el proceso de handshake con un proveedor hotelero
// identificado por su ID en la ruta. Si el proceso es exitoso retorna el token
// de salida generado por el proveedor.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200: mensaje de exito junto con el token de salida del proveedor hotelero
//   - HTTP 400: error si el parametro de ruta ID no es un entero valido o el servicio retorna un error
//
// Notas:
//   - El parametro de ruta :id corresponde al ID del proveedor hotelero
func (ctrl *HandshakeHoteleraController) IniciarHandshake(c *gin.Context) {
	proveedorID, err := strconv.Atoi(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "ID de proveedor inválido"})
		return
	}

	tokenSalida, err := ctrl.service.IniciarHandshake(proveedorID)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"mensaje":      "handshake completado exitosamente",
		"token_salida": tokenSalida,
	})
}
