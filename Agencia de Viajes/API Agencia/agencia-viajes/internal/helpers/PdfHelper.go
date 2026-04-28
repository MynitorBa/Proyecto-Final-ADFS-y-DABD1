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
	Subtotal      float64
	MontoImpuestos float64
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

var (
	cOsc = [3]int{28, 26, 24}    // #1C1A18
	cAma = [3]int{255, 204, 0}   // #FFCC00
	cCre = [3]int{245, 242, 236} // #F5F2EC
	cBla = [3]int{255, 255, 255} // blanco
	cGri = [3]int{154, 144, 137} // #9A9089 gris suave
	cBor = [3]int{220, 212, 200} // borde
	cAlt = [3]int{250, 247, 242} // fila alt
	cTxt = [3]int{74, 64, 53}    // texto
	cVer = [3]int{22, 100, 52}   // verde estado OK
	cRoj = [3]int{180, 30, 30}   // rojo estado cancelado
	cNar = [3]int{140, 90, 20}   // naranja estado pendiente
)

func fill(pdf *gofpdf.Fpdf, c [3]int) { pdf.SetFillColor(c[0], c[1], c[2]) }
func text(pdf *gofpdf.Fpdf, c [3]int) { pdf.SetTextColor(c[0], c[1], c[2]) }
func draw(pdf *gofpdf.Fpdf, c [3]int) { pdf.SetDrawColor(c[0], c[1], c[2]) }

// ── Encoding UTF-8 → Latin-1 ─────────────────────────────────────────────

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

func dash(s string) string {
	if strings.TrimSpace(s) == "" {
		return "-"
	}
	return e(s)
}

// ── Constantes de layout ──────────────────────────────────────────────────

const (
	pageW      = 210.0 // ancho A4
	pageH      = 297.0 // alto A4
	marginX    = 13.0  // margen horizontal
	marginTop  = 42.0  // margen superior (espacio para header)
	footerY    = 288.0 // Y donde empieza el area del footer
	safeBottom = 280.0 // limite seguro de contenido antes del footer
)

// contentW retorna el ancho util del contenido (sin margenes laterales).
func contentW() float64 { return pageW - marginX*2 }

// ── Salto de pagina inteligente ───────────────────────────────────────────

// needSpace verifica si hay espacio suficiente para `needed` mm en la pagina actual.
// Si no hay espacio, agrega una nueva pagina y retorna el Y inicial de contenido.
func needSpace(pdf *gofpdf.Fpdf, y, needed float64) float64 {
	if y+needed > safeBottom {
		pdf.AddPage()
		return pdf.GetY()
	}
	return y
}

// boletoH calcula la altura en mm que ocupa la tarjeta de un boleto.
func boletoH(b BoletoPDF) float64 {
	rows := 4.0 // Vuelo, Clase, Asiento, Precio
	if b.PasajeroNombre != "" {
		rows++
	}
	return 6 + 10 + rows*6 + 3 // header + ruta + filas + gap
}

// habH calcula la altura en mm que ocupa la tarjeta de una habitacion.
func habH() float64 {
	return 6 + 7*6 + 3 // header + 7 filas + gap
}

// ── Entrypoint ─────────────────────────────────────────────────────────────

// GenerarPDFReservacion genera el PDF de comprobante de reservacion con
// layout lineal y saltos de pagina automaticos cuando el contenido desborda.
func GenerarPDFReservacion(data ReservacionPDFData) ([]byte, error) {
	pdf := gofpdf.New("P", "mm", "A4", "")
	pdf.SetMargins(marginX, marginTop, marginX)
	pdf.SetAutoPageBreak(false, 0) // control manual de saltos

	pdf.SetHeaderFunc(func() { header(pdf, data) })
	pdf.SetFooterFunc(func() { footer(pdf) })
	pdf.AddPage()

	w := contentW()

	// ── Banda de resumen ──
	bandaResumen(pdf, data)
	y := pdf.GetY() + 4

	// ── Datos generales (layout horizontal compacto, ancho completo) ──
	y = renderDatosGenerales(pdf, data, marginX, y, w)
	y += 6

	// ── Boletos ──
	if len(data.Boletos) > 0 {
		label := fmt.Sprintf(e("BOLETOS (%d)"), len(data.Boletos))
		y = needSpace(pdf, y, 20) // espacio minimo para titulo + primera tarjeta
		y = secTitle(pdf, label, marginX, y, w)
		for i, b := range data.Boletos {
			y = needSpace(pdf, y, boletoH(b))
			y = renderBoleto(pdf, b, i+1, marginX, y, w)
		}
	}

	// ── Habitaciones ──
	if len(data.Habitaciones) > 0 {
		if len(data.Boletos) > 0 {
			y += 3
		}
		label := fmt.Sprintf(e("HABITACIONES (%d)"), len(data.Habitaciones))
		y = needSpace(pdf, y, 20)
		y = secTitle(pdf, label, marginX, y, w)
		for i, h := range data.Habitaciones {
			y = needSpace(pdf, y, habH())
			y = renderHab(pdf, h, i+1, marginX, y, w)
		}
	}

	// ── Total + Condiciones ──
	y = needSpace(pdf, y, 45) // total 12 + condiciones ~30
	y += 4
	renderTotal(pdf, data.Total, marginX, y, w)
	renderCondiciones(pdf, marginX, y+13, w)

	var buf bytes.Buffer
	if err := pdf.Output(&buf); err != nil {
		return nil, fmt.Errorf("error generando PDF: %w", err)
	}
	return buf.Bytes(), nil
}

