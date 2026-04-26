// # Package helpers
//
// MetricasExportHelper provee el struct MetricasData y las funciones para
// generar reportes de métricas en Excel (.xlsx), ZIP de CSVs y PDF.
// Es usado por MetricasController para exportaciones y correos.
package helpers

import (
	"archive/zip"
	"bytes"
	"crypto/tls"
	"encoding/base64"
	"encoding/csv"
	"fmt"
	"mime/multipart"
	"net"
	"net/smtp"
	"net/textproto"
	"strconv"
	"strings"
	"time"

	"github.com/jung-kurt/gofpdf"
)

// MetricasData agrupa todos los datos necesarios para generar cualquiera
// de los formatos de exportación de métricas de MOVENT.
type MetricasData struct {
	Desde string
	Hasta string

	KPITotalBusquedas     int
	KPIBusquedasVuelo     int
	KPIBusquedasHotel     int
	KPITotalReservaciones int
	KPIReservasPagadas    int
	KPIIngresos           float64
	KPIGanancia           float64
	KPITicketPromedio     float64
	KPIUsuariosActivos    int

	// Filas para tablas: cada []string es una fila de datos
	BusquedasDia  [][]string // fecha, total
	BusquedasTipo [][]string // tipo, total
	Destinos      [][]string // ciudad, pais, total
	ResTipos      [][]string // tipo, total, ingresos
	Embudo        [][]string // etapa, total
	Proveedores   [][]string // nombre, tipo, reservaciones, ingresos, ganancia
	Cancelaciones [][]string // tipo, total
	Tendencia     [][]string // mes, tipo, ingresos, cantidad
	Heatmap       [][]string // dia, hora, total
	Listado       [][]string // id, fecha, tipo, usuario, origen, destino
}

// ── XLSX sin dependencias externas ───────────────────────────────────────────

type xlsxSheetDef struct {
	Name    string
	Headers []string
	Rows    [][]string
}

// GenerarMetricasXLSX construye un archivo XLSX (OpenXML) con múltiples hojas.
// Utiliza únicamente archive/zip y generación de XML en string — sin librerías externas.
func GenerarMetricasXLSX(data MetricasData) []byte {
	sheets := metricasSheets(data)

	var buf bytes.Buffer
	w := zip.NewWriter(&buf)

	xlsxAdd(w, "[Content_Types].xml", xlsxContentTypes(len(sheets)))
	xlsxAdd(w, "_rels/.rels", xlsxPackageRels())
	xlsxAdd(w, "xl/workbook.xml", xlsxWorkbook(sheets))
	xlsxAdd(w, "xl/_rels/workbook.xml.rels", xlsxWorkbookRels(len(sheets)))
	xlsxAdd(w, "xl/styles.xml", xlsxStyles())
	for i, sh := range sheets {
		xlsxAdd(w, fmt.Sprintf("xl/worksheets/sheet%d.xml", i+1), xlsxSheetXML(sh))
	}

	w.Close()
	return buf.Bytes()
}

func xlsxAdd(w *zip.Writer, name, content string) {
	f, _ := w.Create(name)
	f.Write([]byte(content))
}

func xlsxContentTypes(n int) string {
	var sb strings.Builder
	sb.WriteString(`<?xml version="1.0" encoding="UTF-8" standalone="yes"?>`)
	sb.WriteString(`<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">`)
	sb.WriteString(`<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>`)
	sb.WriteString(`<Default Extension="xml" ContentType="application/xml"/>`)
	sb.WriteString(`<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>`)
	sb.WriteString(`<Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>`)
	for i := 1; i <= n; i++ {
		sb.WriteString(fmt.Sprintf(`<Override PartName="/xl/worksheets/sheet%d.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>`, i))
	}
	sb.WriteString(`</Types>`)
	return sb.String()
}

func xlsxPackageRels() string {
	return `<?xml version="1.0" encoding="UTF-8" standalone="yes"?>` +
		`<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">` +
		`<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>` +
		`</Relationships>`
}

