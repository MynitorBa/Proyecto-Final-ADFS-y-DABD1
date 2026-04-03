package services

import (
	"agencia-viajes/internal/helpers"
	"agencia-viajes/internal/repositories"
	"encoding/json"
	"errors"
	"fmt"
)

// ── Structs de deserialización ─────────────────────────────────────────────

type reservacionDetallePDF struct {
	ID            int          `json:"id"`
	NoReservacion string       `json:"no_reservacion"`
	TipoReserva   int          `json:"tipo_reserva"`
	EstadoID      int          `json:"estado_id"`
	Total         float64      `json:"total"`
	FechaCreacion string       `json:"fecha_creacion"`
	Detalles      []detallePDF `json:"detalles"`
}

type detallePDF struct {
	TipoDetalleID int             `json:"tipo_detalle_id"`
	DataProveedor json.RawMessage `json:"data_proveedor"`
}

type dataVueloPDF struct {
	Boletos []struct {
		NoBoleto      string  `json:"noBoleto"`
		NumeroVuelo   string  `json:"numeroVuelo"`
		Clase         string  `json:"clase"`
		NoAsiento     string  `json:"noAsiento"`
		OrigenCodigo  string  `json:"origenCodigo"`
		OrigenCiudad  string  `json:"origenCiudad"`
		DestinoCodigo string  `json:"destinoCodigo"`
		DestinoCiudad string  `json:"destinoCiudad"`
		DestinaCiudad string  `json:"destinaCiudad"`
		HoraSalida    string  `json:"horaSalida"`
		HoraLlegada   string  `json:"horaLlegada"`
		FechaVuelo    string  `json:"fechaVuelo"`
		AvionMarca    string  `json:"avionMarca"`
		AvionModelo   string  `json:"avionModelo"`
		Precio        float64 `json:"precio"`
		EstadoBoleto  string  `json:"estadoBoleto"`
		Pasajero      *struct {
			Nombre   string `json:"nombre"`
			Apellido string `json:"apellido"`
		} `json:"pasajero"`
	} `json:"boletos"`
	EstadoReserva string `json:"estadoReserva"`
	UsuarioNombre string `json:"usuarioNombre"`
	UsuarioEmail  string `json:"usuarioEmail"`
}

type habitacionProveedorPDF struct {
	NombreHotel      string  `json:"nombreHotel"`
	TipoHabitacion   string  `json:"tipoHabitacion"`
	TipoCama         string  `json:"tipoCama"`
	NumeroHabitacion string  `json:"numeroHabitacion"`
	FechaCheckIn     string  `json:"fechaCheckIn"`
	FechaCheckOut    string  `json:"fechaCheckOut"`
	CantidadPersonas int     `json:"cantidadPersonas"`
	TotalDetalle     float64 `json:"totalDetalle"`
	Estado           string  `json:"estado"`
}

var estadoIDLabelPDF = map[int]string{
	1: "Pendiente", 2: "Confirmada", 3: "Cancelada",
	4: "Expirada", 5: "Completada", 6: "En Curso",
}

// ── Service ────────────────────────────────────────────────────────────────

type PdfReservacionService struct {
	misSvc  *MisReservacionesService
	usuRepo *repositories.UsuarioRepository
}

func NewPdfReservacionService(
	misSvc *MisReservacionesService,
	usuRepo *repositories.UsuarioRepository,
) *PdfReservacionService {
	return &PdfReservacionService{misSvc: misSvc, usuRepo: usuRepo}
}

