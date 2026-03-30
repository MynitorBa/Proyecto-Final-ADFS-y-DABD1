package dto

type CrearProveedorRequest struct {
	Nombre             string  `json:"nombre" binding:"required"`
	TipoProveedorID    int     `json:"tipo_proveedor_id" binding:"required"`
	URLAPI             string  `json:"url_api"`
	UsuarioID          int     `json:"usuario_id" binding:"required"` // debe ser rol 3
	PorcentajeGanancia float64 `json:"porcentaje_ganancia"`
}

type CrearProveedorResponse struct {
	ID                 int     `json:"id"`
	Nombre             string  `json:"nombre"`
	TipoProveedorID    int     `json:"tipo_proveedor_id"`
	URLAPI             string  `json:"url_api"`
	UsuarioID          int     `json:"usuario_id"`
	EstadoID           int     `json:"estado_id"`
	PorcentajeGanancia float64 `json:"porcentaje_ganancia"`
}
