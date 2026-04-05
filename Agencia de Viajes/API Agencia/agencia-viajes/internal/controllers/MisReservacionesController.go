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

// MisReservacionesController
//
// Controlador encargado de exponer las reservaciones del usuario autenticado,
// tanto en formato de listado resumido como en detalle completo consultando
// a los proveedores externos.
type MisReservacionesController struct {
	service *services.MisReservacionesService
}

// NewMisReservacionesController
//
// Crea e inicializa un nuevo MisReservacionesController con el servicio recibido.
//
// Parametros:
//   - s: instancia del servicio de mis reservaciones
//
// Retorna:
//   - *MisReservacionesController: puntero al controlador creado
func NewMisReservacionesController(s *services.MisReservacionesService) *MisReservacionesController {
	return &MisReservacionesController{service: s}
}

// Listar
//
// Handler HTTP que devuelve todas las reservaciones del usuario autenticado
// con los datos almacenados localmente en la base de datos de la agencia.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200: lista de reservaciones del usuario
//   - HTTP 500: error interno si el servicio falla al consultar las reservaciones
//
// Notas:
//   - Ruta esperada: GET /api/reservaciones/mias
func (ctrl *MisReservacionesController) Listar(c *gin.Context) {
	usuarioID, _ := c.Get("usuario_id")

	reservaciones, err := ctrl.service.ListarReservaciones(usuarioID.(int))
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, reservaciones)
}

// Detalle
//
// Handler HTTP que devuelve el detalle completo de una reservacion especifica
// del usuario autenticado, consultando informacion actualizada de los proveedores
// externos.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200: objeto con el detalle completo de la reservacion
//   - HTTP 400: error si el parametro de ruta ID no es un entero valido
//   - HTTP 404: error si la reservacion no existe o no pertenece al usuario
//
// Notas:
//   - Ruta esperada: GET /api/reservaciones/mias/:id
func (ctrl *MisReservacionesController) Detalle(c *gin.Context) {
	usuarioID, _ := c.Get("usuario_id")

	reservacionID, err := strconv.Atoi(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "ID de reservación inválido"})
		return
	}

	detalle, err := ctrl.service.ObtenerDetalle(reservacionID, usuarioID.(int))
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, detalle)
}
