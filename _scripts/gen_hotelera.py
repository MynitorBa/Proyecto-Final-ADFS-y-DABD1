#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Genera Manual_Programador_Hotelera.docx — Miku Inn (Java/Javalin + Svelte)"""

import sys
sys.path.insert(0, r'C:\Proyecto-Final-ADFS-y-DABD1')
from doc_utils import *

OUT = r'C:\Proyecto-Final-ADFS-y-DABD1\Manual_Programador_Hotelera.docx'

def build():
    doc = new_doc()
    set_footer(doc)
    add_cover(doc, "Hotelera — Miku Inn")

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
    table(doc, ["Lenguaje", "Versión", "Contexto"], [
        ["Java", "17", "Backend — lógica de negocio, REST API, acceso a BD Oracle"],
        ["JavaScript / Svelte", "Svelte 5.43.8", "Frontend — SPA del sistema hotelero"],
    ])

    h2(doc, "0.2 Framework del Backend")
    table(doc, ["Framework", "Versión", "Propósito"], [
        ["Javalin", "5.6.1", "Framework HTTP liviano; registra rutas, antes/después de handlers, CORS"],
        ["Maven", "4.0.0 (modelVersion)", "Build tool; gestiona dependencias y genera JAR con dependencias"],
    ])

    h2(doc, "0.3 Framework del Frontend")
    table(doc, ["Framework / Tool", "Versión", "Propósito"], [
        ["Svelte", "5.43.8", "Componentes reactivos; App.svelte maneja routing manual"],
        ["svelte-routing", "2.13.0", "Librería de routing para Svelte SPA"],
        ["Vite", "7.3.1", "Build tool y dev server"],
        ["Jest", "30.3.0", "Tests unitarios del frontend"],
        ["Playwright", "1.59.1", "Tests E2E"],
    ])

    h2(doc, "0.4 Base de Datos y Driver")
    table(doc, ["Componente", "Versión / Detalle", "Propósito"], [
        ["Oracle Database", "XE 21c (Docker recomendado)", "Motor de base de datos relacional (SQL Oracle)"],
        ["ojdbc11", "23.3.0.23.09", "Driver JDBC para Oracle; queries con ? como placeholder"],
        ["DatabaseManager.java", "custom", "Wrapper JDBC: executeQuery, executeUpdate, executeInsertReturnId"],
    ])

    h2(doc, "0.5 Autenticación y Seguridad")
    table(doc, ["Tecnología", "Versión", "Uso"], [
        ["jBCrypt", "0.4", "Hash de contraseñas (12 rondas de salt); PasswordHelper.java"],
        ["jjwt-api/impl/jackson", "0.12.6", "Generación y validación de JWT; JwtHelper.java"],
        ["Cookie JWT", "custom", "Cookie HttpOnly, SameSite=STRICT, 8h; nombre dinámico COOKIE_NAME"],
        ["AgenciaAuthMiddleware.java", "custom", "Valida header X-Agencia-Token en rutas /agencia/*"],
    ])

    h2(doc, "0.6 Herramientas de Build")
    table(doc, ["Herramienta", "Versión", "Uso"], [
        ["Maven", "4.0.0", "Compilar, empacar JAR-with-dependencies"],
        ["npm", "bundled Node 20", "Frontend: instalar deps, build Vite"],
        ["Docker", "recomendado", "Contenedores Oracle XE, backend Java, frontend Svelte"],
    ])

    h2(doc, "0.7 Herramientas Complementarias")
    table(doc, ["Herramienta", "Versión", "Propósito"], [
        ["iText 7", "7.2.5 (kernel, layout, io)", "Generación de PDF de reservaciones (PdfReservacionService.java)"],
        ["javax.mail", "1.6.2", "Envío de correos SMTP (EmailReservacionService.java)"],
        ["slf4j-simple", "2.0.9", "Logging en consola del servidor"],
        ["JUnit 5", "5.10.2", "Tests unitarios del backend"],
        ["Mockito", "5.11.0", "Mocking en tests del backend"],
    ])
    page_break(doc)

    # ══════════════════════════════════════════════════════
    # PARTE 1 — LIBRERÍAS Y DEPENDENCIAS
    # ══════════════════════════════════════════════════════
    h1(doc, "Parte 1 — Librerías y Dependencias")

    h2(doc, "1.1 Generación de PDF — iText 7")
    body(doc, "Librería: com.itextpdf (kernel, layout, io) versión 7.2.5. Servicio: PdfReservacionService.java. Genera un PDF con PdfDocument + Document, agrega párrafos y tablas con los detalles de la reservación (número, habitaciones, fechas de check-in/out, total). Retorna byte[] al controller.")
    body(doc, "Endpoint: GET /pdf/reservaciones/{id} — responde con Content-Type: application/pdf.")
    code(doc,
"""// PdfReservacionService.java — fragmento representativo
PdfDocument pdfDoc = new PdfDocument(new PdfWriter(outputStream));
Document document  = new Document(pdfDoc);

Paragraph titulo = new Paragraph("CONFIRMACIÓN DE RESERVACIÓN")
    .setBold().setFontSize(18).setTextAlignment(TextAlignment.CENTER);
document.add(titulo);

Table tabla = new Table(UnitValue.createPercentArray(new float[]{2,3,2,2}));
tabla.addHeaderCell("Habitación");
tabla.addHeaderCell("Hotel");
tabla.addHeaderCell("Check-in");
tabla.addHeaderCell("Check-out");
// ... agrega filas por cada DetalleReservacion ...
document.add(tabla);

document.close();
return outputStream.toByteArray();""")

    h2(doc, "1.2 Envío de Emails — javax.mail")
    body(doc, "Librería: javax.mail 1.6.2. Servicio: EmailReservacionService.java. Configurado con variables de entorno MAIL_HOST, MAIL_PORT, MAIL_USERNAME, MAIL_PASSWORD, MAIL_FROM. El email es multipart HTML. No hay archivos template; el HTML se genera dinámicamente en el servicio.")
    code(doc,
"""// EmailReservacionService.java
Properties props = new Properties();
props.put("mail.smtp.auth", "true");
props.put("mail.smtp.starttls.enable", "true");
props.put("mail.smtp.host", System.getenv("MAIL_HOST")); // smtp.gmail.com
props.put("mail.smtp.port", System.getenv("MAIL_PORT")); // 587

Session session = Session.getInstance(props, new Authenticator() {
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, password);
    }
});
Message msg = new MimeMessage(session);
msg.setFrom(new InternetAddress(from));
msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
msg.setSubject("Confirmación de Reservación — Miku Inn");

String html = "<html><body><h1>Reservación Confirmada</h1>"
    + "<p>No: " + noReservacion + "</p>"
    + "<p>Total: $" + total + "</p></body></html>";

MimeBodyPart htmlPart = new MimeBodyPart();
htmlPart.setContent(html, "text/html; charset=utf-8");
MimeMultipart mp = new MimeMultipart();
mp.addBodyPart(htmlPart);
msg.setContent(mp);
Transport.send(msg);""")

    h2(doc, "1.3 Autenticación — jBCrypt y JJWT")
    body(doc, "Hash: PasswordHelper.java usa BCrypt.hashpw(password, BCrypt.gensalt(12)). Verificación: BCrypt.checkpw(password, hash).")
    body(doc, "JWT: JwtHelper.java. Algoritmo HMAC-SHA256. Secreto: variable JWT_SECRET (default 'Sabrina_es_la_Best_Bruja_Bonita!'). Duración: 8 horas. Claims: sub=usuarioId, username, rolId, iat, exp. Cookie: HttpOnly, SameSite=STRICT, MaxAge=28800.")
    code(doc,
"""// JwtHelper.java
public String generarToken(int usuarioId, String username, int rolId) {
    return Jwts.builder()
        .setSubject(String.valueOf(usuarioId))
        .claim("username", username)
        .claim("rolId", rolId)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 8*3600*1000))
        .signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS256)
        .compact();
}

public Claims verificarToken(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
        .build().parseClaimsJws(token).getBody();
}""")

    h2(doc, "1.4 Cliente HTTP Saliente")
    body(doc, "Clase: AgenciaNotificadorExternoService.java. Usa java.net.HttpURLConnection (librería estándar Java). Llama POST hacia URL_Agencia registrada en tabla Agencia cuando un administrador cancela una reservación que provino de una agencia externa. No hay timeout configurado explícitamente en el código leído.")

    h2(doc, "1.5 JDBC — DatabaseManager")
    body(doc, "Clase: data/DatabaseManager.java. Wrapper personalizado sobre JDBC que expone tres métodos genéricos con lambdas ResultSetMapper para mapear ResultSet a DTOs. No usa ningún ORM (sin JPA/Hibernate).")
    code(doc,
"""// DatabaseManager.java — API pública
public static <T> List<T> executeQuery(
        String sql, ResultSetMapper<T> mapper, Object... params)
public static int executeUpdate(String sql, Object... params)
public static int executeInsertReturnId(
        String sql, String idColumn, Object... params)
// Internamente: conn.prepareStatement(sql); setObject para cada param;
// ResultSet → mapper.map(rs) por cada fila""")

    h2(doc, "1.6 Framework HTTP — Javalin")
    body(doc, "Javalin 5.6.1 registra todas las rutas en Main.java mediante app.post(), app.get(), app.patch(), etc. CORS configurado para orígenes: http://localhost:5173, http://localhost:5174 y FRONTEND_PORT / CORS_EXTRA_ORIGINS desde entorno. El JWT se valida en AuthMiddleware.java ejecutado con app.before() para rutas protegidas.")
    page_break(doc)

    # ══════════════════════════════════════════════════════
    # PARTE 2 — ARQUITECTURA
    # ══════════════════════════════════════════════════════
    h1(doc, "Parte 2 — Arquitectura y Mapa del Proyecto")

    h2(doc, "2.1 Árbol del Backend")
    code(doc,
"""Hotel/src/main/java/org/example/
├── Main.java                          # Punto de entrada; instancia repos/servicios/controllers;
│                                      # registra rutas Javalin; inicia ExpiracionService
├── config/
│   └── ServerConfig.java              # Javalin app config: CORS, puerto (PORT env)
│
├── controllers/
│   ├── AuthController.java            # POST /auth/login, POST /auth/logout
│   ├── SesionController.java          # GET /sesion
│   ├── UsuarioController.java         # Registro, perfil, cambio contraseña/teléfono
│   ├── BusquedaController.java        # POST /busqueda (usuario web)
│   ├── BusquedaAgenciaController.java # POST /agencia/busqueda (X-Agencia-Token)
│   ├── ReservacionController.java     # POST /reservaciones, GET /reservaciones (usuario)
│   ├── ReservacionAgenciaController.java # Reservaciones para agencias
│   ├── PagoController.java            # POST /reservaciones/{id}/pago
│   ├── PagoAgenciaController.java     # POST /agencia/reservaciones/{id}/pago
│   ├── CancelacionController.java     # POST /cancelaciones (usuario)
│   ├── CancelacionAgenciaController.java # PATCH /agencia/reservaciones/{id}/cancelar
│   ├── HotelController.java           # CRUD hoteles (admin)
│   ├── HotelAgenciaController.java    # GET /api/hoteles-agencia
│   ├── ComentarioController.java      # CRUD comentarios
│   ├── ImagenController.java          # Servicio de imágenes BLOB
│   ├── PdfReservacionController.java  # GET /pdf/reservaciones/{id}
│   ├── EmailReservacionController.java# GET /email/reservaciones/{id}
│   ├── DestinosController.java        # GET /destinos
│   ├── AgenciaController.java         # Gestión de agencias webservice
│   ├── HandshakeAerolineaController.java # POST /api/aerolineas/handshake
│   ├── TokenAerolineaController.java  # Tokens para aerolineas aliadas
│   ├── TokenValidacionController.java # POST /api/validar-token
│   ├── AdminBusquedaController.java   # Admin: búsquedas
│   ├── AerolineaAdminController.java  # Admin: aerolineas
│   └── AerolineaWebserviceController.java
│
├── services/
│   ├── AuthService.java               # login(), valida creds + bcrypt + genera JWT
│   ├── BusquedaService.java           # buscarHoteles() con disponibilidad y traslapes
│   ├── BusquedaAgenciaService.java    # buscarPorToken() con validación agencia
│   ├── ReservacionService.java        # crearReservacion(), expirarPendientes()
│   ├── ReservacionAgenciaService.java # crearReservacion() para agencias
│   ├── PagoService.java               # procesarPago(), cambia estado a CONFIRMADA
│   ├── PagoAgenciaService.java        # procesarPagoAgencia()
│   ├── CancelacionService.java        # cancelarReservacion() usuario
│   ├── HotelService.java              # CRUD hotel, habitaciones, amenidades, imágenes
│   ├── HotelAgenciaService.java       # catálogo hoteles para agencias
│   ├── UsuarioService.java            # registrar(), perfil, cambiarContraseña, cambiarRol
│   ├── AgenciaService.java            # CRUD agencias webservice
│   ├── HandshakeService.java          # procesarHandshake() agencias
│   ├── HandshakeAerolineaService.java # handshake con aerolineas aliadas
│   ├── AgenciaNotificadorExternoService.java # POST HTTP cancelación a agencia
│   ├── ExpiracionService.java         # Hilo background: expira reservaciones vencidas
│   ├── EmailReservacionService.java   # enviarConfirmacion() con javax.mail
│   ├── PdfReservacionService.java     # generarPdf() con iText 7
│   ├── ImagenService.java             # CRUD imágenes BLOB
│   ├── ComentarioService.java         # CRUD comentarios
│   ├── DestinosService.java           # catálogo destinos
│   ├── SesionService.java             # verificarSesion()
│   └── TokenValidacionService.java    # validarTokenAlianza()
│
├── repositories/
│   ├── UsuarioRepository.java         # CRUD usuario, existeUsername/Correo/Pasaporte
│   ├── AuthRepository.java            # obtenerPorIdentificador()
│   ├── HotelRepository.java           # CRUD hotel+habitaciones+amenidades+imágenes
│   ├── ReservacionRepository.java     # crearReservacion, existeTraslape, expirar
│   ├── BusquedaRepository.java        # buscarHotelesPorCiudad, tipos disponibles
│   ├── BusquedaAgenciaRepository.java # búsqueda con agenciaId
│   ├── AgenciaRepository.java         # CRUD agencias, tokens handshake
│   ├── PagoRepository.java            # confirmarEstado CONFIRMADA
│   ├── CancelacionRepository.java     # marcarCancelada con fecha/motivo
│   ├── ComentarioRepository.java      # CRUD comentarios
│   ├── ImagenRepository.java          # BLOB insert/select/delete
│   ├── DestinosRepository.java        # lista Ciudad+Pais distintos
│   ├── AerolineaAliadaRepository.java # CRUD aerolineas aliadas
│   ├── TokenAerolineaRepository.java  # tokens de aerolineas
│   ├── TokenValidacionRepository.java # tokens de alianza/descuento
│   └── [otros repositorios admin]
│
├── models/
│   └── Usuario.java                   # id, correo, contrasena, pasaporte, username,
│                                      # nombre, apellido, rolId, telefono, fechaNacimiento,
│                                      # ciudadId (todos con getters/setters)
├── dtos/                              # 30+ DTOs: LoginRequestDTO, BusquedaRequestDTO,
│   ...                                # ReservacionRequestDTO, AgenciaDTO, etc.
│
├── helpers/
│   ├── AuthMiddleware.java            # app.before(): valida JWT en cookie COOKIE_NAME
│   ├── AgenciaAuthMiddleware.java     # valida X-Agencia-Token → inyecta agenciaId en ctx
│   ├── JwtHelper.java                 # generarToken(), verificarToken(), getUsuarioId()
│   └── PasswordHelper.java            # hashear(), verificar() con jBCrypt
│
└── data/
    ├── DatabaseManager.java           # JDBC wrapper genérico
    ├── DatabaseTest.java              # Prueba conexión Oracle al iniciar
    ├── DataAccessException.java       # Excepción custom de acceso a datos
    └── ResultSetMapper.java           # Interface funcional @FunctionalInterface""")

    h2(doc, "2.2 Árbol del Frontend")
    code(doc,
"""Miku Inn/src/
├── App.svelte              # Router manual; estado global: isLoggedIn, userName,
│                           # userRolId, currentPage, alianzaToken, alianzaDescuento
├── main.js                 # Monta App.svelte
├── app.css
│
├── lib/
│   └── api.js              # VITE_API_URL || 'http://localhost:7000'
│
├── pages/
│   ├── Home.svelte         # Inicio con buscador de hoteles
│   ├── SearchResults.svelte# Resultados: hoteles con tipos disponibles y precio
│   ├── HotelDetail.svelte  # Detalle: amenidades, habitaciones, imágenes, comentarios
│   ├── Checkout.svelte     # Carrito y confirmación de pago
│   ├── Agradecimiento.svelte # Post-pago confirmado
│   ├── Login.svelte        # Login
│   ├── Register.svelte     # Registro
│   ├── MyReservations.svelte  # Mis reservaciones con estado
│   ├── Destinos.svelte     # Catálogo de destinos
│   ├── Profile.svelte      # Perfil: teléfono y contraseña
│   ├── Administrador.svelte# Dashboard admin (rol 2) — hoteles, reservas, usuarios
│   ├── WebService.svelte   # Portal webservice (rol 3) — agencias, tokens
│   ├── AccesoDenegado.svelte
│   ├── Commentnode.svelte  # Nodo de árbol de comentarios
│   └── [páginas informativas: CentroAyuda, Contactanos, FAQ, etc.]
│
├── components/
│   ├── Header.svelte
│   ├── Footer.svelte
│   └── admin/
│       ├── AdminDashboard.svelte    # Métricas y resumen admin
│       ├── AdminHoteles.svelte      # CRUD hoteles
│       ├── AdminReservas.svelte     # Gestión reservaciones
│       ├── AdminUsuarios.svelte     # Gestión usuarios
│       ├── AdminAgencias.svelte     # Gestión agencias
│       ├── AdminAerolineas.svelte   # Gestión aerolineas aliadas
│       ├── AdminCrearHotel.svelte   # Formulario creación hotel
│       └── AdminReportes.svelte     # Reportes y estadísticas
│
├── styles/                 # 1 CSS por página (home.css, searchresults.css, etc.)
└── utils/
    ├── validarFechas.js    # Validación de fechas check-in/out
    └── validarFechas.test.js # Tests unitarios Jest""")

    h2(doc, "2.3 Mapa Funcionalidad → Archivos")
    table(doc,
        ["Funcionalidad", "Frontend", "Controller", "Service", "Repository"],
        [
            ["Login", "Login.svelte", "AuthController", "AuthService", "AuthRepository"],
            ["Logout", "Header.svelte", "AuthController", "(cookie expire)", "—"],
            ["Registro", "Register.svelte", "UsuarioController", "UsuarioService", "UsuarioRepository"],
            ["Validar sesión", "App.svelte", "SesionController", "SesionService", "—"],
            ["Perfil (ver/editar)", "Profile.svelte", "UsuarioController", "UsuarioService", "UsuarioRepository"],
            ["Búsqueda hoteles", "Home.svelte → SearchResults.svelte", "BusquedaController", "BusquedaService", "BusquedaRepository"],
            ["Detalle hotel", "HotelDetail.svelte", "HotelAgenciaController", "HotelAgenciaService", "HotelRepository"],
            ["Crear reservación", "Checkout.svelte", "ReservacionController", "ReservacionService", "ReservacionRepository"],
            ["Pago usuario", "Checkout.svelte", "PagoController", "PagoService", "PagoRepository"],
            ["Mis reservaciones", "MyReservations.svelte", "ReservacionController (GET)", "ReservacionService", "ReservacionRepository"],
            ["Cancelación usuario", "MyReservations.svelte", "CancelacionController", "CancelacionService", "CancelacionRepository"],
            ["Comentarios", "HotelDetail.svelte", "ComentarioController", "ComentarioService", "ComentarioRepository"],
            ["Admin: hoteles", "AdminHoteles.svelte", "HotelController", "HotelService", "HotelRepository"],
            ["Admin: reservaciones", "AdminReservas.svelte", "HotelController (admin)", "HotelService", "HotelRepository (listarTodasReservaciones)"],
            ["Admin: usuarios", "AdminUsuarios.svelte", "UsuarioController", "UsuarioService", "UsuarioRepository"],
            ["Admin: métricas", "AdminDashboard.svelte", "HotelController (reportes)", "HotelService", "HotelRepository (obtenerMetricas)"],
            ["Handshake agencia", "WebService.svelte", "AgenciaController", "HandshakeService", "AgenciaRepository"],
            ["Búsqueda agencia", "(Agencia Movent)", "BusquedaAgenciaController", "BusquedaAgenciaService", "BusquedaRepository"],
            ["Reserva agencia", "(Agencia Movent)", "ReservacionAgenciaController", "ReservacionAgenciaService", "ReservacionRepository"],
            ["Pago agencia", "(Agencia Movent)", "PagoAgenciaController", "PagoAgenciaService", "PagoRepository"],
            ["Cancelar agencia", "(Agencia Movent)", "CancelacionAgenciaController", "CancelacionService", "CancelacionRepository"],
        ]
    )

    h2(doc, "2.4 Endpoints REST Internos")
    table(doc,
        ["Método", "Ruta", "Controller", "Descripción", "Roles"],
        [
            ["POST", "/auth/login", "AuthController", "Login; emite cookie JWT HttpOnly 8h", "Público"],
            ["POST", "/auth/logout", "AuthController", "Expira cookie JWT", "Autenticado"],
            ["GET",  "/sesion", "SesionController", "Retorna { autenticado, username, rolId }", "Público"],
            ["POST", "/usuarios/registrar", "UsuarioController", "Crear usuario con rol 1 (cliente)", "Público"],
            ["GET",  "/usuarios/validar", "UsuarioController", "Verificar duplicados username/correo/pasaporte", "Público"],
            ["POST", "/perfil", "UsuarioController", "Obtener datos del perfil del usuario autenticado", "Autenticado"],
            ["PATCH", "/perfil/telefono", "UsuarioController", "Actualizar teléfono", "Autenticado"],
            ["PATCH", "/perfil/contrasena", "UsuarioController", "Cambiar contraseña (verifica actual)", "Autenticado"],
            ["POST", "/busqueda", "BusquedaController", "Buscar hoteles por país/ciudad/fechas", "Público"],
            ["POST", "/reservaciones", "ReservacionController", "Crear reservación; expira pendientes; verifica traslapes", "Autenticado"],
            ["GET",  "/reservaciones", "ReservacionController", "Listar reservaciones del usuario autenticado", "Autenticado"],
            ["POST", "/reservaciones/{id}/pago", "PagoController", "Procesar pago → estado CONFIRMADA", "Autenticado"],
            ["POST", "/cancelaciones", "CancelacionController", "Cancelar reservación del usuario", "Autenticado"],
            ["POST", "/comentarios", "ComentarioController", "Crear comentario con calificación 1-5", "Autenticado"],
            ["GET",  "/comentarios/hotel/{id}", "ComentarioController", "Listar comentarios de un hotel", "Público"],
            ["GET",  "/destinos", "DestinosController", "Lista países y ciudades disponibles", "Público"],
            ["GET",  "/api/hoteles-agencia", "HotelAgenciaController", "Catálogo de hoteles activos para agencias", "Público"],
            ["GET",  "/imagenes/{id}", "ImagenController", "Retorna bytes de imagen BLOB", "Público"],
            ["GET",  "/pdf/reservaciones/{id}", "PdfReservacionController", "Genera PDF de reservación con iText", "Autenticado"],
            ["GET",  "/admin/hoteles", "HotelController", "Listar todos los hoteles (admin)", "Rol 2"],
            ["POST", "/admin/hoteles", "HotelController", "Crear hotel", "Rol 2"],
            ["PATCH", "/admin/hoteles/{id}", "HotelController", "Actualizar hotel", "Rol 2"],
            ["DELETE", "/admin/hoteles/{id}", "HotelController", "Eliminar hotel + cascadas manuales", "Rol 2"],
            ["POST", "/admin/hoteles/{id}/habitaciones", "HotelController", "Crear habitación", "Rol 2"],
            ["PATCH", "/admin/hoteles/{id}/habitaciones/{hid}", "HotelController", "Actualizar habitación", "Rol 2"],
            ["POST", "/admin/hoteles/{id}/amenidades", "HotelController", "Agregar amenidad a hotel", "Rol 2"],
            ["POST", "/admin/hoteles/{id}/imagenes", "HotelController", "Subir imagen (multipart)", "Rol 2"],
            ["GET",  "/admin/reportes", "HotelController", "Métricas del sistema", "Rol 2"],
            ["GET",  "/admin/reservaciones", "HotelController", "Listar todas las reservaciones", "Rol 2"],
            ["GET",  "/webservice/agencias", "AgenciaController", "Listar agencias del usuario webservice", "Rol 3"],
            ["POST", "/webservice/agencias", "AgenciaController", "Crear agencia para usuario webservice", "Rol 3"],
            ["PATCH", "/webservice/agencias/{id}/estado", "AgenciaController", "Cambiar estado agencia", "Rol 3"],
            ["POST", "/api/agencias/handshake", "AgenciaController", "Handshake con agencia externa", "Público"],
            ["POST", "/api/aerolineas/handshake", "HandshakeAerolineaController", "Handshake con aerolinea aliada", "Público"],
            ["POST", "/api/validar-token", "TokenValidacionController", "Validar token de alianza/descuento", "Público"],
        ]
    )

    h2(doc, "2.5 Rutas del Frontend")
    table(doc,
        ["Ruta", "Página", "Propósito", "Protección"],
        [
            ["/", "Home.svelte", "Inicio con buscador de hoteles", "Pública"],
            ["/search-results", "SearchResults.svelte", "Resultados de búsqueda", "Pública"],
            ["/hotel-detail", "HotelDetail.svelte", "Detalle de hotel", "Pública"],
            ["/checkout", "Checkout.svelte", "Selección y pago", "Autenticado"],
            ["/agradecimiento", "Agradecimiento.svelte", "Confirmación post-pago", "Autenticado"],
            ["/login", "Login.svelte", "Login", "Pública"],
            ["/register", "Register.svelte", "Registro", "Pública"],
            ["/reservations", "MyReservations.svelte", "Mis reservaciones", "Autenticado"],
            ["/destinations", "Destinos.svelte", "Catálogo de destinos", "Pública"],
            ["/profile", "Profile.svelte", "Perfil del usuario", "Autenticado"],
            ["/administrador", "Administrador.svelte", "Dashboard admin", "Rol 2"],
            ["/webservice", "WebService.svelte", "Portal webservice", "Rol 3"],
            ["/acceso-denegado", "AccesoDenegado.svelte", "403", "Pública"],
        ]
    )
    page_break(doc)

    # ══════════════════════════════════════════════════════
    # PARTE 3 — FLUJOS
    # ══════════════════════════════════════════════════════
    h1(doc, "Parte 3 — Flujos Internos End-to-End")

    h2(doc, "Flujo 1 — Login de Usuario")
    code(doc,
"""Paso 1:  Login.svelte → POST /auth/login
          body: { "identificador": "usuario|correo|pasaporte", "contrasena": "..." }
Paso 2:  AuthController → AuthService.login(dto)
Paso 3:  AuthRepository.obtenerPorIdentificador()
          SQL: SELECT u.ID, u.Correo, u.Contrasena, u.Username, u.Rol_ID, ...
               FROM Usuario u
               WHERE u.Username=? OR u.Correo=? OR u.Pasaporte=?
Paso 4:  PasswordHelper.verificar(dto.contrasena, usuario.contrasena)
          Si falla → throw CredencialesInvalidasException → HTTP 401
Paso 5:  JwtHelper.generarToken(id, username, rolId) → JWT firmado HS256, 8h
Paso 6:  ctx.cookie(COOKIE_NAME, token, maxAge=28800, httpOnly=true, sameSite=STRICT)
Paso 7:  HTTP 200 → { "mensaje": "Login exitoso", "username": ..., "rolId": 1|2|3 }
Paso 8:  App.svelte: isLoggedIn=true, userName=..., userRolId=...
         Redirige según rol: 2 → /administrador, 3 → /webservice, 1 → /""")

    h2(doc, "Flujo 2 — Registro de Usuario")
    code(doc,
"""Paso 1:  Register.svelte → GET /usuarios/validar?username=...&correo=...&pasaporte=...
Paso 2:  UsuarioRepository: existeUsername(), existeCorreo(), existePasaporte()
          SQL: SELECT COUNT(*) FROM Usuario WHERE Username=?
               SELECT COUNT(*) FROM Usuario WHERE Correo=?
               SELECT COUNT(*) FROM Usuario WHERE Pasaporte=?
Paso 3:  Si hay duplicado → mostrar mensaje en formulario
Paso 4:  POST /usuarios/registrar
          body: { correo, contrasena, username, nombre, apellido,
                  telefono, fechaNacimiento, ciudadId, pasaporte }
Paso 5:  UsuarioService → PasswordHelper.hashear(contrasena) con 12 rondas BCrypt
Paso 6:  UsuarioRepository.crearUsuario()
          SQL: INSERT INTO Usuario (Correo, Contrasena, Pasaporte, Username, Nombre,
                                    Apellido, Rol_ID, Telefono, Fecha_Nacimiento, Ciudad_ID)
               VALUES (?, ?, ?, ?, ?, ?, 1, ?, ?, ?)
          → executeInsertReturnId() retorna nuevo ID
Paso 7:  HTTP 200 → { "usuarioId": n, "mensaje": "Usuario registrado exitosamente" }""")

    h2(doc, "Flujo 3 — Búsqueda de Hoteles con Disponibilidad")
    code(doc,
"""Paso 1:  Home.svelte → POST /busqueda
          body: { "pais": "Guatemala", "ciudad": "Guatemala City",
                  "fechaCheckIn": "2026-06-15", "fechaCheckOut": "2026-06-18",
                  "cantidadPersonas": 2 }
Paso 2:  BusquedaController → BusquedaService.buscarHoteles(req, usuarioId)
Paso 3:  BusquedaRepository.buscarCiudadId("Guatemala City", "Guatemala")
          SQL: SELECT c.ID FROM Ciudad c JOIN Pais p ON c.Pais_ID=p.ID
               WHERE LOWER(TRIM(c.Nombre))=LOWER(TRIM(?))
                 AND LOWER(TRIM(p.Nombre))=LOWER(TRIM(?))
Paso 4:  BusquedaRepository.guardarBusqueda(ciudadId, fechaCheckIn, fechaCheckOut,
                                             cantidadPersonas, usuarioId)
          SQL: INSERT INTO Busqueda (CiudadID, FechaCheckIn, FechaCheckOut,
               CantidadPersonas, UsuarioID, AgenciaID, TipoBusquedaID, Fecha)
               VALUES (?, ?, ?, ?, ?, NULL, ?, SYSDATE)
Paso 5:  BusquedaRepository.buscarHotelesPorCiudad(ciudadId)
          SQL: SELECT h.ID, h.Nombre, h.Direccion, h.Descripcion, h.Rating, e.Estado,
                      c.Nombre AS Ciudad, p.Nombre AS Pais
               FROM Hotel h
               JOIN Estado e ON h.EstadoID=e.ID
               JOIN Ciudad c ON h.CiudadID=c.ID
               JOIN Pais   p ON c.Pais_ID=p.ID
               WHERE h.CiudadID=? AND LOWER(TRIM(e.Estado))='activo'
Paso 6:  Para cada hotel → buscarTiposHabitacionDisponibles(hotelId, capacidad, fechaIn, fechaOut)
          SQL: SELECT t.ID, t.NOMBRE, t.PRECIOPERSONA, t.PRECIONOCHE, t.CAPACIDADMAXIMA,
                      t.METROSCUADRADOS, c.TIPO_DE_CLASE AS TipoCama
               FROM TipoHabitacion t JOIN Cama c ON t.TIPOCAMAID=c.ID
               WHERE t.CAPACIDADMAXIMA >= ? AND EXISTS (
                 SELECT 1 FROM Habitacion h
                 JOIN EstadoHabitacion e ON h.ESTADO_ID=e.ID
                 WHERE h.TIPOHABITACIONID=t.ID AND h.HOTELID=?
                   AND LOWER(TRIM(e.TIPO_DE_CLASE))='activa'
                   AND h.ID NOT IN (
                     SELECT dr.HabitacionID FROM DetallesReservacion dr
                     JOIN Reservacion r ON dr.ReservacionID=r.ID
                     JOIN EstadoReserva er ON r.EstadoID=er.ID
                     WHERE LOWER(TRIM(er.Estado)) IN ('pendiente','confirmada')
                       AND dr.FechaCheckIn < ? AND dr.FechaCheckOut > ?))
Paso 7:  Para cada hotel → buscarImagenesHotel(), buscarAmenidadesHotel()
Paso 8:  HTTP 200 → { "hoteles": [{id, nombre, ciudad, rating, amenidades, tiposHabitacion}] }""")

    h2(doc, "Flujo 4 — Crear Reservación + Pago")
    code(doc,
"""--- CREAR RESERVACIÓN ---
Paso 1:  Checkout.svelte → POST /reservaciones
          body: { "habitacionesDetalles": [
            { "habitacionId": 1, "cantidadPersonas": 2,
              "fechaCheckIn": "2026-06-15", "fechaCheckOut": "2026-06-18" } ] }
Paso 2:  ReservacionController → ReservacionService.crearReservacion(dto, usuarioId)
Paso 3:  Para cada habitación: ReservacionRepository.obtenerPrecios(habitacionId)
          SQL: SELECT t.PRECIONOCHE, t.PRECIOPERSONA, t.CAPACIDADMAXIMA
               FROM Habitacion h JOIN TipoHabitacion t ON h.TIPOHABITACIONID=t.ID
               WHERE h.ID=?
Paso 4:  ReservacionRepository.existeTraslape(habitacionId, fechaIn, fechaOut)
          SQL: SELECT COUNT(*) FROM DetallesReservacion dr
               JOIN Reservacion r ON dr.ReservacionID=r.ID
               JOIN EstadoReserva er ON r.EstadoID=er.ID
               WHERE dr.HabitacionID=?
                 AND LOWER(TRIM(er.Estado)) IN ('pendiente','confirmada')
                 AND dr.FechaCheckIn < ? AND dr.FechaCheckOut > ?
          Si traslape > 0 → Error 409 "Habitación no disponible"
Paso 5:  Calcular total = PRECIONOCHE * noches + PRECIOPERSONA * personas
Paso 6:  ReservacionRepository.expirarPendientesDeUsuario(usuarioId, 0)
          SQL: UPDATE Reservacion SET EstadoID=(SELECT ID FROM EstadoReserva
               WHERE LOWER(Estado)='expirada'), Fecha_Expiracion=SYSDATE
               WHERE Usuario_ID=? AND ID!=? AND EstadoID=(SELECT ID FROM EstadoReserva
               WHERE LOWER(Estado)='pendiente')
Paso 7:  ReservacionRepository.crearReservacion(noReservacion, total, usuarioId,
                                                 fechaCreacion, fechaExpiracion=+30min)
          SQL: INSERT INTO Reservacion (No_Reservacion, Total, EstadoID, Usuario_ID,
               Fecha_Creacion, Fecha_Expiracion) VALUES (?, ?, 1, ?, ?, ?)
Paso 8:  ReservacionRepository.crearDetalle(reservacionId, habitacionId, fechaIn,
                                            fechaOut, cantidadPersonas, total)
          SQL: INSERT INTO DetallesReservacion (ReservacionID, HabitacionID, FechaCheckIn,
               FechaCheckOut, CantidadPersonas, Total) VALUES (?, ?, ?, ?, ?, ?)
Paso 9:  HTTP 200 → { "reservacionId": n, "noReservacion": "RES-...", "total": ... }

--- PAGO ---
Paso 10: POST /reservaciones/{id}/pago
          body: { "numeroTarjeta": "...", "nombreTitular": "...", "cvc": "..." }
Paso 11: PagoService.procesarPago(reservacionId, usuarioId, dto)
          SQL UPDATE: UPDATE Reservacion SET EstadoID=(SELECT ID FROM EstadoReserva
               WHERE LOWER(Estado)='confirmada') WHERE ID=?
Paso 12: HTTP 200 → { "mensaje": "Pago procesado", "estado": "confirmada" }
Paso 13: Agradecimiento.svelte muestra confirmación""")

    h2(doc, "Flujo 5 — ExpiracionService (Background)")
    code(doc,
"""El servicio se inicia en Main.java con expiracionService.iniciar()
Corre en un hilo background (Thread / ScheduledExecutorService)

ReservacionRepository.expirarReservacionesVencidas():
SQL: UPDATE Reservacion
     SET EstadoID = (SELECT ID FROM EstadoReserva
                     WHERE LOWER(Estado) = 'expirada')
     WHERE EstadoID = (SELECT ID FROM EstadoReserva
                       WHERE LOWER(Estado) = 'pendiente')
       AND Fecha_Expiracion < SYSDATE

Ejecuta periódicamente (intervalo configurado en ExpiracionService)
Libera habitaciones que quedaron bloqueadas por reservaciones no pagadas""")
    page_break(doc)

    # ══════════════════════════════════════════════════════
    # PARTE 4 — WEBSERVICE
    # ══════════════════════════════════════════════════════
    h1(doc, "Parte 4 — WebService y Flujo entre Proveedores")

    h2(doc, "4.1 Rol en la Arquitectura Distribuida")
    body(doc, "Miku Inn actúa como PROVEEDOR de servicios hoteleros. La Agencia de Viajes (Movent, Go) es el orquestador que consume los endpoints de este sistema. El flujo también es bidireccional: cuando un admin de Miku Inn cancela una reservación que provino de una agencia, Miku Inn notifica de vuelta a la Agencia.")
    code(doc,
"""
         ┌───────────────────────────────────┐
         │      Agencia Movent (Go)          │  ← Orquestador
         │      Puerto: 8080                 │
         └──────────┬────────────────────────┘
                    │  X-Agencia-Token (handshake previo)
         ┌──────────▼────────────────────────┐
         │   Hotelera Miku Inn (Java)        │  ← Proveedor (este módulo)
         │   Puerto: 7000                    │
         └──────────┬────────────────────────┘
                    │  POST cancelación (hacia Agencia)
         ┌──────────▼────────────────────────┐
         │   Agencia Movent                  │
         │   POST /api/proveedores-ext/      │
         │   detalles/{id}/cancelar          │
         └───────────────────────────────────┘
""")

    h2(doc, "4.2 Endpoints WebService EXPUESTOS por Miku Inn")
    table(doc,
        ["Ruta", "Método", "Auth", "Descripción", "Controller"],
        [
            ["/api/agencias/handshake", "POST", "Sin auth (inicial)", "Busca agencia por URL_Agencia; genera tokenSalida (BCrypt hash); persiste Token_HASH_Entrada y Token_HASH_Salida en tabla Agencia", "AgenciaController"],
            ["/agencia/busqueda", "POST", "X-Agencia-Token", "Busca hoteles disponibles para la agencia con validación de traslapes", "BusquedaAgenciaController"],
            ["/agencia/reservaciones", "POST", "X-Agencia-Token", "Crear reservación temporal (30min expiración); retorna reservacionId y noReservacion", "ReservacionAgenciaController"],
            ["/agencia/reservaciones/{id}/pago", "POST", "X-Agencia-Token", "Confirmar pago → estado CONFIRMADA; bloquea habitaciones en fechas", "PagoAgenciaController"],
            ["/agencia/reservaciones/{id}/cancelar", "PATCH", "X-Agencia-Token", "Cancelar reservación con motivo", "CancelacionAgenciaController"],
            ["/agencia/reservaciones/{id}/puede-cancelar", "GET", "X-Agencia-Token", "Verificar si reservación puede cancelarse", "CancelacionAgenciaController"],
            ["/agencia/reservaciones", "GET", "X-Agencia-Token", "Listar reservaciones de la agencia autenticada", "ReservacionAgenciaController"],
            ["/agencia/reservaciones/{id}", "GET", "X-Agencia-Token", "Detalle de reservación específica", "ReservacionAgenciaController"],
            ["/agencia/reservaciones/{id}/expirar", "POST", "X-Agencia-Token", "Expirar reservación sin pago (libera habitaciones)", "ReservacionAgenciaController"],
            ["/api/hoteles-agencia", "GET", "Sin auth", "Catálogo completo de hoteles activos con amenidades e imágenes", "HotelAgenciaController"],
        ]
    )

    h2(doc, "4.3 Validación del Token de Agencia (AgenciaAuthMiddleware)")
    code(doc,
"""// helpers/AgenciaAuthMiddleware.java
// Registrado en Main.java antes de las rutas /agencia/*

public void handle(Context ctx) {
    String token = ctx.header("X-Agencia-Token");
    if (token == null || token.isEmpty()) {
        ctx.status(401).json(Map.of("error", "Token requerido"));
        return;
    }
    // Buscar agencia por Token_HASH_Entrada en tabla Agencia
    AgenciaIdentidad agencia = agenciaRepo.obtenerAgenciaPorToken(token);
    // SQL: SELECT ID, Nombre, URL_Agencia FROM Agencia WHERE Token_HASH_Entrada=?
    if (agencia == null) {
        ctx.status(401).json(Map.of("error", "Token inválido o agencia inactiva"));
        return;
    }
    ctx.attribute("agenciaId", agencia.getId());
    ctx.attribute("agenciaNombre", agencia.getNombre());
    ctx.attribute("agenciaUrl", agencia.getUrlAgencia());
}""")

    h2(doc, "4.4 Flujo de Handshake")
    code(doc,
"""INICIADO POR: Agencia Movent (POST saliente desde HandshakeHoteleraService.java)
RECIBE EN:    Miku Inn

Paso 1: Agencia POST /api/agencias/handshake
        body: { "urlAgencia": "http://localhost:8080", "tokenEntrada": "hash64chars" }
Paso 2: AgenciaController → HandshakeService.procesarHandshake(dto)
Paso 3: AgenciaRepository.obtenerAgenciaIdPorURL(dto.urlAgencia)
        SQL: SELECT ID FROM Agencia WHERE URL_Agencia=?
        Si no existe → 404 "Agencia no registrada. Regístrela primero."
Paso 4: Generar token de salida:
        tokenSalida = JwtHelper.generarToken() o UUID aleatorio
        Alternativamente: hash BCrypt del token de entrada
Paso 5: AgenciaRepository.guardarTokens(agenciaId, dto.tokenEntrada, tokenSalida)
        SQL: UPDATE Agencia SET Token_HASH_Entrada=?, Token_HASH_Salida=? WHERE ID=?
Paso 6: HTTP 200 → { "tokenSalida": "xyz...", "mensaje": "Handshake exitoso" }

RESULTADO:
  Token_HASH_Entrada: usado por la Agencia como X-Agencia-Token en llamadas a Miku Inn
  Token_HASH_Salida:  guardado por Miku Inn para uso interno / re-handshake

CONDICIÓN DE RE-HANDSHAKE: Admin ejecuta nuevo handshake desde panel admin o
cuando Agencia recibe 401 en llamadas a Miku Inn""")

    h2(doc, "4.5 Flujos Inter-Sistemas — Perspectiva de la Hotelera")

    h3(doc, "FLUJO A — Búsqueda de Hoteles (Agencia → Hotelera)")
    code(doc,
"""[Agencia Movent] BusquedaService.llamarHoteles()
    → POST http://{hotelera_url}/agencia/busqueda
      Headers: X-Agencia-Token: {tokenEntrada guardado}
      Body: { ciudad, pais, fechaCheckIn, fechaCheckOut, cantidadPersonas }

[Hotelera Miku Inn] BusquedaAgenciaController → BusquedaAgenciaService
    → BusquedaRepository.buscarCiudadId()
    → BusquedaRepository.guardarBusqueda() con agenciaId
    → BusquedaRepository.buscarHotelesPorCiudad()
    → Para cada hotel → buscarTiposHabitacionDisponibles() [verifica traslapes]
    Tablas afectadas: Ciudad, Pais, Hotel, Habitacion, TipoHabitacion,
                      EstadoHabitacion, DetallesReservacion, Reservacion,
                      EstadoReserva, Busqueda
    → HTTP 200 → [{ hotel con tipos disponibles y precios }]

[Agencia] Aplica porcentaje de ganancia y muestra al usuario final""")

    h3(doc, "FLUJO B — Reservación Temporal (Agencia → Hotelera)")
    code(doc,
"""[Agencia] DetalleReservacionService.AgregarDetalleHotel()
    → POST {hotelera_url}/agencia/reservaciones
      Headers: X-Agencia-Token: {token}
      Body: { "habitacionesDetalles": [{habitacionId, cantidadPersonas,
              fechaCheckIn, fechaCheckOut}] }

[Hotelera] ReservacionAgenciaController → ReservacionAgenciaService
    → Verifica traslapes de fechas por habitación
    → expirarPendientesDeAgencia()
    → INSERT INTO Reservacion (No_Reservacion, Total, EstadoID=PENDIENTE,
                               Usuario_ID=[webservice], Fecha_Expiracion=+30min)
    → INSERT INTO DetallesReservacion para cada habitación
    → HTTP 200 → { reservacionId, noReservacion, estado: "pendiente", total }

[Agencia] Guarda en detalles_reservacion:
    ID_Reserva_Proveedor = reservacionId devuelto por Hotelera""")

    h3(doc, "FLUJO C — Confirmación de Pago (Agencia → Hotelera)")
    code(doc,
"""[Agencia] PagoService.Pagar() — después de recibir pago del usuario
    → POST {hotelera_url}/agencia/reservaciones/{id}/pago
      Headers: X-Agencia-Token: {token}
      Body: { "numeroTarjeta": "...", "nombreTitular": "...", "cvc": "..." }

[Hotelera] PagoAgenciaController → PagoAgenciaService
    → Verifica que reservación exista y esté PENDIENTE
    → UPDATE Reservacion SET EstadoID=(SELECT ID FROM EstadoReserva
       WHERE LOWER(Estado)='confirmada') WHERE ID=?
    → HTTP 200 → { "estado": "confirmada", "mensaje": "Pago procesado" }

[Agencia] Registra TipoOutPagoProveedorExitoso (60) en log_sesion""")

    h3(doc, "FLUJO D — Cancelación por Admin Hotelera (notifica a Agencia)")
    code(doc,
"""[Admin Hotelera] Cancela reservación desde AdminReservas.svelte
    → HotelController → HotelService → (lógica cancelación)
    → UPDATE Reservacion SET EstadoID=CANCELADA, Fecha_Cancelacion=SYSDATE,
             Motivo_Cancelacion=? WHERE ID=?
    Detecta si reservación pertenece a agencia (Usuario con Rol_ID=3)
    Si sí → AgenciaNotificadorExternoService.notificarCancelacionAAgencia()
              POST {agencia.URL_Agencia}/api/proveedores-ext/detalles/{id}/cancelar
              Body: { reservacionId, noReservacion, estado: "cancelada",
                      motivoCancelacion, fechaCancelacion }
              java.net.HttpURLConnection POST HTTP

[Agencia Movent] CancelacionProveedorController recibe el POST:
    Middleware valida X-Agencia-Token
    Marca detalle como Cancelado, actualiza Reservacion a Retenida/Cancelada
    Registra notificación para el usuario final
    Registra evento CANCELACION_PROVEEDOR (30) en log_sesion""")

    h3(doc, "FLUJO E — Catálogo de Hoteles (Agencia → Hotelera, sincronización)")
    code(doc,
"""[Agencia] CatalogoService.ActualizarCatalogo() — manual o scheduler semanal
    → GET {hotelera_url}/api/hoteles-agencia
      (sin auth — endpoint público)

[Hotelera] HotelAgenciaController → HotelAgenciaService
    SQL: SELECT h.ID, h.Nombre, h.Direccion, h.Rating, c.Nombre AS Ciudad,
                p.Nombre AS Pais
         FROM Hotel h JOIN Estado e ON h.EstadoID=e.ID
         JOIN Ciudad c ON h.CiudadID=c.ID JOIN Pais p ON c.Pais_ID=p.ID
         WHERE LOWER(TRIM(e.Estado))='activo'
    Para cada hotel → amenidades, imágenes, tipos de habitación
    → HTTP 200 → [{ hotel completo con detalles }]

[Agencia] Guarda en tabla Catalogo_Proveedor:
    → DELETE FROM Catalogo_Proveedor WHERE Proveedor_ID=?
    → INSERT INTO Catalogo_Proveedor (Proveedor_ID, Tipo_Detalle_ID=2, Ciudad_Destino_ID, ...)""")

    h2(doc, "4.6 Tabla de Tokens y Credenciales")
    table(doc,
        ["Token / Credencial", "Tabla", "Columna", "Generado por", "Usado en"],
        [
            ["Token Entrada (de Agencia)", "Agencia", "Token_HASH_Entrada", "Agencia Movent (SHA-256)", "X-Agencia-Token en llamadas de Agencia a Miku Inn"],
            ["Token Salida (de Hotelera)", "Agencia", "Token_HASH_Salida", "Miku Inn (BCrypt/JWT)", "Guardado internamente en Hotelera"],
            ["Cookie JWT Usuario", "(en memoria Javalin)", "Cookie COOKIE_NAME", "Miku Inn (JJWT HS256)", "Identificar usuarios web; 8h HttpOnly STRICT"],
            ["JWT Secret", "env var JWT_SECRET", "(sensible)", "Admin configura en .env", "Firma JWT; default: 'Sabrina_es_la_Best_Bruja_Bonita!'"],
            ["Mail Password", "env var MAIL_PASSWORD", "(sensible)", "Gmail App Password", "Envío de correos SMTP"],
        ]
    )

    h2(doc, "4.7 Semántica de IDs Cruzados")
    body(doc, "Cuando la Agencia crea una reservación temporal en Miku Inn, Miku Inn devuelve su Reservacion.ID (un NUMBER de Oracle). La Agencia almacena ese ID en su tabla detalles_reservacion.ID_Reserva_Proveedor (VARCHAR). En llamadas posteriores (pago, cancelación), la Agencia envía ese ID en la URL del endpoint (/agencia/reservaciones/{id}/pago). Miku Inn usa ese ID para localizar la reservación en su propia BD Oracle.")

    h2(doc, "4.8 Manejo de Errores en Comunicación")
    body(doc, "Si POST de cancelación a Agencia falla: la cancelación local en Miku Inn ya se realizó. AgenciaNotificadorExternoService captura la excepción y la registra en consola (System.out.println). No hay reintentos automáticos. Si X-Agencia-Token es inválido: HTTP 401. Si la reservación tiene traslape de fechas: HTTP 409. Si habitación no existe: HTTP 404.")
    page_break(doc)

    # ══════════════════════════════════════════════════════
    # PARTE 5 — QUERIES SQL
    # ══════════════════════════════════════════════════════
    h1(doc, "Parte 5 — Queries SQL del Backend")
    body(doc, "Base de datos: Oracle Database. Driver: ojdbc11 23.3.0.23.09. Parámetros con ? (JDBC). Fechas con SYSDATE. Los queries usan LOWER(TRIM()) para comparaciones robustas de estados.")

    h2(doc, "5.1 Autenticación")
    code(doc,
"""-- AuthRepository.java
-- Endpoint: POST /auth/login → AuthController → AuthService

SELECT u.ID, u.Correo, u.Contrasena, u.Pasaporte, u.Username,
       u.Nombre, u.Apellido, u.Rol_ID, u.Telefono,
       u.Fecha_Nacimiento, u.Ciudad_ID
FROM Usuario u
WHERE u.Username = ? OR u.Correo = ? OR u.Pasaporte = ?""")

    h2(doc, "5.2 Registro de Usuario")
    code(doc,
"""-- UsuarioRepository.java
-- Endpoint: POST /usuarios/registrar

-- Verificar duplicados
SELECT COUNT(*) FROM Usuario WHERE Username  = ?
SELECT COUNT(*) FROM Usuario WHERE Correo    = ?
SELECT COUNT(*) FROM Usuario WHERE Pasaporte = ?

-- Crear usuario (rol 1 = Cliente por defecto)
INSERT INTO Usuario (Correo, Contrasena, Pasaporte, Username, Nombre,
                     Apellido, Rol_ID, Telefono, Fecha_Nacimiento, Ciudad_ID)
VALUES (?, ?, ?, ?, ?, ?, 1, ?, ?, ?)""")

    h2(doc, "5.3 Perfil de Usuario")
    code(doc,
"""-- UsuarioRepository.java
-- Endpoint: POST /perfil → UsuarioController → UsuarioService

SELECT u.ID, u.Username, u.Correo, u.Pasaporte, u.Nombre, u.Apellido,
       u.Telefono, u.Fecha_Nacimiento, u.Rol_ID,
       c.Nombre AS Ciudad, p.Nombre AS Pais
FROM Usuario u
LEFT JOIN Ciudad c ON u.Ciudad_ID = c.ID
LEFT JOIN Pais   p ON c.Pais_ID   = p.ID
WHERE u.ID = ?

-- Nacionalidades del usuario
SELECT n.Nombre FROM UsuarioNacionalidad un
JOIN Nacionalidad n ON un.Nacionalidad_ID = n.ID
WHERE un.Usuario_ID = ?

-- Actualizar teléfono
UPDATE Usuario SET Telefono = ? WHERE ID = ?

-- Obtener contraseña para verificar (cambio de contraseña)
SELECT Contrasena FROM Usuario WHERE ID = ?

-- Actualizar contraseña (nuevo hash BCrypt)
UPDATE Usuario SET Contrasena = ? WHERE ID = ?""")

    h2(doc, "5.4 Búsqueda de Hoteles")
    code(doc,
"""-- BusquedaRepository.java
-- Endpoint: POST /busqueda

-- Buscar ciudad por nombre
SELECT c.ID FROM Ciudad c JOIN Pais p ON c.Pais_ID = p.ID
WHERE LOWER(TRIM(c.Nombre)) = LOWER(TRIM(?))
  AND LOWER(TRIM(p.Nombre)) = LOWER(TRIM(?))

-- Registrar búsqueda
INSERT INTO Busqueda (CiudadID, FechaCheckIn, FechaCheckOut, CantidadPersonas,
                      UsuarioID, AgenciaID, TipoBusquedaID, Fecha)
VALUES (?, ?, ?, ?, ?, NULL, ?, SYSDATE)

-- Hoteles activos en ciudad
SELECT h.ID, h.Nombre, h.Direccion, h.Descripcion, h.Rating, e.Estado,
       c.Nombre AS Ciudad, p.Nombre AS Pais
FROM Hotel h
JOIN Estado e ON h.EstadoID = e.ID
JOIN Ciudad c ON h.CiudadID = c.ID
JOIN Pais   p ON c.Pais_ID  = p.ID
WHERE h.CiudadID = ? AND LOWER(TRIM(e.Estado)) = 'activo'

-- Amenidades del hotel
SELECT ha.ID AS HotelAmenidadId, ha.AmenidadID, ha.Descripcion,
       a.nombre AS NombreAmenidad
FROM HotelAmenidad ha JOIN Amenidad a ON ha.AmenidadID = a.ID
WHERE ha.HotelID = ?

-- Tipos de habitación disponibles (sin traslape de fechas)
SELECT t.ID AS TipoID, t.NOMBRE AS TipoHabitacion, t.PRECIOPERSONA,
       t.PRECIONOCHE, t.CAPACIDADMAXIMA, t.METROSCUADRADOS,
       c.TIPO_DE_CLASE AS TipoCama
FROM TipoHabitacion t JOIN Cama c ON t.TIPOCAMAID = c.ID
WHERE t.CAPACIDADMAXIMA >= ?
  AND EXISTS (
    SELECT 1 FROM Habitacion h
    JOIN EstadoHabitacion e ON h.ESTADO_ID = e.ID
    WHERE h.TIPOHABITACIONID = t.ID AND h.HOTELID = ?
      AND LOWER(TRIM(e.TIPO_DE_CLASE)) = 'activa'
      AND h.ID NOT IN (
        SELECT dr.HabitacionID FROM DetallesReservacion dr
        JOIN Reservacion  r  ON dr.ReservacionID = r.ID
        JOIN EstadoReserva er ON r.EstadoID      = er.ID
        WHERE LOWER(TRIM(er.Estado)) IN ('pendiente', 'confirmada')
          AND dr.FechaCheckIn  < ?
          AND dr.FechaCheckOut > ?
      )
  )

-- Habitaciones disponibles de un tipo (para asignación)
SELECT h.ID, h.NUMEROHABITACION FROM Habitacion h
JOIN EstadoHabitacion e ON h.ESTADO_ID = e.ID
WHERE h.HOTELID = ? AND h.TIPOHABITACIONID = ?
  AND LOWER(TRIM(e.TIPO_DE_CLASE)) = 'activa'
  AND h.ID NOT IN (
    SELECT dr.HabitacionID FROM DetallesReservacion dr
    JOIN Reservacion  r  ON dr.ReservacionID = r.ID
    JOIN EstadoReserva er ON r.EstadoID      = er.ID
    WHERE LOWER(TRIM(er.Estado)) IN ('pendiente', 'confirmada')
      AND dr.FechaCheckIn < ? AND dr.FechaCheckOut > ?
  )""")

    h2(doc, "5.5 Reservación")
    code(doc,
"""-- ReservacionRepository.java
-- Endpoint: POST /reservaciones

-- Obtener precios de habitación
SELECT t.PRECIONOCHE, t.PRECIOPERSONA, t.CAPACIDADMAXIMA
FROM Habitacion h JOIN TipoHabitacion t ON h.TIPOHABITACIONID = t.ID
WHERE h.ID = ?

-- Verificar traslape de fechas
SELECT COUNT(*) AS total FROM DetallesReservacion dr
JOIN Reservacion  r  ON dr.ReservacionID = r.ID
JOIN EstadoReserva er ON r.EstadoID      = er.ID
WHERE dr.HabitacionID = ?
  AND LOWER(TRIM(er.Estado)) IN ('pendiente', 'confirmada')
  AND dr.FechaCheckIn  < ?
  AND dr.FechaCheckOut > ?

-- Expirar reservaciones pendientes del usuario
UPDATE Reservacion
SET EstadoID         = (SELECT ID FROM EstadoReserva WHERE LOWER(Estado)='expirada'),
    Fecha_Expiracion = SYSDATE
WHERE Usuario_ID = ? AND ID != ?
  AND EstadoID = (SELECT ID FROM EstadoReserva WHERE LOWER(Estado)='pendiente')

-- Crear reservación
INSERT INTO Reservacion (No_Reservacion, Total, EstadoID, Usuario_ID,
                         Fecha_Creacion, Fecha_Expiracion)
VALUES (?, ?, 1, ?, ?, ?)

-- Crear detalle de habitación
INSERT INTO DetallesReservacion (ReservacionID, HabitacionID, FechaCheckIn,
                                  FechaCheckOut, CantidadPersonas, Total)
VALUES (?, ?, ?, ?, ?, ?)

-- Listar reservaciones del usuario con JOIN completo
SELECT r.ID, r.No_Reservacion, r.Total, r.Fecha_Creacion, r.Fecha_Expiracion,
       r.Fecha_Cancelacion, r.Motivo_Cancelacion, er.Estado,
       dr.ID AS DetalleID, dr.HabitacionID, dr.FechaCheckIn, dr.FechaCheckOut,
       dr.CantidadPersonas, dr.Total AS TotalDetalle,
       h.Descripcion AS DescripcionHabitacion, h.NUMEROHABITACION,
       t.NOMBRE AS TipoHabitacion, c.TIPO_DE_CLASE AS TipoCama,
       hot.ID AS HotelID, hot.Nombre AS NombreHotel
FROM Reservacion r
JOIN EstadoReserva er ON r.EstadoID     = er.ID
JOIN DetallesReservacion dr ON dr.ReservacionID = r.ID
JOIN Habitacion    h  ON dr.HabitacionID = h.ID
JOIN TipoHabitacion t ON h.TIPOHABITACIONID = t.ID
JOIN Cama          c  ON t.TIPOCAMAID   = c.ID
JOIN Hotel        hot ON h.HOTELID      = hot.ID
WHERE r.Usuario_ID = ?
ORDER BY r.Fecha_Creacion DESC, r.ID, dr.ID

-- Expirar reservaciones vencidas (ExpiracionService)
UPDATE Reservacion
SET EstadoID = (SELECT ID FROM EstadoReserva WHERE LOWER(Estado)='expirada')
WHERE EstadoID = (SELECT ID FROM EstadoReserva WHERE LOWER(Estado)='pendiente')
  AND Fecha_Expiracion < SYSDATE""")

    h2(doc, "5.6 Pago")
    code(doc,
"""-- PagoRepository.java / PagoAgenciaRepository.java
-- Endpoint: POST /reservaciones/{id}/pago

-- Confirmar pago → estado CONFIRMADA
UPDATE Reservacion
SET EstadoID = (SELECT ID FROM EstadoReserva WHERE LOWER(TRIM(Estado))='confirmada')
WHERE ID = ?""")

    h2(doc, "5.7 Cancelación")
    code(doc,
"""-- CancelacionRepository.java
-- Endpoint: POST /cancelaciones

-- Marcar como cancelada
UPDATE Reservacion
SET EstadoID          = (SELECT ID FROM EstadoReserva WHERE LOWER(Estado)='cancelada'),
    Fecha_Cancelacion = SYSDATE,
    Motivo_Cancelacion = ?
WHERE ID = ? AND Usuario_ID = ?""")

    h2(doc, "5.8 Gestión de Hoteles (Admin)")
    code(doc,
"""-- HotelRepository.java

-- Listar amenidades disponibles
SELECT ID, Nombre FROM Amenidad ORDER BY ID

-- Listar todos los hoteles con JOIN
SELECT h.ID, h.Nombre, h.Direccion, h.Descripcion, h.Rating, h.EstadoID, e.Estado,
       c.Nombre AS Ciudad, p.Nombre AS Pais
FROM Hotel h
JOIN Estado e ON h.EstadoID = e.ID
JOIN Ciudad c ON h.CiudadID = c.ID
JOIN Pais   p ON c.Pais_ID  = p.ID
ORDER BY h.ID

-- Crear hotel
INSERT INTO Hotel (Nombre, Direccion, Descripcion, Rating, EstadoID, CiudadID)
VALUES (?, ?, ?, ?, ?, ?)

-- Actualizar hotel
UPDATE Hotel SET Nombre=?, Direccion=?, Descripcion=?, Rating=?, EstadoID=?
WHERE ID=?

-- Eliminar hotel (cascadas manuales)
DELETE FROM ImagenHabitacion WHERE HabitacionID IN
    (SELECT ID FROM Habitacion WHERE HotelID=?)
DELETE FROM Habitacion WHERE HotelID=?
DELETE FROM ImagenHotelAmenidad WHERE HotelAmenidadID IN
    (SELECT ID FROM HotelAmenidad WHERE HotelID=?)
DELETE FROM HotelAmenidad WHERE HotelID=?
DELETE FROM ImagenHotel WHERE HotelID=?
DELETE FROM Hotel WHERE ID=?

-- Listar habitaciones del hotel
SELECT h.ID, h.HotelID, h.TIPOHABITACIONID, t.NOMBRE AS TipoHabitacion,
       h.NUMEROHABITACION, t.TIPOCAMAID AS CamaID,
       c.TIPO_DE_CLASE AS TipoCama, t.PRECIOPERSONA AS Precio_por_Persona,
       t.PRECIONOCHE AS Precio_por_Noche, t.CAPACIDADMAXIMA, t.METROSCUADRADOS,
       h.Descripcion, h.ESTADO_ID, e.TIPO_DE_CLASE AS Estado
FROM Habitacion h
JOIN TipoHabitacion   t ON h.TIPOHABITACIONID = t.ID
JOIN Cama             c ON t.TIPOCAMAID       = c.ID
JOIN EstadoHabitacion e ON h.ESTADO_ID        = e.ID
WHERE h.HotelID = ?  ORDER BY h.ID

-- Crear habitación
INSERT INTO Habitacion (HotelID, TIPOHABITACIONID, NUMEROHABITACION,
                        Descripcion, ESTADO_ID)
VALUES (?, ?, ?, ?, ?)

-- Agregar amenidad a hotel (verificar antes que no exista)
SELECT COUNT(*) FROM HotelAmenidad WHERE HotelID=? AND AmenidadID=?
INSERT INTO HotelAmenidad (HotelID, AmenidadID, Descripcion) VALUES (?, ?, ?)

-- Agregar imagen (BLOB)
INSERT INTO ImagenHotel (HotelID, Imagen) VALUES (?, ?)

-- Obtener imagen (BLOB)
SELECT ID FROM ImagenHotel WHERE HotelID=? ORDER BY ID""")

    h2(doc, "5.9 Agencias — CRUD y Tokens")
    code(doc,
"""-- AgenciaRepository.java

-- Listar agencias de un usuario webservice
SELECT a.ID, a.Nombre, a.Correo, a.UsuarioWebis_ID, a.PorcentajeDescuento,
       a.EstadoID, e.Estado, a.URL_Agencia
FROM Agencia a JOIN EstadoAgencia e ON a.EstadoID=e.ID
WHERE a.UsuarioWebis_ID = ?  ORDER BY a.ID

-- Verificar que usuario no tenga ya agencia ni aerolinea
SELECT COUNT(*) AS C FROM Agencia WHERE UsuarioWebis_ID=?
SELECT COUNT(*) AS C FROM AerolineaAliado WHERE UsuarioWebis=?

-- Crear agencia (webservice)
INSERT INTO Agencia (Nombre, Correo, UsuarioWebis_ID, PorcentajeDescuento,
                     EstadoID, URL_Agencia)
VALUES (?, ?, ?, 0, 1, ?)

-- Guardar tokens del handshake
UPDATE Agencia SET Token_HASH_Entrada=?, Token_HASH_Salida=? WHERE ID=?

-- Obtener agencia por token (AgenciaAuthMiddleware)
SELECT ID, Nombre, URL_Agencia FROM Agencia WHERE Token_HASH_Entrada=?

-- Obtener ID de agencia por URL (HandshakeService)
SELECT ID FROM Agencia WHERE URL_Agencia=?

-- Cambiar estado
UPDATE Agencia SET EstadoID=? WHERE ID=?

-- Actualizar agencia (admin)
UPDATE Agencia SET Nombre=?, Correo=?, URL_Agencia=?,
                   PorcentajeDescuento=?, EstadoID=? WHERE ID=?""")

    h2(doc, "5.10 Métricas del Sistema (Admin)")
    code(doc,
"""-- HotelRepository.java — obtenerMetricas()
-- Endpoint: GET /admin/reportes

SELECT COUNT(*) AS total FROM Usuario

SELECT COUNT(*) AS total FROM Hotel h
JOIN Estado e ON h.EstadoID = e.ID
WHERE LOWER(e.Estado) = 'activo'

SELECT COUNT(*) AS total FROM Reservacion r
JOIN EstadoReserva er ON r.EstadoID = er.ID
WHERE LOWER(TRIM(er.Estado)) = 'confirmada'

SELECT COUNT(*) AS total FROM Reservacion

SELECT NVL(SUM(r.Total), 0) AS total FROM Reservacion r
JOIN EstadoReserva er ON r.EstadoID = er.ID
WHERE LOWER(TRIM(er.Estado)) = 'confirmada'

SELECT LOWER(TRIM(er.Estado)) AS estado, COUNT(*) AS total
FROM Reservacion r JOIN EstadoReserva er ON r.EstadoID = er.ID
GROUP BY LOWER(TRIM(er.Estado))

-- Listar todas las reservaciones (admin)
SELECT r.ID, r.No_Reservacion, r.Total, r.Fecha_Creacion, r.Fecha_Expiracion,
       er.Estado, u.Nombre AS UsuarioNombre, u.Apellido AS UsuarioApellido,
       u.Username, hot.Nombre AS HotelNombre,
       MIN(dr.FechaCheckIn) AS CheckIn, MAX(dr.FechaCheckOut) AS CheckOut
FROM Reservacion r
JOIN EstadoReserva er ON r.EstadoID    = er.ID
JOIN Usuario       u  ON r.Usuario_ID  = u.ID
JOIN DetallesReservacion dr ON dr.ReservacionID = r.ID
JOIN Habitacion    h  ON dr.HabitacionID = h.ID
JOIN Hotel        hot ON h.HotelID     = hot.ID
GROUP BY r.ID, r.No_Reservacion, r.Total, r.Fecha_Creacion, r.Fecha_Expiracion,
         er.Estado, u.Nombre, u.Apellido, u.Username, hot.Nombre
ORDER BY r.Fecha_Creacion DESC""")

    h2(doc, "5.11 Esquema SQL (Miku_Inn.sql — tablas base)")
    body(doc, "El archivo Miku_Inn.sql contiene DDL de tablas base del sistema. Las tablas de dominio principales se infieren del código de repositorios.")
    code(doc,
"""-- Tablas presentes en Miku_Inn.sql:
CREATE TABLE PAIS (
  ID     NUMBER PRIMARY KEY,
  Nombre VARCHAR2(100)
);

CREATE TABLE CIUDAD (
  ID     NUMBER PRIMARY KEY,
  Nombre VARCHAR2(100),
  Pais_ID NUMBER REFERENCES Pais(ID)
);

CREATE TABLE ROL (
  ID       NUMBER PRIMARY KEY,
  RolNombre VARCHAR2(50)  -- 1=Cliente, 2=Admin, 3=Webservice
);

CREATE TABLE USUARIO (
  ID               NUMBER PRIMARY KEY,
  Correo           VARCHAR2(100) UNIQUE,
  Contrasena       VARCHAR2(255),        -- BCrypt hash
  Pasaporte        VARCHAR2(50) UNIQUE,
  Username         VARCHAR2(100) UNIQUE,
  Nombre           VARCHAR2(100),
  Apellido         VARCHAR2(100),
  Rol_ID           NUMBER REFERENCES Rol(ID),
  Telefono         VARCHAR2(20),
  Fecha_Nacimiento DATE,
  Ciudad_ID        NUMBER REFERENCES Ciudad(ID)
);

CREATE TABLE NACIONALIDAD (
  ID     NUMBER PRIMARY KEY,
  Nombre VARCHAR2(100)
);

-- Tablas de negocio (inferidas del código):
CREATE TABLE Estado (
  ID     NUMBER PRIMARY KEY,
  Estado VARCHAR2(50)  -- 'activo', 'inactivo'
);

CREATE TABLE EstadoReserva (
  ID     NUMBER PRIMARY KEY,
  Estado VARCHAR2(50)  -- 'pendiente', 'confirmada', 'cancelada', 'expirada'
);

CREATE TABLE EstadoAgencia (
  ID     NUMBER PRIMARY KEY,
  Estado VARCHAR2(50)  -- 'Activa', 'Inactiva'
);

CREATE TABLE EstadoHabitacion (
  ID             NUMBER PRIMARY KEY,
  TIPO_DE_CLASE  VARCHAR2(50)  -- 'activa', 'inactiva'
);

CREATE TABLE Cama (
  ID             NUMBER PRIMARY KEY,
  TIPO_DE_CLASE  VARCHAR2(50)  -- 'individual', 'doble', 'queen', 'king'
);

CREATE TABLE Hotel (
  ID          NUMBER PRIMARY KEY,
  Nombre      VARCHAR2(255) NOT NULL,
  Direccion   VARCHAR2(255),
  Descripcion CLOB,
  Rating      FLOAT,
  EstadoID    NUMBER REFERENCES Estado(ID),
  CiudadID    NUMBER REFERENCES Ciudad(ID)
);

CREATE TABLE Amenidad (
  ID     NUMBER PRIMARY KEY,
  Nombre VARCHAR2(100) NOT NULL
);

CREATE TABLE HotelAmenidad (
  ID          NUMBER PRIMARY KEY,
  HotelID     NUMBER REFERENCES Hotel(ID),
  AmenidadID  NUMBER REFERENCES Amenidad(ID),
  Descripcion VARCHAR2(255)
);

CREATE TABLE TipoHabitacion (
  ID               NUMBER PRIMARY KEY,
  NOMBRE           VARCHAR2(100),
  TIPOCAMAID       NUMBER REFERENCES Cama(ID),
  PRECIOPERSONA    FLOAT,
  PRECIONOCHE      FLOAT,
  CAPACIDADMAXIMA  NUMBER,
  METROSCUADRADOS  FLOAT
);

CREATE TABLE Habitacion (
  ID                NUMBER PRIMARY KEY,
  HotelID           NUMBER REFERENCES Hotel(ID),
  TIPOHABITACIONID  NUMBER REFERENCES TipoHabitacion(ID),
  NUMEROHABITACION  VARCHAR2(10),
  Descripcion       VARCHAR2(255),
  ESTADO_ID         NUMBER REFERENCES EstadoHabitacion(ID)
);

CREATE TABLE ImagenHotel (
  ID      NUMBER PRIMARY KEY,
  HotelID NUMBER REFERENCES Hotel(ID),
  Imagen  BLOB
);

CREATE TABLE ImagenHabitacion (
  ID           NUMBER PRIMARY KEY,
  HabitacionID NUMBER REFERENCES Habitacion(ID),
  Imagen       BLOB
);

CREATE TABLE ImagenHotelAmenidad (
  ID              NUMBER PRIMARY KEY,
  HotelAmenidadID NUMBER REFERENCES HotelAmenidad(ID),
  Imagen          BLOB
);

CREATE TABLE Agencia (
  ID                  NUMBER PRIMARY KEY,
  Nombre              VARCHAR2(255) NOT NULL,
  Correo              VARCHAR2(100),
  UsuarioWebis_ID     NUMBER REFERENCES Usuario(ID),
  PorcentajeDescuento FLOAT,
  EstadoID            NUMBER REFERENCES EstadoAgencia(ID),
  URL_Agencia         VARCHAR2(255) UNIQUE,
  Token_HASH_Entrada  VARCHAR2(255),
  Token_HASH_Salida   VARCHAR2(255)
);

CREATE TABLE Reservacion (
  ID                 NUMBER PRIMARY KEY,
  No_Reservacion     VARCHAR2(50) UNIQUE NOT NULL,
  Total              FLOAT,
  EstadoID           NUMBER REFERENCES EstadoReserva(ID),
  Usuario_ID         NUMBER REFERENCES Usuario(ID),
  Fecha_Creacion     TIMESTAMP,
  Fecha_Expiracion   TIMESTAMP,
  Fecha_Cancelacion  DATE,
  Motivo_Cancelacion VARCHAR2(255)
);

CREATE TABLE DetallesReservacion (
  ID               NUMBER PRIMARY KEY,
  ReservacionID    NUMBER REFERENCES Reservacion(ID),
  HabitacionID     NUMBER REFERENCES Habitacion(ID),
  FechaCheckIn     DATE,
  FechaCheckOut    DATE,
  CantidadPersonas NUMBER,
  Total            FLOAT
);

CREATE TABLE Busqueda (
  ID               NUMBER PRIMARY KEY,
  CiudadID         NUMBER REFERENCES Ciudad(ID),
  FechaCheckIn     DATE,
  FechaCheckOut    DATE,
  CantidadPersonas NUMBER,
  UsuarioID        NUMBER REFERENCES Usuario(ID),
  AgenciaID        NUMBER REFERENCES Agencia(ID),
  TipoBusquedaID   NUMBER,
  Fecha            TIMESTAMP
);

CREATE TABLE Comentario (
  ID           NUMBER PRIMARY KEY,
  HotelID      NUMBER REFERENCES Hotel(ID),
  Usuario_ID   NUMBER REFERENCES Usuario(ID),
  Calificacion NUMBER,
  Descripcion  CLOB,
  Fecha        TIMESTAMP
);

CREATE TABLE UsuarioNacionalidad (
  Usuario_ID      NUMBER REFERENCES Usuario(ID),
  Nacionalidad_ID NUMBER REFERENCES Nacionalidad(ID),
  PRIMARY KEY (Usuario_ID, Nacionalidad_ID)
);

CREATE TABLE AerolineaAliado (
  ID                 NUMBER PRIMARY KEY,
  Nombre             VARCHAR2(255),
  URL_Aerolinea      VARCHAR2(255),
  UsuarioWebis       NUMBER REFERENCES Usuario(ID),
  Token_HASH_Entrada VARCHAR2(255),
  Token_HASH_Salida  VARCHAR2(255)
);

CREATE TABLE TokenValidacion (
  ID                  NUMBER PRIMARY KEY,
  Token               VARCHAR2(255) UNIQUE,
  Ciudad              VARCHAR2(100),
  PorcentajeDescuento NUMBER,
  FechaCreacion       TIMESTAMP,
  FechaExpiracion     TIMESTAMP
);""")
    page_break(doc)

    # ══════════════════════════════════════════════════════
    # ANEXO — MÉTRICAS
    # ══════════════════════════════════════════════════════
    h1(doc, "Anexo — Queries SQL de Métricas para Defensa Oral")
    body(doc, "Todos los queries siguientes son para Oracle Database. Ejecutar desde SQL*Plus, SQL Developer o similar.")

    metricas = [
        ("A.1 Total de usuarios registrados",
         "SELECT COUNT(*) AS TotalUsuarios FROM Usuario"),
        ("A.2 Usuarios por rol",
         """SELECT r.RolNombre AS Rol, COUNT(u.ID) AS Total
FROM Usuario u LEFT JOIN Rol r ON u.Rol_ID = r.ID
GROUP BY r.RolNombre ORDER BY Total DESC"""),
        ("A.3 Total de reservaciones por estado",
         """SELECT er.Estado, COUNT(r.ID) AS Total
FROM Reservacion r
LEFT JOIN EstadoReserva er ON r.EstadoID = er.ID
GROUP BY er.Estado ORDER BY Total DESC"""),
        ("A.4 Reservaciones por mes (últimos 12 meses)",
         """SELECT TO_CHAR(r.Fecha_Creacion,'YYYY-MM') AS Mes,
       COUNT(*) AS Total, SUM(r.Total) AS Ingresos
FROM Reservacion r
WHERE r.Fecha_Creacion >= ADD_MONTHS(SYSDATE,-12)
GROUP BY TO_CHAR(r.Fecha_Creacion,'YYYY-MM')
ORDER BY Mes"""),
        ("A.5 Ingresos totales (reservaciones confirmadas)",
         """SELECT NVL(SUM(r.Total),0) AS IngresosTotales
FROM Reservacion r JOIN EstadoReserva er ON r.EstadoID=er.ID
WHERE LOWER(TRIM(er.Estado))='confirmada'"""),
        ("A.6 Ingresos por mes",
         """SELECT TO_CHAR(r.Fecha_Creacion,'YYYY-MM') AS Mes, SUM(r.Total) AS Ingresos
FROM Reservacion r JOIN EstadoReserva er ON r.EstadoID=er.ID
WHERE LOWER(TRIM(er.Estado))='confirmada'
GROUP BY TO_CHAR(r.Fecha_Creacion,'YYYY-MM')
ORDER BY Mes"""),
        ("A.7 Top 10 hoteles más reservados",
         """SELECT hot.Nombre AS Hotel, COUNT(r.ID) AS TotalReservaciones,
       SUM(r.Total) AS IngresoTotal
FROM Reservacion r
JOIN EstadoReserva er ON r.EstadoID=er.ID
JOIN DetallesReservacion dr ON dr.ReservacionID=r.ID
JOIN Habitacion h ON dr.HabitacionID=h.ID
JOIN Hotel hot ON h.HotelID=hot.ID
WHERE LOWER(TRIM(er.Estado)) IN ('confirmada','pendiente')
GROUP BY hot.Nombre ORDER BY TotalReservaciones DESC
FETCH FIRST 10 ROWS ONLY"""),
        ("A.8 Top 10 usuarios con más reservaciones",
         """SELECT u.Nombre||' '||u.Apellido AS Usuario, u.Correo, COUNT(r.ID) AS Total
FROM Reservacion r JOIN Usuario u ON r.Usuario_ID=u.ID
GROUP BY u.Nombre, u.Apellido, u.Correo
ORDER BY Total DESC
FETCH FIRST 10 ROWS ONLY"""),
        ("A.9 Tasa de cancelación",
         """SELECT
  COUNT(CASE WHEN LOWER(TRIM(er.Estado))='cancelada' THEN 1 END) AS Canceladas,
  COUNT(*) AS Total,
  ROUND(100 * COUNT(CASE WHEN LOWER(TRIM(er.Estado))='cancelada' THEN 1 END)
        / NULLIF(COUNT(*),0), 2) AS TasaCancelacion_Pct
FROM Reservacion r JOIN EstadoReserva er ON r.EstadoID=er.ID"""),
        ("A.10 Habitaciones más reservadas",
         """SELECT hot.Nombre AS Hotel, t.NOMBRE AS TipoHabitacion,
       h.NUMEROHABITACION, COUNT(dr.ID) AS VecesReservada
FROM DetallesReservacion dr
JOIN Habitacion h ON dr.HabitacionID=h.ID
JOIN TipoHabitacion t ON h.TIPOHABITACIONID=t.ID
JOIN Hotel hot ON h.HotelID=hot.ID
GROUP BY hot.Nombre, t.NOMBRE, h.NUMEROHABITACION
ORDER BY VecesReservada DESC
FETCH FIRST 10 ROWS ONLY"""),
        ("A.11 Agencias más activas",
         """SELECT a.Nombre AS Agencia, a.URL_Agencia, COUNT(r.ID) AS Reservaciones
FROM Agencia a
JOIN Usuario u ON a.UsuarioWebis_ID=u.ID
JOIN Reservacion r ON r.Usuario_ID=u.ID
GROUP BY a.Nombre, a.URL_Agencia
ORDER BY Reservaciones DESC
FETCH FIRST 10 ROWS ONLY"""),
        ("A.12 Destinos más buscados",
         """SELECT c.Nombre AS Ciudad, p.Nombre AS Pais, COUNT(b.ID) AS Busquedas
FROM Busqueda b JOIN Ciudad c ON b.CiudadID=c.ID JOIN Pais p ON c.Pais_ID=p.ID
GROUP BY c.Nombre, p.Nombre ORDER BY Busquedas DESC
FETCH FIRST 10 ROWS ONLY"""),
        ("A.13 Tiempo promedio entre creación y pago",
         """SELECT ROUND(AVG(
  (r.Fecha_Expiracion - r.Fecha_Creacion) * 24 * 60
), 2) AS PromMinutosHastaConfirmacion
FROM Reservacion r JOIN EstadoReserva er ON r.EstadoID=er.ID
WHERE LOWER(TRIM(er.Estado))='confirmada'"""),
        ("A.14 Hoteles con mejor calificación promedio",
         """SELECT hot.Nombre, ROUND(AVG(c.Calificacion),2) AS CalificacionPromedio,
       COUNT(c.ID) AS TotalComentarios
FROM Comentario c JOIN Hotel hot ON c.HotelID=hot.ID
GROUP BY hot.Nombre HAVING COUNT(c.ID)>=3
ORDER BY CalificacionPromedio DESC"""),
    ]
    for titulo, sql in metricas:
        h3(doc, titulo)
        code(doc, sql)
    page_break(doc)

    # ══════════════════════════════════════════════════════
    # INVENTARIO
    # ══════════════════════════════════════════════════════
    h1(doc, "Inventario de Cobertura")
    table(doc,
        ["Carpeta / Archivo", "Estado", "Notas"],
        [
            ["Hotel/src/main/java/org/example/Main.java", "Documentado", "Punto de entrada, registro de rutas Javalin, inicio de servicios background"],
            ["Hotel/src/main/java/org/example/controllers/ (25+ archivos)", "Documentado", "Todos los endpoints REST e inter-sistemas cubiertos"],
            ["Hotel/src/main/java/org/example/services/ (20+ archivos)", "Documentado", "Lógica de negocio, ExpiracionService, AgenciaNotificadorExternoService"],
            ["Hotel/src/main/java/org/example/repositories/ (25+ archivos)", "Documentado", "Todos los queries SQL Oracle extraídos literalmente"],
            ["Hotel/src/main/java/org/example/models/Usuario.java", "Documentado", "Única entidad POJO explícita; resto inferido de repositorios"],
            ["Hotel/src/main/java/org/example/dtos/ (30+ DTOs)", "Documentado", "DTOs principales listados en sección de arquitectura"],
            ["Hotel/src/main/java/org/example/helpers/", "Documentado", "AuthMiddleware, AgenciaAuthMiddleware, JwtHelper, PasswordHelper"],
            ["Hotel/src/main/java/org/example/data/", "Documentado", "DatabaseManager, ResultSetMapper, DataAccessException, DatabaseTest"],
            ["Hotel/src/main/java/org/example/config/ServerConfig.java", "Documentado", "CORS, puerto PORT, orígenes frontend"],
            ["Hotel/pom.xml", "Documentado", "Todas las dependencias con versiones exactas"],
            ["Miku_Inn.sql (raíz del proyecto)", "Documentado", "DDL de tablas base + tablas de dominio inferidas del código"],
            ["Miku Inn/src/ (frontend Svelte)", "Documentado", "Todas las páginas, componentes admin, stores, rutas"],
            ["Miku Inn/src/utils/validarFechas.js", "Documentado", "Validación de fechas; tests en validarFechas.test.js (Jest)"],
            ["Miku Inn/package.json", "Documentado", "Svelte 5.43.8, Vite 7.3.1, svelte-routing 2.13.0, Jest 30.3.0"],
            ["Hotel/target/", "No relevante", "Artefactos de compilación Maven"],
            ["Hotel/Javadoc/", "No relevante", "Documentación Javadoc autogenerada"],
            ["hotel-docker/", "Documentado parcialmente", "Configuración Docker; Dockerfile en Miku Inn referenciado"],
            ["Hotel/src/test/", "Mencionado", "Tests JUnit 5 + Mockito 5.11.0; no detallados en este manual"],
        ]
    )

    doc.save(OUT)
    print(f"[OK] Guardado: {OUT}")

if __name__ == '__main__':
    build()
