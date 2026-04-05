// # Package helpers
//
// Provee funciones auxiliares reutilizables para tareas comunes de la
// aplicacion Movent: generacion de tokens, hashing de contrasenas,
// manejo de sesiones JWT, envio de correos electronicos y generacion
// de documentos PDF.
package helpers

import (
	"bytes"
	"fmt"
	"strings"

	"github.com/jung-kurt/gofpdf"
)

// ── DTOs ──────────────────────────────────────────────────────────────────

// ReservacionPDFData
//
// Agrupa todos los datos necesarios para generar el PDF y el correo
// HTML de una reservacion. Contiene informacion del encabezado,
// del usuario titular y las listas de boletos y habitaciones.
type ReservacionPDFData struct {
	NoReservacion string
	EstadoReserva string
	FechaCreacion string
	Total         float64
	TipoReserva   int
	UsuarioNombre string
	UsuarioEmail  string
	Boletos       []BoletoPDF
	Habitaciones  []HabitacionPDF
}

// BoletoPDF
//
// Representa los datos de un boleto de vuelo para incluir
// en el PDF o correo de confirmacion de reservacion.
type BoletoPDF struct {
	NoBoleto       string
	NumeroVuelo    string
	Clase          string
	NoAsiento      string
	OrigenCodigo   string
	OrigenCiudad   string
	DestinoCodigo  string
	DestinoCiudad  string
	HoraSalida     string
	HoraLlegada    string
	FechaVuelo     string
	AvionMarca     string
	AvionModelo    string
	Precio         float64
	EstadoBoleto   string
	PasajeroNombre string
}

// HabitacionPDF
//
// Representa los datos de una habitacion de hotel para incluir
// en el PDF o correo de confirmacion de reservacion.
type HabitacionPDF struct {
	NombreHotel      string
	TipoHabitacion   string
	TipoCama         string
	NumeroHabitacion string
	FechaCheckIn     string
	FechaCheckOut    string
	CantidadPersonas int
	TotalDetalle     float64
	Estado           string
}

// ── Paleta MOVENT ─────────────────────────────────────────────────────────
// Solo 3 colores base: oscuro, amarillo, crema

var (
	cOsc  = [3]int{28, 26, 24}     // #1C1A18
	cAma  = [3]int{255, 204, 0}    // #FFCC00
	cCre  = [3]int{245, 242, 236}  // #F5F2EC
	cBla  = [3]int{255, 255, 255}  // blanco
	cGri  = [3]int{154, 144, 137}  // #9A9089 gris suave
	cBor  = [3]int{220, 212, 200}  // borde
	cAlt  = [3]int{250, 247, 242}  // fila alt
	cTxt  = [3]int{74, 64, 53}     // texto
	cVer  = [3]int{22, 100, 52}    // verde estado OK
	cRoj  = [3]int{180, 30, 30}    // rojo estado cancelado
	cNar  = [3]int{140, 90, 20}    // naranja estado pendiente
)

// fill establece el color de relleno del PDF con los valores RGB dados.
func fill(pdf *gofpdf.Fpdf, c [3]int) { pdf.SetFillColor(c[0], c[1], c[2]) }

// text establece el color de texto del PDF con los valores RGB dados.
func text(pdf *gofpdf.Fpdf, c [3]int) { pdf.SetTextColor(c[0], c[1], c[2]) }

// draw establece el color de linea del PDF con los valores RGB dados.
func draw(pdf *gofpdf.Fpdf, c [3]int) { pdf.SetDrawColor(c[0], c[1], c[2]) }

// ── Encoding UTF-8 → Latin-1 ─────────────────────────────────────────────

