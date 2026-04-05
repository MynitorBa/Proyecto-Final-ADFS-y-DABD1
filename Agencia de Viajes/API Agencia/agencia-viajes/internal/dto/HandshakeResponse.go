// # Package dto
//
// Contiene los Data Transfer Objects utilizados para la comunicacion
// entre la capa de transporte y la capa de servicio de la agencia de viajes.
package dto

// HandshakeResponse
//
// Representa la respuesta recibida del proveedor externo
// durante el proceso de handshake de autenticacion,
// que incluye el token de sesion devuelto por la aerolinea.
type HandshakeResponse struct {
	Mensaje     string `json:"mensaje"`      // Mensaje descriptivo del resultado del handshake
	TokenSalida string `json:"token_salida"` // Token de autenticacion devuelto por la aerolinea para sesiones posteriores
}
