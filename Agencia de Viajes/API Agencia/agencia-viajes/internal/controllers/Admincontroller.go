// # Package controllers
//
// Controladores HTTP de la API de Movent. Cada controlador agrupa los handlers
// relacionados a un recurso o dominio especifico de la aplicacion.
package controllers

import (
	"agencia-viajes/internal/helpers"
	"agencia-viajes/internal/services"
	"context"
	"database/sql"
	"fmt"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
)

// AdminController
//
// Controlador que maneja los endpoints del panel de administracion,
// incluyendo gestion de usuarios, roles, proveedores, reservaciones
// recientes y metricas financieras.
type AdminController struct {
	db        *sql.DB
	logSesion *services.LogSesionService
}

// NewAdminController
//
// Constructor que retorna una nueva instancia de AdminController
// con la conexion a la base de datos y el servicio de auditoria inyectados.
//
// Parametros:
//   - db:        puntero a la conexion de base de datos SQL
//   - logSesion: instancia del servicio de auditoria de sesion
//
// Retorna:
//   - *AdminController: puntero a la nueva instancia
func NewAdminController(db *sql.DB, logSesion *services.LogSesionService) *AdminController {
	return &AdminController{db: db, logSesion: logSesion}
}

// ListarUsuarios
//
// Retorna la lista completa de usuarios registrados en el sistema junto
// con su rol asignado, ordenada por ID ascendente.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200 OK: JSON con arreglo de usuarios (id, nombre, apellido, correo, fechaRegistro, rolId, rol)
//   - HTTP 500 Internal Server Error: si ocurre un error de conexion o consulta
func (ctrl *AdminController) ListarUsuarios(c *gin.Context) {
	conn, err := ctrl.db.Conn(context.Background())
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "error de conexión"})
		return
	}
	defer conn.Close()

	rows, err := conn.QueryContext(context.Background(), `
		SELECT u.ID, u.Nombre, u.Apellido, u.Correo, u.FechaNacimiento,
		       u.RolID, r.RolNombre
		FROM Usuario u
		JOIN Rol r ON u.RolID = r.ID
		ORDER BY u.ID
	`)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "error consultando usuarios"})
		return
	}
	defer rows.Close()

	type UsuarioAdmin struct {
		ID            int    `json:"id"`
		Nombre        string `json:"nombre"`
		Apellido      string `json:"apellido"`
		Correo        string `json:"correo"`
		FechaRegistro string `json:"fechaRegistro"`
		RolID         int    `json:"rolId"`
		Rol           string `json:"rol"`
	}

	var lista []UsuarioAdmin
	for rows.Next() {
		var u UsuarioAdmin
		if err := rows.Scan(&u.ID, &u.Nombre, &u.Apellido, &u.Correo,
			&u.FechaRegistro, &u.RolID, &u.Rol); err != nil {
			continue
		}
		lista = append(lista, u)
	}
	if lista == nil {
		lista = []UsuarioAdmin{}
	}
	c.JSON(http.StatusOK, lista)
}

// ActualizarRol
//
// Actualiza el rol de un usuario especifico. El ID del usuario se lee
// desde el parametro de URL :id y el nuevo rol desde el body JSON.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200 OK: JSON con mensaje de confirmacion
//   - HTTP 400 Bad Request: si el ID es invalido o el campo rolId no esta presente
//   - HTTP 500 Internal Server Error: si ocurre un error de conexion o al ejecutar el UPDATE
func (ctrl *AdminController) ActualizarRol(c *gin.Context) {
	id, err := strconv.Atoi(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "ID inválido"})
		return
	}

	var req struct {
		RolID int `json:"rolId"`
	}
	if err := c.ShouldBindJSON(&req); err != nil || req.RolID == 0 {
		c.JSON(http.StatusBadRequest, gin.H{"error": "rolId requerido"})
		return
	}

	// FIX: admin no puede degradarse a sí mismo
	adminIDRaw, _ := c.Get("usuario_id")
	adminID, _ := adminIDRaw.(int)
	if adminID == id {
		c.JSON(http.StatusBadRequest, gin.H{
			"error": "No puedes cambiar tu propio rol. Pide a otro administrador que lo haga.",
		})
		return
	}

	conn, err := ctrl.db.Conn(context.Background())
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "error de conexión"})
		return
	}
	defer conn.Close()

	// Consultar rol anterior para incluirlo en el mensaje de auditoría
	var rolAnterior int
	_ = conn.QueryRowContext(context.Background(),
		"SELECT RolID FROM Usuario WHERE ID = ?", id,
	).Scan(&rolAnterior)

	_, err = conn.ExecContext(context.Background(),
		"UPDATE Usuario SET RolID = ? WHERE ID = ?", req.RolID, id)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "error actualizando rol"})
		return
	}

	// Log de cambio de rol exitoso (ID 43)
	ctrl.logSesion.Registrar(c, helpers.TipoRolUsuarioActualizado,
		&adminID, fmt.Sprintf("usuario_objetivo=%d", id),
		fmt.Sprintf("Admin (ID=%d) cambió rol del usuario ID=%d: rol_anterior=%d → rol_nuevo=%d",
			adminID, id, rolAnterior, req.RolID))

	c.JSON(http.StatusOK, gin.H{"mensaje": "rol actualizado"})
}

