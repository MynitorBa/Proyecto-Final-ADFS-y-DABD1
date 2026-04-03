package controllers

import (
	"context"
	"database/sql"
	"net/http"

	"github.com/gin-gonic/gin"
)

type StatsController struct {
	db *sql.DB
}

func NewStatsController(db *sql.DB) *StatsController {
	return &StatsController{db: db}
}

// GET /api/stats — público, sin auth
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