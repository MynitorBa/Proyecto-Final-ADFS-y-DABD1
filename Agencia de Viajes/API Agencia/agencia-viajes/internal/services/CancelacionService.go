// # Package services
//
// Servicios de negocio de la agencia de viajes. Este paquete contiene la logica
// central para reservaciones, busquedas, autenticacion, catalogos y comunicacion
// con proveedores externos (aerolineas y hoteleras).
package services

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/helpers"
	"agencia-viajes/internal/repositories"
	"bytes"
	"crypto/tls"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"log"
	"net/http"
)

// CancelacionService
//
// Servicio encargado de gestionar el proceso de cancelacion de reservaciones.
// Implementa un flujo de dos pasos: primero verifica si todos los proveedores
// involucrados permiten la cancelacion, y luego ejecuta la cancelacion en cada
// proveedor y en la base de datos local de forma atomica (todo o nada).
type CancelacionService struct {
	repo *repositories.CancelacionRepository
}

// NewCancelacionService
//
// Crea e inicializa una nueva instancia de CancelacionService con el repositorio
// de cancelacion proporcionado.
//
// Parametros:
//   - repo: repositorio de cancelacion ya inicializado
//
// Retorna:
//   - *CancelacionService: instancia lista para usar
func NewCancelacionService(repo *repositories.CancelacionRepository) *CancelacionService {
	return &CancelacionService{repo: repo}
}

// VerificarCancelacion
//
// Paso 1 del flujo de cancelacion. Verifica si una reservacion puede ser cancelada
// consultando el estado local y luego preguntando a cada proveedor externo
// involucrado. Si cualquier proveedor rechaza la cancelacion, el resultado global
// indica que no es posible cancelar.
//
// Parametros:
//   - reservacionID: identificador de la reservacion a verificar
//   - usuarioID: identificador del usuario dueno de la reservacion
//
// Retorna:
//   - *dto.VerificarCancelacionResponse: resultado con flag global y detalle por proveedor
//   - error: si la reservacion no existe, no pertenece al usuario o falla la BD
func (s *CancelacionService) VerificarCancelacion(reservacionID, usuarioID int) (*dto.VerificarCancelacionResponse, error) {
	// 1. Verificar que existe y pertenece al usuario
	estadoID, _, err := s.repo.ObtenerReservacionParaCancelar(reservacionID, usuarioID)
	if err != nil {
		return nil, err
	}

	// 2. Verificar estado cancelable
	if estadoID != 1 && estadoID != 2 {
		return &dto.VerificarCancelacionResponse{
			PuedeCancelar: false,
			Detalles:      []dto.VerificarDetalleResponse{},
		}, nil
	}

	// 3. Obtener detalles y consultar cada proveedor
	detalles, err := s.repo.ObtenerDetallesParaCancelar(reservacionID)
	if err != nil {
		return nil, err
	}

	respuesta := &dto.VerificarCancelacionResponse{
		PuedeCancelar: true,
		Detalles:      []dto.VerificarDetalleResponse{},
	}

	for _, d := range detalles {
		resultado, err := s.consultarPuedeCancelar(d)
		detalle := dto.VerificarDetalleResponse{
			TipoDetalleID:      d.TipoDetalleID,
			IDReservaProveedor: d.IDReservaProveedor,
		}

		if err != nil {
			log.Printf("[Cancelacion] Error verificando proveedor %s (ID=%d, tipo=%d): %v",
				d.NombreProveedor, d.ProveedorID, d.TipoDetalleID, err)
			detalle.PuedeCancelar = false
			detalle.Razon = helpers.ErrorProveedorUsuario(
				d.NombreProveedor, helpers.TipoProveedorStr(d.TipoDetalleID), err, "verificar cancelacion").Error()
		} else {
			detalle.PuedeCancelar = resultado.PuedeCancelar
			detalle.Razon = resultado.Razon
		}

		// Si cualquier detalle no puede cancelarse, el global es false
		if !detalle.PuedeCancelar {
			respuesta.PuedeCancelar = false
		}

		respuesta.Detalles = append(respuesta.Detalles, detalle)
	}

	return respuesta, nil
}