// e convierte caracteres UTF-8 especiales del espanol a su equivalente
// Latin-1 para compatibilidad con la libreria gofpdf.
func e(s string) string {
	r := strings.NewReplacer(
		"á", "\xe1", "é", "\xe9", "í", "\xed", "ó", "\xf3", "ú", "\xfa",
		"Á", "\xc1", "É", "\xc9", "Í", "\xcd", "Ó", "\xd3", "Ú", "\xda",
		"ñ", "\xf1", "Ñ", "\xd1", "ü", "\xfc", "ö", "\xf6",
		"·", "\xb7", "—", "-", "–", "-",
		"\u2019", "'", "\u201c", "\"", "\u201d", "\"",
	)
	return r.Replace(s)
}

// dash retorna un guion si la cadena s esta vacia o solo espacios,
// de lo contrario retorna la cadena convertida a Latin-1 mediante e().
func dash(s string) string {
	if strings.TrimSpace(s) == "" {
		return "-"
	}
	return e(s)
}

// ── Entrypoint ─────────────────────────────────────────────────────────────

// GenerarPDFReservacion
//
// Genera el documento PDF de comprobante de reservacion a partir de
// los datos proporcionados. Construye el layout en dos columnas con
// un header fijo, banda de resumen, datos generales, boletos o
// habitaciones, total y condiciones. Retorna los bytes del PDF.
//
// Parametros:
//   - data: struct ReservacionPDFData con toda la informacion de la reservacion
//
// Retorna:
//   - []byte: contenido binario del PDF generado
//   - error: error si la generacion o escritura del PDF falla
func GenerarPDFReservacion(data ReservacionPDFData) ([]byte, error) {
	pdf := gofpdf.New("P", "mm", "A4", "")
	// Márgenes: top grande para header fijo, laterales ajustados
	pdf.SetMargins(13, 42, 13)
	pdf.SetAutoPageBreak(false, 0) // Control manual de espacio

	pdf.SetHeaderFunc(func() { header(pdf, data) })
	pdf.SetFooterFunc(func() { footer(pdf) })
	pdf.AddPage()

	// ── Banda de resumen debajo del header ──
	bandaResumen(pdf, data)

	y := pdf.GetY() + 4

	// ── Layout de dos columnas: info | detalles ──
	pw := 210.0
	mx := 13.0
	colW := (pw - mx*2 - 4) / 2 // dos columnas con gap de 4

	// Columna izquierda: datos generales
	yIzq := renderColIzq(pdf, data, mx, y, colW)

	// Columna derecha: boletos / habitaciones
	yDer := renderColDer(pdf, data, mx+colW+4, y, colW)

	yFin := yIzq
	if yDer > yFin {
		yFin = yDer
	}

	// ── Total ──
	yFin += 4
	renderTotal(pdf, data.Total, mx, yFin, pw-mx*2)

	// ── Condiciones (compactas) ──
	renderCondiciones(pdf, mx, yFin+13, pw-mx*2)

	var buf bytes.Buffer
	if err := pdf.Output(&buf); err != nil {
		return nil, fmt.Errorf("error generando PDF: %w", err)
	}
	return buf.Bytes(), nil
}

// ── Header ─────────────────────────────────────────────────────────────────

// header dibuja el encabezado fijo de cada pagina del PDF. Incluye el
// logotipo MOVENT, subtitulo, numero de reservacion y estado.
func header(pdf *gofpdf.Fpdf, data ReservacionPDFData) {
	pw := 210.0

	// Fondo oscuro
	fill(pdf, cOsc)
	pdf.Rect(0, 0, pw, 35, "F")

	// Franja amarilla inferior del header
	fill(pdf, cAma)
	pdf.Rect(0, 32.5, pw, 2.5, "F")

	// MOVENT
	pdf.SetFont("Helvetica", "B", 24)
	text(pdf, cAma)
	pdf.SetXY(13, 5)
	pdf.CellFormat(60, 11, "MOVENT", "", 0, "L", false, 0, "")

	// Subtítulo
	pdf.SetFont("Helvetica", "", 7)
	text(pdf, cGri)
	pdf.SetXY(13, 17)
	pdf.CellFormat(90, 5, e("Agencia de Viajes · Guatemala City · info@movent.gt"), "", 0, "L", false, 0, "")

	// Nro. reservación (derecha)
	pdf.SetFont("Helvetica", "", 6.5)
	text(pdf, cGri)
	pdf.SetXY(pw/2, 5)
	pdf.CellFormat(pw/2-13, 5, "COMPROBANTE DE RESERVACION", "", 0, "R", false, 0, "")

	pdf.SetFont("Helvetica", "B", 15)
	text(pdf, cBla)
	pdf.SetXY(pw/2, 11)
	pdf.CellFormat(pw/2-13, 8, data.NoReservacion, "", 0, "R", false, 0, "")

	// Estado
	er, eg, eb := estadoRGB(data.EstadoReserva)
	pdf.SetFont("Helvetica", "B", 7.5)
	pdf.SetTextColor(er, eg, eb)
	pdf.SetXY(pw/2, 21)
	pdf.CellFormat(pw/2-13, 6, "* "+e(data.EstadoReserva), "", 0, "R", false, 0, "")

	pdf.SetY(37)
}

