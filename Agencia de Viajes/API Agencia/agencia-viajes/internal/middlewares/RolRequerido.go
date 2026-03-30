package middlewares

import (
	"net/http"

	"github.com/gin-gonic/gin"
)

// RolRequerido verifica que el usuario autenticado tenga uno de los roles permitidos
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
