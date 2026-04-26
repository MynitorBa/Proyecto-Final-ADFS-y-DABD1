// # Package dto
//
// Contiene los Data Transfer Objects utilizados para la comunicacion
// entre la capa de transporte y la capa de servicio de la agencia de viajes.
package dto

// RegistroUsuarioRequest
//
// Representa los datos enviados por un nuevo usuario
// para crear una cuenta en el sistema de la agencia de viajes.
type RegistroUsuarioRequest struct {
	Nombre          string   `json:"nombre"`           // Nombre del usuario
	Apellido        string   `json:"apellido"`         // Apellido del usuario
	Correo          string   `json:"correo"`           // Correo electronico del usuario
	Username        string   `json:"username"`         // Nombre de usuario unico
	Contrasena      string   `json:"contrasena"`       // Contrasena de acceso al sistema
	Pasaporte       string   `json:"pasaporte"`        // Numero de pasaporte del usuario
	Telefono        string   `json:"telefono"`         // Numero de telefono de contacto
	FechaNacimiento string   `json:"fecha_nacimiento"` // Fecha de nacimiento en formato ISO 8601
	Ciudad          string   `json:"ciudad"`           // Ciudad de residencia del usuario
	Pais            string   `json:"pais"`             // Pais de residencia del usuario
	Nacionalidades  []string `json:"nacionalidades"`   // Lista de nacionalidades del usuario
	RecibirOfertas  bool     `json:"recibir_ofertas"`  // Suscripcion a ofertas por correo cada 5 dias
}

// ValidacionUsuarioResponse
//
// Representa el resultado de la validacion de unicidad de los datos
// de un usuario durante el proceso de registro, indicando si
// el correo, pasaporte o username ya existen en el sistema.
type ValidacionUsuarioResponse struct {
	Correo    bool `json:"correo"`    // Indica si el correo ya esta registrado en el sistema
	Pasaporte bool `json:"pasaporte"` // Indica si el pasaporte ya esta registrado en el sistema
	Username  bool `json:"username"`  // Indica si el nombre de usuario ya esta registrado en el sistema
}
