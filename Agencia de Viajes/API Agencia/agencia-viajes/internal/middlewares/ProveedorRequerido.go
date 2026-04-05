// # Package middlewares
//
// Contiene los middlewares HTTP de la aplicacion Movent.
// Provee funciones de verificacion de autenticacion JWT,
// control de roles y validacion de tokens de proveedor
// para proteger las rutas de la API REST.
package middlewares

import (
	"agencia-viajes/internal/repositories"
	"database/sql"
	"net/http"

	"github.com/gin-gonic/gin"
)

// ProveedorRequerido
//
// Retorna un middleware de Gin que valida el token de proveedor
// enviado en el encabezado HTTP X-Proveedor-Token. Busca el proveedor
// correspondiente en la base de datos mediante el repositorio. Si el
// token esta ausente, no se encuentra o produce un error, rechaza la
// solicitud con HTTP 401 o 500 segun corresponda. Si es valido, inyecta
// proveedor_id, proveedor_nombre y proveedor_tipo en el contexto de Gin.
//
// Parametros:
//   - db: conexion activa a la base de datos para consultar el proveedor
//
// Retorna:
//   - gin.HandlerFunc: funcion de middleware lista para usar con router.Use
func ProveedorRequerido(db *sql.DB) gin.HandlerFunc {
	repo := repositories.NewProveedorRepository(db)

	return func(c *gin.Context) {
		token := c.GetHeader("X-Proveedor-Token")
		if token == "" {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "token de proveedor requerido"})
			c.Abort()
			return
		}

		proveedor, err := repo.ObtenerProveedorPorToken(token)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "error validando token"})
			c.Abort()
			return
		}
		if proveedor == nil {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "token inválido — proveedor no reconocido"})
			c.Abort()
			return
		}

		// Inyectar identidad para usarla en el controller
		c.Set("proveedor_id", proveedor.ID)
		c.Set("proveedor_nombre", proveedor.Nombre)
		c.Set("proveedor_tipo", proveedor.TipoProveedorID)
		c.Next()
	}
}
