// # Package controllers
//
// MetricasController expone los endpoints del panel de métricas de MOVENT.
// Proporciona KPIs, gráficas, listados paginados y exportaciones en múltiples
// formatos (Excel, CSV, PDF) tanto para descarga directa como envío por correo.
package controllers

import (
	"agencia-viajes/internal/helpers"
	"agencia-viajes/internal/services"
	"context"
	"database/sql"
	"fmt"
	"net/http"
	"strconv"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
)

// MetricasController gestiona todos los endpoints de métricas del panel admin.
type MetricasController struct {
	db        *sql.DB
	logSesion *services.LogSesionService
}

// NewMetricasController retorna una nueva instancia de MetricasController.
func NewMetricasController(db *sql.DB, logSesion *services.LogSesionService) *MetricasController {
	return &MetricasController{db: db, logSesion: logSesion}
}

// ── helpers de fecha ─────────────────────────────────────────────────────────

func metParseFechas(c *gin.Context) (desde, hasta time.Time) {
	now := time.Now()
	hasta = time.Date(now.Year(), now.Month(), now.Day(), 23, 59, 59, 0, time.UTC)
	desde = time.Date(now.Year(), now.Month(), now.Day()-29, 0, 0, 0, 0, time.UTC)
	if d := c.Query("fechaDesde"); d != "" {
		if t, err := time.Parse("2006-01-02", d); err == nil {
			desde = time.Date(t.Year(), t.Month(), t.Day(), 0, 0, 0, 0, time.UTC)
		}
	}
	if h := c.Query("fechaHasta"); h != "" {
		if t, err := time.Parse("2006-01-02", h); err == nil {
			hasta = time.Date(t.Year(), t.Month(), t.Day(), 23, 59, 59, 0, time.UTC)
		}
	}
	return
}

func metParseFechasBody(fechaDesde, fechaHasta string) (desde, hasta time.Time) {
	now := time.Now()
	hasta = time.Date(now.Year(), now.Month(), now.Day(), 23, 59, 59, 0, time.UTC)
	desde = time.Date(now.Year(), now.Month(), now.Day()-29, 0, 0, 0, 0, time.UTC)
	if fechaDesde != "" {
		if t, err := time.Parse("2006-01-02", fechaDesde); err == nil {
			desde = time.Date(t.Year(), t.Month(), t.Day(), 0, 0, 0, 0, time.UTC)
		}
	}
	if fechaHasta != "" {
		if t, err := time.Parse("2006-01-02", fechaHasta); err == nil {
			hasta = time.Date(t.Year(), t.Month(), t.Day(), 23, 59, 59, 0, time.UTC)
		}
	}
	return
}

func metTipoNombre(id int) string {
	switch id {
	case 1:
		return "Vuelo"
	case 2:
		return "Hotel"
	default:
		return "Paquete"
	}
}

// ── ObtenerResumen ───────────────────────────────────────────────────────────

