package dto

type RegistroUsuarioRequest struct {
	Nombre          string   `json:"nombre"`
	Apellido        string   `json:"apellido"`
	Correo          string   `json:"correo"`
	Username        string   `json:"username"`
	Contrasena      string   `json:"contrasena"`
	Pasaporte       string   `json:"pasaporte"`
	Telefono        string   `json:"telefono"`
	FechaNacimiento string   `json:"fecha_nacimiento"`
	Ciudad          string   `json:"ciudad"`
	Pais            string   `json:"pais"`
	Nacionalidades  []string `json:"nacionalidades"`
}

type ValidacionUsuarioResponse struct {
	Correo    bool `json:"correo"`
	Pasaporte bool `json:"pasaporte"`
	Username  bool `json:"username"`
}
