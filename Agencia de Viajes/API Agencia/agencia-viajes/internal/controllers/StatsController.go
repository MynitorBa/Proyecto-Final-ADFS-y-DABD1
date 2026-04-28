// # Package controllers
//
// Controladores HTTP de la API de Movent. Cada controlador agrupa los handlers
// relacionados a un recurso o dominio especifico de la aplicacion.
package controllers

import (
	"context"
	"database/sql"
	"net/http"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
)

// StatsController
//
// Controlador que maneja los endpoints de estadisticas generales de la
// plataforma, incluyendo conteos de proveedores, usuarios y reservaciones.
// Es de acceso publico, no requiere autenticacion.
type StatsController struct {
	db *sql.DB
}

// NewStatsController
//
// Constructor que retorna una nueva instancia de StatsController
// con la conexion a la base de datos inyectada.
//
// Parametros:
//   - db: puntero a la conexion de base de datos SQL
//
// Retorna:
//   - *StatsController: puntero a la nueva instancia
func NewStatsController(db *sql.DB) *StatsController {
	return &StatsController{db: db}
}

// ObtenerStats
//
// Retorna un resumen estadistico de la plataforma consultando directamente
// la base de datos. Incluye conteos de aerolineas, hoteles, usuarios
// y reservaciones por estado y por tipo.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200 OK: JSON con todas las estadisticas de la plataforma
//   - HTTP 500 Internal Server Error: si ocurre un error de conexion a la base de datos
//
// Notas:
//   - Este endpoint es publico y devuelve contadores generales del sistema
//     (aerolineas, hoteles, usuarios, reservaciones).
//   - Los datos financieros sensibles estan en /api/admin/metricas,
//     protegido con rol de administrador.
func (ctrl *StatsController) ObtenerStats(c *gin.Context) {
	conn, err := ctrl.db.Conn(context.Background())
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "error de conexión"})
		return
	}
	defer conn.Close()

	scanInt := func(q string) int {
		var n int
		conn.QueryRowContext(context.Background(), q).Scan(&n)
		return n
	}

	c.JSON(http.StatusOK, gin.H{
		// Proveedores
		"aerolineas": scanInt("SELECT COUNT(*) FROM Proveedor WHERE Tipo_Proveedor_ID = 1"),
		"hoteles":    scanInt("SELECT COUNT(*) FROM Proveedor WHERE Tipo_Proveedor_ID = 2"),

		// Usuarios registrados (rol 1)
		"usuarios": scanInt("SELECT COUNT(*) FROM Usuario WHERE RolID = 1"),

		// Reservaciones totales
		"reservaciones": scanInt("SELECT COUNT(*) FROM Reservacion"),

		// Por estado
		"pendientes":  scanInt("SELECT COUNT(*) FROM Reservacion WHERE EstadoID = 1"),
		"confirmadas": scanInt("SELECT COUNT(*) FROM Reservacion WHERE EstadoID = 2"),
		"canceladas":  scanInt("SELECT COUNT(*) FROM Reservacion WHERE EstadoID = 3"),
		"expiradas":   scanInt("SELECT COUNT(*) FROM Reservacion WHERE EstadoID = 4"),
		"completadas": scanInt("SELECT COUNT(*) FROM Reservacion WHERE EstadoID = 5"),
		"enCurso":     scanInt("SELECT COUNT(*) FROM Reservacion WHERE EstadoID = 6"),

		// Por tipo (1=Aerolinea 2=Hotelera 3=Paquete)
		"vuelosReservados":  scanInt("SELECT COUNT(*) FROM Reservacion WHERE Tipo_Reserva_ID = 1"),
		"hotelesReservados": scanInt("SELECT COUNT(*) FROM Reservacion WHERE Tipo_Reserva_ID = 2"),
		"paquetesActivos":   scanInt("SELECT COUNT(*) FROM Reservacion WHERE Tipo_Reserva_ID = 3"),
	})
}

// PanelDescubrir representa un destino sugerido para mostrar en la pantalla de inicio.
type PanelDescubrir struct {
	Tipo        string  `json:"tipo"`        // "vuelo" | "hotel"
	Ciudad      string  `json:"ciudad"`      // nombre de la ciudad destino
	Pais        string  `json:"pais"`        // nombre del pais destino
	PrecioDesde float64 `json:"precioDesde"` // precio promedio estimado (puede ser 0 si sin datos)
	Popularidad int     `json:"popularidad"` // numero de busquedas historicas
}

