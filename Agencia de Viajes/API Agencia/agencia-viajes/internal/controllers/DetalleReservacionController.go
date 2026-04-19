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

// DetalleReservacionController
//
// Controlador encargado de agregar detalles a una reservacion existente,
// incluyendo vuelos con sus pasajeros y habitaciones de hotel.
type DetalleReservacionController struct {
	service *services.DetalleReservacionService
}

// NewDetalleReservacionController
//
// Crea e inicializa un nuevo DetalleReservacionController con el servicio recibido.
//
// Parametros:
//   - service: instancia del servicio de detalle de reservacion
//
// Retorna:
//   - *DetalleReservacionController: puntero al controlador creado
func NewDetalleReservacionController(service *services.DetalleReservacionService) *DetalleReservacionController {
	return &DetalleReservacionController{service: service}
}

// AgregarDetalleVuelo
//
// Handler HTTP que agrega un detalle de vuelo a la reservacion del usuario
// autenticado. Valida la sesion y deserializa la solicitud antes de delegar
// al servicio.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200: respuesta del servicio con el detalle de vuelo registrado
//   - HTTP 400: error si el body JSON es invalido o el servicio retorna un error
//   - HTTP 401: error si el usuario no esta autenticado
func (ctrl *DetalleReservacionController) AgregarDetalleVuelo(c *gin.Context) {
	usuarioID, exists := c.Get("usuario_id")
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "usuario no autenticado"})
		return
	}

	var req dto.AgregarDetalleVueloRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "datos inválidos"})
		return
	}

	resp, err := ctrl.service.AgregarDetalleVuelo(c, usuarioID.(int), req)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, resp)
}

// AgregarDetalleHotel
//
// Handler HTTP que agrega un detalle de hotel a la reservacion del usuario
// autenticado. Deserializa la solicitud y delega la operacion al servicio.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200: respuesta del servicio con el detalle de hotel registrado
//   - HTTP 400: error si el body JSON es invalido o el servicio retorna un error
func (ctrl *DetalleReservacionController) AgregarDetalleHotel(c *gin.Context) {
	usuarioID, _ := c.Get("usuario_id")

	var req dto.AgregarDetalleHotelRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "datos inválidos en la solicitud de hotel"})
		return
	}

	resp, err := ctrl.service.AgregarDetalleHotel(c, usuarioID.(int), req)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, resp)
}

// AgregarPasajerosVuelo
//
// Handler HTTP que registra los datos de los pasajeros asociados a un vuelo
// dentro de la reservacion del usuario autenticado.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200: mensaje confirmando que los datos de pasajeros fueron guardados correctamente
//   - HTTP 400: error si el body JSON es invalido o el servicio retorna un error
//   - HTTP 401: error si el usuario no esta autenticado
func (ctrl *DetalleReservacionController) AgregarPasajerosVuelo(c *gin.Context) {
	usuarioID, exists := c.Get("usuario_id")
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "usuario no autenticado"})
		return
	}

	var req dto.AgregarPasajerosVueloRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "datos inválidos"})
		return
	}

	err := ctrl.service.AgregarPasajerosVuelo(c, usuarioID.(int), req)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"mensaje": "datos de pasajeros guardados correctamente"})
}
