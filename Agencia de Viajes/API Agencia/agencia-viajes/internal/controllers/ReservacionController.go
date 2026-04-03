package controllers

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/services"
	"log"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
)

type ReservacionController struct {
	service      *services.ReservacionService
	pdfService   *services.PdfReservacionService
	emailService *services.EmailReservacionService
}

func NewReservacionController(
	service *services.ReservacionService,
	pdfService *services.PdfReservacionService,
	emailService *services.EmailReservacionService,
) *ReservacionController {
	return &ReservacionController{
		service:      service,
		pdfService:   pdfService,
		emailService: emailService,
	}
}

// POST /api/reservaciones
func (ctrl *ReservacionController) CrearReservacion(c *gin.Context) {
	usuarioID, exists := c.Get("usuario_id")
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "usuario no autenticado"})
		return
	}

	var req dto.CrearReservacionRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "datos inválidos"})
		return
	}

	if req.TipoReservaID < 1 || req.TipoReservaID > 3 {
		c.JSON(http.StatusBadRequest, gin.H{"error": "tipo_reserva_id inválido. Use 1=Aerolinea, 2=Hotelera, 3=Paquete"})
		return
	}

	resp, err := ctrl.service.CrearReservacion(usuarioID.(int), req.TipoReservaID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusCreated, resp)
}

// GET /api/reservaciones/:id/pdf
func (ctrl *ReservacionController) DescargarPDF(c *gin.Context) {
	usuarioID, exists := c.Get("usuario_id")
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "usuario no autenticado"})
		return
	}

	id, err := strconv.Atoi(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "id de reservación inválido"})
		return
	}

	pdfBytes, err := ctrl.pdfService.GenerarPDF(id, usuarioID.(int))
	if err != nil {
		if err.Error() == "reservación no encontrada" {
			c.JSON(http.StatusNotFound, gin.H{"error": err.Error()})
			return
		}
		log.Printf("[PDF] Error generando PDF reservación %d: %v", id, err)
		c.JSON(http.StatusInternalServerError, gin.H{"error": "error al generar el PDF: " + err.Error()})
		return
	}

	c.Header("Content-Disposition", "attachment; filename=\"reservacion-"+strconv.Itoa(id)+".pdf\"")
	c.Header("Content-Type", "application/pdf")
	c.Header("Content-Length", strconv.Itoa(len(pdfBytes)))
	c.Data(http.StatusOK, "application/pdf", pdfBytes)
}

// POST /api/reservaciones/:id/correo
func (ctrl *ReservacionController) EnviarCorreo(c *gin.Context) {
	usuarioID, exists := c.Get("usuario_id")
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "usuario no autenticado"})
		return
	}

	id, err := strconv.Atoi(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "id de reservación inválido"})
		return
	}

	if err := ctrl.emailService.EnviarConfirmacion(id, usuarioID.(int)); err != nil {
		// Loguear error real en consola del servidor
		log.Printf("[CORREO] Error enviando correo reservación %d: %v", id, err)
		// Devolver el error real al cliente para facilitar diagnóstico
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"mensaje": "Correo de confirmación enviado exitosamente"})
}