// ── Banda de resumen rápido ────────────────────────────────────────────────

// bandaResumen dibuja la franja de resumen inmediatamente debajo del header,
// mostrando el tipo de reservacion, fecha, titular y correo del usuario.
func bandaResumen(pdf *gofpdf.Fpdf, data ReservacionPDFData) {
	pw := 210.0
	mx := 13.0
	y := pdf.GetY()
	h := 11.0

	fill(pdf, cCre)
	draw(pdf, cBor)
	pdf.Rect(mx, y, pw-mx*2, h, "FD")

	// Tipo
	pdf.SetFont("Helvetica", "B", 7.5)
	text(pdf, cOsc)
	pdf.SetXY(mx+3, y+2)
	pdf.CellFormat(40, 4, tipoLabel(data.TipoReserva), "", 0, "L", false, 0, "")

	// Fecha
	pdf.SetFont("Helvetica", "", 7.5)
	text(pdf, cTxt)
	pdf.SetXY(mx+3, y+6.5)
	pdf.CellFormat(60, 4, e("Reservado el ")+formatFecha(data.FechaCreacion), "", 0, "L", false, 0, "")

	// Titular
	titular := dash(data.UsuarioNombre)
	pdf.SetFont("Helvetica", "", 7.5)
	pdf.SetXY(mx+80, y+2)
	pdf.CellFormat(30, 4, "Titular:", "", 0, "L", false, 0, "")
	pdf.SetFont("Helvetica", "B", 7.5)
	text(pdf, cOsc)
	pdf.SetXY(mx+95, y+2)
	pdf.CellFormat(60, 4, titular, "", 0, "L", false, 0, "")

	// Email
	pdf.SetFont("Helvetica", "", 7.5)
	text(pdf, cTxt)
	pdf.SetXY(mx+80, y+6.5)
	pdf.CellFormat(30, 4, "Correo:", "", 0, "L", false, 0, "")
	pdf.SetFont("Helvetica", "", 7.5)
	pdf.SetXY(mx+95, y+6.5)
	pdf.CellFormat(70, 4, dash(data.UsuarioEmail), "", 0, "L", false, 0, "")

	pdf.SetY(y + h)
}

// ── Columna izquierda: datos generales ────────────────────────────────────

// renderColIzq dibuja la columna izquierda del PDF con los datos
// generales de la reservacion (codigo, estado, tipo y fecha).
// Retorna la coordenada Y final tras dibujar el contenido.
func renderColIzq(pdf *gofpdf.Fpdf, data ReservacionPDFData, x, y, w float64) float64 {
	y = secTitle(pdf, e("DATOS DE LA RESERVACION"), x, y, w)

	rows := [][2]string{
		{"Codigo", data.NoReservacion},
		{"Estado", e(data.EstadoReserva)},
		{"Tipo", tipoLabel(data.TipoReserva)},
		{"Fecha", formatFecha(data.FechaCreacion)},
	}
	y = kvTable(pdf, rows, x, y, w, 6.5)
	return y
}

