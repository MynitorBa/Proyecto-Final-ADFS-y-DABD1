package dto

type LoginRequest struct {
	Login      string `json:"login"`
	Contrasena string `json:"contrasena"`
}

type LoginResponse struct {
	ID       int    `json:"id"`
	Nombre   string `json:"nombre"`
	Apellido string `json:"apellido"`
	Correo   string `json:"correo"`
	Username string `json:"username"`
	RolID    int    `json:"rol_id"`
}
