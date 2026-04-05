// # Package helpers
//
// Provee funciones auxiliares reutilizables para tareas comunes de la
// aplicacion Movent: generacion de tokens, hashing de contrasenas,
// manejo de sesiones JWT, envio de correos electronicos y generacion
// de documentos PDF.
package helpers

import (
	"crypto/rand"
	"crypto/sha256"
	"encoding/hex"
)

// GenerarTokenHash
//
// Genera un token aleatorio seguro de 32 bytes y retorna su hash
// SHA-256 codificado en hexadecimal. Se usa para crear tokens de
// autenticacion o identificacion de proveedores.
//
// Retorna:
//   - string: cadena hexadecimal de 64 caracteres con el hash SHA-256
//   - error: error si la lectura de bytes aleatorios falla
func GenerarTokenHash() (string, error) {
	bytes := make([]byte, 32)
	if _, err := rand.Read(bytes); err != nil {
		return "", err
	}
	hash := sha256.Sum256(bytes)
	return hex.EncodeToString(hash[:]), nil
}
