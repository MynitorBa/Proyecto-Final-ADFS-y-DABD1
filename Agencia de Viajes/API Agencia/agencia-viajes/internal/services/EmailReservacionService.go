// # Package services
//
// Contiene los servicios de negocio de la agencia de viajes,
// incluyendo procesamiento de pagos, reservaciones, proveedores y usuarios.
package services

import (
	"agencia-viajes/internal/helpers"
	"agencia-viajes/internal/repositories"
	"encoding/json"
	"errors"
	"fmt"
)

// EmailReservacionService
//
// Servicio encargado de generar el PDF de confirmacion de una reservacion
// y enviarlo al correo electronico del usuario titular.
type EmailReservacionService struct {
	misSvc  *MisReservacionesService
	pdfSvc  *PdfReservacionService
	usuRepo *repositories.UsuarioRepository
}

// NewEmailReservacionService
//
// Crea e inicializa una nueva instancia de EmailReservacionService con sus dependencias.
//
// Parametros:
//   - misSvc: servicio de mis reservaciones para obtener el detalle completo desde proveedores
//   - pdfSvc: servicio de PDF para generar el archivo adjunto
//   - usuRepo: repositorio de usuarios para obtener nombre y correo cuando no los provee el proveedor
//
// Retorna:
//   - *EmailReservacionService: instancia inicializada del servicio de envio de correos
func NewEmailReservacionService(
	misSvc *MisReservacionesService,
	pdfSvc *PdfReservacionService,
	usuRepo *repositories.UsuarioRepository,
) *EmailReservacionService {
	return &EmailReservacionService{
		misSvc:  misSvc,
		pdfSvc:  pdfSvc,
		usuRepo: usuRepo,
	}
}

// EnviarConfirmacion
//
// Ejecuta el flujo completo de envio del correo de confirmacion: obtiene el detalle
// de la reservacion desde los proveedores, prepara los datos del PDF, completa nombre
// y correo del usuario desde la base de datos si es necesario (caso reservas de hotel puro),
// genera el PDF como adjunto, construye el cuerpo HTML del correo y lo envia al destinatario.
//
// Parametros:
//   - reservacionID: identificador de la reservacion a confirmar
//   - usuarioID: identificador del usuario propietario de la reservacion
//
// Retorna:
//   - error: error si la reservacion no existe, no se puede determinar el correo destinatario,
//     falla la generacion del PDF o el envio del correo
func (s *EmailReservacionService) EnviarConfirmacion(reservacionID, usuarioID int) error {
	// 1. Datos completos del proveedor
	resultado, err := s.misSvc.ObtenerDetalle(reservacionID, usuarioID)
	if err != nil {
		return errors.New("reservación no encontrada")
	}

	// 2. Mapear a struct PDF
	pdfData, err := buildPDFDataFromResult(resultado)
	if err != nil {
		return fmt.Errorf("error preparando datos: %w", err)
	}

	// 3. Si el email o nombre vienen vacíos (ej: reserva de hotel puro),
	//    consultarlos directamente desde la tabla Usuario de MOVENT.
	if pdfData.UsuarioEmail == "" || pdfData.UsuarioNombre == "" {
		if nombre, email, err2 := s.usuRepo.ObtenerNombreYEmail(usuarioID); err2 == nil {
			if pdfData.UsuarioNombre == "" {
				pdfData.UsuarioNombre = nombre
			}
			if pdfData.UsuarioEmail == "" {
				pdfData.UsuarioEmail = email
			}
		}
	}

	// 4. Verificar que tenemos destinatario
	destinatario := pdfData.UsuarioEmail
	if destinatario == "" {
		return fmt.Errorf("no se encontró correo para el usuario %d", usuarioID)
	}

	// 5. Generar PDF (reutiliza PdfReservacionService para no duplicar lógica)
	pdfBytes, err := s.pdfSvc.GenerarPDF(reservacionID, usuarioID)
	if err != nil {
		return fmt.Errorf("error generando PDF adjunto: %w", err)
	}

	// 6. Construir HTML y enviar
	htmlBody      := helpers.BuildHTMLEmail(pdfData)
	asunto        := fmt.Sprintf("MOVENT · Confirmacion de reservacion %s", pdfData.NoReservacion)
	nombreArchivo := fmt.Sprintf("MOVENT-%s.pdf", pdfData.NoReservacion)

	if err := helpers.EnviarEmailConPDF(destinatario, asunto, htmlBody, pdfBytes, nombreArchivo); err != nil {
		return fmt.Errorf("error enviando correo a %s: %w", destinatario, err)
	}

	return nil
}

// buildPDFDataFromResult
//
// Convierte el resultado generico de ObtenerDetalle al struct ReservacionPDFData
// reutilizando reservacionDetallePDF y mapearAPDFData definidos en PdfReservacionService.go.
// Serializa el resultado a JSON y lo deserializa al struct interno antes de mapear.
//
// Parametros:
//   - resultado: respuesta generica del servicio ObtenerDetalle con el detalle completo de la reservacion
//
// Retorna:
//   - helpers.ReservacionPDFData: datos formateados listos para la generacion del PDF y del correo
//   - error: error si falla la serializacion, la deserializacion o el mapeo de datos
func buildPDFDataFromResult(resultado interface{}) (helpers.ReservacionPDFData, error) {
	jsonBytes, err := json.Marshal(resultado)
	if err != nil {
		return helpers.ReservacionPDFData{}, fmt.Errorf("error serializando: %w", err)
	}

	var raw reservacionDetallePDF
	if err := json.Unmarshal(jsonBytes, &raw); err != nil {
		return helpers.ReservacionPDFData{}, fmt.Errorf("error deserializando: %w", err)
	}

	return mapearAPDFData(raw)
}
