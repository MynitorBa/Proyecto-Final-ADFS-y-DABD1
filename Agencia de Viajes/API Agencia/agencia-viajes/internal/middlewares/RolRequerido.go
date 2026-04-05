// # Package middlewares
//
// Contiene los middlewares HTTP de la aplicacion Movent.
// Provee funciones de verificacion de autenticacion JWT,
// control de roles y validacion de tokens de proveedor
// para proteger las rutas de la API REST.
package middlewares

import (
	"net/http"

	"github.com/gin-gonic/gin"
)

// RolRequerido
//
// Retorna un middleware de Gin que verifica que el usuario autenticado
// tenga uno de los roles permitidos indicados como argumentos. Lee el
// rol del contexto de Gin (previamente inyectado por AuthRequerido) y
// compara con los roles aceptados. Si el rol no coincide con ninguno,
// rechaza la solicitud con HTTP 403.
//
// Parametros:
//   - roles: uno o mas identificadores de rol que tienen acceso permitido
//
// Retorna:
//   - gin.HandlerFunc: funcion de middleware lista para usar con router.Use
//
// Notas:
//   - Debe usarse siempre despues de AuthRequerido en la cadena de middlewares
func RolRequerido(roles ...int) gin.HandlerFunc {
	return func(c *gin.Context) {
		rolID, exists := c.Get("rol_id")
		if !exists {
			c.JSON(http.StatusForbidden, gin.H{"error": "acceso denegado"})
			c.Abort()
			return
		}

		for _, r := range roles {
			if rolID.(int) == r {
				c.Next()
				return
			}
		}

		c.JSON(http.StatusForbidden, gin.H{"error": "no tienes permiso para realizar esta acción"})
		c.Abort()
	}
}
