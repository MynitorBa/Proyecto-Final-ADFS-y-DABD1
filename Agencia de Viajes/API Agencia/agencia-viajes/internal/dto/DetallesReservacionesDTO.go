package dto

type AgregarDetalleVueloRequest struct {
	ReservacionID int              `json:"reservacion_id"`
	ProveedorID   int              `json:"proveedor_id"`
	Vuelos        []SeleccionVuelo `json:"vuelos"`
}

type SeleccionVuelo struct {
	VueloId           int `json:"vueloId"`
	ClaseId           int `json:"claseId"`
	CantidadPasajeros int `json:"cantidadPasajeros"`
}

type AgregarDetalleHotelRequest struct {
	ReservacionID int                   `json:"reservacionId"`
	ProveedorID   int                   `json:"proveedorId"`
	Habitaciones  []SeleccionHabitacion `json:"habitaciones"`
}

type SeleccionHabitacion struct {
	HabitacionID     int    `json:"habitacionId"`
	FechaCheckIn     string `json:"fechaCheckIn"`
	FechaCheckOut    string `json:"fechaCheckOut"`
	CantidadPersonas int    `json:"cantidadPersonas"`
}

type ReservacionValidada struct {
	ID            int
	EstadoID      int
	TipoReservaID int
	UsuarioID     int
}
