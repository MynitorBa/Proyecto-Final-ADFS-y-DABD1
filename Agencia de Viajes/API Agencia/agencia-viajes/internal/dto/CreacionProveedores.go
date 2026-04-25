// # Package dto
//
// Contiene los Data Transfer Objects utilizados para la comunicacion
// entre la capa de transporte y la capa de servicio de la agencia de viajes.
package dto

// CrearProveedorRequest
//
// Representa los datos necesarios para registrar un nuevo proveedor
// en el sistema de la agencia. El campo UsuarioID debe corresponder
// a un usuario con rol 3 (administrador de proveedor).
type CrearProveedorRequest struct {
	Nombre             string  `json:"nombre" binding:"required"`             // Nombre comercial del proveedor
	TipoProveedorID    int     `json:"tipo_proveedor_id" binding:"required"`  // ID del tipo de proveedor (aerolinea u hotel)
	URLAPI             string  `json:"url_api"`                               // URL base de la API del proveedor
	UsuarioID          int     `json:"usuario_id" binding:"required"`         // ID del usuario vinculado; debe tener rol 3
	PorcentajeGanancia float64 `json:"porcentaje_ganancia"`                   // Porcentaje de ganancia aplicado sobre el precio base
	ImagenBase64       string  `json:"imagenBase64"`                          // Imagen del proveedor codificada en Base64
}

// CrearProveedorResponse
//
// Representa los datos del proveedor recien creado que se retornan
// al cliente tras el registro exitoso en el sistema.
type CrearProveedorResponse struct {
	ID                 int     `json:"id"`                  // ID generado para el nuevo proveedor
	Nombre             string  `json:"nombre"`              // Nombre comercial del proveedor
	TipoProveedorID    int     `json:"tipo_proveedor_id"`   // ID del tipo de proveedor registrado
	URLAPI             string  `json:"url_api"`             // URL base de la API del proveedor
	UsuarioID          int     `json:"usuario_id"`          // ID del usuario vinculado al proveedor
	EstadoID           int     `json:"estado_id"`           // ID del estado inicial del proveedor
	PorcentajeGanancia float64 `json:"porcentaje_ganancia"` // Porcentaje de ganancia configurado
	ImagenBase64       string  `json:"imagenBase64"`        // Imagen del proveedor codificada en Base64
}
