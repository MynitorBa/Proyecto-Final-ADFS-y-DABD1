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
import org.example.controllers.ImagenAgenciaController;


import org.example.controllers.BusquedaAerolineaController;
import org.example.controllers.TokenAerolineaController;
import org.example.controllers.TokenValidacionController;

import org.example.controllers.ReservacionAgenciaController;
import org.example.controllers.PagoAgenciaController;

// Controllers de gestion de entidades webservice
import org.example.controllers.AerolineaWebserviceController;
import org.example.controllers.AerolineaAdminController;

// Controller de handshake con aerolineas aliadas externas
import org.example.controllers.HandshakeAerolineaController;

import java.util.Map;

/**
 * Punto de entrada de la aplicacion.
 * Arranca el servidor, registra middleware, controllers y el hilo de expiracion.
 */
public class Main {

    public static void main(String[] args) {

        // Verifica conexion a Oracle al iniciar
        DatabaseTest.testConnection();

        //logs
        LogRepository  logRepository  = new LogRepository();
        LogReservacionRepository  logReservacionRepository= new LogReservacionRepository();

        // Repositories
        AdminBusquedaRepository       adminBusquedaRepository       = new AdminBusquedaRepository();
        AdminReservacionRepository adminReservacionRepository = new AdminReservacionRepository();
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

        AerolineaAliadaRepository      aerolineaAliadaRepository      = new AerolineaAliadaRepository();
        TokenAerolineaRepository       tokenAerolineaRepository       = new TokenAerolineaRepository();
        TokenValidacionRepository      tokenValidacionRepository      = new TokenValidacionRepository();

        // Repositories de gestion de entidades webservice
        AerolineaWebserviceRepository  aerolineaWebserviceRepository  = new AerolineaWebserviceRepository();
        AerolineaAdminRepository       aerolineaAdminRepository       = new AerolineaAdminRepository();

        // Services
        AdminBusquedaService      adminBusquedaService      = new AdminBusquedaService(adminBusquedaRepository);
        AgenciaService            agenciaService            = new AgenciaService(agenciaRepository);
        AuthService    authService    = new AuthService(authRepository, logRepository);
        BusquedaAgenciaService    busquedaAgenciaService    = new BusquedaAgenciaService(busquedaAgenciaRepository);
        BusquedaService           busquedaService           = new BusquedaService(busquedaRepository);
        CancelacionService        cancelacionService        = new CancelacionService(cancelacionRepository, logReservacionRepository);
        ComentarioService         comentarioService         = new ComentarioService(comentarioRepository);
        DestinosService           destinosService           = new DestinosService(destinosRepository);
        DownsService              downsService              = new DownsService(downsRepository, comentarioRepository);
        EmailReservacionService   emailReservacionService   = new EmailReservacionService(pdfReservacionRepository);
        ExpiracionService         expiracionService         = new ExpiracionService(reservacionRepository, logReservacionRepository);
        HandshakeService          handshakeService          = new HandshakeService(agenciaRepository);
        HotelAgenciaService       hotelAgenciaService       = new HotelAgenciaService(hotelAgenciaRepository);
        // Service de notificacion a agencias externas — se crea antes de HotelService porque
        // ambos lo necesitan: HotelService para el flujo de cierre de hotel, y
        // AdminReservacionService para cancelaciones individuales desde el panel admin.
        AgenciaNotificadorExternoService agenciaNotificadorExternoService =
                new AgenciaNotificadorExternoService();

        HotelService              hotelService              = new HotelService(hotelRepository, ciudadRepository, paisRepository, agenciaNotificadorExternoService);
        ImagenService             imagenService             = new ImagenService(imagenRepository);
        PagoAgenciaService        pagoAgenciaService        = new PagoAgenciaService(pagoAgenciaRepository, logReservacionRepository);
        PagoService               pagoService               = new PagoService(pagoRepository, tokenValidacionRepository, logReservacionRepository);
        PdfReservacionService     pdfReservacionService     = new PdfReservacionService(pdfReservacionRepository);
        ReservacionAgenciaService reservacionAgenciaService = new ReservacionAgenciaService(reservacionAgenciaRepository, logReservacionRepository);
        ReservacionService        reservacionService        = new ReservacionService(reservacionRepository, logReservacionRepository);
        SesionService             sesionService             = new SesionService(sesionRepository);
        UsuarioService            usuarioService            = new UsuarioService(usuarioRepository, paisRepository, ciudadRepository, nacionalidadRepository, usuarioNacionalidadRepository, logRepository);

        BusquedaAerolineaService  busquedaAerolineaService  = new BusquedaAerolineaService(aerolineaAliadaRepository);
        TokenAerolineaService     tokenAerolineaService     = new TokenAerolineaService(tokenAerolineaRepository, aerolineaAliadaRepository);
        TokenValidacionService    tokenValidacionService    = new TokenValidacionService(tokenValidacionRepository);

        // Services de gestion de entidades webservice
        AerolineaWebserviceService aerolineaWebserviceService = new AerolineaWebserviceService(aerolineaWebserviceRepository);
        AerolineaAdminService      aerolineaAdminService      = new AerolineaAdminService(aerolineaAdminRepository);

        // Service de handshake con aerolineas aliadas externas
        HandshakeAerolineaService  handshakeAerolineaService  = new HandshakeAerolineaService(aerolineaAliadaRepository);

        // AdminReservacionService recibe el notificador para enviar aviso a la agencia
        // y el correo al usuario al cancelar una reservacion desde el panel admin
        AdminReservacionService adminReservacionService =
                new AdminReservacionService(adminReservacionRepository, agenciaNotificadorExternoService);

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
        new ImagenAgenciaController(imagenService).registerRoutes(app);

        // Controllers de aerolineas (busqueda y tokens para integraciones externas)
        new BusquedaAerolineaController(busquedaAerolineaService).registerRoutes(app);
        new TokenAerolineaController(tokenAerolineaService).registerRoutes(app);
        new TokenValidacionController(tokenValidacionService).registerRoutes(app);

        // Controller de handshake para aerolineas aliadas externas
        // Endpoint publico: POST /api/aerolineas/handshake
        new HandshakeAerolineaController(handshakeAerolineaService).registerRoutes(app);

        // Controllers de gestion de entidades webservice desde sus respectivos portales
        new AerolineaWebserviceController(aerolineaWebserviceService).registerRoutes(app);
        new AerolineaAdminController(aerolineaAdminService).registerRoutes(app);

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