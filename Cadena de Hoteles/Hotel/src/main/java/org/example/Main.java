package org.example;

import io.javalin.Javalin;
import org.example.config.ServerConfig;
import org.example.controllers.AgenciaController;
import org.example.controllers.HotelAgenciaController;
import org.example.controllers.AuthController;
import org.example.controllers.BusquedaController;
import org.example.controllers.CancelacionController;
import org.example.controllers.ComentarioController;
import org.example.controllers.DownsController;
import org.example.controllers.HotelController;
import org.example.controllers.ImagenController;
import org.example.controllers.PagoController;
import org.example.controllers.ReservacionController;
import org.example.controllers.SesionController;
import org.example.controllers.UsuarioController;
import org.example.data.DatabaseTest;
import org.example.helpers.AuthMiddleware;
import org.example.repositories.*;
import org.example.services.*;
import org.example.controllers.PdfReservacionController;
import org.example.controllers.DestinosController;
import org.example.controllers.AdminBusquedaController;
import org.example.controllers.EmailReservacionController;
import org.example.controllers.CancelacionAgenciaController;
import org.example.controllers.BusquedaAgenciaController;
import org.example.controllers.ReservacionAgenciaController;
import org.example.controllers.PagoAgenciaController;

import java.util.Map;

/**
 * Punto de entrada de la aplicacion.
 * Arranca el servidor, registra middleware, controllers y el hilo de expiracion.
 */
public class Main {

