// # Package services
//
// Contiene los servicios de negocio de la agencia de viajes,
// incluyendo procesamiento de pagos, reservaciones, proveedores y usuarios.
package services

import (
	"agencia-viajes/internal/repositories"
	"crypto/tls"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
)

// ComentarioService
//
// Servicio encargado de obtener comentarios de vuelos y hoteles
// consultando directamente las APIs de los proveedores externos registrados.
type ComentarioService struct {
	proveedorRepo *repositories.ProveedorRepository
}

// NewComentarioService
//
// Crea e inicializa una nueva instancia de ComentarioService con su repositorio de proveedores.
//
// Parametros:
//   - proveedorRepo: repositorio de proveedores para obtener URL y token de acceso
//
// Retorna:
//   - *ComentarioService: instancia inicializada del servicio de comentarios
func NewComentarioService(proveedorRepo *repositories.ProveedorRepository) *ComentarioService {
	return &ComentarioService{proveedorRepo: proveedorRepo}
}

// ObtenerComentariosVuelo
//
// Obtiene los comentarios de una ruta de vuelo consultando la API del proveedor
// identificado por su ID. Construye la URL del endpoint de comentarios de aerolinea
// y delega la llamada HTTP al metodo interno llamarProveedor.
//
// Parametros:
//   - proveedorID: identificador del proveedor de aerolinea en la base de datos
//   - rutaID: identificador de la ruta de vuelo en el sistema del proveedor
//
// Retorna:
//   - interface{}: respuesta JSON deserializada del proveedor
//   - error: error si el proveedor no existe o la llamada HTTP falla
func (s *ComentarioService) ObtenerComentariosVuelo(proveedorID, rutaID int) (interface{}, error) {
	proveedor, err := s.proveedorRepo.ObtenerProveedorPorID(proveedorID)
	if err != nil {
		return nil, fmt.Errorf("proveedor no encontrado: %w", err)
	}

	url := fmt.Sprintf("%s/api/comentarios/agencia/ruta/%d", proveedor.URLAPI, rutaID)
	return s.llamarProveedor(url, proveedor.TokenEntrada)
}

// ObtenerComentariosHotel
//
// Obtiene los comentarios de un hotel consultando la API del proveedor
// identificado por su ID. Construye la URL del endpoint de comentarios de hotel
// y delega la llamada HTTP al metodo interno llamarProveedor.
//
// Parametros:
//   - proveedorID: identificador del proveedor hotelero en la base de datos
//   - hotelID: identificador del hotel en el sistema del proveedor
//
// Retorna:
//   - interface{}: respuesta JSON deserializada del proveedor
//   - error: error si el proveedor no existe o la llamada HTTP falla
func (s *ComentarioService) ObtenerComentariosHotel(proveedorID, hotelID int) (interface{}, error) {
	proveedor, err := s.proveedorRepo.ObtenerProveedorPorID(proveedorID)
	if err != nil {
		return nil, fmt.Errorf("proveedor no encontrado: %w", err)
	}

	url := fmt.Sprintf("%s/agencia/comentarios/hotel/%d", proveedor.URLAPI, hotelID)
	return s.llamarProveedor(url, proveedor.TokenEntrada)
}

// llamarProveedor
//
// Realiza una solicitud HTTP GET a la URL indicada con el token de autenticacion
// del proveedor en el header X-Agencia-Token. Usa un cliente HTTP con TLS sin
// verificacion de certificado para compatibilidad con proveedores externos.
// Deserializa la respuesta JSON a interface{}.
//
// Parametros:
//   - url: endpoint completo al que se realizara la solicitud GET
//   - token: token de acceso del proveedor para el header de autenticacion
//
// Retorna:
//   - interface{}: respuesta JSON deserializada del proveedor
//   - error: error si falla la solicitud HTTP, el proveedor responde con status 400 o superior,
//     o si no se puede deserializar el cuerpo de la respuesta
func (s *ComentarioService) llamarProveedor(url, token string) (interface{}, error) {
	req, err := http.NewRequest("GET", url, nil)
	if err != nil {
		return nil, err
	}
	req.Header.Set("X-Agencia-Token", token)

	client := &http.Client{
		Transport: &http.Transport{
			TLSClientConfig: &tls.Config{InsecureSkipVerify: true},
		},
	}

	resp, err := client.Do(req)
	if err != nil {
		return nil, fmt.Errorf("error contactando proveedor: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode >= 400 {
		return nil, fmt.Errorf("proveedor respondió con status %d", resp.StatusCode)
	}

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, err
	}

	var resultado interface{}
	if err := json.Unmarshal(body, &resultado); err != nil {
		return nil, err
	}
	return resultado, nil
}