// ListarProveedores
//
// Retorna la lista completa de proveedores registrados con su tipo, estado,
// URL de API y porcentaje de ganancia configurado, ordenada por ID ascendente.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200 OK: JSON con arreglo de proveedores incluyendo campo activo derivado del EstadoID
//   - HTTP 500 Internal Server Error: si ocurre un error de conexion o consulta
func (ctrl *AdminController) ListarProveedores(c *gin.Context) {
	conn, err := ctrl.db.Conn(context.Background())
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "error de conexión"})
		return
	}
	defer conn.Close()

	rows, err := conn.QueryContext(context.Background(), `
		SELECT ID, Nombre, Tipo_Proveedor_ID, URL_API, EstadoID, Porcentaje_Ganancia,
		       COALESCE(Imagen_Base64, '')
		FROM Proveedor
		ORDER BY ID
	`)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "error consultando proveedores"})
		return
	}
	defer rows.Close()

	type ProveedorAdmin struct {
		ID                 int     `json:"id"`
		Nombre             string  `json:"nombre"`
		TipoProveedorID    int     `json:"tipoProveedorId"`
		TipoNombre         string  `json:"tipoNombre"`
		URL                string  `json:"url"`
		EstadoID           int     `json:"estadoId"`
		Activo             bool    `json:"activo"`
		PorcentajeGanancia float64 `json:"porcentajeGanancia"`
		ImagenBase64       string  `json:"imagenBase64"`
	}

	tipoNombre := func(id int) string {
		if id == 1 {
			return "Aerolínea"
		}
		return "Hotel"
	}

	var lista []ProveedorAdmin
	for rows.Next() {
		var p ProveedorAdmin
		if err := rows.Scan(&p.ID, &p.Nombre, &p.TipoProveedorID,
			&p.URL, &p.EstadoID, &p.PorcentajeGanancia, &p.ImagenBase64); err != nil {
			continue
		}
		p.Activo = p.EstadoID == 1
		p.TipoNombre = tipoNombre(p.TipoProveedorID)
		lista = append(lista, p)
	}
	if lista == nil {
		lista = []ProveedorAdmin{}
	}
	c.JSON(http.StatusOK, lista)
}

// ToggleEstadoProveedor
//
// Activa o desactiva un proveedor segun el valor del campo activo recibido
// en el body. El ID del proveedor se lee desde el parametro de URL :id.
// EstadoID 1 equivale a activo, EstadoID 2 equivale a inactivo.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200 OK: JSON con mensaje de confirmacion
//   - HTTP 400 Bad Request: si el ID es invalido o el body no puede ser parseado
//   - HTTP 500 Internal Server Error: si ocurre un error de conexion o al ejecutar el UPDATE
func (ctrl *AdminController) ToggleEstadoProveedor(c *gin.Context) {
	id, err := strconv.Atoi(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "ID inválido"})
		return
	}

	var req struct {
		Activo bool `json:"activo"`
	}
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "datos inválidos"})
		return
	}

	estadoID := 2
	if req.Activo {
		estadoID = 1
	}

	conn, err := ctrl.db.Conn(context.Background())
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "error de conexión"})
		return
	}
	defer conn.Close()

	// Consultar nombre del proveedor para mensaje de auditoría más descriptivo
	var nombreProv string
	_ = conn.QueryRowContext(context.Background(),
		"SELECT Nombre FROM Proveedor WHERE ID = ?", id,
	).Scan(&nombreProv)

	_, err = conn.ExecContext(context.Background(),
		"UPDATE Proveedor SET EstadoID = ? WHERE ID = ?", estadoID, id)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "error actualizando estado"})
		return
	}

	// Log de cambio de estado de proveedor (ID 42)
	adminIDRaw, _ := c.Get("usuario_id")
	adminID, _ := adminIDRaw.(int)

	estadoTxt := "desactivó"
	if req.Activo {
		estadoTxt = "activó"
	}
	ctrl.logSesion.Registrar(c, helpers.TipoProveedorEstadoCambiado,
		&adminID, fmt.Sprintf("proveedor_id=%d", id),
		fmt.Sprintf("Admin %s proveedor '%s' (ID=%d)", estadoTxt, nombreProv, id))

	c.JSON(http.StatusOK, gin.H{"mensaje": "estado actualizado"})
}

