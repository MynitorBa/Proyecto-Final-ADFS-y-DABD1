package dto

type CrearReservacionRequest struct {
	TipoReservaID int `json:"tipo_reserva_id"`
}

type CrearReservacionResponse struct {
	ID              int    `json:"id"`
	NoReservacion   string `json:"no_reservacion"`
	EstadoID        int    `json:"estado_id"`
	Estado          string `json:"estado"`
	TipoReservaID   int    `json:"tipo_reserva_id"`
	FechaExpiracion string `json:"fecha_expiracion"`
	FechaCreacion   string `json:"fecha_creacion"`
}
