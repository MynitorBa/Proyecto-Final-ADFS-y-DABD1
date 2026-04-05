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
	service *services.LoginService
}

// NewLoginController
//
// Crea e inicializa un nuevo LoginController con el servicio recibido.
//
// Parametros:
//   - service: instancia del servicio de login
//
// Retorna:
//   - *LoginController: puntero al controlador creado
func NewLoginController(service *services.LoginService) *LoginController {
	return &LoginController{service: service}
}

// Login
//
// Handler HTTP que autentica al usuario con sus credenciales. Si son validas
// genera un token JWT y lo persiste en una cookie HttpOnly con duracion de
// 24 horas, retornando ademas los datos del usuario en el cuerpo de la respuesta.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200: datos del usuario autenticado y cookie de sesion establecida
//   - HTTP 400: error si el body JSON es invalido
//   - HTTP 401: error si las credenciales son incorrectas
//   - HTTP 500: error interno al generar el token JWT o al procesar el login
func (ctrl *LoginController) Login(c *gin.Context) {
	var req dto.LoginRequest

	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Datos inválidos"})
		return
	}

	response, err := ctrl.service.Login(req)
	if err != nil {
		if err == services.ErrCredencialesInvalidas {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "Usuario o contraseña incorrectos"})
			return
		}
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Error al iniciar sesión"})
		return
	}

	// Generar JWT
	token, err := helpers.GenerarToken(response.ID, response.Username, response.RolID)
	if err != nil {
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
	c.SetCookie("session", "", -1, "/", "", false, true)
	c.JSON(http.StatusOK, gin.H{"mensaje": "Sesión cerrada"})
}
