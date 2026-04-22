package services

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/helpers"
	"agencia-viajes/internal/repositories"
	"bytes"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"log"
	"net/http"
)

// AdminCancelacionService
//
// Servicio que gestiona la cancelacion de reservaciones iniciada por un
// administrador desde el panel de administracion. A diferencia del
// CancelacionService (cancelacion por el usuario), aqui no se verifica
// la pertenencia de la reservacion a un usuario especifico: el administrador
// puede cancelar cualquier reservacion activa del sistema.
//
// El flujo es identico al del usuario en cuanto a la comunicacion con
// proveedores externos, pero el motivo se prefija con "[Admin]" para
// distinguirlo en la base de datos, y el correo enviado al cliente
// usa la plantilla administrativa.
type AdminCancelacionService struct {
	repo *repositories.CancelacionRepository
}

// NewAdminCancelacionService
//
// Crea e inicializa una nueva instancia del servicio.
//
// Parametros:
//   - repo: repositorio de cancelacion ya inicializado (compartido con CancelacionService)
//
// Retorna:
//   - *AdminCancelacionService: instancia lista para usar
func NewAdminCancelacionService(repo *repositories.CancelacionRepository) *AdminCancelacionService {
	return &AdminCancelacionService{repo: repo}
}

// CancelarReservacion
//
// Ejecuta el flujo completo de cancelacion administrativa:
//  1. Verifica que la reservacion exista y este en estado cancelable (1 o 2)
//  2. Obtiene los detalles de proveedores asociados
//  3. Consulta a cada proveedor si permite cancelar
//  4. Cancela en cada proveedor externo
//  5. Cancela en BD local (incluye notificacion in-app via transaccion)
//  6. Envia correo de notificacion al usuario (goroutine no bloqueante)
//
// El motivo se almacena con el prefijo "[Admin]" para diferenciarlo de
// cancelaciones del usuario en la base de datos y en los logs.
//
// Parametros:
//   - reservacionID: ID de la reservacion a cancelar
//   - motivoAdmin:   motivo ingresado por el administrador (sin prefijo)
//
// Retorna:
//   - noReservacion: numero de reservacion cancelada
//   - error: si la reservacion no existe, no es cancelable, algun proveedor
//     rechaza o falla alguna operacion de BD
func (s *AdminCancelacionService) CancelarReservacion(reservacionID int, motivoAdmin string) (noReservacion string, err error) {
	// 1. Verificar que la reservacion existe (sin restriccion de usuario)
	estadoID, _, noReservacion, err := s.repo.ObtenerReservacionParaAdmin(reservacionID)
	if err != nil {
		return "", err
	}
	if estadoID != 1 && estadoID != 2 {
		return "", errors.New("la reservacion no puede cancelarse en su estado actual")
	}

	// 2. Obtener detalles de proveedores activos
	detalles, err := s.repo.ObtenerDetallesParaCancelar(reservacionID)
	if err != nil {
		return "", err
	}

	// 3. Verificar que todos los proveedores permitan cancelar
	for _, d := range detalles {
		resultado, err := s.consultarPuedeCancelar(d)
		if err != nil {
			log.Printf("[AdminCancelacion] Error verificando proveedor %s (ID=%d): %v", d.NombreProveedor, d.ProveedorID, err)
			return "", helpers.ErrorProveedorUsuario(
				d.NombreProveedor, helpers.TipoProveedorStr(d.TipoDetalleID), err, "verificar cancelacion")
		}
		if !resultado.PuedeCancelar {
			return "", fmt.Errorf("no se puede cancelar: %s", resultado.Razon)
		}
	}

	// 4. Cancelar en cada proveedor externo
	motivo := "[Admin] " + motivoAdmin
	for _, d := range detalles {
		if err := s.cancelarEnProveedor(d, motivo); err != nil {
			log.Printf("[AdminCancelacion] Error cancelando en proveedor %s (ID=%d): %v", d.NombreProveedor, d.ProveedorID, err)
			return "", err
		}
	}

	// 5. Cancelar en BD local (incluye INSERT de notificacion in-app dentro de la transaccion)
	if err := s.repo.CancelarReservacion(reservacionID, motivo); err != nil {
		return "", err
	}

	// 6. Enviar correo de notificacion al usuario (no bloqueante)
	go func(rID int, motv string) {
		correo, nombre, apellido, noRes, errDatos := s.repo.ObtenerDatosCorreoReservacion(rID)
		if errDatos != nil {
			log.Printf("[AdminCancelacion] error obteniendo datos de correo para reservacion %d: %v", rID, errDatos)
			return
		}
		htmlBody := helpers.BuildHTMLCancelacionAdmin(nombre, apellido, noRes, motv)
		asunto := fmt.Sprintf("MOVENT · Cancelacion de tu reservacion %s", noRes)
		if errEmail := helpers.EnviarEmailHTML(correo, asunto, htmlBody); errEmail != nil {
			log.Printf("[AdminCancelacion] error enviando correo a %s para reservacion %d: %v", correo, rID, errEmail)
		}
	}(reservacionID, motivoAdmin)

	return noReservacion, nil
}

// consultarPuedeCancelar
//
// Consulta al proveedor externo si una reservacion especifica puede ser
// cancelada. Replica la logica de CancelacionService para el contexto
// de cancelacion administrativa.
//
// Parametros:
//   - d: detalle del proveedor con tipo, URL, token e ID de reserva
//
// Retorna:
//   - *dto.ProveedorPuedeCancelarResponse: respuesta del proveedor
//   - error: si el tipo es desconocido, falla la peticion o el JSON es invalido
func (s *AdminCancelacionService) consultarPuedeCancelar(d dto.DetalleProveedor) (*dto.ProveedorPuedeCancelarResponse, error) {
	var url string
	switch d.TipoDetalleID {
	case 1: // Aerolinea
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
// Ejecuta la cancelacion de una reservacion en el sistema del proveedor
// externo. Replica la logica de CancelacionService para el contexto
// de cancelacion administrativa.
//
// Parametros:
//   - d:      detalle del proveedor con tipo, URL, token e ID de reserva
//   - motivo: motivo de cancelacion (ya con prefijo "[Admin]")
//
// Retorna:
//   - error: si el tipo es desconocido, falla la peticion o el proveedor rechaza
func (s *AdminCancelacionService) cancelarEnProveedor(d dto.DetalleProveedor, motivo string) error {
	var url, method string
	var bodyBytes []byte
	tipoStr := helpers.TipoProveedorStr(d.TipoDetalleID)

	switch d.TipoDetalleID {
	case 1: // Aerolinea — POST con {"motivo": "..."}
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
		log.Printf("[AdminCancelacion] HTTP %d al cancelar en proveedor %s (ID=%d): %s",
			resp.StatusCode, d.NombreProveedor, d.ProveedorID, string(body))
		return helpers.ErrorProveedorUsuario(d.NombreProveedor, tipoStr, nil, "cancelar")
	}
	return nil
}
