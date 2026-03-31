package dto

// AgregarPasajerosVueloRequest recibe el endpoint de agencia
type AgregarPasajerosVueloRequest struct {
	ReservacionID int                `json:"reservacion_id"` // ID reservación en BD agencia
	ProveedorID   int                `json:"proveedor_id"`   // ID proveedor (aerolínea)
	Pasajeros     []PasajeroVueloDTO `json:"pasajeros"`
}

type PasajeroVueloDTO struct {
	BoletoID  int    `json:"boletoId"` // ID boleto devuelto por la aerolínea
	Nombre    string `json:"nombre"`
	Apellido  string `json:"apellido"`
	Pasaporte string `json:"pasaporte"` // Solo números
	Telefono  string `json:"telefono"`
	Pais      string `json:"pais"`
	Ciudad    string `json:"ciudad"`
}

// AgregarPasajerosVueloAerolineaBody envía a la aerolínea
type AgregarPasajerosVueloAerolineaBody struct {
	ReservacionID int                `json:"reservacionId"` // ID reservación en aerolínea
	Pasajeros     []PasajeroVueloDTO `json:"pasajeros"`
}
