// # Package controllers
//
// Controladores HTTP de la agencia de viajes. Cada controlador recibe
// solicitudes de Gin, delega la logica de negocio al servicio correspondiente
// y devuelve la respuesta JSON al cliente.
package controllers

import (
	"agencia-viajes/internal/repositories"
	"database/sql"
	"net/http"

	"github.com/gin-gonic/gin"
)

// ConfiguracionController
//
// Controlador encargado de exponer la configuracion global de la agencia,
// incluyendo el porcentaje de descuento aplicado a reservaciones de tipo paquete.
type ConfiguracionController struct {
	repo *repositories.AgenciaConfiguracionRepository
}

// NewConfiguracionController
//
// Crea e inicializa un nuevo ConfiguracionController con la conexion a la base de datos.
//
// Parametros:
//   - db: conexion activa a la base de datos
//
// Retorna:
//   - *ConfiguracionController: puntero al controlador creado
func NewConfiguracionController(db *sql.DB) *ConfiguracionController {
	return &ConfiguracionController{
		repo: repositories.NewAgenciaConfiguracionRepository(db),
	}
}

// ObtenerDescuento
//
// Handler HTTP publico que retorna el porcentaje de descuento configurado
// para reservaciones de tipo paquete. Si la consulta falla retorna 0.
//
// Retorna:
//   - HTTP 200: JSON con el campo porcentaje_descuento
//
// Notas:
//   - Ruta esperada: GET /api/configuracion/descuento
func (ctrl *ConfiguracionController) ObtenerDescuento(c *gin.Context) {
	porcentaje, _ := ctrl.repo.ObtenerPorcentajeDescuento()
	c.JSON(http.StatusOK, gin.H{"porcentaje_descuento": porcentaje})
}