func xlsxWorkbook(sheets []xlsxSheetDef) string {
	var sb strings.Builder
	sb.WriteString(`<?xml version="1.0" encoding="UTF-8" standalone="yes"?>`)
	sb.WriteString(`<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">`)
	sb.WriteString(`<sheets>`)
	for i, sh := range sheets {
		sb.WriteString(fmt.Sprintf(`<sheet name="%s" sheetId="%d" r:id="rId%d"/>`, xlEscape(sh.Name), i+1, i+1))
	}
	sb.WriteString(`</sheets></workbook>`)
	return sb.String()
}

func xlsxWorkbookRels(n int) string {
	var sb strings.Builder
	sb.WriteString(`<?xml version="1.0" encoding="UTF-8" standalone="yes"?>`)
	sb.WriteString(`<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">`)
	for i := 1; i <= n; i++ {
		sb.WriteString(fmt.Sprintf(`<Relationship Id="rId%d" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet%d.xml"/>`, i, i))
	}
	sb.WriteString(fmt.Sprintf(`<Relationship Id="rId%d" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>`, n+1))
	sb.WriteString(`</Relationships>`)
	return sb.String()
}

func xlsxStyles() string {
	return `<?xml version="1.0" encoding="UTF-8" standalone="yes"?>` +
		`<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">` +
		`<fonts count="2">` +
		`<font><sz val="11"/><name val="Calibri"/></font>` +
		`<font><b/><sz val="11"/><name val="Calibri"/><color rgb="FF1C1A18"/></font>` +
		`</fonts>` +
		`<fills count="3">` +
		`<fill><patternFill patternType="none"/></fill>` +
		`<fill><patternFill patternType="gray125"/></fill>` +
		`<fill><patternFill patternType="solid"><fgColor rgb="FFD4AF37"/></patternFill></fill>` +
		`</fills>` +
		`<borders count="1"><border><left/><right/><top/><bottom/><diagonal/></border></borders>` +
		`<cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>` +
		`<cellXfs count="2">` +
		`<xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>` +
		`<xf numFmtId="0" fontId="1" fillId="2" borderId="0" xfId="0" applyFont="1" applyFill="1"/>` +
		`</cellXfs>` +
		`</styleSheet>`
}

func xlsxSheetXML(sh xlsxSheetDef) string {
	var sb strings.Builder
	sb.WriteString(`<?xml version="1.0" encoding="UTF-8" standalone="yes"?>`)
	sb.WriteString(`<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>`)

	if len(sh.Headers) > 0 {
		sb.WriteString(`<row r="1">`)
		for col, h := range sh.Headers {
			ref := xlCellRef(1, col+1)
			sb.WriteString(fmt.Sprintf(`<c r="%s" t="inlineStr" s="1"><is><t>%s</t></is></c>`, ref, xlEscape(h)))
		}
		sb.WriteString(`</row>`)
	}

	for ri, row := range sh.Rows {
		rowNum := ri + 2
		sb.WriteString(fmt.Sprintf(`<row r="%d">`, rowNum))
		for ci, val := range row {
			ref := xlCellRef(rowNum, ci+1)
			if _, err := strconv.ParseFloat(strings.TrimSpace(val), 64); err == nil && val != "" {
				sb.WriteString(fmt.Sprintf(`<c r="%s"><v>%s</v></c>`, ref, strings.TrimSpace(val)))
			} else {
				sb.WriteString(fmt.Sprintf(`<c r="%s" t="inlineStr"><is><t>%s</t></is></c>`, ref, xlEscape(val)))
			}
		}
		sb.WriteString(`</row>`)
	}

	sb.WriteString(`</sheetData></worksheet>`)
	return sb.String()
}

func xlCellRef(row, col int) string {
	return xlColLetter(col) + strconv.Itoa(row)
}

func xlColLetter(n int) string {
	result := ""
	for n > 0 {
		n--
		result = string(rune('A'+n%26)) + result
		n /= 26
	}
	return result
}

func xlEscape(s string) string {
	s = strings.ReplaceAll(s, "&", "&amp;")
	s = strings.ReplaceAll(s, "<", "&lt;")
	s = strings.ReplaceAll(s, ">", "&gt;")
	s = strings.ReplaceAll(s, `"`, "&quot;")
	return s
}

