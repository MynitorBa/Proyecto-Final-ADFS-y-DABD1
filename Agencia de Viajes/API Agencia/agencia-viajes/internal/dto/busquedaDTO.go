package dto

//Request compartido para ambas rutas

type BusquedaVuelosRequest struct {
	Origen            string `json:"origen"`
	OrigenPais        string `json:"origenPais"`
	Destino           string `json:"destino"`
	DestinoPais       string `json:"destinoPais"`
	Fecha             string `json:"fecha"`
	CantidadPasajeros int    `json:"cantidadPasajeros"`
}

type BusquedaHotelesRequest struct {
	Ciudad           string `json:"ciudad"`
	Pais             string `json:"pais"`
	FechaCheckIn     string `json:"fechaCheckIn"`
	FechaCheckOut    string `json:"fechaCheckOut"`
	CantidadPersonas int    `json:"cantidadPersonas"`
}

//Proveedor del catálogo (aerolinea u hotelera)

type ProveedorCatalogo struct {
	ProveedorID        int
	Nombre             string
	URLApi             string
	TokenEntrada       string
	PorcentajeGanancia float64
}

//Respuesta final
type BusquedaVuelosResponse struct {
	Proveedor   string      `json:"proveedor"`
	ProveedorID int         `json:"proveedor_id"`
	Datos       interface{} `json:"datos"`
	Error       string      `json:"error,omitempty"`
}

type BusquedaHotelesResponse struct {
	Proveedor   string      `json:"proveedor"`
	ProveedorID int         `json:"proveedor_id"`
	Datos       interface{} `json:"datos"`
	Error       string      `json:"error,omitempty"`
}
