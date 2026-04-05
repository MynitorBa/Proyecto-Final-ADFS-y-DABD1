// # Package dto
//
// Contiene los Data Transfer Objects utilizados para la comunicacion
// entre la capa de transporte y la capa de servicio de la agencia de viajes.
package dto

// AgregarPasajerosVueloRequest
//
// Representa la solicitud recibida por el endpoint de la agencia
// para agregar los datos de pasajeros a los boletos de un vuelo reservado.
type AgregarPasajerosVueloRequest struct {
	ReservacionID int                `json:"reservacion_id"` // ID de la reservacion en la base de datos de la agencia
	ProveedorID   int                `json:"proveedor_id"`   // ID del proveedor aerolinea
	Pasajeros     []PasajeroVueloDTO `json:"pasajeros"`      // Lista de pasajeros con sus datos personales
}

// PasajeroVueloDTO
//
// Representa los datos personales de un pasajero asociado
// a un boleto especifico dentro de una reservacion de vuelo.
type PasajeroVueloDTO struct {
	BoletoID  int    `json:"boletoId"`  // ID del boleto devuelto por la aerolinea
	Nombre    string `json:"nombre"`    // Nombre del pasajero
	Apellido  string `json:"apellido"`  // Apellido del pasajero
	Pasaporte string `json:"pasaporte"` // Numero de pasaporte (solo digitos)
	Telefono  string `json:"telefono"`  // Numero de telefono de contacto
	Pais      string `json:"pais"`      // Pais de residencia del pasajero
	Ciudad    string `json:"ciudad"`    // Ciudad de residencia del pasajero
}

// AgregarPasajerosVueloAerolineaBody
//
// Representa el cuerpo de la solicitud que se envia directamente
// a la API de la aerolinea para registrar los datos de pasajeros
// en la reservacion del proveedor.
type AgregarPasajerosVueloAerolineaBody struct {
	ReservacionID int                `json:"reservacionId"` // ID de la reservacion en el sistema de la aerolinea
	Pasajeros     []PasajeroVueloDTO `json:"pasajeros"`     // Lista de pasajeros con sus datos personales
}