func metricasSheets(data MetricasData) []xlsxSheetDef {
	all := []xlsxSheetDef{
		{
			Name:    "KPI Resumen",
			Headers: []string{"Metrica", "Valor"},
			Rows: [][]string{
				{"Total Busquedas", strconv.Itoa(data.KPITotalBusquedas)},
				{"Busquedas Vuelo", strconv.Itoa(data.KPIBusquedasVuelo)},
				{"Busquedas Hotel", strconv.Itoa(data.KPIBusquedasHotel)},
				{"Total Reservaciones", strconv.Itoa(data.KPITotalReservaciones)},
				{"Reservaciones Pagadas", strconv.Itoa(data.KPIReservasPagadas)},
				{"Ingresos Totales USD", fmt.Sprintf("%.2f", data.KPIIngresos)},
				{"Ganancia MOVENT USD", fmt.Sprintf("%.2f", data.KPIGanancia)},
				{"Ticket Promedio USD", fmt.Sprintf("%.2f", data.KPITicketPromedio)},
				{"Usuarios Activos", strconv.Itoa(data.KPIUsuariosActivos)},
				{"Periodo Desde", data.Desde},
				{"Periodo Hasta", data.Hasta},
			},
		},
		{Name: "Busquedas Dia", Headers: []string{"Fecha", "Total"}, Rows: data.BusquedasDia},
		{Name: "Busquedas Tipo", Headers: []string{"Tipo", "Total"}, Rows: data.BusquedasTipo},
		{Name: "Destinos", Headers: []string{"Ciudad", "Pais", "Total"}, Rows: data.Destinos},
		{Name: "Reservaciones Tipo", Headers: []string{"Tipo", "Total", "Ingresos"}, Rows: data.ResTipos},
		{Name: "Embudo", Headers: []string{"Etapa", "Total"}, Rows: data.Embudo},
		{Name: "Proveedores", Headers: []string{"Proveedor", "Tipo", "Reservaciones", "Ingresos", "Ganancia"}, Rows: data.Proveedores},
		{Name: "Cancelaciones", Headers: []string{"Tipo Reserva", "Total"}, Rows: data.Cancelaciones},
		{Name: "Tendencia Mensual", Headers: []string{"Mes", "Tipo", "Ingresos", "Cantidad"}, Rows: data.Tendencia},
		{Name: "Heatmap", Headers: []string{"Dia Semana", "Hora", "Total"}, Rows: data.Heatmap},
		{Name: "Registro Busquedas", Headers: []string{"ID", "Fecha", "Tipo", "Usuario", "Origen", "Destino"}, Rows: data.Listado},
	}
	var result []xlsxSheetDef
	for _, s := range all {
		if len(s.Rows) > 0 || s.Name == "KPI Resumen" {
			result = append(result, s)
		}
	}
	return result
}

// ── CSV + ZIP ─────────────────────────────────────────────────────────────────

