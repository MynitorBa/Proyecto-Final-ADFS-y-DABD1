// # Package dto
//
// Contiene los Data Transfer Objects utilizados para la comunicacion
// entre la capa de transporte y la capa de servicio de la agencia de viajes.
package dto

// BusquedaVuelosRequest
//
// Representa los criterios de busqueda enviados por el cliente
// para consultar vuelos disponibles en todos los proveedores aerolinea.
// Es compartido por las rutas de busqueda individual y general.
type BusquedaVuelosRequest struct {
	Origen            string `json:"origen"`            // Ciudad de origen del vuelo
	OrigenPais        string `json:"origenPais"`        // Pais de origen del vuelo
	Destino           string `json:"destino"`           // Ciudad de destino del vuelo
	DestinoPais       string `json:"destinoPais"`       // Pais de destino del vuelo
	Fecha             string `json:"fecha"`             // Fecha del vuelo en formato ISO 8601
	CantidadPasajeros int    `json:"cantidadPasajeros"` // Numero de pasajeros requeridos
}

// BusquedaHotelesRequest
//
// Representa los criterios de busqueda enviados por el cliente
// para consultar hoteles disponibles en todos los proveedores hoteleros.
// Es compartido por las rutas de busqueda individual y general.
type BusquedaHotelesRequest struct {
	Ciudad           string `json:"ciudad"`           // Ciudad donde se busca el hotel
	Pais             string `json:"pais"`             // Pais donde se busca el hotel
	FechaCheckIn     string `json:"fechaCheckIn"`     // Fecha de entrada en formato ISO 8601
	FechaCheckOut    string `json:"fechaCheckOut"`    // Fecha de salida en formato ISO 8601
	CantidadPersonas int    `json:"cantidadPersonas"` // Numero de personas que se hospedan
}

// ProveedorCatalogo
//
// Representa los datos de un proveedor obtenidos del catalogo interno
// de la agencia, utilizados para realizar consultas a su API externa.
type ProveedorCatalogo struct {
	ProveedorID        int     // ID unico del proveedor en la base de datos de la agencia
	Nombre             string  // Nombre comercial del proveedor
	URLApi             string  // URL base de la API del proveedor
	TokenEntrada       string  // Token de autenticacion para acceder a la API del proveedor
	PorcentajeGanancia float64 // Porcentaje de ganancia aplicado al precio base del proveedor
	ImagenBase64       string  // Imagen del proveedor codificada en Base64
}

// BusquedaVuelosResponse
//
// Representa la respuesta de busqueda de vuelos para un proveedor especifico,
// encapsulando los datos crudos devueltos por su API o el error ocurrido.
type BusquedaVuelosResponse struct {
	Proveedor      string      `json:"proveedor"`                 // Nombre del proveedor aerolinea
	ProveedorID    int         `json:"proveedor_id"`              // ID del proveedor aerolinea
	ProveedorImagen string     `json:"proveedorImagen,omitempty"` // Imagen del proveedor en Base64
	Datos          interface{} `json:"datos"`                     // Datos crudos de vuelos devueltos por el proveedor
	Error          string      `json:"error,omitempty"`           // Mensaje de error si la consulta al proveedor fallo
}

// BusquedaHotelesResponse
//
// Representa la respuesta de busqueda de hoteles para un proveedor especifico,
// encapsulando los datos crudos devueltos por su API o el error ocurrido.
type BusquedaHotelesResponse struct {
	Proveedor       string      `json:"proveedor"`                 // Nombre del proveedor hotelero
	ProveedorID     int         `json:"proveedor_id"`              // ID del proveedor hotelero
	ProveedorImagen string      `json:"proveedorImagen,omitempty"` // Imagen del proveedor en Base64
	Datos           interface{} `json:"datos"`                     // Datos crudos de hoteles devueltos por el proveedor
	Error           string      `json:"error,omitempty"`           // Mensaje de error si la consulta al proveedor fallo
}
