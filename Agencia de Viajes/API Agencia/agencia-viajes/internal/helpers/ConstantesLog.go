// # Package helpers
//
// Provee funciones auxiliares reutilizables para tareas comunes de la
// aplicacion Movent: generacion de tokens, hashing de contrasenas,
// manejo de sesiones JWT, envio de correos electronicos y generacion
// de documentos PDF.
package helpers

// Constantes de tipo de evento de sesion.
//
// Corresponden a los IDs de la tabla tipo_evento_sesion en la base de datos.
// Se usan en LogSesionService.Registrar para identificar el tipo de evento
// que se esta registrando.
const (
	TipoLoginExitoso               = 1
	TipoLoginFallidoCredenciales   = 2
	TipoLoginFallidoPayload        = 3
	TipoLoginErrorInterno          = 4
	TipoRegistroExitoso            = 5
	TipoRegistroFallidoPayload     = 6
	TipoRegistroFallidoCorreoDup   = 7
	TipoRegistroFallidoUsernameDup = 8
	TipoRegistroFallidoValidacion  = 9
	TipoRegistroErrorInterno       = 10
	TipoLoginFallidoDeshabilitado        = 11
	TipoRegistroFallidoPasaporteDup      = 12
	TipoRegistroFallidoCamposRequeridos  = 13
	TipoRegistroFallidoEdadMinima        = 14
	TipoRegistroFallidoContrasenaDebil   = 15
	TipoRegistroFallidoEmailInvalido     = 16
	TipoRegistroFallidoUsernameInvalido  = 17
	TipoRegistroFallidoPasaporteInvalido = 18
	TipoRegistroFallidoTelefonoInvalido  = 19
	TipoLoginFallidoCampos              = 20
	TipoLoginFallidoCaptchaAusente      = 21
	TipoLoginFallidoCaptchaInvalido     = 22
	TipoLogout                          = 23
	TipoLogoutSinSesionActiva           = 24
	TipoCompraExitosa                   = 25
	TipoCompraFallidaPago               = 26
	TipoReservaCreada                   = 27
	TipoReservaExpirada                 = 28
	TipoCancelacionUsuario              = 29
	TipoCancelacionProveedor            = 30
	TipoCancelacionFallida              = 31
	TipoActualizacionProveedor          = 32
	TipoCambioPassword                  = 33
	TipoCambioPasswordFallido           = 34
	TipoHandshakeProveedorExitoso       = 35
	TipoHandshakeProveedorFallido       = 36
	TipoCatalogoActualizadoExitoso      = 37
	TipoCatalogoActualizadoFallido      = 38
	TipoCambioPerfil                    = 39
	TipoProveedorCreado                 = 40
	TipoProveedorEditado                = 41
	TipoProveedorEstadoCambiado         = 42
	TipoRolUsuarioActualizado           = 43
)

// ==========================================
// EVENTOS DE COMUNICACIÓN REST AGENCIA → PROVEEDOR
// IDs 44-61 — Ver matriz de inserción en docs
// Importante: seguir el patrón Tipo* (no Evento*) para mantener consistencia
// con las 43 constantes existentes
// ==========================================

// Flujo A: Búsquedas (44-49)
const (
	TipoOutBusquedaVuelosExitosa        = 44
	TipoOutBusquedaVuelosSinResultados  = 45
	TipoOutBusquedaVuelosFallida        = 46
	TipoOutBusquedaHotelesExitosa       = 47
	TipoOutBusquedaHotelesSinResultados = 48
	TipoOutBusquedaHotelesFallida       = 49
)

// Flujo B: Reserva temporal en proveedor (50-53)
const (
	TipoOutReservaVueloProveedorExitosa = 50
	TipoOutReservaVueloProveedorFallida = 51
	TipoOutReservaHotelProveedorExitosa = 52
	TipoOutReservaHotelProveedorFallida = 53
)

// Flujo C: Pasajeros al proveedor (54-55)
const (
	TipoOutPasajerosProveedorExitosa = 54
	TipoOutPasajerosProveedorFallida = 55
)

// Flujo D: Cargar mapa de asientos (56-57)
const (
	TipoOutAsientosCargarExitosa = 56
	TipoOutAsientosCargarFallida = 57
)

// Flujo E: Cambio de asiento (58-59)
const (
	TipoOutAsientoCambiarExitosa = 58
	TipoOutAsientoCambiarFallida = 59
)

// Flujo F: Pago al proveedor (60-61)
const (
	TipoOutPagoProveedorExitoso = 60
	TipoOutPagoProveedorFallido = 61
)

// Acciones administrativas sobre reservaciones (62)
// IMPORTANTE: el registro ID=62 debe insertarse manualmente en la tabla
// tipo_evento_sesion de la base de datos con el nombre "Cancelacion por Admin".
const (
	TipoCancelacionAdmin = 62
)

// Flujo G: Edición de reservación (63-64)
const (
	TipoOutEditarReservacionExitosa = 63
	TipoOutEditarReservacionFallida = 64
)