// GenerarMetricasCSVZip genera un ZIP con múltiples archivos CSV.
func GenerarMetricasCSVZip(data MetricasData) []byte {
	var buf bytes.Buffer
	w := zip.NewWriter(&buf)

	type csvDef struct {
		name    string
		headers []string
		rows    [][]string
	}

	archivos := []csvDef{
		{"kpi_resumen.csv", []string{"Metrica", "Valor"}, [][]string{
			{"Total Busquedas", strconv.Itoa(data.KPITotalBusquedas)},
			{"Busquedas Vuelo", strconv.Itoa(data.KPIBusquedasVuelo)},
			{"Busquedas Hotel", strconv.Itoa(data.KPIBusquedasHotel)},
			{"Total Reservaciones", strconv.Itoa(data.KPITotalReservaciones)},
			{"Reservaciones Pagadas", strconv.Itoa(data.KPIReservasPagadas)},
			{"Ingresos Totales", fmt.Sprintf("%.2f", data.KPIIngresos)},
			{"Ganancia MOVENT", fmt.Sprintf("%.2f", data.KPIGanancia)},
			{"Ticket Promedio", fmt.Sprintf("%.2f", data.KPITicketPromedio)},
			{"Usuarios Activos", strconv.Itoa(data.KPIUsuariosActivos)},
		}},
		{"busquedas_por_dia.csv", []string{"Fecha", "Total"}, data.BusquedasDia},
		{"busquedas_por_tipo.csv", []string{"Tipo", "Total"}, data.BusquedasTipo},
		{"destinos_populares.csv", []string{"Ciudad", "Pais", "Total"}, data.Destinos},
		{"reservaciones_por_tipo.csv", []string{"Tipo", "Total", "Ingresos"}, data.ResTipos},
		{"embudo_conversion.csv", []string{"Etapa", "Total"}, data.Embudo},
		{"proveedores.csv", []string{"Proveedor", "Tipo", "Reservaciones", "Ingresos", "Ganancia"}, data.Proveedores},
		{"cancelaciones.csv", []string{"Tipo", "Total"}, data.Cancelaciones},
		{"tendencia_mensual.csv", []string{"Mes", "Tipo", "Ingresos", "Cantidad"}, data.Tendencia},
		{"heatmap_busquedas.csv", []string{"Dia", "Hora", "Total"}, data.Heatmap},
		{"registro_busquedas.csv", []string{"ID", "Fecha", "Tipo", "Usuario", "Origen", "Destino"}, data.Listado},
	}

	for _, cf := range archivos {
		f, _ := w.Create(cf.name)
		cw := csv.NewWriter(f)
		cw.Write(cf.headers)
		for _, row := range cf.rows {
			cw.Write(row)
		}
		cw.Flush()
	}

	w.Close()
	return buf.Bytes()
}

// ── PDF con gofpdf ───────────────────────────────────────────────────────────

var (
	pdfColorOscuro = [3]int{28, 26, 24}
	pdfColorDorado = [3]int{212, 175, 55}
	pdfColorGris   = [3]int{107, 115, 128}
	pdfColorRojo   = [3]int{220, 38, 38}
)