// Descubrir
//
// Devuelve hasta 5 paneles de destinos sugeridos para mostrar en la pagina de inicio.
// Si se pasan los parametros opcionales ?ciudad= y ?pais=, los primeros 3 paneles
// son destinos de vuelo populares DESDE esa ciudad. Los ultimos 2 son hoteles
// populares a nivel global. Cuando no hay datos historicos suficientes se
// completa con los destinos mas buscados a nivel mundial.
//
// Parametros (query string, opcionales):
//   - ciudad: nombre de la ciudad de origen del usuario
//   - pais:   nombre del pais de origen del usuario
//
// Retorna:
//   - HTTP 200 OK: JSON con origen y paneles[]
func (ctrl *StatsController) Descubrir(c *gin.Context) {
	ciudad := strings.TrimSpace(c.Query("ciudad"))
	pais := strings.TrimSpace(c.Query("pais"))

	ctx := context.Background()
	conn, err := ctrl.db.Conn(ctx)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "error de conexión"})
		return
	}
	defer conn.Close()

	// Precio promedio por tipo de reservacion (vuelo=1, hotel=2)
	avgPrecio := func(tipoID int) float64 {
		var avg sql.NullFloat64
		conn.QueryRowContext(ctx, `
			SELECT AVG(dr.Total)
			FROM detalles_reservacion dr
			JOIN Reservacion r ON r.ID = dr.ReservacionID
			WHERE r.Tipo_Reserva_ID = ? AND r.EstadoID IN (2, 5)
		`, tipoID).Scan(&avg)
		if avg.Valid && avg.Float64 > 0 {
			return avg.Float64
		}
		if tipoID == 1 {
			return 350.0
		}
		return 180.0
	}

	precioVuelo := avgPrecio(1)
	precioHotel := avgPrecio(2)

	// Resolver ID de la ciudad de origen del usuario (puede ser nil)
	var origenID *int
	if ciudad != "" && pais != "" {
		var id int
		err := conn.QueryRowContext(ctx, `
			SELECT c.ID FROM Ciudad c
			JOIN Pais p ON c.PaisID = p.ID
			WHERE LOWER(TRIM(c.Nombre)) = LOWER(TRIM(?))
			  AND LOWER(TRIM(p.Nombre)) = LOWER(TRIM(?))
			LIMIT 1
		`, ciudad, pais).Scan(&id)
		if err == nil {
			origenID = &id
		}
	}

	var paneles []PanelDescubrir
	yaIncluidos := map[string]bool{}

	// ── Paneles de vuelo ─────────────────────────────────────────────────────
	// Solo mostrar vuelos si la ciudad del usuario tiene aerolineas activas
	// en el catalogo (Tipo_Catalogo_ID = 1). Si no tiene, no tiene sentido
	// mostrar vuelos desde esa ciudad.
	tieneAerolineas := false
	if origenID != nil {
		var cnt int
		conn.QueryRowContext(ctx, `
			SELECT COUNT(*)
			FROM Catalogo_Proveedor cp
			JOIN Proveedor pr ON cp.Proveedor_ID = pr.ID
			WHERE cp.Ciudad_Origen_ID = ? AND cp.Tipo_Catalogo_ID = 1 AND pr.EstadoID = 1
		`, *origenID).Scan(&cnt)
		tieneAerolineas = cnt > 0
	}

	if tieneAerolineas && origenID != nil {
		// Destinos de vuelo: ciudades a las que se ha buscado DESDE este origen
		// (mejor proxy disponible — los destinos exactos de cada aerolinea
		//  solo se conocen llamando a su API en tiempo real)
		rows, err := conn.QueryContext(ctx, `
			SELECT c.Nombre, pa.Nombre, COUNT(*) as cnt
			FROM Busqueda b
			JOIN Ciudad c  ON b.CiudadDestinoID = c.ID
			JOIN Pais   pa ON c.PaisID = pa.ID
			WHERE b.Tipo_Busqueda_ID = 1 AND b.CiudadOrigenID = ?
			GROUP BY c.ID, c.Nombre, pa.Nombre
			ORDER BY cnt DESC
			LIMIT 3
		`, *origenID)
		if err == nil {
			defer rows.Close()
			for rows.Next() {
				var p PanelDescubrir
				p.Tipo = "vuelo"
				p.PrecioDesde = precioVuelo
				if rows.Scan(&p.Ciudad, &p.Pais, &p.Popularidad) == nil {
					key := strings.ToLower(p.Ciudad + "|" + p.Pais)
					paneles = append(paneles, p)
					yaIncluidos[key] = true
				}
			}
		}
	}

	// Si no hay suficientes vuelos (origen sin historial o sin aerolineas),
	// completar con los destinos mas buscados en toda la plataforma
	if len(paneles) < 3 {
		necesita := 3 - len(paneles)
		rows, err := conn.QueryContext(ctx, `
			SELECT c.Nombre, pa.Nombre, COUNT(*) as cnt
			FROM Busqueda b
			JOIN Ciudad c  ON b.CiudadDestinoID = c.ID
			JOIN Pais   pa ON c.PaisID = pa.ID
			WHERE b.Tipo_Busqueda_ID = 1
			GROUP BY c.ID, c.Nombre, pa.Nombre
			ORDER BY cnt DESC
			LIMIT 10
		`)
		if err == nil {
			defer rows.Close()
			for rows.Next() && necesita > 0 {
				var p PanelDescubrir
				p.Tipo = "vuelo"
				p.PrecioDesde = precioVuelo
				if rows.Scan(&p.Ciudad, &p.Pais, &p.Popularidad) == nil {
					key := strings.ToLower(p.Ciudad + "|" + p.Pais)
					if !yaIncluidos[key] {
						paneles = append(paneles, p)
						yaIncluidos[key] = true
						necesita--
					}
				}
			}
		}
	}

	// ── Paneles de hotel ─────────────────────────────────────────────────────
	// FUENTE REAL: ciudades que tienen proveedores hoteleros ACTIVOS en el
	// catalogo (Tipo_Catalogo_ID = 2). Estos son destinos donde SÍ se puede
	// reservar un hotel en este sistema.
	hotelRows, err := conn.QueryContext(ctx, `
		SELECT c.Nombre, pa.Nombre, COUNT(*) as cnt
		FROM Catalogo_Proveedor cp
		JOIN Proveedor pr ON cp.Proveedor_ID = pr.ID
		JOIN Ciudad    c  ON cp.Ciudad_Origen_ID = c.ID
		JOIN Pais      pa ON c.PaisID = pa.ID
		WHERE cp.Tipo_Catalogo_ID = 2 AND pr.EstadoID = 1
		GROUP BY c.ID, c.Nombre, pa.Nombre
		ORDER BY cnt DESC
		LIMIT 2
	`)
	if err == nil {
		defer hotelRows.Close()
		for hotelRows.Next() {
			var p PanelDescubrir
			p.Tipo = "hotel"
			p.PrecioDesde = precioHotel
			if hotelRows.Scan(&p.Ciudad, &p.Pais, &p.Popularidad) == nil {
				key := strings.ToLower(p.Ciudad + "|" + p.Pais)
				if !yaIncluidos[key] {
					paneles = append(paneles, p)
					yaIncluidos[key] = true
				}
			}
		}
	}

	// Si hoteles del catalogo no alcanzan 2, completar con historial de busquedas
	if len(paneles) < 5 {
		necesita := 5 - len(paneles)
		rows, err := conn.QueryContext(ctx, `
			SELECT c.Nombre, pa.Nombre, COUNT(*) as cnt
			FROM Busqueda b
			JOIN Ciudad c  ON b.CiudadDestinoID = c.ID
			JOIN Pais   pa ON c.PaisID = pa.ID
			WHERE b.Tipo_Busqueda_ID = 2
			GROUP BY c.ID, c.Nombre, pa.Nombre
			ORDER BY cnt DESC
			LIMIT 10
		`)
		if err == nil {
			defer rows.Close()
			for rows.Next() && necesita > 0 {
				var p PanelDescubrir
				p.Tipo = "hotel"
				p.PrecioDesde = precioHotel
				if rows.Scan(&p.Ciudad, &p.Pais, &p.Popularidad) == nil {
					key := strings.ToLower(p.Ciudad + "|" + p.Pais)
					if !yaIncluidos[key] {
						paneles = append(paneles, p)
						yaIncluidos[key] = true
						necesita--
					}
				}
			}
		}
	}

	// ── Fallback completo: BD vacia o sistema recien instalado ───────────────
	if len(paneles) == 0 {
		paneles = []PanelDescubrir{
			{Tipo: "vuelo", Ciudad: "Miami",            Pais: "United States",      PrecioDesde: 350, Popularidad: 0},
			{Tipo: "vuelo", Ciudad: "Ciudad de Mexico", Pais: "Mexico",             PrecioDesde: 280, Popularidad: 0},
			{Tipo: "vuelo", Ciudad: "Bogota",           Pais: "Colombia",           PrecioDesde: 310, Popularidad: 0},
			{Tipo: "hotel", Ciudad: "Cancun",           Pais: "Mexico",             PrecioDesde: 180, Popularidad: 0},
			{Tipo: "hotel", Ciudad: "Punta Cana",       Pais: "Dominican Republic", PrecioDesde: 220, Popularidad: 0},
		}
	}

	if len(paneles) > 5 {
		paneles = paneles[:5]
	}

	c.JSON(http.StatusOK, gin.H{
		"origen":          gin.H{"ciudad": ciudad, "pais": pais},
		"paneles":         paneles,
		"tieneAerolineas": tieneAerolineas,
	})
}

