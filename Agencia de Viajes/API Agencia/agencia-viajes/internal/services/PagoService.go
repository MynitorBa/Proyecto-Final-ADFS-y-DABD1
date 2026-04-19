// # Package services
//
// Contiene los servicios de negocio de la agencia de viajes,
// incluyendo procesamiento de pagos, reservaciones, proveedores y usuarios.
package services

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/helpers"
	"agencia-viajes/internal/repositories"
	"bytes"
	"encoding/json"
	"errors"
	"fmt"
	"net/http"

	"github.com/gin-gonic/gin"
)

// PagoService
//
// Servicio encargado de gestionar el procesamiento de pagos de reservaciones,
// incluyendo validacion de tarjetas, verificacion de integridad de detalles,
// aplicacion de descuento por paquete y notificacion a proveedores externos.
type PagoService struct {
	repo        *repositories.PagoRepository
	reservaRepo *repositories.ReservacionRepository
	configRepo  *repositories.AgenciaConfiguracionRepository
	logSesion   *LogSesionService
}

// NewPagoService
//
// Crea e inicializa una nueva instancia de PagoService con sus dependencias.
//
// Parametros:
//   - repo:      repositorio de pagos para operaciones en base de datos
//   - rr:        repositorio de reservaciones para consultar detalles
//   - cr:        repositorio de configuracion de la agencia para leer el descuento de paquetes
//   - logSesion: servicio de auditoria para registrar eventos REST salientes
//
// Retorna:
//   - *PagoService: instancia inicializada del servicio de pagos
func NewPagoService(
	repo *repositories.PagoRepository,
	rr *repositories.ReservacionRepository,
	cr *repositories.AgenciaConfiguracionRepository,
	logSesion *LogSesionService,
) *PagoService {
	return &PagoService{repo: repo, reservaRepo: rr, configRepo: cr, logSesion: logSesion}
}

// ProcesarPago
//
// Ejecuta el flujo completo de pago de una reservacion: valida los datos de la
// tarjeta, verifica que la reserva pertenezca al usuario y este pendiente,
// obtiene el no_reservacion para auditoria, valida la integridad de los detalles
// segun el tipo de reserva, notifica a cada proveedor externo y finalmente
// confirma la reserva en la base de datos. Si el tipo de reserva es paquete (3),
// aplica el porcentaje de descuento configurado en Agencia_Configuracion sobre
// el total final. El descuento es absorbido por la agencia, no por los proveedores.
//
// Parametros:
//   - usuarioID: identificador del usuario que realiza el pago
//   - req: datos del pago incluyendo numero de tarjeta, CVV, NIT y codigo postal
//
// Retorna:
//   - noReservacion: numero de reservacion legible para incluir en el log de auditoria
//   - error: error si la tarjeta es invalida, la reserva no existe o ya fue pagada,
//     si los detalles no cumplen la estructura del tipo de reserva,
//     o si algun proveedor rechaza el pago
func (s *PagoService) ProcesarPago(c *gin.Context, usuarioID int, req dto.PagoReservacionRequest) (noReservacion string, err error) {
	// 1. Validar tarjeta
	if len(req.TarjetaNumero) < 16 {
		return "", errors.New("número de tarjeta inválido")
	}
	if len(req.TarjetaCVV) != 3 {
		return "", errors.New("CVV inválido")
	}

	// 2. Verificar existencia, dueño y estado pendiente
	tipoReserva, total, err := s.repo.ObtenerReservaParaPago(req.ReservacionID, usuarioID)
	if err != nil {
		return "", errors.New("reservación no encontrada, ya pagada o no le pertenece")
	}

	// 2b. Obtener no_reservacion para auditoria
	noReservacion, err = s.repo.ObtenerNoReservacion(req.ReservacionID)
	if err != nil {
		return "", fmt.Errorf("error obteniendo número de reservación: %w", err)
	}

	// 3. Validar integridad de detalles (1=Vuelo, 2=Hotel, 3=Paquete)
	vuelos, hoteles, _ := s.repo.ContarDetallesPorTipo(req.ReservacionID)

	switch tipoReserva {
	case 1: // Aerolínea
		if vuelos != 1 || hoteles != 0 {
			return "", errors.New("la reserva debe tener exactamente 1 vuelo")
		}
	case 2: // Hotelera
		if hoteles != 1 || vuelos != 0 {
			return "", errors.New("la reserva debe tener exactamente 1 hotel")
		}
	case 3: // Paquete
		if vuelos != 1 || hoteles != 1 {
			return "", errors.New("el paquete debe tener exactamente 1 vuelo y 1 hotel")
		}
	}

	// 4. Leer descuento de paquete si aplica (tipo 3)
	var porcentajeDescuento float64
	if tipoReserva == 3 {
		porcentajeDescuento, err = s.configRepo.ObtenerPorcentajeDescuento()
		if err != nil {
			// Si falla la lectura del descuento, continuar sin descuento
			porcentajeDescuento = 0
		}
	}

	// 5. Notificar a proveedores
	uid := usuarioID
	detalles, _ := s.reservaRepo.ObtenerDetallesDeReservacion(req.ReservacionID)
	for _, d := range detalles {
		if err := s.notificarProveedor(c, &uid, d, req.Nit, req.CodigoPostal); err != nil {
			return "", fmt.Errorf("error al confirmar con proveedor: %w", err)
		}
	}

	// 6. Finalizar en BD agencia (estado 2, descuento aplicado y factura)
	return noReservacion, s.repo.ConfirmarReservaYFacturar(req.ReservacionID, total, req.Nit, req.CodigoPostal, porcentajeDescuento)
}

