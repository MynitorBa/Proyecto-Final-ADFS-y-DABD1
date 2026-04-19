package helpers

import (
	"encoding/json"
	"io"
	"net/http"
	"strings"
)

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
