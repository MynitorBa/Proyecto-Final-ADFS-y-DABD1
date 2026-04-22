package helpers

import (
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"net/http"
	"strings"
)

// ErrorProveedorUsuario convierte un error tecnico de comunicacion con un
// proveedor externo en un mensaje amigable para el usuario final.
// El error tecnico completo debe registrarse por el llamador antes de invocar
// esta funcion, ya que aqui se descarta el detalle tecnico del mensaje.
//
// Categorias de error:
//   - Red no disponible (dial tcp, connectex, connection refused, no such host):
//     "{nombre} ({tipo}) no esta disponible en este momento. Intenta mas tarde."
//   - Timeout (timeout, deadline exceeded):
//     "{nombre} ({tipo}) no respondio a tiempo. Intenta nuevamente en unos minutos."
//   - Rechazo HTTP (4xx/5xx):
//     "{nombre} ({tipo}) rechazo la operacion de {accion}. Contacta al administrador si el problema persiste."
//   - Otro:
//     "No se pudo completar {accion} con {nombre} ({tipo}). Intenta mas tarde."
//
// Parametros:
//   - nombre:      nombre del proveedor (ej. "Broom AirLine")
//   - tipo:        tipo legible del proveedor (ej. "Aerolinea", "Hotelera")
//   - errTecnico:  error original de Go (puede ser nil para el caso HTTP)
//   - accion:      descripcion de la operacion fallida (ej. "verificar cancelacion")
//
// Retorna:
//   - error: mensaje de error amigable para el usuario
func ErrorProveedorUsuario(nombre, tipo string, errTecnico error, accion string) error {
	if errTecnico != nil {
		msg := strings.ToLower(errTecnico.Error())
		switch {
		case strings.Contains(msg, "dial tcp"),
			strings.Contains(msg, "connectex"),
			strings.Contains(msg, "connection refused"),
			strings.Contains(msg, "no such host"):
			return fmt.Errorf("%s (%s) no esta disponible en este momento. Intenta mas tarde", nombre, tipo)
		case strings.Contains(msg, "timeout"),
			strings.Contains(msg, "deadline exceeded"):
			return fmt.Errorf("%s (%s) no respondio a tiempo. Intenta nuevamente en unos minutos", nombre, tipo)
		default:
			return fmt.Errorf("no se pudo completar %s con %s (%s). Intenta mas tarde", accion, nombre, tipo)
		}
	}
	return fmt.Errorf("%s (%s) rechazo la operacion de %s. Contacta al administrador si el problema persiste", nombre, tipo, accion)
}

// TipoProveedorStr convierte el ID de tipo de detalle en una cadena legible
// para el usuario. Utilizado al construir mensajes de error amigables.
//
// Parametros:
//   - tipoDetalleID: 1=Aerolinea, 2=Hotelera
//
// Retorna:
//   - string: nombre del tipo de proveedor
func TipoProveedorStr(tipoDetalleID int) string {
	switch tipoDetalleID {
	case 1:
		return "Aerolinea"
	case 2:
		return "Hotelera"
	default:
		return "Proveedor"
	}
}

// ErrProveedorHTTP es un centinela para indicar que el error proviene de
// un codigo de estado HTTP 4xx/5xx del proveedor (no un error de red).
var ErrProveedorHTTP = errors.New("error http del proveedor")

// ParseErrorProveedor extrae el mensaje de error de una respuesta HTTP
// fallida del proveedor. Intenta deserializar a JSON con campos
// "mensaje" o "error"; si falla, usa el body crudo. Trunca a 200 chars.
func ParseErrorProveedor(resp *http.Response) string {
	if resp == nil || resp.Body == nil {
		return "sin respuesta"
	}
	bodyBytes, err := io.ReadAll(resp.Body)
	if err != nil || len(bodyBytes) == 0 {
		return "cuerpo vacío"
	}

	var errResp struct {
		Mensaje string `json:"mensaje"`
		Error   string `json:"error"`
	}
	if json.Unmarshal(bodyBytes, &errResp) == nil {
		msg := errResp.Mensaje
		if msg == "" {
			msg = errResp.Error
		}
		if msg != "" {
			return truncar(msg, 200)
		}
	}

	return truncar(strings.TrimSpace(string(bodyBytes)), 200)
}

func truncar(s string, n int) string {
	if len(s) <= n {
		return s
	}
	return s[:n] + "..."
}
