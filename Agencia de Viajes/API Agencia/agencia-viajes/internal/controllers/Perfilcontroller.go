// # Package controllers
//
// Controladores HTTP de la API de Movent. Cada controlador agrupa los handlers
// relacionados a un recurso o dominio especifico de la aplicacion.
package controllers

import (
	"agencia-viajes/internal/helpers"
	"agencia-viajes/internal/services"
	"fmt"
	"net/http"
	"regexp"
	"strings"

	"github.com/gin-gonic/gin"
)

// telefonoValidoRegex valida que el telefono solo contenga el simbolo +, digitos,
// espacios, guiones y parentesis (formato internacional estandar).
var telefonoValidoRegex = regexp.MustCompile(`^\+[0-9\s\-\(\)]+$`)

// PerfilController
//
// Controlador que maneja los endpoints de consulta y actualizacion del
// perfil del usuario autenticado, incluyendo telefono y contrasena.
type PerfilController struct {
	service   *services.PerfilService
	logSesion *services.LogSesionService
}

// NewPerfilController
//
// Constructor que retorna una nueva instancia de PerfilController
// con el servicio de perfil y el servicio de auditoria inyectados.
//
// Parametros:
//   - service:   puntero al servicio de perfil
//   - logSesion: puntero al servicio de auditoria de sesion
//
// Retorna:
//   - *PerfilController: puntero a la nueva instancia
func NewPerfilController(service *services.PerfilService, logSesion *services.LogSesionService) *PerfilController {
	return &PerfilController{service: service, logSesion: logSesion}
}

// ObtenerPerfil
//
// Retorna los datos del perfil del usuario actualmente autenticado.
// El ID del usuario se extrae del contexto de Gin inyectado por el middleware.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200 OK: JSON con los datos del perfil del usuario
//   - HTTP 500 Internal Server Error: si ocurre un error al obtener el perfil
func (ctrl *PerfilController) ObtenerPerfil(c *gin.Context) {
	usuarioID := c.GetInt("usuario_id")
	perfil, err := ctrl.service.ObtenerPerfil(usuarioID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "No se pudo obtener el perfil"})
		return
	}
	c.JSON(http.StatusOK, perfil)
}

// ActualizarTelefono
//
// Actualiza el numero de telefono del usuario autenticado aplicando las
// siguientes validaciones antes de persistir el cambio:
//   - El campo telefono no puede estar vacio
//   - Debe tener entre 8 y 25 caracteres
//   - Debe comenzar con "+"
//   - Solo puede contener digitos, espacios, guiones y parentesis
//   - Debe tener exactamente los digitos locales que corresponden al pais del usuario
//     (segun helpers.DigitosPorPais; si el pais no se conoce, minimo 7 digitos totales)
//   - No puede ser igual al telefono actualmente registrado
//
// Registra el evento CAMBIO_PERFIL (ID 39) en log_sesion si la actualizacion
// es exitosa.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200 OK: JSON con mensaje de confirmacion
//   - HTTP 400 Bad Request: si el campo falta o no supera las validaciones de formato
//   - HTTP 500 Internal Server Error: si ocurre un error al actualizar el telefono
func (ctrl *PerfilController) ActualizarTelefono(c *gin.Context) {
	usuarioID := c.GetInt("usuario_id")

	var req struct {
		Telefono string `json:"telefono"`
	}
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Teléfono requerido"})
		return
	}

	telefono := strings.TrimSpace(req.Telefono)

	if telefono == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "El teléfono no puede estar vacío"})
		return
	}

	if len(telefono) < 8 || len(telefono) > 25 {
		c.JSON(http.StatusBadRequest, gin.H{"error": "El teléfono debe tener entre 8 y 25 caracteres"})
		return
	}

	if !strings.HasPrefix(telefono, "+") {
		c.JSON(http.StatusBadRequest, gin.H{"error": "El teléfono debe comenzar con '+' seguido del código de país"})
		return
	}

	if !telefonoValidoRegex.MatchString(telefono) {
		c.JSON(http.StatusBadRequest, gin.H{"error": "El teléfono solo puede contener dígitos, espacios, guiones y paréntesis"})
		return
	}

	telefonoActual, paisUsuario, err := ctrl.service.ObtenerTelefonoYPais(usuarioID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "No se pudo verificar el teléfono actual"})
		return
	}

	// Validar cantidad exacta de dígitos según país (fuente única de verdad: helpers.DigitosPorPais)
	ok, digitosDetectados, esperados := helpers.ValidarDigitosTelefono(telefono, paisUsuario)
	if !ok {
		if paisUsuario == "" {
			c.JSON(http.StatusBadRequest, gin.H{
				"error": fmt.Sprintf("El teléfono debe tener al menos %d dígitos", esperados),
			})
		} else {
			c.JSON(http.StatusBadRequest, gin.H{
				"error": fmt.Sprintf("El teléfono para %s debe tener exactamente %d dígitos locales (detectados: %d)",
					paisUsuario, esperados, digitosDetectados),
			})
		}
		return
	}

	if telefono == telefonoActual {
		c.JSON(http.StatusBadRequest, gin.H{"error": "El nuevo teléfono debe ser diferente al registrado actualmente"})
		return
	}

	if err := ctrl.service.ActualizarTelefono(usuarioID, telefono); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "No se pudo actualizar el teléfono"})
		return
	}

	// Log de cambio de perfil exitoso (ID 39)
	uid := usuarioID
	ctrl.logSesion.Registrar(c, helpers.TipoCambioPerfil,
		&uid, fmt.Sprintf("usuario_id=%d", usuarioID),
		fmt.Sprintf("Usuario actualizó su teléfono a %s", telefono))

	c.JSON(http.StatusOK, gin.H{"mensaje": "Teléfono actualizado correctamente"})
}

