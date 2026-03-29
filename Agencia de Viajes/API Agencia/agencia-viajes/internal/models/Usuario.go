package models

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
