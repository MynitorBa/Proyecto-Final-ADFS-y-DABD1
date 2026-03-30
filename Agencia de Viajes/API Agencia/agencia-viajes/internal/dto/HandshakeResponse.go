package dto

type HandshakeResponse struct {
	Mensaje     string `json:"mensaje"`
	TokenSalida string `json:"token_salida"` // el que devuelve la aerolinea
}