// notificarProveedor
//
// Envia una solicitud HTTP POST al endpoint del proveedor externo para confirmar
// el pago de un detalle de reservacion. Construye la URL segun si el detalle
// corresponde a una aerolinea (tipo 1) o a un hotel (tipo 2).
//
// Parametros:
//   - d: detalle del proveedor con la URL de la API, el ID de reserva en el proveedor y el token de acceso
//   - nit: NIT del cliente para la facturacion
//   - cp: codigo postal del cliente para la facturacion
//
// Retorna:
//   - error: error si la solicitud falla o el proveedor responde con un codigo HTTP 400 o superior
func (s *PagoService) notificarProveedor(c *gin.Context, usuarioID *int, d dto.DetalleProveedor, nit, cp string) error {
	// TODO: agregar timeout al http.DefaultClient (deuda técnica identificada)
	body, _ := json.Marshal(dto.PagoProveedorBody{Nit: nit, CodigoPostal: cp})

	nombreProv := "Broom"
	if d.TipoDetalleID == 2 {
		nombreProv = "Miku"
	}

	var url string
	if d.TipoDetalleID == 1 {
		url = fmt.Sprintf("%s/api/reservaciones-agencia/%s/confirmar", d.URLAPI, d.IDReservaProveedor)
	} else {
		url = fmt.Sprintf("%s/agencia/reservaciones/%s/pago", d.URLAPI, d.IDReservaProveedor)
	}

	req, _ := http.NewRequest("POST", url, bytes.NewBuffer(body))
	req.Header.Set("X-Agencia-Token", d.TokenEntrada)
	req.Header.Set("Content-Type", "application/json")

	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		s.logSesion.Registrar(c, helpers.TipoOutPagoProveedorFallido, usuarioID, "pago-proveedor",
			fmt.Sprintf("%s status=ERR reservaId=%s msg='%s'", nombreProv, d.IDReservaProveedor, err.Error()))
		return errors.New("el proveedor rechazó el pago")
	}
	if resp.StatusCode >= 400 {
		msg := fmt.Sprintf("%s status=%d reservaId=%s msg='%s'",
			nombreProv, resp.StatusCode, d.IDReservaProveedor, helpers.ParseErrorProveedor(resp))
		s.logSesion.Registrar(c, helpers.TipoOutPagoProveedorFallido, usuarioID, "pago-proveedor", msg)
		return errors.New("el proveedor rechazó el pago")
	}

	s.logSesion.Registrar(c, helpers.TipoOutPagoProveedorExitoso, usuarioID, "pago-proveedor",
		fmt.Sprintf("%s: pago confirmado reservaId=%s", nombreProv, d.IDReservaProveedor))

	return nil
}