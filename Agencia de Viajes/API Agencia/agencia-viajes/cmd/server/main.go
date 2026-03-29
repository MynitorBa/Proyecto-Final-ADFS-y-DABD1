package main

import (
	"agencia-viajes/internal/config"
	"agencia-viajes/internal/controllers"
	"agencia-viajes/internal/middlewares"
	"agencia-viajes/internal/services"
	"agencia-viajes/pkg/database"
	"log"

	"github.com/gin-gonic/gin"
)

func main() {
	cfg := config.Load()
	db := database.Connect(cfg)
	defer db.Close()

	router := gin.Default()

	// Servicios
	ubicacionService := services.NewUbicacionService(db)
	usuarioService := services.NewUsuarioService(db, ubicacionService)
	loginService := services.NewLoginService(db)

	// Controllers
	usuarioController := controllers.NewUsuarioController(usuarioService)
	loginController := controllers.NewLoginController(loginService)
	sesionController := controllers.NewSesionController()

	api := router.Group("/api")
	{
		// Rutas públicas
		usuarios := api.Group("/usuarios")
		{
			usuarios.POST("/registro", usuarioController.Registrar)
			usuarios.POST("/login", loginController.Login)
			usuarios.POST("/logout", loginController.Logout)
		}

		// Rutas protegidas — requieren sesión activa
		protegido := api.Group("/")
		protegido.Use(middlewares.AuthRequerido())
		{
			protegido.GET("/sesion", sesionController.ObtenerSesion)
		}
	}

	log.Println("Servidor corriendo en puerto " + cfg.ServerPort)
	router.Run(":" + cfg.ServerPort)
}
