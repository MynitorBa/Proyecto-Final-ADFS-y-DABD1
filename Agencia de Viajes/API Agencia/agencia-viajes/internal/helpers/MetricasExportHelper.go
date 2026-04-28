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
	Proveedores   [][]string // nombre, grupo, tipoProveedor, reservaciones, ingresos, ganancia
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

// xlsxChartDef describe una gráfica que se incrustará en una hoja de Excel.
type xlsxChartDef struct {
	SheetIdx  int    // índice 1-based de la hoja en el slice final
	ChartIdx  int    // índice 1-based de la gráfica (chart1.xml, drawing1.xml…)
	SheetName string // nombre de la hoja (para referencia de fórmulas)
	CatCol    string // columna de categorías, e.g. "A"
	ValCol    string // columna de valores, e.g. "B"
	RowCount  int    // número de filas de datos (sin cabecera)
	ChartType string // "col" | "bar" | "line"
	Title     string
}

// GenerarMetricasXLSX construye un archivo XLSX (OpenXML) con múltiples hojas
// y gráficas embebidas al lado derecho de los datos en las hojas relevantes.
// Utiliza únicamente archive/zip y generación de XML — sin librerías externas.
func GenerarMetricasXLSX(data MetricasData) []byte {
	sheets := metricasSheets(data)
	charts := metricasCharts(sheets)

	var buf bytes.Buffer
	w := zip.NewWriter(&buf)

	xlsxAdd(w, "[Content_Types].xml", xlsxContentTypes(len(sheets), len(charts)))
	xlsxAdd(w, "_rels/.rels", xlsxPackageRels())
	xlsxAdd(w, "xl/workbook.xml", xlsxWorkbook(sheets))
	xlsxAdd(w, "xl/_rels/workbook.xml.rels", xlsxWorkbookRels(len(sheets)))
	xlsxAdd(w, "xl/styles.xml", xlsxStyles())

	// Map sheet → chart
	sheetChart := map[int]int{}
	for _, ch := range charts {
		sheetChart[ch.SheetIdx] = ch.ChartIdx
	}

	for i, sh := range sheets {
		chartIdx := sheetChart[i+1]
		drawingRId := ""
		if chartIdx > 0 {
			drawingRId = "rId1"
		}
		xlsxAdd(w, fmt.Sprintf("xl/worksheets/sheet%d.xml", i+1), xlsxSheetXML(sh, drawingRId))
		if chartIdx > 0 {
			xlsxAdd(w, fmt.Sprintf("xl/worksheets/_rels/sheet%d.xml.rels", i+1), xlsxSheetRels(chartIdx))
			xlsxAdd(w, fmt.Sprintf("xl/drawings/drawing%d.xml", chartIdx), xlsxDrawingXML(chartIdx))
			xlsxAdd(w, fmt.Sprintf("xl/drawings/_rels/drawing%d.xml.rels", chartIdx), xlsxDrawingRels(chartIdx))
		}
	}

	for _, ch := range charts {
		xlsxAdd(w, fmt.Sprintf("xl/charts/chart%d.xml", ch.ChartIdx), xlsxChartXML(ch))
	}

	w.Close()
	return buf.Bytes()
}

// metricasCharts determina qué hojas llevan gráfica y construye sus definiciones.
func metricasCharts(sheets []xlsxSheetDef) []xlsxChartDef {
	var charts []xlsxChartDef
	chartIdx := 0
	for i, sh := range sheets {
		if len(sh.Rows) == 0 {
			continue
		}
		var def *xlsxChartDef
		switch sh.Name {
		case "Busquedas Dia":
			def = &xlsxChartDef{SheetIdx: i + 1, SheetName: sh.Name, CatCol: "A", ValCol: "B", RowCount: len(sh.Rows), ChartType: "col", Title: "Busquedas por Dia"}
		case "Busquedas Tipo":
			def = &xlsxChartDef{SheetIdx: i + 1, SheetName: sh.Name, CatCol: "A", ValCol: "B", RowCount: len(sh.Rows), ChartType: "bar", Title: "Busquedas por Tipo"}
		case "Destinos":
			def = &xlsxChartDef{SheetIdx: i + 1, SheetName: sh.Name, CatCol: "A", ValCol: "C", RowCount: len(sh.Rows), ChartType: "bar", Title: "Destinos Populares"}
		case "Tendencia Mensual":
			def = &xlsxChartDef{SheetIdx: i + 1, SheetName: sh.Name, CatCol: "A", ValCol: "C", RowCount: len(sh.Rows), ChartType: "line", Title: "Tendencia de Ingresos"}
		case "Proveedores":
			def = &xlsxChartDef{SheetIdx: i + 1, SheetName: sh.Name, CatCol: "A", ValCol: "E", RowCount: len(sh.Rows), ChartType: "bar", Title: "Ingresos por Proveedor"}
		case "Cancelaciones":
			def = &xlsxChartDef{SheetIdx: i + 1, SheetName: sh.Name, CatCol: "A", ValCol: "B", RowCount: len(sh.Rows), ChartType: "bar", Title: "Cancelaciones por Tipo"}
		}
		if def != nil {
			chartIdx++
			def.ChartIdx = chartIdx
			charts = append(charts, *def)
		}
	}
	return charts
}

func xlsxAdd(w *zip.Writer, name, content string) {
	f, _ := w.Create(name)
	f.Write([]byte(content))
}