// ObtenerResumen retorna los KPIs generales, búsquedas por día, distribución
// por tipo y destinos más populares para el período indicado.
//
// GET /api/admin/metricas/resumen?fechaDesde=YYYY-MM-DD&fechaHasta=YYYY-MM-DD
func (ctrl *MetricasController) ObtenerResumen(c *gin.Context) {
	desde, hasta := metParseFechas(c)
	d := desde.Format("2006-01-02 15:04:05")
	h := hasta.Format("2006-01-02 15:04:05")

	conn, err := ctrl.db.Conn(context.Background())
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "error de conexión"})
		return
	}
	defer conn.Close()

	// ── KPIs ────────────────────────────────────────────────────────────
	type KPI struct {
		TotalBusquedas     int     `json:"totalBusquedas"`
		BusquedasVuelo     int     `json:"busquedasVuelo"`
		BusquedasHotel     int     `json:"busquedasHotel"`
		TotalReservaciones int     `json:"totalReservaciones"`
		ReservasPagadas    int     `json:"reservasPagadas"`
		IngresosTotales    float64 `json:"ingresosTotales"`
		GananciaMovent     float64 `json:"gananciaMovent"`
		TicketPromedio     float64 `json:"ticketPromedio"`
		UsuariosActivos    int     `json:"usuariosActivos"`
	}
	var kpi KPI

	rows, _ := conn.QueryContext(context.Background(), `
		SELECT Tipo_Busqueda_ID, COUNT(*) FROM busqueda
		WHERE Fecha_de_Busqueda BETWEEN ? AND ? GROUP BY Tipo_Busqueda_ID
	`, d, h)
	if rows != nil {
		for rows.Next() {
			var tid, total int
			rows.Scan(&tid, &total)
			kpi.TotalBusquedas += total
			if tid == 1 {
				kpi.BusquedasVuelo = total
			} else {
				kpi.BusquedasHotel += total
			}
		}
		rows.Close()
	}

	conn.QueryRowContext(context.Background(), `
		SELECT COUNT(*), SUM(CASE WHEN EstadoID IN (2,5,6) THEN 1 ELSE 0 END)
		FROM Reservacion WHERE Fecha_Creacion BETWEEN ? AND ?
	`, d, h).Scan(&kpi.TotalReservaciones, &kpi.ReservasPagadas)

	var base float64
	conn.QueryRowContext(context.Background(), `
		SELECT COALESCE(SUM(dt.Total),0), COALESCE(SUM(dt.Total/(1+p.Porcentaje_Ganancia/100)),0)
		FROM Reservacion r
		JOIN detalles_reservacion dt ON dt.Reservacion_ID=r.ID
		JOIN Proveedor p ON dt.Proveedor_ID=p.ID
		WHERE r.EstadoID IN (2,5,6) AND r.Fecha_Creacion BETWEEN ? AND ?
	`, d, h).Scan(&kpi.IngresosTotales, &base)
	kpi.GananciaMovent = kpi.IngresosTotales - base

	conn.QueryRowContext(context.Background(), `
		SELECT COALESCE(AVG(Total),0) FROM Reservacion
		WHERE EstadoID IN (2,5,6) AND Fecha_Creacion BETWEEN ? AND ?
	`, d, h).Scan(&kpi.TicketPromedio)

	conn.QueryRowContext(context.Background(), `
		SELECT COUNT(DISTINCT UsuarioID) FROM busqueda
		WHERE Fecha_de_Busqueda BETWEEN ? AND ? AND UsuarioID IS NOT NULL
	`, d, h).Scan(&kpi.UsuariosActivos)

	// ── Búsquedas por día ────────────────────────────────────────────────
	type BusquedaDia struct {
		Fecha string `json:"fecha"`
		Total int    `json:"total"`
	}
	var busquedasDia []BusquedaDia
	rows, _ = conn.QueryContext(context.Background(), `
		SELECT DATE(Fecha_de_Busqueda), COUNT(*) FROM busqueda
		WHERE Fecha_de_Busqueda BETWEEN ? AND ?
		GROUP BY DATE(Fecha_de_Busqueda) ORDER BY 1 ASC
	`, d, h)
	if rows != nil {
		for rows.Next() {
			var b BusquedaDia
			rows.Scan(&b.Fecha, &b.Total)
			busquedasDia = append(busquedasDia, b)
		}
		rows.Close()
	}
	if busquedasDia == nil {
		busquedasDia = []BusquedaDia{}
	}

	// ── Búsquedas por tipo ───────────────────────────────────────────────
	type BusquedaTipo struct {
		Tipo  string `json:"tipo"`
		Total int    `json:"total"`
	}
	var busquedasTipo []BusquedaTipo
	rows, _ = conn.QueryContext(context.Background(), `
		SELECT CASE Tipo_Busqueda_ID WHEN 1 THEN 'Vuelo' ELSE 'Hotel' END, COUNT(*)
		FROM busqueda WHERE Fecha_de_Busqueda BETWEEN ? AND ? GROUP BY Tipo_Busqueda_ID ORDER BY 2 DESC
	`, d, h)
	if rows != nil {
		for rows.Next() {
			var b BusquedaTipo
			rows.Scan(&b.Tipo, &b.Total)
			busquedasTipo = append(busquedasTipo, b)
		}
		rows.Close()
	}
	if busquedasTipo == nil {
		busquedasTipo = []BusquedaTipo{}
	}

	// ── Destinos más populares ───────────────────────────────────────────
	type Destino struct {
		Ciudad string `json:"ciudad"`
		Pais   string `json:"pais"`
		Total  int    `json:"total"`
	}
	var destinos []Destino
	rows, _ = conn.QueryContext(context.Background(), `
		SELECT c.Nombre, p.Nombre, COUNT(*) FROM busqueda b
		JOIN ciudad c ON b.CiudadDestinoID=c.ID JOIN pais p ON c.PaisID=p.ID
		WHERE b.Fecha_de_Busqueda BETWEEN ? AND ?
		GROUP BY c.ID, c.Nombre, p.Nombre ORDER BY 3 DESC LIMIT 8
	`, d, h)
	if rows != nil {
		for rows.Next() {
			var dest Destino
			rows.Scan(&dest.Ciudad, &dest.Pais, &dest.Total)
			destinos = append(destinos, dest)
		}
		rows.Close()
	}
	if destinos == nil {
		destinos = []Destino{}
	}

	// ── Reservaciones activas por tipo ───────────────────────────────────
	type ResTipo struct {
		Tipo     string  `json:"tipo"`
		TipoID   int     `json:"tipoId"`
		Total    int     `json:"total"`
		Ingresos float64 `json:"ingresos"`
	}
	var resTipos []ResTipo
	rows, _ = conn.QueryContext(context.Background(), `
		SELECT r.Tipo_Reserva_ID, COUNT(DISTINCT r.ID), COALESCE(SUM(dt.Total),0)
		FROM Reservacion r LEFT JOIN detalles_reservacion dt ON dt.Reservacion_ID=r.ID
		WHERE r.EstadoID IN (2,5,6) AND r.Fecha_Creacion BETWEEN ? AND ?
		GROUP BY r.Tipo_Reserva_ID ORDER BY 2 DESC
	`, d, h)
	if rows != nil {
		for rows.Next() {
			var rt ResTipo
			rows.Scan(&rt.TipoID, &rt.Total, &rt.Ingresos)
			rt.Tipo = metTipoNombre(rt.TipoID)
			resTipos = append(resTipos, rt)
		}
		rows.Close()
	}
	if resTipos == nil {
		resTipos = []ResTipo{}
	}

	c.JSON(http.StatusOK, gin.H{
		"kpi":                  kpi,
		"busquedasPorDia":      busquedasDia,
		"busquedasPorTipo":     busquedasTipo,
		"destinosPopulares":    destinos,
		"reservacionesPorTipo": resTipos,
		"periodo":              gin.H{"desde": desde.Format("2006-01-02"), "hasta": hasta.Format("2006-01-02")},
	})
}

