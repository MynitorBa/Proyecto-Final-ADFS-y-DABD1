// # Package controllers
//
// Controladores HTTP de la agencia de viajes. Cada controlador recibe
// solicitudes de Gin, delega la logica de negocio al servicio correspondiente
// y devuelve la respuesta JSON al cliente.
package controllers

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/helpers"
	"agencia-viajes/internal/services"
	"net/http"
	"time"

	"github.com/gin-gonic/gin"
)

// LoginController
//
// Controlador encargado de gestionar la autenticacion de usuarios,
// incluyendo el inicio y cierre de sesion mediante JWT almacenado en cookie.
type LoginController struct {
	service   *services.LoginService
	logSesion *services.LogSesionService
}

// NewLoginController
//
// Crea e inicializa un nuevo LoginController con los servicios recibidos.
//
// Parametros:
//   - service: instancia del servicio de login
//   - logSesion: instancia del servicio de log de sesion para auditoria
//
// Retorna:
//   - *LoginController: puntero al controlador creado
func NewLoginController(service *services.LoginService, logSesion *services.LogSesionService) *LoginController {
	return &LoginController{service: service, logSesion: logSesion}
}

// Login
//
// Handler HTTP que autentica al usuario con sus credenciales. Si son validas
// genera un token JWT y lo persiste en una cookie HttpOnly con duracion de
// 24 horas, retornando ademas los datos del usuario en el cuerpo de la respuesta.
// Registra en log_sesion el resultado del intento de autenticacion.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200: datos del usuario autenticado y cookie de sesion establecida
//   - HTTP 400: error si el body JSON es invalido, campos vacios, o captcha falla
//   - HTTP 401: error si las credenciales son incorrectas
//   - HTTP 403: error si el usuario esta deshabilitado
//   - HTTP 500: error interno al generar el token JWT o al procesar el login
func (ctrl *LoginController) Login(c *gin.Context) {
	var req dto.LoginRequest

	if err := c.ShouldBindJSON(&req); err != nil {
		ctrl.logSesion.Registrar(c, helpers.TipoLoginFallidoPayload, nil, "", err.Error())
		c.JSON(http.StatusBadRequest, gin.H{"error": "Datos inválidos"})
		return
	}

	response, err := ctrl.service.Login(req)
	if err != nil {
		if err == services.ErrCredencialesInvalidas {
			ctrl.logSesion.Registrar(c, helpers.TipoLoginFallidoCredenciales, nil, req.Login, "")
			c.JSON(http.StatusUnauthorized, gin.H{"error": "Usuario o contraseña incorrectos"})
			return
		}
		if err == services.ErrUsuarioDeshabilitado {
			ctrl.logSesion.Registrar(c, helpers.TipoLoginFallidoDeshabilitado, nil, req.Login, "Usuario deshabilitado intentó iniciar sesión")
			c.JSON(http.StatusForbidden, gin.H{"error": "Cuenta deshabilitada. Contacta al administrador."})
			return
		}
		if err == services.ErrCamposVacios {
			ctrl.logSesion.Registrar(c, helpers.TipoLoginFallidoCampos, nil, req.Login, "Login o contraseña vacíos")
			c.JSON(http.StatusBadRequest, gin.H{"error": "Ingresa tu usuario y contraseña"})
			return
		}
		if err == services.ErrCaptchaAusente {
			ctrl.logSesion.Registrar(c, helpers.TipoLoginFallidoCaptchaAusente, nil, req.Login, "Login sin token de captcha")
			c.JSON(http.StatusBadRequest, gin.H{"error": "Completa el CAPTCHA para continuar"})
			return
		}
		if err == services.ErrCaptchaInvalido {
			ctrl.logSesion.Registrar(c, helpers.TipoLoginFallidoCaptchaInvalido, nil, req.Login, "Token de captcha rechazado por Google")
			c.JSON(http.StatusBadRequest, gin.H{"error": "Verificación de CAPTCHA falló. Intenta de nuevo."})
			return
		}
		ctrl.logSesion.Registrar(c, helpers.TipoLoginErrorInterno, nil, req.Login, err.Error())
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Error al iniciar sesión"})
		return
	}

	// Generar JWT
	token, err := helpers.GenerarToken(response.ID, response.Username, response.RolID)
	if err != nil {
		ctrl.logSesion.Registrar(c, helpers.TipoLoginErrorInterno, &response.ID, req.Login, "Error generando JWT: "+err.Error())
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Error al iniciar sesión"})
		return
	}

	// Guardar en cookie HttpOnly
	c.SetCookie(
		"session",
		token,
		int(24*time.Hour.Seconds()),
		"/",
		"",
		false,
		true,
	)

	ctrl.logSesion.Registrar(c, helpers.TipoLoginExitoso, &response.ID, req.Login, "")
	c.JSON(http.StatusOK, response)
}

// Logout
//
// Handler HTTP que cierra la sesion del usuario eliminando la cookie de sesion
// al establecer su tiempo de vida en -1.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200: mensaje confirmando que la sesion fue cerrada
func (ctrl *LoginController) Logout(c *gin.Context) {
	usuarioID, username := helpers.ExtraerUsuarioIDDeCookie(c)
	if usuarioID > 0 {
		uid := usuarioID
		ctrl.logSesion.Registrar(c, helpers.TipoLogout, &uid, username, "Usuario cerró sesión correctamente")
	} else {
		ctrl.logSesion.Registrar(c, helpers.TipoLogoutSinSesionActiva, nil, "", "Logout sin cookie de sesión válida")
	}
	c.SetCookie("session", "", -1, "/", "", false, true)
	c.JSON(http.StatusOK, gin.H{"mensaje": "Sesión cerrada"})
}
