package dto

//Request

type CancelarReservacionRequest struct {
	Motivo string `json:"motivo"`
}

//Verificar

type VerificarCancelacionResponse struct {
	PuedeCancelar bool                       `json:"puede_cancelar"`
	Detalles      []VerificarDetalleResponse `json:"detalles"`
}

type VerificarDetalleResponse struct {
	TipoDetalleID      int    `json:"tipo_detalle_id"`
	IDReservaProveedor string `json:"id_reserva_proveedor"`
	PuedeCancelar      bool   `json:"puede_cancelar"`
	Razon              string `json:"razon"`
}

//Respuesta proveedor puede-cancelar
type ProveedorPuedeCancelarResponse struct {
	PuedeCancelar bool   `json:"puedeCancelar"`
	Razon         string `json:"razon"`
}
