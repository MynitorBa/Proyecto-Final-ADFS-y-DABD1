// # Package controllers
//
// Controladores HTTP de la API de Movent. Cada controlador agrupa los handlers
// relacionados a un recurso o dominio especifico de la aplicacion.
package controllers

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/helpers"
	"agencia-viajes/internal/services"
	"log"
	"net/http"

	"github.com/gin-gonic/gin"
)

// UsuarioController
//
// Controlador que maneja los endpoints de gestion de usuarios,
// incluyendo el registro de nuevas cuentas y la consulta del listado
// completo para el panel de administracion.
type UsuarioController struct {
	service   *services.UsuarioService
	logSesion *services.LogSesionService
}

// NewUsuarioController
//
// Constructor que retorna una nueva instancia de UsuarioController
// con los servicios inyectados.
//
// Parametros:
//   - service: puntero al servicio de usuario
//   - logSesion: instancia del servicio de log de sesion para auditoria
//
// Retorna:
//   - *UsuarioController: puntero a la nueva instancia
func NewUsuarioController(service *services.UsuarioService, logSesion *services.LogSesionService) *UsuarioController {
	return &UsuarioController{service: service, logSesion: logSesion}
}

// Registrar
//
// Registra un nuevo usuario en el sistema a partir de los datos enviados en el
// body de la solicitud. Primero valida reglas de negocio (edad, contrasena,
// formatos), luego verifica duplicados de correo, pasaporte y username.
// Si el registro es exitoso, envia un correo de bienvenida en segundo plano
// sin bloquear la respuesta al cliente. Registra en log_sesion el resultado
// del intento de registro.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 201 Created: JSON con mensaje de exito si el usuario fue registrado
//   - HTTP 400 Bad Request: si el body no puede ser parseado, tiene datos invalidos
//     o falla alguna validacion de negocio (edad, contrasena, formatos)
//   - HTTP 409 Conflict: JSON con campos duplicados (correo, pasaporte, username)
//   - HTTP 500 Internal Server Error: si ocurre un error interno al registrar
//
// Notas:
//   - Las validaciones de reglas de negocio (edad, contrasena fuerte,
//     formato de email, etc.) se ejecutan en helpers.ValidarRegistro
//     antes de llegar al servicio
//   - Cada falla se registra en log_sesion con su tipo de evento especifico
//   - El correo de bienvenida se envia de forma asincrona (goroutine fire-and-forget)
//   - Los errores del envio de correo se registran en el log del servidor
//   - Para duplicados multiples se prioriza: correo > username > pasaporte
func (ctrl *UsuarioController) Registrar(c *gin.Context) {
	var req dto.RegistroUsuarioRequest

	if err := c.ShouldBindJSON(&req); err != nil {
		ctrl.logSesion.Registrar(c, helpers.TipoRegistroFallidoPayload, nil, "", err.Error())
		c.JSON(http.StatusBadRequest, gin.H{"error": "Datos inválidos"})
		return
	}

	// Validacion de reglas de negocio (fuente de verdad del backend).
	// El frontend tambien valida para UX, pero aqui es donde protegemos
	// contra bypass via Postman/curl.
	if tipoEvento, mensajeError, valido := helpers.ValidarRegistro(req); !valido {
		ctrl.logSesion.Registrar(c, tipoEvento, nil, req.Correo, mensajeError)
		c.JSON(http.StatusBadRequest, gin.H{"error": mensajeError})
		return
	}

	validacion, err := ctrl.service.Registrar(req)
	if err != nil {
		ctrl.logSesion.Registrar(c, helpers.TipoRegistroErrorInterno, nil, req.Correo, err.Error())
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Error al registrar usuario"})
		return
	}

	if validacion.Correo || validacion.Pasaporte || validacion.Username {
		// Determinar el tipo de evento mas especifico (prioridad: correo > username > pasaporte)
		tipoEvento := helpers.TipoRegistroFallidoValidacion
		loginIntentado := req.Correo
		if validacion.Correo {
			tipoEvento = helpers.TipoRegistroFallidoCorreoDup
		} else if validacion.Username {
			tipoEvento = helpers.TipoRegistroFallidoUsernameDup
			loginIntentado = req.Username
		} else if validacion.Pasaporte {
			tipoEvento = helpers.TipoRegistroFallidoPasaporteDup
			loginIntentado = req.Pasaporte
		}
		ctrl.logSesion.Registrar(c, tipoEvento, nil, loginIntentado, "")
		c.JSON(http.StatusConflict, validacion)
		return
	}

	go func() {
		if err := helpers.EnviarBienvenida(
			req.Correo,
			req.Nombre,
			req.Apellido,
			req.Username,
			req.Telefono,
			req.FechaNacimiento,
			req.Ciudad,
			req.Pais,
			req.Nacionalidades,
		); err != nil {
			log.Printf("[BIENVENIDA] Error enviando correo a %s: %v", req.Correo, err)
		}
	}()

	ctrl.logSesion.Registrar(c, helpers.TipoRegistroExitoso, nil, req.Correo, "")
	c.JSON(http.StatusCreated, gin.H{"mensaje": "Usuario registrado exitosamente"})
}

// ObtenerTodos
//
// Retorna la lista completa de usuarios registrados en el sistema con sus
// datos basicos y rol asignado. Usado por el panel de administracion para
// gestion de roles y asignacion de usuarios WebService a proveedores.
//
// Parametros:
//   - c: contexto de Gin con la solicitud HTTP
//
// Retorna:
//   - HTTP 200 OK: JSON con el listado de usuarios
//   - HTTP 500 Internal Server Error: si ocurre un error al consultar la base de datos
func (ctrl *UsuarioController) ObtenerTodos(c *gin.Context) {
	lista, err := ctrl.service.ObtenerTodos()
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Error al obtener usuarios"})
		return
	}
	c.JSON(http.StatusOK, lista)
}
