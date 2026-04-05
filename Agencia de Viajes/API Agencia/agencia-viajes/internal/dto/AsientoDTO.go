// # Package dto
//
// Contiene los Data Transfer Objects utilizados para la comunicacion
// entre la capa de transporte y la capa de servicio de la agencia de viajes.
package dto

// ObtenerAsientosVueloRequest
//
// Representa la solicitud para consultar los asientos disponibles
// de un vuelo asociado a una reservacion especifica.
type ObtenerAsientosVueloRequest struct {
	ReservacionID int `json:"reservacion_id"` // ID de la reservacion en la base de datos de la agencia
	ProveedorID   int `json:"proveedor_id"`   // ID del proveedor aerolinea
}

// CambiarAsientoVueloRequest
//
// Representa la solicitud para cambiar el asiento asignado
// a un boleto dentro de una reservacion de vuelo.
type CambiarAsientoVueloRequest struct {
	ReservacionID int    `json:"reservacion_id"` // ID de la reservacion en la base de datos de la agencia
	ProveedorID   int    `json:"proveedor_id"`   // ID del proveedor aerolinea
	BoletoID      int    `json:"boleto_id"`      // ID del boleto al que se le cambia el asiento
	NuevoAsiento  string `json:"nuevo_asiento"`  // Codigo del nuevo asiento solicitado
}

// BoletoAsientoDTO
//
// Representa los datos de un boleto individual con su asiento
// y clase asignados dentro de un vuelo.
type BoletoAsientoDTO struct {
	BoletoID int    `json:"boletoId"` // ID unico del boleto
	NoBoleto string `json:"noBoleto"` // Numero de boleto legible
	Asiento  string `json:"asiento"`  // Codigo de asiento asignado (ej. "12A")
	ClaseID  int    `json:"claseId"`  // ID de la clase de servicio
	Clase    string `json:"clase"`    // Nombre de la clase de servicio (ej. "Economica")
}

// CambiarAsientoAerolineaBody
//
// Representa el cuerpo de la solicitud que se envia directamente
// a la API de la aerolinea para actualizar el asiento de un pasajero.
type CambiarAsientoAerolineaBody struct {
	NuevoAsiento string `json:"nuevoAsiento"` // Codigo del nuevo asiento a asignar
}

// AsientoVueloDetalleDTO
//
// Representa la informacion detallada del mapa de asientos
// de un vuelo individual, incluyendo ocupacion y boletos de la agencia.
type AsientoVueloDetalleDTO struct {
	VueloID            int                `json:"vueloId"`            // ID unico del vuelo
	NumeroVuelo        string             `json:"numeroVuelo"`        // Numero identificador del vuelo
	CapacidadPasajeros int                `json:"capacidadPasajeros"` // Capacidad total de pasajeros
	Columnas           []string           `json:"columnas"`           // Letras de columnas disponibles (ej. ["A","B","C"])
	FilasEjecutiva     int                `json:"filasEjecutiva"`     // Numero de filas de clase ejecutiva
	TotalFilas         int                `json:"totalFilas"`         // Total de filas en el avion
	AsientosOcupados   []string           `json:"asientosOcupados"`   // Lista de codigos de asientos ya ocupados
	BoletosAgencia     []BoletoAsientoDTO `json:"boletosAgencia"`     // Boletos pertenecientes a la agencia en este vuelo
}

// AsientosVueloResponse
//
// Representa la respuesta completa de consulta de asientos,
// siendo una lista de detalles por cada vuelo de la reservacion.
type AsientosVueloResponse []AsientoVueloDetalleDTO