// ── Header ─────────────────────────────────────────────────────────────────

func header(pdf *gofpdf.Fpdf, data ReservacionPDFData) {
	// Fondo oscuro
	fill(pdf, cOsc)
	pdf.Rect(0, 0, pageW, 35, "F")

	// Franja amarilla inferior
	fill(pdf, cAma)
	pdf.Rect(0, 32.5, pageW, 2.5, "F")

	// MOVENT
	pdf.SetFont("Helvetica", "B", 24)
	text(pdf, cAma)
	pdf.SetXY(13, 5)
	pdf.CellFormat(60, 11, "MOVENT", "", 0, "L", false, 0, "")

	// Subtitulo
	pdf.SetFont("Helvetica", "", 7)
	text(pdf, cGri)
	pdf.SetXY(13, 17)
	pdf.CellFormat(90, 5, e("Agencia de Viajes · Guatemala City · info@movent.gt"), "", 0, "L", false, 0, "")

	// Nro. reservacion
	pdf.SetFont("Helvetica", "", 6.5)
	text(pdf, cGri)
	pdf.SetXY(pageW/2, 5)
	pdf.CellFormat(pageW/2-13, 5, "COMPROBANTE DE RESERVACION", "", 0, "R", false, 0, "")

	pdf.SetFont("Helvetica", "B", 15)
	text(pdf, cBla)
	pdf.SetXY(pageW/2, 11)
	pdf.CellFormat(pageW/2-13, 8, data.NoReservacion, "", 0, "R", false, 0, "")

	// Estado
	er, eg, eb := estadoRGB(data.EstadoReserva)
	pdf.SetFont("Helvetica", "B", 7.5)
	pdf.SetTextColor(er, eg, eb)
	pdf.SetXY(pageW/2, 21)
	pdf.CellFormat(pageW/2-13, 6, "* "+e(data.EstadoReserva), "", 0, "R", false, 0, "")

	pdf.SetY(37)
}

// ── Banda de resumen ──────────────────────────────────────────────────────

func bandaResumen(pdf *gofpdf.Fpdf, data ReservacionPDFData) {
	y := pdf.GetY()
	h := 11.0
	w := contentW()

	fill(pdf, cCre)
	draw(pdf, cBor)
	pdf.Rect(marginX, y, w, h, "FD")

	pdf.SetFont("Helvetica", "B", 7.5)
	text(pdf, cOsc)
	pdf.SetXY(marginX+3, y+2)
	pdf.CellFormat(40, 4, tipoLabel(data.TipoReserva), "", 0, "L", false, 0, "")

	pdf.SetFont("Helvetica", "", 7.5)
	text(pdf, cTxt)
	pdf.SetXY(marginX+3, y+6.5)
	pdf.CellFormat(60, 4, e("Reservado el ")+formatFecha(data.FechaCreacion), "", 0, "L", false, 0, "")

	titular := dash(data.UsuarioNombre)
	pdf.SetFont("Helvetica", "", 7.5)
	pdf.SetXY(marginX+80, y+2)
	pdf.CellFormat(30, 4, "Titular:", "", 0, "L", false, 0, "")
	pdf.SetFont("Helvetica", "B", 7.5)
	text(pdf, cOsc)
	pdf.SetXY(marginX+95, y+2)
	pdf.CellFormat(60, 4, titular, "", 0, "L", false, 0, "")

	pdf.SetFont("Helvetica", "", 7.5)
	text(pdf, cTxt)
	pdf.SetXY(marginX+80, y+6.5)
	pdf.CellFormat(30, 4, "Correo:", "", 0, "L", false, 0, "")
	pdf.SetFont("Helvetica", "", 7.5)
	pdf.SetXY(marginX+95, y+6.5)
	pdf.CellFormat(70, 4, dash(data.UsuarioEmail), "", 0, "L", false, 0, "")

	pdf.SetY(y + h)
}

// ── Datos generales (horizontal, ancho completo) ──────────────────────────