func xlsxContentTypes(nSheets, nCharts int) string {
	var sb strings.Builder
	sb.WriteString(`<?xml version="1.0" encoding="UTF-8" standalone="yes"?>`)
	sb.WriteString(`<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">`)
	sb.WriteString(`<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>`)
	sb.WriteString(`<Default Extension="xml" ContentType="application/xml"/>`)
	sb.WriteString(`<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>`)
	sb.WriteString(`<Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>`)
	for i := 1; i <= nSheets; i++ {
		sb.WriteString(fmt.Sprintf(`<Override PartName="/xl/worksheets/sheet%d.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>`, i))
	}
	for i := 1; i <= nCharts; i++ {
		sb.WriteString(fmt.Sprintf(`<Override PartName="/xl/charts/chart%d.xml" ContentType="application/vnd.openxmlformats-officedocument.drawingml.chart+xml"/>`, i))
		sb.WriteString(fmt.Sprintf(`<Override PartName="/xl/drawings/drawing%d.xml" ContentType="application/vnd.openxmlformats-officedocument.drawing+xml"/>`, i))
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

func xlsxSheetXML(sh xlsxSheetDef, drawingRId string) string {
	var sb strings.Builder
	sb.WriteString(`<?xml version="1.0" encoding="UTF-8" standalone="yes"?>`)
	if drawingRId != "" {
		sb.WriteString(`<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">`)
	} else {
		sb.WriteString(`<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">`)
	}
	sb.WriteString(`<sheetData>`)

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

	sb.WriteString(`</sheetData>`)
	if drawingRId != "" {
		sb.WriteString(fmt.Sprintf(`<drawing r:id="%s"/>`, drawingRId))
	}
	sb.WriteString(`</worksheet>`)
	return sb.String()
}

// ── Gráficas XLSX ─────────────────────────────────────────────────────────────

// xlsxSheetRels genera el archivo de relaciones de una hoja con su drawing.
func xlsxSheetRels(chartIdx int) string {
	return `<?xml version="1.0" encoding="UTF-8" standalone="yes"?>` +
		`<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">` +
		fmt.Sprintf(`<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/drawing" Target="../drawings/drawing%d.xml"/>`, chartIdx) +
		`</Relationships>`
}

// xlsxDrawingXML posiciona la gráfica al lado derecho de los datos.
func xlsxDrawingXML(chartIdx int) string {
	return `<?xml version="1.0" encoding="UTF-8" standalone="yes"?>` +
		`<xdr:wsDr xmlns:xdr="http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing" ` +
		`xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" ` +
		`xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">` +
		`<xdr:twoCellAnchor moveWithCells="1" sizeWithCells="1">` +
		// Inicio: columna E (índice 4), fila 1 (índice 0)
		`<xdr:from><xdr:col>4</xdr:col><xdr:colOff>0</xdr:colOff><xdr:row>0</xdr:row><xdr:rowOff>0</xdr:rowOff></xdr:from>` +
		// Fin: columna O (índice 14), fila 22 (índice 21)
		`<xdr:to><xdr:col>14</xdr:col><xdr:colOff>0</xdr:colOff><xdr:row>21</xdr:row><xdr:rowOff>0</xdr:rowOff></xdr:to>` +
		`<xdr:graphicFrame macro="">` +
		fmt.Sprintf(`<xdr:nvGraphicFramePr><xdr:cNvPr id="2" name="Grafico %d"/><xdr:cNvGraphicFramePr/></xdr:nvGraphicFramePr>`, chartIdx) +
		`<xdr:xfrm><a:off x="0" y="0"/><a:ext cx="0" cy="0"/></xdr:xfrm>` +
		`<a:graphic><a:graphicData uri="http://schemas.openxmlformats.org/drawingml/2006/chart">` +
		`<c:chart xmlns:c="http://schemas.openxmlformats.org/drawingml/2006/chart" r:id="rId1"/>` +
		`</a:graphicData></a:graphic>` +
		`</xdr:graphicFrame><xdr:clientData/>` +
		`</xdr:twoCellAnchor></xdr:wsDr>`
}

// xlsxDrawingRels vincula el drawing con su archivo de gráfica.
func xlsxDrawingRels(chartIdx int) string {
	return `<?xml version="1.0" encoding="UTF-8" standalone="yes"?>` +
		`<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">` +
		fmt.Sprintf(`<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/chart" Target="../charts/chart%d.xml"/>`, chartIdx) +
		`</Relationships>`
}

// xlsxChartXML genera el XML completo de una gráfica en formato OpenXML.
func xlsxChartXML(ch xlsxChartDef) string {
	// Escapar el nombre de la hoja para fórmulas de Excel
	sheetRef := "'" + strings.ReplaceAll(ch.SheetName, "'", "''") + "'"
	lastRow := ch.RowCount + 1 // fila 2 a N+1 (fila 1 es cabecera)
	catRef := fmt.Sprintf("%s!$%s$2:$%s$%d", sheetRef, ch.CatCol, ch.CatCol, lastRow)
	valRef := fmt.Sprintf("%s!$%s$2:$%s$%d", sheetRef, ch.ValCol, ch.ValCol, lastRow)

	var plotArea string
	switch ch.ChartType {
	case "line":
		plotArea = xlsxLineChartBody(catRef, valRef)
	default: // "col" o "bar"
		plotArea = xlsxBarChartBody(catRef, valRef, ch.ChartType)
	}

	return `<?xml version="1.0" encoding="UTF-8" standalone="yes"?>` +
		`<c:chartSpace xmlns:c="http://schemas.openxmlformats.org/drawingml/2006/chart" ` +
		`xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" ` +
		`xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">` +
		`<c:roundedCorners val="0"/>` +
		`<c:chart>` +
		`<c:title><c:tx><c:rich><a:bodyPr/><a:lstStyle/>` +
		`<a:p><a:pPr><a:defRPr b="1"/></a:pPr>` +
		`<a:r><a:rPr b="1"/><a:t>` + xlEscape(ch.Title) + `</a:t></a:r></a:p>` +
		`</c:rich></c:tx><c:overlay val="0"/></c:title>` +
		`<c:autoTitleDeleted val="0"/>` +
		`<c:plotArea><c:layout/>` + plotArea + `</c:plotArea>` +
		`<c:legend><c:legendPos val="b"/><c:overlay val="0"/></c:legend>` +
		`<c:plotVisOnly val="1"/>` +
		`</c:chart>` +
		`<c:spPr>` +
		`<a:solidFill><a:srgbClr val="FFFFFF"/></a:solidFill>` +
		`<a:ln><a:solidFill><a:srgbClr val="DDD6CC"/></a:solidFill></a:ln>` +
		`</c:spPr>` +
		`</c:chartSpace>`
}

func xlsxBarChartBody(catRef, valRef, dir string) string {
	return fmt.Sprintf(
		`<c:barChart><c:barDir val="%s"/><c:grouping val="clustered"/><c:varyColors val="1"/>`,
		dir,
	) +
		`<c:ser><c:idx val="0"/><c:order val="0"/>` +
		`<c:spPr><a:solidFill><a:srgbClr val="D4AF37"/></a:solidFill>` +
		`<a:ln><a:solidFill><a:srgbClr val="B8922E"/></a:solidFill></a:ln></c:spPr>` +
		`<c:cat><c:strRef><c:f>` + xlEscape(catRef) + `</c:f></c:strRef></c:cat>` +
		`<c:val><c:numRef><c:f>` + xlEscape(valRef) + `</c:f></c:numRef></c:val>` +
		`</c:ser>` +
		`<c:axId val="1001"/><c:axId val="1002"/>` +
		`</c:barChart>` +
		`<c:catAx><c:axId val="1001"/><c:scaling><c:orientation val="minMax"/></c:scaling>` +
		`<c:delete val="0"/><c:axPos val="b"/>` +
		`<c:numFmt formatCode="General" sourceLinked="0"/>` +
		`<c:tickLblPos val="nextTo"/><c:crossAx val="1002"/>` +
		`</c:catAx>` +
		`<c:valAx><c:axId val="1002"/><c:scaling><c:orientation val="minMax"/></c:scaling>` +
		`<c:delete val="0"/><c:axPos val="l"/>` +
		`<c:numFmt formatCode="#,##0" sourceLinked="0"/>` +
		`<c:tickLblPos val="nextTo"/><c:crossAx val="1001"/>` +
		`</c:valAx>`
}

func xlsxLineChartBody(catRef, valRef string) string {
	return `<c:lineChart><c:grouping val="standard"/><c:varyColors val="0"/>` +
		`<c:ser><c:idx val="0"/><c:order val="0"/>` +
		`<c:spPr><a:ln w="25400"><a:solidFill><a:srgbClr val="D4AF37"/></a:solidFill></a:ln></c:spPr>` +
		`<c:marker><c:symbol val="circle"/><c:size val="5"/>` +
		`<c:spPr><a:solidFill><a:srgbClr val="D4AF37"/></a:solidFill>` +
		`<a:ln><a:solidFill><a:srgbClr val="D4AF37"/></a:solidFill></a:ln></c:spPr>` +
		`</c:marker>` +
		`<c:cat><c:strRef><c:f>` + xlEscape(catRef) + `</c:f></c:strRef></c:cat>` +
		`<c:val><c:numRef><c:f>` + xlEscape(valRef) + `</c:f></c:numRef></c:val>` +
		`<c:smooth val="0"/>` +
		`</c:ser>` +
		`<c:axId val="1001"/><c:axId val="1002"/>` +
		`</c:lineChart>` +
		`<c:catAx><c:axId val="1001"/><c:scaling><c:orientation val="minMax"/></c:scaling>` +
		`<c:delete val="0"/><c:axPos val="b"/>` +
		`<c:numFmt formatCode="General" sourceLinked="0"/>` +
		`<c:tickLblPos val="nextTo"/><c:crossAx val="1002"/>` +
		`</c:catAx>` +
		`<c:valAx><c:axId val="1002"/><c:scaling><c:orientation val="minMax"/></c:scaling>` +
		`<c:delete val="0"/><c:axPos val="l"/>` +
		`<c:numFmt formatCode="#,##0.00" sourceLinked="0"/>` +
		`<c:tickLblPos val="nextTo"/><c:crossAx val="1001"/>` +
		`</c:valAx>`
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
		{Name: "Proveedores", Headers: []string{"Proveedor", "Grupo", "Tipo Proveedor", "Reservaciones", "Ingresos", "Ganancia"}, Rows: data.Proveedores},
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

	// ── CSV (ZIP de hojas) ────────────────────────────────────────────────────

	// Calcular totales para porcentajes
	var totalDestinos, totalCancel, totalBusqTipo, totalResTipo, totalEmbudo int
	for _, r := range data.Destinos {
		if len(r) >= 3 {
			v, _ := strconv.Atoi(r[2])
			totalDestinos += v
		}
	}
	for _, r := range data.Cancelaciones {
		if len(r) >= 2 {
			v, _ := strconv.Atoi(r[1])
			totalCancel += v
		}
	}
	for _, r := range data.BusquedasTipo {
		if len(r) >= 2 {
			v, _ := strconv.Atoi(r[1])
			totalBusqTipo += v
		}
	}
	for _, r := range data.ResTipos {
		if len(r) >= 2 {
			v, _ := strconv.Atoi(r[1])
			totalResTipo += v
		}
	}
	for _, r := range data.Embudo {
		if len(r) >= 2 {
			v, _ := strconv.Atoi(r[1])
			totalEmbudo += v
		}
	}
	pctStr := func(num, den int) string {
		if den == 0 {
			return "0.0%"
		}
		return fmt.Sprintf("%.1f%%", float64(num)/float64(den)*100)
	}

	// Agregar % a filas de destinos
	var destinosPct [][]string
	for _, r := range data.Destinos {
		if len(r) >= 3 {
			v, _ := strconv.Atoi(r[2])
			destinosPct = append(destinosPct, append(r, pctStr(v, totalDestinos)))
		}
	}
	if totalDestinos > 0 {
		destinosPct = append(destinosPct, []string{"Total", "", strconv.Itoa(totalDestinos), "100.0%"})
	}

	var cancelPct [][]string
	for _, r := range data.Cancelaciones {
		if len(r) >= 2 {
			v, _ := strconv.Atoi(r[1])
			cancelPct = append(cancelPct, append(r, pctStr(v, totalCancel)))
		}
	}
	if totalCancel > 0 {
		cancelPct = append(cancelPct, []string{"Total", strconv.Itoa(totalCancel), "100.0%"})
	}

	var busqTipoPct [][]string
	for _, r := range data.BusquedasTipo {
		if len(r) >= 2 {
			v, _ := strconv.Atoi(r[1])
			busqTipoPct = append(busqTipoPct, append(r, pctStr(v, totalBusqTipo)))
		}
	}
	if totalBusqTipo > 0 {
		busqTipoPct = append(busqTipoPct, []string{"Total", strconv.Itoa(totalBusqTipo), "100.0%"})
	}

	var resTipoPct [][]string
	for _, r := range data.ResTipos {
		if len(r) >= 2 {
			v, _ := strconv.Atoi(r[1])
			resTipoPct = append(resTipoPct, append(r, pctStr(v, totalResTipo)))
		}
	}
	if totalResTipo > 0 {
		resTipoPct = append(resTipoPct, []string{"Total", strconv.Itoa(totalResTipo), "", "100.0%"})
	}

	var embudoPct [][]string
	for _, r := range data.Embudo {
		if len(r) >= 2 {
			v, _ := strconv.Atoi(r[1])
			embudoPct = append(embudoPct, append(r, pctStr(v, totalEmbudo)))
		}
	}
	if totalEmbudo > 0 {
		embudoPct = append(embudoPct, []string{"Total", strconv.Itoa(totalEmbudo), "100.0%"})
	}

	kpisRows := [][]string{
		{"Total Busquedas", strconv.Itoa(data.KPITotalBusquedas)},
		{"Busquedas Vuelo", strconv.Itoa(data.KPIBusquedasVuelo)},
		{"Busquedas Hotel", strconv.Itoa(data.KPIBusquedasHotel)},
		{"Total Reservaciones", strconv.Itoa(data.KPITotalReservaciones)},
		{"Reservas Confirmadas", strconv.Itoa(data.KPIReservasPagadas)},
		{"Usuarios Activos", strconv.Itoa(data.KPIUsuariosActivos)},
		{"Ingresos Totales", fmt.Sprintf("%.2f", data.KPIIngresos)},
		{"Ganancia MOVENT", fmt.Sprintf("%.2f", data.KPIGanancia)},
		{"Ticket Promedio", fmt.Sprintf("%.2f", data.KPITicketPromedio)},
	}

	csvFiles := []struct {
		name    string
		headers []string
		rows    [][]string
	}{
		{"kpis.csv", []string{"Metrica", "Valor"}, kpisRows},
		{"busquedas_dia.csv", []string{"Fecha", "Total"}, data.BusquedasDia},
		{"busquedas_tipo.csv", []string{"Tipo", "Total", "Porcentaje"}, busqTipoPct},
		{"destinos.csv", []string{"Ciudad", "Pais", "Total", "Porcentaje"}, destinosPct},
		{"reservaciones_tipo.csv", []string{"Tipo", "Total", "Ingresos", "Porcentaje"}, resTipoPct},
		{"embudo.csv", []string{"Etapa", "Total", "Porcentaje"}, embudoPct},
		{"proveedores.csv", []string{"Proveedor", "Grupo", "Tipo Proveedor", "Reservaciones", "Ingresos", "Ganancia"}, data.Proveedores},
		{"cancelaciones.csv", []string{"Tipo", "Total", "Porcentaje"}, cancelPct},
		{"tendencia_mensual.csv", []string{"Mes", "Tipo", "Ingresos", "Cantidad"}, data.Tendencia},
		{"heatmap_busquedas.csv", []string{"Dia", "Hora", "Total"}, data.Heatmap},
		{"registro_busquedas.csv", []string{"ID", "Fecha", "Tipo", "Usuario", "Origen", "Destino"}, data.Listado},
	}

	for _, cf := range csvFiles {
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
	pdfColorVerde  = [3]int{5, 150, 105}
	pdfColorAzul   = [3]int{59, 130, 246}
	pdfColorMorado = [3]int{124, 58, 237}
)

// pdfStr convierte cadenas UTF-8 a Latin-1 compatible con gofpdf (Helvetica).
func pdfStr(s string) string {
	r := strings.NewReplacer(
		"á", "a", "é", "e", "í", "i", "ó", "o", "ú", "u",
		"Á", "A", "É", "E", "Í", "I", "Ó", "O", "Ú", "U",
		"ñ", "n", "Ñ", "N", "ü", "u", "Ü", "U",
		"ä", "a", "ö", "o",
		"¿", "?", "¡", "!",
		"—", "-", "–", "-", "·", ".",
		"\u2019", "'", "\u201C", "\"", "\u201D", "\"",
		"€", "EUR",
	)
	return r.Replace(s)
}

// GenerarMetricasPDF genera un PDF de métricas completo usando gofpdf.
func GenerarMetricasPDF(data MetricasData) ([]byte, error) {
	pdf := gofpdf.New("P", "mm", "A4", "")
	pdf.SetMargins(15, 15, 15)
	pdf.SetAutoPageBreak(true, 18)

	pdf.SetFooterFunc(func() {
		pdf.SetY(-12)
		pdf.SetTextColor(pdfColorGris[0], pdfColorGris[1], pdfColorGris[2])
		pdf.SetFont("Helvetica", "", 7)
		pdf.CellFormat(0, 10,
			fmt.Sprintf("MOVENT  -  Reporte de Metricas  -  Pagina %d  -  Periodo: %s al %s",
				pdf.PageNo(), data.Desde, data.Hasta),
			"", 0, "C", false, 0, "")
	})

	pdf.AddPage()

	// ── Encabezado ───────────────────────────────────────────────────────────
	pdf.SetFillColor(pdfColorOscuro[0], pdfColorOscuro[1], pdfColorOscuro[2])
	pdf.Rect(0, 0, 210, 42, "F")
	pdf.SetFillColor(pdfColorDorado[0], pdfColorDorado[1], pdfColorDorado[2])
	pdf.Rect(0, 42, 210, 2, "F")

	pdf.SetTextColor(pdfColorDorado[0], pdfColorDorado[1], pdfColorDorado[2])
	pdf.SetFont("Helvetica", "B", 26)
	pdf.SetXY(15, 7)
	pdf.Cell(100, 14, "MOVENT")

	pdf.SetTextColor(255, 255, 255)
	pdf.SetFont("Helvetica", "", 10)
	pdf.SetXY(15, 22)
	pdf.Cell(180, 6, "Reporte de Metricas - Panel Administrativo")

	pdf.SetTextColor(pdfColorDorado[0], pdfColorDorado[1], pdfColorDorado[2])
	pdf.SetFont("Helvetica", "", 8)
	pdf.SetXY(15, 30)
	pdf.Cell(180, 6, fmt.Sprintf("Periodo: %s  al  %s     Generado: %s",
		data.Desde, data.Hasta, time.Now().Format("02/01/2006 15:04")))

	pdf.SetY(50)

	// ── KPIs (3 por fila) ────────────────────────────────────────────────────
	pdfSeccion(pdf, "KPI RESUMEN")
	kpiW := 57.0
	kpiH := 14.0
	kpiPad := 3.0
	type kpiItem struct {
		lbl, val string
		color    [3]int
	}
	kpis := []kpiItem{
		{"Total Busquedas", strconv.Itoa(data.KPITotalBusquedas), pdfColorOscuro},
		{"Busquedas Vuelo", strconv.Itoa(data.KPIBusquedasVuelo), pdfColorOscuro},
		{"Busquedas Hotel", strconv.Itoa(data.KPIBusquedasHotel), pdfColorOscuro},
		{"Total Reservaciones", strconv.Itoa(data.KPITotalReservaciones), pdfColorOscuro},
		{"Confirmadas/Activas", strconv.Itoa(data.KPIReservasPagadas), pdfColorVerde},
		{"Usuarios Activos", strconv.Itoa(data.KPIUsuariosActivos), pdfColorAzul},
		{"Ingresos Totales", fmt.Sprintf("$ %.2f", data.KPIIngresos), pdfColorVerde},
		{"Ganancia MOVENT", fmt.Sprintf("$ %.2f", data.KPIGanancia), pdfColorDorado},
		{"Ticket Promedio", fmt.Sprintf("$ %.2f", data.KPITicketPromedio), pdfColorAzul},
	}
	startX := pdf.GetX()
	rowY := pdf.GetY() // Y fijo para toda la fila actual
	for i, k := range kpis {
		col := i % 3
		if col == 0 && i > 0 {
			// Avanzar al inicio de la siguiente fila
			pdf.SetXY(startX, rowY+kpiH+kpiPad)
			rowY = pdf.GetY()
		}
		x := startX + float64(col)*(kpiW+kpiPad)
		y := rowY // Todos los cards de la fila comparten el mismo Y
		// Card background
		pdf.SetFillColor(248, 248, 246)
		pdf.Rect(x, y, kpiW, kpiH, "F")
		// Accent left bar
		pdf.SetFillColor(k.color[0], k.color[1], k.color[2])
		pdf.Rect(x, y, 2.5, kpiH, "F")
		// Label
		pdf.SetFont("Helvetica", "", 7)
		pdf.SetTextColor(pdfColorGris[0], pdfColorGris[1], pdfColorGris[2])
		pdf.SetXY(x+5, y+2)
		pdf.CellFormat(kpiW-7, 4, pdfStr(k.lbl), "", 0, "L", false, 0, "")
		// Value
		pdf.SetFont("Helvetica", "B", 10)
		pdf.SetTextColor(k.color[0], k.color[1], k.color[2])
		pdf.SetXY(x+5, y+7)
		pdf.CellFormat(kpiW-7, 5, k.val, "", 0, "L", false, 0, "")
	}
	pdf.SetXY(startX, rowY+kpiH+kpiPad+4)

	// ── Busquedas por tipo ───────────────────────────────────────────────────
	if len(data.BusquedasTipo) > 0 {
		pdfSeccion(pdf, "BUSQUEDAS POR TIPO")
		totalBT := 0
		for _, r := range data.BusquedasTipo {
			if len(r) >= 2 {
				v, _ := strconv.Atoi(r[1])
				totalBT += v
			}
		}
		maxBT := pdfMaxInt(data.BusquedasTipo, 1)
		colors := [][3]int{pdfColorDorado, pdfColorAzul, pdfColorMorado}
		for i, row := range data.BusquedasTipo {
			if len(row) >= 2 {
				v, _ := strconv.Atoi(row[1])
				c := colors[i%len(colors)]
				pdfBarraPct(pdf, pdfStr(row[0]), v, maxBT, totalBT, c)
			}
		}
		pdfBarraTotal(pdf, totalBT)
		pdf.Ln(3)
	}

	// ── Reservaciones por tipo ───────────────────────────────────────────────
	if len(data.ResTipos) > 0 {
		pdfSeccion(pdf, "RESERVACIONES POR TIPO (activas/completadas)")
		totalRT := 0
		for _, r := range data.ResTipos {
			if len(r) >= 2 {
				v, _ := strconv.Atoi(r[1])
				totalRT += v
			}
		}
		maxRT := pdfMaxInt(data.ResTipos, 1)
		colors := [][3]int{pdfColorDorado, pdfColorAzul, pdfColorMorado}
		for i, row := range data.ResTipos {
			if len(row) >= 2 {
				v, _ := strconv.Atoi(row[1])
				ing := ""
				if len(row) >= 3 {
					ing = "  $ " + row[2]
				}
				c := colors[i%len(colors)]
				pdfBarraPctExtra(pdf, pdfStr(row[0]), v, maxRT, totalRT, ing, c)
			}
		}
		pdfBarraTotal(pdf, totalRT)
		pdf.Ln(3)
	}

	// ── Embudo ───────────────────────────────────────────────────────────────
	if len(data.Embudo) > 0 {
		pdfSeccion(pdf, "EMBUDO DE CONVERSION")
		totalEmb := 0
		for _, r := range data.Embudo {
			if len(r) >= 2 {
				v, _ := strconv.Atoi(r[1])
				totalEmb += v
			}
		}
		maxEmb := pdfMaxInt(data.Embudo, 1)
		embudoColors := [][3]int{
			pdfColorDorado, pdfColorAzul, pdfColorVerde, pdfColorVerde,
			pdfColorRojo, pdfColorGris, {245, 158, 11},
		}
		for i, row := range data.Embudo {
			if len(row) >= 2 {
				v, _ := strconv.Atoi(row[1])
				c := embudoColors[i%len(embudoColors)]
				pdfBarraPct(pdf, pdfStr(row[0]), v, maxEmb, totalEmb, c)
			}
		}
		pdfBarraTotal(pdf, totalEmb)
		pdf.Ln(3)
	}

	// ── Destinos ─────────────────────────────────────────────────────────────
	if len(data.Destinos) > 0 {
		pdfSeccion(pdf, "DESTINOS MAS POPULARES")
		totalD := 0
		for _, r := range data.Destinos {
			if len(r) >= 3 {
				v, _ := strconv.Atoi(r[2])
				totalD += v
			}
		}
		maxD := pdfMaxInt(data.Destinos, 2)
		for _, row := range data.Destinos {
			if len(row) >= 3 {
				v, _ := strconv.Atoi(row[2])
				label := pdfStr(row[0])
				if len(row) > 1 && row[1] != "" {
					label += ", " + pdfStr(row[1])
				}
				pdfBarraPct(pdf, label, v, maxD, totalD, pdfColorDorado)
			}
		}
		pdfBarraTotal(pdf, totalD)
		pdf.Ln(3)
	}

	// ── Cancelaciones ────────────────────────────────────────────────────────
	if len(data.Cancelaciones) > 0 {
		pdfSeccion(pdf, "CANCELACIONES POR TIPO DE RESERVA")
		totalC := 0
		for _, r := range data.Cancelaciones {
			if len(r) >= 2 {
				v, _ := strconv.Atoi(r[1])
				totalC += v
			}
		}
		maxC := pdfMaxInt(data.Cancelaciones, 1)
		for _, row := range data.Cancelaciones {
			if len(row) >= 2 {
				v, _ := strconv.Atoi(row[1])
				pdfBarraPct(pdf, pdfStr(row[0]), v, maxC, totalC, pdfColorRojo)
			}
		}
		pdfBarraTotal(pdf, totalC)
		pdf.Ln(3)
	}

	// ── Proveedores agrupados ─────────────────────────────────────────────────
	if len(data.Proveedores) > 0 {
		pdfSeccion(pdf, "RENDIMIENTO DE PROVEEDORES")
		var totalIng, totalGan float64
		var totalRes int
		currentGrupo := ""
		for _, row := range data.Proveedores {
			// row: [nombre, grupo, tipoProveedor, reservaciones, ingresos, ganancia]
			if len(row) >= 6 {
				cnt, _ := strconv.Atoi(row[3])
				ing, _ := strconv.ParseFloat(row[4], 64)
				gan, _ := strconv.ParseFloat(row[5], 64)
				totalRes += cnt
				totalIng += ing
				totalGan += gan

				grupo := pdfStr(row[1])
				if grupo != currentGrupo {
					currentGrupo = grupo
					// Group header
					pdf.SetFillColor(240, 238, 235)
					pdf.SetTextColor(pdfColorOscuro[0], pdfColorOscuro[1], pdfColorOscuro[2])
					pdf.SetFont("Helvetica", "B", 7)
					pdf.CellFormat(180, 5, "  "+grupo, "", 1, "L", true, 0, "")
					pdf.Ln(1)
				}
				// Row
				pdf.SetFont("Helvetica", "", 7)
				pdf.SetTextColor(50, 50, 50)
				if indexInProveedores(data.Proveedores, row)%2 == 0 {
					pdf.SetFillColor(252, 251, 249)
				} else {
					pdf.SetFillColor(255, 255, 255)
				}
				pdf.CellFormat(62, 4.5, "  "+pdfStr(row[0]), "1", 0, "L", true, 0, "")
				pdf.CellFormat(30, 4.5, pdfStr(row[2]), "1", 0, "L", true, 0, "")
				pdf.CellFormat(18, 4.5, row[3], "1", 0, "C", true, 0, "")
				pdf.CellFormat(35, 4.5, "$ "+row[4], "1", 0, "R", true, 0, "")
				pdf.SetTextColor(pdfColorVerde[0], pdfColorVerde[1], pdfColorVerde[2])
				pdf.SetFont("Helvetica", "B", 7)
				pdf.CellFormat(35, 4.5, "$ "+row[5], "1", 1, "R", true, 0, "")
			}
		}
		// Total row
		pdf.SetFillColor(pdfColorOscuro[0], pdfColorOscuro[1], pdfColorOscuro[2])
		pdf.SetTextColor(pdfColorDorado[0], pdfColorDorado[1], pdfColorDorado[2])
		pdf.SetFont("Helvetica", "B", 7)
		pdf.CellFormat(62, 5, "  TOTAL PERIODO", "1", 0, "L", true, 0, "")
		pdf.CellFormat(30, 5, "", "1", 0, "L", true, 0, "")
		pdf.CellFormat(18, 5, strconv.Itoa(totalRes), "1", 0, "C", true, 0, "")
		pdf.CellFormat(35, 5, fmt.Sprintf("$ %.2f", totalIng), "1", 0, "R", true, 0, "")
		pdf.CellFormat(35, 5, fmt.Sprintf("$ %.2f", totalGan), "1", 1, "R", true, 0, "")
		pdf.Ln(3)
	}

	// ── Tendencia mensual + Comparacion ─────────────────────────────────────
	if len(data.Tendencia) > 0 {
		pdfSeccion(pdf, "TENDENCIA DE INGRESOS MENSUALES")

		// Agrupar por mes para calcular totales
		type mesTotals struct {
			ing float64
			cnt int
		}
		mesTotMap := map[string]*mesTotals{}
		var mesesOrden []string
		for _, row := range data.Tendencia {
			if len(row) >= 4 {
				mes := row[0]
				ing, _ := strconv.ParseFloat(row[2], 64)
				cnt, _ := strconv.Atoi(row[3])
				if _, ok := mesTotMap[mes]; !ok {
					mesTotMap[mes] = &mesTotals{}
					mesesOrden = append(mesesOrden, mes)
				}
				mesTotMap[mes].ing += ing
				mesTotMap[mes].cnt += cnt
			}
		}

		pdfTabla(pdf,
			[]string{"Mes", "Tipo", "Ingresos $", "Cantidad"},
			[]float64{28, 28, 55, 25},
			func() [][]string {
				var rows [][]string
				for _, r := range data.Tendencia {
					rows = append(rows, []string{r[0], pdfStr(r[1]), r[2], r[3]})
				}
				return rows
			}())

		// Totales por mes
		pdf.Ln(3)
		pdf.SetFont("Helvetica", "B", 7)
		pdf.SetTextColor(pdfColorGris[0], pdfColorGris[1], pdfColorGris[2])
		pdf.Cell(60, 4, "Totales por mes:")
		pdf.Ln(-1) // avanza exactamente la altura de la celda (4mm)
		for _, mes := range mesesOrden {
			t := mesTotMap[mes]
			pdf.SetFont("Helvetica", "", 7)
			pdf.SetTextColor(50, 50, 50)
			pdf.CellFormat(28, 4, mes, "", 0, "L", false, 0, "")
			pdf.SetFont("Helvetica", "B", 7)
			pdf.SetTextColor(pdfColorVerde[0], pdfColorVerde[1], pdfColorVerde[2])
			pdf.CellFormat(55, 4, fmt.Sprintf("$ %.2f", t.ing), "", 0, "L", false, 0, "")
			pdf.SetTextColor(pdfColorGris[0], pdfColorGris[1], pdfColorGris[2])
			pdf.SetFont("Helvetica", "", 7)
			pdf.CellFormat(25, 4, fmt.Sprintf("%d reserv.", t.cnt), "", 1, "L", false, 0, "")
		}

		// Comparacion mes actual vs anterior
		if len(mesesOrden) >= 2 {
			mesAct := mesesOrden[len(mesesOrden)-1]
			mesAnt := mesesOrden[len(mesesOrden)-2]
			ingAct := mesTotMap[mesAct].ing
			ingAnt := mesTotMap[mesAnt].ing
			diff := ingAct - ingAnt
			var pct float64
			if ingAnt > 0 {
				pct = diff / ingAnt * 100
			}
			up := diff >= 0

			pdf.Ln(3)
			boxY := pdf.GetY()
			boxColor := [3]int{220, 252, 231}
			textColor := pdfColorVerde
			arrow := "+"
			if !up {
				boxColor = [3]int{254, 226, 226}
				textColor = pdfColorRojo
				arrow = ""
			}
			pdf.SetFillColor(boxColor[0], boxColor[1], boxColor[2])
			pdf.Rect(15, boxY, 180, 18, "F")

			pdf.SetFont("Helvetica", "B", 7)
			pdf.SetTextColor(pdfColorGris[0], pdfColorGris[1], pdfColorGris[2])
			pdf.SetXY(19, boxY+2)
			pdf.Cell(60, 4, "COMPARACION MENSUAL")

			pdf.SetFont("Helvetica", "B", 11)
			pdf.SetTextColor(textColor[0], textColor[1], textColor[2])
			pdf.SetXY(19, boxY+7)
			pctDisplay := fmt.Sprintf("%s%.1f%%", arrow, pct)
			if pct > 9999 {
				pctDisplay = ">9999%"
			}
			if !up && pct < -9999 {
				pctDisplay = "<-9999%"
			}
			pdf.Cell(40, 6, pctDisplay)

			pdf.SetFont("Helvetica", "", 8)
			pdf.SetTextColor(50, 50, 50)
			pdf.SetXY(70, boxY+3)
			pdf.Cell(30, 4, mesAnt+":")
			pdf.SetFont("Helvetica", "B", 8)
			pdf.SetXY(70, boxY+8)
			pdf.Cell(40, 4, fmt.Sprintf("$ %.2f", ingAnt))

			// Flecha dibujada: linea + triangulo relleno
			arrowMidY := boxY + 5.5
			pdf.SetDrawColor(80, 80, 80)
			pdf.SetLineWidth(0.5)
			pdf.Line(115, arrowMidY, 121, arrowMidY)
			pdf.SetFillColor(80, 80, 80)
			pdf.Polygon([]gofpdf.PointType{
				{X: 121, Y: arrowMidY - 1.5},
				{X: 121, Y: arrowMidY + 1.5},
				{X: 124, Y: arrowMidY},
			}, "F")
			pdf.SetLineWidth(0.2)
			pdf.SetXY(125, boxY+3)
			pdf.Cell(30, 4, mesAct+":")
			pdf.SetFont("Helvetica", "B", 8)
			pdf.SetTextColor(textColor[0], textColor[1], textColor[2])
			pdf.SetXY(125, boxY+8)
			pdf.Cell(40, 4, fmt.Sprintf("$ %.2f", ingAct))

			pdf.SetFont("Helvetica", "B", 8)
			diffStr := fmt.Sprintf("%s$ %.2f", arrow, diff)
			if !up {
				diffStr = fmt.Sprintf("$ %.2f", diff)
			}
			pdf.SetXY(165, boxY+5)
			pdf.Cell(25, 8, diffStr)

			pdf.SetY(boxY + 22)
		}
		pdf.Ln(2)
	}

	// ── Heatmap visual ───────────────────────────────────────────────────────
	if len(data.Heatmap) > 0 {
		// Necesita ~62mm: encabezado(8)+descripción(6)+horas(4)+7filas(31.5)+leyenda(10)+margen(2)
		if pdf.GetY()+62 > 279 {
			pdf.AddPage()
		}
		pdfSeccion(pdf, "MAPA DE CALOR - BUSQUEDAS POR HORA Y DIA")
		pdf.SetFont("Helvetica", "I", 7)
		pdf.SetTextColor(pdfColorGris[0], pdfColorGris[1], pdfColorGris[2])
		pdf.MultiCell(180, 4, "Intensidad: cuadro claro = poca demanda, cuadro marron oscuro = alta demanda.", "", "L", false)
		pdf.Ln(1)

		// Build 7x24 matrix
		dias := []string{"Dom", "Lun", "Mar", "Mie", "Jue", "Vie", "Sab"}
		matrix := [7][24]int{}
		maxH := 1
		for _, row := range data.Heatmap {
			if len(row) < 3 {
				continue
			}
			diaStr := row[0]
			hora, _ := strconv.Atoi(row[1])
			val, _ := strconv.Atoi(row[2])
			diaIdx := -1
			for i, d := range dias {
				if d == diaStr {
					diaIdx = i
					break
				}
			}
			if diaIdx >= 0 && hora >= 0 && hora < 24 {
				matrix[diaIdx][hora] = val
				if val > maxH {
					maxH = val
				}
			}
		}

		cellW := 6.5
		cellH := 4.5
		lblW := 9.0
		startXH := 15.0 + lblW // label width
		pageBreakLimit := 279.0 // A4(297) - bottomMargin(18)

		drawHeatmapHours := func() {
			y0 := pdf.GetY()
			pdf.SetFont("Helvetica", "", 5)
			pdf.SetTextColor(pdfColorGris[0], pdfColorGris[1], pdfColorGris[2])
			for h := 0; h < 24; h++ {
				pdf.SetXY(startXH+float64(h)*cellW, y0)
				pdf.CellFormat(cellW, 4, strconv.Itoa(h), "", 0, "C", false, 0, "")
			}
			pdf.SetXY(15, y0+4) // reset cursor below hour row
		}
		drawHeatmapHours()

		// Grid rows — page-break-safe
		for di, dia := range dias {
			// If this row won't fit on the current page, start a new one
			if pdf.GetY()+cellH > pageBreakLimit {
				pdf.AddPage()
				drawHeatmapHours() // redraw hour numbers on the new page
			}
			rowY := pdf.GetY()
			// Day label
			pdf.SetFont("Helvetica", "B", 6)
			pdf.SetTextColor(pdfColorGris[0], pdfColorGris[1], pdfColorGris[2])
			pdf.SetXY(15, rowY)
			pdf.CellFormat(lblW, cellH, dia, "", 0, "R", false, 0, "")
			// Cells
			for h := 0; h < 24; h++ {
				val := matrix[di][h]
				ratio := 0.0
				if maxH > 0 {
					ratio = float64(val) / float64(maxH)
				}
				// Color: from very light gold to dark brown
				r := int(212 - ratio*80)
				g := int(175 - ratio*120)
				b := int(55 - ratio*40)
				if val == 0 {
					r, g, b = 240, 238, 234
				}
				cx := startXH + float64(h)*cellW
				pdf.SetFillColor(r, g, b)
				pdf.Rect(cx, rowY, cellW-0.5, cellH-0.5, "F")
				// Number in cell if > 0 — use SetXY only after safety check
				if val > 0 {
					if val > 99 {
						pdf.SetFont("Helvetica", "B", 4)
					} else {
						pdf.SetFont("Helvetica", "B", 5)
					}
					textR, textG, textB := 28, 26, 24
					if ratio > 0.6 {
						textR, textG, textB = 255, 255, 255
					}
					pdf.SetTextColor(textR, textG, textB)
					// SetXY+CellFormat without auto-line-break (ln=0) is safe once
					// rowY is guaranteed to be inside the page (checked above).
					pdf.SetXY(cx, rowY+0.5)
					pdf.CellFormat(cellW-0.5, cellH-0.5, strconv.Itoa(val), "", 0, "C", false, 0, "")
				}
			}
			// Advance to next row manually (avoids gofpdf cursor confusion)
			pdf.SetXY(15, rowY+cellH)
		}

		// Legend
		pdf.Ln(2)
		pdf.SetFont("Helvetica", "", 6)
		pdf.SetTextColor(pdfColorGris[0], pdfColorGris[1], pdfColorGris[2])
		pdf.SetX(15)
		pdf.Cell(20, 4, "Leyenda: ")
		legendColors := [][3]int{{240, 238, 234}, {224, 195, 100}, {175, 130, 40}, {130, 85, 15}}
		legendLabels := []string{"Sin datos", "Poca demanda", "Demanda media", "Alta demanda"}
		for i, lc := range legendColors {
			pdf.SetFillColor(lc[0], lc[1], lc[2])
			x := pdf.GetX()
			y := pdf.GetY() + 0.5
			pdf.Rect(x, y, 4, 3, "F")
			pdf.SetX(x + 5)
			pdf.SetTextColor(pdfColorGris[0], pdfColorGris[1], pdfColorGris[2])
			pdf.CellFormat(22, 4, legendLabels[i], "", 0, "L", false, 0, "")
		}
		pdf.Ln(5)
	}

	// ── Registro de búsquedas ─────────────────────────────────────────────────
	if len(data.Listado) > 0 {
		pdfSeccion(pdf, "REGISTRO DE BUSQUEDAS (ultimas 30)")
		rows := data.Listado
		if len(rows) > 30 {
			rows = rows[:30]
		}
		var reducida [][]string
		for _, r := range rows {
			orig := "Sin origen"
			dest := ""
			if len(r) > 4 && strings.TrimSpace(r[4]) != "" {
				orig = pdfStr(r[4])
			}
			if len(r) > 5 {
				dest = pdfStr(r[5])
			}
			usuario := ""
			if len(r) > 3 {
				usuario = pdfStr(r[3])
			}
			fecha := r[1]
			if len(fecha) > 10 {
				fecha = fecha[:10]
			}
			reducida = append(reducida, []string{r[0], fecha, pdfStr(r[2]), usuario, orig, dest})
		}
		pdfTabla(pdf,
			[]string{"ID", "Fecha", "Tipo", "Usuario", "Origen", "Destino"},
			[]float64{10, 22, 14, 42, 38, 54},
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
	pdf.CellFormat(180, 6, "  "+titulo, "", 1, "L", true, 0, "")
	pdf.SetTextColor(50, 50, 50)
	pdf.SetFont("Helvetica", "", 9)
	pdf.Ln(2)
}

// pdfBarraPct dibuja: label | barra de color | valor | porcentaje
func pdfBarraPct(pdf *gofpdf.Fpdf, label string, val, maxVal, total int, color [3]int) {
	barMax := 90.0
	ratio := 0.0
	if maxVal > 0 {
		ratio = float64(val) / float64(maxVal)
	}
	barW := ratio * barMax
	if barW < 1 {
		barW = 1
	}

	pct := 0.0
	if total > 0 {
		pct = float64(val) / float64(total) * 100
	}

	pdf.SetFont("Helvetica", "", 8)
	pdf.SetTextColor(pdfColorGris[0], pdfColorGris[1], pdfColorGris[2])
	pdf.CellFormat(50, 5, label, "", 0, "L", false, 0, "")

	pdf.SetFillColor(color[0], color[1], color[2])
	x := pdf.GetX()
	y := pdf.GetY() + 0.8
	pdf.Rect(x, y, barW, 3.5, "F")

	pdf.SetX(x + barMax + 2)
	pdf.SetFont("Helvetica", "B", 8)
	pdf.SetTextColor(50, 50, 50)
	pdf.CellFormat(16, 5, strconv.Itoa(val), "", 0, "R", false, 0, "")
	pdf.SetFont("Helvetica", "", 7)
	pdf.SetTextColor(pdfColorGris[0], pdfColorGris[1], pdfColorGris[2])
	pdf.CellFormat(18, 5, fmt.Sprintf("%.1f%%", pct), "", 1, "R", false, 0, "")
}

// pdfBarraPctExtra igual que pdfBarraPct pero con campo extra (ej: ingresos)
func pdfBarraPctExtra(pdf *gofpdf.Fpdf, label string, val, maxVal, total int, extra string, color [3]int) {
	barMax := 70.0
	ratio := 0.0
	if maxVal > 0 {
		ratio = float64(val) / float64(maxVal)
	}
	barW := ratio * barMax
	if barW < 1 {
		barW = 1
	}
	pct := 0.0
	if total > 0 {
		pct = float64(val) / float64(total) * 100
	}

	pdf.SetFont("Helvetica", "", 8)
	pdf.SetTextColor(pdfColorGris[0], pdfColorGris[1], pdfColorGris[2])
	pdf.CellFormat(40, 5, label, "", 0, "L", false, 0, "")

	pdf.SetFillColor(color[0], color[1], color[2])
	x := pdf.GetX()
	y := pdf.GetY() + 0.8
	pdf.Rect(x, y, barW, 3.5, "F")

	pdf.SetX(x + barMax + 2)
	pdf.SetFont("Helvetica", "B", 8)
	pdf.SetTextColor(50, 50, 50)
	pdf.CellFormat(14, 5, strconv.Itoa(val), "", 0, "R", false, 0, "")
	pdf.SetFont("Helvetica", "", 7)
	pdf.SetTextColor(pdfColorGris[0], pdfColorGris[1], pdfColorGris[2])
	pdf.CellFormat(16, 5, fmt.Sprintf("%.1f%%", pct), "", 0, "R", false, 0, "")
	pdf.SetFont("Helvetica", "", 7)
	pdf.SetTextColor(pdfColorVerde[0], pdfColorVerde[1], pdfColorVerde[2])
	pdf.CellFormat(38, 5, extra, "", 1, "R", false, 0, "")
}

// pdfBarraTotal dibuja la fila de Total (barra completa, negrita, 100%)
func pdfBarraTotal(pdf *gofpdf.Fpdf, total int) {
	pdf.SetFont("Helvetica", "B", 8)
	pdf.SetTextColor(pdfColorOscuro[0], pdfColorOscuro[1], pdfColorOscuro[2])
	pdf.CellFormat(50, 5, "Total", "", 0, "L", false, 0, "")
	pdf.SetFillColor(pdfColorOscuro[0], pdfColorOscuro[1], pdfColorOscuro[2])
	x := pdf.GetX()
	y := pdf.GetY() + 0.8
	pdf.Rect(x, y, 90, 3.5, "F")
	pdf.SetX(x + 92)
	pdf.SetTextColor(pdfColorOscuro[0], pdfColorOscuro[1], pdfColorOscuro[2])
	pdf.CellFormat(16, 5, strconv.Itoa(total), "", 0, "R", false, 0, "")
	pdf.SetTextColor(pdfColorGris[0], pdfColorGris[1], pdfColorGris[2])
	pdf.SetFont("Helvetica", "", 7)
	pdf.CellFormat(18, 5, "100.0%", "", 1, "R", false, 0, "")
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

// indexInProveedores devuelve el índice de una fila en el slice para alternar color.
func indexInProveedores(all [][]string, target []string) int {
	for i, r := range all {
		if len(r) > 0 && len(target) > 0 && r[0] == target[0] {
			return i
		}
	}
	return 0
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
	// Tasa de conversion busquedas -> reservaciones
	convRate := 0.0
	if data.KPITotalBusquedas > 0 {
		convRate = float64(data.KPIReservasPagadas) / float64(data.KPITotalBusquedas) * 100
	}

	// Top 3 destinos
	destinosHTML := ""
	for i, d := range data.Destinos {
		if i >= 3 || len(d) < 3 {
			break
		}
		bg := "#fff"
		if i%2 == 1 {
			bg = "#faf8f5"
		}
		destinosHTML += fmt.Sprintf(
			`<tr style="background:%s;"><td style="padding:6px 16px;font-size:11px;color:#5a5047;border-bottom:1px solid #f0ebe3;">%d. %s, %s</td><td style="padding:6px 16px;font-size:11px;font-weight:bold;color:#1C1A18;border-bottom:1px solid #f0ebe3;text-align:right;">%s busq.</td></tr>`,
			bg, i+1, d[0], d[1], d[2],
		)
	}

	// Comparacion mensual desde tendencia
	compHTML := ""
	type mT struct{ ing float64 }
	mMap := map[string]*mT{}
	var mOrd []string
	for _, r := range data.Tendencia {
		if len(r) >= 3 {
			mes := r[0]
			ing, _ := strconv.ParseFloat(r[2], 64)
			if _, ok := mMap[mes]; !ok {
				mMap[mes] = &mT{}
				mOrd = append(mOrd, mes)
			}
			mMap[mes].ing += ing
		}
	}
	if len(mOrd) >= 2 {
		mesAct := mOrd[len(mOrd)-1]
		mesAnt := mOrd[len(mOrd)-2]
		ingAct := mMap[mesAct].ing
		ingAnt := mMap[mesAnt].ing
		diff := ingAct - ingAnt
		pct := 0.0
		if ingAnt > 0 {
			pct = diff / ingAnt * 100
		}
		color := "#059669"
		arrow := "&#9650;"
		sign := "+"
		if diff < 0 {
			color = "#DC2626"
			arrow = "&#9660;"
			sign = ""
		}
		pctStr := fmt.Sprintf("%.1f%%", pct)
		if pct > 9999 {
			pctStr = ">9999%"
		}
		compHTML = fmt.Sprintf(`
    <tr><td colspan="2" style="padding:0 16px 12px;">
      <div style="background:#f0fdf4;border:1px solid #bbf7d0;border-radius:8px;padding:12px 16px;display:flex;align-items:center;gap:16px;">
        <div style="font-size:20px;font-weight:900;color:%s;">%s %s</div>
        <div>
          <div style="font-size:10px;color:#9a9089;font-weight:700;text-transform:uppercase;letter-spacing:0.5px;">Comparacion mensual</div>
          <div style="font-size:11px;color:#5a5047;margin-top:2px;">%s: <b>$%.2f</b> &rarr; %s: <b style="color:%s;">$%.2f</b> &nbsp; <b style="color:%s;">%s$%.2f</b></div>
        </div>
      </div>
    </td></tr>`,
			color, arrow, pctStr,
			mesAnt, ingAnt, mesAct, color, ingAct,
			color, sign, diff,
		)
	}

	return fmt.Sprintf(`<!DOCTYPE html>
<html lang="es">
<head><meta charset="UTF-8"><title>MOVENT Metricas</title></head>
<body style="margin:0;padding:0;background:#F5F2EC;font-family:Helvetica,Arial,sans-serif;">
<table width="100%%" cellpadding="0" cellspacing="0">
<tr><td align="center" style="padding:32px 12px;">
<table width="600" cellpadding="0" cellspacing="0"
  style="max-width:600px;border-radius:14px;overflow:hidden;box-shadow:0 8px 32px rgba(28,26,24,0.14);border:1px solid #ddd6cc;">

  <!-- Header -->
  <tr><td style="background:#1C1A18;padding:28px 28px 20px;">
    <div style="font-size:28px;font-weight:900;color:#FFCC00;letter-spacing:5px;">MOVENT</div>
    <div style="font-size:11px;color:#9a9089;margin-top:4px;letter-spacing:1px;">AGENCIA DE VIAJES &middot; REPORTE DE METRICAS</div>
  </td></tr>
  <tr><td style="background:linear-gradient(90deg,#D4AF37,#FFCC00);height:3px;"></td></tr>

  <!-- Intro -->
  <tr><td style="background:#fff;padding:24px 28px 16px;">
    <p style="font-size:16px;font-weight:700;color:#1C1A18;margin:0 0 8px;">Reporte listo para revisar</p>
    <p style="font-size:13px;color:#5a5047;line-height:1.7;margin:0;">
      Adjunto encontraras el reporte completo de metricas del periodo
      <strong style="color:#1C1A18;">%s</strong> al <strong style="color:#1C1A18;">%s</strong>.
      A continuacion un resumen ejecutivo.
    </p>
  </td></tr>

  <!-- KPI Grid -->
  <tr><td style="background:#fff;padding:0 28px 20px;">
    <table width="100%%" cellpadding="0" cellspacing="0">
      <tr>
        <td style="padding:4px;">
          <div style="background:#FFFBEA;border:1px solid #D4AF37;border-radius:10px;padding:12px 14px;">
            <div style="font-size:9px;color:#92701F;font-weight:700;letter-spacing:0.5px;text-transform:uppercase;">Total Busquedas</div>
            <div style="font-size:22px;font-weight:900;color:#1C1A18;margin-top:4px;">%d</div>
          </div>
        </td>
        <td style="padding:4px;">
          <div style="background:#F0FDF4;border:1px solid #86EFAC;border-radius:10px;padding:12px 14px;">
            <div style="font-size:9px;color:#166534;font-weight:700;letter-spacing:0.5px;text-transform:uppercase;">Ingresos Totales</div>
            <div style="font-size:22px;font-weight:900;color:#1C1A18;margin-top:4px;">$%.2f</div>
          </div>
        </td>
        <td style="padding:4px;">
          <div style="background:#FFFBEA;border:1px solid #D4AF37;border-radius:10px;padding:12px 14px;">
            <div style="font-size:9px;color:#92701F;font-weight:700;letter-spacing:0.5px;text-transform:uppercase;">Ganancia MOVENT</div>
            <div style="font-size:22px;font-weight:900;color:#D4AF37;margin-top:4px;">$%.2f</div>
          </div>
        </td>
      </tr>
      <tr>
        <td style="padding:4px;">
          <div style="background:#EFF6FF;border:1px solid #93C5FD;border-radius:10px;padding:12px 14px;">
            <div style="font-size:9px;color:#1E40AF;font-weight:700;letter-spacing:0.5px;text-transform:uppercase;">Reservaciones</div>
            <div style="font-size:22px;font-weight:900;color:#1C1A18;margin-top:4px;">%d</div>
          </div>
        </td>
        <td style="padding:4px;">
          <div style="background:#F0FDF4;border:1px solid #86EFAC;border-radius:10px;padding:12px 14px;">
            <div style="font-size:9px;color:#166534;font-weight:700;letter-spacing:0.5px;text-transform:uppercase;">Confirmadas/Activas</div>
            <div style="font-size:22px;font-weight:900;color:#059669;margin-top:4px;">%d</div>
          </div>
        </td>
        <td style="padding:4px;">
          <div style="background:#EFF6FF;border:1px solid #93C5FD;border-radius:10px;padding:12px 14px;">
            <div style="font-size:9px;color:#1E40AF;font-weight:700;letter-spacing:0.5px;text-transform:uppercase;">Tasa Conversion</div>
            <div style="font-size:22px;font-weight:900;color:#3B82F6;margin-top:4px;">%.1f%%</div>
          </div>
        </td>
      </tr>
    </table>
  </td></tr>

  <!-- Comparacion mensual -->
  <tr><td style="background:#fff;padding:0 28px 8px;">
    <table width="100%%" cellpadding="0" cellspacing="0">
      %s
    </table>
  </td></tr>

  <!-- Top destinos -->
  %s

  <!-- Footer -->
  <tr><td style="background:#1C1A18;padding:16px 28px;text-align:center;">
    <div style="font-size:10px;color:#6b6358;">MOVENT &middot; Panel Administrativo &middot; Correo automatico</div>
    <div style="font-size:9px;color:#4a4540;margin-top:3px;">No responder este mensaje &middot; Guatemala City, Guatemala</div>
  </td></tr>
</table>
</td></tr>
</table>
</body></html>`,
		data.Desde, data.Hasta,
		data.KPITotalBusquedas,
		data.KPIIngresos, data.KPIGanancia,
		data.KPITotalReservaciones, data.KPIReservasPagadas, convRate,
		compHTML,
		func() string {
			if len(destinosHTML) == 0 {
				return ""
			}
			return fmt.Sprintf(`
  <tr><td style="background:#fff;padding:0 28px 20px;">
    <p style="font-size:11px;font-weight:700;color:#9a9089;text-transform:uppercase;letter-spacing:0.5px;margin:0 0 8px;">Top Destinos</p>
    <table width="100%%" cellpadding="0" cellspacing="0" style="border-radius:8px;overflow:hidden;border:1px solid #f0ebe3;">
      %s
    </table>
  </td></tr>`, destinosHTML)
		}(),
	)
}
