// # Package dto
//
// Contiene los Data Transfer Objects utilizados para la comunicacion
// entre la capa de transporte y la capa de servicio de la agencia de viajes.
package dto

// CancelarReservacionRequest
//
// Representa la solicitud para cancelar una reservacion,
// incluyendo el motivo de la cancelacion proporcionado por el usuario.
type CancelarReservacionRequest struct {
	Motivo string `json:"motivo"` // Motivo de la cancelacion descrito por el usuario
}

// VerificarCancelacionResponse
//
// Representa la respuesta de verificacion previa a la cancelacion,
// indicando si la reservacion puede cancelarse y el detalle por cada componente.
type VerificarCancelacionResponse struct {
	PuedeCancelar bool                       `json:"puede_cancelar"` // Indica si la reservacion completa puede cancelarse
	Detalles      []VerificarDetalleResponse `json:"detalles"`       // Lista de verificaciones por detalle de reservacion
}

// VerificarDetalleResponse
//
// Representa el resultado de verificacion de cancelacion
// para un detalle especifico de la reservacion (vuelo u hotel).
type VerificarDetalleResponse struct {
	TipoDetalleID      int    `json:"tipo_detalle_id"`      // ID del tipo de detalle (1=Vuelo, 2=Hotel)
	IDReservaProveedor string `json:"id_reserva_proveedor"` // ID de la reserva en el sistema del proveedor
	PuedeCancelar      bool   `json:"puede_cancelar"`       // Indica si este detalle puede cancelarse
	Razon              string `json:"razon"`                // Razon del resultado de la verificacion
}

// ProveedorPuedeCancelarResponse
//
// Representa la respuesta que entrega el proveedor externo
// al consultar si una reserva puede ser cancelada en su sistema.
type ProveedorPuedeCancelarResponse struct {
	PuedeCancelar bool   `json:"puedeCancelar"` // Indica si el proveedor permite la cancelacion
	Razon         string `json:"razon"`         // Mensaje explicativo del proveedor
}