// ActualizarInfoPersonal
//
// Actualiza nombre, apellido, correo, username, pasaporte y fecha de nacimiento
// del usuario autenticado. Devuelve 409 si alguno de los campos unicos
// (correo, username, pasaporte) ya esta en uso por otro usuario.
func (ctrl *PerfilController) ActualizarInfoPersonal(c *gin.Context) {
	usuarioID := c.GetInt("usuario_id")

	var req struct {
		Nombre          string `json:"nombre"`
		Apellido        string `json:"apellido"`
		Correo          string `json:"correo"`
		Username        string `json:"username"`
		Pasaporte       string `json:"pasaporte"`
		FechaNacimiento string `json:"fecha_nacimiento"`
	}
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Datos inválidos"})
		return
	}

	req.Nombre    = strings.TrimSpace(req.Nombre)
	req.Apellido  = strings.TrimSpace(req.Apellido)
	req.Correo    = strings.TrimSpace(strings.ToLower(req.Correo))
	req.Username  = strings.TrimSpace(req.Username)
	req.Pasaporte = strings.TrimSpace(strings.ToUpper(req.Pasaporte))

	if req.Nombre == "" || req.Apellido == "" || req.Correo == "" || req.Username == "" || req.Pasaporte == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Todos los campos son requeridos"})
		return
	}

	campo, err := ctrl.service.ActualizarInfoPersonal(
		usuarioID, req.Nombre, req.Apellido, req.Correo,
		req.Username, req.Pasaporte, req.FechaNacimiento,
	)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "No se pudo actualizar la información"})
		return
	}
	if campo != "" {
		msgs := map[string]string{
			"correo":    "Este correo ya está registrado por otro usuario.",
			"username":  "Este nombre de usuario ya está en uso.",
			"pasaporte": "Este pasaporte ya está registrado por otro usuario.",
		}
		c.JSON(http.StatusConflict, gin.H{"campo": campo, "error": msgs[campo]})
		return
	}

	uid := usuarioID
	ctrl.logSesion.Registrar(c, helpers.TipoCambioPerfil,
		&uid, fmt.Sprintf("usuario_id=%d", usuarioID),
		"Usuario actualizó su información personal")

	c.JSON(http.StatusOK, gin.H{"mensaje": "Información actualizada correctamente"})
}

