// # Package models
//
// Define las estructuras de dominio utilizadas en toda la aplicacion
// Movent para representar las entidades de la base de datos.
package models

// Usuario
//
// Representa un usuario registrado en la plataforma Movent.
// Contiene datos personales, credenciales de acceso y referencias
// a su ciudad, rol y estado dentro del sistema.
//
// Notas:
//   - El campo Contrasena usa la etiqueta json:"-" para evitar
//     que sea serializado en las respuestas HTTP
type Usuario struct {
	ID              int    `json:"id"`
	Nombre          string `json:"nombre"`
	Apellido        string `json:"apellido"`
	Correo          string `json:"correo"`
	Username        string `json:"username"`
	Contrasena      string `json:"-"`
	Pasaporte       string `json:"pasaporte"`
	Telefono        string `json:"telefono"`
	FechaNacimiento string `json:"fecha_nacimiento"`
	CiudadID        int    `json:"ciudad_id"`
	RolID           int    `json:"rol_id"`
	EstadoID        int    `json:"estado_id"`
}
