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

// HandshakeService
//
// Servicio encargado de gestionar el proceso de handshake de autenticacion
// con proveedores de tipo aerolinea. Genera un token de entrada para la agencia,
// lo envia a la aerolinea y almacena ambos tokens (entrada y salida) en BD
// para su uso en comunicaciones posteriores.
type HandshakeService struct {
	repo      *repositories.ProveedorRepository
	serverURL string
}

// NewHandshakeService
//
// Crea e inicializa una nueva instancia de HandshakeService con su repositorio
// de proveedores y la URL publica de la agencia.
//
// Parametros:
//   - db: conexion activa a la base de datos SQL
//   - cfg: configuracion de la aplicacion que contiene la URL del servidor
//
// Retorna:
//   - *HandshakeService: instancia lista para usar
func NewHandshakeService(db *sql.DB, cfg *config.Config) *HandshakeService {
	return &HandshakeService{
		repo:      repositories.NewProveedorRepository(db),
		serverURL: cfg.ServerURL,
	}
}

// IniciarHandshake
//
// Ejecuta el flujo completo de handshake con una aerolinea proveedora.
// Obtiene la URL del proveedor, genera un token de entrada para la agencia,
// lo envia a la aerolinea junto con la URL de la agencia, recibe el token
// de salida y el porcentaje de ganancia, y guarda ambos en BD.
//
// Parametros:
//   - proveedorID: identificador del proveedor aerolinea con quien hacer handshake
//
// Retorna:
//   - string: token de salida recibido del proveedor aerolinea
//   - error: si el proveedor no existe, no tiene URL, falla la generacion del token,
//     falla la comunicacion con la aerolinea o falla el guardado en BD
func (s *HandshakeService) IniciarHandshake(proveedorID int) (string, error) {

	// 1. Obtener URL de la aerolinea
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

	// 3. Enviar token a la aerolinea y recibir su token y porcentaje
	respuesta, err := s.llamarHandshakeAerolinea(urlAPI, tokenEntrada)
	if err != nil {
		return "", fmt.Errorf("error en handshake con aerolinea: %w", err)
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

// llamarHandshakeAerolinea
//
// Realiza la llamada HTTP POST al endpoint de handshake de la aerolinea,
// enviando el token de entrada de la agencia y su URL publica. Retorna
// la respuesta completa con token y porcentaje de ganancia.
//
// Parametros:
//   - urlAPI: URL base del API del proveedor aerolinea
//   - tokenEntrada: token generado por la agencia para identificarse ante la aerolinea
//
// Retorna:
//   - *dto.HandshakeResponse: respuesta con token de salida y porcentaje de ganancia
//   - error: si la peticion HTTP falla, la aerolinea retorna error o no incluye token_salida
func (s *HandshakeService) llamarHandshakeAerolinea(urlAPI, tokenEntrada string) (*dto.HandshakeResponse, error) {
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
		return nil, fmt.Errorf("la aerolinea respondió con status %d", resp.StatusCode)
	}

	var resultado dto.HandshakeResponse
	if err := json.NewDecoder(resp.Body).Decode(&resultado); err != nil {
		return nil, errors.New("respuesta inválida de la aerolinea")
	}

	if resultado.TokenSalida == "" {
		return nil, errors.New("la aerolinea no devolvió token_salida")
	}

	return &resultado, nil
}
