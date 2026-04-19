// # Package dto
//
// Contiene los Data Transfer Objects utilizados para la comunicacion
// entre la capa de transporte y la capa de servicio de la agencia de viajes.
package dto

// CrearReservacionRequest
//
// Representa la solicitud para crear una nueva reservacion,
// indicando el tipo de reserva que el usuario desea iniciar.
type CrearReservacionRequest struct {
	TipoReservaID int `json:"tipo_reserva_id"` // ID del tipo de reserva (1=Aerolinea, 2=Hotel, 3=Paquete)
}

// CrearReservacionResponse
//
// Representa la respuesta retornada al cliente tras crear
// exitosamente una nueva reservacion en el sistema.
type CrearReservacionResponse struct {
	ID              int    `json:"id"`               // ID unico generado para la reservacion
	NoReservacion   string `json:"no_reservacion"`   // Numero de reservacion legible para el usuario
	EstadoID        int    `json:"estado_id"`        // ID del estado inicial de la reservacion
	Estado          string `json:"estado"`           // Nombre del estado inicial de la reservacion
	TipoReservaID   int    `json:"tipo_reserva_id"`  // ID del tipo de reserva creado
	FechaExpiracion string `json:"fecha_expiracion"` // Fecha y hora de expiracion de la reservacion
	FechaCreacion   string `json:"fecha_creacion"`   // Fecha y hora de creacion de la reservacion
}

// ReservacionConDetalles
//
// Representa una reservacion junto con la lista de sus detalles
// de proveedores, utilizado internamente para operaciones de negocio.
type ReservacionConDetalles struct {
	ID       int               // ID unico de la reservacion
	Detalles []DetalleProveedor // Lista de detalles asociados a proveedores externos
}

// DetalleProveedor
//
// Representa la informacion de un detalle de reservacion vinculado
// a un proveedor externo, incluyendo los datos de acceso a su API.
type DetalleProveedor struct {
	IDReservaProveedor string // ID de la reserva en el sistema del proveedor externo
	ProveedorID        int    // ID del proveedor en la base de datos de la agencia
	URLAPI             string // URL base de la API del proveedor
	TokenEntrada       string // Token de autenticacion para acceder a la API del proveedor
	TipoDetalleID      int    // ID del tipo de detalle (1=Vuelo, 2=Hotel)
}

// ReservacionExpirable
//
// Datos minimos de una reservacion pendiente que el scheduler de expiracion
// necesita para notificar proveedores, actualizar el estado en BD y registrar
// correctamente el evento en log_sesion.
type ReservacionExpirable struct {
	ID            int    // ID unico de la reservacion en la base de datos
	UsuarioID     int    // ID del usuario propietario de la reservacion
	NoReservacion string // Numero de reservacion legible para el usuario
}
