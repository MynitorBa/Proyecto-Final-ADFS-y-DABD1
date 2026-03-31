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

	// ── CORS ──────────────────────────────────────────────────
	router.Use(func(c *gin.Context) {
		c.Header("Access-Control-Allow-Origin", "http://localhost:5173")
		c.Header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
		c.Header("Access-Control-Allow-Headers", "Content-Type, Authorization")
		if c.Request.Method == "OPTIONS" {
			c.AbortWithStatus(204)
			return
		}
		c.Next()
	})
	// ──────────────────────────────────────────────────────────

	// Servicios
	ubicacionService := services.NewUbicacionService(db)
	usuarioService := services.NewUsuarioService(db, ubicacionService)
	loginService := services.NewLoginService(db)
	proveedorService := services.NewProveedorService(db)
	handshakeService := services.NewHandshakeService(db, cfg)
	handshakeHoteleraService := services.NewHandshakeHoteleraService(db, cfg)
	catalogoService := services.NewCatalogoService(db, ubicacionService)
	busquedaService := services.NewBusquedaService(db)
	reservacionService := services.NewReservacionService(db)
	expiracionService := services.NewExpiracionService(db)

	// Controllers
	usuarioController := controllers.NewUsuarioController(usuarioService)
	loginController := controllers.NewLoginController(loginService)
	sesionController := controllers.NewSesionController()
	proveedorController := controllers.NewProveedorController(proveedorService)
	handshakeController := controllers.NewHandshakeController(handshakeService)
	handshakeHoteleraController := controllers.NewHandshakeHoteleraController(handshakeHoteleraService)
	catalogoController := controllers.NewCatalogoController(catalogoService)
	busquedaController := controllers.NewBusquedaController(busquedaService)
	reservacionController := controllers.NewReservacionController(reservacionService)

	// Iniciar servicio de expiración
	expiracionService.Iniciar()
	defer expiracionService.Detener()

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

			protegido.POST("/busqueda/vuelos", busquedaController.BuscarVuelos)
			protegido.POST("/busqueda/hoteles", busquedaController.BuscarHoteles)

			protegido.POST("/reservaciones", reservacionController.CrearReservacion)

			protegido.POST("/proveedores", middlewares.RolRequerido(2), proveedorController.CrearProveedor)
			protegido.POST("/catalogo/actualizar", middlewares.RolRequerido(2), catalogoController.ActualizarCatalogo)
			protegido.POST("/proveedores/:id/handshake", middlewares.RolRequerido(2), handshakeController.IniciarHandshake)
			protegido.POST("/proveedores/:id/handshake-hotelera", middlewares.RolRequerido(2), handshakeHoteleraController.IniciarHandshake)
		}
	}

	log.Println("Servidor corriendo en puerto " + cfg.ServerPort)
	router.Run(":" + cfg.ServerPort)
}
