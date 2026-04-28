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

// reservacionDetallePDF
//
// Struct interno de deserializacion que representa el detalle completo
// de una reservacion obtenido desde los proveedores, usado para la generacion del PDF.
type reservacionDetallePDF struct {
	ID            int          `json:"id"`
	NoReservacion string       `json:"no_reservacion"`
	TipoReserva   int          `json:"tipo_reserva"`
	EstadoID      int          `json:"estado_id"`
	Total         float64      `json:"total"`
	FechaCreacion string       `json:"fecha_creacion"`
	Detalles      []detallePDF `json:"detalles"`
}

// detallePDF
//
// Struct interno que representa un detalle individual de una reservacion,
// con el tipo de detalle, el total, los parametros y los datos crudos del proveedor en formato JSON.
type detallePDF struct {
	TipoDetalleID int             `json:"tipo_detalle_id"`
	Total         float64         `json:"total"`
	ParametrosJson interface{}    `json:"parametros_json"`
	DataProveedor json.RawMessage `json:"data_proveedor"`
}

// dataVueloPDF
//
// Struct interno de deserializacion para los datos de vuelo recibidos
// desde el proveedor de aerolinea, incluyendo boletos y datos del usuario titular.
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

// habitacionProveedorPDF
//
// Struct interno de deserializacion para los datos de habitacion de hotel
// recibidos desde el proveedor hotelero.
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

// estadoIDLabelPDF
//
// Mapa que relaciona el ID de estado de una reservacion con su etiqueta
// de texto para mostrar en el PDF generado.
var estadoIDLabelPDF = map[int]string{
	1: "Pendiente", 2: "Confirmada", 3: "Cancelada",
	4: "Expirada", 5: "Completada", 6: "En Curso",
}

// PdfReservacionService
//
// Servicio encargado de generar el PDF de una reservacion combinando
// los datos obtenidos desde los proveedores externos con la informacion
// del usuario almacenada en la base de datos propia.
type PdfReservacionService struct {
	misSvc  *MisReservacionesService
	usuRepo *repositories.UsuarioRepository
}

// NewPdfReservacionService
//
// Crea e inicializa una nueva instancia de PdfReservacionService con sus dependencias.
//
// Parametros:
//   - misSvc: servicio de mis reservaciones para obtener el detalle completo desde proveedores
//   - usuRepo: repositorio de usuarios para consultar nombre y correo cuando no los provee la aerolinea
//
// Retorna:
//   - *PdfReservacionService: instancia inicializada del servicio de generacion de PDF
func NewPdfReservacionService(
	misSvc *MisReservacionesService,
	usuRepo *repositories.UsuarioRepository,
) *PdfReservacionService {
	return &PdfReservacionService{misSvc: misSvc, usuRepo: usuRepo}
}

// GenerarPDF
//
// Obtiene el detalle completo de una reservacion desde los proveedores, lo mapea
// a la estructura de datos del PDF, completa el nombre y correo del usuario
// desde la base de datos si no fueron provistos por el proveedor (caso reservas de hotel puro),
// y genera los bytes del archivo PDF.
//
// Parametros:
//   - reservacionID: identificador de la reservacion a convertir en PDF
//   - usuarioID: identificador del usuario propietario de la reservacion
//
// Retorna:
//   - []byte: bytes del PDF generado
//   - error: error si la reservacion no existe, falla la serializacion,
//     el mapeo de datos o la generacion del PDF
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

// mapearAPDFData
//
// Convierte la estructura cruda de deserializacion de una reservacion al formato
// ReservacionPDFData usado por el helper de generacion de PDF. Procesa cada detalle
// segun su tipo: vuelo (tipo 1) extrae boletos y datos del titular, hotel (tipo 2)
// extrae las habitaciones reservadas.
//
// Calcula el Subtotal extrayendo los totales directamente de data_proveedor
// (sin exponerlos individualmente en el HTML, solo se usa para el cálculo).
//
// Parametros:
//   - raw: datos crudos deserializados de la reservacion
//
// Retorna:
//   - helpers.ReservacionPDFData: datos formateados listos para la generacion del PDF
//   - error: siempre nil, los errores de deserializacion de detalles individuales se ignoran
func mapearAPDFData(raw reservacionDetallePDF) (helpers.ReservacionPDFData, error) {
	data := helpers.ReservacionPDFData{
		NoReservacion: raw.NoReservacion,
		EstadoReserva: estadoIDLabelPDF[raw.EstadoID],
		FechaCreacion: raw.FechaCreacion,
		Total:         raw.Total,
		TipoReserva:   raw.TipoReserva,
	}

	// Calcular Subtotal extrayendo totales desde data_proveedor
	// (no se muestran individualmente, solo se usan para el cálculo)
	var subtotal float64
	for _, det := range raw.Detalles {
		totalProveedor := extraerTotalProveedorPDF(det)
		subtotal += totalProveedor
	}
	data.Subtotal = subtotal
	data.MontoImpuestos = raw.Total - subtotal
	if data.MontoImpuestos < 0 {
		data.MontoImpuestos = 0
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

// extraerTotalProveedorPDF
//
// Extrae el total del proveedor desde data_proveedor segun el tipo de detalle.
// Para vuelos (tipo 1): suma los precios de los boletos.
// Para hoteles (tipo 2): suma el totalDetalle de las habitaciones.
// Usado internamente para calcular subtotales, NO se expone en HTML.
//
// Parametros:
//   - det: detalle con tipo_detalle_id y data_proveedor
//
// Retorna:
//   - float64: total extraido del proveedor, o 0 si no disponible
func extraerTotalProveedorPDF(det detallePDF) float64 {
	if det.DataProveedor == nil || len(det.DataProveedor) == 0 {
		return 0
	}

	// Unmarshal a map generico para inspeccionar la estructura
	var dataMap map[string]interface{}
	if err := json.Unmarshal(det.DataProveedor, &dataMap); err != nil {
		return 0
	}

	// Vuelo (tipo_detalle_id = 1): sumar precios de boletos
	if det.TipoDetalleID == 1 {
		if boletosList, ok := dataMap["boletos"].([]interface{}); ok {
			var total float64
			for _, b := range boletosList {
				if boleto, ok := b.(map[string]interface{}); ok {
					if precio, ok := boleto["precio"].(float64); ok {
						total += precio
					}
				}
			}
			if total > 0 {
				return total
			}
		}
		// Fallback: retornar total directo si existe
		if totalVal, ok := dataMap["total"].(float64); ok {
			return totalVal
		}
		return 0
	}

	// Hotel (tipo_detalle_id = 2): sumar totalDetalle de habitaciones
	if det.TipoDetalleID == 2 {
		// Puede ser array o objeto con habitaciones
		if habitaciones, ok := dataMap["habitaciones"].([]interface{}); ok {
			var total float64
			for _, h := range habitaciones {
				if hab, ok := h.(map[string]interface{}); ok {
					if totalDetalle, ok := hab["totalDetalle"].(float64); ok {
						total += totalDetalle
					} else if totalVal, ok := hab["total"].(float64); ok {
						total += totalVal
					}
				}
			}
			if total > 0 {
				return total
			}
		}
		// Fallback: total directo
		if totalVal, ok := dataMap["total"].(float64); ok {
			return totalVal
		}
		if totalDetalle, ok := dataMap["totalDetalle"].(float64); ok {
			return totalDetalle
		}
		return 0
	}

	return 0
}
