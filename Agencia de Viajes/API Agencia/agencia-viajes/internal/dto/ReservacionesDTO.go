package dto

// ReservacionResumenResponse — lista de reservaciones con datos locales
type ReservacionResumenResponse struct {
	ID              int                      `json:"id"`
	NoReservacion   string                   `json:"no_reservacion"`
	TipoReserva     int                      `json:"tipo_reserva"` // 1=Aerolinea, 2=Hotel, 3=Paquete
	EstadoID        int                      `json:"estado_id"`
	Total           float64                  `json:"total"`
	FechaCreacion   string                   `json:"fecha_creacion"`
	FechaExpiracion *string                  `json:"fecha_expiracion"`
	Detalles        []DetalleResumenResponse `json:"detalles"`
}

// DetalleResumenResponse — datos guardados localmente por detalle
type DetalleResumenResponse struct {
	ID                 int         `json:"id"`
	TipoDetalleID      int         `json:"tipo_detalle_id"` // 1=Vuelo, 2=Hotel
	IDReservaProveedor string      `json:"id_reserva_proveedor"`
	Total              float64     `json:"total"`
	EstadoDetalleID    int         `json:"estado_detalle_id"`
	ParametrosJson     interface{} `json:"parametros_json"`
}

// ReservacionDetalladaResponse — combina datos locales + respuesta de proveedores
type ReservacionDetalladaResponse struct {
	ID              int                       `json:"id"`
	NoReservacion   string                    `json:"no_reservacion"`
	TipoReserva     int                       `json:"tipo_reserva"`
	EstadoID        int                       `json:"estado_id"`
	Total           float64                   `json:"total"`
	FechaCreacion   string                    `json:"fecha_creacion"`
	FechaExpiracion *string                   `json:"fecha_expiracion"`
	Detalles        []DetalleCompletoResponse `json:"detalles"`
}

// DetalleCompletoResponse — detalle local + data cruda del proveedor
type DetalleCompletoResponse struct {
	ID                 int         `json:"id"`
	TipoDetalleID      int         `json:"tipo_detalle_id"`
	IDReservaProveedor string      `json:"id_reserva_proveedor"`
	Total              float64     `json:"total"`
	EstadoDetalleID    int         `json:"estado_detalle_id"`
	ParametrosJson     interface{} `json:"parametros_json"`
	DataProveedor      interface{} `json:"data_proveedor"` // respuesta cruda del proveedor
}

type FilaReservacionDetalle struct {
	ReservacionID      int
	NoReservacion      string
	TipoReservaID      int
	EstadoID           int
	Total              float64
	FechaCreacion      string
	FechaExpiracion    *string
	DetalleID          int
	TipoDetalleID      int
	IDReservaProveedor string
	DetalleTotal       float64
	EstadoDetalleID    int
	ParametrosJson     string
	ProveedorID        int
	URLAPI             string
	TokenEntrada       string
}
