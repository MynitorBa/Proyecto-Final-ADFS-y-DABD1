// # Package main
//
// Punto de entrada del servidor HTTP de la agencia de viajes Movent.
// Inicializa la configuracion, la base de datos, todos los servicios,
// repositorios y controladores, y registra las rutas de la API REST.
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

// main
//
// Funcion principal del servidor. Carga la configuracion desde variables
// de entorno, conecta con la base de datos MySQL, instancia todos los
// servicios y controladores, configura los middlewares CORS y de
// autenticacion, registra las rutas publicas y protegidas de la API,
// arranca el servicio de expiracion de reservaciones en segundo plano
// y levanta el servidor HTTP en el puerto configurado.
//
// Notas:
//   - Las rutas bajo /api/ son publicas excepto las agrupadas en "protegido"
//   - Las rutas bajo el grupo "admin" requieren rol 2 (administrador)
//   - El servicio de expiracion se detiene de forma ordenada al cerrar
func main() {
	cfg := config.Load()
	db := database.Connect(cfg)
	defer db.Close()

	router := gin.Default()

	router.Use(func(c *gin.Context) {
		c.Header("Access-Control-Allow-Origin", "http://localhost:5173")
		c.Header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH")
		c.Header("Access-Control-Allow-Headers", "Content-Type, Authorization")
		c.Header("Access-Control-Allow-Credentials", "true")
		if c.Request.Method == "OPTIONS" {
			c.AbortWithStatus(204)
			return
		}
		c.Next()
	})

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
	perfilService := services.NewPerfilService(db)

	pagoRepo := repositories.NewPagoRepository(db)
	reservacionRepo := repositories.NewReservacionRepository(db)
	configRepo := repositories.NewAgenciaConfiguracionRepository(db)
	misReservacionesRepo := repositories.NewMisReservacionesRepository(db)
	cancelacionRepo := repositories.NewCancelacionRepository(db)
	proveedorRepo := repositories.NewProveedorRepository(db)
	usuarioRepo := repositories.NewUsuarioRepository(db)
	cancelacionProveedorRepo := repositories.NewCancelacionProveedorRepository(db)
	actualizacionProveedorRepo := repositories.NewActualizacionProveedorRepository(db)
	notificacionesRepo       := repositories.NewNotificacionesRepository(db)

	pagoService := services.NewPagoService(pagoRepo, reservacionRepo, configRepo)
	misReservacionesService := services.NewMisReservacionesService(misReservacionesRepo)
	cancelacionService := services.NewCancelacionService(cancelacionRepo)
	comentarioService := services.NewComentarioService(proveedorRepo)
	pdfService := services.NewPdfReservacionService(misReservacionesService, usuarioRepo)
	emailService := services.NewEmailReservacionService(misReservacionesService, pdfService, usuarioRepo)
	cancelacionProveedorService := services.NewCancelacionProveedorService(cancelacionProveedorRepo)
	actualizacionProveedorService := services.NewActualizacionProveedorService(actualizacionProveedorRepo)
	notificacionesService    := services.NewNotificacionesService(notificacionesRepo)

	usuarioController := controllers.NewUsuarioController(usuarioService)
	loginController := controllers.NewLoginController(loginService)
	sesionController := controllers.NewSesionController()
	proveedorController := controllers.NewProveedorController(proveedorService)
	handshakeController := controllers.NewHandshakeController(handshakeService)
	handshakeHoteleraController := controllers.NewHandshakeHoteleraController(handshakeHoteleraService)
	catalogoController := controllers.NewCatalogoController(catalogoService)
	busquedaController := controllers.NewBusquedaController(busquedaService)
	reservacionController := controllers.NewReservacionController(reservacionService, pdfService, emailService)
	detalleReservacionController := controllers.NewDetalleReservacionController(detalleReservacionService)
	asientoVueloController := controllers.NewAsientoVueloController(asientoVueloService)
	pagoController := controllers.NewPagoController(pagoService)
	misReservacionesController := controllers.NewMisReservacionesController(misReservacionesService)
	cancelacionController := controllers.NewCancelacionController(cancelacionService)
	comentarioController := controllers.NewComentarioController(comentarioService)
	statsController := controllers.NewStatsController(db)
	adminController := controllers.NewAdminController(db)
	perfilController := controllers.NewPerfilController(perfilService)
	configuracionController := controllers.NewConfiguracionController(db)
	cancelacionProveedorController := controllers.NewCancelacionProveedorController(cancelacionProveedorService)
	actualizacionProveedorController := controllers.NewActualizacionProveedorController(actualizacionProveedorService)
	notificacionesController := controllers.NewNotificacionesController(notificacionesService)

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

		proveedoresExt := api.Group("/proveedores-ext")
		proveedoresExt.Use(middlewares.ProveedorAuthRequerido(db))
		{

			proveedoresExt.POST("/detalles/:idReservaProveedor/cancelar", cancelacionProveedorController.CancelarDetalle)
			proveedoresExt.POST("/detalles/:idReservaProveedor/actualizar", actualizacionProveedorController.NotificarActualizacion)
		}

		api.POST("/busqueda/vuelos", busquedaController.BuscarVuelos)
		api.POST("/busqueda/hoteles", busquedaController.BuscarHoteles)

		api.GET("/comentarios/vuelo/:proveedorId/:rutaId", comentarioController.ObtenerComentariosVuelo)
		api.GET("/comentarios/hotel/:proveedorId/:hotelId", comentarioController.ObtenerComentariosHotel)
		api.GET("/stats", statsController.ObtenerStats)
		api.GET("/configuracion/descuento", configuracionController.ObtenerDescuento)

		api.POST("/contacto", controllers.EnviarContacto)

		protegido := api.Group("/")
		protegido.Use(middlewares.AuthRequerido())
		{
			protegido.GET("/sesion", sesionController.ObtenerSesion)

			protegido.GET("/perfil", perfilController.ObtenerPerfil)
			protegido.PUT("/perfil/telefono", perfilController.ActualizarTelefono)
			protegido.PUT("/perfil/contrasena", perfilController.CambiarContrasena)

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
			protegido.GET("/reservaciones/:id/pdf", reservacionController.DescargarPDF)
			protegido.POST("/reservaciones/:id/correo", reservacionController.EnviarCorreo)

			protegido.GET("/notificaciones",          notificacionesController.ObtenerTodas)
			protegido.PATCH("/notificaciones/:id/leida", notificacionesController.MarcarComoLeida)
		
			admin := protegido.Group("/")
			admin.Use(middlewares.RolRequerido(2))
			{
				admin.GET("/usuarios", adminController.ListarUsuarios)
				admin.PUT("/usuarios/:id/rol", adminController.ActualizarRol)
				admin.GET("/proveedores", adminController.ListarProveedores)
				admin.PATCH("/proveedores/:id/estado", adminController.ToggleEstadoProveedor)
				admin.PUT("/proveedores/:id", adminController.EditarProveedor)
				admin.GET("/admin/reservaciones/recientes", adminController.ReservacionesRecientes)
				admin.GET("/admin/metricas", adminController.ObtenerMetricas)
				admin.POST("/proveedores", proveedorController.CrearProveedor)
				admin.POST("/catalogo/actualizar", catalogoController.ActualizarCatalogo)
				admin.POST("/proveedores/:id/handshake", handshakeController.IniciarHandshake)
				admin.POST("/proveedores/:id/handshake-hotelera", handshakeHoteleraController.IniciarHandshake)
				admin.GET("/admin/usuarios", usuarioController.ObtenerTodos)
			}
		}
	}

	log.Println("Servidor corriendo en puerto " + cfg.ServerPort)
	router.Run(":" + cfg.ServerPort)
}
