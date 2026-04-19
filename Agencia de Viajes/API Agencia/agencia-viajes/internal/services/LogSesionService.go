// # Package services
//
// Servicios de negocio de la agencia de viajes. Este paquete contiene la logica
// central para reservaciones, busquedas, autenticacion, catalogos y comunicacion
// con proveedores externos (aerolineas y hoteleras).
package services

import (
	"agencia-viajes/internal/helpers"
	"agencia-viajes/internal/models"
	"agencia-viajes/internal/repositories"
	"database/sql"
	"log"

	"github.com/gin-gonic/gin"
)

// LogSesionService
//
// Servicio encargado de registrar eventos de autenticacion (login y registro)
// en la tabla log_sesion para auditoria y trazabilidad. Todas las inserciones
// se realizan de forma asincrona para no bloquear el flujo del request.
type LogSesionService struct {
	repo *repositories.LogSesionRepository
}

// NewLogSesionService
//
// Crea e inicializa una nueva instancia de LogSesionService con su repositorio.
//
// Parametros:
//   - repo: repositorio de log de sesion ya inicializado
//
// Retorna:
//   - *LogSesionService: instancia lista para usar
func NewLogSesionService(repo *repositories.LogSesionRepository) *LogSesionService {
	return &LogSesionService{repo: repo}
}

// Registrar
//
// Registra de forma asincrona un evento de autenticacion en la tabla log_sesion.
// Extrae la IP y el User-Agent del contexto de Gin antes de lanzar la goroutine,
// para evitar acceder al contexto fuera del ciclo de vida del request.
// Si la insercion falla, solo registra el error en consola sin propagar la falla
// al caller ni interrumpir el flujo del usuario.
//
// Parametros:
//   - c: contexto de Gin del request actual, usado para extraer IP y User-Agent
//   - tipoEventoID: ID del tipo de evento segun la tabla tipo_evento_sesion
//   - usuarioID: puntero al ID del usuario autenticado; nil si no se conoce
//   - loginIntentado: valor enviado en el campo login del request (username o correo)
//   - mensaje: detalle adicional del evento (ej: mensaje de error); puede ser vacio
//
// Notas:
//   - El campo Exitoso se deriva automaticamente: true para IDs 1 (LOGIN_EXITOSO)
//     y 5 (REGISTRO_EXITOSO), false para cualquier otro tipo de evento
//   - User-Agent y Mensaje se truncan a 500 caracteres si exceden ese limite
func (s *LogSesionService) Registrar(c *gin.Context, tipoEventoID int, usuarioID *int, loginIntentado string, mensaje string) {
	ip := c.ClientIP()
	userAgent := c.GetHeader("User-Agent")

	if len(userAgent) > 500 {
		userAgent = userAgent[:500]
	}
	if len(mensaje) > 500 {
		mensaje = mensaje[:500]
	}

	exitoso := tipoEventoID == helpers.TipoLoginExitoso ||
		tipoEventoID == helpers.TipoRegistroExitoso ||
		tipoEventoID == helpers.TipoLogout ||
		tipoEventoID == helpers.TipoCompraExitosa ||
		tipoEventoID == helpers.TipoReservaCreada ||
		tipoEventoID == helpers.TipoCancelacionUsuario ||
		tipoEventoID == helpers.TipoCancelacionProveedor ||
		tipoEventoID == helpers.TipoActualizacionProveedor ||
		tipoEventoID == helpers.TipoCambioPassword ||
		tipoEventoID == helpers.TipoHandshakeProveedorExitoso ||
		tipoEventoID == helpers.TipoCatalogoActualizadoExitoso ||
		tipoEventoID == helpers.TipoCambioPerfil ||
		tipoEventoID == helpers.TipoProveedorCreado ||
		tipoEventoID == helpers.TipoProveedorEditado ||
		tipoEventoID == helpers.TipoProveedorEstadoCambiado ||
		tipoEventoID == helpers.TipoRolUsuarioActualizado

	var uid sql.NullInt64
	if usuarioID != nil {
		uid = sql.NullInt64{Int64: int64(*usuarioID), Valid: true}
	}

	entry := models.LogSesion{
		TipoEventoID:   tipoEventoID,
		UsuarioID:      uid,
		LoginIntentado: sql.NullString{String: loginIntentado, Valid: loginIntentado != ""},
		Exitoso:        exitoso,
		IPOrigen:       sql.NullString{String: ip, Valid: ip != ""},
		UserAgent:      sql.NullString{String: userAgent, Valid: userAgent != ""},
		Mensaje:        sql.NullString{String: mensaje, Valid: mensaje != ""},
	}

	go func() {
		if err := s.repo.Insertar(entry); err != nil {
			log.Printf("[LOG_SESION] error: %v", err)
		}
	}()
}

