// # Package models
//
// Define las estructuras de dominio utilizadas en toda la aplicacion
// Movent para representar las entidades de la base de datos.
package models

// Pais
//
// Representa un pais registrado en el sistema.
// Se utiliza como referencia geografica para ciudades,
// usuarios y destinos de viaje.
type Pais struct {
	ID     int    `json:"id"`
	Nombre string `json:"nombre"`
}
