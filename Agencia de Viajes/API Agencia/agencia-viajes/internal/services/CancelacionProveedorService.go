package services

import (
	"agencia-viajes/internal/helpers"
	"agencia-viajes/internal/repositories"
	"fmt"
	"log"
)

// CancelacionProveedorService
//
// Servicio que orquesta el flujo de cancelacion iniciado por un proveedor externo.
// A diferencia del CancelacionService (cancelacion iniciada por el usuario),
// aqui NO se consulta al proveedor si puede cancelar: el proveedor YA cancelo
// en su sistema y nos esta notificando. El flujo es:
//
//  1. Verificar que el detalle existe y pertenece al proveedor autenticado
//  2. Verificar que el estado del detalle sea cancelable
//  3. En transaccion: cancelar detalle + actualizar reservacion + crear notificacion
//  4. Enviar correo al usuario con el resultado y el mensaje del proveedor
type CancelacionProveedorService struct {
	repo *repositories.CancelacionProveedorRepository
}

// NewCancelacionProveedorService
//
// Crea e inicializa una nueva instancia del servicio.
//
// Parametros:
//   - repo: repositorio de cancelacion por proveedor ya inicializado
//
// Retorna:
//   - *CancelacionProveedorService: instancia lista para usar
func NewCancelacionProveedorService(repo *repositories.CancelacionProveedorRepository) *CancelacionProveedorService {
	return &CancelacionProveedorService{repo: repo}
}

// CancelarDetallePorProveedor
//
// Ejecuta el flujo completo de cancelacion de un detalle de reservacion
// iniciado por el proveedor. Valida que el detalle sea cancelable, ejecuta
// la transaccion atomica en BD y dispara el correo de notificacion al usuario.
//
// El envio del correo es NO bloqueante para el proveedor: si falla, se registra
// en el log del servidor pero la respuesta HTTP ya fue exitosa porque la
// transaccion de BD se completo correctamente.
//
// Parametros:
//   - idReservaProveedor: ID de la reserva en el sistema del proveedor
//   - proveedorID:        ID del proveedor autenticado (inyectado por el middleware)
//   - mensajeProveedor:   mensaje explicativo que el proveedor envia como razon
//
// Retorna:
//   - error: si el detalle no existe, no pertenece al proveedor,
//     ya esta cancelado, o falla alguna operacion de BD
func (s *CancelacionProveedorService) CancelarDetallePorProveedor(
	idReservaProveedor string,
	proveedorID int,
	mensajeProveedor string,
) error {
	// 1. Verificar que el detalle existe, pertenece al proveedor y obtener sus IDs internos
	detalleID, reservacionID, estadoDetalleID, err := s.repo.ObtenerDetalleParaCancelarPorProveedor(
		idReservaProveedor, proveedorID,
	)
	if err != nil {
		return err
	}

	// 2. Solo se pueden cancelar detalles en estado Pendiente (1) o Confirmado (2)
	if estadoDetalleID != 1 && estadoDetalleID != 2 {
		return fmt.Errorf("el detalle no puede cancelarse en su estado actual (estado: %d)", estadoDetalleID)
	}

	// 3. Ejecutar la transaccion: cancelar detalle + actualizar reservacion + notificacion
	//    Retorna el estadoDestino para saber si la reservacion quedo Cancelada o Retenida
	estadoDestino, err := s.repo.CancelarDetalleYActualizarReservacion(detalleID, reservacionID, mensajeProveedor)
	if err != nil {
		return err
	}

	// 4. Enviar correo de notificacion al usuario
	//    Se hace en goroutine para no bloquear la respuesta al proveedor:
	//    la transaccion ya fue exitosa y el proveedor no debe esperar al SMTP.
	//    Los errores de correo se loguean en el servidor pero no se propagan.
	go func() {
		correo, nombre, apellido, noReservacion, errDatos := s.repo.ObtenerDatosCorreoReservacion(reservacionID)
		if errDatos != nil {
			log.Printf("[CancelacionProveedor] error obteniendo datos de correo para reservacion %d: %v", reservacionID, errDatos)
			return
		}

		htmlBody := helpers.BuildHTMLCancelacionProveedor(
			nombre, apellido, noReservacion, mensajeProveedor, estadoDestino,
		)

		asunto := fmt.Sprintf("MOVENT · Notificacion sobre tu reservacion %s", noReservacion)

		if errEmail := helpers.EnviarEmailHTML(correo, asunto, htmlBody); errEmail != nil {
			log.Printf("[CancelacionProveedor] error enviando correo a %s para reservacion %d: %v", correo, reservacionID, errEmail)
		}
	}()

	return nil
}