// CancelarReservacion
//
// Paso 2 del flujo de cancelacion. Ejecuta la cancelacion completa de una
// reservacion. Primero verifica el estado y re-valida con cada proveedor
// (rollback logico), luego cancela en cada proveedor externo y finalmente
// actualiza el estado en la base de datos local. Si algun proveedor rechaza
// la cancelacion, el proceso se detiene sin cancelar nada.
//
// Parametros:
//   - reservacionID: identificador de la reservacion a cancelar
//   - usuarioID: identificador del usuario dueno de la reservacion
//   - motivo: descripcion del motivo de cancelacion
//
// Retorna:
//   - noReservacion: numero de reservacion cancelada (ej. "RES-000123"), vacio si falla
//   - error: si la reservacion no es cancelable, algun proveedor rechaza o falla la BD
func (s *CancelacionService) CancelarReservacion(reservacionID, usuarioID int, motivo string) (noReservacion string, err error) {
	// 1. Verificar que existe, pertenece al usuario y estado es cancelable
	estadoID, noReservacion, err := s.repo.ObtenerReservacionParaCancelar(reservacionID, usuarioID)
	if err != nil {
		return "", err
	}
	if estadoID != 1 && estadoID != 2 {
		return "", errors.New("la reservación no puede cancelarse en su estado actual")
	}

	// 2. Obtener detalles
	detalles, err := s.repo.ObtenerDetallesParaCancelar(reservacionID)
	if err != nil {
		return "", err
	}

	// 3. Verificar TODOS antes de cancelar (rollback lógico)
	for _, d := range detalles {
		resultado, err := s.consultarPuedeCancelar(d)
		if err != nil {
			log.Printf("[Cancelacion] Error verificando proveedor %s (ID=%d): %v", d.NombreProveedor, d.ProveedorID, err)
			return "", helpers.ErrorProveedorUsuario(
				d.NombreProveedor, helpers.TipoProveedorStr(d.TipoDetalleID), err, "verificar cancelacion")
		}
		if !resultado.PuedeCancelar {
			return "", fmt.Errorf("no se puede cancelar: %s", resultado.Razon)
		}
	}

	// 4. Cancelar en cada proveedor
	for _, d := range detalles {
		if err := s.cancelarEnProveedor(d, motivo); err != nil {
			log.Printf("[Cancelacion] Error cancelando en proveedor %s (ID=%d): %v", d.NombreProveedor, d.ProveedorID, err)
			return "", err
		}
	}

	// 5. Cancelar en BD local (incluye INSERT de notificacion in-app dentro de la transaccion)
	if err := s.repo.CancelarReservacion(reservacionID, motivo); err != nil {
		return "", err
	}

	// 6. Enviar correo de confirmacion de cancelacion (no bloqueante)
	//    La transaccion ya fue exitosa; si falla el correo no afecta la respuesta HTTP.
	go func(rID int, motv string) {
		correo, nombre, apellido, noRes, errDatos := s.repo.ObtenerDatosCorreoReservacion(rID)
		if errDatos != nil {
			log.Printf("[Cancelacion] error obteniendo datos de correo para reservacion %d: %v", rID, errDatos)
			return
		}
		htmlBody := helpers.BuildHTMLCancelacionUsuario(nombre, apellido, noRes, motv)
		asunto := fmt.Sprintf("MOVENT · Cancelacion de tu reservacion %s", noRes)
		if errEmail := helpers.EnviarEmailHTML(correo, asunto, htmlBody); errEmail != nil {
			log.Printf("[Cancelacion] error enviando correo a %s para reservacion %d: %v", correo, rID, errEmail)
		}
	}(reservacionID, motivo)

	return noReservacion, nil
}

