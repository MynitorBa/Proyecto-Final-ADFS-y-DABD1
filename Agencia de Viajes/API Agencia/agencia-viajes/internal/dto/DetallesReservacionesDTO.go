// # Package dto
//
// Contiene los Data Transfer Objects utilizados para la comunicacion
// entre la capa de transporte y la capa de servicio de la agencia de viajes.
package dto

// AgregarDetalleVueloRequest
//
// Representa la solicitud para agregar uno o varios vuelos
// seleccionados como detalle dentro de una reservacion existente.
type AgregarDetalleVueloRequest struct {
	ReservacionID int              `json:"reservacion_id"` // ID de la reservacion a la que se agrega el detalle
	ProveedorID   int              `json:"proveedor_id"`   // ID del proveedor aerolinea
	Vuelos        []SeleccionVuelo `json:"vuelos"`         // Lista de vuelos seleccionados con clase y cantidad de pasajeros
}

// SeleccionVuelo
//
// Representa un vuelo especifico elegido por el usuario,
// junto con la clase de servicio y la cantidad de pasajeros.
type SeleccionVuelo struct {
	VueloId           int `json:"vueloId"`           // ID del vuelo en el catalogo del proveedor
	ClaseId           int `json:"claseId"`           // ID de la clase de servicio seleccionada
	CantidadPasajeros int `json:"cantidadPasajeros"` // Numero de pasajeros para este vuelo
	GrupoID           int `json:"grupoId"`           // Grupo de direccion: 0=ida (default), 1=regreso (idaVuelta)
}

// AgregarDetalleHotelRequest
//
// Representa la solicitud para agregar una o varias habitaciones
// seleccionadas como detalle de hotel dentro de una reservacion existente.
type AgregarDetalleHotelRequest struct {
	ReservacionID int                   `json:"reservacionId"` // ID de la reservacion a la que se agrega el detalle
	ProveedorID   int                   `json:"proveedorId"`   // ID del proveedor hotelero
	Habitaciones  []SeleccionHabitacion `json:"habitaciones"`  // Lista de habitaciones seleccionadas con fechas y personas
}

// SeleccionHabitacion
//
// Representa una habitacion especifica elegida por el usuario,
// con las fechas de estancia y la cantidad de personas.
type SeleccionHabitacion struct {
	HabitacionID     int    `json:"habitacionId"`     // ID de la habitacion en el catalogo del proveedor
	FechaCheckIn     string `json:"fechaCheckIn"`     // Fecha de entrada en formato ISO 8601
	FechaCheckOut    string `json:"fechaCheckOut"`    // Fecha de salida en formato ISO 8601
	CantidadPersonas int    `json:"cantidadPersonas"` // Numero de personas que ocuparan la habitacion
}

// ReservacionValidada
//
// Representa los datos minimos de una reservacion ya verificada
// que se usan internamente para validar operaciones posteriores.
type ReservacionValidada struct {
	ID            int // ID unico de la reservacion
	EstadoID      int // ID del estado actual de la reservacion
	TipoReservaID int // ID del tipo de reservacion (1=Aerolinea, 2=Hotel, 3=Paquete)
	UsuarioID     int // ID del usuario propietario de la reservacion
}
