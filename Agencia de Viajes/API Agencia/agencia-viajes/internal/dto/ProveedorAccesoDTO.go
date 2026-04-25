package dto

// ProveedorAcceso contiene los datos minimos necesarios para autenticar
// y construir peticiones HTTP hacia un proveedor hotelero aliado.
// Se usa exclusivamente para el proxy de imagenes.
type ProveedorAcceso struct {
	// URLApi es la URL base del proveedor (ej: "https://proveedor.com")
	URLApi string

	// TokenEntrada es el token que se envia en el header X-Agencia-Token
	// para autenticarse con el proveedor.
	TokenEntrada string
}
