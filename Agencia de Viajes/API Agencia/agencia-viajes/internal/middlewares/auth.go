// # Package middlewares
//
// Contiene los middlewares HTTP de la aplicacion Movent.
// Provee funciones de verificacion de autenticacion JWT,
// control de roles y validacion de tokens de proveedor
// para proteger las rutas de la API REST.
package middlewares

import (
	"agencia-viajes/internal/helpers"
	"net/http"

	"github.com/gin-gonic/gin"
)

// AuthRequerido
//
// Retorna un middleware de Gin que verifica que la solicitud incluya
// una cookie de sesion con un JWT valido. Si la cookie no existe o el
// token es invalido o esta expirado, la solicitud es rechazada con
// HTTP 401. Si la autenticacion es exitosa, inyecta los datos del
// usuario (usuario_id, username, rol_id) en el contexto de Gin para
// que los controladores puedan usarlos.
//
// Retorna:
//   - gin.HandlerFunc: funcion de middleware lista para usar con router.Use
func AuthRequerido() gin.HandlerFunc {
	return func(c *gin.Context) {
		tokenStr, err := c.Cookie("session")
		if err != nil {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "No autorizado"})
			c.Abort()
			return
		}

		claims, err := helpers.VerificarToken(tokenStr)
		if err != nil {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "Sesión inválida o expirada"})
			c.Abort()
			return
		}

		// Poner datos en el contexto para usarlos en los controllers
		c.Set("usuario_id", claims.UsuarioID)
		c.Set("username", claims.Username)
		c.Set("rol_id", claims.RolID)

		c.Next()
	}
}