// ── ObtenerNegocio ───────────────────────────────────────────────────────────

// ObtenerNegocio retorna el embudo de conversión, rendimiento de proveedores,
// cancelaciones, tendencia mensual y heatmap de búsquedas.
//
// GET /api/admin/metricas/negocio?fechaDesde=YYYY-MM-DD&fechaHasta=YYYY-MM-DD
func (ctrl *MetricasController) ObtenerNegocio(c *gin.Context) {
	desde, hasta := metParseFechas(c)
	d := desde.Format("2006-01-02 15:04:05")
	h := hasta.Format("2006-01-02 15:04:05")

	conn, err := ctrl.db.Conn(context.Background())
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "error de conexión"})
		return
	}
	defer conn.Close()

	// ── Embudo ───────────────────────────────────────────────────────────
	type Embudo struct {
		Busquedas     int `json:"busquedas"`
		Reservaciones int `json:"reservaciones"`
		Activas       int `json:"activas"`
		Completadas   int `json:"completadas"`
		Canceladas    int `json:"canceladas"`
		Expiradas     int `json:"expiradas"`
		Pendientes    int `json:"pendientes"`
	}
	var embudo Embudo
	conn.QueryRowContext(context.Background(), `
		SELECT
		  (SELECT COUNT(*) FROM busqueda WHERE Fecha_de_Busqueda BETWEEN ? AND ?),
		  (SELECT COUNT(*) FROM Reservacion WHERE Fecha_Creacion BETWEEN ? AND ?),
		  (SELECT COUNT(*) FROM Reservacion WHERE EstadoID IN (2,5,6) AND Fecha_Creacion BETWEEN ? AND ?),
		  (SELECT COUNT(*) FROM Reservacion WHERE EstadoID=5 AND Fecha_Creacion BETWEEN ? AND ?),
		  (SELECT COUNT(*) FROM Reservacion WHERE EstadoID=3 AND Fecha_Creacion BETWEEN ? AND ?),
		  (SELECT COUNT(*) FROM Reservacion WHERE EstadoID=4 AND Fecha_Creacion BETWEEN ? AND ?),
		  (SELECT COUNT(*) FROM Reservacion WHERE EstadoID=1 AND Fecha_Creacion BETWEEN ? AND ?)
	`, d, h, d, h, d, h, d, h, d, h, d, h, d, h,
	).Scan(&embudo.Busquedas, &embudo.Reservaciones, &embudo.Activas,
		&embudo.Completadas, &embudo.Canceladas, &embudo.Expiradas, &embudo.Pendientes)

	// ── Proveedores ──────────────────────────────────────────────────────
	type ProveedorRend struct {
		Nombre        string  `json:"nombre"`
		Tipo          string  `json:"tipo"`
		TipoID        int     `json:"tipoId"`
		TipoReservaID int     `json:"tipoReservaId"`
		Reservaciones int     `json:"reservaciones"`
		Ingresos      float64 `json:"ingresos"`
		Ganancia      float64 `json:"ganancia"`
	}
	var proveedores []ProveedorRend
	rows, _ := conn.QueryContext(context.Background(), `
		SELECT p.Nombre, p.Tipo_Proveedor_ID, r.Tipo_Reserva_ID,
		       COUNT(DISTINCT dt.Reservacion_ID),
		       COALESCE(SUM(dt.Total),0),
		       COALESCE(SUM(dt.Total - dt.Total/(1+p.Porcentaje_Ganancia/100)),0)
		FROM detalles_reservacion dt JOIN Proveedor p ON dt.Proveedor_ID=p.ID
		JOIN Reservacion r ON r.ID=dt.Reservacion_ID
		WHERE r.EstadoID IN (2,5,6) AND r.Fecha_Creacion BETWEEN ? AND ?
		GROUP BY p.ID, p.Nombre, p.Tipo_Proveedor_ID, r.Tipo_Reserva_ID
		ORDER BY p.Tipo_Proveedor_ID, 5 DESC
	`, d, h)
	if rows != nil {
		for rows.Next() {
			var p ProveedorRend
			rows.Scan(&p.Nombre, &p.TipoID, &p.TipoReservaID, &p.Reservaciones, &p.Ingresos, &p.Ganancia)
			if p.TipoID == 1 {
				p.Tipo = "Aerolínea"
			} else {
				p.Tipo = "Hotel"
			}
			proveedores = append(proveedores, p)
		}
		rows.Close()
	}
	if proveedores == nil {
		proveedores = []ProveedorRend{}
	}

	// ── Cancelaciones por tipo ───────────────────────────────────────────
	type CancelTipo struct {
		Tipo  string `json:"tipo"`
		Total int    `json:"total"`
	}
	var cancelaciones []CancelTipo
	rows, _ = conn.QueryContext(context.Background(), `
		SELECT Tipo_Reserva_ID, COUNT(*) FROM Reservacion
		WHERE EstadoID=3 AND Fecha_Creacion BETWEEN ? AND ? GROUP BY Tipo_Reserva_ID ORDER BY 2 DESC
	`, d, h)
	if rows != nil {
		for rows.Next() {
			var ct CancelTipo
			var tid int
			rows.Scan(&tid, &ct.Total)
			ct.Tipo = metTipoNombre(tid)
			cancelaciones = append(cancelaciones, ct)
		}
		rows.Close()
	}
	if cancelaciones == nil {
		cancelaciones = []CancelTipo{}
	}

	// ── Tendencia mensual ─────────────────────────────────────────────────
	type IngresoMensual struct {
		Mes        string  `json:"mes"`
		TipoNombre string  `json:"tipoNombre"`
		Ingresos   float64 `json:"ingresos"`
		Cantidad   int     `json:"cantidad"`
	}
	var tendencia []IngresoMensual
	rows, _ = conn.QueryContext(context.Background(), `
		SELECT DATE_FORMAT(r.Fecha_Creacion,'%Y-%m'), r.Tipo_Reserva_ID,
		       COALESCE(SUM(dt.Total),0), COUNT(DISTINCT r.ID)
		FROM Reservacion r JOIN detalles_reservacion dt ON dt.Reservacion_ID=r.ID
		WHERE r.EstadoID IN (2,5,6) AND r.Fecha_Creacion BETWEEN ? AND ?
		GROUP BY 1,2 ORDER BY 1 ASC
	`, d, h)
	if rows != nil {
		for rows.Next() {
			var im IngresoMensual
			var tid int
			rows.Scan(&im.Mes, &tid, &im.Ingresos, &im.Cantidad)
			im.TipoNombre = metTipoNombre(tid)
			tendencia = append(tendencia, im)
		}
		rows.Close()
	}
	if tendencia == nil {
		tendencia = []IngresoMensual{}
	}

	// ── Heatmap ──────────────────────────────────────────────────────────
	type HeatCell struct {
		DiaSemana int `json:"diaSemana"`
		Hora      int `json:"hora"`
		Total     int `json:"total"`
	}
	var heatmap []HeatCell
	rows, _ = conn.QueryContext(context.Background(), `
		SELECT DAYOFWEEK(Fecha_de_Busqueda), HOUR(Fecha_de_Busqueda), COUNT(*)
		FROM busqueda WHERE Fecha_de_Busqueda BETWEEN ? AND ?
		GROUP BY 1,2 ORDER BY 1,2
	`, d, h)
	if rows != nil {
		for rows.Next() {
			var hc HeatCell
			rows.Scan(&hc.DiaSemana, &hc.Hora, &hc.Total)
			heatmap = append(heatmap, hc)
		}
		rows.Close()
	}
	if heatmap == nil {
		heatmap = []HeatCell{}
	}

	c.JSON(http.StatusOK, gin.H{
		"embudo":        embudo,
		"proveedores":   proveedores,
		"cancelaciones": cancelaciones,
		"tendencia":     tendencia,
		"heatmap":       heatmap,
		"periodo":       gin.H{"desde": desde.Format("2006-01-02"), "hasta": hasta.Format("2006-01-02")},
	})
}