// GenerarMetricasPDF genera un PDF de métricas completo usando gofpdf.
func GenerarMetricasPDF(data MetricasData) ([]byte, error) {
	pdf := gofpdf.New("P", "mm", "A4", "")
	pdf.SetMargins(15, 15, 15)
	pdf.SetAutoPageBreak(true, 18)

	pdf.SetFooterFunc(func() {
		pdf.SetY(-12)
		pdf.SetTextColor(pdfColorGris[0], pdfColorGris[1], pdfColorGris[2])
		pdf.SetFont("Helvetica", "", 8)
		pdf.CellFormat(0, 10,
			fmt.Sprintf("MOVENT — Reporte de Metricas — Pagina %d", pdf.PageNo()),
			"", 0, "C", false, 0, "")
	})

	pdf.AddPage()

	// ── Encabezado ───────────────────────────────────────────────────────
	pdf.SetFillColor(pdfColorOscuro[0], pdfColorOscuro[1], pdfColorOscuro[2])
	pdf.Rect(0, 0, 210, 38, "F")

	pdf.SetTextColor(pdfColorDorado[0], pdfColorDorado[1], pdfColorDorado[2])
	pdf.SetFont("Helvetica", "B", 22)
	pdf.SetXY(15, 8)
	pdf.Cell(100, 12, "MOVENT")

	pdf.SetTextColor(255, 255, 255)
	pdf.SetFont("Helvetica", "", 10)
	pdf.SetXY(15, 21)
	pdf.Cell(180, 6, "Reporte de Metricas — Panel Administrativo")

	pdf.SetTextColor(pdfColorGris[0], pdfColorGris[1], pdfColorGris[2])
	pdf.SetFont("Helvetica", "", 8)
	pdf.SetXY(15, 29)
	pdf.Cell(180, 6, fmt.Sprintf("Periodo: %s al %s    Generado: %s",
		data.Desde, data.Hasta, time.Now().Format("02/01/2006 15:04")))

	pdf.SetFillColor(pdfColorDorado[0], pdfColorDorado[1], pdfColorDorado[2])
	pdf.Rect(0, 38, 210, 1.5, "F")
	pdf.SetY(44)

	// ── KPIs ─────────────────────────────────────────────────────────────
	pdfSeccion(pdf, "KPI RESUMEN")
	pdfKpiPar(pdf, "Total Busquedas", strconv.Itoa(data.KPITotalBusquedas),
		"Busquedas Vuelo", strconv.Itoa(data.KPIBusquedasVuelo))
	pdfKpiPar(pdf, "Busquedas Hotel", strconv.Itoa(data.KPIBusquedasHotel),
		"Total Reservaciones", strconv.Itoa(data.KPITotalReservaciones))
	pdfKpiPar(pdf, "Reservas Pagadas", strconv.Itoa(data.KPIReservasPagadas),
		"Usuarios Activos", strconv.Itoa(data.KPIUsuariosActivos))
	pdfKpiPar(pdf, "Ingresos Totales", fmt.Sprintf("$ %.2f", data.KPIIngresos),
		"Ganancia MOVENT", fmt.Sprintf("$ %.2f", data.KPIGanancia))
	pdfKpiPar(pdf, "Ticket Promedio", fmt.Sprintf("$ %.2f", data.KPITicketPromedio), "", "")
	pdf.Ln(3)

	// ── Embudo ───────────────────────────────────────────────────────────
	if len(data.Embudo) > 0 {
		pdfSeccion(pdf, "EMBUDO DE CONVERSION")
		maxVal := pdfMaxInt(data.Embudo, 1)
		for _, row := range data.Embudo {
			if len(row) >= 2 {
				v, _ := strconv.Atoi(row[1])
				pdfBarra(pdf, row[0], v, maxVal, pdfColorDorado)
			}
		}
		pdf.Ln(3)
	}

	// ── Proveedores ──────────────────────────────────────────────────────
	if len(data.Proveedores) > 0 {
		pdfSeccion(pdf, "RENDIMIENTO DE PROVEEDORES")
		pdfTabla(pdf,
			[]string{"Proveedor", "Tipo", "Reserv.", "Ingresos $", "Ganancia $"},
			[]float64{52, 28, 20, 38, 38},
			data.Proveedores)
		pdf.Ln(3)
	}

	// ── Cancelaciones ────────────────────────────────────────────────────
	if len(data.Cancelaciones) > 0 {
		pdfSeccion(pdf, "CANCELACIONES POR TIPO DE RESERVA")
		maxVal := pdfMaxInt(data.Cancelaciones, 1)
		for _, row := range data.Cancelaciones {
			if len(row) >= 2 {
				v, _ := strconv.Atoi(row[1])
				pdfBarra(pdf, row[0], v, maxVal, pdfColorRojo)
			}
		}
		pdf.Ln(3)
	}

	// ── Tendencia mensual ─────────────────────────────────────────────────
	if len(data.Tendencia) > 0 {
		pdfSeccion(pdf, "TENDENCIA DE INGRESOS MENSUALES")
		pdfTabla(pdf,
			[]string{"Mes", "Tipo", "Ingresos $", "Cantidad"},
			[]float64{28, 28, 50, 25},
			data.Tendencia)
		pdf.Ln(3)
	}

	// ── Destinos ─────────────────────────────────────────────────────────
	if len(data.Destinos) > 0 {
		pdfSeccion(pdf, "DESTINOS MAS POPULARES")
		maxVal := pdfMaxInt(data.Destinos, 2)
		for _, row := range data.Destinos {
			if len(row) >= 3 {
				v, _ := strconv.Atoi(row[2])
				label := row[0]
				if row[1] != "" {
					label += ", " + row[1]
				}
				pdfBarra(pdf, label, v, maxVal, pdfColorDorado)
			}
		}
		pdf.Ln(3)
	}

	// ── Registro de búsquedas ─────────────────────────────────────────────
	if len(data.Listado) > 0 {
		pdfSeccion(pdf, "REGISTRO DE BUSQUEDAS (ultimas 30)")
		rows := data.Listado
		if len(rows) > 30 {
			rows = rows[:30]
		}
		var reducida [][]string
		for _, r := range rows {
			dest := ""
			if len(r) > 5 {
				dest = r[5]
			}
			reducida = append(reducida, []string{r[0], r[1], r[2], r[3], dest})
		}
		pdfTabla(pdf,
			[]string{"ID", "Fecha", "Tipo", "Usuario", "Destino"},
			[]float64{12, 38, 18, 48, 54},
			reducida)
	}

	var buf bytes.Buffer
	if err := pdf.Output(&buf); err != nil {
		return nil, err
	}
	return buf.Bytes(), nil
}

