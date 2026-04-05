// # Package dto
//
// Contiene los Data Transfer Objects utilizados para la comunicacion
// entre la capa de transporte y la capa de servicio de la agencia de viajes.
package dto

// HotelProveedorDTO
//
// Representa la informacion basica de un hotel registrado
// en el catalogo de un proveedor hotelero externo.
type HotelProveedorDTO struct {
	ID     int    `json:"id"`     // ID unico del hotel en el sistema del proveedor
	Nombre string `json:"nombre"` // Nombre comercial del hotel
	Ciudad string `json:"ciudad"` // Ciudad donde se encuentra el hotel
	Pais   string `json:"pais"`   // Pais donde se encuentra el hotel
}
