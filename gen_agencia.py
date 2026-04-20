#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Genera Manual_Programador_Agencia.docx — Módulo Agencia de Viajes (Movent)."""

from doc_utils import (
    new_doc, set_footer, add_cover, page_break,
    h1, h2, h3, body, bullet, code, table
)

def build():
    doc = new_doc()
    set_footer(doc)
    add_cover(doc, "Módulo Agencia de Viajes\nMovent (Go + Vue + MySQL)")

    # ══════════════════════════════════════════════════════
    # PARTE 0 — STACK TECNOLÓGICO
    # ══════════════════════════════════════════════════════
    h1(doc, "Parte 0 — Stack Tecnológico")

    h2(doc, "0.1 Lenguaje Backend")
    table(doc, ["Componente", "Versión", "Rol"], [
        ["Go (Golang)", "1.26.1", "Lenguaje principal del backend REST"],
        ["Gin-Gonic", "v1.12.0", "Framework HTTP para routing y middleware"],
        ["godotenv", "v1.5.1", "Carga de variables de entorno desde .env"],
    ])

    h2(doc, "0.2 Base de Datos")
    table(doc, ["Componente", "Versión", "Rol"], [
        ["MySQL", "8.x", "Base de datos relacional principal"],
        ["go-sql-driver/mysql", "v1.9.3", "Driver JDBC-equivalente para Go"],
    ])

    h2(doc, "0.3 Seguridad y Autenticación")
    table(doc, ["Componente", "Versión", "Rol"], [
        ["golang-jwt/jwt/v5", "v5.3.1", "Generación y validación de tokens JWT HS256"],
        ["golang.org/x/crypto", "v0.49.0", "Hash de contraseñas bcrypt"],
        ["JWT_SECRET", "—", "Secreto HS256 (obligatorio en .env, app falla sin él)"],
    ])

    h2(doc, "0.4 Generación de Documentos")
    table(doc, ["Componente", "Versión", "Rol"], [
        ["jung-kurt/gofpdf", "v1.16.2", "Generación de PDF de confirmación de reservación"],
    ])

    h2(doc, "0.5 Utilidades Generales")
    table(doc, ["Componente", "Versión", "Rol"], [
        ["google/uuid", "v1.6.0", "Generación de UUIDs para números de reservación"],
        ["gin-contrib/cors", "v1.7.7", "Middleware CORS para comunicación con frontend"],
    ])

    h2(doc, "0.6 Frontend")
    table(doc, ["Componente", "Versión", "Rol"], [
        ["Vue 3", "3.5.25", "Framework SPA principal"],
        ["Vue Router", "5.0.3", "Gestión de rutas SPA (30 rutas definidas)"],
        ["Vite", "7.3.1", "Bundler y dev server"],
        ["sessionStorage / localStorage", "—", "Persistencia de estado de UI y JWT del usuario"],
    ])

    h2(doc, "0.7 Arquitectura de Servicios en Background")
    table(doc, ["Servicio", "Frecuencia", "Propósito"], [
        ["CatalogoSchedulerService", "Cada 7 días", "Sincronización automática de catálogos de proveedores"],
        ["ExpiracionService", "Cada 1 minuto", "Expiración automática de reservaciones pendientes"],
    ])
    page_break(doc)

    # ══════════════════════════════════════════════════════
    # PARTE 1 — LIBRERÍAS Y DEPENDENCIAS
    # ══════════════════════════════════════════════════════
    h1(doc, "Parte 1 — Librerías y Dependencias")

    h2(doc, "1.1 go.mod — Dependencias Directas")
    code(doc, """module agencia

go 1.26.1

require (
    github.com/gin-contrib/cors      v1.7.7
    github.com/gin-gonic/gin         v1.12.0
    github.com/go-sql-driver/mysql   v1.9.3
    github.com/golang-jwt/jwt/v5     v5.3.1
    github.com/google/uuid           v1.6.0
    github.com/joho/godotenv         v1.5.1
    github.com/jung-kurt/gofpdf      v1.16.2
    golang.org/x/crypto              v0.49.0
)""")

    h2(doc, "1.2 Gin-Gonic — Framework HTTP")
    body(doc, "Gin-Gonic es el framework HTTP principal. Provee routing declarativo con grupos, middleware encadenado, binding JSON automático con validación de structs, y context injection para datos de autenticación. El servidor HTTP se inicializa en main.go y delega el enrutado a internal/router/.")
    body(doc, "Características clave utilizadas:")
    bullet(doc, "Grupos de rutas (/api, /admin, /api/proveedores-ext) para separar áreas públicas, autenticadas y de callbacks de proveedores.")
    bullet(doc, "c.ShouldBindJSON() para parseo y validación de payloads entrantes.")
    bullet(doc, "c.Set() / c.Get() para paso de datos entre middlewares y controllers.")
    bullet(doc, "c.AbortWithStatusJSON() para corte inmediato del pipeline en errores de autenticación.")

    h2(doc, "1.3 golang-jwt/jwt/v5 — Autenticación JWT")
    body(doc, "Librería: github.com/golang-jwt/jwt/v5 v5.3.1. Algoritmo: HS256. El token se almacena en una cookie HTTP llamada 'session'. El secreto se lee de JWT_SECRET en .env; si está vacío la aplicación falla al iniciar (log.Fatal).")
    code(doc, """// helpers/TokenHelper.go
func GenerarToken(usuarioID int, username string, rolID int) (string, error) {
    claims := jwt.MapClaims{
        "usuario_id": usuarioID,
        "username":   username,
        "rol_id":     rolID,
        "exp":        time.Now().Add(24 * time.Hour).Unix(),
    }
    token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
    return token.SignedString([]byte(os.Getenv("JWT_SECRET")))
}

func VerificarToken(tokenStr string) (*CustomClaims, error) {
    token, err := jwt.ParseWithClaims(tokenStr, &CustomClaims{},
        func(t *jwt.Token) (interface{}, error) {
            return []byte(os.Getenv("JWT_SECRET")), nil
        })
    ...
}""")

    h2(doc, "1.4 golang.org/x/crypto — BCrypt")
    body(doc, "Paquete: golang.org/x/crypto v0.49.0, subpaquete bcrypt. Utilizado en LoginService y UsuarioService para hash y comparación de contraseñas.")
    code(doc, """// Hashear nueva contraseña
hash, err := bcrypt.GenerateFromPassword([]byte(password), bcrypt.DefaultCost)

// Verificar contraseña en login
err := bcrypt.CompareHashAndPassword([]byte(storedHash), []byte(inputPassword))""")

    h2(doc, "1.5 gofpdf — Generación de PDF")
    body(doc, "Librería: github.com/jung-kurt/gofpdf v1.16.2. Genera confirmaciones de reservación en formato A4 con diseño de marca MOVENT (colores oscuro #1C1A18, amarillo #FFCC00, crema #F5F2EC). El PDF incluye encabezado con número de reservación, badge de estado, detalles de boletos de vuelo y/o habitaciones de hotel, y pie de página corporativo.")
    body(doc, "Función principal: GenerarPDFReservacion(data ReservacionPDFData) []byte — invocada por ReservacionController.DescargarPDF y por EmailReservacionService para adjuntar el PDF al correo de confirmación.")
    body(doc, "Nota de codificación: gofpdf usa Latin-1 internamente. La función helper e(string) convierte caracteres UTF-8 con tilde a secuencias Latin-1 (\xe1, \xf1, etc.) antes de agregarlos al PDF.")

    h2(doc, "1.6 go-sql-driver/mysql — Acceso a MySQL")
    body(doc, "Driver: github.com/go-sql-driver/mysql v1.9.3. Usado a través de database/sql (stdlib de Go). El pool de conexiones se configura en internal/config/config.go con el DSN: usuario:contraseña@tcp(host:puerto)/nombre_bd?parseTime=true. Todas las queries usan placeholders ? para prevenir inyección SQL.")
    page_break(doc)

    # ══════════════════════════════════════════════════════
    # PARTE 2 — ARQUITECTURA DEL SISTEMA
    # ══════════════════════════════════════════════════════
    h1(doc, "Parte 2 — Arquitectura del Sistema")

    h2(doc, "2.1 Rol del Módulo en el Sistema Tri-Módulo")
    body(doc, "Movent actúa como el ORQUESTADOR central del ecosistema. Es el único módulo con el que el usuario final interactúa directamente. No almacena inventario propio (vuelos, habitaciones) — únicamente coordina la reservación distribuida entre los proveedores de Aerolínea (Broom AirLine) y Hotelera (Miku Inn).")
    table(doc, ["Módulo", "Rol", "Tecnología"], [
        ["Movent (Agencia)", "ORQUESTADOR — recibe peticiones de usuarios, coordina proveedores", "Go + Gin + MySQL"],
        ["Broom AirLine (Aerolínea)", "PROVEEDOR — expone vuelos, asientos, reservaciones", "ASP.NET Core 8 + SQL Server"],
        ["Miku Inn (Hotelera)", "PROVEEDOR — expone hoteles, habitaciones, reservaciones", "Java 17 + Javalin + Oracle"],
    ])

    h2(doc, "2.2 Árbol de Directorios del Backend")
    code(doc, """Agencia/
├── main.go                          # Punto de entrada, wiring de dependencias
├── go.mod / go.sum
├── .env                             # Variables de entorno (no commitear)
└── internal/
    ├── config/
    │   └── config.go                # Carga godotenv, inicializa DB pool
    ├── router/
    │   └── router.go                # Definición de todos los grupos de rutas
    ├── middlewares/
    │   ├── auth.go                  # JWT cookie validation → usuario_id, rol_id
    │   ├── ProveedorAuthRequerido.go # X-Agencia-Token → proveedor_id, proveedor_tipo
    │   └── RolRequerido.go          # RBAC: require rol_id=2 para rutas admin
    ├── controllers/                 # 23 controllers HTTP
    ├── services/                    # 15+ servicios de lógica de negocio
    │   ├── CatalogoSchedulerService.go  # Goroutine 7-day ticker
    │   └── ExpiracionService.go         # Goroutine 1-min ticker
    ├── repositories/                # 17+ accesos a MySQL
    ├── helpers/
    │   ├── TokenHelper.go           # JWT generate/verify
    │   └── PdfHelper.go             # gofpdf PDF generation
    ├── models/                      # Structs de dominio y DTOs
    └── constants/
        └── ConstantesLog.go         # 61 tipos de eventos de auditoría""")

    h2(doc, "2.3 Árbol de Directorios del Frontend")
    code(doc, """Movent/
├── index.html
├── vite.config.js
├── package.json
└── src/
    ├── main.js                      # Monta app Vue, registra router
    ├── router/
    │   └── index.js                 # 30 rutas, guards de autenticación JWT
    ├── views/                       # Páginas (una por ruta)
    ├── components/                  # Componentes reutilizables
    ├── stores/                      # Estado reactivo (Pinia o composition)
    └── assets/                      # Imágenes, íconos, estilos globales""")

    h2(doc, "2.4 Tabla Completa de Endpoints API")
    table(doc, ["Método", "Ruta", "Middleware", "Descripción"], [
        # Auth
        ["POST", "/api/auth/login", "—", "Login; genera JWT en cookie 'session'"],
        ["POST", "/api/auth/logout", "AuthRequerido", "Invalida sesión"],
        ["GET",  "/api/auth/sesion", "AuthRequerido", "Retorna datos del usuario actual"],
        ["POST", "/api/auth/registro", "—", "Registro de nuevo usuario"],
        # Búsqueda
        ["POST", "/api/busqueda/vuelos", "AuthRequerido", "Busca vuelos en proveedores activos"],
        ["POST", "/api/busqueda/hoteles", "AuthRequerido", "Busca hoteles en proveedores activos"],
        # Reservaciones
        ["POST", "/api/reservaciones", "AuthRequerido", "Crea reservación pendiente"],
        ["POST", "/api/reservaciones/detalle/vuelo", "AuthRequerido", "Agrega detalle de vuelo (llama Aerolínea)"],
        ["POST", "/api/reservaciones/detalle/hotel", "AuthRequerido", "Agrega detalle de hotel (llama Hotelera)"],
        ["POST", "/api/reservaciones/detalle/pasajeros-vuelo", "AuthRequerido", "Envía pasajeros a Aerolínea"],
        ["GET",  "/api/reservaciones/asientos-vuelo", "AuthRequerido", "Obtiene mapa de asientos de Aerolínea"],
        ["PUT",  "/api/reservaciones/asientos-vuelo", "AuthRequerido", "Cambia asiento en Aerolínea"],
        ["POST", "/api/reservaciones/pagar", "AuthRequerido", "Confirma pago y notifica proveedores"],
        ["GET",  "/api/reservaciones/pdf/:id", "AuthRequerido", "Descarga PDF de confirmación"],
        ["POST", "/api/reservaciones/email/:id", "AuthRequerido", "Envía PDF por correo"],
        # Mis reservaciones
        ["GET",  "/api/mis-reservaciones", "AuthRequerido", "Lista reservaciones del usuario"],
        ["GET",  "/api/mis-reservaciones/:id", "AuthRequerido", "Detalle de una reservación"],
        # Cancelación usuario
        ["GET",  "/api/reservaciones/:id/cancelar/verificar", "AuthRequerido", "Verifica si cancelación es posible"],
        ["POST", "/api/reservaciones/:id/cancelar", "AuthRequerido", "Cancela reservación"],
        # Perfil
        ["GET",  "/api/perfil", "AuthRequerido", "Obtiene perfil del usuario"],
        ["PUT",  "/api/perfil/telefono", "AuthRequerido", "Actualiza teléfono"],
        ["PUT",  "/api/perfil/contrasena", "AuthRequerido", "Cambia contraseña"],
        # Notificaciones
        ["GET",  "/api/notificaciones", "AuthRequerido", "Lista notificaciones del usuario"],
        ["PUT",  "/api/notificaciones/:id/leer", "AuthRequerido", "Marca notificación como leída"],
        # Comentarios
        ["GET",  "/api/comentarios/vuelo/:id", "—", "Comentarios de un vuelo"],
        ["GET",  "/api/comentarios/hotel/:id", "—", "Comentarios de un hotel"],
        # Configuración
        ["GET",  "/api/configuracion/descuento", "AuthRequerido", "Obtiene porcentaje de descuento vigente"],
        # Contacto
        ["POST", "/api/contacto", "—", "Envía formulario de contacto"],
        # Admin (requiere rol=2)
        ["GET",  "/api/admin/usuarios", "AuthRequerido + RolRequerido(2)", "Lista usuarios"],
        ["PUT",  "/api/admin/usuarios/:id/rol", "AuthRequerido + RolRequerido(2)", "Cambia rol de usuario"],
        ["GET",  "/api/admin/proveedores", "AuthRequerido + RolRequerido(2)", "Lista proveedores"],
        ["POST", "/api/admin/proveedores", "AuthRequerido + RolRequerido(2)", "Crea proveedor"],
        ["PUT",  "/api/admin/proveedores/:id", "AuthRequerido + RolRequerido(2)", "Edita proveedor"],
        ["PATCH","/api/admin/proveedores/:id/estado", "AuthRequerido + RolRequerido(2)", "Activa/desactiva proveedor"],
        ["POST", "/api/admin/proveedores/:id/handshake", "AuthRequerido + RolRequerido(2)", "Handshake con Aerolínea"],
        ["POST", "/api/admin/proveedores/:id/handshake-hotelera", "AuthRequerido + RolRequerido(2)", "Handshake con Hotelera"],
        ["POST", "/api/admin/catalogo/actualizar", "AuthRequerido + RolRequerido(2)", "Sincronización manual de catálogos"],
        ["GET",  "/api/admin/reservaciones/recientes", "AuthRequerido + RolRequerido(2)", "Reservaciones recientes (dashboard)"],
        ["GET",  "/api/admin/metricas", "AuthRequerido + RolRequerido(2)", "Métricas del sistema"],
        ["GET",  "/api/stats", "AuthRequerido + RolRequerido(2)", "Estadísticas generales"],
        # Callbacks de proveedores
        ["POST", "/api/proveedores-ext/detalles/:idReservaProveedor/cancelar", "ProveedorAuthRequerido", "Proveedor notifica cancelación a Agencia"],
        ["POST", "/api/proveedores-ext/detalles/:idReservaProveedor/actualizar", "ProveedorAuthRequerido", "Proveedor notifica actualización a Agencia"],
    ])

    h2(doc, "2.5 Rutas del Frontend (Vue Router)")
    table(doc, ["Ruta", "Componente / Vista", "Requiere Auth"], [
        ["/", "→ redirige a /principal", "No"],
        ["/principal", "PrincipalView", "No"],
        ["/ingreso", "IngresoView", "No"],
        ["/registro", "RegistroView", "No"],
        ["/acceso-denegado", "AccesoDenegadoView", "No"],
        ["/informacion", "InformacionView", "No"],
        ["/sobre-movent", "SobreMoventView", "No"],
        ["/centro-ayuda", "CentroAyudaView", "No"],
        ["/preguntas-frecuentes", "FaqView", "No"],
        ["/privacidad", "PrivacidadView", "No"],
        ["/terminos", "TerminosView", "No"],
        ["/contacto", "ContactoView", "No"],
        ["/cancelacion", "CancelacionInfoView", "No"],
        ["/resultados-vuelos", "ResultadosVuelosView", "No"],
        ["/resultados-hoteles", "ResultadosHotelesView", "No"],
        ["/resultados-paquetes", "ResultadosPaquetesView", "No"],
        ["/reservar", "ReservarView", "JWT"],
        ["/seleccion-asientos", "SeleccionAsientosView", "JWT"],
        ["/checkout", "CheckoutView", "JWT"],
        ["/confirmacion", "ConfirmacionView", "JWT"],
        ["/mis-reservaciones", "MisReservacionesView", "JWT"],
        ["/perfil", "PerfilView", "JWT"],
        ["/notificaciones", "NotificacionesView", "JWT"],
        ["/admin", "→ redirige a /admin/dashboard", "JWT + rol=2"],
        ["/admin/dashboard", "AdminDashboardView", "JWT + rol=2"],
        ["/admin/roles", "AdminRolesView", "JWT + rol=2"],
        ["/admin/proveedores", "AdminProveedoresView", "JWT + rol=2"],
        ["/admin/paquetes", "AdminPaquetesView", "JWT + rol=2"],
        ["/admin/webservice", "AdminWebserviceView", "JWT + rol=2"],
        ["/:pathMatch(.*)*", "→ redirige a /principal", "No"],
    ])
    page_break(doc)

    # ══════════════════════════════════════════════════════
    # PARTE 3 — FLUJOS INTERNOS DEL SISTEMA
    # ══════════════════════════════════════════════════════
    h1(doc, "Parte 3 — Flujos Internos del Sistema")

    h2(doc, "3.1 Flujo de Autenticación (Login)")
    body(doc, "El usuario envía POST /api/auth/login con username y password. LoginService obtiene el hash almacenado de LoginRepository y compara con bcrypt.CompareHashAndPassword. Si es válido, TokenHelper.GenerarToken crea un JWT HS256 con claims (usuario_id, username, rol_id, exp=24h). El token se escribe como cookie HTTP 'session'. LogSesionService registra TipoLoginExitoso (1) o el tipo de fallo correspondiente (2-4, 11, 20-22).")

    h2(doc, "3.2 Middlewares de Autenticación")
    h3(doc, "auth.go — AuthRequerido")
    body(doc, "Lee la cookie 'session'. Si ausente: HTTP 401 'No autorizado'. Verifica JWT con VerificarToken(). Si inválido/expirado: HTTP 401 'Sesión inválida o expirada'. Si válido, inyecta en contexto Gin: usuario_id (int), username (string), rol_id (int).")

    h3(doc, "ProveedorAuthRequerido.go")
    body(doc, "Lee header X-Agencia-Token. Consulta proveedor por Token_HASH_Salida en tabla proveedor. Si no existe: HTTP 401. Si EstadoID != 1: HTTP 401 'Proveedor inactivo'. Si válido, inyecta: proveedor_id, proveedor_tipo (1=aerolínea, 2=hotel), proveedor_nombre.")

    h3(doc, "RolRequerido.go")
    body(doc, "Recibe lista de roles permitidos. Lee rol_id del contexto (inyectado por auth.go). Si el rol no está en la lista: HTTP 403 'no tienes permiso para realizar esta acción'. Úsado con RolRequerido(2) en todas las rutas /admin.")

    h2(doc, "3.3 Creación y Expiración de Reservaciones")
    body(doc, "Al crear una reservación (POST /api/reservaciones), ReservacionService genera un No_Reservacion de 8 caracteres alfanuméricos únicos usando google/uuid. La reservación se inserta con EstadoID=1 (Pendiente) y Fecha_Expiracion=NOW()+30 minutos.")
    body(doc, "ExpiracionService verifica cada minuto con una goroutine. Al encontrar reservaciones pendientes vencidas, notifica a cada proveedor involucrado y actualiza EstadoID=4 (Expirada) en la base de datos.")

    h2(doc, "3.4 CatalogoSchedulerService — Sincronización Automática")
    body(doc, "Servicio en background que ejecuta ActualizarCatalogo() cada 7 días.")
    code(doc, """func (s *CatalogoSchedulerService) Iniciar() {
    go func() {
        ticker := time.NewTicker(7 * 24 * time.Hour)
        defer ticker.Stop()
        for {
            select {
            case <-ticker.C:
                n, err := s.service.ActualizarCatalogo()
                if err != nil {
                    log.Printf("[CATALOGO] Error en actualización automática: %v", err)
                } else {
                    log.Printf("[CATALOGO] Actualización automática completada: %d proveedores", n)
                }
            case <-s.stopCh:
                log.Println("[CATALOGO] Scheduler detenido")
                return
            }
        }
    }()
}""")
    body(doc, "Por cada proveedor activo, llama GET {url}/api/catalogos/rutas (Aerolínea) o GET {url}/agencia/catalogos (Hotelera) con X-Agencia-Token. Los resultados se insertan en Catalogo_Proveedor, creando ciudades si no existen.")

    h2(doc, "3.5 ExpiracionService — Expiración Automática")
    code(doc, """func (s *ExpiracionService) Iniciar() {
    go func() {
        ticker := time.NewTicker(1 * time.Minute)
        defer ticker.Stop()
        for {
            select {
            case <-ticker.C:
                s.expirarPendientes()
                log.Println("[EXPIRACION] Revisión completada")
            case <-s.stopCh:
                log.Println("[EXPIRACION] Servicio detenido")
                return
            }
        }
    }()
}""")
    body(doc, "expirarPendientes() consulta reservaciones con EstadoID=1 y Fecha_Expiracion<=NOW(). Por cada una: llama el endpoint /expirar del proveedor correspondiente, actualiza Estado_Detalle_ID=3 en Detalles_Reservacion y EstadoID=4 en Reservacion. Registra TipoReservaExpirada (28) en log_sesion.")

    h2(doc, "3.6 Generación de PDF con gofpdf")
    body(doc, "GenerarPDFReservacion() en helpers/PdfHelper.go crea un PDF A4 (210×297 mm) con márgenes: top 42mm, left/right 13mm. Las secciones son:")
    bullet(doc, "Encabezado fijo: fondo oscuro (#1C1A18), título MOVENT en amarillo (#FFCC00), número de reservación, badge de estado con color semántico (verde=confirmada, rojo=cancelada, naranja=pendiente).")
    bullet(doc, "Banda de resumen: fondo crema (#F5F2EC), tipo de reservación, fecha, titular.")
    bullet(doc, "Cuerpo en dos columnas: datos de reservación (izquierda), boletos/habitaciones (derecha).")
    bullet(doc, "Barra de total: fondo oscuro con monto destacado.")
    bullet(doc, "Pie de página: datos de contacto corporativos.")
    body(doc, "Problema de encoding: gofpdf usa Latin-1. La función e(s string) reemplaza caracteres UTF-8 con tildes por sus equivalentes Latin-1 (\xe1→á, \xf1→ñ, etc.).")
    page_break(doc)

    # ══════════════════════════════════════════════════════
    # PARTE 4 — FLUJOS WEBSERVICE (INTER-SISTEMA)
    # ══════════════════════════════════════════════════════
    h1(doc, "Parte 4 — Flujos WebService Inter-Sistema")
    body(doc, "Movent es el ORQUESTADOR. Inicia todas las llamadas salientes. Los proveedores (Aerolínea y Hotelera) pueden también notificar a Movent de forma asíncrona usando el Token_HASH_Salida establecido durante el handshake.")

    h2(doc, "4.1 Handshake — Establecimiento de Tokens de Autenticación")
    body(doc, "Flujo ejecutado por el administrador una vez por proveedor. Establece el par de tokens criptográficos que autentican todas las llamadas futuras.")
    body(doc, "Handshake con Aerolínea:")
    code(doc, """// 1. Admin: POST /api/admin/proveedores/{id}/handshake
// 2. HandshakeService genera tokenEntrada (SHA-256, 64 hex chars)
// 3. Movent llama:
POST {aerolinea_url}/api/agencias/handshake
Headers: { "Content-Type": "application/json" }
Body: {
    "token_entrada": "a3f7...64hex",
    "url_agencia":   "https://movent.example.com"
}
// 4. Aerolínea responde:
{ "token_salida": "b9c2...64hex" }
// 5. Movent persiste ambos tokens en tabla proveedor:
UPDATE proveedor
SET Token_HASH_Entrada = ?, Token_HASH_Salida = ?
WHERE ID = ?""")
    body(doc, "Handshake con Hotelera: flujo idéntico vía POST /api/admin/proveedores/{id}/handshake-hotelera. Llama POST {hotelera_url}/agencia/handshake con el mismo body. Persiste tokens en la misma tabla.")
    body(doc, "Semántica de tokens: Token_HASH_Entrada = token que Movent usa en el header X-Agencia-Token al llamar al proveedor. Token_HASH_Salida = token que el proveedor usa cuando llama de regreso a Movent.")
    table(doc, ["Token", "Quién lo usa", "Dónde se valida"], [
        ["Token_HASH_Entrada", "Movent → outbound calls a proveedor", "Proveedor valida en AgenciaAuthMiddleware"],
        ["Token_HASH_Salida", "Proveedor → callbacks a Movent", "Movent valida en ProveedorAuthRequerido.go"],
    ])

    h2(doc, "4.2 Sincronización de Catálogos — Movent llama a proveedores")
    body(doc, "Triggereado por: CatalogoSchedulerService (automático c/7 días) o POST /api/admin/catalogo/actualizar (manual).")
    code(doc, """// Para cada proveedor activo con tipo_proveedor_id=1 (Aerolínea):
GET {aerolinea_url}/api/catalogos/rutas
Headers: { "X-Agencia-Token": "{Token_HASH_Entrada}" }
// Respuesta esperada:
[
  {
    "origen": {"ciudad": "Guatemala", "pais": "Guatemala"},
    "destino": {"ciudad": "Miami",     "pais": "Estados Unidos"},
    ...
  }
]
// Movent inserta en Catalogo_Proveedor y crea ciudades si no existen.

// Para proveedor con tipo_proveedor_id=2 (Hotelera):
GET {hotelera_url}/agencia/catalogos
Headers: { "X-Agencia-Token": "{Token_HASH_Entrada}" }
// Respuesta: lista de hoteles por ciudad""")
    body(doc, "Registra TipoCatalogoActualizadoExitoso (37) o TipoCatalogoActualizadoFallido (38) por cada proveedor procesado.")

    h2(doc, "4.3 Búsqueda — Movent consulta proveedores en paralelo")
    body(doc, "Búsqueda de vuelos: POST /api/busqueda/vuelos. BusquedaService obtiene la lista de aerolíneas activas para la ruta origen→destino de Catalogo_Proveedor. Llama a todas en concurrente con timeout de 10 segundos cada una.")
    code(doc, """// Para cada aerolínea activa en la ruta:
POST {aerolinea_url}/api/vuelos-agencia/buscar
Headers: { "X-Agencia-Token": "{Token_HASH_Entrada}" }
Body: {
    "ciudadOrigen":  "Guatemala",
    "paisOrigen":    "Guatemala",
    "ciudadDestino": "Miami",
    "paisDestino":   "Estados Unidos",
    "fechaSalida":   "2026-06-15",
    "claseVuelo":    "Economica",
    "cantidadAdultos": 2,
    "cantidadNinos":   0
}
// Respuesta: array de vuelos con precios
// Movent aplica markup: precio_final = precio_proveedor * Porcentaje_Ganancia""")
    body(doc, "Registra TipoOutBusquedaVuelosExitosa (44), TipoOutBusquedaVuelosSinResultados (45), o TipoOutBusquedaVuelosFallida (46) por cada aerolínea contactada.")
    body(doc, "Búsqueda de hoteles: flujo análogo. Llama POST {hotelera_url}/agencia/hoteles/buscar. Registra eventos 47-49.")

    h2(doc, "4.4 Reservación Temporal — Movent reserva en proveedores")
    body(doc, "Paso 1 — Agregar detalle de vuelo (POST /api/reservaciones/detalle/vuelo):")
    code(doc, """// Movent llama a Aerolínea para crear reservación temporal:
POST {aerolinea_url}/api/reservaciones-agencia
Headers: { "X-Agencia-Token": "{Token_HASH_Entrada}" }
Body: {
    "vueloID":           123,
    "cantidadPasajeros": 2
}
// Aerolínea responde con ID de la reservación del proveedor:
{ "reservacionID": 456, ... }
// Movent guarda ID_Reserva_Proveedor = 456 en Detalles_Reservacion""")
    body(doc, "Registra TipoOutReservaVueloProveedorExitosa (50) o TipoOutReservaVueloProveedorFallida (51).")
    body(doc, "Paso 2 — Enviar pasajeros (POST /api/reservaciones/detalle/pasajeros-vuelo):")
    code(doc, """POST {aerolinea_url}/api/reservaciones-agencia/{ID_Reserva_Proveedor}/pasajeros
Headers: { "X-Agencia-Token": "{Token_HASH_Entrada}" }
Body: {
    "pasajeros": [
        {"nombre": "...", "apellido": "...", "pasaporte": "...", "fechaNacimiento": "..."},
        ...
    ]
}""")
    body(doc, "Registra TipoOutPasajerosProveedorExitosa (54) o Fallida (55).")
    body(doc, "Reservación de hotel (POST /api/reservaciones/detalle/hotel): llama POST {hotelera_url}/agencia/reservaciones con IDs de habitación y fechas. Registra eventos 52-53.")

    h2(doc, "4.5 Selección de Asientos — Solo para vuelos")
    code(doc, """// Consultar mapa de asientos disponibles:
GET {aerolinea_url}/api/reservaciones-agencia/{ID_Reserva_Proveedor}/asientos
Headers: { "X-Agencia-Token": "{Token_HASH_Entrada}" }
// Respuesta: lista de asientos con estado (disponible/ocupado)

// Cambiar asiento seleccionado:
PUT {aerolinea_url}/api/reservaciones-agencia/{ID_Reserva_Proveedor}/asientos/{numeroAsiento}
Headers: { "X-Agencia-Token": "{Token_HASH_Entrada}" }""")
    body(doc, "Registra TipoOutAsientosCargarExitosa (56), TipoOutAsientosCargarFallida (57), TipoOutAsientoCambiarExitosa (58), TipoOutAsientoCambiarFallida (59).")

    h2(doc, "4.6 Confirmación de Pago — Movent notifica a proveedores")
    body(doc, "POST /api/reservaciones/pagar. PagoService procesa el pago internamente (actualiza Reservacion y Detalles_Reservacion, inserta Factura en transacción). Luego notifica a cada proveedor:")
    code(doc, """// Para cada Detalle_Reservacion de tipo vuelo:
POST {aerolinea_url}/api/reservaciones-agencia/{ID_Reserva_Proveedor}/confirmar
Headers: { "X-Agencia-Token": "{Token_HASH_Entrada}" }
Body: { "confirmado": true, "monto": 450.00 }

// Para cada Detalle_Reservacion de tipo hotel:
POST {hotelera_url}/agencia/reservaciones/{ID_Reserva_Proveedor}/confirmar
Headers: { "X-Agencia-Token": "{Token_HASH_Entrada}" }
Body: { "confirmado": true }""")
    body(doc, "Registra TipoOutPagoProveedorExitoso (60) o TipoOutPagoProveedorFallido (61). Si todas las confirmaciones son exitosas: TipoCompraExitosa (25). Si hay fallo en pago: TipoCompraFallidaPago (26).")

    h2(doc, "4.7 Expiración — Movent notifica a proveedores (background)")
    code(doc, """// Para detalles de vuelo vencidos:
POST {aerolinea_url}/api/reservaciones-agencia/{ID_Reserva_Proveedor}/expirar
Headers: { "X-Agencia-Token": "{Token_HASH_Entrada}" }

// Para detalles de hotel vencidos:
POST {hotelera_url}/agencia/reservaciones/{ID_Reserva_Proveedor}/expirar
Headers: { "X-Agencia-Token": "{Token_HASH_Entrada}" }""")
    body(doc, "Iniciado por ExpiracionService. Registra TipoReservaExpirada (28).")

    h2(doc, "4.8 Cancelación — Bidireccional")
    body(doc, "Cancelación iniciada por usuario (POST /api/reservaciones/{id}/cancelar):")
    code(doc, """// Movent llama al proveedor:
POST {proveedor_url}/api/reservaciones-agencia/{ID_Reserva_Proveedor}/cancelar  (Aerolínea)
POST {proveedor_url}/agencia/reservaciones/{ID_Reserva_Proveedor}/cancelar      (Hotelera)
Headers: { "X-Agencia-Token": "{Token_HASH_Entrada}" }""")
    body(doc, "Registra TipoCancelacionUsuario (29) o TipoCancelacionFallida (31).")
    body(doc, "Cancelación iniciada por proveedor (callback a Movent):")
    code(doc, """// El proveedor llama a Movent:
POST /api/proveedores-ext/detalles/{idReservaProveedor}/cancelar
Headers: { "X-Agencia-Token": "{Token_HASH_Salida}" }
// ProveedorAuthRequerido valida el token y determina qué proveedor es.
// CancelacionProveedorController actualiza el estado del detalle a Cancelado.
// Registra TipoCancelacionProveedor (30).""")

    h2(doc, "4.9 Notificación de Actualización — Proveedor → Movent")
    code(doc, """// El proveedor notifica un cambio (precio, horario, etc.):
POST /api/proveedores-ext/detalles/{idReservaProveedor}/actualizar
Headers: { "X-Agencia-Token": "{Token_HASH_Salida}" }
Body: { "tipo": "precio", "valor_nuevo": 520.00, "razon": "cambio de tarifa" }
// Registra TipoActualizacionProveedor (32).""")
    page_break(doc)

    # ══════════════════════════════════════════════════════
    # PARTE 5 — QUERIES SQL (MySQL)
    # ══════════════════════════════════════════════════════
    h1(doc, "Parte 5 — Queries SQL (MySQL)")

    h2(doc, "5.1 Autenticación y Sesión")
    code(doc, """-- LoginRepository: credenciales del usuario
SELECT ID, Username, PasswordHash, RolID, EstadoID
FROM usuario
WHERE LOWER(TRIM(Username)) = LOWER(TRIM(?))
LIMIT 1

-- LogSesionRepository: registrar evento de auditoría
INSERT INTO log_sesion
    (Tipo_Evento_ID, Usuario_ID, IP_Origen, Descripcion, Fecha_Hora)
VALUES (?, ?, ?, ?, NOW())""")

    h2(doc, "5.2 Búsqueda de Proveedores")
    code(doc, """-- Obtener ID de ciudad por nombre y país
SELECT c.ID FROM Ciudad c
JOIN Pais p ON c.PaisID = p.ID
WHERE LOWER(TRIM(c.Nombre)) = LOWER(TRIM(?))
  AND LOWER(TRIM(p.Nombre)) = LOWER(TRIM(?))

-- Obtener proveedores activos para una ruta y tipo
SELECT DISTINCT p.ID, p.Nombre, p.URL_API, p.Token_HASH_Entrada, p.Porcentaje_Ganancia
FROM Catalogo_Proveedor cp
JOIN Proveedor p ON cp.Proveedor_ID = p.ID
WHERE cp.Ciudad_Origen_ID = ?
  AND cp.Tipo_Catalogo_ID = ?
  AND p.EstadoID = 1

-- Registrar búsqueda
INSERT INTO Busqueda
    (Tipo_Busqueda_ID, UsuarioID, parametros_json, CiudadOrigenID, CiudadDestinoID)
VALUES (?, ?, ?, ?, ?)""")

    h2(doc, "5.3 Gestión de Reservaciones")
    code(doc, """-- Crear reservación pendiente
INSERT INTO Reservacion
    (No_Reservacion, Total, EstadoID, Usuario_ID, Fecha_Expiracion, Fecha_Creacion, Tipo_Reserva_ID)
VALUES (?, 0, ?, ?, ?, NOW(), ?)

-- Agregar detalle de reservación
INSERT INTO Detalles_Reservacion
    (Reservacion_ID, Proveedor_ID, ID_Reserva_Proveedor, Tipo_Detalle_ID, Estado_Detalle_ID, Precio)
VALUES (?, ?, ?, ?, 1, ?)

-- Obtener detalles de una reservación (para notificar proveedores)
SELECT dr.ID_Reserva_Proveedor, p.ID, p.URL_API, p.Token_HASH_Entrada, dr.Tipo_Detalle_ID
FROM Detalles_Reservacion dr
JOIN Proveedor p ON dr.Proveedor_ID = p.ID
WHERE dr.Reservacion_ID = ?

-- Actualizar estado de reservación
UPDATE Reservacion SET EstadoID = ? WHERE ID = ? AND EstadoID = ?
UPDATE Detalles_Reservacion SET Estado_Detalle_ID = ? WHERE Reservacion_ID = ?""")

    h2(doc, "5.4 Expiración de Reservaciones")
    code(doc, """-- Consultar reservaciones pendientes vencidas (ExpiracionService)
SELECT ID, Usuario_ID, No_Reservacion
FROM Reservacion
WHERE EstadoID = 1 AND Fecha_Expiracion <= NOW()

-- Expirar reservaciones (batch por fecha)
UPDATE Reservacion
SET EstadoID = ?
WHERE EstadoID = ? AND Fecha_Expiracion < NOW()

-- Obtener detalles pendientes para notificar expiraciones a proveedores
SELECT dr.ID_Reserva_Proveedor, p.ID, p.URL_API, p.Token_HASH_Entrada, dr.Tipo_Detalle_ID
FROM Detalles_Reservacion dr
JOIN Proveedor p ON dr.Proveedor_ID = p.ID
WHERE dr.Reservacion_ID = ? AND dr.Estado_Detalle_ID = 1""")

    h2(doc, "5.5 Proceso de Pago")
    code(doc, """-- Validar reservación para pago
SELECT Tipo_Reserva_ID, Total
FROM Reservacion
WHERE ID = ? AND Usuario_ID = ? AND EstadoID = 1

-- Contar detalles por tipo (para calcular descuento de paquete)
SELECT
    SUM(CASE WHEN Tipo_Detalle_ID = 1 THEN 1 ELSE 0 END) AS vuelos,
    SUM(CASE WHEN Tipo_Detalle_ID = 2 THEN 1 ELSE 0 END) AS hoteles
FROM Detalles_Reservacion
WHERE Reservacion_ID = ? AND Estado_Detalle_ID = 1

-- Confirmar pago (dentro de transacción)
UPDATE Reservacion SET EstadoID = 2, Total = ? WHERE ID = ?
UPDATE Detalles_Reservacion SET Estado_Detalle_ID = 2 WHERE Reservacion_ID = ?
INSERT INTO Factura (Reservacion_ID, NIT, Total, Codigo_Postal) VALUES (?, ?, ?, ?)

-- Obtener número de reservación (para PDF/correo)
SELECT No_Reservacion FROM Reservacion WHERE ID = ?""")

    h2(doc, "5.6 Autenticación de Proveedores (ProveedorAuthRequerido)")
    code(doc, """-- Validar token de callback de proveedor
SELECT ID, Tipo_Proveedor_ID, Nombre, EstadoID
FROM proveedor
WHERE Token_HASH_Salida = ?
LIMIT 1""")

    h2(doc, "5.7 Gestión de Catálogos")
    code(doc, """-- Obtener proveedores activos para sincronización
SELECT p.ID, p.URL_API, p.Token_HASH_Entrada, p.Tipo_Proveedor_ID
FROM Proveedor p
WHERE p.EstadoID = 1

-- Eliminar catálogo anterior antes de re-sincronizar
DELETE FROM Catalogo_Proveedor WHERE Proveedor_ID = ?

-- Insertar ruta de aerolínea en catálogo
INSERT INTO Catalogo_Proveedor
    (Proveedor_ID, Ciudad_Origen_ID, Ciudad_Destino_ID, Tipo_Catalogo_ID)
VALUES (?, ?, ?, 1)

-- Insertar hotel en catálogo
INSERT INTO Catalogo_Proveedor
    (Proveedor_ID, Ciudad_ID, Tipo_Catalogo_ID)
VALUES (?, ?, 2)""")

    h2(doc, "5.8 Mis Reservaciones (Consulta del Usuario)")
    code(doc, """-- Listar reservaciones del usuario con estado
SELECT r.ID, r.No_Reservacion, r.Total, r.Fecha_Creacion, r.Fecha_Expiracion,
       er.Nombre AS Estado, tr.Nombre AS TipoReserva
FROM Reservacion r
JOIN EstadoReserva er ON r.EstadoID = er.ID
JOIN TipoReserva   tr ON r.Tipo_Reserva_ID = tr.ID
WHERE r.Usuario_ID = ?
ORDER BY r.Fecha_Creacion DESC

-- Detalle de una reservación con sus detalles de vuelo/hotel
SELECT dr.ID, dr.ID_Reserva_Proveedor, dr.Tipo_Detalle_ID,
       dr.Precio, p.Nombre AS Proveedor, ed.Nombre AS Estado
FROM Detalles_Reservacion dr
JOIN Proveedor p ON dr.Proveedor_ID = p.ID
JOIN EstadoDetalle ed ON dr.Estado_Detalle_ID = ed.ID
WHERE dr.Reservacion_ID = ?""")

    h2(doc, "5.9 Handshake — Persistencia de Tokens")
    code(doc, """-- Guardar tokens tras handshake exitoso
UPDATE Proveedor
SET Token_HASH_Entrada = ?, Token_HASH_Salida = ?
WHERE ID = ?

-- Obtener URL_API del proveedor para handshake
SELECT URL_API FROM Proveedor WHERE ID = ?""")
    page_break(doc)

    # ══════════════════════════════════════════════════════
    # PARTE 6 — SISTEMA DE AUDITORÍA (61 EVENTOS)
    # ══════════════════════════════════════════════════════
    h1(doc, "Parte 6 — Sistema de Auditoría (ConstantesLog.go)")
    body(doc, "Movent implementa un sistema de auditoría exhaustivo con 61 tipos de eventos. Cada evento se registra en la tabla log_sesion con Tipo_Evento_ID, Usuario_ID (nullable), IP_Origen, Descripcion y Fecha_Hora. El servicio LogSesionService.RegistrarEvento() es invocado desde controllers y servicios background.")

    h2(doc, "6.1 Eventos de Autenticación (1–23)")
    table(doc, ["ID", "Constante", "Descripción"], [
        [1,  "TipoLoginExitoso",                      "Login exitoso"],
        [2,  "TipoLoginFallidoCredenciales",          "Credenciales incorrectas"],
        [3,  "TipoLoginFallidoPayload",                "Payload de login inválido"],
        [4,  "TipoLoginErrorInterno",                  "Error interno en login"],
        [5,  "TipoRegistroExitoso",                    "Registro de usuario exitoso"],
        [6,  "TipoRegistroFallidoPayload",             "Payload de registro inválido"],
        [7,  "TipoRegistroFallidoCorreoDup",           "Correo ya registrado"],
        [8,  "TipoRegistroFallidoUsernameDup",         "Username ya registrado"],
        [9,  "TipoRegistroFallidoValidacion",          "Error de validación en registro"],
        [10, "TipoRegistroErrorInterno",               "Error interno en registro"],
        [11, "TipoLoginFallidoDeshabilitado",          "Cuenta deshabilitada"],
        [12, "TipoRegistroFallidoPasaporteDup",        "Pasaporte ya registrado"],
        [13, "TipoRegistroFallidoCamposRequeridos",    "Campos requeridos ausentes"],
        [14, "TipoRegistroFallidoEdadMinima",          "Edad por debajo del mínimo"],
        [15, "TipoRegistroFallidoContrasenaDebil",     "Contraseña débil"],
        [16, "TipoRegistroFallidoEmailInvalido",       "Formato de email inválido"],
        [17, "TipoRegistroFallidoUsernameInvalido",    "Formato de username inválido"],
        [18, "TipoRegistroFallidoPasaporteInvalido",   "Formato de pasaporte inválido"],
        [19, "TipoRegistroFallidoTelefonoInvalido",    "Formato de teléfono inválido"],
        [20, "TipoLoginFallidoCampos",                 "Campos de login ausentes"],
        [21, "TipoLoginFallidoCaptchaAusente",         "CAPTCHA no enviado"],
        [22, "TipoLoginFallidoCaptchaInvalido",        "CAPTCHA inválido"],
        [23, "TipoLogout",                             "Logout exitoso"],
    ])

    h2(doc, "6.2 Eventos de Sesión y Transacciones (24–43)")
    table(doc, ["ID", "Constante", "Descripción"], [
        [24, "TipoLogoutSinSesionActiva",          "Logout sin sesión activa"],
        [25, "TipoCompraExitosa",                  "Compra/pago completado"],
        [26, "TipoCompraFallidaPago",              "Fallo en proceso de pago"],
        [27, "TipoReservaCreada",                  "Reservación creada (pendiente)"],
        [28, "TipoReservaExpirada",                "Reservación expirada por tiempo"],
        [29, "TipoCancelacionUsuario",             "Usuario canceló reservación"],
        [30, "TipoCancelacionProveedor",           "Proveedor notificó cancelación"],
        [31, "TipoCancelacionFallida",             "Intento de cancelación fallido"],
        [32, "TipoActualizacionProveedor",         "Proveedor notificó actualización"],
        [33, "TipoCambioPassword",                 "Contraseña cambiada exitosamente"],
        [34, "TipoCambioPasswordFallido",          "Fallo al cambiar contraseña"],
        [35, "TipoHandshakeProveedorExitoso",      "Handshake exitoso con proveedor"],
        [36, "TipoHandshakeProveedorFallido",      "Handshake fallido con proveedor"],
        [37, "TipoCatalogoActualizadoExitoso",     "Catálogo sincronizado exitosamente"],
        [38, "TipoCatalogoActualizadoFallido",     "Fallo en sincronización de catálogo"],
        [39, "TipoCambioPerfil",                   "Perfil de usuario actualizado"],
        [40, "TipoProveedorCreado",                "Nuevo proveedor registrado"],
        [41, "TipoProveedorEditado",               "Datos de proveedor editados"],
        [42, "TipoProveedorEstadoCambiado",        "Estado de proveedor cambiado"],
        [43, "TipoRolUsuarioActualizado",          "Rol de usuario actualizado"],
    ])

    h2(doc, "6.3 Eventos Salientes — Llamadas a Proveedores (44–61)")
    table(doc, ["ID", "Constante", "Descripción"], [
        [44, "TipoOutBusquedaVuelosExitosa",            "Búsqueda de vuelos exitosa"],
        [45, "TipoOutBusquedaVuelosSinResultados",      "Búsqueda de vuelos sin resultados"],
        [46, "TipoOutBusquedaVuelosFallida",            "Error en búsqueda de vuelos"],
        [47, "TipoOutBusquedaHotelesExitosa",           "Búsqueda de hoteles exitosa"],
        [48, "TipoOutBusquedaHotelesSinResultados",     "Búsqueda de hoteles sin resultados"],
        [49, "TipoOutBusquedaHotelesFallida",           "Error en búsqueda de hoteles"],
        [50, "TipoOutReservaVueloProveedorExitosa",     "Reservación temporal de vuelo exitosa"],
        [51, "TipoOutReservaVueloProveedorFallida",     "Error en reservación temporal de vuelo"],
        [52, "TipoOutReservaHotelProveedorExitosa",     "Reservación temporal de hotel exitosa"],
        [53, "TipoOutReservaHotelProveedorFallida",     "Error en reservación temporal de hotel"],
        [54, "TipoOutPasajerosProveedorExitosa",        "Envío de pasajeros exitoso"],
        [55, "TipoOutPasajerosProveedorFallida",        "Error en envío de pasajeros"],
        [56, "TipoOutAsientosCargarExitosa",            "Carga de mapa de asientos exitosa"],
        [57, "TipoOutAsientosCargarFallida",            "Error al cargar mapa de asientos"],
        [58, "TipoOutAsientoCambiarExitosa",            "Cambio de asiento exitoso"],
        [59, "TipoOutAsientoCambiarFallida",            "Error en cambio de asiento"],
        [60, "TipoOutPagoProveedorExitoso",             "Confirmación de pago a proveedor exitosa"],
        [61, "TipoOutPagoProveedorFallido",             "Error al confirmar pago con proveedor"],
    ])
    page_break(doc)

    # ══════════════════════════════════════════════════════
    # ANEXO — QUERIES DE MÉTRICAS PARA DEFENSA
    # ══════════════════════════════════════════════════════
    h1(doc, "Anexo — Queries de Métricas para Defensa")
    body(doc, "Queries de análisis operacional ejecutables directamente contra la base de datos MySQL de Movent.")

    h2(doc, "A.1 Reservaciones por Estado")
    code(doc, """SELECT er.Nombre AS Estado, COUNT(*) AS Total,
       SUM(r.Total) AS Monto_Total
FROM Reservacion r
JOIN EstadoReserva er ON r.EstadoID = er.ID
GROUP BY er.Nombre
ORDER BY Total DESC;""")

    h2(doc, "A.2 Distribución por Tipo de Reservación")
    code(doc, """SELECT tr.Nombre AS Tipo, COUNT(*) AS Total,
       AVG(r.Total) AS Promedio_Monto
FROM Reservacion r
JOIN TipoReserva tr ON r.Tipo_Reserva_ID = tr.ID
WHERE r.EstadoID = 2  -- solo confirmadas
GROUP BY tr.Nombre;""")

    h2(doc, "A.3 Proveedores más Utilizados")
    code(doc, """SELECT p.Nombre AS Proveedor,
       tp.Nombre AS Tipo,
       COUNT(dr.ID) AS Detalles_Totales,
       SUM(dr.Precio) AS Ingresos_Generados
FROM Detalles_Reservacion dr
JOIN Proveedor p ON dr.Proveedor_ID = p.ID
JOIN TipoProveedor tp ON p.Tipo_Proveedor_ID = tp.ID
WHERE dr.Estado_Detalle_ID = 2
GROUP BY p.ID, p.Nombre, tp.Nombre
ORDER BY Detalles_Totales DESC;""")

    h2(doc, "A.4 Análisis de Eventos de Auditoría")
    code(doc, """SELECT te.Nombre AS Tipo_Evento, COUNT(*) AS Ocurrencias,
       MAX(ls.Fecha_Hora) AS Ultimo_Registro
FROM log_sesion ls
JOIN TipoEvento te ON ls.Tipo_Evento_ID = te.ID
GROUP BY te.ID, te.Nombre
ORDER BY Ocurrencias DESC
LIMIT 20;""")

    h2(doc, "A.5 Tasa de Éxito vs Fallo en Búsquedas")
    code(doc, """SELECT
    SUM(CASE WHEN ls.Tipo_Evento_ID IN (44, 47) THEN 1 ELSE 0 END) AS Busquedas_Exitosas,
    SUM(CASE WHEN ls.Tipo_Evento_ID IN (46, 49) THEN 1 ELSE 0 END) AS Busquedas_Fallidas,
    SUM(CASE WHEN ls.Tipo_Evento_ID IN (45, 48) THEN 1 ELSE 0 END) AS Sin_Resultados,
    ROUND(
        100.0 * SUM(CASE WHEN ls.Tipo_Evento_ID IN (44, 47) THEN 1 ELSE 0 END) /
        NULLIF(COUNT(*), 0), 2
    ) AS Tasa_Exito_Pct
FROM log_sesion ls
WHERE ls.Tipo_Evento_ID BETWEEN 44 AND 49;""")

    h2(doc, "A.6 Reservaciones Expiradas por Día")
    code(doc, """SELECT DATE(Fecha_Hora) AS Dia,
       COUNT(*) AS Reservaciones_Expiradas
FROM log_sesion
WHERE Tipo_Evento_ID = 28  -- TipoReservaExpirada
GROUP BY DATE(Fecha_Hora)
ORDER BY Dia DESC
LIMIT 30;""")

    h2(doc, "A.7 Intentos de Login Fallidos por IP")
    code(doc, """SELECT IP_Origen,
       COUNT(*) AS Intentos_Fallidos,
       MAX(Fecha_Hora) AS Ultimo_Intento
FROM log_sesion
WHERE Tipo_Evento_ID IN (2, 3, 11, 20, 21, 22)
GROUP BY IP_Origen
HAVING Intentos_Fallidos >= 3
ORDER BY Intentos_Fallidos DESC;""")

    h2(doc, "A.8 Ingresos Mensuales Confirmados")
    code(doc, """SELECT DATE_FORMAT(r.Fecha_Creacion, '%Y-%m') AS Mes,
       COUNT(*) AS Reservaciones_Pagadas,
       SUM(r.Total) AS Ingresos_Totales,
       AVG(r.Total) AS Ticket_Promedio
FROM Reservacion r
WHERE r.EstadoID = 2
GROUP BY DATE_FORMAT(r.Fecha_Creacion, '%Y-%m')
ORDER BY Mes DESC
LIMIT 12;""")

    h2(doc, "A.9 Handshakes y Sincronizaciones por Proveedor")
    code(doc, """SELECT p.Nombre AS Proveedor,
    SUM(CASE WHEN ls.Tipo_Evento_ID = 35 THEN 1 ELSE 0 END) AS Handshakes_Exitosos,
    SUM(CASE WHEN ls.Tipo_Evento_ID = 36 THEN 1 ELSE 0 END) AS Handshakes_Fallidos,
    SUM(CASE WHEN ls.Tipo_Evento_ID = 37 THEN 1 ELSE 0 END) AS Sincronizaciones_OK,
    SUM(CASE WHEN ls.Tipo_Evento_ID = 38 THEN 1 ELSE 0 END) AS Sincronizaciones_Error
FROM log_sesion ls
JOIN Proveedor p ON ls.Descripcion LIKE CONCAT('%', p.Nombre, '%')
GROUP BY p.Nombre;""")

    h2(doc, "A.10 Ciudades más Buscadas (Origen)")
    code(doc, """SELECT c.Nombre AS Ciudad_Origen, pa.Nombre AS Pais,
       COUNT(*) AS Total_Busquedas
FROM Busqueda b
JOIN Ciudad c ON b.CiudadOrigenID = c.ID
JOIN Pais pa ON c.PaisID = pa.ID
GROUP BY c.ID, c.Nombre, pa.Nombre
ORDER BY Total_Busquedas DESC
LIMIT 10;""")

    h2(doc, "A.11 Rutas más Populares")
    code(doc, """SELECT
    co.Nombre AS Ciudad_Origen, cd.Nombre AS Ciudad_Destino,
    COUNT(*) AS Busquedas
FROM Busqueda b
JOIN Ciudad co ON b.CiudadOrigenID = co.ID
JOIN Ciudad cd ON b.CiudadDestinoID = cd.ID
WHERE b.Tipo_Busqueda_ID = 1  -- solo vuelos
GROUP BY co.ID, cd.ID, co.Nombre, cd.Nombre
ORDER BY Busquedas DESC
LIMIT 10;""")

    h2(doc, "A.12 Cancelaciones por Origen")
    code(doc, """SELECT
    SUM(CASE WHEN Tipo_Evento_ID = 29 THEN 1 ELSE 0 END) AS Cancel_Usuario,
    SUM(CASE WHEN Tipo_Evento_ID = 30 THEN 1 ELSE 0 END) AS Cancel_Proveedor,
    SUM(CASE WHEN Tipo_Evento_ID = 31 THEN 1 ELSE 0 END) AS Cancel_Fallida
FROM log_sesion
WHERE Tipo_Evento_ID IN (29, 30, 31);""")

    h2(doc, "A.13 Tasa de Conversión (Reservación → Pago)")
    code(doc, """SELECT
    COUNT(*) AS Total_Reservaciones,
    SUM(CASE WHEN EstadoID = 2 THEN 1 ELSE 0 END) AS Pagadas,
    SUM(CASE WHEN EstadoID = 3 THEN 1 ELSE 0 END) AS Canceladas,
    SUM(CASE WHEN EstadoID = 4 THEN 1 ELSE 0 END) AS Expiradas,
    ROUND(100.0 * SUM(CASE WHEN EstadoID = 2 THEN 1 ELSE 0 END) /
        NULLIF(COUNT(*), 0), 2) AS Conversion_Pct
FROM Reservacion;""")

    h2(doc, "A.14 Top 10 Usuarios por Volumen de Compras")
    code(doc, """SELECT u.Username,
       COUNT(r.ID) AS Reservaciones_Pagadas,
       SUM(r.Total) AS Gasto_Total
FROM Reservacion r
JOIN usuario u ON r.Usuario_ID = u.ID
WHERE r.EstadoID = 2
GROUP BY u.ID, u.Username
ORDER BY Gasto_Total DESC
LIMIT 10;""")
    page_break(doc)

    # ══════════════════════════════════════════════════════
    # INVENTARIO DE COBERTURA
    # ══════════════════════════════════════════════════════
    h1(doc, "Inventario de Cobertura del Documento")
    table(doc, ["Ítem", "Cubierto", "Detalle"], [
        ["Stack completo (Go + Vue + MySQL)", "Sí", "Parte 0 — 7 secciones"],
        ["Dependencias go.mod", "Sí", "8 dependencias directas con versiones"],
        ["Arquitectura de directorios", "Sí", "Backend y frontend, Parte 2"],
        ["Tabla completa de endpoints", "Sí", "44 endpoints, Parte 2.4"],
        ["Rutas Vue Router", "Sí", "30 rutas con guards, Parte 2.5"],
        ["Middleware JWT (auth.go)", "Sí", "Parte 3.2"],
        ["Middleware Proveedor (X-Agencia-Token)", "Sí", "Parte 3.2 + 4.1"],
        ["Middleware RBAC (RolRequerido)", "Sí", "Parte 3.2"],
        ["CatalogoSchedulerService (7 días)", "Sí", "Parte 3.4 con código"],
        ["ExpiracionService (1 min)", "Sí", "Parte 3.5 con código"],
        ["PDF con gofpdf", "Sí", "Parte 3.6 con colores y layout"],
        ["Flujo Handshake (Aerolínea + Hotelera)", "Sí", "Parte 4.1 con payloads"],
        ["Flujo Catálogo", "Sí", "Parte 4.2"],
        ["Flujo Búsqueda (vuelos + hoteles)", "Sí", "Parte 4.3 con markup"],
        ["Flujo Reservación Temporal", "Sí", "Parte 4.4 (vuelo + hotel)"],
        ["Flujo Asientos", "Sí", "Parte 4.5"],
        ["Flujo Pago y Confirmación", "Sí", "Parte 4.6"],
        ["Flujo Expiración (background)", "Sí", "Parte 4.7"],
        ["Flujo Cancelación (bidireccional)", "Sí", "Parte 4.8"],
        ["Flujo Notificación de Actualización", "Sí", "Parte 4.9"],
        ["Queries MySQL (9 categorías)", "Sí", "Parte 5"],
        ["Sistema de auditoría (61 eventos)", "Sí", "Parte 6, tablas completas"],
        ["Queries de métricas para defensa", "Sí", "Anexo, 14 queries"],
        ["Variables de entorno (.env)", "Sí", "Parte 2.3 + texto"],
        ["Estructura de modelo de roles", "Sí", "rol_id=1 (usuario), rol_id=2 (admin)"],
    ])

    out = "Manual_Programador_Agencia.docx"
    doc.save(out)
    print(f"[OK] Guardado: {out}")

if __name__ == "__main__":
    build()
