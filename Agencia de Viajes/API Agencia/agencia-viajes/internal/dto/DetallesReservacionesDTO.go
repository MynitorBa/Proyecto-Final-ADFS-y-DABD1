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

type ReservacionValidada struct {
	ID            int
	EstadoID      int
	TipoReservaID int
	UsuarioID     int
}