// ── ListadoBusquedas ─────────────────────────────────────────────────────────

// ListadoBusquedas retorna el listado paginado de búsquedas con filtros.
//
// POST /api/admin/metricas/listado
func (ctrl *MetricasController) ListadoBusquedas(c *gin.Context) {
	var req struct {
		FechaDesde   string `json:"fechaDesde"`
		FechaHasta   string `json:"fechaHasta"`
		Tipo         string `json:"tipo"`
		Usuario      string `json:"usuario"`
		Pagina       int    `json:"pagina"`
		TamanoPagina int    `json:"tamanoPagina"`
	}
	c.ShouldBindJSON(&req)
	if req.Pagina < 1 {
		req.Pagina = 1
	}
	if req.TamanoPagina < 1 || req.TamanoPagina > 100 {
		req.TamanoPagina = 25
	}

	desde, hasta := metParseFechasBody(req.FechaDesde, req.FechaHasta)
	d := desde.Format("2006-01-02 15:04:05")
	h := hasta.Format("2006-01-02 15:04:05")

	conn, err := ctrl.db.Conn(context.Background())
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "error de conexión"})
		return
	}
	defer conn.Close()

	var conds []string
	var args []interface{}
	conds = append(conds, "b.Fecha_de_Busqueda BETWEEN ? AND ?")
	args = append(args, d, h)

	if req.Tipo == "Vuelo" {
		conds = append(conds, "b.Tipo_Busqueda_ID = 1")
	} else if req.Tipo == "Hotel" {
		conds = append(conds, "b.Tipo_Busqueda_ID = 2")
	}
	if req.Usuario != "" {
		conds = append(conds, "(u.Nombre LIKE ? OR u.Apellido LIKE ? OR u.Username LIKE ?)")
		like := "%" + req.Usuario + "%"
		args = append(args, like, like, like)
	}

	where := strings.Join(conds, " AND ")

	var total int
	conn.QueryRowContext(context.Background(),
		fmt.Sprintf("SELECT COUNT(*) FROM busqueda b LEFT JOIN usuario u ON b.UsuarioID=u.ID WHERE %s", where),
		args...).Scan(&total)

	offset := (req.Pagina - 1) * req.TamanoPagina
	dataArgs := append(args, req.TamanoPagina, offset)

	rows, err := conn.QueryContext(context.Background(), fmt.Sprintf(`
		SELECT b.ID, b.Fecha_de_Busqueda,
		       CASE b.Tipo_Busqueda_ID WHEN 1 THEN 'Vuelo' ELSE 'Hotel' END,
		       COALESCE(u.Nombre,'Anónimo'), COALESCE(u.Apellido,''),
		       COALESCE(co.Nombre,''), cd.Nombre
		FROM busqueda b
		LEFT JOIN usuario u ON b.UsuarioID=u.ID
		LEFT JOIN ciudad co ON b.CiudadOrigenID=co.ID
		JOIN ciudad cd ON b.CiudadDestinoID=cd.ID
		WHERE %s ORDER BY b.Fecha_de_Busqueda DESC LIMIT ? OFFSET ?
	`, where), dataArgs...)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "error en listado"})
		return
	}
	defer rows.Close()

	type Registro struct {
		ID            int    `json:"id"`
		Fecha         string `json:"fecha"`
		Tipo          string `json:"tipo"`
		Usuario       string `json:"usuario"`
		CiudadOrigen  string `json:"ciudadOrigen"`
		CiudadDestino string `json:"ciudadDestino"`
	}

	var registros []Registro
	for rows.Next() {
		var r Registro
		var nombre, apellido string
		rows.Scan(&r.ID, &r.Fecha, &r.Tipo, &nombre, &apellido, &r.CiudadOrigen, &r.CiudadDestino)
		r.Usuario = strings.TrimSpace(nombre + " " + apellido)
		registros = append(registros, r)
	}
	if registros == nil {
		registros = []Registro{}
	}

	totalPaginas := total / req.TamanoPagina
	if total%req.TamanoPagina != 0 {
		totalPaginas++
	}

	c.JSON(http.StatusOK, gin.H{
		"registros":      registros,
		"totalRegistros": total,
		"totalPaginas":   totalPaginas,
		"paginaActual":   req.Pagina,
	})
}