// EditarProveedor
//
// Actualiza el nombre, URL de API y porcentaje de ganancia de un proveedor.
// El ID del proveedor se lee desde el parametro de URL :id.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200 OK: JSON con mensaje de confirmacion
//   - HTTP 400 Bad Request: si el ID es invalido o el body no puede ser parseado
//   - HTTP 500 Internal Server Error: si ocurre un error de conexion o al ejecutar el UPDATE
func (ctrl *AdminController) EditarProveedor(c *gin.Context) {
	id, err := strconv.Atoi(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "ID inválido"})
		return
	}

	var req struct {
		Nombre             string  `json:"nombre"`
		URL                string  `json:"url"`
		PorcentajeGanancia float64 `json:"porcentajeGanancia"`
		ImagenBase64       string  `json:"imagenBase64"`
	}
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "datos inválidos"})
		return
	}

	conn, err := ctrl.db.Conn(context.Background())
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "error de conexión"})
		return
	}
	defer conn.Close()

	_, err = conn.ExecContext(context.Background(),
		"UPDATE Proveedor SET Nombre = ?, URL_API = ?, Porcentaje_Ganancia = ?, Imagen_Base64 = ? WHERE ID = ?",
		req.Nombre, req.URL, req.PorcentajeGanancia, req.ImagenBase64, id)
	if err != nil {
		fmt.Printf("[EditarProveedor] error SQL: %v\n", err)
		c.JSON(http.StatusInternalServerError, gin.H{"error": "error actualizando proveedor", "detalle": err.Error()})
		return
	}

	// Log de edición de proveedor (ID 41) — cubre también cambios de % de ganancia
	adminIDRaw, _ := c.Get("usuario_id")
	adminID, _ := adminIDRaw.(int)

	ctrl.logSesion.Registrar(c, helpers.TipoProveedorEditado,
		&adminID, fmt.Sprintf("proveedor_id=%d", id),
		fmt.Sprintf("Admin editó proveedor ID=%d: nombre='%s' url='%s' ganancia=%.2f%%",
			id, req.Nombre, req.URL, req.PorcentajeGanancia))

	c.JSON(http.StatusOK, gin.H{"mensaje": "proveedor actualizado"})
}

