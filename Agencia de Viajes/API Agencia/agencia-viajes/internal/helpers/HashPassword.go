// # Package helpers
//
// Provee funciones auxiliares reutilizables para tareas comunes de la
// aplicacion Movent: generacion de tokens, hashing de contrasenas,
// manejo de sesiones JWT, envio de correos electronicos y generacion
// de documentos PDF.
package helpers

import "golang.org/x/crypto/bcrypt"

// HashPassword
//
// Genera el hash bcrypt de la contrasena en texto plano proporcionada,
// usando el costo por defecto de la libreria bcrypt.
//
// Parametros:
//   - password: contrasena en texto plano a hashear
//
// Retorna:
//   - string: hash bcrypt resultante
//   - error: error si la generacion del hash falla
func HashPassword(password string) (string, error) {
	bytes, err := bcrypt.GenerateFromPassword([]byte(password), bcrypt.DefaultCost)
	return string(bytes), err
}

// CheckPassword
//
// Compara una contrasena en texto plano contra su hash bcrypt almacenado.
// Retorna verdadero si coinciden, falso en cualquier otro caso.
//
// Parametros:
//   - password: contrasena en texto plano ingresada por el usuario
//   - hash: hash bcrypt almacenado en la base de datos
//
// Retorna:
//   - bool: true si la contrasena es valida, false si no coincide
func CheckPassword(password, hash string) bool {
	err := bcrypt.CompareHashAndPassword([]byte(hash), []byte(password))
	return err == nil
}
