package controllers

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/helpers"
	"agencia-viajes/internal/services"
	"net/http"
	"time"

	"github.com/gin-gonic/gin"
)

type LoginController struct {
	service *services.LoginService
}

func NewLoginController(service *services.LoginService) *LoginController {
	return &LoginController{service: service}
}

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

func (ctrl *LoginController) Logout(c *gin.Context) {
	c.SetCookie("session", "", -1, "/", "", false, true)
	c.JSON(http.StatusOK, gin.H{"mensaje": "Sesión cerrada"})
}
