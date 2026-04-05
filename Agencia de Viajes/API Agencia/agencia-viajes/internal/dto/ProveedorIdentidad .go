// # Package dto
//
// Contiene los Data Transfer Objects utilizados para la comunicacion
// entre la capa de transporte y la capa de servicio de la agencia de viajes.
package dto

// ProveedorIdentidad
//
// Representa los datos de identificacion minimos de un proveedor,
// utilizados para reconocer y clasificar al proveedor en operaciones
// internas sin cargar todos sus datos completos.
type ProveedorIdentidad struct {
	ID              int    `json:"id"`               // ID unico del proveedor en la base de datos de la agencia
	Nombre          string `json:"nombre"`           // Nombre comercial del proveedor
	TipoProveedorID int    `json:"tipo_proveedor_id"` // ID del tipo de proveedor (aerolinea u hotel)
}