// ── cargarDatosExport ────────────────────────────────────────────────────────

func (ctrl *MetricasController) cargarDatosExport(desde, hasta time.Time) helpers.MetricasData {
	d := desde.Format("2006-01-02 15:04:05")
	h := hasta.Format("2006-01-02 15:04:05")

	data := helpers.MetricasData{
		Desde: desde.Format("2006-01-02"),
		Hasta: hasta.Format("2006-01-02"),
	}

	conn, err := ctrl.db.Conn(context.Background())
	if err != nil {
		return data
	}
	defer conn.Close()

	// KPIs
	rows, _ := conn.QueryContext(context.Background(), `
		SELECT Tipo_Busqueda_ID, COUNT(*) FROM busqueda
		WHERE Fecha_de_Busqueda BETWEEN ? AND ? GROUP BY Tipo_Busqueda_ID
	`, d, h)
	if rows != nil {
		for rows.Next() {
			var tid, total int
			rows.Scan(&tid, &total)
			data.KPITotalBusquedas += total
			if tid == 1 {
				data.KPIBusquedasVuelo = total
			} else {
				data.KPIBusquedasHotel += total
			}
		}
		rows.Close()
	}

	conn.QueryRowContext(context.Background(), `
		SELECT COUNT(*), SUM(CASE WHEN EstadoID IN (2,5,6) THEN 1 ELSE 0 END)
		FROM Reservacion WHERE Fecha_Creacion BETWEEN ? AND ?
	`, d, h).Scan(&data.KPITotalReservaciones, &data.KPIReservasPagadas)

	var base float64
	conn.QueryRowContext(context.Background(), `
		SELECT COALESCE(SUM(dt.Total),0), COALESCE(SUM(dt.Total/(1+p.Porcentaje_Ganancia/100)),0)
		FROM Reservacion r JOIN detalles_reservacion dt ON dt.Reservacion_ID=r.ID
		JOIN Proveedor p ON dt.Proveedor_ID=p.ID
		WHERE r.EstadoID IN (2,5,6) AND r.Fecha_Creacion BETWEEN ? AND ?
	`, d, h).Scan(&data.KPIIngresos, &base)
	data.KPIGanancia = data.KPIIngresos - base

	conn.QueryRowContext(context.Background(), `
		SELECT COALESCE(AVG(Total),0) FROM Reservacion
		WHERE EstadoID IN (2,5,6) AND Fecha_Creacion BETWEEN ? AND ?
	`, d, h).Scan(&data.KPITicketPromedio)

	conn.QueryRowContext(context.Background(), `
		SELECT COUNT(DISTINCT UsuarioID) FROM busqueda
		WHERE Fecha_de_Busqueda BETWEEN ? AND ? AND UsuarioID IS NOT NULL
	`, d, h).Scan(&data.KPIUsuariosActivos)

	// Búsquedas por día
	rows, _ = conn.QueryContext(context.Background(), `
		SELECT DATE(Fecha_de_Busqueda), COUNT(*) FROM busqueda
		WHERE Fecha_de_Busqueda BETWEEN ? AND ? GROUP BY 1 ORDER BY 1 ASC
	`, d, h)
	if rows != nil {
		for rows.Next() {
			var f string
			var t int
			rows.Scan(&f, &t)
			data.BusquedasDia = append(data.BusquedasDia, []string{f, strconv.Itoa(t)})
		}
		rows.Close()
	}

	// Búsquedas por tipo
	rows, _ = conn.QueryContext(context.Background(), `
		SELECT CASE Tipo_Busqueda_ID WHEN 1 THEN 'Vuelo' ELSE 'Hotel' END, COUNT(*)
		FROM busqueda WHERE Fecha_de_Busqueda BETWEEN ? AND ? GROUP BY Tipo_Busqueda_ID
	`, d, h)
	if rows != nil {
		for rows.Next() {
			var tp string
			var t int
			rows.Scan(&tp, &t)
			data.BusquedasTipo = append(data.BusquedasTipo, []string{tp, strconv.Itoa(t)})
		}
		rows.Close()
	}

	// Destinos
	rows, _ = conn.QueryContext(context.Background(), `
		SELECT c.Nombre, p.Nombre, COUNT(*) FROM busqueda b
		JOIN ciudad c ON b.CiudadDestinoID=c.ID JOIN pais p ON c.PaisID=p.ID
		WHERE b.Fecha_de_Busqueda BETWEEN ? AND ?
		GROUP BY c.ID, c.Nombre, p.Nombre ORDER BY 3 DESC LIMIT 8
	`, d, h)
	if rows != nil {
		for rows.Next() {
			var ciudad, pais string
			var t int
			rows.Scan(&ciudad, &pais, &t)
			data.Destinos = append(data.Destinos, []string{ciudad, pais, strconv.Itoa(t)})
		}
		rows.Close()
	}

	// Reservaciones por tipo
	rows, _ = conn.QueryContext(context.Background(), `
		SELECT r.Tipo_Reserva_ID, COUNT(DISTINCT r.ID), COALESCE(SUM(dt.Total),0)
		FROM Reservacion r LEFT JOIN detalles_reservacion dt ON dt.Reservacion_ID=r.ID
		WHERE r.EstadoID IN (2,5,6) AND r.Fecha_Creacion BETWEEN ? AND ?
		GROUP BY r.Tipo_Reserva_ID ORDER BY 2 DESC
	`, d, h)
	if rows != nil {
		for rows.Next() {
			var tid, total int
			var ing float64
			rows.Scan(&tid, &total, &ing)
			data.ResTipos = append(data.ResTipos, []string{
				metTipoNombre(tid), strconv.Itoa(total), fmt.Sprintf("%.2f", ing),
			})
		}
		rows.Close()
	}

	// Embudo
	var b1, b2, b3, b4, b5, b6, b7 int
	conn.QueryRowContext(context.Background(), `
		SELECT
		  (SELECT COUNT(*) FROM busqueda WHERE Fecha_de_Busqueda BETWEEN ? AND ?),
		  (SELECT COUNT(*) FROM Reservacion WHERE Fecha_Creacion BETWEEN ? AND ?),
		  (SELECT COUNT(*) FROM Reservacion WHERE EstadoID IN (2,5,6) AND Fecha_Creacion BETWEEN ? AND ?),
		  (SELECT COUNT(*) FROM Reservacion WHERE EstadoID=5 AND Fecha_Creacion BETWEEN ? AND ?),
		  (SELECT COUNT(*) FROM Reservacion WHERE EstadoID=3 AND Fecha_Creacion BETWEEN ? AND ?),
		  (SELECT COUNT(*) FROM Reservacion WHERE EstadoID=4 AND Fecha_Creacion BETWEEN ? AND ?),
		  (SELECT COUNT(*) FROM Reservacion WHERE EstadoID=1 AND Fecha_Creacion BETWEEN ? AND ?)
	`, d, h, d, h, d, h, d, h, d, h, d, h, d, h,
	).Scan(&b1, &b2, &b3, &b4, &b5, &b6, &b7)
	data.Embudo = [][]string{
		{"Busquedas", strconv.Itoa(b1)},
		{"Reservaciones", strconv.Itoa(b2)},
		{"Activas (conf./comp./curso)", strconv.Itoa(b3)},
		{"Completadas", strconv.Itoa(b4)},
		{"Canceladas", strconv.Itoa(b5)},
		{"Expiradas", strconv.Itoa(b6)},
		{"Pendientes", strconv.Itoa(b7)},
	}

	// Proveedores (agrupados por tipo de reservacion + tipo de proveedor)
	rows, _ = conn.QueryContext(context.Background(), `
		SELECT p.Nombre, p.Tipo_Proveedor_ID, r.Tipo_Reserva_ID,
		       COUNT(DISTINCT dt.Reservacion_ID),
		       COALESCE(SUM(dt.Total),0),
		       COALESCE(SUM(dt.Total - dt.Total/(1+p.Porcentaje_Ganancia/100)),0)
		FROM detalles_reservacion dt JOIN Proveedor p ON dt.Proveedor_ID=p.ID
		JOIN Reservacion r ON r.ID=dt.Reservacion_ID
		WHERE r.EstadoID IN (2,5,6) AND r.Fecha_Creacion BETWEEN ? AND ?
		GROUP BY p.ID, p.Nombre, p.Tipo_Proveedor_ID, r.Tipo_Reserva_ID
		ORDER BY r.Tipo_Reserva_ID, p.Tipo_Proveedor_ID, 5 DESC
	`, d, h)
	if rows != nil {
		for rows.Next() {
			var nombre string
			var tipoP, tipoR, cnt int
			var ing, gan float64
			rows.Scan(&nombre, &tipoP, &tipoR, &cnt, &ing, &gan)
			// Etiqueta de grupo: combina tipoReserva + tipoProveedor
			grupo := "Hotel directo"
			switch {
			case tipoR == 1:
				grupo = "Vuelo directo"
			case tipoR == 2:
				grupo = "Hotel directo"
			case tipoR == 3 && tipoP == 1:
				grupo = "Paquete · Vuelo"
			case tipoR == 3 && tipoP == 2:
				grupo = "Paquete · Hotel"
			}
			provTipo := "Hotel"
			if tipoP == 1 {
				provTipo = "Aerolinea"
			}
			data.Proveedores = append(data.Proveedores, []string{
				nombre, grupo, provTipo, strconv.Itoa(cnt),
				fmt.Sprintf("%.2f", ing), fmt.Sprintf("%.2f", gan),
			})
		}
		rows.Close()
	}

	// Cancelaciones
	rows, _ = conn.QueryContext(context.Background(), `
		SELECT Tipo_Reserva_ID, COUNT(*) FROM Reservacion
		WHERE EstadoID=3 AND Fecha_Creacion BETWEEN ? AND ? GROUP BY Tipo_Reserva_ID
	`, d, h)
	if rows != nil {
		for rows.Next() {
			var tid, t int
			rows.Scan(&tid, &t)
			data.Cancelaciones = append(data.Cancelaciones, []string{metTipoNombre(tid), strconv.Itoa(t)})
		}
		rows.Close()
	}

	// Tendencia mensual
	rows, _ = conn.QueryContext(context.Background(), `
		SELECT DATE_FORMAT(r.Fecha_Creacion,'%Y-%m'), r.Tipo_Reserva_ID,
		       COALESCE(SUM(dt.Total),0), COUNT(DISTINCT r.ID)
		FROM Reservacion r JOIN detalles_reservacion dt ON dt.Reservacion_ID=r.ID
		WHERE r.EstadoID IN (2,5,6) AND r.Fecha_Creacion BETWEEN ? AND ?
		GROUP BY 1,2 ORDER BY 1 ASC
	`, d, h)
	if rows != nil {
		for rows.Next() {
			var mes string
			var tid, cnt int
			var ing float64
			rows.Scan(&mes, &tid, &ing, &cnt)
			data.Tendencia = append(data.Tendencia, []string{
				mes, metTipoNombre(tid), fmt.Sprintf("%.2f", ing), strconv.Itoa(cnt),
			})
		}
		rows.Close()
	}

	// Heatmap
	rows, _ = conn.QueryContext(context.Background(), `
		SELECT DAYOFWEEK(Fecha_de_Busqueda), HOUR(Fecha_de_Busqueda), COUNT(*)
		FROM busqueda WHERE Fecha_de_Busqueda BETWEEN ? AND ? GROUP BY 1,2 ORDER BY 1,2
	`, d, h)
	if rows != nil {
		dias := []string{"", "Dom", "Lun", "Mar", "Mie", "Jue", "Vie", "Sab"}
		for rows.Next() {
			var dia, hora, t int
			rows.Scan(&dia, &hora, &t)
			diaNombre := "?"
			if dia >= 1 && dia <= 7 {
				diaNombre = dias[dia]
			}
			data.Heatmap = append(data.Heatmap, []string{diaNombre, strconv.Itoa(hora), strconv.Itoa(t)})
		}
		rows.Close()
	}

	// Listado (últimos 200)
	rows, _ = conn.QueryContext(context.Background(), `
		SELECT b.ID, b.Fecha_de_Busqueda,
		       CASE b.Tipo_Busqueda_ID WHEN 1 THEN 'Vuelo' ELSE 'Hotel' END,
		       COALESCE(u.Nombre,'Anonimo'), COALESCE(u.Apellido,''),
		       COALESCE(co.Nombre,''), cd.Nombre
		FROM busqueda b
		LEFT JOIN usuario u ON b.UsuarioID=u.ID
		LEFT JOIN ciudad co ON b.CiudadOrigenID=co.ID
		JOIN ciudad cd ON b.CiudadDestinoID=cd.ID
		WHERE b.Fecha_de_Busqueda BETWEEN ? AND ?
		ORDER BY b.Fecha_de_Busqueda DESC LIMIT 200
	`, d, h)
	if rows != nil {
		for rows.Next() {
			var id int
			var fecha, tipo, nom, ape, orig, dest string
			rows.Scan(&id, &fecha, &tipo, &nom, &ape, &orig, &dest)
			data.Listado = append(data.Listado, []string{
				strconv.Itoa(id), fecha, tipo,
				strings.TrimSpace(nom + " " + ape), orig, dest,
			})
		}
		rows.Close()
	}

	return data
}

