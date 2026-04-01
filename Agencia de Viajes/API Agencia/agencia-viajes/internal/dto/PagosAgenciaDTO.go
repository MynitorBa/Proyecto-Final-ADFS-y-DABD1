package dto

type PagoReservacionRequest struct {
	ReservacionID int    `json:"reservacion_id" binding:"required"`
	TarjetaNumero string `json:"tarjeta_numero" binding:"required"`
	TarjetaCVV    string `json:"tarjeta_cvv" binding:"required"`
	TarjetaMes    string `json:"tarjeta_mes" binding:"required"`
	TarjetaAnio   string `json:"tarjeta_anio" binding:"required"`
	Nit           string `json:"nit" binding:"required"`
	CodigoPostal  string `json:"codigo_postal" binding:"required"`
}

type PagoProveedorBody struct {
	Nit          string `json:"nit"`
	CodigoPostal string `json:"codigoPostal"`
}
