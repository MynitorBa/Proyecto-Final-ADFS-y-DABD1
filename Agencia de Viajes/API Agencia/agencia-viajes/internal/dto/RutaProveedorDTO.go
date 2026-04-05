// # Package dto
//
// Contiene los Data Transfer Objects utilizados para la comunicacion
// entre la capa de transporte y la capa de servicio de la agencia de viajes.
package dto

// RutaProveedorDTO
//
// Representa la informacion de una ruta de vuelo devuelta
// por el proveedor aerolinea, incluyendo origen, destino y duracion.
type RutaProveedorDTO struct {
	ID            int    `json:"id"`            // ID unico de la ruta en el sistema del proveedor
	CiudadOrigen  string `json:"ciudadOrigen"`  // Ciudad de origen del vuelo
	PaisOrigen    string `json:"paisOrigen"`    // Pais de origen del vuelo
	CiudadDestino string `json:"ciudadDestino"` // Ciudad de destino del vuelo
	PaisDestino   string `json:"paisDestino"`   // Pais de destino del vuelo
	Duracion      int    `json:"duracion"`      // Duracion del vuelo en minutos
}

// ActualizarCatalogoResponse
//
// Representa la respuesta retornada al cliente tras ejecutar
// la actualizacion del catalogo de rutas o habitaciones de un proveedor.
type ActualizarCatalogoResponse struct {
	Proveedor  string `json:"proveedor"`  // Nombre del proveedor cuyo catalogo fue actualizado
	Insertados int    `json:"insertados"` // Cantidad de registros nuevos insertados en la base de datos
	Mensaje    string `json:"mensaje"`    // Mensaje descriptivo del resultado de la operacion
}
