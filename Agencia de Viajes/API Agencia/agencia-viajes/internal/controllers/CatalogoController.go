// # Package controllers
//
// Controladores HTTP de la agencia de viajes. Cada controlador recibe
// solicitudes de Gin, delega la logica de negocio al servicio correspondiente
// y devuelve la respuesta JSON al cliente.
package controllers

import (
	"agencia-viajes/internal/helpers"
	"agencia-viajes/internal/services"
	"fmt"
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
)

// CatalogoController
//
// Controlador encargado de gestionar la actualizacion del catalogo de productos
// (vuelos, hoteles y paquetes) obtenidos desde los proveedores externos.
// Registra el resultado en log_sesion para trazabilidad de operaciones de admin.
type CatalogoController struct {
	service   *services.CatalogoService
	logSesion *services.LogSesionService
}

// NewCatalogoController
//
// Crea e inicializa un nuevo CatalogoController con el servicio recibido.
//
// Parametros:
//   - service:   instancia del servicio de catalogo
//   - logSesion: instancia del servicio de auditoria de sesion
//
// Retorna:
//   - *CatalogoController: puntero al controlador creado
func NewCatalogoController(service *services.CatalogoService, logSesion *services.LogSesionService) *CatalogoController {
	return &CatalogoController{service: service, logSesion: logSesion}
}

// ActualizarCatalogo
//
// Handler HTTP que dispara el proceso de actualizacion del catalogo consultando
// los proveedores registrados y sincronizando la informacion en la base de datos.
// Registra CATALOGO_ACTUALIZADO_EXITOSO (ID 37) si todos los proveedores
// respondieron correctamente, o CATALOGO_ACTUALIZADO_FALLIDO (ID 38) si al
// menos uno fallo.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200: mensaje de proceso completado junto con el detalle de resultados por proveedor
//   - HTTP 500: error interno si el servicio falla durante la actualizacion
//
// Notas:
//   - Solo accesible por administradores (RolRequerido(2))
//   - Un fallo parcial en uno o mas proveedores retorna 200 con errores en el array de resultados
func (ctrl *CatalogoController) ActualizarCatalogo(c *gin.Context) {
	// Obtener usuario_id del admin para el log (inyectado por AuthRequerido)
	uidRaw, _ := c.Get("usuario_id")
	uidInt := 0
	if u, ok := uidRaw.(int); ok {
		uidInt = u
	}

	resultados, err := ctrl.service.ActualizarCatalogo()
	if err != nil {
		// Log de fallo critico en la consulta inicial de proveedores (ID 38)
		ctrl.logSesion.Registrar(c, helpers.TipoCatalogoActualizadoFallido,
			&uidInt, "catalogo",
			fmt.Sprintf("Error al obtener proveedores activos: %s", err.Error()))

		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	// Analizar resultados: contar insertados y detectar proveedores con error
	totalInsertados := 0
	proveedoresConError := []string{}
	for _, r := range resultados {
		totalInsertados += r.Insertados
		if strings.Contains(strings.ToLower(r.Mensaje), "error") {
			proveedoresConError = append(proveedoresConError, r.Proveedor)
		}
	}

	if len(proveedoresConError) == 0 {
		// Todos los proveedores respondieron correctamente (ID 37)
		ctrl.logSesion.Registrar(c, helpers.TipoCatalogoActualizadoExitoso,
			&uidInt, "catalogo",
			fmt.Sprintf("Catálogo actualizado. %d registros insertados en %d proveedores",
				totalInsertados, len(resultados)))
	} else {
		// Al menos un proveedor falló (ID 38)
		ctrl.logSesion.Registrar(c, helpers.TipoCatalogoActualizadoFallido,
			&uidInt, "catalogo",
			fmt.Sprintf("Catálogo parcialmente actualizado. %d insertados. Errores en: %s",
				totalInsertados, strings.Join(proveedoresConError, ", ")))
	}

	c.JSON(http.StatusOK, gin.H{
		"mensaje":    "proceso de actualización completado",
		"resultados": resultados,
	})
}