// GenerarPDF obtiene el detalle completo y produce el PDF.
func (s *PdfReservacionService) GenerarPDF(reservacionID, usuarioID int) ([]byte, error) {
	// 1. Detalle completo desde proveedores
	resultado, err := s.misSvc.ObtenerDetalle(reservacionID, usuarioID)
	if err != nil {
		return nil, errors.New("reservación no encontrada")
	}

	// 2. Marshal → Unmarshal al struct de PDF
	jsonBytes, err := json.Marshal(resultado)
	if err != nil {
		return nil, fmt.Errorf("error serializando: %w", err)
	}
	var raw reservacionDetallePDF
	if err := json.Unmarshal(jsonBytes, &raw); err != nil {
		return nil, fmt.Errorf("error deserializando: %w", err)
	}

	// 3. Mapear a ReservacionPDFData
	pdfData, err := mapearAPDFData(raw)
	if err != nil {
		return nil, fmt.Errorf("error mapeando: %w", err)
	}

	// 4. Si no hay titular (reservas de hotel puro), consultarlo desde la DB
	if pdfData.UsuarioNombre == "" || pdfData.UsuarioEmail == "" {
		if s.usuRepo != nil {
			if nombre, email, err2 := s.usuRepo.ObtenerNombreYEmail(usuarioID); err2 == nil {
				if pdfData.UsuarioNombre == "" {
					pdfData.UsuarioNombre = nombre
				}
				if pdfData.UsuarioEmail == "" {
					pdfData.UsuarioEmail = email
				}
			}
		}
	}

	// 5. Generar PDF
	return helpers.GenerarPDFReservacion(pdfData)
}

// ── Mapeador ───────────────────────────────────────────────────────────────

func mapearAPDFData(raw reservacionDetallePDF) (helpers.ReservacionPDFData, error) {
	data := helpers.ReservacionPDFData{
		NoReservacion: raw.NoReservacion,
		EstadoReserva: estadoIDLabelPDF[raw.EstadoID],
		FechaCreacion: raw.FechaCreacion,
		Total:         raw.Total,
		TipoReserva:   raw.TipoReserva,
	}

	for _, det := range raw.Detalles {
		switch det.TipoDetalleID {

		case 1: // Vuelo (Broom AirLine)
			var dp dataVueloPDF
			if err := json.Unmarshal(det.DataProveedor, &dp); err != nil {
				continue
			}
			if dp.EstadoReserva != "" {
				data.EstadoReserva = dp.EstadoReserva
			}
			if data.UsuarioNombre == "" {
				data.UsuarioNombre = dp.UsuarioNombre
			}
			if data.UsuarioEmail == "" {
				data.UsuarioEmail = dp.UsuarioEmail
			}
			for _, b := range dp.Boletos {
				dest := b.DestinoCiudad
				if dest == "" {
					dest = b.DestinaCiudad
				}
				pas := ""
				if b.Pasajero != nil {
					pas = b.Pasajero.Nombre + " " + b.Pasajero.Apellido
				}
				data.Boletos = append(data.Boletos, helpers.BoletoPDF{
					NoBoleto:       b.NoBoleto,
					NumeroVuelo:    b.NumeroVuelo,
					Clase:          b.Clase,
					NoAsiento:      b.NoAsiento,
					OrigenCodigo:   b.OrigenCodigo,
					OrigenCiudad:   b.OrigenCiudad,
					DestinoCodigo:  b.DestinoCodigo,
					DestinoCiudad:  dest,
					HoraSalida:     b.HoraSalida,
					HoraLlegada:    b.HoraLlegada,
					FechaVuelo:     b.FechaVuelo,
					AvionMarca:     b.AvionMarca,
					AvionModelo:    b.AvionModelo,
					Precio:         b.Precio,
					EstadoBoleto:   b.EstadoBoleto,
					PasajeroNombre: pas,
				})
			}

		case 2: // Hotel (Miku Inn)
			var habs []habitacionProveedorPDF
			if err := json.Unmarshal(det.DataProveedor, &habs); err != nil {
				continue
			}
			for _, h := range habs {
				data.Habitaciones = append(data.Habitaciones, helpers.HabitacionPDF{
					NombreHotel:      h.NombreHotel,
					TipoHabitacion:   h.TipoHabitacion,
					TipoCama:         h.TipoCama,
					NumeroHabitacion: h.NumeroHabitacion,
					FechaCheckIn:     h.FechaCheckIn,
					FechaCheckOut:    h.FechaCheckOut,
					CantidadPersonas: h.CantidadPersonas,
					TotalDetalle:     h.TotalDetalle,
					Estado:           h.Estado,
				})
			}
		}
	}

	return data, nil
}