// ── ExportarArchivo ──────────────────────────────────────────────────────────

// ExportarArchivo genera y descarga un reporte en formato excel, csv o pdf.
//
// POST /api/admin/metricas/exportar-archivo
func (ctrl *MetricasController) ExportarArchivo(c *gin.Context) {
	var req struct {
		Formato    string `json:"formato"`
		FechaDesde string `json:"fechaDesde"`
		FechaHasta string `json:"fechaHasta"`
	}
	if err := c.ShouldBindJSON(&req); err != nil || req.Formato == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "formato requerido (excel|csv|pdf)"})
		return
	}

	desde, hasta := metParseFechasBody(req.FechaDesde, req.FechaHasta)
	data := ctrl.cargarDatosExport(desde, hasta)
	nombre := fmt.Sprintf("metricas_movent_%s_%s", data.Desde, data.Hasta)

	switch req.Formato {
	case "excel":
		b := helpers.GenerarMetricasXLSX(data)
		c.Header("Content-Disposition", fmt.Sprintf(`attachment; filename="%s.xlsx"`, nombre))
		c.Data(http.StatusOK, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", b)

	case "csv":
		b := helpers.GenerarMetricasCSVZip(data)
		c.Header("Content-Disposition", fmt.Sprintf(`attachment; filename="%s.zip"`, nombre))
		c.Data(http.StatusOK, "application/zip", b)

	case "pdf":
		b, err := helpers.GenerarMetricasPDF(data)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "error generando PDF"})
			return
		}
		c.Header("Content-Disposition", fmt.Sprintf(`attachment; filename="%s.pdf"`, nombre))
		c.Data(http.StatusOK, "application/pdf", b)

	default:
		c.JSON(http.StatusBadRequest, gin.H{"error": "formato no soportado"})
	}
}

