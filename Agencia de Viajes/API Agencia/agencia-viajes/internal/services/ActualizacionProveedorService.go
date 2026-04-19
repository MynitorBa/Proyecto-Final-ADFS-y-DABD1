package services

import (
	"agencia-viajes/internal/helpers"
	"agencia-viajes/internal/repositories"
	"fmt"
	"log"
)

// ActualizacionProveedorService
//
// Servicio que orquesta el flujo de notificacion de actualizacion iniciado
// por un proveedor externo. Es el mas simple de los flujos de proveedor:
// no modifica ningun estado de reservacion ni detalle, solo registra la
// notificacion en BD y envia un correo informativo al usuario.
//
// Flujo:
//  1. Verificar que el detalle existe y pertenece al proveedor autenticado
//  2. Insertar la notificacion en BD (Tipo 4 = Actualizacion por Proveedor)
//  3. Enviar correo informativo al usuario en goroutine
type ActualizacionProveedorService struct {
	repo *repositories.ActualizacionProveedorRepository
}

// NewActualizacionProveedorService
//
// Crea e inicializa una nueva instancia del servicio.
//
// Parametros:
//   - repo: repositorio de actualizacion por proveedor ya inicializado
//
// Retorna:
//   - *ActualizacionProveedorService: instancia lista para usar
func NewActualizacionProveedorService(repo *repositories.ActualizacionProveedorRepository) *ActualizacionProveedorService {
	return &ActualizacionProveedorService{repo: repo}
}

// NotificarActualizacion
//
// Registra la notificacion de actualizacion del proveedor y envia el correo
// informativo al usuario dueno de la reservacion.
//
// El envio de correo es NO bloqueante: se ejecuta en goroutine para no
// hacer esperar al proveedor por el SMTP. Si falla, se registra en el log.
//
// Parametros:
//   - idReservaProveedor: ID de la reserva en el sistema del proveedor
//   - proveedorID:        ID del proveedor autenticado (inyectado por middleware)
//   - mensajeProveedor:   descripcion de la actualizacion enviada por el proveedor
//
// Retorna:
//   - error: si el detalle no existe, no pertenece al proveedor o falla la BD
func (s *ActualizacionProveedorService) NotificarActualizacion(
	idReservaProveedor string,
	proveedorID int,
	mensajeProveedor string,
) error {
	// 1. Verificar que el detalle existe y pertenece al proveedor, obtener IDs internos
	detalleID, reservacionID, err := s.repo.ObtenerDetalleParaActualizacion(
		idReservaProveedor, proveedorID,
	)
	if err != nil {
		return err
	}

	// 2. Insertar la notificacion en BD (sin tocar ningun estado)
	if err := s.repo.InsertarNotificacionActualizacion(reservacionID, detalleID, mensajeProveedor); err != nil {
		return fmt.Errorf("error registrando notificacion: %w", err)
	}

	// 3. Enviar correo al usuario en goroutine — no bloquea la respuesta al proveedor
	go func() {
		correo, nombre, apellido, noReservacion, errDatos := s.repo.ObtenerDatosCorreoActualizacion(reservacionID)
		if errDatos != nil {
			log.Printf("[ActualizacionProveedor] error obteniendo datos de correo para reservacion %d: %v", reservacionID, errDatos)
			return
		}

		htmlBody := helpers.BuildHTMLActualizacionProveedor(nombre, apellido, noReservacion, mensajeProveedor)
		asunto := fmt.Sprintf("MOVENT · Actualizacion sobre tu reservacion %s", noReservacion)

		if errEmail := helpers.EnviarEmailHTML(correo, asunto, htmlBody); errEmail != nil {
			log.Printf("[ActualizacionProveedor] error enviando correo a %s para reservacion %d: %v", correo, reservacionID, errEmail)
		}
	}()

	return nil
}
