// # Package dto
//
// Contiene los Data Transfer Objects utilizados para la comunicacion
// entre la capa de transporte y la capa de servicio de la agencia de viajes.
package dto

// ReservacionResumenResponse
//
// Representa el resumen de una reservacion con sus datos locales,
// utilizado para listar las reservaciones de un usuario sin consultar
// informacion adicional a los proveedores externos.
type ReservacionResumenResponse struct {
	ID              int                      `json:"id"`               // ID unico de la reservacion
	NoReservacion   string                   `json:"no_reservacion"`   // Numero de reservacion legible para el usuario
	TipoReserva     int                      `json:"tipo_reserva"`     // Tipo de reserva: 1=Aerolinea, 2=Hotel, 3=Paquete
	EstadoID        int                      `json:"estado_id"`        // ID del estado actual de la reservacion
	Total           float64                  `json:"total"`            // Monto total de la reservacion
	FechaCreacion   string                   `json:"fecha_creacion"`   // Fecha y hora de creacion de la reservacion
	FechaExpiracion *string                  `json:"fecha_expiracion"` // Fecha y hora de expiracion (puede ser nula)
	Detalles        []DetalleResumenResponse `json:"detalles"`         // Lista de detalles asociados a la reservacion
}

// DetalleResumenResponse
//
// Representa los datos de un detalle de reservacion guardados
// localmente en la base de datos de la agencia, sin datos del proveedor.
type DetalleResumenResponse struct {
	ID                 int         `json:"id"`                   // ID unico del detalle
	TipoDetalleID      int         `json:"tipo_detalle_id"`      // Tipo de detalle: 1=Vuelo, 2=Hotel
	IDReservaProveedor string      `json:"id_reserva_proveedor"` // ID de la reserva en el sistema del proveedor
	Total              float64     `json:"total"`                // Monto total del detalle
	EstadoDetalleID    int         `json:"estado_detalle_id"`    // ID del estado del detalle
	ParametrosJson     interface{} `json:"parametros_json"`      // Parametros adicionales almacenados como JSON
}

// ReservacionDetalladaResponse
//
// Representa la informacion completa de una reservacion combinando
// los datos locales de la agencia con las respuestas de los proveedores externos.
type ReservacionDetalladaResponse struct {
	ID              int                       `json:"id"`               // ID unico de la reservacion
	NoReservacion   string                    `json:"no_reservacion"`   // Numero de reservacion legible para el usuario
	TipoReserva     int                       `json:"tipo_reserva"`     // Tipo de reserva: 1=Aerolinea, 2=Hotel, 3=Paquete
	EstadoID        int                       `json:"estado_id"`        // ID del estado actual de la reservacion
	Total           float64                   `json:"total"`            // Monto total de la reservacion
	FechaCreacion   string                    `json:"fecha_creacion"`   // Fecha y hora de creacion de la reservacion
	FechaExpiracion *string                   `json:"fecha_expiracion"` // Fecha y hora de expiracion (puede ser nula)
	Detalles        []DetalleCompletoResponse `json:"detalles"`         // Lista de detalles con datos locales y del proveedor
}

// DetalleCompletoResponse
//
// Representa un detalle de reservacion enriquecido con los datos
// locales de la agencia mas la respuesta cruda obtenida del proveedor externo.
type DetalleCompletoResponse struct {
	ID                 int         `json:"id"`                   // ID unico del detalle
	TipoDetalleID      int         `json:"tipo_detalle_id"`      // Tipo de detalle: 1=Vuelo, 2=Hotel
	ProveedorID        int         `json:"proveedor_id"`         // ID del proveedor en la base de datos de la agencia
	IDReservaProveedor string      `json:"id_reserva_proveedor"` // ID de la reserva en el sistema del proveedor
	Total              float64     `json:"total"`                // Monto total del detalle
	EstadoDetalleID    int         `json:"estado_detalle_id"`    // ID del estado del detalle
	ParametrosJson     interface{} `json:"parametros_json"`      // Parametros adicionales almacenados como JSON
	DataProveedor      interface{} `json:"data_proveedor"`       // Respuesta cruda retornada por el proveedor externo
}

// FilaReservacionDetalle
//
// Representa una fila plana del resultado de una consulta SQL que une
// datos de reservacion con sus detalles y la informacion del proveedor,
// utilizada para construir las respuestas estructuradas.
type FilaReservacionDetalle struct {
	ReservacionID      int     // ID unico de la reservacion
	NoReservacion      string  // Numero de reservacion
	TipoReservaID      int     // Tipo de reserva
	EstadoID           int     // Estado de la reservacion
	Total              float64 // Monto total de la reservacion
	FechaCreacion      string  // Fecha de creacion de la reservacion
	FechaExpiracion    *string // Fecha de expiracion (puede ser nula)
	DetalleID          int     // ID del detalle de reservacion
	TipoDetalleID      int     // Tipo del detalle (1=Vuelo, 2=Hotel)
	IDReservaProveedor string  // ID de la reserva en el proveedor
	DetalleTotal       float64 // Monto total del detalle
	EstadoDetalleID    int     // Estado del detalle
	ParametrosJson     string  // Parametros del detalle en formato JSON crudo
	ProveedorID        int     // ID del proveedor asociado al detalle
	URLAPI             string  // URL base de la API del proveedor
	TokenEntrada       string  // Token de autenticacion del proveedor
}
