package controllers

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/services"
	"net/http"

	"github.com/gin-gonic/gin"
)

type AsientoVueloController struct {
	service *services.AsientoVueloService
}

func NewAsientoVueloController(service *services.AsientoVueloService) *AsientoVueloController {
	return &AsientoVueloController{service: service}
}

// ObtenerAsientos maneja el GET para visualizar el mapa de asientos
// POST o GET /api/reservaciones/asientos-vuelo
// (Si usas ShouldBindJSON, recuerda que el cliente debe enviar un Body JSON)
func (ctrl *AsientoVueloController) ObtenerAsientos(c *gin.Context) {
	// Extraer el usuario del middleware de autenticación
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

	// Llamada al servicio que ahora retorna AsientosVueloResponse (con BoletosAgencia)
	resp, err := ctrl.service.ObtenerAsientosVuelo(usuarioID, req)
	if err != nil {
		// El error devuelto por el servicio ya viene formateado
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, resp)
}

// CambiarAsiento maneja el PUT para actualizar el asiento de un boleto específico
// PUT /api/reservaciones/asientos-vuelo
func (ctrl *AsientoVueloController) CambiarAsiento(c *gin.Context) {
	val, exists := c.Get("usuario_id")
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "usuario no autenticado"})
		return
	}
	usuarioID := val.(int)

	// Usamos la estructura CambiarAsientoVueloRequest que definimos como pública
	var req dto.CambiarAsientoVueloRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "datos de cambio de asiento inválidos"})
		return
	}

	// El servicio se encarga de validar que el boleto pertenezca a la reserva
	if err := ctrl.service.CambiarAsientoVuelo(usuarioID, req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"message": "Solicitud de cambio procesada con éxito por la aerolínea",
	})
}
