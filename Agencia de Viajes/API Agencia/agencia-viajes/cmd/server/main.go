package main

import (
	"agencia-viajes/internal/config"
	"agencia-viajes/internal/controllers"
	"agencia-viajes/internal/middlewares"
	"agencia-viajes/internal/repositories"
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

	router.Use(func(c *gin.Context) {
		c.Header("Access-Control-Allow-Origin", "http://localhost:5173")
		c.Header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
		c.Header("Access-Control-Allow-Headers", "Content-Type, Authorization")
		c.Header("Access-Control-Allow-Credentials", "true")
		if c.Request.Method == "OPTIONS" {
			c.AbortWithStatus(204)
			return
		}
		c.Next()
	})

	// Servicios
	ubicacionService := services.NewUbicacionService(db)
	usuarioService := services.NewUsuarioService(db, ubicacionService)
	loginService := services.NewLoginService(db)
	proveedorService := services.NewProveedorService(db)
	handshakeService := services.NewHandshakeService(db, cfg)
	handshakeHoteleraService := services.NewHandshakeHoteleraService(db, cfg)
	catalogoService := services.NewCatalogoService(db, ubicacionService)
	busquedaService := services.NewBusquedaService(db)
	expiracionService := services.NewExpiracionService(db)
	reservacionService := services.NewReservacionService(db, expiracionService)
	detalleReservacionService := services.NewDetalleReservacionService(db)
	asientoVueloService := services.NewAsientoVueloService(db)
	pagoRepo := repositories.NewPagoRepository(db)
	reservacionRepo := repositories.NewReservacionRepository(db)
	pagoService := services.NewPagoService(pagoRepo, reservacionRepo)
	misReservacionesRepo := repositories.NewMisReservacionesRepository(db)
	misReservacionesService := services.NewMisReservacionesService(misReservacionesRepo)
	cancelacionRepo := repositories.NewCancelacionRepository(db)
	cancelacionService := services.NewCancelacionService(cancelacionRepo)
	proveedorRepo := repositories.NewProveedorRepository(db)
	comentarioService := services.NewComentarioService(proveedorRepo)

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
	detalleReservacionController := controllers.NewDetalleReservacionController(detalleReservacionService)
	asientoVueloController := controllers.NewAsientoVueloController(asientoVueloService)
	pagoController := controllers.NewPagoController(pagoService)
	misReservacionesController := controllers.NewMisReservacionesController(misReservacionesService)
	cancelacionController := controllers.NewCancelacionController(cancelacionService)
	comentarioController := controllers.NewComentarioController(comentarioService)

	expiracionService.Iniciar()
	defer expiracionService.Detener()

	api := router.Group("/api")
	{
		usuarios := api.Group("/usuarios")
		{
			usuarios.POST("/registro", usuarioController.Registrar)
			usuarios.POST("/login", loginController.Login)
			usuarios.POST("/logout", loginController.Logout)
		}

		api.POST("/busqueda/vuelos", busquedaController.BuscarVuelos)
		api.POST("/busqueda/hoteles", busquedaController.BuscarHoteles)

		api.GET("/comentarios/vuelo/:proveedorId/:rutaId", comentarioController.ObtenerComentariosVuelo)
		api.GET("/comentarios/hotel/:proveedorId/:hotelId", comentarioController.ObtenerComentariosHotel)

		protegido := api.Group("/")
		protegido.Use(middlewares.AuthRequerido())
		{
			protegido.GET("/sesion", sesionController.ObtenerSesion)

			protegido.POST("/reservaciones", reservacionController.CrearReservacion)

			protegido.POST("/reservaciones/detalle/vuelo", detalleReservacionController.AgregarDetalleVuelo)
			protegido.POST("/reservaciones/detalle/hotel", detalleReservacionController.AgregarDetalleHotel)

			protegido.POST("/reservaciones/detalle/pasajeros-vuelo", detalleReservacionController.AgregarPasajerosVuelo)

			protegido.POST("/reservaciones/asientos-vuelo", asientoVueloController.ObtenerAsientos)
			protegido.PUT("/reservaciones/asientos-vuelo", asientoVueloController.CambiarAsiento)

			protegido.POST("/reservaciones/pagar", pagoController.Pagar)

			protegido.GET("/reservaciones/mias", misReservacionesController.Listar)
			protegido.GET("/reservaciones/mias/:id", misReservacionesController.Detalle)

			protegido.GET("/reservaciones/:id/cancelar/verificar", cancelacionController.Verificar)
			protegido.POST("/reservaciones/:id/cancelar", cancelacionController.Cancelar)

			admin := protegido.Group("/")
			admin.Use(middlewares.RolRequerido(2))
			{
				admin.POST("/proveedores", proveedorController.CrearProveedor)
				admin.POST("/catalogo/actualizar", catalogoController.ActualizarCatalogo)
				admin.POST("/proveedores/:id/handshake", handshakeController.IniciarHandshake)
				admin.POST("/proveedores/:id/handshake-hotelera", handshakeHoteleraController.IniciarHandshake)
			}
		}
	}

	log.Println("Servidor corriendo en puerto " + cfg.ServerPort)
	router.Run(":" + cfg.ServerPort)
}