func pdfSeccion(pdf *gofpdf.Fpdf, titulo string) {
	pdf.SetFillColor(pdfColorOscuro[0], pdfColorOscuro[1], pdfColorOscuro[2])
	pdf.SetTextColor(pdfColorDorado[0], pdfColorDorado[1], pdfColorDorado[2])
	pdf.SetFont("Helvetica", "B", 8)
	pdf.CellFormat(180, 6, titulo, "", 1, "L", true, 0, "")
	pdf.SetTextColor(50, 50, 50)
	pdf.SetFont("Helvetica", "", 9)
	pdf.Ln(2)
}

func pdfKpiPar(pdf *gofpdf.Fpdf, lbl1, val1, lbl2, val2 string) {
	w := 87.5
	pdf.SetFont("Helvetica", "", 8)
	pdf.SetTextColor(100, 100, 100)
	pdf.CellFormat(w, 5, lbl1, "", 0, "L", false, 0, "")
	if lbl2 != "" {
		pdf.CellFormat(w, 5, lbl2, "", 1, "L", false, 0, "")
	} else {
		pdf.Ln(5)
	}
	pdf.SetFont("Helvetica", "B", 11)
	pdf.SetTextColor(pdfColorOscuro[0], pdfColorOscuro[1], pdfColorOscuro[2])
	pdf.CellFormat(w, 6, val1, "B", 0, "L", false, 0, "")
	if lbl2 != "" {
		pdf.CellFormat(w, 6, val2, "B", 1, "L", false, 0, "")
	} else {
		pdf.Ln(6)
	}
	pdf.Ln(2)
}

func pdfBarra(pdf *gofpdf.Fpdf, label string, val, maxVal int, color [3]int) {
	barMax := 115.0
	ratio := 0.0
	if maxVal > 0 {
		ratio = float64(val) / float64(maxVal)
	}
	barW := ratio * barMax
	if barW < 1 {
		barW = 1
	}

	pdf.SetFont("Helvetica", "", 8)
	pdf.SetTextColor(pdfColorGris[0], pdfColorGris[1], pdfColorGris[2])
	pdf.CellFormat(58, 5, label, "", 0, "L", false, 0, "")

	pdf.SetFillColor(color[0], color[1], color[2])
	x := pdf.GetX()
	y := pdf.GetY() + 0.5
	pdf.Rect(x, y, barW, 3.5, "F")

	pdf.SetX(x + barMax + 3)
	pdf.SetTextColor(50, 50, 50)
	pdf.CellFormat(18, 5, strconv.Itoa(val), "", 1, "L", false, 0, "")
}

func pdfTabla(pdf *gofpdf.Fpdf, headers []string, widths []float64, rows [][]string) {
	pdf.SetFillColor(pdfColorOscuro[0], pdfColorOscuro[1], pdfColorOscuro[2])
	pdf.SetTextColor(pdfColorDorado[0], pdfColorDorado[1], pdfColorDorado[2])
	pdf.SetFont("Helvetica", "B", 7)
	for i, h := range headers {
		if i < len(widths) {
			pdf.CellFormat(widths[i], 5, h, "1", 0, "L", true, 0, "")
		}
	}
	pdf.Ln(-1)

	pdf.SetFont("Helvetica", "", 7)
	for ri, row := range rows {
		if ri%2 == 0 {
			pdf.SetFillColor(250, 249, 247)
		} else {
			pdf.SetFillColor(255, 255, 255)
		}
		pdf.SetTextColor(50, 50, 50)
		for ci, cell := range row {
			if ci < len(widths) {
				pdf.CellFormat(widths[ci], 4.5, cell, "1", 0, "L", true, 0, "")
			}
		}
		pdf.Ln(-1)
	}
}

func pdfMaxInt(rows [][]string, colIdx int) int {
	max := 1
	for _, row := range rows {
		if colIdx < len(row) {
			if v, err := strconv.Atoi(row[colIdx]); err == nil && v > max {
				max = v
			}
		}
	}
	return max
}

// ── Email con adjunto genérico ────────────────────────────────────────────────

