package dto

type ProveedorIdentidad struct {
	ID              int    `json:"id"`
	Nombre          string `json:"nombre"`
	TipoProveedorID int    `json:"tipo_proveedor_id"`
}