// ── Columna derecha: boletos / habitaciones ───────────────────────────────

// renderColDer dibuja la columna derecha del PDF con las tarjetas de
// boletos de vuelo y habitaciones de hotel incluidos en la reservacion.
// Retorna la coordenada Y final tras dibujar todo el contenido.
func renderColDer(pdf *gofpdf.Fpdf, data ReservacionPDFData, x, y, w float64) float64 {
	if len(data.Boletos) > 0 {
		label := fmt.Sprintf(e("BOLETOS (%d)"), len(data.Boletos))
		y = secTitle(pdf, label, x, y, w)
		for i, b := range data.Boletos {
			y = renderBoleto(pdf, b, i+1, x, y, w)
		}
	}

	if len(data.Habitaciones) > 0 {
		if len(data.Boletos) > 0 {
			y += 3
		}
		label := fmt.Sprintf(e("HABITACIONES (%d)"), len(data.Habitaciones))
		y = secTitle(pdf, label, x, y, w)
		for i, h := range data.Habitaciones {
			y = renderHab(pdf, h, i+1, x, y, w)
		}
	}

	return y
}

// renderBoleto dibuja la tarjeta de un boleto de vuelo individual en el PDF,
// mostrando la ruta, horarios, clase, asiento, precio y pasajero.
// Retorna la coordenada Y final tras dibujar la tarjeta.
func renderBoleto(pdf *gofpdf.Fpdf, b BoletoPDF, num int, x, y, w float64) float64 {
	// Mini header boleto
	fill(pdf, cOsc)
	pdf.Rect(x, y, w, 6, "F")
	pdf.SetFont("Helvetica", "B", 7)
	text(pdf, cAma)
	pdf.SetXY(x+2, y+1)
	pdf.CellFormat(w-4, 4, fmt.Sprintf("Boleto %d", num), "", 0, "L", false, 0, "")
	y += 6

	// Ruta
	fill(pdf, cCre)
	draw(pdf, cBor)
	pdf.Rect(x, y, w, 10, "FD")

	pdf.SetFont("Helvetica", "B", 14)
	text(pdf, cOsc)
	pdf.SetXY(x+2, y+1)
	pdf.CellFormat(20, 7, b.OrigenCodigo, "", 0, "L", false, 0, "")

	pdf.SetFont("Helvetica", "B", 10)
	text(pdf, cAma)
	pdf.SetXY(x+22, y+2)
	pdf.CellFormat(10, 5, "->", "", 0, "C", false, 0, "")

	pdf.SetFont("Helvetica", "B", 14)
	text(pdf, cOsc)
	pdf.SetXY(x+33, y+1)
	pdf.CellFormat(20, 7, b.DestinoCodigo, "", 0, "L", false, 0, "")

	pdf.SetFont("Helvetica", "", 6)
	text(pdf, cGri)
	pdf.SetXY(x+2, y+7)
	pdf.CellFormat(30, 3, e(b.OrigenCiudad), "", 0, "L", false, 0, "")
	pdf.SetXY(x+33, y+7)
	pdf.CellFormat(30, 3, e(b.DestinoCiudad), "", 0, "L", false, 0, "")

	// Fecha y hora (derecha)
	pdf.SetFont("Helvetica", "B", 7)
	text(pdf, cOsc)
	pdf.SetXY(x+55, y+1)
	pdf.CellFormat(w-57, 4, fmt.Sprintf("%s -> %s", formatHora(b.HoraSalida), formatHora(b.HoraLlegada)), "", 0, "R", false, 0, "")
	pdf.SetFont("Helvetica", "", 6)
	text(pdf, cGri)
	pdf.SetXY(x+55, y+6)
	pdf.CellFormat(w-57, 3, formatFecha(b.FechaVuelo), "", 0, "R", false, 0, "")
	y += 10

	rows := [][2]string{
		{"Vuelo", b.NumeroVuelo},
		{"Clase", e(b.Clase)},
		{"Asiento", b.NoAsiento},
		{e("Precio"), fmt.Sprintf("$ %.2f", b.Precio)},
	}
	if b.PasajeroNombre != "" {
		rows = append(rows, [2]string{"Pasajero", e(b.PasajeroNombre)})
	}
	y = kvTable(pdf, rows, x, y, w, 6)
	return y + 2
}