// ── ExportarCorreo ───────────────────────────────────────────────────────────

// ExportarCorreo genera un reporte y lo envía por correo electrónico.
//
// POST /api/admin/metricas/exportar-correo
func (ctrl *MetricasController) ExportarCorreo(c *gin.Context) {
	var req struct {
		Correos    []string `json:"correos"`
		Formato    string   `json:"formato"`
		FechaDesde string   `json:"fechaDesde"`
		FechaHasta string   `json:"fechaHasta"`
	}
	if err := c.ShouldBindJSON(&req); err != nil || len(req.Correos) == 0 || req.Formato == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "correos y formato son requeridos"})
		return
	}

	desde, hasta := metParseFechasBody(req.FechaDesde, req.FechaHasta)
	data := ctrl.cargarDatosExport(desde, hasta)
	nombre := fmt.Sprintf("metricas_movent_%s_%s", data.Desde, data.Hasta)

	var fileBytes []byte
	var mime, ext string

	switch req.Formato {
	case "excel":
		fileBytes = helpers.GenerarMetricasXLSX(data)
		mime = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
		ext = "xlsx"
	case "csv":
		fileBytes = helpers.GenerarMetricasCSVZip(data)
		mime = "application/zip"
		ext = "zip"
	case "pdf":
		var err error
		fileBytes, err = helpers.GenerarMetricasPDF(data)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "error generando PDF"})
			return
		}
		mime = "application/pdf"
		ext = "pdf"
	default:
		c.JSON(http.StatusBadRequest, gin.H{"error": "formato no soportado"})
		return
	}

	htmlBody := helpers.BuildHTMLMetricasCorreo(data)
	asunto := fmt.Sprintf("Reporte de Metricas MOVENT — %s a %s", data.Desde, data.Hasta)
	archivoNombre := fmt.Sprintf("%s.%s", nombre, ext)

	var errores []string
	for _, correo := range req.Correos {
		correo = strings.TrimSpace(correo)
		if correo == "" {
			continue
		}
		if err := helpers.EnviarEmailConAdjunto(correo, asunto, htmlBody, fileBytes, archivoNombre, mime); err != nil {
			errores = append(errores, fmt.Sprintf("%s: %v", correo, err))
		}
	}

	if len(errores) > 0 {
		c.JSON(http.StatusPartialContent, gin.H{
			"message": fmt.Sprintf("Enviado con errores: %s", strings.Join(errores, "; ")),
		})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"message": fmt.Sprintf("Reporte enviado a %s", strings.Join(req.Correos, ", ")),
	})
}
