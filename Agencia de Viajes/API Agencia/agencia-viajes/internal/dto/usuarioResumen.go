// # Package dto
//
// Contiene los Data Transfer Objects utilizados para la comunicacion
// entre la capa de transporte y la capa de servicio de la agencia de viajes.
package dto

// UsuarioResumen
//
// Representa los datos basicos de un usuario retornados por el panel
// de administracion para gestion de roles y asignacion de WebService.
type UsuarioResumen struct {
	ID       int    `json:"id"`       // ID unico del usuario en la base de datos
	Nombre   string `json:"nombre"`   // Nombre del usuario
	Apellido string `json:"apellido"` // Apellido del usuario
	Correo   string `json:"correo"`   // Correo electronico del usuario
	RolID    int    `json:"rolId"`    // ID del rol asignado al usuario
	Rol      string `json:"rol"`      // Nombre del rol asignado al usuario
}