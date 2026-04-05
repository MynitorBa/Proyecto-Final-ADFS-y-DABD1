// # Package controllers
//
// Controladores HTTP de la agencia de viajes. Cada controlador recibe
// solicitudes de Gin, delega la logica de negocio al servicio correspondiente
// y devuelve la respuesta JSON al cliente.
package controllers

import (
	"agencia-viajes/internal/services"
	"net/http"

	"github.com/gin-gonic/gin"
)

// CatalogoController
//
// Controlador encargado de gestionar la actualizacion del catalogo de productos
// (vuelos, hoteles y paquetes) obtenidos desde los proveedores externos.
type CatalogoController struct {
	service *services.CatalogoService
}

// NewCatalogoController
//
// Crea e inicializa un nuevo CatalogoController con el servicio recibido.
//
// Parametros:
//   - service: instancia del servicio de catalogo
//
// Retorna:
//   - *CatalogoController: puntero al controlador creado
func NewCatalogoController(service *services.CatalogoService) *CatalogoController {
	return &CatalogoController{service: service}
}

// ActualizarCatalogo
//
// Handler HTTP que dispara el proceso de actualizacion del catalogo consultando
// los proveedores registrados y sincronizando la informacion en la base de datos.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200: mensaje de proceso completado junto con el detalle de resultados por proveedor
//   - HTTP 500: error interno si el servicio falla durante la actualizacion
func (ctrl *CatalogoController) ActualizarCatalogo(c *gin.Context) {
	resultados, err := ctrl.service.ActualizarCatalogo()
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"mensaje":    "proceso de actualización completado",
		"resultados": resultados,
	})
}