// ActualizarOfertas
//
// Actualiza la preferencia del usuario autenticado para recibir ofertas por correo.
// Espera { "recibir_ofertas": true|false } en el body.
func (ctrl *PerfilController) ActualizarOfertas(c *gin.Context) {
	usuarioID := c.GetInt("usuario_id")

	var req struct {
		RecibirOfertas bool `json:"recibir_ofertas"`
	}
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Datos inválidos"})
		return
	}

	if err := ctrl.service.ActualizarOfertas(usuarioID, req.RecibirOfertas); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "No se pudo actualizar la preferencia"})
		return
	}

	msg := "Te has suscrito a ofertas de Movent"
	if !req.RecibirOfertas {
		msg = "Te has dado de baja de las ofertas de Movent"
	}
	c.JSON(http.StatusOK, gin.H{"mensaje": msg})
}

// CambiarContrasena
//
// Cambia la contrasena del usuario autenticado aplicando las siguientes
// validaciones en orden:
//   - Los tres campos (actual, nueva, confirma) son obligatorios
//   - La nueva contrasena y su confirmacion deben coincidir
//   - La nueva contrasena debe tener al menos 8 caracteres
//   - La contrasena actual proporcionada debe ser correcta (bcrypt)
//   - La nueva contrasena no puede ser identica a la actual
//
// Registra CAMBIO_PASSWORD_FALLIDO (ID 34) si la contrasena actual es
// incorrecta, o CAMBIO_PASSWORD (ID 33) si el cambio es exitoso.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200 OK: JSON con mensaje de confirmacion
//   - HTTP 400 Bad Request: si faltan campos, las contrasenas no coinciden, la nueva es muy
//     corta, o la nueva es identica a la actual
//   - HTTP 401 Unauthorized: si la contrasena actual proporcionada es incorrecta
//   - HTTP 500 Internal Server Error: si ocurre un error al verificar o cambiar la contrasena
//
// Notas:
//   - La verificacion de la contrasena actual se realiza comparando el hash almacenado
func (ctrl *PerfilController) CambiarContrasena(c *gin.Context) {
	usuarioID := c.GetInt("usuario_id")
	var req struct {
		Actual   string `json:"actual"   binding:"required"`
		Nueva    string `json:"nueva"    binding:"required"`
		Confirma string `json:"confirma" binding:"required"`
	}
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Todos los campos son requeridos"})
		return
	}
	if req.Nueva != req.Confirma {
		c.JSON(http.StatusBadRequest, gin.H{"error": "La nueva contraseña y la confirmación no coinciden"})
		return
	}
	if len(req.Nueva) < 8 {
		c.JSON(http.StatusBadRequest, gin.H{"error": "La nueva contraseña debe tener al menos 8 caracteres"})
		return
	}

	// Obtener hash actual
	hashActual, err := ctrl.service.ObtenerHash(usuarioID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Error al verificar la contraseña"})
		return
	}

	// CheckPassword(plaintext, hash) — orden correcto según helpers/password.go
	if !helpers.CheckPassword(req.Actual, hashActual) {
		// Log de intento fallido de cambio de contraseña (ID 34)
		uid := usuarioID
		ctrl.logSesion.Registrar(c, helpers.TipoCambioPasswordFallido,
			&uid, fmt.Sprintf("usuario_id=%d", usuarioID),
			"Intento fallido de cambio de contraseña — contraseña actual incorrecta")

		c.JSON(http.StatusUnauthorized, gin.H{"error": "La contraseña actual es incorrecta"})
		return
	}

	// Verificar que la nueva contraseña no sea igual a la actual
	if req.Actual == req.Nueva {
		c.JSON(http.StatusBadRequest, gin.H{"error": "La nueva contraseña debe ser diferente a la contraseña actual"})
		return
	}

	if err := ctrl.service.CambiarContrasena(usuarioID, req.Nueva); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "No se pudo cambiar la contraseña"})
		return
	}

	// Log de cambio de contraseña exitoso (ID 33)
	uid := usuarioID
	ctrl.logSesion.Registrar(c, helpers.TipoCambioPassword,
		&uid, fmt.Sprintf("usuario_id=%d", usuarioID),
		"Contraseña actualizada exitosamente")

	c.JSON(http.StatusOK, gin.H{"mensaje": "Contraseña actualizada correctamente"})
}
