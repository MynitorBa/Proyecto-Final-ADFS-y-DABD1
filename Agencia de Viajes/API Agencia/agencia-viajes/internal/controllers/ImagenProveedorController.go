package controllers

import (
	"agencia-viajes/internal/services"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
)

// ImagenProveedorController
//
// Controlador que actua como proxy de imagenes hacia proveedores hoteleros aliados.
// El frontend envia el proveedorID y el imagenID como path params; el controller
// resuelve la URL del proveedor, obtiene la imagen y la sirve directamente.
// Las rutas son publicas: no requieren sesion de usuario.
type ImagenProveedorController struct {
	service *services.ImagenProveedorService
}

// NewImagenProveedorController crea una nueva instancia de ImagenProveedorController.
func NewImagenProveedorController(service *services.ImagenProveedorService) *ImagenProveedorController {
	return &ImagenProveedorController{service: service}
}

// RegisterRoutes registra las rutas de proxy de imagenes en el router de Gin.
//
// Rutas:
//
//	GET /api/imagenes/proveedor/:proveedorId/hotel/:id
//	GET /api/imagenes/proveedor/:proveedorId/habitacion/:id
//	GET /api/imagenes/proveedor/:proveedorId/amenidad/:id
func (ctrl *ImagenProveedorController) RegisterRoutes(r *gin.Engine) {
	g := r.Group("/api/imagenes/proveedor/:proveedorId")
	{
		g.GET("/hotel/:id", ctrl.ObtenerImagenHotel)
		g.GET("/habitacion/:id", ctrl.ObtenerImagenHabitacion)
		g.GET("/amenidad/:id", ctrl.ObtenerImagenAmenidad)
	}
}

// ObtenerImagenHotel godoc
// GET /api/imagenes/proveedor/:proveedorId/hotel/:id
func (ctrl *ImagenProveedorController) ObtenerImagenHotel(c *gin.Context) {
	ctrl.proxy(c, "hotel")
}

// ObtenerImagenHabitacion godoc
// GET /api/imagenes/proveedor/:proveedorId/habitacion/:id
func (ctrl *ImagenProveedorController) ObtenerImagenHabitacion(c *gin.Context) {
	ctrl.proxy(c, "habitacion")
}

// ObtenerImagenAmenidad godoc
// GET /api/imagenes/proveedor/:proveedorId/amenidad/:id
func (ctrl *ImagenProveedorController) ObtenerImagenAmenidad(c *gin.Context) {
	ctrl.proxy(c, "amenidad")
}

// proxy es el handler interno compartido por los tres endpoints.
// Extrae los path params, llama al service y escribe los bytes
// de la imagen directamente en la respuesta HTTP.
func (ctrl *ImagenProveedorController) proxy(c *gin.Context, tipoImagen string) {
	proveedorID, err := strconv.Atoi(c.Param("proveedorId"))
	if err != nil {
		c.Status(http.StatusBadRequest)
		return
	}

	imagenID, err := strconv.Atoi(c.Param("id"))
	if err != nil {
		c.Status(http.StatusBadRequest)
		return
	}

	bytes, contentType, err := ctrl.service.ObtenerImagen(proveedorID, tipoImagen, imagenID)
	if err != nil {
		c.Status(http.StatusNotFound)
		return
	}

	c.Data(http.StatusOK, contentType, bytes)
}
