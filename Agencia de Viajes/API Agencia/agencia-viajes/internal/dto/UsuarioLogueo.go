// # Package dto
//
// Contiene los Data Transfer Objects utilizados para la comunicacion
// entre la capa de transporte y la capa de servicio de la agencia de viajes.
package dto

// LoginRequest
//
// Representa las credenciales enviadas por el usuario
// para iniciar sesion en el sistema de la agencia.
type LoginRequest struct {
	Login      string `json:"login"`      // Correo electronico o nombre de usuario
	Contrasena string `json:"contrasena"` // Contrasena del usuario
}

// LoginResponse
//
// Representa los datos del usuario autenticado que se retornan
// al cliente tras un inicio de sesion exitoso.
type LoginResponse struct {
	ID       int    `json:"id"`       // ID unico del usuario en la base de datos
	Nombre   string `json:"nombre"`   // Nombre del usuario
	Apellido string `json:"apellido"` // Apellido del usuario
	Correo   string `json:"correo"`   // Correo electronico del usuario
	Username string `json:"username"` // Nombre de usuario unico
	RolID    int    `json:"rol_id"`   // ID del rol asignado al usuario
}
