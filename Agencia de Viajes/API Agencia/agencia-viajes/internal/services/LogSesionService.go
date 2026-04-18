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

	exitoso := tipoEventoID == helpers.TipoLoginExitoso || tipoEventoID == helpers.TipoRegistroExitoso

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
