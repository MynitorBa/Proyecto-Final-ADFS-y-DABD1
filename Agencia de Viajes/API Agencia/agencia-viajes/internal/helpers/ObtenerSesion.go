// # Package helpers
//
// Provee funciones auxiliares reutilizables para tareas comunes de la
// aplicacion Movent: generacion de tokens, hashing de contrasenas,
// manejo de sesiones JWT, envio de correos electronicos y generacion
// de documentos PDF.
package helpers

import (
	"errors"
	"os"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v5"
)

// Claims
//
// Estructura de los claims personalizados incluidos en el JWT de sesion.
// Extiende jwt.RegisteredClaims con los datos de identidad del usuario
// necesarios para autorizacion en los middlewares y controladores.
type Claims struct {
	UsuarioID int    `json:"usuario_id"`
	Username  string `json:"username"`
	RolID     int    `json:"rol_id"`
	jwt.RegisteredClaims
}

// GenerarToken
//
// Crea y firma un JWT HS256 con los datos del usuario autenticado.
// El token tiene una vigencia de 24 horas a partir del momento
// de su emision y se firma con la clave JWT_SECRET del entorno.
//
// Parametros:
//   - usuarioID: identificador unico del usuario en la base de datos
//   - username: nombre de usuario para incluir en los claims
//   - rolID: identificador del rol del usuario para control de acceso
//
// Retorna:
//   - string: token JWT firmado listo para enviar al cliente
//   - error: error si la firma del token falla
func GenerarToken(usuarioID int, username string, rolID int) (string, error) {
	claims := Claims{
		UsuarioID: usuarioID,
		Username:  username,
		RolID:     rolID,
		RegisteredClaims: jwt.RegisteredClaims{
			ExpiresAt: jwt.NewNumericDate(time.Now().Add(24 * time.Hour)),
			IssuedAt:  jwt.NewNumericDate(time.Now()),
		},
	}

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	return token.SignedString([]byte(os.Getenv("JWT_SECRET")))
}

// VerificarToken
//
// Parsea y valida un JWT firmado con HS256. Verifica que el metodo
// de firma sea HMAC, que la firma sea correcta y que el token no
// haya expirado, retornando los claims si todo es valido.
//
// Parametros:
//   - tokenStr: cadena JWT recibida desde el cliente
//
// Retorna:
//   - *Claims: puntero a los claims extraidos del token valido
//   - error: error si el metodo de firma es invalido, la firma no
//     coincide, el token esta expirado o los claims no son validos
func VerificarToken(tokenStr string) (*Claims, error) {
	token, err := jwt.ParseWithClaims(tokenStr, &Claims{}, func(t *jwt.Token) (interface{}, error) {
		if _, ok := t.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, errors.New("método de firma inválido")
		}
		return []byte(os.Getenv("JWT_SECRET")), nil
	})

	if err != nil {
		return nil, err
	}

	claims, ok := token.Claims.(*Claims)
	if !ok || !token.Valid {
		return nil, errors.New("token inválido")
	}

	return claims, nil
}

// ExtraerUsuarioIDDeCookie
//
// Intenta extraer el ID y username del usuario desde la cookie de sesion
// sin propagar errores. Si la cookie no existe, esta expirada o es invalida,
// retorna ceros y cadena vacia. Disenada para usarse en el logout donde
// la ausencia de sesion no es un error fatal sino un evento a registrar.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - usuarioID: ID del usuario extraido del JWT, o 0 si no hay sesion valida
//   - username: nombre de usuario extraido del JWT, o cadena vacia si no hay sesion valida
func ExtraerUsuarioIDDeCookie(c *gin.Context) (usuarioID int, username string) {
	tokenStr, err := c.Cookie("session")
	if err != nil {
		return 0, ""
	}
	claims, err := VerificarToken(tokenStr)
	if err != nil {
		return 0, ""
	}
	return claims.UsuarioID, claims.Username
}