// EnviarEmailConAdjunto envía un correo HTML con cualquier tipo de archivo adjunto.
// A diferencia de EnviarEmailConPDF, acepta el tipo MIME del adjunto como parámetro.
func EnviarEmailConAdjunto(destinatario, asunto, htmlBody string, fileBytes []byte, nombreArchivo, mimeType string) error {
	cfg := GetSMTPConfig()
	if cfg.User == "" {
		return fmt.Errorf("SMTP_USER no configurado")
	}
	if cfg.Password == "" {
		return fmt.Errorf("SMTP_PASS no configurado")
	}
	from := cfg.From
	if from == "" {
		from = cfg.User
	}

	var buf bytes.Buffer
	writer := multipart.NewWriter(&buf)

	// Parte 1 — HTML
	htmlH := make(textproto.MIMEHeader)
	htmlH.Set("Content-Type", "text/html; charset=UTF-8")
	htmlH.Set("Content-Transfer-Encoding", "base64")
	htmlPart, err := writer.CreatePart(htmlH)
	if err != nil {
		return fmt.Errorf("error creando parte HTML: %w", err)
	}
	htmlPart.Write(base64Lines([]byte(htmlBody)))

	// Parte 2 — Adjunto
	attH := make(textproto.MIMEHeader)
	attH.Set("Content-Type", mimeType)
	attH.Set("Content-Transfer-Encoding", "base64")
	attH.Set("Content-Disposition", fmt.Sprintf(`attachment; filename="%s"`, nombreArchivo))
	attPart, err := writer.CreatePart(attH)
	if err != nil {
		return fmt.Errorf("error creando parte adjunto: %w", err)
	}
	attPart.Write(base64Lines(fileBytes))

	writer.Close()

	header := fmt.Sprintf(
		"From: %s\r\nTo: %s\r\nSubject: %s\r\nDate: %s\r\nMIME-Version: 1.0\r\nContent-Type: multipart/mixed; boundary=\"%s\"\r\n\r\n",
		from, destinatario, asunto,
		time.Now().Format(time.RFC1123Z),
		writer.Boundary(),
	)
	msg := []byte(header + buf.String())

	addr := net.JoinHostPort(cfg.Host, cfg.Port)
	conn, err := net.DialTimeout("tcp", addr, 10*time.Second)
	if err != nil {
		return fmt.Errorf("error conectando SMTP: %w", err)
	}
	client, err := smtp.NewClient(conn, cfg.Host)
	if err != nil {
		return err
	}
	defer client.Close()

	if err = client.StartTLS(tlsConfigFor(cfg.Host)); err != nil {
		return err
	}
	auth := smtp.PlainAuth("", cfg.User, cfg.Password, cfg.Host)
	if err = client.Auth(auth); err != nil {
		return err
	}
	if err = client.Mail(cfg.User); err != nil {
		return err
	}
	if err = client.Rcpt(destinatario); err != nil {
		return err
	}
	wc, err := client.Data()
	if err != nil {
		return err
	}
	if _, err = wc.Write(msg); err != nil {
		return err
	}
	if err = wc.Close(); err != nil {
		return err
	}
	return client.Quit()
}

// ── helpers internos ─────────────────────────────────────────────────────────

// base64Lines codifica bytes en base64 con saltos de línea cada 76 caracteres.
func base64Lines(data []byte) []byte {
	encoded := base64.StdEncoding.EncodeToString(data)
	var sb strings.Builder
	for i, ch := range encoded {
		sb.WriteRune(ch)
		if (i+1)%76 == 0 {
			sb.WriteString("\r\n")
		}
	}
	return []byte(sb.String())
}

// tlsConfigFor retorna la configuración TLS para el servidor SMTP indicado.
func tlsConfigFor(host string) *tls.Config {
	return &tls.Config{ServerName: host}
}