    public static void main(String[] args) {

        // Verifica conexion a Oracle al iniciar
        DatabaseTest.testConnection();

        // Repositories
        AdminBusquedaRepository       adminBusquedaRepository       = new AdminBusquedaRepository();
        AdminReservacionRepository     adminReservacionRepository     = new AdminReservacionRepository();
        AgenciaRepository              agenciaRepository              = new AgenciaRepository();
        AuthRepository                 authRepository                 = new AuthRepository();
        BusquedaAgenciaRepository      busquedaAgenciaRepository      = new BusquedaAgenciaRepository();
        BusquedaRepository             busquedaRepository             = new BusquedaRepository();
        CancelacionRepository          cancelacionRepository          = new CancelacionRepository();
        CiudadRepository               ciudadRepository               = new CiudadRepository();
        ComentarioRepository           comentarioRepository           = new ComentarioRepository();
        DestinosRepository             destinosRepository             = new DestinosRepository();
        DownsRepository                downsRepository                = new DownsRepository();
        HotelAgenciaRepository         hotelAgenciaRepository         = new HotelAgenciaRepository();
        HotelRepository                hotelRepository                = new HotelRepository();
        ImagenRepository               imagenRepository               = new ImagenRepository();
        NacionalidadRepository         nacionalidadRepository         = new NacionalidadRepository();
        PagoAgenciaRepository          pagoAgenciaRepository          = new PagoAgenciaRepository();
        PagoRepository                 pagoRepository                 = new PagoRepository();
        PaisRepository                 paisRepository                 = new PaisRepository();
        PdfReservacionRepository       pdfReservacionRepository       = new PdfReservacionRepository();
        ReservacionAgenciaRepository   reservacionAgenciaRepository   = new ReservacionAgenciaRepository();
        ReservacionRepository          reservacionRepository          = new ReservacionRepository();
        SesionRepository               sesionRepository               = new SesionRepository();
        UsuarioNacionalidadRepository  usuarioNacionalidadRepository  = new UsuarioNacionalidadRepository();
        UsuarioRepository              usuarioRepository              = new UsuarioRepository();

        // Services
        AdminBusquedaService      adminBusquedaService      = new AdminBusquedaService(adminBusquedaRepository);
        AdminReservacionService   adminReservacionService   = new AdminReservacionService(adminReservacionRepository);
        AgenciaService            agenciaService            = new AgenciaService(agenciaRepository);
        AuthService               authService               = new AuthService(authRepository);
        BusquedaAgenciaService    busquedaAgenciaService    = new BusquedaAgenciaService(busquedaAgenciaRepository);
        BusquedaService           busquedaService           = new BusquedaService(busquedaRepository);
        CancelacionService        cancelacionService        = new CancelacionService(cancelacionRepository);
        ComentarioService         comentarioService         = new ComentarioService(comentarioRepository);
        DestinosService           destinosService           = new DestinosService(destinosRepository);
        DownsService              downsService              = new DownsService(downsRepository, comentarioRepository);
        EmailReservacionService   emailReservacionService   = new EmailReservacionService(pdfReservacionRepository);
        ExpiracionService         expiracionService         = new ExpiracionService(reservacionRepository);
        HandshakeService          handshakeService          = new HandshakeService(agenciaRepository);
        HotelAgenciaService       hotelAgenciaService       = new HotelAgenciaService(hotelAgenciaRepository);
        HotelService              hotelService              = new HotelService(hotelRepository, ciudadRepository, paisRepository);
        ImagenService             imagenService             = new ImagenService(imagenRepository);
        PagoAgenciaService        pagoAgenciaService        = new PagoAgenciaService(pagoAgenciaRepository);
        PagoService               pagoService               = new PagoService(pagoRepository);
        PdfReservacionService     pdfReservacionService     = new PdfReservacionService(pdfReservacionRepository);
        ReservacionAgenciaService reservacionAgenciaService = new ReservacionAgenciaService(reservacionAgenciaRepository);
        ReservacionService        reservacionService        = new ReservacionService(reservacionRepository);
        SesionService             sesionService             = new SesionService(sesionRepository);
        UsuarioService            usuarioService            = new UsuarioService(usuarioRepository, paisRepository, ciudadRepository, nacionalidadRepository, usuarioNacionalidadRepository);

        // Hilo de expiracion de reservaciones pendientes
        expiracionService.iniciar();

        Javalin app = ServerConfig.createServer();

        // Manejador global de excepciones
        // Filtra errores de Oracle para devolver mensajes legibles al cliente
        app.exception(Exception.class, (e, ctx) -> {
            String msg = e.getMessage() != null ? e.getMessage() : "Error interno del servidor";
            if (msg.contains("ORA-00001")) {
                msg = "Registro duplicado: ya existe un valor con esa restriccion unica.";
            } else if (msg.contains("ORA-")) {
                msg = msg.split("\n")[0].trim();
            }
            ctx.status(500).json(Map.of("mensaje", msg));
        });

        // Middleware de autenticacion JWT
        AuthMiddleware.registrar(app);

        // Controllers generales
        new AuthController(authService).registerRoutes(app);
        new SesionController(sesionService).registerRoutes(app);
        new UsuarioController(usuarioService).registerRoutes(app);
        new BusquedaController(busquedaService).registerRoutes(app);
        new ReservacionController(reservacionService).registerRoutes(app);
        new PagoController(pagoService).registerRoutes(app);
        new ComentarioController(comentarioService).registerRoutes(app);
        new DownsController(downsService).registerRoutes(app);
        new CancelacionController(cancelacionService).registerRoutes(app);
        new ImagenController(imagenService).registerRoutes(app);
        new HotelController(hotelService, adminReservacionService).registerRoutes(app);
        new AgenciaController(agenciaService, handshakeService).registerRoutes(app);
        new PdfReservacionController(pdfReservacionService).registerRoutes(app);
        new HotelAgenciaController(hotelAgenciaService).registrarRutas(app);

        // Controllers de agencias
        new CancelacionAgenciaController(cancelacionService).registerRoutes(app);
        new ReservacionAgenciaController(reservacionAgenciaService).registerRoutes(app);
        new BusquedaAgenciaController(busquedaAgenciaService).registerRoutes(app);
        new PagoAgenciaController(pagoAgenciaService).registerRoutes(app);

        // Controllers adicionales
        new DestinosController(destinosService).registerRoutes(app);
        new EmailReservacionController(emailReservacionService).registerRoutes(app);
        new AdminBusquedaController(adminBusquedaService).registerRoutes(app);

        // Rutas base de salud del servidor
        app.get("/", ctx -> ctx.json("OK"));
        app.get("/health", ctx -> ctx.json("OK"));

        // Detiene el hilo de expiracion cuando la JVM se apaga
        Runtime.getRuntime().addShutdownHook(new Thread(expiracionService::detener));
    }
}