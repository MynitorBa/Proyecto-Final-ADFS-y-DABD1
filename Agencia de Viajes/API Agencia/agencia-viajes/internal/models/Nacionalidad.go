// # Package models
//
// Define las estructuras de dominio utilizadas en toda la aplicacion
// Movent para representar las entidades de la base de datos.
package models

// Nacionalidad
//
// Representa una nacionalidad disponible en el sistema.
// Se asocia a los usuarios durante el registro para registrar
// su origen o ciudadania.
type Nacionalidad struct {
	ID     int    `json:"id"`
	Nombre string `json:"nombre"`
}