// ReservacionesRecientes
//
// Retorna las ultimas 10 reservaciones realizadas en la plataforma por
// cualquier usuario, ordenadas por fecha de creacion descendente.
// Incluye nombre del usuario, tipo, total y estado de cada reservacion.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200 OK: JSON con arreglo de hasta 10 reservaciones recientes
//   - HTTP 500 Internal Server Error: si ocurre un error de conexion o consulta
func (ctrl *AdminController) ReservacionesRecientes(c *gin.Context) {
	conn, err := ctrl.db.Conn(context.Background())
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "error de conexión"})
		return
	}
	defer conn.Close()

	rows, err := conn.QueryContext(context.Background(), `
		SELECT r.ID, r.No_Reservacion, u.Nombre, u.Apellido,
		       r.Tipo_Reserva_ID, r.Total, r.Fecha_Creacion, r.EstadoID
		FROM Reservacion r
		JOIN Usuario u ON r.Usuario_ID = u.ID
		ORDER BY r.Fecha_Creacion DESC
		LIMIT 10
	`)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "error consultando reservaciones"})
		return
	}
	defer rows.Close()

	estadoNombre := func(id int) string {
		m := map[int]string{
			1: "pendiente", 2: "confirmada", 3: "cancelada",
			4: "expirada", 5: "completada", 6: "en curso",
		}
		if n, ok := m[id]; ok {
			return n
		}
		return "pendiente"
	}

	tipoNombreR := func(id int) string {
		switch id {
		case 1:
			return "vuelo"
		case 2:
			return "hotel"
		default:
			return "paquete"
		}
	}

	type ReservacionReciente struct {
		ID            int     `json:"id"`
		NoReservacion string  `json:"noReservacion"`
		Usuario       string  `json:"usuario"`
		Tipo          string  `json:"tipo"`
		Total         float64 `json:"totalReservacion"`
		FechaCreacion string  `json:"fechaReservacion"`
		Estado        string  `json:"estado"`
	}

	var lista []ReservacionReciente
	for rows.Next() {
		var r ReservacionReciente
		var nombre, apellido string
		var tipoID, estadoID int
		if err := rows.Scan(&r.ID, &r.NoReservacion, &nombre, &apellido,
			&tipoID, &r.Total, &r.FechaCreacion, &estadoID); err != nil {
			continue
		}
		r.Usuario = nombre + " " + apellido
		r.Tipo = tipoNombreR(tipoID)
		r.Estado = estadoNombre(estadoID)
		lista = append(lista, r)
	}
	if lista == nil {
		lista = []ReservacionReciente{}
	}
	c.JSON(http.StatusOK, lista)
}