// BuildHTMLMetricasCorreo genera el HTML del correo que acompaña al reporte adjunto.
func BuildHTMLMetricasCorreo(data MetricasData) string {
	return fmt.Sprintf(`<!DOCTYPE html>
<html lang="es">
<head><meta charset="UTF-8"><title>MOVENT Metricas</title></head>
<body style="margin:0;padding:0;background:#F5F2EC;font-family:Helvetica,Arial,sans-serif;">
<table width="100%%" cellpadding="0" cellspacing="0">
<tr><td align="center" style="padding:32px 12px;">
<table width="600" cellpadding="0" cellspacing="0"
  style="max-width:600px;border-radius:12px;overflow:hidden;box-shadow:0 4px 24px rgba(28,26,24,0.12);border:1px solid #ddd6cc;">
  <tr><td style="background:#1C1A18;padding:26px 28px 18px;">
    <div style="font-size:26px;font-weight:bold;color:#FFCC00;letter-spacing:4px;">MOVENT</div>
    <div style="font-size:10px;color:#6b6358;margin-top:4px;">Agencia de Viajes &middot; Reporte de Metricas</div>
  </td></tr>
  <tr><td style="background:#FFCC00;height:3px;"></td></tr>
  <tr><td style="background:#F5F2EC;padding:28px 28px 24px;">
    <p style="font-size:16px;font-weight:bold;color:#1C1A18;margin:0 0 10px;">Reporte listo para revisar</p>
    <p style="font-size:13px;color:#5a5047;line-height:1.7;margin:0 0 20px;">
      Adjunto encontraras el reporte de metricas de MOVENT para el periodo
      <strong>%s</strong> al <strong>%s</strong>.
    </p>
    <table width="100%%" cellpadding="0" cellspacing="0" style="border-radius:8px;overflow:hidden;margin-bottom:20px;">
      <tr style="background:#1C1A18;">
        <td style="padding:8px 16px;font-size:10px;font-weight:bold;color:#FFCC00;letter-spacing:1px;width:60%%;">INDICADOR</td>
        <td style="padding:8px 16px;font-size:10px;font-weight:bold;color:#FFCC00;letter-spacing:1px;">VALOR</td>
      </tr>
      <tr style="background:#fff;"><td style="padding:8px 16px;font-size:12px;color:#5a5047;border-bottom:1px solid #f0ebe3;">Total Busquedas</td><td style="padding:8px 16px;font-size:12px;font-weight:bold;color:#1C1A18;border-bottom:1px solid #f0ebe3;">%d</td></tr>
      <tr style="background:#faf8f5;"><td style="padding:8px 16px;font-size:12px;color:#5a5047;border-bottom:1px solid #f0ebe3;">Total Reservaciones</td><td style="padding:8px 16px;font-size:12px;font-weight:bold;color:#1C1A18;border-bottom:1px solid #f0ebe3;">%d</td></tr>
      <tr style="background:#fff;"><td style="padding:8px 16px;font-size:12px;color:#5a5047;border-bottom:1px solid #f0ebe3;">Ingresos Totales</td><td style="padding:8px 16px;font-size:12px;font-weight:bold;color:#1C1A18;border-bottom:1px solid #f0ebe3;">$ %.2f</td></tr>
      <tr style="background:#faf8f5;"><td style="padding:8px 16px;font-size:12px;color:#5a5047;border-bottom:1px solid #f0ebe3;">Ganancia MOVENT</td><td style="padding:8px 16px;font-size:12px;font-weight:bold;color:#1C1A18;border-bottom:1px solid #f0ebe3;">$ %.2f</td></tr>
      <tr style="background:#fff;"><td style="padding:8px 16px;font-size:12px;color:#5a5047;">Usuarios Activos</td><td style="padding:8px 16px;font-size:12px;font-weight:bold;color:#1C1A18;">%d</td></tr>
    </table>
    <p style="font-size:10px;color:#9a9089;margin:0;">Correo automatico — no responder a este mensaje.</p>
  </td></tr>
  <tr><td style="background:#1C1A18;padding:14px 28px;text-align:center;">
    <div style="font-size:10px;color:#6b6358;">MOVENT &middot; info@movent.gt &middot; Guatemala City, Guatemala</div>
  </td></tr>
</table>
</td></tr>
</table>
</body></html>`,
		data.Desde, data.Hasta,
		data.KPITotalBusquedas, data.KPITotalReservaciones,
		data.KPIIngresos, data.KPIGanancia, data.KPIUsuariosActivos,
	)
}
