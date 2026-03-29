package models

type Ciudad struct {
	ID     int    `json:"id"`
	Nombre string `json:"nombre"`
	PaisID int    `json:"pais_id"`
}
