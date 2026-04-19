// # Package controllers
//
// Controladores HTTP de la API de Movent. Cada controlador agrupa los handlers
// relacionados a un recurso o dominio especifico de la aplicacion.
package controllers

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/helpers"
	"agencia-viajes/internal/services"
	"fmt"
	"log"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
)

// ReservacionController
//
// Controlador que maneja los endpoints de creacion de reservaciones,
// descarga de PDF y envio de correo de confirmacion al usuario autenticado.
type ReservacionController struct {
	service      *services.ReservacionService
	pdfService   *services.PdfReservacionService
	emailService *services.EmailReservacionService
	logSesion    *services.LogSesionService
}

// NewReservacionController
//
// Constructor que retorna una nueva instancia de ReservacionController
// con los servicios de reservacion, PDF, correo y log inyectados.
//
// Parametros:
//   - service: servicio principal de reservaciones
//   - pdfService: servicio de generacion de PDF
//   - emailService: servicio de envio de correo de confirmacion
//   - logSesion: servicio de log de sesion para auditoria de reservaciones
//
// Retorna:
//   - *ReservacionController: puntero a la nueva instancia
func NewReservacionController(
	service *services.ReservacionService,
	pdfService *services.PdfReservacionService,
	emailService *services.EmailReservacionService,
	logSesion *services.LogSesionService,
) *ReservacionController {
	return &ReservacionController{
		service:      service,
		pdfService:   pdfService,
		emailService: emailService,
		logSesion:    logSesion,
	}
}

// CrearReservacion
//
// Crea una nueva reservacion para el usuario autenticado. Valida que el
// tipo de reserva sea 1 (Aerolinea), 2 (Hotelera) o 3 (Paquete) antes
// de delegar al servicio.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 201 Created: JSON con los datos de la reservacion creada
//   - HTTP 400 Bad Request: si el body es invalido o el tipo de reserva es incorrecto
//   - HTTP 401 Unauthorized: si el usuario no esta autenticado
//   - HTTP 500 Internal Server Error: si ocurre un error en la capa de servicio
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

	// Log de reserva creada (evento pre-compra, antes de pasar a pago)
	uid := usuarioID.(int)
	ctrl.logSesion.Registrar(c, helpers.TipoReservaCreada,
		&uid, resp.NoReservacion,
		fmt.Sprintf("Reservación %s creada (tipo %d)", resp.NoReservacion, req.TipoReservaID))

	c.JSON(http.StatusCreated, resp)
}

// DescargarPDF
//
// Genera y retorna el PDF de una reservacion especifica del usuario autenticado.
// El ID de la reservacion se lee desde el parametro de URL :id.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200 OK: archivo PDF adjunto con nombre reservacion-{id}.pdf
//   - HTTP 400 Bad Request: si el ID de la reservacion no es un entero valido
//   - HTTP 401 Unauthorized: si el usuario no esta autenticado
//   - HTTP 404 Not Found: si la reservacion no existe
//   - HTTP 500 Internal Server Error: si ocurre un error al generar el PDF
//
// Notas:
//   - El PDF se entrega con Content-Disposition attachment para forzar la descarga
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

// EnviarCorreo
//
// Envia un correo de confirmacion al usuario autenticado para una reservacion
// especifica. El ID de la reservacion se lee desde el parametro de URL :id.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200 OK: JSON con mensaje de exito al enviar el correo
//   - HTTP 400 Bad Request: si el ID de la reservacion no es un entero valido
//   - HTTP 401 Unauthorized: si el usuario no esta autenticado
//   - HTTP 500 Internal Server Error: si ocurre un error al enviar el correo
//
// Notas:
//   - Los errores del servicio de correo se registran en el log del servidor
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