// renderHab dibuja la tarjeta de una habitacion de hotel individual en el PDF,
// mostrando el nombre del hotel, tipo, cama, fechas y numero de huespedes.
// Retorna la coordenada Y final tras dibujar la tarjeta.
func renderHab(pdf *gofpdf.Fpdf, h HabitacionPDF, num int, x, y, w float64) float64 {
	// Mini header habitación
	fill(pdf, cOsc)
	pdf.Rect(x, y, w, 6, "F")
	pdf.SetFont("Helvetica", "B", 7)
	text(pdf, cAma)
	pdf.SetXY(x+2, y+1)
	pdf.CellFormat(w-4, 4, fmt.Sprintf("Hab. %d - %s", num, e(h.NombreHotel)), "", 0, "L", false, 0, "")
	y += 6

	// Subtotal omitido intencionalmente — info interna de la agencia
	rows := [][2]string{
		{"Tipo", e(h.TipoHabitacion)},
		{"Cama", e(h.TipoCama)},
		{"Nro. Hab.", e(h.NumeroHabitacion)},
		{"Check-in", formatFecha(h.FechaCheckIn)},
		{"Check-out", formatFecha(h.FechaCheckOut)},
		{"Noches", fmt.Sprintf("%d", calcNoches(h.FechaCheckIn, h.FechaCheckOut))},
		{e("Huespedes"), fmt.Sprintf("%d", h.CantidadPersonas)},
	}
	y = kvTable(pdf, rows, x, y, w, 6)
	return y + 2
}

// ── Total ──────────────────────────────────────────────────────────────────

// renderTotal dibuja la barra de total de la reservacion en el PDF,
// con fondo oscuro a la izquierda y fondo amarillo con el monto a la derecha.
func renderTotal(pdf *gofpdf.Fpdf, total float64, x, y, w float64) {
	h := 12.0

	fill(pdf, cOsc)
	pdf.Rect(x, y, w*0.7, h, "F")

	fill(pdf, cAma)
	pdf.Rect(x+w*0.7, y, w*0.3, h, "F")

	pdf.SetFont("Helvetica", "B", 9.5)
	text(pdf, cBla)
	pdf.SetXY(x+3, y+4)
	pdf.CellFormat(w*0.7-6, 5, e("TOTAL RESERVACION"), "", 0, "L", false, 0, "")

	pdf.SetFont("Helvetica", "B", 11)
	text(pdf, cOsc)
	pdf.SetXY(x+w*0.7, y+3.5)
	pdf.CellFormat(w*0.3-3, 5, fmt.Sprintf("$ %.2f", total), "", 0, "R", false, 0, "")
}

// ── Condiciones ────────────────────────────────────────────────────────────

// renderCondiciones dibuja el bloque de terminos y condiciones al pie
// del PDF con las politicas de la agencia en formato compacto.
func renderCondiciones(pdf *gofpdf.Fpdf, x, y, w float64) {
	// Título mínimo
	fill(pdf, cCre)
	draw(pdf, cBor)
	pdf.Rect(x, y, w, 5, "FD")
	pdf.SetFont("Helvetica", "B", 6.5)
	text(pdf, cGri)
	pdf.SetXY(x+2, y+1)
	pdf.CellFormat(w-4, 3, e("TERMINOS Y CONDICIONES"), "", 0, "L", false, 0, "")
	y += 5

	pdf.SetFont("Helvetica", "", 6.5)
	text(pdf, cGri)
	lineas := []string{
		e("1. Esta reservacion es valida unicamente para las fechas indicadas."),
		e("2. Las cancelaciones estan sujetas a las politicas de cada proveedor."),
		e("3. MOVENT actua como intermediario; el comprobante oficial lo emite el proveedor."),
		e("4. Soporte: info@movent.gt · +502 0000-0000"),
	}
	for _, l := range lineas {
		pdf.SetXY(x+2, y)
		pdf.CellFormat(w-4, 4, l, "", 1, "L", false, 0, "")
		y += 4
	}
}