// RegistrarSistema
//
// Variante de Registrar para eventos generados por procesos en background
// (scheduler, jobs, etc.) que no tienen un *gin.Context disponible. Usa
// "SYSTEM" como IP_Origen y describe el proceso en User_Agent.
//
// Parametros:
//   - tipoEventoID: ID del tipo de evento segun la tabla tipo_evento_sesion
//   - usuarioID: puntero al ID del usuario afectado; nil si no aplica
//   - loginIntentado: texto a guardar en Login_Intentado (ej: no_reservacion)
//   - mensaje: descripcion corta del evento
//   - origen: identificador del proceso que genera el evento (ej: "ExpiracionService")
//
// Notas:
//   - El campo Exitoso se deriva automaticamente: true para IDs 25 (COMPRA_EXITOSA)
//     y 27 (RESERVA_CREADA), false para cualquier otro tipo de evento
//   - User-Agent y Mensaje se truncan a 500 caracteres si exceden ese limite
//   - El error de insercion se loggea internamente sin propagarse al llamador
func (s *LogSesionService) RegistrarSistema(tipoEventoID int, usuarioID *int, loginIntentado, mensaje, origen string) {
	if len(origen) > 500 {
		origen = origen[:500]
	}
	if len(mensaje) > 500 {
		mensaje = mensaje[:500]
	}

	exitoso := tipoEventoID == helpers.TipoLoginExitoso ||
		tipoEventoID == helpers.TipoRegistroExitoso ||
		tipoEventoID == helpers.TipoLogout ||
		tipoEventoID == helpers.TipoCompraExitosa ||
		tipoEventoID == helpers.TipoReservaCreada ||
		tipoEventoID == helpers.TipoCancelacionUsuario ||
		tipoEventoID == helpers.TipoCancelacionProveedor ||
		tipoEventoID == helpers.TipoActualizacionProveedor ||
		tipoEventoID == helpers.TipoCambioPassword ||
		tipoEventoID == helpers.TipoHandshakeProveedorExitoso ||
		tipoEventoID == helpers.TipoCatalogoActualizadoExitoso ||
		tipoEventoID == helpers.TipoCambioPerfil ||
		tipoEventoID == helpers.TipoProveedorCreado ||
		tipoEventoID == helpers.TipoProveedorEditado ||
		tipoEventoID == helpers.TipoProveedorEstadoCambiado ||
		tipoEventoID == helpers.TipoRolUsuarioActualizado

	var uid sql.NullInt64
	if usuarioID != nil {
		uid = sql.NullInt64{Int64: int64(*usuarioID), Valid: true}
	}

	entry := models.LogSesion{
		TipoEventoID:   tipoEventoID,
		UsuarioID:      uid,
		LoginIntentado: sql.NullString{String: loginIntentado, Valid: loginIntentado != ""},
		Exitoso:        exitoso,
		IPOrigen:       sql.NullString{String: "SYSTEM", Valid: true},
		UserAgent:      sql.NullString{String: origen, Valid: origen != ""},
		Mensaje:        sql.NullString{String: mensaje, Valid: mensaje != ""},
	}

	go func() {
		if err := s.repo.Insertar(entry); err != nil {
			log.Printf("[LOG_SESION] error sistema: %v", err)
		}
	}()
}
