// # Package helpers
//
// Provee funciones auxiliares reutilizables para tareas comunes de la
// aplicacion Movent: generacion de tokens, hashing de contrasenas,
// manejo de sesiones JWT, envio de correos electronicos y generacion
// de documentos PDF.
package helpers

import (
	"agencia-viajes/internal/dto"
	"fmt"
	"regexp"
	"strings"
	"time"
)

// Expresiones regulares precompiladas para validacion de registro.
// Se compilan una sola vez al iniciar el proceso, evitando el costo
// de compilacion en cada llamada a ValidarRegistro.
var (
	regexEmail     = regexp.MustCompile(`^[^\s@]+@[^\s@]+\.[^\s@]+$`)
	regexUsername  = regexp.MustCompile(`^[a-zA-Z0-9_.]+$`)
	regexMayuscula = regexp.MustCompile(`[A-Z]`)
	regexMinuscula = regexp.MustCompile(`[a-z]`)
	regexNumero    = regexp.MustCompile(`[0-9]`)
)

// calcularEdad
//
// Parsea una fecha en formato "2006-01-02" y calcula la edad en años
// completos respecto a la fecha actual, considerando correctamente si
// el cumpleanos ya ocurrio este anio.
//
// Parametros:
//   - fechaISO: fecha de nacimiento en formato "YYYY-MM-DD"
//
// Retorna:
//   - int: edad en anios completos
//   - error: si el formato de la fecha es invalido
func calcularEdad(fechaISO string) (int, error) {
	nacimiento, err := time.Parse("2006-01-02", fechaISO)
	if err != nil {
		return 0, err
	}
	ahora := time.Now()
	edad := ahora.Year() - nacimiento.Year()
	if ahora.Month() < nacimiento.Month() ||
		(ahora.Month() == nacimiento.Month() && ahora.Day() < nacimiento.Day()) {
		edad--
	}
	return edad, nil
}