// ── Helpers de layout ──────────────────────────────────────────────────────

// secTitle dibuja el encabezado de una seccion con fondo oscuro y franja
// amarilla izquierda. Retorna la coordenada Y siguiente para continuar
// dibujando debajo del titulo.
func secTitle(pdf *gofpdf.Fpdf, titulo string, x, y, w float64) float64 {
	h := 7.5

	fill(pdf, cOsc)
	pdf.Rect(x, y, w, h, "F")

	// Franja amarilla izquierda
	fill(pdf, cAma)
	pdf.Rect(x, y, 2.5, h, "F")

	pdf.SetFont("Helvetica", "B", 7.5)
	text(pdf, cBla)
	pdf.SetXY(x+5, y+2)
	pdf.CellFormat(w-7, h-4, titulo, "", 0, "L", false, 0, "")

	return y + h
}

// kvTable dibuja una tabla de pares clave/valor con fondo crema para
// etiquetas y fondo blanco/alterno para valores. Retorna la Y final.
func kvTable(pdf *gofpdf.Fpdf, rows [][2]string, x, y, w, rowH float64) float64 {
	lblW := w * 0.40
	valW := w - lblW

	for i, row := range rows {
		// Fondo etiqueta: crema
		fill(pdf, cCre)
		draw(pdf, cBor)
		pdf.Rect(x, y, lblW, rowH, "FD")

		// Fondo valor: blanco / alterno
		if i%2 == 0 {
			fill(pdf, cBla)
		} else {
			fill(pdf, cAlt)
		}
		pdf.Rect(x+lblW, y, valW, rowH, "FD")

		// Texto etiqueta
		pdf.SetFont("Helvetica", "B", 7)
		text(pdf, cTxt)
		pdf.SetXY(x+2, y+rowH/2-1.5)
		pdf.CellFormat(lblW-4, 3, row[0], "", 0, "L", false, 0, "")

		// Texto valor
		pdf.SetFont("Helvetica", "", 7)
		text(pdf, cOsc)
		pdf.SetXY(x+lblW+2, y+rowH/2-1.5)
		pdf.CellFormat(valW-4, 3, row[1], "", 0, "L", false, 0, "")

		y += rowH
	}
	return y
}

// footer dibuja el pie de pagina MOVENT con fondo oscuro y franja amarilla.
// Se invoca automaticamente al final de cada pagina del PDF.
func footer(pdf *gofpdf.Fpdf) {
	pw := 210.0
	fill(pdf, cOsc)
	pdf.Rect(0, 290, pw, 7, "F")
	fill(pdf, cAma)
	pdf.Rect(0, 289.5, pw, 0.8, "F")
	pdf.SetFont("Helvetica", "", 6.5)
	text(pdf, cGri)
	pdf.SetXY(0, 291.5)
	pdf.CellFormat(pw, 4, e("MOVENT · info@movent.gt · +502 0000-0000 · Guatemala City, Guatemala"), "", 0, "C", false, 0, "")
}

// ── Utilidades ─────────────────────────────────────────────────────────────

// estadoRGB retorna los valores RGB correspondientes al estado de la
// reservacion para colorear el texto: verde (confirmada/completada),
// rojo (cancelada) o naranja (cualquier otro estado).
func estadoRGB(estado string) (int, int, int) {
	switch strings.ToLower(estado) {
	case "confirmada", "completada":
		return cVer[0], cVer[1], cVer[2]
	case "cancelada":
		return cRoj[0], cRoj[1], cRoj[2]
	default:
		return cNar[0], cNar[1], cNar[2]
	}
}

// tipoLabel, formatFecha, formatHora, calcNoches están definidas en EmailHelper.go
