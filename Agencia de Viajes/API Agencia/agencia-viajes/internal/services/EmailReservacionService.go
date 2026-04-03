package services

import (
	"agencia-viajes/internal/helpers"
	"agencia-viajes/internal/repositories"
	"encoding/json"
	"errors"
	"fmt"
)

// EmailReservacionService genera el PDF y envía el correo de confirmación.
type EmailReservacionService struct {
	misSvc  *MisReservacionesService
	pdfSvc  *PdfReservacionService
	usuRepo *repositories.UsuarioRepository
}

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

// EnviarConfirmacion genera el PDF y lo envía al correo del usuario.
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

// buildPDFDataFromResult convierte el resultado de ObtenerDetalle → ReservacionPDFData.
// Reutiliza reservacionDetallePDF y mapearAPDFData definidos en PdfReservacionService.go.
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