// ValidarRegistro
//
// Valida las reglas de negocio del payload de registro de usuario antes
// de que llegue al servicio. Es la fuente de verdad del backend para
// prevenir bypass de validaciones del frontend via Postman o curl.
//
// Las validaciones se ejecutan en orden de prioridad y se detienen en
// el primer error encontrado, de modo que el log registra el motivo exacto.
// El orden es: campos requeridos → pasaporte → email → username →
// telefono → edad minima → contrasena fuerte.
//
// Parametros:
//   - req: DTO con los datos del formulario de registro
//
// Retorna:
//   - tipoEvento: ID del tipo de evento que fallo (0 si valido=true)
//   - mensaje: descripcion clara del error en espanol, para mostrar al
//     usuario y registrar en log_sesion.Mensaje
//   - valido: true si todas las validaciones pasaron correctamente
func ValidarRegistro(req dto.RegistroUsuarioRequest) (tipoEvento int, mensaje string, valido bool) {

	// ── 1. Campos requeridos básicos ─────────────────────────────────────────

	nombreTrim := strings.TrimSpace(req.Nombre)
	if len(nombreTrim) < 2 {
		return TipoRegistroFallidoCamposRequeridos, "Nombre debe tener al menos 2 caracteres", false
	}
	if len(nombreTrim) > 100 {
		return TipoRegistroFallidoCamposRequeridos, "Nombre demasiado largo", false
	}

	apellidoTrim := strings.TrimSpace(req.Apellido)
	if len(apellidoTrim) < 2 {
		return TipoRegistroFallidoCamposRequeridos, "Apellido debe tener al menos 2 caracteres", false
	}
	if len(apellidoTrim) > 100 {
		return TipoRegistroFallidoCamposRequeridos, "Apellido demasiado largo", false
	}

	if strings.TrimSpace(req.Pais) == "" {
		return TipoRegistroFallidoCamposRequeridos, "País requerido", false
	}

	if strings.TrimSpace(req.Ciudad) == "" {
		return TipoRegistroFallidoCamposRequeridos, "Ciudad requerida", false
	}

	if strings.TrimSpace(req.FechaNacimiento) == "" {
		return TipoRegistroFallidoCamposRequeridos, "Fecha de nacimiento requerida", false
	}

	if len(req.Nacionalidades) == 0 {
		return TipoRegistroFallidoCamposRequeridos, "Debes seleccionar al menos una nacionalidad", false
	}
	for _, n := range req.Nacionalidades {
		if strings.TrimSpace(n) == "" {
			return TipoRegistroFallidoCamposRequeridos, "Nacionalidad inválida", false
		}
	}

	// ── 2. Pasaporte ─────────────────────────────────────────────────────────

	pasaporteTrim := strings.TrimSpace(req.Pasaporte)
	if pasaporteTrim == "" {
		return TipoRegistroFallidoPasaporteInvalido, "Pasaporte requerido", false
	}
	if len(pasaporteTrim) < 5 {
		return TipoRegistroFallidoPasaporteInvalido, "Pasaporte debe tener al menos 5 caracteres", false
	}
	if len(pasaporteTrim) > 50 {
		return TipoRegistroFallidoPasaporteInvalido, "Pasaporte demasiado largo", false
	}

	// ── 3. Email ─────────────────────────────────────────────────────────────

	correoTrim := strings.TrimSpace(req.Correo)
	if correoTrim == "" {
		return TipoRegistroFallidoEmailInvalido, "Correo electrónico requerido", false
	}
	if len(correoTrim) > 150 {
		return TipoRegistroFallidoEmailInvalido, "Correo demasiado largo", false
	}
	if !regexEmail.MatchString(correoTrim) {
		return TipoRegistroFallidoEmailInvalido, "Formato de correo inválido", false
	}

	// ── 4. Username ───────────────────────────────────────────────────────────

	usernameTrim := strings.TrimSpace(req.Username)
	if usernameTrim == "" {
		return TipoRegistroFallidoUsernameInvalido, "Username requerido", false
	}
	if len(usernameTrim) < 3 {
		return TipoRegistroFallidoUsernameInvalido, "Username debe tener al menos 3 caracteres", false
	}
	if len(usernameTrim) > 100 {
		return TipoRegistroFallidoUsernameInvalido, "Username demasiado largo", false
	}
	if !regexUsername.MatchString(usernameTrim) {
		return TipoRegistroFallidoUsernameInvalido, "Username solo permite letras, números, puntos y guion bajo", false
	}

	// ── 5. Teléfono ───────────────────────────────────────────────────────────

	telefonoTrim := strings.TrimSpace(req.Telefono)
	if telefonoTrim == "" {
		return TipoRegistroFallidoTelefonoInvalido, "Teléfono requerido", false
	}
	if len(telefonoTrim) > 25 {
		return TipoRegistroFallidoTelefonoInvalido, "Teléfono demasiado largo (máximo 25 caracteres)", false
	}

	// Usar el validador compartido por país (misma fuente de verdad que ActualizarTelefono)
	ok, digitosDetectados, esperados := ValidarDigitosTelefono(telefonoTrim, req.Pais)
	if !ok {
		if req.Pais == "" {
			return TipoRegistroFallidoTelefonoInvalido,
				fmt.Sprintf("Teléfono debe tener al menos %d dígitos", esperados), false
		}
		return TipoRegistroFallidoTelefonoInvalido,
			fmt.Sprintf("Teléfono para %s debe tener exactamente %d dígitos locales (detectados: %d)",
				req.Pais, esperados, digitosDetectados), false
	}

	// ── 6. Edad mínima ────────────────────────────────────────────────────────

	nacimiento, err := time.Parse("2006-01-02", strings.TrimSpace(req.FechaNacimiento))
	if err != nil {
		return TipoRegistroFallidoCamposRequeridos, "Fecha de nacimiento inválida", false
	}
	if nacimiento.After(time.Now()) {
		return TipoRegistroFallidoCamposRequeridos, "Fecha de nacimiento inválida", false
	}
	if nacimiento.Year() < 1900 {
		return TipoRegistroFallidoCamposRequeridos, "Fecha de nacimiento inválida", false
	}
	edad, _ := calcularEdad(strings.TrimSpace(req.FechaNacimiento))
	if edad < 18 {
		return TipoRegistroFallidoEdadMinima, "Debes tener al menos 18 años para registrarte", false
	}

	// ── 7. Contraseña ─────────────────────────────────────────────────────────

	if len(req.Contrasena) < 8 {
		return TipoRegistroFallidoContrasenaDebil, "La contraseña debe tener al menos 8 caracteres", false
	}
	if len(req.Contrasena) > 72 {
		return TipoRegistroFallidoContrasenaDebil, "La contraseña no puede exceder 72 caracteres", false
	}
	if !regexMayuscula.MatchString(req.Contrasena) {
		return TipoRegistroFallidoContrasenaDebil, "La contraseña debe contener al menos una mayúscula", false
	}
	if !regexMinuscula.MatchString(req.Contrasena) {
		return TipoRegistroFallidoContrasenaDebil, "La contraseña debe contener al menos una minúscula", false
	}
	if !regexNumero.MatchString(req.Contrasena) {
		return TipoRegistroFallidoContrasenaDebil, "La contraseña debe contener al menos un número", false
	}

	// ── Todo válido ───────────────────────────────────────────────────────────

	return 0, "", true
}
