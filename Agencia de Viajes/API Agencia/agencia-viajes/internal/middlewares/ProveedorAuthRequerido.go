// # Package middlewares
//
// Contiene los middlewares HTTP de la aplicacion Movent.
// Provee funciones de verificacion de autenticacion JWT,
// control de roles y validacion de tokens de proveedor
// para proteger las rutas de la API REST.
package middlewares

import (
	"context"
	"database/sql"
	"net/http"

	"github.com/gin-gonic/gin"
)

// ProveedorAuthRequerido
//
// Retorna un middleware de Gin que autentica solicitudes entrantes
// provenientes de proveedores externos (aerolineas y hoteleras).
//
// La autenticacion se basa en el header "X-Agencia-Token", cuyo valor
// es comparado contra el campo Token_HASH_Salida de la tabla proveedor.
// El proveedor envia este token en cada callback a la agencia para autenticarse.
// Si el token coincide con un proveedor activo, se inyectan en el contexto
// de Gin los datos del proveedor para que los controladores puedan usarlos
// sin necesidad de volver a consultar la base de datos.
//
// Parametros:
//   - db: conexion activa a la base de datos MySQL
//
// Retorna:
//   - gin.HandlerFunc: funcion de middleware lista para usar con router.Use
//
// Codigos HTTP de respuesta:
//   - 401: header ausente, token no encontrado, o proveedor inactivo
//   - 500: error inesperado al consultar la base de datos
//
// Claves inyectadas en el contexto de Gin si la autenticacion es exitosa:
//   - "proveedor_id"     (int)    ID del proveedor en la tabla proveedor
//   - "proveedor_tipo"   (int)    ID del tipo: 1=Aerolinea, 2=Hotelera
//   - "proveedor_nombre" (string) Nombre del proveedor
func ProveedorAuthRequerido(db *sql.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		// 1. Leer el token del header X-Agencia-Token
		token := c.GetHeader("X-Agencia-Token")
		if token == "" {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "Token de proveedor ausente"})
			c.Abort()
			return
		}

		// 2. Buscar el proveedor por Token_HASH_Salida en la BD
		//    Solo se considera valido si el proveedor esta Activo (EstadoID = 1)
		var (
			proveedorID     int
			proveedorTipo   int
			proveedorNombre string
			estadoID        int
		)

		err := db.QueryRowContext(
			context.Background(),
			`SELECT ID, Tipo_Proveedor_ID, Nombre, EstadoID
			 FROM proveedor
			 WHERE Token_HASH_Salida = ?
			 LIMIT 1`,
			token,
		).Scan(&proveedorID, &proveedorTipo, &proveedorNombre, &estadoID)

		if err == sql.ErrNoRows {
			// Token no corresponde a ningun proveedor registrado
			c.JSON(http.StatusUnauthorized, gin.H{"error": "Token de proveedor inválido"})
			c.Abort()
			return
		}
		if err != nil {
			// Error inesperado de base de datos
			c.JSON(http.StatusInternalServerError, gin.H{"error": "Error al validar token de proveedor"})
			c.Abort()
			return
		}

		// 3. Verificar que el proveedor este activo (EstadoID = 1)
		if estadoID != 1 {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "Proveedor inactivo"})
			c.Abort()
			return
		}

		// 4. Inyectar datos del proveedor en el contexto para los controladores
		c.Set("proveedor_id", proveedorID)
		c.Set("proveedor_tipo", proveedorTipo)
		c.Set("proveedor_nombre", proveedorNombre)

		c.Next()
	}
}
