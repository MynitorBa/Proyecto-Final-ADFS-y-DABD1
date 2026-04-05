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

// BusquedaController
//
// Controlador encargado de gestionar las busquedas de vuelos y hoteles
// disponibles a traves de los proveedores registrados.
type BusquedaController struct {
	service *services.BusquedaService
}

// NewBusquedaController
//
// Crea e inicializa un nuevo BusquedaController con el servicio recibido.
//
// Parametros:
//   - service: instancia del servicio de busqueda
//
// Retorna:
//   - *BusquedaController: puntero al controlador creado
func NewBusquedaController(service *services.BusquedaService) *BusquedaController {
	return &BusquedaController{service: service}
}

// BuscarVuelos
//
// Handler HTTP que recibe los criterios de busqueda de vuelos y retorna
// los resultados obtenidos de los proveedores de aerolineas.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200: lista de vuelos disponibles que coinciden con los criterios
//   - HTTP 400: error si el body JSON es invalido o el servicio retorna un error
//
// Notas:
//   - Ruta esperada: POST /busqueda/vuelos
func (ctrl *BusquedaController) BuscarVuelos(c *gin.Context) {
	var req dto.BusquedaVuelosRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Body inválido"})
		return
	}

	resultados, err := ctrl.service.BuscarVuelos(req)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, resultados)
}

// BuscarHoteles
//
// Handler HTTP que recibe los criterios de busqueda de hoteles y retorna
// los resultados obtenidos de los proveedores hoteleros.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200: lista de hoteles disponibles que coinciden con los criterios
//   - HTTP 400: error si el body JSON es invalido o el servicio retorna un error
//
// Notas:
//   - Ruta esperada: POST /busqueda/hoteles
func (ctrl *BusquedaController) BuscarHoteles(c *gin.Context) {
	var req dto.BusquedaHotelesRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Body inválido"})
		return
	}

	resultados, err := ctrl.service.BuscarHoteles(req)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, resultados)
}
