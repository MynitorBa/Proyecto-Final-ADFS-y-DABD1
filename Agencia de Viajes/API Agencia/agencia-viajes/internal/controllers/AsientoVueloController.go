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

// AsientoVueloController
//
// Controlador encargado de gestionar el mapa de asientos de un vuelo
// y el cambio de asiento de un boleto especifico.
type AsientoVueloController struct {
	service *services.AsientoVueloService
}

// NewAsientoVueloController
//
// Crea e inicializa un nuevo AsientoVueloController con el servicio recibido.
//
// Parametros:
//   - service: instancia del servicio de asientos de vuelo
//
// Retorna:
//   - *AsientoVueloController: puntero al controlador creado
func NewAsientoVueloController(service *services.AsientoVueloService) *AsientoVueloController {
	return &AsientoVueloController{service: service}
}

// ObtenerAsientos
//
// Handler HTTP que devuelve el mapa de asientos de un vuelo para el usuario
// autenticado. Valida la sesion del usuario y delega la consulta al servicio.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200: objeto AsientosVueloResponse con el mapa de asientos y boletos de la agencia
//   - HTTP 400: error si el body JSON es invalido o el servicio retorna un error
//   - HTTP 401: error si el usuario no esta autenticado
func (ctrl *AsientoVueloController) ObtenerAsientos(c *gin.Context) {
	val, exists := c.Get("usuario_id")
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "usuario no autenticado"})
		return
	}
	usuarioID := val.(int)

	var req dto.ObtenerAsientosVueloRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "formato de petición inválido"})
		return
	}

	resp, err := ctrl.service.ObtenerAsientosVuelo(c, usuarioID, req)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, resp)
}

// CambiarAsiento — pegar después de ObtenerAsientos
func (ctrl *AsientoVueloController) CambiarAsiento(c *gin.Context) {
	val, exists := c.Get("usuario_id")
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "usuario no autenticado"})
		return
	}
	usuarioID := val.(int)

	var req dto.CambiarAsientoVueloRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "formato de petición inválido"})
		return
	}

	if err := ctrl.service.CambiarAsientoVuelo(c, usuarioID, req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"mensaje": "asiento actualizado correctamente"})
}
