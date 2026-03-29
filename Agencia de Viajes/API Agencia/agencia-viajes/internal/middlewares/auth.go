package middlewares

import (
	"agencia-viajes/internal/helpers"
	"net/http"

	"github.com/gin-gonic/gin"
)

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