// consultarPuedeCancelar
//
// Consulta al proveedor externo (aerolinea o hotelera) si una reservacion
// especifica puede ser cancelada. Construye la URL correcta segun el tipo
// de detalle y realiza una peticion GET con el token de autenticacion.
//
// Parametros:
//   - d: datos del detalle del proveedor incluyendo tipo, URL, token e ID de reserva
//
// Retorna:
//   - *dto.ProveedorPuedeCancelarResponse: respuesta del proveedor con flag y razon
//   - error: si el tipo de detalle es desconocido, falla la peticion o el JSON es invalido
func (s *CancelacionService) consultarPuedeCancelar(d dto.DetalleProveedor) (*dto.ProveedorPuedeCancelarResponse, error) {
	var url string
	switch d.TipoDetalleID {
	case 1: // Aerolínea
		url = fmt.Sprintf("%s/api/reservaciones-agencia/gestion/%s/puede-cancelar", d.URLAPI, d.IDReservaProveedor)
	case 2: // Hotel
		url = fmt.Sprintf("%s/agencia/reservaciones/%s/puede-cancelar", d.URLAPI, d.IDReservaProveedor)
	default:
		return nil, fmt.Errorf("tipo de detalle desconocido: %d", d.TipoDetalleID)
	}

	req, err := http.NewRequest("GET", url, nil)
	if err != nil {
		return nil, helpers.ErrorProveedorUsuario(
			d.NombreProveedor, helpers.TipoProveedorStr(d.TipoDetalleID), err, "verificar cancelacion")
	}
	req.Header.Set("X-Agencia-Token", d.TokenEntrada)

	resp, err := httpClient().Do(req)
	if err != nil {
		return nil, helpers.ErrorProveedorUsuario(
			d.NombreProveedor, helpers.TipoProveedorStr(d.TipoDetalleID), err, "verificar cancelacion")
	}
	defer resp.Body.Close()

	body, _ := io.ReadAll(resp.Body)
	var resultado dto.ProveedorPuedeCancelarResponse
	if err := json.Unmarshal(body, &resultado); err != nil {
		return nil, helpers.ErrorProveedorUsuario(
			d.NombreProveedor, helpers.TipoProveedorStr(d.TipoDetalleID), err, "verificar cancelacion")
	}
	return &resultado, nil
}

// cancelarEnProveedor
//
// Ejecuta la cancelacion de una reservacion en el sistema del proveedor externo.
// Construye la URL y el metodo HTTP correcto segun el tipo de detalle:
// POST para aerolineas y PATCH para hoteleras, enviando el motivo de cancelacion
// en el cuerpo de la peticion.
//
// Parametros:
//   - d: datos del detalle del proveedor incluyendo tipo, URL, token e ID de reserva
//   - motivo: descripcion del motivo de cancelacion a enviar al proveedor
//
// Retorna:
//   - error: si el tipo es desconocido, falla la peticion o el proveedor retorna error HTTP
func (s *CancelacionService) cancelarEnProveedor(d dto.DetalleProveedor, motivo string) error {
	var url, method string
	var bodyBytes []byte
	tipoStr := helpers.TipoProveedorStr(d.TipoDetalleID)

	switch d.TipoDetalleID {
	case 1: // Aerolínea — POST con {"motivo": "..."}
		url = fmt.Sprintf("%s/api/reservaciones-agencia/gestion/%s/cancelar", d.URLAPI, d.IDReservaProveedor)
		method = "POST"
		bodyBytes, _ = json.Marshal(map[string]string{"motivo": motivo})
	case 2: // Hotel — PATCH con {"motivoCancelacion": "..."}
		url = fmt.Sprintf("%s/agencia/reservaciones/%s/cancelar", d.URLAPI, d.IDReservaProveedor)
		method = "PATCH"
		bodyBytes, _ = json.Marshal(map[string]string{"motivoCancelacion": motivo})
	default:
		return fmt.Errorf("tipo de detalle desconocido: %d", d.TipoDetalleID)
	}

	req, err := http.NewRequest(method, url, bytes.NewBuffer(bodyBytes))
	if err != nil {
		return helpers.ErrorProveedorUsuario(d.NombreProveedor, tipoStr, err, "cancelar")
	}
	req.Header.Set("X-Agencia-Token", d.TokenEntrada)
	req.Header.Set("Content-Type", "application/json")

	resp, err := httpClient().Do(req)
	if err != nil {
		return helpers.ErrorProveedorUsuario(d.NombreProveedor, tipoStr, err, "cancelar")
	}
	defer resp.Body.Close()

	if resp.StatusCode >= 400 {
		body, _ := io.ReadAll(resp.Body)
		log.Printf("[Cancelacion] HTTP %d al cancelar en proveedor %s (ID=%d): %s",
			resp.StatusCode, d.NombreProveedor, d.ProveedorID, string(body))
		return helpers.ErrorProveedorUsuario(d.NombreProveedor, tipoStr, nil, "cancelar")
	}
	return nil
}

// httpClient
//
// Crea y retorna un cliente HTTP configurado para omitir la verificacion
// de certificados TLS. Se usa para comunicacion con proveedores externos
// que pueden tener certificados autofirmados o en entornos de desarrollo.
//
// Retorna:
//   - *http.Client: cliente HTTP con TLS InsecureSkipVerify habilitado
//
// Notas:
//   - No usar en produccion con datos sensibles sin certificados validos
func httpClient() *http.Client {
	return &http.Client{
		Transport: &http.Transport{
			TLSClientConfig: &tls.Config{InsecureSkipVerify: true},
		},
	}
}
