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
// El precio YA incluye el descuento de ganancia aplicado por la agencia.
type SeleccionVuelo struct {
	VueloId           int     `json:"vueloId"`           // ID del vuelo en el catalogo del proveedor
	ClaseId           int     `json:"claseId"`           // ID de la clase de servicio seleccionada
	CantidadPasajeros int     `json:"cantidadPasajeros"` // Numero de pasajeros para este vuelo
	GrupoID           int     `json:"grupoId"`           // Grupo de direccion: 0=ida (default), 1=regreso (idaVuelta)
	Precio            float64 `json:"precio"`            // Precio con descuento ya aplicado por la agencia
}

// AgregarDetalleHotelRequest
//
// Representa la solicitud para agregar una o varias habitaciones
// seleccionadas como detalle de hotel dentro de una reservacion existente.
type AgregarDetalleHotelRequest struct {
	ReservacionID int                   `json:"reservacionId"` // ID de la reservacion a la que se agrega el detalle
	ProveedorID   int                   `json:"proveedorId"`   // ID del proveedor hotelero
	Habitaciones  []SeleccionHabitacion `json:"habitaciones"`  // Lista de habitaciones seleccionadas con fechas y personas
	// Criterios de búsqueda para poder obtener alternativas de habitaciones
	CriteriosBusqueda *BusquedaHotelesRequest `json:"criteriosBusqueda,omitempty"` // Criterios de búsqueda original (para obtener alternativas)
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

// DetalleHabitacionElegibleDTO
//
// DTO que representa una habitacion elegible para cambio,
// incluyendo numero, tipo y precio por noche.
type DetalleHabitacionElegibleDTO struct {
	ID               int    `json:"id"`               // ID de la habitacion
	NumeroHabitacion string `json:"numeroHabitacion"` // Numero de la habitacion
	PrecioPorNoche   int    `json:"precioPorNoche"`   // Precio por noche en la moneda del proveedor
}

// EditarReservacionRequest
//
// DTO que contiene los datos a editar de una reservacion.
// Permite actualizar nombres de pasajeros, datos de pasaporte y fechas.
type EditarReservacionRequest struct {
	Pasajeros []EditarPasajeroRequest `json:"pasajeros"`          // Nombres y datos de pasajeros
	FechaIda  string                  `json:"fechaIda"`           // Nueva fecha de ida (opcional)
	FechaRetorno string                `json:"fechaRetorno"`       // Nueva fecha de retorno (opcional)
	FechaIdaActual string              `json:"fechaIdaActual"`     // Fecha de ida actual (para validar duraciones)
	FechaRetornoActual string          `json:"fechaRetornoActual"` // Fecha de retorno actual (para validar duraciones)
	FechaCheckIn  string                `json:"fechaCheckIn"`       // Nueva fecha check-in hotel (opcional)
	FechaCheckOut string                `json:"fechaCheckOut"`      // Nueva fecha check-out hotel (opcional)
	FechaCheckInActual string           `json:"fechaCheckInActual"`     // Fecha check-in actual (para validar duraciones)
	FechaCheckOutActual string          `json:"fechaCheckOutActual"`    // Fecha check-out actual (para validar duraciones)
}

// EditarPasajeroRequest
//
// DTO que contiene los datos de un pasajero para edición.
type EditarPasajeroRequest struct {
	ID            int    `json:"id"`            // ID del pasajero (0 si es nuevo)
	Nombre        string `json:"nombre"`        // Nombre completo del pasajero
	Apellido      string `json:"apellido"`      // Apellido del pasajero
	NumPasaporte  string `json:"numPasaporte"`  // Número de pasaporte
	FechaNac      string `json:"fechaNac"`      // Fecha de nacimiento (ISO 8601)
	Nacionalidad  string `json:"nacionalidad"`  // Nacionalidad del pasajero
}

// EditarReservacionResponse
//
// DTO con la confirmación de edición de reservacion.
type EditarReservacionResponse struct {
	Exitoso bool   `json:"exitoso"`        // Indica si la edición fue exitosa
	Mensaje string `json:"mensaje"`        // Mensaje descriptivo
	Cambios []string `json:"cambios"`      // Lista de cambios realizados
}