// renderDatosGenerales dibuja los 4 campos de la reservacion (Codigo, Estado,
// Tipo, Fecha) en una fila horizontal de 4 celdas de igual ancho.
func renderDatosGenerales(pdf *gofpdf.Fpdf, data ReservacionPDFData, x, y, w float64) float64 {
	y = secTitle(pdf, e("DATOS DE LA RESERVACION"), x, y, w)

	campos := [][2]string{
		{"Codigo", data.NoReservacion},
		{"Estado", e(data.EstadoReserva)},
		{"Tipo", tipoLabel(data.TipoReserva)},
		{"Fecha", formatFecha(data.FechaCreacion)},
	}

	colW := w / float64(len(campos))
	lblH := 5.5
	valH := 6.5

	for i, campo := range campos {
		cx := x + float64(i)*colW

		// Etiqueta
		fill(pdf, cCre)
		draw(pdf, cBor)
		pdf.Rect(cx, y, colW, lblH, "FD")
		pdf.SetFont("Helvetica", "B", 7)
		text(pdf, cTxt)
		pdf.SetXY(cx+2, y+1.5)
		pdf.CellFormat(colW-4, lblH-2, campo[0], "", 0, "L", false, 0, "")

		// Valor
		if i%2 == 0 {
			fill(pdf, cBla)
		} else {
			fill(pdf, cAlt)
		}
		pdf.Rect(cx, y+lblH, colW, valH, "FD")
		pdf.SetFont("Helvetica", "", 8)
		text(pdf, cOsc)
		pdf.SetXY(cx+2, y+lblH+1.5)
		pdf.CellFormat(colW-4, valH-2, campo[1], "", 0, "L", false, 0, "")
	}

	return y + lblH + valH
}

// ── Boleto ────────────────────────────────────────────────────────────────

func renderBoleto(pdf *gofpdf.Fpdf, b BoletoPDF, num int, x, y, w float64) float64 {
	// Mini header
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

	pdf.SetFont("Helvetica", "B", 7)
	text(pdf, cOsc)
	pdf.SetXY(x+65, y+1)
	pdf.CellFormat(w-67, 4, fmt.Sprintf("%s -> %s", formatHora(b.HoraSalida), formatHora(b.HoraLlegada)), "", 0, "R", false, 0, "")
	pdf.SetFont("Helvetica", "", 6)
	text(pdf, cGri)
	pdf.SetXY(x+65, y+6)
	pdf.CellFormat(w-67, 3, formatFecha(b.FechaVuelo), "", 0, "R", false, 0, "")
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
	return y + 3
}

// ── Habitacion ────────────────────────────────────────────────────────────

func renderHab(pdf *gofpdf.Fpdf, h HabitacionPDF, num int, x, y, w float64) float64 {
	fill(pdf, cOsc)
	pdf.Rect(x, y, w, 6, "F")
	pdf.SetFont("Helvetica", "B", 7)
	text(pdf, cAma)
	pdf.SetXY(x+2, y+1)
	pdf.CellFormat(w-4, 4, fmt.Sprintf("Hab. %d - %s", num, e(h.NombreHotel)), "", 0, "L", false, 0, "")
	y += 6

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
	return y + 3
}

// ── Total ──────────────────────────────────────────────────────────────────

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

func renderCondiciones(pdf *gofpdf.Fpdf, x, y, w float64) {
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

func secTitle(pdf *gofpdf.Fpdf, titulo string, x, y, w float64) float64 {
	h := 7.5

	fill(pdf, cOsc)
	pdf.Rect(x, y, w, h, "F")

	fill(pdf, cAma)
	pdf.Rect(x, y, 2.5, h, "F")

	pdf.SetFont("Helvetica", "B", 7.5)
	text(pdf, cBla)
	pdf.SetXY(x+5, y+2)
	pdf.CellFormat(w-7, h-4, titulo, "", 0, "L", false, 0, "")

	return y + h
}

func kvTable(pdf *gofpdf.Fpdf, rows [][2]string, x, y, w, rowH float64) float64 {
	lblW := w * 0.28
	valW := w - lblW

	for i, row := range rows {
		fill(pdf, cCre)
		draw(pdf, cBor)
		pdf.Rect(x, y, lblW, rowH, "FD")

		if i%2 == 0 {
			fill(pdf, cBla)
		} else {
			fill(pdf, cAlt)
		}
		pdf.Rect(x+lblW, y, valW, rowH, "FD")

		pdf.SetFont("Helvetica", "B", 7)
		text(pdf, cTxt)
		pdf.SetXY(x+2, y+rowH/2-1.5)
		pdf.CellFormat(lblW-4, 3, row[0], "", 0, "L", false, 0, "")

		pdf.SetFont("Helvetica", "", 7)
		text(pdf, cOsc)
		pdf.SetXY(x+lblW+2, y+rowH/2-1.5)
		pdf.CellFormat(valW-4, 3, row[1], "", 0, "L", false, 0, "")

		y += rowH
	}
	return y
}

// footer dibuja el pie de pagina en todas las paginas del PDF.
func footer(pdf *gofpdf.Fpdf) {
	fill(pdf, cOsc)
	pdf.Rect(0, 290, pageW, 7, "F")
	fill(pdf, cAma)
	pdf.Rect(0, 289.5, pageW, 0.8, "F")
	pdf.SetFont("Helvetica", "", 6.5)
	text(pdf, cGri)
	pdf.SetXY(0, 291.5)
	pdf.CellFormat(pageW, 4, e("MOVENT · info@movent.gt · +502 0000-0000 · Guatemala City, Guatemala"), "", 0, "C", false, 0, "")
}

// ── Utilidades ─────────────────────────────────────────────────────────────

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

// tipoLabel, formatFecha, formatHora, calcNoches estan definidas en EmailHelper.go