// DestinoVuelo — parámetros exactos de búsqueda para una card de vuelo disponible.
type DestinoVuelo struct {
	Origen            string `json:"origen"`
	OrigenPais        string `json:"origenPais"`
	Destino           string `json:"destino"`
	DestinoPais       string `json:"destinoPais"`
	Fecha             string `json:"fecha"`
	CantidadPasajeros int    `json:"cantidadPasajeros"`
	Popularidad       int    `json:"popularidad"`
}

// DestinoHotel — parámetros exactos de búsqueda para una card de hotel disponible.
type DestinoHotel struct {
	Ciudad           string `json:"ciudad"`
	Pais             string `json:"pais"`
	FechaCheckIn     string `json:"fechaCheckIn"`
	FechaCheckOut    string `json:"fechaCheckOut"`
	CantidadPersonas int    `json:"cantidadPersonas"`
	Popularidad      int    `json:"popularidad"`
}

// DescubrirDisponibles
//
// Devuelve hasta 4 destinos de vuelo y 4 de hotel con los parámetros exactos
// de búsqueda, derivados de reservaciones reales (pendientes o confirmadas).
// Las ciudades provienen de catalogo_proveedor, por lo que están garantizadas
// en el catálogo y no generarán errores 400 al buscar. Fecha = hoy+7.
// Los paquetes reutilizan los destinos de vuelo. Es público, no requiere auth.
//
// Retorna:
//   - HTTP 200: JSON con { vuelos: [], hoteles: [], paquetes: [] }
func (ctrl *StatsController) DescubrirDisponibles(c *gin.Context) {
	ctx := context.Background()
	conn, err := ctrl.db.Conn(ctx)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "error de conexión"})
		return
	}
	defer conn.Close()

	f7  := time.Now().AddDate(0, 6, 0).Format("2006-01-02")
	f10 := time.Now().AddDate(0, 6, 3).Format("2006-01-02")

	// ── Vuelos ──────────────────────────────────────────────────────────────────
	// Reservaciones pendientes/confirmadas/en curso/completadas con detalle de vuelo (Tipo_Detalle_ID=1).
	// JOIN con catalogo_proveedor garantiza que origen y destino existen en catálogo.
	vuelos := func() []DestinoVuelo {
		rows, err := conn.QueryContext(ctx, `
			SELECT
				co.Nombre AS origen,  po.Nombre AS origenPais,
				cd.Nombre AS destino, pd.Nombre AS destinoPais,
				COUNT(*)  AS popularidad
			FROM reservacion r
			JOIN detalles_reservacion dr
			     ON dr.Reservacion_ID = r.ID AND dr.Tipo_Detalle_ID = 1
			JOIN catalogo_proveedor cp
			     ON cp.Proveedor_ID = dr.Proveedor_ID AND cp.Tipo_Catalogo_ID = 1
			JOIN ciudad co ON co.ID = cp.Ciudad_Origen_ID
			JOIN pais   po ON po.ID = co.PaisID
			JOIN ciudad cd ON cd.ID = cp.Ciudad_Destino_ID
			JOIN pais   pd ON pd.ID = cd.PaisID
			WHERE r.EstadoID IN (1, 2, 3, 4)
			GROUP BY cp.Ciudad_Origen_ID, cp.Ciudad_Destino_ID
			ORDER BY popularidad DESC
			LIMIT 4
		`)
		if err != nil {
			return nil
		}
		defer rows.Close()
		var out []DestinoVuelo
		for rows.Next() {
			var d DestinoVuelo
			if rows.Scan(&d.Origen, &d.OrigenPais, &d.Destino, &d.DestinoPais, &d.Popularidad) == nil {
				d.Fecha = f7
				d.CantidadPasajeros = 1
				out = append(out, d)
			}
		}
		return out
	}()

	// ── Hoteles ─────────────────────────────────────────────────────────────────
	// Reservaciones pendientes/confirmadas/en curso/completadas con detalle de hotel (Tipo_Detalle_ID=2).
	hoteles := func() []DestinoHotel {
		rows, err := conn.QueryContext(ctx, `
			SELECT
				c.Nombre AS ciudad, p.Nombre AS pais,
				COUNT(*) AS popularidad
			FROM reservacion r
			JOIN detalles_reservacion dr
			     ON dr.Reservacion_ID = r.ID AND dr.Tipo_Detalle_ID = 2
			JOIN catalogo_proveedor cp
			     ON cp.Proveedor_ID = dr.Proveedor_ID AND cp.Tipo_Catalogo_ID = 2
			JOIN ciudad c ON c.ID = cp.Ciudad_Origen_ID
			JOIN pais   p ON p.ID = c.PaisID
			WHERE r.EstadoID IN (1, 2, 3, 4)
			GROUP BY cp.Ciudad_Origen_ID
			ORDER BY popularidad DESC
			LIMIT 4
		`)
		if err != nil {
			return nil
		}
		defer rows.Close()
		var out []DestinoHotel
		for rows.Next() {
			var d DestinoHotel
			if rows.Scan(&d.Ciudad, &d.Pais, &d.Popularidad) == nil {
				d.FechaCheckIn = f7
				d.FechaCheckOut = f10
				d.CantidadPersonas = 1
				out = append(out, d)
			}
		}
		return out
	}()

	// ── Fallback: sin reservaciones pendientes/confirmadas ───────────────────────
	// Consulta el catálogo directamente (proveedores activos) para garantizar
	// que los destinos sean buscables sin errores 400.
	if len(vuelos) == 0 {
		rows, _ := conn.QueryContext(ctx, `
			SELECT DISTINCT co.Nombre, po.Nombre, cd.Nombre, pd.Nombre
			FROM catalogo_proveedor cp
			JOIN proveedor pr ON pr.ID = cp.Proveedor_ID AND pr.EstadoID = 1
			JOIN ciudad co ON co.ID = cp.Ciudad_Origen_ID
			JOIN pais   po ON po.ID = co.PaisID
			JOIN ciudad cd ON cd.ID = cp.Ciudad_Destino_ID
			JOIN pais   pd ON pd.ID = cd.PaisID
			WHERE cp.Tipo_Catalogo_ID = 1
			LIMIT 4
		`)
		if rows != nil {
			defer rows.Close()
			for rows.Next() {
				var d DestinoVuelo
				if rows.Scan(&d.Origen, &d.OrigenPais, &d.Destino, &d.DestinoPais) == nil {
					d.Fecha = f7
					d.CantidadPasajeros = 1
					vuelos = append(vuelos, d)
				}
			}
		}
	}
	if len(hoteles) == 0 {
		rows, _ := conn.QueryContext(ctx, `
			SELECT DISTINCT c.Nombre, p.Nombre
			FROM catalogo_proveedor cp
			JOIN proveedor pr ON pr.ID = cp.Proveedor_ID AND pr.EstadoID = 1
			JOIN ciudad c ON c.ID = cp.Ciudad_Origen_ID
			JOIN pais   p ON p.ID = c.PaisID
			WHERE cp.Tipo_Catalogo_ID = 2
			LIMIT 4
		`)
		if rows != nil {
			defer rows.Close()
			for rows.Next() {
				var d DestinoHotel
				if rows.Scan(&d.Ciudad, &d.Pais) == nil {
					d.FechaCheckIn = f7
					d.FechaCheckOut = f10
					d.CantidadPersonas = 1
					hoteles = append(hoteles, d)
				}
			}
		}
	}

	// Paquetes usan los mismos destinos de vuelo
	paquetes := vuelos
	if len(paquetes) > 4 {
		paquetes = paquetes[:4]
	}

	c.JSON(http.StatusOK, gin.H{
		"vuelos":   vuelos,
		"hoteles":  hoteles,
		"paquetes": paquetes,
	})
}
