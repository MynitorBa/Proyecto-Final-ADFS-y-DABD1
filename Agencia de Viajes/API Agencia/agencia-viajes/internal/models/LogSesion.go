// # Package models
//
// Define las estructuras de dominio utilizadas en toda la aplicacion
// Movent para representar las entidades de la base de datos.
package models

import "database/sql"

// LogSesion
//
// Representa un registro de evento de sesion almacenado en la tabla
// log_sesion. Contiene informacion sobre el tipo de evento ocurrido,
// el usuario involucrado (si aplica), los datos de red del cliente
// y un mensaje descriptivo opcional.
//
// Notas:
//   - UsuarioID es nullable porque en eventos fallidos puede no
//     conocerse el usuario (ej: credenciales incorrectas)
//   - LoginIntentado guarda el valor enviado en el campo login del
//     request (username o correo) para auditar intentos fallidos
//   - Fecha se omite en el INSERT; MySQL aplica DEFAULT current_timestamp()
type LogSesion struct {
	ID             int64
	TipoEventoID   int
	UsuarioID      sql.NullInt64
	LoginIntentado sql.NullString
	Exitoso        bool
	IPOrigen       sql.NullString
	UserAgent      sql.NullString
	Mensaje        sql.NullString
}
