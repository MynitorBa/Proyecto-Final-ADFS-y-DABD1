// # Package models
//
// Define las estructuras de dominio utilizadas en toda la aplicacion
// Movent para representar las entidades de la base de datos.
package models

// Ciudad
//
// Representa una ciudad registrada en el sistema.
// Se utiliza para asociar usuarios y ubicaciones geograficas
// dentro de la plataforma de reservaciones.
type Ciudad struct {
	ID     int    `json:"id"`
	Nombre string `json:"nombre"`
	PaisID int    `json:"pais_id"`
}
