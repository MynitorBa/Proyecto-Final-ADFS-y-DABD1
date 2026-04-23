#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Genera Manual_Programador_Aerolinea.docx — Broom AirLine (.NET C# + Svelte)"""

import sys
sys.path.insert(0, r'C:\Proyecto-Final-ADFS-y-DABD1')
from doc_utils import *

OUT = r'C:\Proyecto-Final-ADFS-y-DABD1\Manual_Programador_Aerolinea.docx'

def build():
    doc = new_doc()
    set_footer(doc)
    add_cover(doc, "Aerolínea — Broom AirLine")

    # ── ÍNDICE ──────────────────────────────────────────────
    h1(doc, "Índice")
    for line in [
        "Parte 0 — Stack Técnico",
        "Parte 1 — Librerías y Dependencias",
        "Parte 2 — Arquitectura y Mapa del Proyecto",
        "Parte 3 — Flujos Internos End-to-End",
        "Parte 4 — WebService y Flujo entre Proveedores",
        "Parte 5 — Queries SQL del Backend",
        "Anexo  — Queries SQL de Métricas para Defensa",
        "Inventario de Cobertura",
    ]:
        bullet(doc, line)
    page_break(doc)

    # ══════════════════════════════════════════════════════
    # PARTE 0 — STACK TÉCNICO
    # ══════════════════════════════════════════════════════
    h1(doc, "Parte 0 — Stack Técnico")

    h2(doc, "0.1 Lenguajes")
    table(doc,
        ["Lenguaje", "Versión", "Contexto"],
        [
            ["C#", ".NET 8.0 (net8.0)", "Backend — toda la lógica de negocio y REST API"],
            ["JavaScript/Svelte", "Svelte 5.43.8", "Frontend — SPA del sistema"],
        ]
    )

    h2(doc, "0.2 Framework del Backend")
    table(doc,
        ["Framework", "Versión", "Propósito"],
        [
            ["ASP.NET Core", "8.0", "Framework HTTP, middleware, DI, routing, cookies auth"],
            ["Cookie Authentication", "(ASP.NET Core built-in)", "Sesiones cifradas; cookie 'aerolinea_session', 8h sliding"],
        ]
    )

    h2(doc, "0.3 Framework del Frontend")
    table(doc,
        ["Framework / Tool", "Versión", "Propósito"],
        [
            ["Svelte", "5.43.8", "Componentes reactivos compilados a JS vanilla"],
            ["Vite", "7.2.4", "Build tool y dev-server (HMR)"],
            ["Node.js", "20-alpine (Docker)", "Runtime para build"],
        ]
    )

    h2(doc, "0.4 Base de Datos y Driver")
    table(doc,
        ["Componente", "Versión / Detalle", "Propósito"],
        [
            ["SQL Server 2022 Express", "2022-latest (Docker image)", "Motor de base de datos relacional"],
            ["Microsoft.Data.SqlClient", "6.1.4", "Driver ADO.NET para SQL Server, parámetros @param"],
            ["Transacciones SERIALIZABLE", "SET TRANSACTION ISOLATION LEVEL SERIALIZABLE", "Evita race-condition en reservaciones"],
        ]
    )

    h2(doc, "0.5 Autenticación y Seguridad")
    table(doc,
        ["Tecnología", "Versión", "Uso en el Sistema"],
        [
            ["BCrypt.Net-Next", "4.0.3", "Hash de contraseñas en registro; verificación en login"],
            ["ASP.NET Cookie Auth", "built-in", "Sesión cifrada HttpOnly SameSite=Lax, 8h sliding"],
            ["TokenHelper (SHA-256)", "custom", "Tokens para handshake de agencias; 64 chars hex"],
            ["AgenciaAuthMiddleware", "custom IAsyncActionFilter", "Valida header X-Agencia-Token en endpoints de webservice"],
        ]
    )

    h2(doc, "0.6 Herramientas de Build")
    table(doc,
        ["Herramienta", "Versión", "Uso"],
        [
            ["dotnet SDK", "8.0", "Compilar y publicar el backend"],
            ["npm", "bundled con Node 20", "Gestión de dependencias del frontend"],
            ["Vite", "7.2.4", "Bundler del frontend Svelte"],
            ["Docker / Compose", "multi-stage", "Contenedores de BD, backend y frontend"],
        ]
    )

    h2(doc, "0.7 Herramientas Complementarias")
    table(doc,
        ["Herramienta", "Detalle"],
        [
            ["MailKit 4.15.0", "Envío de correos SMTP (Gmail 587 TLS)"],
            ["QuestPDF 2026.2.2", "Generación de PDF (activo en código)"],
            ["DinkToPdf 1.0.8", "Conversión HTML→PDF (comentado, nativo no disponible en Docker)"],
            ["Playwright 1.59.1", "Tests E2E del frontend"],
        ]
    )
    page_break(doc)

    # ══════════════════════════════════════════════════════
    # PARTE 1 — LIBRERÍAS Y DEPENDENCIAS
    # ══════════════════════════════════════════════════════
    h1(doc, "Parte 1 — Librerías y Dependencias")

    h2(doc, "1.1 Generación de PDF")
    body(doc, "El sistema tiene dos librerías de PDF en el proyecto .csproj. DinkToPdf (1.0.8) está referenciada pero comentada en Program.cs porque su DLL nativa wkhtmltopdf no está compilada para el entorno Docker. QuestPDF (2026.2.2) está activa. El flujo real de producción genera HTML desde PdfHtmlHelper.cs y lo retorna como text/html; el navegador lo imprime como PDF.")
    body(doc, "Archivo principal: Helpers/PdfHtmlHelper.cs — genera HTML completo con estilos inline (encabezado Broom AirLine, tabla de boletos, datos de pasajeros, total).")
    code(doc, "// PdfHtmlHelper.cs — fragmento\nstatic string GenerarComprobante(reservacionDetail)\n{\n    return $\"\"\"\n    <html><head><style>\n      body {{ font-family: Arial; }}\n      .header {{ background:#1F3864; color:white; padding:20px; }}\n      table {{ width:100%; border-collapse:collapse; }}\n      th {{ background:#2E75B6; color:white; }}\n    </style></head>\n    <body>\n      <div class='header'><h1>Broom AirLine</h1></div>\n      <h2>Reservacion: {reservacionDetail.NoReservacion}</h2>\n      ...\n    </body></html>\"\"\";\n}")

    h2(doc, "1.2 Envío de Emails")
    body(doc, "Librería: System.Net.Mail (built-in .NET). Configuración en Helpers/EmailHelper.cs. Servidor Gmail smtp.gmail.com:587 con SSL. Cuenta: distribuidorapine@gmail.com (App Password). Tres tipos de correo: comprobante de reservación, formulario de contacto reenviado al admin, y notificación de newsletter.")
    code(doc, "// EmailHelper.cs — método Enviar\nstatic async Task Enviar(string destinatario, string asunto, string cuerpoHtml)\n{\n    using var client = new SmtpClient(\"smtp.gmail.com\", 587);\n    client.EnableSsl = true;\n    client.Credentials = new NetworkCredential(\n        \"distribuidorapine@gmail.com\", \"[APP_PASSWORD]\");\n    var msg = new MailMessage(\n        \"distribuidorapine@gmail.com\", destinatario, asunto, cuerpoHtml);\n    msg.IsBodyHtml = true;\n    await client.SendMailAsync(msg);\n}")
    body(doc, "Correos que envía el sistema: (1) Comprobante de reservación al usuario tras compra exitosa — endpoint GET /api/reservaciones/{id}/correo. (2) Copia del formulario de contacto al admin — POST /api/contacto. (3) Confirmación de suscripción newsletter — POST /api/newsletter.")

    h2(doc, "1.3 Autenticación — BCrypt y Cookie Session")
    body(doc, "Hash de contraseña: Helpers/PasswordHasher.cs usa BCrypt.Net.BCrypt.HashPassword() con DefaultCost (10 rondas). Verificación en AuthService.cs con BCrypt.Net.BCrypt.Verify(). No existe JWT en este módulo; la autenticación se basa en cookies cifradas de ASP.NET Core.")
    code(doc, "// PasswordHasher.cs\npublic static string Hash(string password)\n    => BCrypt.Net.BCrypt.HashPassword(password);\n\npublic static bool Verify(string password, string hash)\n    => BCrypt.Net.BCrypt.Verify(password, hash);\n\n// AuthService.cs — Login\nbool ok = BCrypt.Net.BCrypt.Verify(request.Contrasena, usuario.ContrasenaHash);\nif (!ok) throw new CredencialesInvalidasException();\n// Luego HttpContext.SignInAsync con Claims")

    body(doc, "Cookie: nombre 'aerolinea_session' (configurable COOKIE_NAME). HttpOnly=true, SameSite=Lax, Secure=SameAsRequest. Expira en 8h con SlidingExpiration=true.")

    h2(doc, "1.4 Token de Agencia (SHA-256)")
    code(doc, "// TokenHelper.cs\npublic static string GenerarTokenHash()\n{\n    var bytes = new byte[32];\n    RandomNumberGenerator.Fill(bytes);\n    var hash = SHA256.HashData(bytes);\n    return Convert.ToHexString(hash).ToLower(); // 64 chars\n}")

    h2(doc, "1.5 Cliente HTTP Saliente")
    body(doc, "Clase: Services/AgenciaNotificadorExternoService.cs. Usa IHttpClientFactory (registrado en Program.cs). Realiza POST hacia la URL registrada de la agencia para notificar cancelaciones de reservaciones. No hay timeout explícito configurado en el código leído.")

    h2(doc, "1.6 Otras Librerías Críticas")
    table(doc,
        ["Librería", "Versión", "Propósito", "Archivo de Uso"],
        [
            ["Microsoft.Data.SqlClient", "6.1.4", "Queries SQL Server con parámetros @param", "Todos los Repositories"],
            ["IHttpClientFactory", "built-in ASP.NET Core", "Cliente HTTP para llamadas salientes a agencias", "AgenciaNotificadorExternoService.cs"],
            ["BackgroundService", "built-in ASP.NET Core", "Servicio de limpieza de reservaciones expiradas", "ReservasCleanupService.cs"],
            ["ILogger<T>", "built-in ASP.NET Core", "Logging estructurado en cancelaciones", "AdminReservacionesController.cs"],
        ]
    )
    page_break(doc)

    # ══════════════════════════════════════════════════════
    # PARTE 2 — ARQUITECTURA
    # ══════════════════════════════════════════════════════
    h1(doc, "Parte 2 — Arquitectura y Mapa del Proyecto")

    h2(doc, "2.1 Árbol del Backend")
    code(doc,
"""Aerolinea.API/Aerolinea.API/
├── Program.cs                      # Punto de entrada; DI, middleware, auth, CORS, routing
├── appsettings.json                # Cadena de conexión SQL Server local
├── appsettings.Docker.json         # Cadena Docker + orígenes CORS
├── Dockerfile                      # Multi-stage: sdk:8.0 build → aspnet:8.0 runtime
│
├── Controllers/ (34 archivos)
│   ├── AuthController.cs           # POST /api/auth/login, GET /api/auth/sesion, POST /api/auth/logout
│   ├── UsuariosController.cs       # POST /api/usuarios (registro), PUT /api/usuarios/cambiar-rol
│   ├── VuelosController.cs         # GET /api/vuelos/busqueda-general, POST /api/vuelos/buscar
│   ├── ReservacionesController.cs  # POST /api/reservaciones, PUT pasajeros, POST comprar
│   ├── FacturaController.cs        # Lógica de facturación interna
│   ├── AdminVuelosController.cs    # POST /api/admin/vuelos (crear vuelo), cancelar, historial
│   ├── AdminReservacionesController.cs # GET/POST /api/admin/reservaciones/*
│   ├── AsientoController.cs        # GET /api/asientos/{vueloId}, PUT /api/asientos/{boletoId}
│   ├── AsientoAgenciaController.cs # GET/PUT /api/asientos-agencia/* (auth: X-Agencia-Token)
│   ├── HandshakeController.cs      # POST /api/agencias/handshake (sin auth previa)
│   ├── AgenciaController.cs        # CRUD agencias + GET /api/agencias/mi-agencia
│   ├── VueloAgenciaController.cs   # POST /api/vuelos-agencia/buscar (auth: AgenciaToken)
│   ├── ReservacionAgenciaController.cs    # POST /api/reservaciones-agencia (auth: AgenciaToken)
│   ├── ReservacionesAgenciaController.cs  # GET gestión reservas agencia
│   ├── ConfirmarReservacionAgenciaController.cs # POST confirmar compra agencia
│   ├── EmailController.cs          # GET correo reservación, POST contacto, POST newsletter
│   ├── PdfController.cs            # GET /api/reservaciones/{id}/comprobante (text/html)
│   ├── AeropuertosController.cs    # GET /api/aeropuertos?query=...
│   ├── RutasController.cs          # CRUD rutas
│   ├── RutaAgenciaController.cs    # GET /api/rutas-agencia (auth: AgenciaToken)
│   ├── ComentariosController.cs    # GET/POST /api/comentarios
│   ├── VotosController.cs          # POST /api/comentarios/{id}/votos
│   ├── PerfilController.cs         # GET/PATCH /api/perfil
│   ├── MisReservacionesController.cs # GET /api/mis-reservaciones
│   ├── NacionalidadesController.cs # GET /api/nacionalidades
│   ├── AvionesController.cs        # CRUD aviones
│   ├── TripulacionController.cs    # CRUD tripulación
│   ├── MetricasController.cs       # GET /api/metricas (admin)
│   ├── HealthController.cs         # GET /health
│   ├── HotelAliadoController.cs    # CRUD hoteles aliados
│   ├── HandshakeHotelController.cs # POST handshake con hotel aliado
│   └── Busquedacontroller.cs       # Búsquedas guardadas
│
├── Services/ (33 archivos)
│   ├── AuthService.cs              # Validar credenciales + BCrypt
│   ├── UsuarioService.cs           # Registro, validación duplicados
│   ├── VueloService.cs             # Búsqueda vuelos directos + BFS escalas
│   ├── ReservacionService.cs       # Crear reserva, asignar asientos (TX SERIALIZABLE)
│   ├── FacturaService.cs           # Procesar compra, crear factura
│   ├── AgenciaService.cs           # CRUD agencias
│   ├── HandshakeService.cs         # Intercambio de tokens con agencias
│   ├── VueloAgenciaService.cs      # Búsqueda con descuento de agencia
│   ├── ReservacionAgenciaService.cs# Reservas para agencias (10min expiración)
│   ├── ConfirmarReservacionAgenciaService.cs # Confirmación pago agencia
│   ├── GestionReservacionService.cs# Admin: ver y cancelar reservas
│   ├── AdminVueloService.cs        # Admin: crear/cancelar vuelos, zonas horarias
│   ├── AdminReservacionesService.cs# Admin: listar, cancelar + notificar agencia
│   ├── AsientoService.cs           # Disponibilidad y cambio de asientos (usuario)
│   ├── AsientoAgenciaService.cs    # Asientos para agencias
│   ├── AgenciaNotificadorExternoService.cs # POST HTTP a URL de agencia externa
│   ├── RutaService.cs              # Cálculo de zonas horarias y hora llegada
│   ├── ComentarioService.cs        # Comentarios de vuelos
│   ├── PdfService.cs               # Generación PDF (QuestPDF)
│   ├── MetricasService.cs          # Estadísticas del sistema
│   ├── ReservasCleanupService.cs   # BackgroundService: expira reservas pendientes
│   └── ... (otros servicios)
│
├── Repositories/ (26 archivos)
│   ├── UsuarioRepository.cs        # CRUD usuario, verificar duplicados
│   ├── VueloRepository.cs          # Búsqueda vuelos, BFS escalas, tripulantes
│   ├── ReservacionRepository.cs    # TX SERIALIZABLE: create, asientos, expirar
│   ├── FacturaRepository.cs        # Crear factura, confirmar boletos
│   ├── AgenciaRepository.cs        # CRUD agencia, guardar/leer tokens
│   ├── AdminVueloRepository.cs     # Crear vuelo, asignar tripulación, cancelar
│   ├── AdminReservacionesRepository.cs # Listar reservas con join completo
│   ├── AsientoRepository.cs        # Asientos ocupados, cambiar asiento
│   ├── AsientoAgenciaRepository.cs # Asientos para agencias
│   ├── ComentarioRepository.cs     # Comentarios, votos
│   ├── MetricasRepository.cs       # Queries de estadísticas
│   └── ... (otros repositorios)
│
├── Models/ (19 entidades)
│   # Usuario, Vuelo, Reservacion, Boleto, DatosPasajero, Ruta,
│   # Aeropuerto, Avion, Tripulante, RolTripulacion, Agencia,
│   # Factura, Comentario, Down, Ciudad, Pais, Nacionalidad...
│
├── DTOs/ (40+ objetos de transferencia)
│   # CrearUsuarioDTO, LoginRequestDto, BuscarVueloDTO,
│   # CrearReservacionDTO, AgregarPasajerosDTO, ComprarReservacionDTO,
│   # HandshakeRequestDTO, HandshakeResponseDTO, ...
│
├── Helpers/
│   ├── SessionHelper.cs            # GetUsuarioId, GetRolId, GetNombre, TieneRol
│   ├── AgenciaAuthMiddleware.cs    # IAsyncActionFilter: valida X-Agencia-Token
│   ├── EmailHelper.cs              # SMTP Gmail: Enviar, EnviarConCopia, Esc (sanitize)
│   ├── EmailTemplates.cs           # Plantillas HTML: reservación, contacto, newsletter
│   ├── PdfHtmlHelper.cs            # HTML del comprobante para impresión
│   ├── TokenHelper.cs              # SHA-256, 64 chars hex
│   ├── PasswordHasher.cs           # BCrypt wrapper
│   └── TarjetaHelper.cs            # Validación de tarjetas (demo)
│
└── Data/
    └── DbConnectionFactory.cs      # Singleton: crea SqlConnection""")

    h2(doc, "2.2 Árbol del Frontend")
    code(doc,
"""AirLine Broom/src/
├── App.svelte          # Router SPA manual (window.location.pathname),
│                       # sesión reactiva, protección de rutas
├── main.js             # Monta App.svelte
├── app.css             # Estilos globales
│
├── lib/
│   └── api.js          # export API = VITE_API_URL || 'http://localhost:5190'
│
├── stores/
│   └── sesion.js       # writable store: null(cargando)/false(anon)/{userData}
│                       # funciones: cargarSesion(), login(), logout()
│
├── components/
│   ├── Header.svelte          # Navbar con links según estado de sesión y rol
│   ├── HeaderSimple.svelte    # Header simplificado para flujo de compra
│   ├── Footer.svelte
│   ├── Loading.svelte         # Splash inicial mientras se carga sesión
│   └── FlightNotification.svelte # Sugerencias de vuelos
│
└── pages/ (30+ vistas)
    ├── Home.svelte             # Inicio + buscador de vuelos
    ├── Vuelos.svelte           # Resultados búsqueda de vuelos
    ├── ResultadosBusqueda.svelte
    ├── SeleccionAsientos.svelte # Mapa de asientos interactivo
    ├── DatosPasajeros.svelte   # Formulario datos de pasajeros
    ├── Carrito.svelte          # Resumen antes de pagar
    ├── Checkout.svelte         # Datos de pago
    ├── Confirmacion.svelte     # Post-compra exitosa
    ├── Login.svelte
    ├── Register.svelte
    ├── Profile.svelte          # Ver/editar perfil usuario
    ├── MisReservas.svelte      # Historial de reservaciones
    ├── Admin.svelte            # Panel de administración (Rol 1)
    ├── MiAgencia.svelte        # Portal webservice (Rol 3)
    ├── DetalleVuelo.svelte
    ├── DetallesReserva.svelte
    ├── Contactanos.svelte
    ├── CentroAyuda.svelte
    ├── Accesodenegado.svelte   # 403
    └── ... (páginas informativas)""")

    h2(doc, "2.3 Mapa Funcionalidad → Archivos")
    table(doc,
        ["Funcionalidad", "Frontend (Vista)", "Controller", "Service", "Repository"],
        [
            ["Login", "Login.svelte", "AuthController", "AuthService", "UsuarioRepository"],
            ["Registro", "Register.svelte", "UsuariosController", "UsuarioService", "UsuarioRepository"],
            ["Logout", "Header.svelte", "AuthController", "(SignOutAsync)", "—"],
            ["Sesión activa", "App.svelte", "AuthController (GET /sesion)", "—", "—"],
            ["Perfil", "Profile.svelte", "PerfilController", "PerfilService", "PerfilRepository"],
            ["Búsqueda vuelos", "Home.svelte / Vuelos.svelte", "VuelosController", "VueloService", "VueloRepository"],
            ["Reservar vuelo", "Carrito.svelte", "ReservacionesController", "ReservacionService", "ReservacionRepository (TX)"],
            ["Datos pasajeros", "DatosPasajeros.svelte", "ReservacionesController (PUT pasajeros)", "ReservacionService", "ReservacionRepository"],
            ["Selección asientos", "SeleccionAsientos.svelte", "AsientoController", "AsientoService", "AsientoRepository"],
            ["Pago", "Checkout.svelte", "ReservacionesController (POST comprar)", "FacturaService", "FacturaRepository"],
            ["Mis reservas", "MisReservas.svelte", "MisReservacionesController", "GestionReservacionService", "GestionReservacionRepository"],
            ["Cancelar", "MisReservas.svelte", "AdminReservacionesController (admin)", "GestionReservacionService", "GestionReservacionRepository"],
            ["Panel admin", "Admin.svelte", "AdminVuelosController, AdminReservacionesController, MetricasController", "AdminVueloService, etc.", "Repositorios admin"],
            ["Handshake agencia", "MiAgencia.svelte", "HandshakeController", "HandshakeService", "AgenciaRepository"],
            ["Compra agencia", "(webservice externo)", "ReservacionAgenciaController, ConfirmarReservacionAgenciaController", "ReservacionAgenciaService, ConfirmarReservacionAgenciaService", "ReservacionAgenciaRepository"],
            ["Cancelación por agencia", "(webservice externo)", "AdminReservacionesController (POST cancelar)", "AdminReservacionesService", "AdminReservacionesRepository"],
            ["Notificar agencia", "(automático)", "—", "AgenciaNotificadorExternoService", "—"],
        ]
    )

    h2(doc, "2.4 Endpoints REST Internos (Usuario Final)")
    table(doc,
        ["Método", "Ruta", "Controller.Método", "Descripción", "Roles"],
        [
            ["POST", "/api/auth/login", "AuthController.Login", "Autenticación con cookie cifrada", "Público"],
            ["GET",  "/api/auth/sesion", "AuthController.ObtenerSesion", "Validar sesión activa", "Autenticado"],
            ["POST", "/api/auth/logout", "AuthController.Logout", "Cerrar sesión", "Autenticado"],
            ["POST", "/api/usuarios", "UsuariosController.Crear", "Registro de nuevo usuario", "Público"],
            ["POST", "/api/usuarios/verificar", "UsuariosController.Verificar", "Verificar duplicados correo/username/pasaporte", "Público"],
            ["PUT",  "/api/usuarios/cambiar-rol", "UsuariosController.CambiarRol", "Cambiar rol de usuario", "Admin(RolId=1)"],
            ["GET",  "/api/vuelos/busqueda-general", "VuelosController.BusquedaGeneral", "Búsqueda full-text de vuelos (max 50)", "Público"],
            ["POST", "/api/vuelos/buscar", "VuelosController.Buscar", "Buscar vuelos por ruta/fecha; incluye escalas BFS", "Público"],
            ["POST", "/api/reservaciones", "ReservacionesController.Crear", "Crear reservación (TX SERIALIZABLE, 10min expiración)", "Autenticado"],
            ["PUT",  "/api/reservaciones/{id}/pasajeros", "ReservacionesController.AgregarPasajeros", "Asignar pasajeros a boletos", "Autenticado"],
            ["POST", "/api/reservaciones/{id}/comprar", "ReservacionesController.Comprar", "Pagar y generar factura", "Autenticado"],
            ["GET",  "/api/reservaciones/{id}/correo", "EmailController.EnviarCorreo", "Enviar comprobante por email", "Autenticado o Admin"],
            ["GET",  "/api/reservaciones/{id}/comprobante", "PdfController.Comprobante", "HTML del comprobante (text/html)", "Autenticado o Admin"],
            ["GET",  "/api/asientos/{vueloId}", "AsientoController.Disponibles", "Mapa de asientos por vuelo/clase", "Público"],
            ["PUT",  "/api/asientos/{boletoId}", "AsientoController.Cambiar", "Cambiar asiento seleccionado", "Autenticado"],
            ["GET",  "/api/aeropuertos", "AeropuertosController.Buscar", "Búsqueda aeropuertos por texto", "Público"],
            ["GET",  "/api/nacionalidades", "NacionalidadesController.Listar", "Lista de nacionalidades", "Público"],
            ["POST", "/api/contacto", "EmailController.Contacto", "Reenviar formulario de contacto al admin", "Público"],
            ["POST", "/api/newsletter", "EmailController.Newsletter", "Suscripción newsletter", "Público"],
            ["POST", "/api/admin/vuelos", "AdminVuelosController.Crear", "Crear nuevo vuelo con tripulación", "Admin"],
            ["PUT",  "/api/admin/vuelos/{id}/cancelar", "AdminVuelosController.Cancelar", "Cancelar vuelo", "Admin"],
            ["GET",  "/api/admin/reservaciones", "AdminReservacionesController.ListarTodas", "Listar todas las reservaciones", "Admin"],
            ["POST", "/api/admin/reservaciones/{id}/cancelar", "AdminReservacionesController.Cancelar", "Cancelar reserva + notificar agencia externa", "Admin"],
            ["GET",  "/api/metricas", "MetricasController.Obtener", "Estadísticas del sistema", "Admin"],
            ["GET",  "/health", "HealthController.Check", "Health check para Docker", "Público"],
        ]
    )

    h2(doc, "2.5 Rutas del Frontend")
    table(doc,
        ["Ruta (pathname)", "Componente", "Propósito", "Protección"],
        [
            ["/home", "Home.svelte", "Inicio y buscador de vuelos", "Pública"],
            ["/vuelos", "Vuelos.svelte", "Resultados de búsqueda", "Pública"],
            ["/detalle-vuelo", "DetalleVuelo.svelte", "Detalle de un vuelo específico", "Pública"],
            ["/login", "Login.svelte", "Formulario de login", "Pública"],
            ["/register", "Register.svelte", "Formulario de registro", "Pública"],
            ["/seleccion-asientos", "SeleccionAsientos.svelte", "Seleccionar asientos", "Autenticado"],
            ["/datos-pasajeros", "DatosPasajeros.svelte", "Ingresar datos de pasajeros", "Autenticado"],
            ["/carrito", "Carrito.svelte", "Resumen de la compra", "Autenticado"],
            ["/checkout", "Checkout.svelte", "Datos de pago", "Autenticado"],
            ["/confirmacion", "Confirmacion.svelte", "Compra confirmada", "Autenticado"],
            ["/profile", "Profile.svelte", "Perfil del usuario", "Autenticado"],
            ["/reservas", "MisReservas.svelte", "Historial de reservaciones", "Autenticado"],
            ["/admin", "Admin.svelte", "Panel de administración", "RolNombre='Administrador'"],
            ["/mi-agencia", "MiAgencia.svelte", "Portal de la agencia webservice", "RolId=3 (Webservice)"],
            ["/contactanos", "Contactanos.svelte", "Formulario de contacto", "Pública"],
            ["/acceso-denegado", "Accesodenegado.svelte", "403 Forbidden", "Pública"],
        ]
    )
    page_break(doc)

    # ══════════════════════════════════════════════════════
    # PARTE 3 — FLUJOS INTERNOS END-TO-END
    # ══════════════════════════════════════════════════════
    h1(doc, "Parte 3 — Flujos Internos End-to-End")

    h2(doc, "Flujo 1 — Login de Usuario")
    body(doc, "El usuario ingresa correo/username y contraseña en Login.svelte. El store sesion.js llama a POST /api/auth/login.")
    code(doc,
"""Paso 1: Login.svelte → sesion.login(correo, contrasena)
Paso 2: stores/sesion.js → fetch POST /api/auth/login
        body: { correoOUsername, contrasena }
Paso 3: AuthController.Login() → AuthService.Login(dto)
Paso 4: AuthService → UsuarioRepository.ObtenerPorCorreoOUsername()
        SQL: SELECT Id, Correo, ContrasenaHash, Pasaporte, Username, Nombre,
                    Apellido, Telefono, FechaNacimiento, CiudadId, RolID
             FROM Usuario WHERE Correo=@v OR Username=@v
Paso 5: BCrypt.Verify(dto.Contrasena, usuario.ContrasenaHash)
        Si falla → HTTP 401 "Credenciales inválidas"
Paso 6: Construir ClaimsPrincipal:
        ClaimTypes.NameIdentifier = usuarioId.ToString()
        ClaimTypes.Role           = "Administrador" | "Usuario" | "Webservice"
        ClaimTypes.Name           = nombre
        ClaimTypes.Email          = correo
        "RolId"                   = rolId.ToString()
Paso 7: HttpContext.SignInAsync(CookieAuthenticationScheme, principal, props)
        Cookie "aerolinea_session": HttpOnly=true, SameSite=Lax, Secure=SameAsRequest
        Expiración: 8 horas (SlidingExpiration)
Paso 8: HTTP 200 → { usuarioId, nombre, correo, rolId, rolNombre }
Paso 9: sesion.js → sesion.set(userData); sessionStorage si se desea
Paso 10: App.svelte redirige según rol: Admin → /admin, Webservice → /mi-agencia, Cliente → /home""")

    h2(doc, "Flujo 2 — Registro de Usuario")
    code(doc,
"""Paso 1:  Register.svelte → POST /api/usuarios/verificar (pre-check duplicados)
          body: { correo, username, pasaporte }
          SQL: SELECT COUNT(*) FROM Usuario WHERE Correo=@v
               SELECT COUNT(*) FROM Usuario WHERE Username=@v
               SELECT COUNT(*) FROM Usuario WHERE Pasaporte=@v
Paso 2:  Si hay duplicado → mostrar mensaje al usuario
Paso 3:  POST /api/usuarios
          body: CrearUsuarioDTO { correo, contrasena, pasaporte, username,
                                  nombre, apellido, telefono, fechaNacimiento,
                                  pais, ciudad, nacionalidades[], rolID=2 }
Paso 4:  UsuariosController → UsuarioService.CrearUsuario(dto)
Paso 5:  PasswordHasher.Hash(dto.Contrasena) → ContrasenaHash (BCrypt)
Paso 6:  UsuarioRepository.CrearUsuario()
          SQL: INSERT INTO Usuario (Correo,ContrasenaHash,Pasaporte,Username,Nombre,
                                    Apellido,Telefono,FechaNacimiento,CiudadId,RolID)
               OUTPUT INSERTED.Id VALUES (...)
Paso 7:  Para cada nacionalidad: INSERT INTO UsuarioNacionalidad (UsuarioId, NacionalidadId)
Paso 8:  HTTP 200 → { message: "Usuario creado correctamente" }""")

    h2(doc, "Flujo 3 — Búsqueda de Vuelos (con Escalas BFS)")
    body(doc, "El sistema busca vuelos directos y con escala usando un algoritmo BFS (Breadth-First Search) por capas de rutas.")
    code(doc,
"""Paso 1:  Home.svelte → POST /api/vuelos/buscar
          body: { origenId, destinoId, fecha, cantidadPasajeros, claseId,
                  precioMinimo?, precioMaximo? }
Paso 2:  VuelosController.Buscar() → VueloService.BuscarVuelos(dto, usuarioId?)
Paso 3:  Si usuario autenticado → BusquedaVueloRepository.RegistrarBusqueda()
          SQL: INSERT INTO BusquedaVuelo (UsuarioId, OrigenId, DestinoId, Fecha, FechaBusqueda)
               VALUES (...)
Paso 4:  VueloRepository.BuscarVuelosDirectos(origenId, destinoId, fecha, clase, cant)
          SQL: SELECT v.ID, v.NumeroVuelo, v.Fecha, v.HoraSalida, v.HoraLlegada,
                      e.Estatus, a.Modelo, a.Marca, ...
               FROM Vuelo v
               INNER JOIN Estado e ON v.EstadoID=e.ID
               INNER JOIN Avion a ON v.AvionID=a.ID
               INNER JOIN Ruta r ON v.RutaID=r.ID
               INNER JOIN Aeropuerto ao ON r.OrigenID=ao.ID
               INNER JOIN Aeropuerto ad ON r.DestinoID=ad.ID
               WHERE r.OrigenID=@orig AND r.DestinoID=@dest
                 AND v.Fecha=@fecha AND e.Estatus='A tiempo'
               ORDER BY v.HoraSalida
Paso 5:  BFS: obtener aeropuertos intermedios, buscar vuelos O→M en fecha f,
         luego M→D en fecha f o f+1 (si hay escala overnight)
Paso 6:  Filtrar por disponibilidad (BoletosTurista>0 o BoletosEjecutivo>0 según clase)
Paso 7:  Aplicar filtros de precio si se especificaron
Paso 8:  HTTP 200 → { vuelosDirectos: [...], vuelosConEscala: [...] }""")

    h2(doc, "Flujo 4 — Reservación + Selección de Asientos + Pago")
    code(doc,
"""--- FASE 1: CREAR RESERVACIÓN ---
Paso 1:  Carrito.svelte → POST /api/reservaciones
          body: { vuelos: [{vueloId, claseId, cantidadPasajeros}] }
Paso 2:  ReservacionesController → ReservacionService.CrearReservacion(dto, usuarioId)
Paso 3:  TX SERIALIZABLE:
         a) Expirar reservaciones pendientes antiguas del mismo usuario:
            UPDATE Reservacion SET EstadoReservaID=4
            WHERE UsuarioID=@uid AND EstadoReservaID=1
              AND FechaExpiracion<GETDATE()
         b) Para cada vuelo seleccionado:
            SELECT BoletosTurista FROM Vuelo WITH (UPDLOCK,ROWLOCK) WHERE ID=@vid
            Si disponibles < solicitados → Error 409 "Sin disponibilidad"
         c) Descontar boletos:
            UPDATE Vuelo SET BoletosTurista=BoletosTurista-@cant WHERE ID=@vid
         d) Asignar asientos automáticamente (A1, A2, B1...):
            SELECT NoAsiento FROM Boleto WHERE VueloID=@vid AND ClaseID=@claseId
              AND EstadoBoletoID IN (2,3)  -- ocupados
            Generar secuencialmente el siguiente disponible
         e) INSERT INTO Reservacion (NoReservacion, UsuarioID, FechaCreacion,
                                     FechaExpiracion, Total, EstadoReservaID)
            VALUES (@noRes, @uid, GETDATE(), DATEADD(MINUTE,10,GETDATE()), @total, 1)
         f) INSERT INTO Boleto (ReservacionID, VueloID, ClaseID, NoAsiento,
                                EstadoBoletoID, Precio) VALUES (...)
Paso 4:  HTTP 200 → { reservacionId, noReservacion, boletos:[{boletoId,asiento}] }

--- FASE 2: DATOS DE PASAJEROS ---
Paso 5:  DatosPasajeros.svelte → PUT /api/reservaciones/{id}/pasajeros
          body: { pasajeros: [{nombre,apellido,pasaporte,telefono,ciudad,pais}] }
Paso 6:  INSERT INTO DatosPasajero (Nombre,Apellido,Pasaporte,Telefono,CiudadId,PaisId)
         UPDATE Boleto SET DatosPasajeroID=@dpId WHERE ID=@boletoId

--- FASE 3: PAGO Y FACTURACIÓN ---
Paso 7:  Checkout.svelte → POST /api/reservaciones/{id}/comprar
          body: { nit, codigoPostal, metodoPago }
Paso 8:  FacturaService.ComprarReservacion(reservacionId, usuarioId, dto)
         a) SELECT r.EstadoReservaID, r.FechaExpiracion FROM Reservacion WHERE ID=@id
            Si estado != 1 o expirada → Error
         b) SELECT COUNT(*) FROM Boleto WHERE ReservacionID=@id AND DatosPasajeroID IS NULL
            Si > 0 → Error "Faltan datos de pasajeros"
         c) INSERT INTO Factura (ReservacionID, Fecha, NIT, CodigoPostal, Total)
            SCOPE_IDENTITY() → facturaId
         d) UPDATE Boleto SET EstadoBoletoID=3 WHERE ReservacionID=@id AND EstadoBoletoID=2
         e) UPDATE Reservacion SET EstadoReservaID=2, FechaExpiracion=NULL WHERE ID=@id
Paso 9:  HTTP 200 → { facturaId, noReservacion, total }
Paso 10: Confirmacion.svelte muestra resumen; usuario puede descargar HTML/PDF""")

    h2(doc, "Flujo 5 — Cancelación de Reservación (por Admin, notificando Agencia)")
    code(doc,
"""Paso 1:  Admin.svelte → POST /api/admin/reservaciones/{id}/cancelar
          body: { motivo }
Paso 2:  AdminReservacionesController → AdminReservacionesService.CancelarAsync(id, motivo)
Paso 3:  UPDATE Reservacion SET EstadoReservaID=3, FechaCancelacion=GETDATE(),
                                MotivoCancelacion=@motivo WHERE ID=@id
Paso 4:  Verificar si es reservación de agencia externa:
         SELECT ag.URL_Agencia FROM Agencia ag
         INNER JOIN Boleto b ON b.ReservacionID=@id
         INNER JOIN Reservacion r ON r.ID=b.ReservacionID
         [Lógica para detectar si el usuario es de tipo Webservice]
Paso 5:  Si es agencia → AgenciaNotificadorExternoService.NotificarCancelacion(id, motivo)
         POST {agencia.URL_Agencia}/api/proveedores-ext/detalles/{id}/cancelar
         body: { motivo }
         IHttpClientFactory: POST HTTP saliente
Paso 6:  HTTP 200 → { message, notificacionAgencia: { esReservaDeAgencia, enviado, httpStatus } }
Paso 7:  _logger.LogInformation("Reservacion {Id} cancelada. Motivo: {Motivo}", id, motivo)""")

    h2(doc, "Flujo 6 — Crear Vuelo (Admin)")
    code(doc,
"""Paso 1:  Admin.svelte → POST /api/admin/vuelos
          body: { numeroVuelo, fecha, horaSalida, aeropuertoOrigenId, aeropuertoDestinoId,
                  avionId, tripulantesIds[], boletosTurista, boletosEjecutivo,
                  precioTurista, precioEjecutiva }
Paso 2:  AdminVuelosController → AdminVueloService.CrearVuelo(dto)
Paso 3:  Buscar/crear ruta:
         SELECT ID, DuracionEstimada FROM Ruta WHERE OrigenID=@org AND DestinoID=@dest
         Si no existe: INSERT INTO Ruta (OrigenID,DestinoID,DuracionEstimada)...
Paso 4:  Obtener zonas horarias de los aeropuertos:
         SELECT ao.ZonaHorariaId FROM Aeropuerto ao WHERE ao.ID=@origenId
         SELECT ad.ZonaHorariaId FROM Aeropuerto ad WHERE ad.ID=@destinoId
Paso 5:  RutaService.CalcularLlegadaConZonas(fecha, horaSalida, duracion, tzOrigen, tzDest)
         Convierte a UTC → suma duración → convierte a zona destino → obtiene HoraLlegada y FechaLlegada
Paso 6:  Validar capacidad: SELECT CapacidadPasajeros FROM Avion WHERE ID=@avionId
         Si boletosTurista+boletosEjecutivo > capacidad → Error
Paso 7:  INSERT INTO Vuelo (NumeroVuelo,Fecha,HoraSalida,HoraLlegada,FechaLlegada,
                           EstadoID,AvionID,RutaID,BoletosTurista,BoletosEjecutivo,
                           PrecioTurista,PrecioEjecutivo) OUTPUT INSERTED.ID VALUES (...)
Paso 8:  Para cada tripulante: INSERT INTO EquipoPivote (VueloID, MiembroTripulacionID)
Paso 9:  HTTP 201 → { vueloId, message }""")
    page_break(doc)

    # ══════════════════════════════════════════════════════
    # PARTE 4 — WEBSERVICE
    # ══════════════════════════════════════════════════════
    h1(doc, "Parte 4 — WebService y Flujo entre Proveedores")

    h2(doc, "4.1 Rol en la Arquitectura Distribuida")
    body(doc, "Broom AirLine actúa como PROVEEDOR en la arquitectura. La Agencia de Viajes (Movent) es el orquestador central que consume los endpoints de esta Aerolínea. El flujo también es bidireccional: cuando un administrador de la Aerolínea cancela una reservación que provino de una agencia, la Aerolínea notifica de vuelta a la Agencia.")
    code(doc,
"""
         ┌─────────────────────────────┐
         │   Agencia Movent (Go)       │  ← Orquestador
         │   Puerto: 8080              │
         └──────────┬──────────────────┘
                    │  X-Agencia-Token (handshake previo)
         ┌──────────▼──────────────────┐
         │  Aerolínea Broom (.NET C#)  │  ← Proveedor (este módulo)
         │  Puerto: 5190 / 8080 Docker │
         └──────────┬──────────────────┘
                    │  POST (cancelación notificada a agencia)
         ┌──────────▼──────────────────┐
         │  Agencia Movent             │
         │  POST /api/proveedores-ext/ │
         │  detalles/{id}/cancelar     │
         └─────────────────────────────┘
""")

    h2(doc, "4.2 Endpoints WebService EXPUESTOS por Broom AirLine")
    body(doc, "Los siguientes endpoints son consumidos por sistemas externos (Agencia Movent). Todos requieren el header X-Agencia-Token, validado por AgenciaAuthMiddleware.cs.")
    table(doc,
        ["Ruta", "Método", "Autenticación", "Descripción", "Controller"],
        [
            ["/api/agencias/handshake", "POST", "Sin auth (inicial)", "Intercambio de tokens con agencia. Recibe {urlAgencia, tokenEntrada}. Genera tokenSalida (SHA-256). Persiste ambos en tabla Agencia.", "HandshakeController.cs"],
            ["/api/vuelos-agencia/buscar", "POST", "X-Agencia-Token", "Búsqueda de vuelos con descuento del porcentaje configurado para la agencia", "VueloAgenciaController.cs"],
            ["/api/reservaciones-agencia", "POST", "X-Agencia-Token", "Crear reservación temporal (10min expiración). Retorna {reservacionId, boletos}", "ReservacionAgenciaController.cs"],
            ["/api/reservaciones-agencia/pasajeros", "POST", "X-Agencia-Token", "Agregar datos de pasajeros a boletos de la reservación agencia", "ReservacionAgenciaController.cs"],
            ["/api/reservaciones-agencia/{id}/expirar", "POST", "X-Agencia-Token", "Expirar reservación si no se confirma (libera asientos)", "ReservacionAgenciaController.cs"],
            ["/api/reservaciones-agencia/{id}/confirmar", "POST", "X-Agencia-Token", "Confirmar compra: crea factura, cambia estado a Confirmada", "ConfirmarReservacionAgenciaController.cs"],
            ["/api/reservaciones-agencia/gestion/{id}", "GET", "X-Agencia-Token", "Detalle completo de reservación (boletos+pasajeros)", "ReservacionesAgenciaController.cs"],
            ["/api/reservaciones-agencia/gestion/{id}/cancelar", "POST", "X-Agencia-Token", "Cancelar reservación de agencia. Notifica de vuelta a URL de agencia.", "ReservacionesAgenciaController.cs"],
            ["/api/reservaciones-agencia/gestion/{id}/puede-cancelar", "GET", "X-Agencia-Token", "Verificar si la reservación puede cancelarse", "ReservacionesAgenciaController.cs"],
            ["/api/asientos-agencia/reservacion/{id}", "GET", "X-Agencia-Token", "Mapa de asientos asignados a una reservación de agencia", "AsientoAgenciaController.cs"],
            ["/api/asientos-agencia/{boletoId}", "PUT", "X-Agencia-Token", "Cambiar asiento de un boleto específico", "AsientoAgenciaController.cs"],
            ["/api/rutas-agencia", "GET", "X-Agencia-Token", "Catálogo de rutas disponibles para la agencia", "RutaAgenciaController.cs"],
            ["/api/agencias/mi-agencia", "GET/POST", "Session (RolId=3)", "El usuario webservice gestiona su propia agencia", "AgenciaController.cs"],
        ]
    )

    h2(doc, "4.3 Validación del Token de Agencia (AgenciaAuthMiddleware)")
    code(doc,
"""// Helpers/AgenciaAuthMiddleware.cs (IAsyncActionFilter)
public async Task OnActionExecutionAsync(ActionExecutingContext context, ActionExecutionDelegate next)
{
    var token = context.HttpContext.Request.Headers["X-Agencia-Token"].ToString();
    if (string.IsNullOrEmpty(token))
    {
        context.Result = new UnauthorizedObjectResult(new { error = "Token requerido" });
        return;
    }
    // Buscar agencia por token
    var agencia = await _agenciaRepo.ObtenerAgenciaPorToken(token);
    // SQL: SELECT ID, Nombre, URL_Agencia FROM Agencia
    //      WHERE Token_HASH_Salida=@token OR Token_HASH_Entrada=@token
    if (agencia == null)
    {
        context.Result = new UnauthorizedObjectResult(new { error = "Token inválido" });
        return;
    }
    context.HttpContext.Items["agencia_id"] = agencia.ID;
    context.HttpContext.Items["agencia_nombre"] = agencia.Nombre;
    await next();
}""")

    h2(doc, "4.4 Flujo de Handshake")
    code(doc,
"""INICIADO POR: Agencia Movent (POST saliente)
RECIBE EN:    Broom AirLine

Paso 1: Agencia POST /api/agencias/handshake
        body: { "urlAgencia": "http://localhost:8080", "tokenEntrada": "abc123...64chars" }
Paso 2: HandshakeController → HandshakeService.ProcesarHandshake(dto)
Paso 3: AgenciaRepository.ObtenerAgenciaPorUrl(dto.UrlAgencia)
        SQL: SELECT ID FROM Agencia WHERE URL_Agencia=@url
        Si no existe → 404 "Agencia no registrada"
Paso 4: TokenHelper.GenerarTokenHash() → tokenSalida (SHA-256, 64 chars hex)
Paso 5: AgenciaRepository.GuardarTokens(agenciaId, dto.TokenEntrada, tokenSalida)
        SQL: UPDATE Agencia SET Token_HASH_Entrada=@entrada, Token_HASH_Salida=@salida
             WHERE ID=@agenciaId
Paso 6: HTTP 200 → { "tokenSalida": "xyz789...64chars" }

RESULTADO:
  - Token_HASH_Entrada = token que Agencia generó (la Agencia lo usa para autenticarse
    en llamadas hacia la Aerolínea vía header X-Agencia-Token)
  - Token_HASH_Salida  = token que Aerolínea generó (usado para futuros handshakes)

CONDICIÓN DE RE-HANDSHAKE:
  - Si el admin de la Aerolínea restablece tokens
  - Si la Agencia detecta error 401 en llamadas a la Aerolínea""")

    h2(doc, "4.5 Flujos Inter-Sistemas — Perspectiva de la Aerolínea")

    h3(doc, "FLUJO A — Búsqueda de Vuelos (Agencia → Aerolínea)")
    code(doc,
"""[Agencia Movent] BusquedaService.llamarVuelos()
    → POST http://{aerolinea_url}/api/vuelos-agencia/buscar
      Headers: X-Agencia-Token: {tokenEntrada guardado en handshake}
      Body: { origen, origen_pais, destino, destino_pais, fecha, pasajeros, clase }

[Aerolínea Broom] VueloAgenciaController → VueloAgenciaService.BuscarVuelos()
    → VueloRepository.BuscarVuelosDirectos() + BFS escalas
    → Aplicar descuento: precioConDescuento = precio * (1 - descuento/100)
    Tabla afectada: Vuelo (solo lectura), Ruta, Aeropuerto, Avion
    → HTTP 200 → [{ ...vueloDetalleDTO con precios con descuento... }]

[Agencia Movent] Recibe lista y aplica porcentaje de ganancia propio:
    precio_final = precio_proveedor * (1 + porcentaje_ganancia%)
    Registra evento TipoOutBusquedaVuelosExitosa (44) en log_sesion""")

    h3(doc, "FLUJO B — Reservación Temporal (Agencia → Aerolínea)")
    code(doc,
"""[Agencia] DetalleReservacionService.AgregarDetalleVuelo()
    → POST {aerolinea_url}/api/reservaciones-agencia
      Headers: X-Agencia-Token: {token}
      Body: { vuelos: [{vueloId, claseId, cantidadPasajeros}] }

[Aerolínea] ReservacionAgenciaController → ReservacionAgenciaService.CrearReservacion()
    TX SERIALIZABLE:
    → SELECT BoletosTurista FROM Vuelo WITH (UPDLOCK,ROWLOCK) WHERE ID=@vid
    → UPDATE Vuelo SET BoletosTurista=BoletosTurista-@cant WHERE ID=@vid
    → INSERT INTO Reservacion (..., FechaExpiracion=DATEADD(MINUTE,10,GETDATE()), EstadoReservaID=1)
    → INSERT INTO Boleto (..., EstadoBoletoID=2)  -- Reservado
    → HTTP 200 → { reservacionId, noReservacion, boletos:[{boletoId, noAsiento}] }

[Agencia] Guarda en tabla detalles_reservacion:
    ID_Reserva_Proveedor = reservacionId devuelto por Aerolínea
    Proveedor_ID = ID del proveedor en tabla Proveedor de Agencia""")

    h3(doc, "FLUJO C — Asientos (Agencia → Aerolínea)")
    code(doc,
"""[Agencia] AsientoVueloService.ObtenerAsientos()
    → GET {aerolinea_url}/api/asientos-agencia/reservacion/{reservacionId}
      Headers: X-Agencia-Token: {token}

[Aerolínea] AsientoAgenciaController → AsientoAgenciaService.ObtenerAsientos()
    SQL: SELECT b.BoletoId, b.NoAsiento, v.NumeroVuelo
         FROM Boleto b JOIN Vuelo v ON b.VueloID=v.ID
         WHERE b.ReservacionID=@resId
    Validación: reservación pertenece a esta agencia
    → HTTP 200 → [{ boletoId, asiento, vuelo }]

[Agencia] AsientoVueloService.CambiarAsiento()
    → PUT {aerolinea_url}/api/asientos-agencia/{boletoId}
      Headers: X-Agencia-Token: {token}
      Body: { nuevoAsiento }

[Aerolínea] AsientoAgenciaController → AsientoAgenciaService.CambiarAsiento()
    SQL: UPDATE Boleto SET NoAsiento=@nuevo WHERE ID=@boletoId
    Validación: boleto pertenece a reservación de esta agencia; asiento disponible
    → HTTP 200 → { message }""")

    h3(doc, "FLUJO D — Confirmación de Pago (Agencia → Aerolínea)")
    code(doc,
"""[Agencia] PagoService.Pagar() — después de recibir pago del usuario
    → POST {aerolinea_url}/api/reservaciones-agencia/{id}/confirmar
      Headers: X-Agencia-Token: {token}
      Body: { nit, codigoPostal, metodoPago }

[Aerolínea] ConfirmarReservacionAgenciaController → ConfirmarReservacionAgenciaService()
    TX:
    → SELECT r.EstadoReservaID FROM Reservacion WHERE ID=@id
      Si != 1 (Pendiente) → Error
    → SELECT COUNT(*) FROM Boleto WHERE ReservacionID=@id AND DatosPasajeroID IS NULL
      Si > 0 → Error "Faltan pasajeros"
    → INSERT INTO Factura (ReservacionID, Fecha, NIT, CodigoPostal, Total)
    → UPDATE Boleto SET EstadoBoletoID=3 WHERE ReservacionID=@id AND EstadoBoletoID=2
    → UPDATE Reservacion SET EstadoReservaID=2, FechaExpiracion=NULL WHERE ID=@id
    → HTTP 200 → { facturaId, noReservacion, total }

[Agencia] Registra evento TipoOutPagoProveedorExitoso (60) en log_sesion""")

    h3(doc, "FLUJO E — Cancelación por Admin de Aerolínea (notifica a Agencia)")
    code(doc,
"""[Aerolínea Admin] AdminReservacionesController.CancelarAsync(reservacionId, motivo)
    → AdminReservacionesService.CancelarAsync()
    SQL: UPDATE Reservacion SET EstadoReservaID=3, FechaCancelacion=GETDATE(),
                                MotivoCancelacion=@motivo WHERE ID=@id
    Detecta si reservación es de agencia externa (usuario tiene RolId=3)
    Si sí → AgenciaNotificadorExternoService.NotificarCancelacion(id, motivo)
              POST {agencia.URL_Agencia}/api/proveedores-ext/detalles/{id}/cancelar
              Headers: (ninguno adicional — Agencia valida por URL configurada)
              Body: { motivo }
              Captura respuesta: { httpStatus, cuerpo }

[Agencia Movent] CancelacionProveedorController recibe el POST:
    Middleware ProveedorAuthRequerido valida X-Agencia-Token
    CancelacionProveedorService.CancelarDetallePorProveedor()
    → Marca detalle como Cancelado (estado 3)
    → Actualiza estado de Reservacion → Retenida (7) o Cancelada (3)
    → Registra notificación para el usuario final
    → Registra evento CANCELACION_PROVEEDOR (30) en log_sesion""")

    h2(doc, "4.6 Tabla de Tokens y Credenciales")
    table(doc,
        ["Token / Credencial", "Tabla", "Columna", "Generado por", "Usado en"],
        [
            ["Token Entrada Agencia", "Agencia", "Token_HASH_Entrada", "Sistema Agencia (SHA-256)", "La Agencia lo envía como X-Agencia-Token al llamar a Aerolínea"],
            ["Token Salida Aerolínea", "Agencia", "Token_HASH_Salida", "Aerolínea (SHA-256 interno)", "Handshake posterior"],
            ["Cookie Session", "(en memoria ASP.NET)", "Cookie cifrada", "ASP.NET Core", "Identificar usuarios web; 8h, HttpOnly"],
            ["App Password SMTP", "appsettings / env", "(secreto)", "Gmail", "Envío de correos; valor en .env no mostrado en código"],
        ]
    )

    h2(doc, "4.7 Estados Compartidos y Semántica Cruzada")
    table(doc,
        ["Entidad", "ID Local (Aerolínea)", "ID Externo (en Agencia)", "Tabla donde se guarda"],
        [
            ["Reservación de agencia", "Reservacion.ID (int)", "detalles_reservacion.ID_Reserva_Proveedor en BD Agencia", "Ambas BDs tienen su propio ID"],
            ["Boleto", "Boleto.ID (int)", "Asociado por boletoId en PUT /api/asientos-agencia/{boletoId}", "Boleto tabla en Aerolínea"],
        ]
    )

    h2(doc, "4.8 Manejo de Errores en Comunicación")
    body(doc, "Timeout: IHttpClientFactory no tiene timeout configurado explícitamente en el código leído; usar HttpClient.Timeout recomendado. Si el POST de cancelación a la agencia falla (red, timeout), la cancelación local ya se realizó y el resultado incluye { enviado: false, httpStatus: 0 }. La Aerolínea no tiene reintentos automáticos. Los errores se registran con ILogger.")
    page_break(doc)

    # ══════════════════════════════════════════════════════
    # PARTE 5 — QUERIES SQL
    # ══════════════════════════════════════════════════════
    h1(doc, "Parte 5 — Queries SQL del Backend")
    body(doc, "Base de datos: SQL Server 2022 Express. Driver: Microsoft.Data.SqlClient 6.1.4. Parámetros con @nombre. Transacciones SERIALIZABLE donde se indica.")

    h2(doc, "5.1 Autenticación")
    h3(doc, "Login — obtener usuario por correo o username")
    code(doc,
"""-- UsuarioRepository.cs
-- Endpoint: POST /api/auth/login → AuthController → AuthService → UsuarioRepository
SELECT Id, Correo, ContrasenaHash, Pasaporte, Username, Nombre, Apellido,
       Telefono, FechaNacimiento, CiudadId, RolID
FROM Usuario
WHERE Correo = @CorreoOUsername OR Username = @CorreoOUsername""")

    h2(doc, "5.2 Registro de Usuario")
    code(doc,
"""-- UsuarioRepository.cs
-- Endpoint: POST /api/usuarios → UsuariosController → UsuarioService → UsuarioRepository

-- Verificar duplicados (3 queries separados)
SELECT COUNT(*) FROM Usuario WHERE Correo    = @Correo
SELECT COUNT(*) FROM Usuario WHERE Username  = @Username
SELECT COUNT(*) FROM Usuario WHERE Pasaporte = @Pasaporte

-- Crear usuario
INSERT INTO Usuario (Correo, ContrasenaHash, Pasaporte, Username, Nombre,
                     Apellido, Telefono, FechaNacimiento, CiudadId, RolID)
OUTPUT INSERTED.Id
VALUES (@Correo, @ContrasenaHash, @Pasaporte, @Username, @Nombre,
        @Apellido, @Telefono, @FechaNacimiento, @CiudadId, @RolID)

-- Agregar nacionalidades (1 por nacionalidad)
INSERT INTO UsuarioNacionalidad (UsuarioId, NacionalidadId)
VALUES (@UsuarioId, @NacionalidadId)""")

    h2(doc, "5.3 Búsqueda de Vuelos")
    code(doc,
"""-- VueloRepository.cs
-- Endpoint: POST /api/vuelos/buscar → VuelosController → VueloService

-- Vuelos directos por ruta y fecha
SELECT v.ID, v.NumeroVuelo, v.Fecha, v.HoraSalida, v.HoraLlegada,
       e.ID AS EstadoId, e.Estatus,
       a.ID AS AvionId, a.Modelo, a.Marca, a.CapacidadPasajeros,
       ao.ID, ao.Nombre, ao.Codigo, co.Nombre, po.Nombre,
       ad.ID, ad.Nombre, ad.Codigo, cd.Nombre, pd.Nombre,
       r.ID AS RutaId, r.DuracionEstimada,
       v.PrecioTurista, v.PrecioEjecutivo,
       v.BoletosTurista, v.BoletosEjecutivo, v.FechaLlegada
FROM Vuelo v
INNER JOIN Estado     e  ON v.EstadoID  = e.ID
INNER JOIN Avion      a  ON v.AvionID   = a.ID
INNER JOIN Ruta       r  ON v.RutaID    = r.ID
INNER JOIN Aeropuerto ao ON r.OrigenID  = ao.ID
INNER JOIN Aeropuerto ad ON r.DestinoID = ad.ID
INNER JOIN Ciudad     co ON ao.CiudadID = co.ID
INNER JOIN Ciudad     cd ON ad.CiudadID = cd.ID
INNER JOIN Pais       po ON co.PaisID   = po.ID
INNER JOIN Pais       pd ON cd.PaisID   = pd.ID
WHERE r.OrigenID  = @origenId
  AND r.DestinoID = @destinoId
  AND v.Fecha     = @fecha
  AND e.Estatus   = 'A tiempo'
ORDER BY v.HoraSalida

-- Búsqueda general full-text (Home.svelte)
SELECT TOP 50 v.ID, v.NumeroVuelo, ... [mismo join] ...
WHERE e.Estatus = 'A tiempo' AND v.Fecha >= CAST(GETDATE() AS DATE)
  AND (v.BoletosTurista > 0 OR v.BoletosEjecutivo > 0)
  AND (co.Nombre LIKE @busqueda OR cd.Nombre LIKE @busqueda
    OR ao.Codigo LIKE @busqueda OR ad.Codigo LIKE @busqueda
    OR po.Nombre LIKE @busqueda OR pd.Nombre LIKE @busqueda
    OR ao.Nombre LIKE @busqueda OR ad.Nombre LIKE @busqueda
    OR v.NumeroVuelo LIKE @busqueda)
ORDER BY v.Fecha, v.HoraSalida

-- Tripulantes por vuelo
SELECT t.ID, t.Nombre, t.Apellido, rt.Nombre AS Rol
FROM EquipoPivote ep
INNER JOIN MiembroTripulacion t  ON ep.MiembroTripulacionID = t.ID
INNER JOIN RolTripulacion     rt ON t.RolTripulacionId      = rt.ID
WHERE ep.VueloID = @vueloId""")

    h2(doc, "5.4 Reservación")
    code(doc,
"""-- ReservacionRepository.cs
-- Endpoint: POST /api/reservaciones → TX SERIALIZABLE

-- Expirar pendientes antiguas del usuario
UPDATE Reservacion SET EstadoReservaID = 4
WHERE UsuarioID = @usuarioId AND EstadoReservaID = 1
  AND FechaExpiracion < GETDATE()

-- Verificar disponibilidad (con lock para evitar race-condition)
SELECT BoletosTurista, PrecioTurista FROM Vuelo
WITH (UPDLOCK, ROWLOCK) WHERE ID = @vueloId

-- (si clase Ejecutivo)
SELECT BoletosEjecutivo, PrecioEjecutivo FROM Vuelo
WITH (UPDLOCK, ROWLOCK) WHERE ID = @vueloId

-- Obtener asientos ocupados (para asignar el siguiente libre)
SELECT NoAsiento FROM Boleto
WHERE VueloID = @vueloId AND ClaseID = @claseId
  AND EstadoBoletoID IN (2, 3)

-- Descontar boletos (una vez asegurado el lock)
UPDATE Vuelo SET BoletosTurista = BoletosTurista - @cantidad WHERE ID = @vueloId

-- Crear reservación
INSERT INTO Reservacion (NoReservacion, UsuarioID, FechaCreacion, FechaExpiracion,
                         Total, EstadoReservaID)
VALUES (@noReservacion, @usuarioId, GETDATE(), DATEADD(MINUTE,10,GETDATE()), @total, 1)
SELECT CAST(SCOPE_IDENTITY() AS INT)

-- Insertar boleto con asiento asignado
INSERT INTO Boleto (ReservacionID, VueloID, ClaseID, NoAsiento, EstadoBoletoID, Precio)
VALUES (@reservacionId, @vueloId, @claseId, @asiento, 2, @precio)""")

    h2(doc, "5.5 Pago y Facturación")
    code(doc,
"""-- FacturaRepository.cs
-- Endpoint: POST /api/reservaciones/{id}/comprar

-- Verificar estado
SELECT r.EstadoReservaID, r.FechaExpiracion, r.Total, r.NoReservacion, r.UsuarioID
FROM Reservacion r WHERE r.ID = @reservacionId

-- Verificar boletos sin pasajero asignado
SELECT COUNT(*) FROM Boleto
WHERE ReservacionID = @reservacionId
  AND DatosPasajeroID IS NULL
  AND EstadoBoletoID = 2

-- Crear factura
INSERT INTO Factura (ReservacionID, Fecha, NIT, CodigoPostal, Total)
VALUES (@reservacionId, @fecha, @nit, @codigoPostal, @total)
SELECT CAST(SCOPE_IDENTITY() AS INT)

-- Confirmar boletos
UPDATE Boleto SET EstadoBoletoID = 3
WHERE ReservacionID = @reservacionId AND EstadoBoletoID = 2

-- Confirmar reservación
UPDATE Reservacion SET EstadoReservaID = 2, FechaExpiracion = NULL
WHERE ID = @reservacionId""")

    h2(doc, "5.6 Cancelación")
    code(doc,
"""-- AdminReservacionesRepository.cs / GestionReservacionRepository.cs
-- Endpoint: POST /api/admin/reservaciones/{id}/cancelar

UPDATE Reservacion
SET EstadoReservaID     = 3,
    FechaCancelacion    = GETDATE(),
    MotivoCancelacion   = @motivo
WHERE ID = @reservacionId

-- Liberar boletos (si aplica según política)
UPDATE Boleto SET EstadoBoletoID = 4 WHERE ReservacionID = @reservacionId""")

    h2(doc, "5.7 Agencia — CRUD y Tokens")
    code(doc,
"""-- AgenciaRepository.cs

-- Crear agencia
INSERT INTO Agencia (Nombre, Correo, UsuarioWebID, PorcentajeDescuento,
                     EstadoAgenciaID, Token_HASH_Entrada, Token_HASH_Salida, URL_Agencia)
OUTPUT INSERTED.ID
VALUES (@Nombre, @Correo, @UsuarioWebID, @Desc, @Estado, '', '', @Url)

-- Guardar tokens tras handshake
UPDATE Agencia
SET Token_HASH_Entrada = @entrada, Token_HASH_Salida = @salida
WHERE ID = @agenciaId

-- Obtener agencia por token (AgenciaAuthMiddleware)
SELECT ID, Nombre, URL_Agencia FROM Agencia
WHERE Token_HASH_Salida = @token OR Token_HASH_Entrada = @token

-- Obtener agencia por URL (HandshakeService)
SELECT ID FROM Agencia WHERE URL_Agencia = @url

-- Actualizar descuento
UPDATE Agencia SET PorcentajeDescuento = @descuento WHERE ID = @agenciaId

-- Actualizar estado
UPDATE Agencia SET EstadoAgenciaID = @estadoId WHERE ID = @agenciaId

-- Usuarios disponibles para asignar a agencia (rol Webservice sin agencia)
SELECT u.ID, u.Nombre, u.Correo FROM Usuario u
WHERE u.RolID = 3
  AND u.ID NOT IN (SELECT UsuarioWebID FROM Agencia)
  AND u.ID NOT IN (SELECT UsuarioWEBIs FROM HotelAliado)""")

    h2(doc, "5.8 Asientos")
    code(doc,
"""-- AsientoRepository.cs / AsientoAgenciaRepository.cs

-- Asientos ocupados por vuelo/clase
SELECT NoAsiento FROM Boleto
WHERE VueloID = @vueloId AND ClaseID = @claseId
  AND EstadoBoletoID IN (2, 3)

-- Cambiar asiento
UPDATE Boleto SET NoAsiento = @nuevoAsiento WHERE ID = @boletoId

-- Asientos de reservación de agencia
SELECT b.ID AS BoletoId, b.NoAsiento, v.NumeroVuelo
FROM Boleto b JOIN Vuelo v ON b.VueloID = v.ID
WHERE b.ReservacionID = @reservacionId""")

    h2(doc, "5.9 Admin — Crear y Gestionar Vuelos")
    code(doc,
"""-- AdminVueloRepository.cs

-- Buscar o crear ruta
SELECT ID, DuracionEstimada FROM Ruta
WHERE OrigenID = @OrigenId AND DestinoID = @DestinoId

INSERT INTO Ruta (OrigenID, DestinoID, DuracionEstimada)
OUTPUT INSERTED.ID VALUES (@OrigenId, @DestinoId, @DuracionEstimada)

-- Obtener zona horaria de aeropuerto
SELECT ZonaHorariaId FROM Aeropuerto WHERE ID = @id

-- Obtener capacidad de avión
SELECT CapacidadPasajeros FROM Avion WHERE ID = @avionId

-- Crear vuelo
INSERT INTO Vuelo (NumeroVuelo, Fecha, HoraSalida, HoraLlegada, FechaLlegada,
                   EstadoID, AvionID, RutaID, BoletosTurista, BoletosEjecutivo,
                   PrecioTurista, PrecioEjecutivo)
OUTPUT INSERTED.ID
VALUES (@NumeroVuelo, @Fecha, @HoraSalida, @HoraLlegada, @FechaLlegada,
        @EstadoId, @AvionId, @RutaId, @BoletosTurista, @BoletosEjecutivo,
        @PrecioTurista, @PrecioEjecutivo)

-- Asignar tripulación
INSERT INTO EquipoPivote (VueloID, MiembroTripulacionID)
VALUES (@VueloId, @TripulanteId)

-- Aviones ocupados (para validar antes de crear vuelo)
SELECT DISTINCT v.AvionID FROM Vuelo v
WHERE v.Fecha = @fecha AND v.HoraSalida = @horaSalida
  AND v.EstadoID != 3

-- Tripulantes ocupados
SELECT DISTINCT ep.MiembroTripulacionID FROM EquipoPivote ep
INNER JOIN Vuelo v ON ep.VueloID = v.ID
WHERE v.Fecha = @fecha AND v.HoraSalida = @horaSalida AND v.EstadoID != 3

-- Cancelar vuelo
UPDATE Vuelo SET EstadoID = 3 WHERE ID = @vueloId""")

    h2(doc, "5.10 Admin — Listar Reservaciones (JOIN completo)")
    code(doc,
"""-- AdminReservacionesRepository.cs
-- Endpoint: GET /api/admin/reservaciones

SELECT r.ID AS ReservacionId,
       r.NoReservacion,
       ISNULL(er.Estado, 'Pendiente') AS EstadoReserva,
       ISNULL(r.Total, 0) AS Total,
       r.FechaCreacion, r.FechaExpiracion,
       r.FechaCancelacion, r.MotivoCancelacion,
       ISNULL(u.Nombre + ' ' + u.Apellido, '') AS UsuarioNombre,
       ISNULL(u.Correo, '') AS UsuarioEmail
FROM Reservacion r
INNER JOIN Usuario       u  ON u.ID = r.UsuarioID
LEFT  JOIN EstadoReserva er ON er.ID = r.EstadoReservaID
ORDER BY r.FechaCreacion DESC

-- Boletos con pasajeros (detalle de reservación)
SELECT b.ID AS BoletoId, b.NoBoleto, v.NumeroVuelo,
       ao.Codigo AS OrigenCodigo, co.Nombre AS OrigenCiudad,
       ad.Codigo AS DestinoCodigo, cd.Nombre AS DestinoCiudad,
       v.HoraSalida, v.Fecha AS FechaVuelo,
       r.DuracionEstimada AS DuracionMinutos,
       eb.Estado AS EstadoBoleto, b.Precio, b.NoAsiento,
       c.Nombre AS Clase, a.Marca AS AvionMarca, a.Modelo AS AvionModelo,
       dp.Nombre AS PasajeroNombre, dp.Apellido AS PasajeroApellido,
       dp.Pasaporte, dp.Telefono, cd2.Nombre AS PasajeroCiudad, p.Nombre AS PasajeroPais
FROM Boleto b
LEFT JOIN Vuelo       v   ON b.VueloID        = v.ID
LEFT JOIN Ruta        r   ON v.RutaID         = r.ID
LEFT JOIN Aeropuerto  ao  ON r.OrigenID       = ao.ID
LEFT JOIN Aeropuerto  ad  ON r.DestinoID      = ad.ID
LEFT JOIN Ciudad      co  ON ao.CiudadID      = co.ID
LEFT JOIN Ciudad      cd  ON ad.CiudadID      = cd.ID
LEFT JOIN Avion       a   ON v.AvionID        = a.ID
LEFT JOIN EstadoBoleto eb ON b.EstadoBoletoID = eb.ID
LEFT JOIN Clase       c   ON b.ClaseID        = c.ID
LEFT JOIN DatosPasajero dp ON b.DatosPasajeroID = dp.ID
LEFT JOIN Ciudad      cd2 ON dp.CiudadId     = cd2.ID
LEFT JOIN Pais        p   ON dp.PaisId       = p.ID
WHERE b.ReservacionID = @reservacionId""")
    page_break(doc)

    # ══════════════════════════════════════════════════════
    # ANEXO — MÉTRICAS PARA DEFENSA
    # ══════════════════════════════════════════════════════
    h1(doc, "Anexo — Queries SQL de Métricas para Defensa Oral")
    body(doc, "Todos los queries siguientes pueden ejecutarse directamente contra SQL Server.")

    metricas = [
        ("A.1 Total de usuarios registrados",
         "SELECT COUNT(*) AS TotalUsuarios FROM Usuario"),
        ("A.2 Usuarios por rol",
         """SELECT r.Nombre AS Rol, COUNT(u.Id) AS Total
FROM Usuario u LEFT JOIN Rol r ON u.RolID = r.ID
GROUP BY r.Nombre ORDER BY Total DESC"""),
        ("A.3 Total de reservaciones por estado",
         """SELECT er.Estado, COUNT(r.ID) AS Total
FROM Reservacion r
LEFT JOIN EstadoReserva er ON r.EstadoReservaID = er.ID
GROUP BY er.Estado ORDER BY Total DESC"""),
        ("A.4 Reservaciones por mes (últimos 12 meses)",
         """SELECT FORMAT(r.FechaCreacion, 'yyyy-MM') AS Mes, COUNT(*) AS Total,
       SUM(r.Total) AS Ingresos
FROM Reservacion r
WHERE r.FechaCreacion >= DATEADD(MONTH, -12, GETDATE())
GROUP BY FORMAT(r.FechaCreacion, 'yyyy-MM')
ORDER BY Mes"""),
        ("A.5 Ingresos totales (reservaciones confirmadas)",
         """SELECT SUM(f.Total) AS IngresosTotales
FROM Factura f
INNER JOIN Reservacion r ON r.ID = f.ReservacionID
WHERE r.EstadoReservaID = 2"""),
        ("A.6 Ingresos por mes",
         """SELECT FORMAT(f.Fecha, 'yyyy-MM') AS Mes, SUM(f.Total) AS Ingresos
FROM Factura f
INNER JOIN Reservacion r ON r.ID = f.ReservacionID
WHERE r.EstadoReservaID = 2
GROUP BY FORMAT(f.Fecha, 'yyyy-MM')
ORDER BY Mes"""),
        ("A.7 Top 10 rutas más reservadas",
         """SELECT TOP 10
       ao.Codigo AS Origen, ad.Codigo AS Destino,
       co.Nombre AS CiudadOrigen, cd.Nombre AS CiudadDestino,
       COUNT(b.ID) AS TotalBoletos
FROM Boleto b
INNER JOIN Vuelo      v  ON b.VueloID     = v.ID
INNER JOIN Ruta       r  ON v.RutaID      = r.ID
INNER JOIN Aeropuerto ao ON r.OrigenID    = ao.ID
INNER JOIN Aeropuerto ad ON r.DestinoID   = ad.ID
INNER JOIN Ciudad     co ON ao.CiudadID   = co.ID
INNER JOIN Ciudad     cd ON ad.CiudadID   = cd.ID
WHERE b.EstadoBoletoID IN (2, 3)
GROUP BY ao.Codigo, ad.Codigo, co.Nombre, cd.Nombre
ORDER BY TotalBoletos DESC"""),
        ("A.8 Top 10 usuarios con más reservaciones",
         """SELECT TOP 10 u.Nombre + ' ' + u.Apellido AS Usuario,
       u.Correo, COUNT(r.ID) AS TotalReservaciones
FROM Reservacion r
INNER JOIN Usuario u ON r.UsuarioID = u.Id
GROUP BY u.Nombre, u.Apellido, u.Correo
ORDER BY TotalReservaciones DESC"""),
        ("A.9 Tasa de cancelación",
         """SELECT
  COUNT(CASE WHEN r.EstadoReservaID = 3 THEN 1 END) AS Canceladas,
  COUNT(*)                                           AS Total,
  ROUND(
    100.0 * COUNT(CASE WHEN r.EstadoReservaID = 3 THEN 1 END) / COUNT(*), 2
  ) AS TasaCancelacion_Pct
FROM Reservacion r"""),
        ("A.10 Vuelos con más boletos vendidos",
         """SELECT TOP 10 v.NumeroVuelo, v.Fecha,
       ao.Codigo AS Origen, ad.Codigo AS Destino,
       COUNT(b.ID) AS BoletoVendidos
FROM Boleto b
INNER JOIN Vuelo      v  ON b.VueloID    = v.ID
INNER JOIN Ruta       r  ON v.RutaID     = r.ID
INNER JOIN Aeropuerto ao ON r.OrigenID   = ao.ID
INNER JOIN Aeropuerto ad ON r.DestinoID  = ad.ID
WHERE b.EstadoBoletoID = 3
GROUP BY v.NumeroVuelo, v.Fecha, ao.Codigo, ad.Codigo
ORDER BY BoletoVendidos DESC"""),
        ("A.11 Agencias más activas (más reservaciones)",
         """SELECT TOP 10 ag.Nombre AS Agencia, COUNT(r.ID) AS Reservaciones
FROM Agencia ag
INNER JOIN Usuario u ON ag.UsuarioWebID = u.Id
INNER JOIN Reservacion r ON r.UsuarioID = u.Id
GROUP BY ag.Nombre
ORDER BY Reservaciones DESC"""),
        ("A.12 Handshakes registrados",
         """SELECT ag.Nombre, ag.URL_Agencia,
       CASE WHEN ag.Token_HASH_Entrada <> '' THEN 'Completado' ELSE 'Pendiente' END AS EstadoHandshake
FROM Agencia ag"""),
        ("A.13 Asientos más solicitados",
         """SELECT TOP 10 b.NoAsiento, c.Nombre AS Clase, COUNT(*) AS Veces
FROM Boleto b INNER JOIN Clase c ON b.ClaseID = c.ID
WHERE b.EstadoBoletoID IN (2,3)
GROUP BY b.NoAsiento, c.Nombre
ORDER BY Veces DESC"""),
    ]
    for titulo, sql in metricas:
        h3(doc, titulo)
        code(doc, sql)
    page_break(doc)

    # ══════════════════════════════════════════════════════
    # INVENTARIO DE COBERTURA
    # ══════════════════════════════════════════════════════
    h1(doc, "Inventario de Cobertura")
    table(doc,
        ["Carpeta / Archivo", "Estado", "Notas"],
        [
            ["Aerolinea.API/Aerolinea.API/Controllers/ (34 archivos)", "Documentado", "Todos los endpoints REST y WebService relevantes documentados"],
            ["Aerolinea.API/Aerolinea.API/Services/ (33 archivos)", "Documentado", "Lógica de negocio, BFS búsqueda, BackgroundService"],
            ["Aerolinea.API/Aerolinea.API/Repositories/ (26 archivos)", "Documentado", "Todos los queries SQL extraídos literalmente"],
            ["Aerolinea.API/Aerolinea.API/Models/ (19 entidades)", "Documentado", "Propiedades y relaciones de dominio"],
            ["Aerolinea.API/Aerolinea.API/DTOs/ (40+ DTOs)", "Documentado", "DTOs principales para request/response"],
            ["Aerolinea.API/Aerolinea.API/Helpers/", "Documentado", "PasswordHasher, TokenHelper, EmailHelper, AgenciaAuthMiddleware, SessionHelper"],
            ["Aerolinea.API/Aerolinea.API/Data/DbConnectionFactory.cs", "Documentado", "Fábrica de conexiones SqlConnection"],
            ["Aerolinea.API/Aerolinea.API/Program.cs", "Documentado", "Configuración DI, middleware, auth cookies, CORS"],
            ["Aerolinea.API/Aerolinea.API/appsettings.json", "Documentado", "Connection string SQL Server local"],
            ["Aerolinea.API/Aerolinea.API/appsettings.Docker.json", "Documentado", "Connection string Docker + CORS origins"],
            ["Aerolinea.API/Aerolinea.API/Dockerfile", "Documentado", "Multi-stage build sdk:8.0 + aspnet:8.0 runtime"],
            ["aerolinea-docker/docker-compose.yml", "Documentado", "SQL Server 2022 + backend + frontend con variables de entorno"],
            ["AirLine Broom/src/", "Documentado", "Todas las páginas, stores, componentes del frontend Svelte"],
            ["AirLine Broom/src/stores/sesion.js", "Documentado", "Store writable con cargarSesion/login/logout"],
            ["AirLine Broom/src/lib/api.js", "Documentado", "URL base API configurable por VITE_API_URL"],
            ["AirLine Broom/package.json", "Documentado", "Svelte 5.43.8, Vite 7.2.4, Playwright 1.59.1"],
            ["AirLine Broom/tests/", "Vacío/E2E", "Playwright tests (no cubiertos en detalle en este manual)"],
            ["AerolineaDB (schema SQL)", "No accesible como archivo", "Schema reside en SQL Server; queries documentados desde el código"],
            ["docs-csharp/ (Javadoc .NET)", "No relevante", "Documentación autogenerada del código, no source"],
        ]
    )

    doc.save(OUT)
    print(f"[OK] Guardado: {OUT}")

if __name__ == '__main__':
    build()
