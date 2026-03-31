package dto

type ObtenerAsientosVueloRequest struct {
	ReservacionID int `json:"reservacion_id"`
	ProveedorID   int `json:"proveedor_id"`
}

type CambiarAsientoVueloRequest struct {
	ReservacionID int    `json:"reservacion_id"`
	ProveedorID   int    `json:"proveedor_id"`
	BoletoID      int    `json:"boleto_id"`
	NuevoAsiento  string `json:"nuevo_asiento"`
}

type BoletoAsientoDTO struct {
	BoletoID int    `json:"boletoId"`
	NoBoleto string `json:"noBoleto"`
	Asiento  string `json:"asiento"`
	ClaseID  int    `json:"claseId"`
	Clase    string `json:"clase"`
}

type CambiarAsientoAerolineaBody struct {
	NuevoAsiento string `json:"nuevoAsiento"`
}

// Representa un solo vuelo dentro de la respuesta
type AsientoVueloDetalleDTO struct {
	VueloID            int                `json:"vueloId"`
	NumeroVuelo        string             `json:"numeroVuelo"`
	CapacidadPasajeros int                `json:"capacidadPasajeros"`
	Columnas           []string           `json:"columnas"`
	FilasEjecutiva     int                `json:"filasEjecutiva"`
	TotalFilas         int                `json:"totalFilas"`
	AsientosOcupados   []string           `json:"asientosOcupados"`
	BoletosAgencia     []BoletoAsientoDTO `json:"boletosAgencia"`
}

// La respuesta ahora debe ser una lista (slice) de lo anterior
type AsientosVueloResponse []AsientoVueloDetalleDTO
