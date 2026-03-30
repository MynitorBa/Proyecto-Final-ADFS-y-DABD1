package dto

// Lo que devuelve la aerolinea
type RutaProveedorDTO struct {
	ID            int    `json:"id"`
	CiudadOrigen  string `json:"ciudadOrigen"`
	PaisOrigen    string `json:"paisOrigen"`
	CiudadDestino string `json:"ciudadDestino"`
	PaisDestino   string `json:"paisDestino"`
	Duracion      int    `json:"duracion"`
}

type ActualizarCatalogoResponse struct {
	Proveedor  string `json:"proveedor"`
	Insertados int    `json:"insertados"`
	Mensaje    string `json:"mensaje"`
}
