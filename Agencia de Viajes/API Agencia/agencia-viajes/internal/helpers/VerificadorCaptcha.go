// # Package helpers
//
// Provee funciones auxiliares reutilizables para tareas comunes de la
// aplicacion Movent: generacion de tokens, hashing de contrasenas,
// manejo de sesiones JWT, envio de correos electronicos y generacion
// de documentos PDF.
package helpers

import (
	"encoding/json"
	"net/http"
	"net/url"
	"os"
	"strings"
	"time"
)

// GoogleCaptchaResponse
//
// Representa la respuesta JSON de la API de verificacion de Google
// para reCAPTCHA v2. El campo ErrorCodes esta presente solo cuando
// Success es false.
type GoogleCaptchaResponse struct {
	Success     bool     `json:"success"`
	ChallengeTS string   `json:"challenge_ts"`
	Hostname    string   `json:"hostname"`
	ErrorCodes  []string `json:"error-codes"`
}

// VerificarCaptcha
//
// Envia el token del cliente a la API de Google siteverify para validar
// que el reCAPTCHA v2 fue resuelto correctamente. Usa la clave secreta
// configurada en la variable de entorno RECAPTCHA_SECRET_KEY.
//
// Parametros:
//   - token: string con el token generado por el widget de reCAPTCHA
//     en el navegador del cliente
//
// Retorna:
//   - valido: true si Google confirma que el token es valido y el
//     desafio fue superado
//   - errorMsg: mensaje descriptivo en espanol en caso de falla;
//     incluye los error-codes de Google o el error de red
//
// Notas:
//   - Timeout de 5 segundos para la peticion HTTP a Google
//   - Si RECAPTCHA_SECRET_KEY no esta configurada en el entorno,
//     retorna (false, "RECAPTCHA_SECRET_KEY no configurada") sin
//     realizar ninguna peticion externa
func VerificarCaptcha(token string) (bool, string) {
	secretKey := os.Getenv("RECAPTCHA_SECRET_KEY")
	if secretKey == "" {
		return false, "RECAPTCHA_SECRET_KEY no configurada"
	}

	client := &http.Client{Timeout: 5 * time.Second}

	formData := url.Values{}
	formData.Set("secret", secretKey)
	formData.Set("response", token)

	resp, err := client.PostForm(
		"https://www.google.com/recaptcha/api/siteverify",
		formData,
	)
	if err != nil {
		return false, "Error verificando captcha: " + err.Error()
	}
	defer resp.Body.Close()

	var resultado GoogleCaptchaResponse
	if err := json.NewDecoder(resp.Body).Decode(&resultado); err != nil {
		return false, "Error verificando captcha: " + err.Error()
	}

	if resultado.Success {
		return true, ""
	}

	return false, "Captcha inválido: " + strings.Join(resultado.ErrorCodes, ", ")
}