// ObtenerMetricas
//
// Retorna el desglose financiero detallado de todas las reservaciones,
// separando el monto cobrado, el costo base y la ganancia por cada detalle
// (vuelo u hotel) segun el porcentaje de ganancia configurado en cada proveedor.
// Tambien incluye un resumen global agrupado por tipo de reservacion.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200 OK: JSON con campos resumen (totales globales y por tipo) y
//     reservaciones (lista detallada con desglose financiero individual)
//   - HTTP 500 Internal Server Error: si ocurre un error de conexion o consulta
//
// Notas:
//   - El costo base se calcula como: cobrado / (1 + porcentaje/100)
//   - La ganancia es la diferencia entre cobrado y base
func (ctrl *AdminController) ObtenerMetricas(c *gin.Context) {
	conn, err := ctrl.db.Conn(context.Background())
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "error de conexión"})
		return
	}
	defer conn.Close()

	rows, err := conn.QueryContext(context.Background(), `
		SELECT
			r.ID, r.No_Reservacion, u.Nombre, u.Apellido,
			r.Tipo_Reserva_ID, r.Fecha_Creacion, r.EstadoID,
			COALESCE(d.Tipo_Detalle_ID, 0)         AS TipoDetalle,
			COALESCE(d.Total, 0)                   AS TotalDetalle,
			COALESCE(p.Porcentaje_Ganancia, 0)     AS Porcentaje
		FROM Reservacion r
		JOIN Usuario u ON r.Usuario_ID = u.ID
		LEFT JOIN detalles_reservacion d ON d.Reservacion_ID = r.ID
		LEFT JOIN Proveedor p ON d.Proveedor_ID = p.ID
		ORDER BY r.Fecha_Creacion DESC, r.ID
	`)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "error consultando métricas"})
		return
	}
	defer rows.Close()

	// ── helpers ──────────────────────────────────────────────────────
	estadoN := func(id int) string {
		m := map[int]string{1: "pendiente", 2: "confirmada", 3: "cancelada", 4: "expirada", 5: "completada", 6: "en curso"}
		if v, ok := m[id]; ok { return v }
		return "pendiente"
	}
	tipoN := func(id int) string {
		switch id { case 1: return "vuelo"; case 2: return "hotel"; default: return "paquete" }
	}
	base := func(cobrado, pct float64) float64 {
		if pct == 0 { return cobrado }
		return cobrado / (1 + pct/100)
	}

	// ── acumuladores por reservación ─────────────────────────────────
	type DetalleFin struct {
		Cobrado    float64 `json:"cobrado"`
		Base       float64 `json:"base"`
		Ganancia   float64 `json:"ganancia"`
		Porcentaje float64 `json:"porcentaje"`
	}
	type ReservaFin struct {
		ID            int        `json:"id"`
		NoReservacion string     `json:"noReservacion"`
		Usuario       string     `json:"usuario"`
		TipoReserva   int        `json:"tipoReserva"`
		TipoNombre    string     `json:"tipoNombre"`
		FechaCreacion string     `json:"fechaCreacion"`
		Estado        string     `json:"estado"`
		TotalCobrado  float64    `json:"totalCobrado"`
		TotalBase     float64    `json:"totalBase"`
		TotalGanancia float64    `json:"totalGanancia"`
		Vuelo         *DetalleFin `json:"vuelo,omitempty"`
		Hotel         *DetalleFin `json:"hotel,omitempty"`
	}

	resMap   := map[int]*ReservaFin{}
	resOrder := []int{}

	for rows.Next() {
		var (
			id, tipoRes, estadoID, tipoDetalle int
			noRes, nombre, apellido, fecha     string
			totalDet, pct                      float64
		)
		if err := rows.Scan(&id, &noRes, &nombre, &apellido,
			&tipoRes, &fecha, &estadoID,
			&tipoDetalle, &totalDet, &pct); err != nil {
			continue
		}

		if _, ok := resMap[id]; !ok {
			resMap[id] = &ReservaFin{
				ID: id, NoReservacion: noRes,
				Usuario: nombre + " " + apellido,
				TipoReserva: tipoRes, TipoNombre: tipoN(tipoRes),
				FechaCreacion: fecha, Estado: estadoN(estadoID),
			}
			resOrder = append(resOrder, id)
		}

		if tipoDetalle == 0 || totalDet == 0 { continue }

		b := base(totalDet, pct)
		g := totalDet - b
		det := &DetalleFin{Cobrado: totalDet, Base: b, Ganancia: g, Porcentaje: pct}

		r := resMap[id]
		r.TotalCobrado += totalDet
		r.TotalBase    += b
		r.TotalGanancia += g

		if tipoDetalle == 1 { r.Vuelo = det }
		if tipoDetalle == 2 { r.Hotel = det }
	}

	// ── construir slice + resumen global ─────────────────────────────
	type TipoResumen struct {
		Cobrado  float64 `json:"cobrado"`
		Base     float64 `json:"base"`
		Ganancia float64 `json:"ganancia"`
		Cantidad int     `json:"cantidad"`
	}
	type Resumen struct {
		TotalCobrado  float64     `json:"totalCobrado"`
		TotalBase     float64     `json:"totalBase"`
		TotalGanancia float64     `json:"totalGanancia"`
		Vuelos        TipoResumen `json:"vuelos"`
		Hoteles       TipoResumen `json:"hoteles"`
		Paquetes      TipoResumen `json:"paquetes"`
	}

	var resumen Resumen
	lista := make([]*ReservaFin, 0, len(resOrder))

	for _, id := range resOrder {
		r := resMap[id]
		lista = append(lista, r)

		// Solo acumular ingresos de reservaciones confirmadas, completadas o en curso
		if r.Estado == "confirmada" || r.Estado == "completada" || r.Estado == "en curso" {
			resumen.TotalCobrado  += r.TotalCobrado
			resumen.TotalBase     += r.TotalBase
			resumen.TotalGanancia += r.TotalGanancia

			switch r.TipoReserva {
			case 1:
				resumen.Vuelos.Cobrado  += r.TotalCobrado
				resumen.Vuelos.Base     += r.TotalBase
				resumen.Vuelos.Ganancia += r.TotalGanancia
				resumen.Vuelos.Cantidad++
			case 2:
				resumen.Hoteles.Cobrado  += r.TotalCobrado
				resumen.Hoteles.Base     += r.TotalBase
				resumen.Hoteles.Ganancia += r.TotalGanancia
				resumen.Hoteles.Cantidad++
			case 3:
				resumen.Paquetes.Cobrado  += r.TotalCobrado
				resumen.Paquetes.Base     += r.TotalBase
				resumen.Paquetes.Ganancia += r.TotalGanancia
				resumen.Paquetes.Cantidad++
			}
		}
	}

	c.JSON(http.StatusOK, gin.H{
		"resumen":       resumen,
		"reservaciones": lista,
	})
}
