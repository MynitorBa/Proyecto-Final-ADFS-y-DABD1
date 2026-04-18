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
)
