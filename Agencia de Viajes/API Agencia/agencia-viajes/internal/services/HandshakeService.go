// internal/services/handshake_service.go
package services

import (
	"agencia-viajes/internal/config"
	"agencia-viajes/internal/helpers"
	"agencia-viajes/internal/repositories"
	"bytes"
	"database/sql"
	"encoding/json"
	"errors"
	"fmt"
	"net/http"
)

type HandshakeService struct {
	repo      *repositories.ProveedorRepository
	serverURL string
}

func NewHandshakeService(db *sql.DB, cfg *config.Config) *HandshakeService {
	return &HandshakeService{
		repo:      repositories.NewProveedorRepository(db),
		serverURL: cfg.ServerURL,
	}
}

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

	// 3. Enviar token a la aerolinea y recibir su token
	tokenSalida, err := s.llamarHandshakeAerolinea(urlAPI, tokenEntrada)
	if err != nil {
		return "", fmt.Errorf("error en handshake con aerolinea: %w", err)
	}

	// 4. Guardar ambos tokens en la BD
	err = s.repo.GuardarTokens(proveedorID, tokenEntrada, tokenSalida)
	if err != nil {
		return "", errors.New("error guardando tokens")
	}

	return tokenSalida, nil
}

func (s *HandshakeService) llamarHandshakeAerolinea(urlAPI, tokenEntrada string) (string, error) {
	body, _ := json.Marshal(map[string]string{
		"token_entrada": tokenEntrada,
		"url_agencia":   s.serverURL,
	})

	resp, err := http.Post(urlAPI+"/api/agencias/handshake", "application/json", bytes.NewBuffer(body))
	if err != nil {
		return "", err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return "", fmt.Errorf("la aerolinea respondió con status %d", resp.StatusCode)
	}

	var resultado map[string]string
	if err := json.NewDecoder(resp.Body).Decode(&resultado); err != nil {
		return "", errors.New("respuesta inválida de la aerolinea")
	}

	tokenSalida, ok := resultado["token_salida"]
	if !ok || tokenSalida == "" {
		return "", errors.New("la aerolinea no devolvió token_salida")
	}

	return tokenSalida, nil
}
