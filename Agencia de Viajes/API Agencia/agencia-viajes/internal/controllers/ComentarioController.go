// # Package controllers
//
// Controladores HTTP de la API de Movent. Cada controlador agrupa los handlers
// relacionados a un recurso o dominio especifico de la aplicacion.
package controllers

import (
	"agencia-viajes/internal/services"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
)

// ComentarioController
//
// Controlador que maneja los endpoints de consulta de comentarios
// asociados a vuelos y hoteles de proveedores registrados.
type ComentarioController struct {
	service *services.ComentarioService
}

// NewComentarioController
//
// Constructor que retorna una nueva instancia de ComentarioController
// con el servicio de comentarios inyectado.
//
// Parametros:
//   - s: puntero al servicio de comentarios
//
// Retorna:
//   - *ComentarioController: puntero a la nueva instancia
func NewComentarioController(s *services.ComentarioService) *ComentarioController {
	return &ComentarioController{service: s}
}

// ObtenerComentariosVuelo
//
// Retorna la lista de comentarios asociados a una ruta de vuelo especifica
// de un proveedor dado. Los parametros proveedorId y rutaId se leen desde
// la URL.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200 OK: JSON con la lista de comentarios del vuelo
//   - HTTP 400 Bad Request: si proveedorId o rutaId no son enteros validos
//   - HTTP 500 Internal Server Error: si ocurre un error en la capa de servicio
func (ctrl *ComentarioController) ObtenerComentariosVuelo(c *gin.Context) {
	proveedorID, err := strconv.Atoi(c.Param("proveedorId"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "ID de proveedor inválido"})
		return
	}

	rutaID, err := strconv.Atoi(c.Param("rutaId"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "ID de ruta inválido"})
		return
	}

	comentarios, err := ctrl.service.ObtenerComentariosVuelo(proveedorID, rutaID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, comentarios)
}

// ObtenerComentariosHotel
//
// Retorna la lista de comentarios asociados a un hotel especifico de un
// proveedor dado. Los parametros proveedorId y hotelId se leen desde la URL.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200 OK: JSON con la lista de comentarios del hotel
//   - HTTP 400 Bad Request: si proveedorId o hotelId no son enteros validos
//   - HTTP 500 Internal Server Error: si ocurre un error en la capa de servicio
func (ctrl *ComentarioController) ObtenerComentariosHotel(c *gin.Context) {
	proveedorID, err := strconv.Atoi(c.Param("proveedorId"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "ID de proveedor inválido"})
		return
	}

	hotelID, err := strconv.Atoi(c.Param("hotelId"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "ID de hotel inválido"})
		return
	}

	comentarios, err := ctrl.service.ObtenerComentariosHotel(proveedorID, hotelID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, comentarios)
}
