package middlewares

import (
	"agencia-viajes/internal/repositories"
	"database/sql"
	"net/http"

	"github.com/gin-gonic/gin"
)

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
