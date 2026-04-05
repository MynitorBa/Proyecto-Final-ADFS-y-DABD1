// # Package dto
//
// Contiene los Data Transfer Objects utilizados para la comunicacion
// entre la capa de transporte y la capa de servicio de la agencia de viajes.
package dto

// PagoReservacionRequest
//
// Representa la solicitud de pago enviada por el cliente
// para completar una reservacion, incluyendo los datos de la tarjeta
// y la informacion de facturacion.
type PagoReservacionRequest struct {
	ReservacionID int    `json:"reservacion_id" binding:"required"` // ID de la reservacion a pagar
	TarjetaNumero string `json:"tarjeta_numero" binding:"required"` // Numero completo de la tarjeta de credito o debito
	TarjetaCVV    string `json:"tarjeta_cvv" binding:"required"`    // Codigo de verificacion de la tarjeta
	TarjetaMes    string `json:"tarjeta_mes" binding:"required"`    // Mes de vencimiento de la tarjeta (formato MM)
	TarjetaAnio   string `json:"tarjeta_anio" binding:"required"`   // Anio de vencimiento de la tarjeta (formato AAAA)
	Nit           string `json:"nit" binding:"required"`            // Numero de identificacion tributaria del cliente
	CodigoPostal  string `json:"codigo_postal" binding:"required"`  // Codigo postal de facturacion
}

// PagoProveedorBody
//
// Representa el cuerpo de la solicitud que se envia al proveedor externo
// para procesar el pago de una reservacion en su sistema.
type PagoProveedorBody struct {
	Nit          string `json:"nit"`          // Numero de identificacion tributaria del cliente
	CodigoPostal string `json:"codigoPostal"` // Codigo postal de facturacion
}
