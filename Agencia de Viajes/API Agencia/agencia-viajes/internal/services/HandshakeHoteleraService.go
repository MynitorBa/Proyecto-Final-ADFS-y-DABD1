// # Package services
//
// Servicios de negocio de la agencia de viajes. Este paquete contiene la logica
// central para reservaciones, busquedas, autenticacion, catalogos y comunicacion
// con proveedores externos (aerolineas y hoteleras).
package services

import (
	"agencia-viajes/internal/config"
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/helpers"
	"agencia-viajes/internal/repositories"
	"bytes"
	"database/sql"
	"encoding/json"
	"errors"
	"fmt"
	"net/http"
)

// HandshakeHoteleraService
//
// Servicio encargado de gestionar el proceso de handshake de autenticacion
// con proveedores de tipo hotelera. Genera un token de entrada para la agencia,
// lo envia a la hotelera y almacena ambos tokens (entrada y salida) en BD
// para su uso en comunicaciones posteriores.
type HandshakeHoteleraService struct {
	repo      *repositories.ProveedorRepository
	serverURL string
}

// NewHandshakeHoteleraService
//
// Crea e inicializa una nueva instancia de HandshakeHoteleraService con su
// repositorio de proveedores y la URL publica de la agencia.
//
// Parametros:
//   - db: conexion activa a la base de datos SQL
//   - cfg: configuracion de la aplicacion que contiene la URL del servidor
//
// Retorna:
//   - *HandshakeHoteleraService: instancia lista para usar
func NewHandshakeHoteleraService(db *sql.DB, cfg *config.Config) *HandshakeHoteleraService {
	return &HandshakeHoteleraService{
		repo:      repositories.NewProveedorRepository(db),
		serverURL: cfg.ServerURL,
	}
}

// IniciarHandshake
//
// Ejecuta el flujo completo de handshake con una hotelera proveedora.
// Obtiene la URL del proveedor, genera un token de entrada para la agencia,
// lo envia a la hotelera junto con la URL de la agencia, recibe el token
// de salida y el porcentaje de ganancia, y guarda ambos en BD.
//
// Parametros:
//   - proveedorID: identificador del proveedor hotelera con quien hacer handshake
//
// Retorna:
//   - string: token de salida recibido del proveedor hotelera
//   - error: si el proveedor no existe, no tiene URL, falla la generacion del token,
//     falla la comunicacion con la hotelera o falla el guardado en BD
func (s *HandshakeHoteleraService) IniciarHandshake(proveedorID int) (string, error) {

	// 1. Obtener URL de la hotelera
	urlAPI, err := s.repo.ObtenerURLAPI(proveedorID)
	if err != nil {
		return "", errors.New("proveedor no encontrado")
	}
	if urlAPI == "" {
		return "", errors.New("el proveedor no tiene URL_API configurada")
	}

	// 2. Generar nuestro token de entrada
	tokenEntrada, err := helpers.GenerarTokenHash()
	if err != nil {
		return "", errors.New("error generando token")
	}

	// 3. Enviar token a la hotelera y recibir su token y porcentaje
	respuesta, err := s.llamarHandshakeHotelera(urlAPI, tokenEntrada)
	if err != nil {
		return "", fmt.Errorf("error en handshake con hotelera: %w", err)
	}

	// 4. Guardar ambos tokens en la BD
	err = s.repo.GuardarTokens(proveedorID, tokenEntrada, respuesta.TokenSalida)
	if err != nil {
		return "", errors.New("error guardando tokens")
	}

	// 5. Actualizar porcentaje de ganancia con el valor del proveedor
	err = s.repo.ActualizarPorcentajeGanancia(proveedorID, respuesta.PorcentajeGanancia)
	if err != nil {
		return "", errors.New("error actualizando porcentaje de ganancia")
	}

	return respuesta.TokenSalida, nil
}

// llamarHandshakeHotelera
//
// Realiza la llamada HTTP POST al endpoint de handshake de la hotelera,
// enviando el token de entrada de la agencia y su URL publica. Retorna
// la respuesta completa con token y porcentaje de ganancia.
//
// Parametros:
//   - urlAPI: URL base del API del proveedor hotelera
//   - tokenEntrada: token generado por la agencia para identificarse ante la hotelera
//
// Retorna:
//   - *dto.HandshakeResponse: respuesta con token de salida y porcentaje de ganancia
//   - error: si la peticion HTTP falla, la hotelera retorna error o no incluye token_salida
func (s *HandshakeHoteleraService) llamarHandshakeHotelera(urlAPI, tokenEntrada string) (*dto.HandshakeResponse, error) {
	body, _ := json.Marshal(map[string]string{
		"token_entrada": tokenEntrada,
		"url_agencia":   s.serverURL,
	})

	resp, err := http.Post(urlAPI+"/api/agencias/handshake", "application/json", bytes.NewBuffer(body))
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("la hotelera respondió con status %d", resp.StatusCode)
	}

	var resultado dto.HandshakeResponse
	if err := json.NewDecoder(resp.Body).Decode(&resultado); err != nil {
		return nil, errors.New("respuesta inválida de la hotelera")
	}

	if resultado.TokenSalida == "" {
		return nil, errors.New("la hotelera no devolvió token_salida")
	}

	return &resultado, nil
}
