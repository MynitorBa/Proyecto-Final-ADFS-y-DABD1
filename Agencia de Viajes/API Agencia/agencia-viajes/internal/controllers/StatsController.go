// # Package controllers
//
// Controladores HTTP de la API de Movent. Cada controlador agrupa los handlers
// relacionados a un recurso o dominio especifico de la aplicacion.
package controllers

import (
	"context"
	"database/sql"
	"net/http"

	"github.com/gin-gonic/gin"
)

// StatsController
//
// Controlador que maneja los endpoints de estadisticas generales de la
// plataforma, incluyendo conteos de proveedores, usuarios, reservaciones
// e ingresos totales. Es de acceso publico, no requiere autenticacion.
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
// la base de datos. Incluye conteos de aerolineas, hoteles, usuarios,
// reservaciones por estado y por tipo, asi como los ingresos totales.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200 OK: JSON con todas las estadisticas de la plataforma
//   - HTTP 500 Internal Server Error: si ocurre un error de conexion a la base de datos
//
// Notas:
//   - Este endpoint es publico y no requiere autenticacion
//   - Los ingresos totales se calculan sumando el campo Total de detalles_reservacion
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
	scanFloat := func(q string) float64 {
		var n float64
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

		// Ingresos — misma fuente que Finanzas (suma de detalles_reservacion.Total)
		"ingresosTotales": scanFloat("SELECT COALESCE(SUM(Total), 0) FROM detalles_reservacion"),

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
