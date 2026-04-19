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
	"net/http"

	"github.com/gin-gonic/gin"
)

// BusquedaController
//
// Controlador encargado de gestionar las busquedas de vuelos y hoteles.
// Las rutas son publicas: no requieren AuthRequerido. El controller intenta
// identificar al usuario leyendo la cookie de sesion directamente; si no
// existe o expiro, la busqueda se registra como anonima (UsuarioID = NULL).
type BusquedaController struct {
	service *services.BusquedaService
}

// NewBusquedaController crea una nueva instancia de BusquedaController.
func NewBusquedaController(service *services.BusquedaService) *BusquedaController {
	return &BusquedaController{service: service}
}

// obtenerUsuarioOpcional intenta identificar al usuario leyendo y validando
// la cookie de sesion JWT directamente, sin depender de ningun middleware.
//
// Retorna nil si:
//   - No hay cookie (usuario nunca se logeo)
//   - La cookie fue borrada por /api/usuarios/logout (usuario cerro sesion)
//   - El JWT expiro (sesion vencida)
//
// Retorna el ID si hay una sesion activa valida.
func obtenerUsuarioOpcional(c *gin.Context) *int {
	tokenStr, err := c.Cookie("session")
	if err != nil {
		return nil
	}
	claims, err := helpers.VerificarToken(tokenStr)
	if err != nil {
		return nil
	}
	uid := claims.UsuarioID
	return &uid
}

// BuscarVuelos godoc
// POST /api/busqueda/vuelos — ruta publica, sin AuthRequerido.
func (ctrl *BusquedaController) BuscarVuelos(c *gin.Context) {
	var req dto.BusquedaVuelosRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Body inválido"})
		return
	}

	resultados, err := ctrl.service.BuscarVuelos(c, req, obtenerUsuarioOpcional(c))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, resultados)
}

// BuscarHoteles godoc
// POST /api/busqueda/hoteles — ruta publica, sin AuthRequerido.
func (ctrl *BusquedaController) BuscarHoteles(c *gin.Context) {
	var req dto.BusquedaHotelesRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Body inválido"})
		return
	}

	resultados, err := ctrl.service.BuscarHoteles(c, req, obtenerUsuarioOpcional(c))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, resultados)
}