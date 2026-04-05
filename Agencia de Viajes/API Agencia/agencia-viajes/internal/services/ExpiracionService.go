// # Package services
//
// Servicios de negocio de la agencia de viajes. Este paquete contiene la logica
// central para reservaciones, busquedas, autenticacion, catalogos y comunicacion
// con proveedores externos (aerolineas y hoteleras).
package services

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/repositories"
	"bytes"
	"database/sql"
	"fmt"
	"log"
	"net/http"
	"time"
)

// ExpiracionService
//
// Servicio encargado de expirar automaticamente las reservaciones pendientes
// que han superado su tiempo limite. Ejecuta una revision periodica en segundo
// plano cada minuto y tambien permite expirar manualmente las reservaciones
// pendientes de un usuario especifico. Notifica a cada proveedor externo
// antes de actualizar el estado en la base de datos local.
type ExpiracionService struct {
	repo   *repositories.ReservacionRepository
	stopCh chan struct{}
}

// NewExpiracionService
//
// Crea e inicializa una nueva instancia de ExpiracionService con su repositorio
// de reservaciones y el canal de control para detener el proceso en segundo plano.
//
// Parametros:
//   - db: conexion activa a la base de datos SQL
//
// Retorna:
//   - *ExpiracionService: instancia lista para usar, aun no iniciada
func NewExpiracionService(db *sql.DB) *ExpiracionService {
	return &ExpiracionService{
		repo:   repositories.NewReservacionRepository(db),
		stopCh: make(chan struct{}),
	}
}

// Iniciar
//
// Lanza en segundo plano una goroutine que revisa y expira reservaciones
// pendientes vencidas cada minuto. El proceso puede detenerse llamando a Detener.
//
// Notas:
//   - Registra en log cada revision completada o error ocurrido
func (s *ExpiracionService) Iniciar() {
	go func() {
		ticker := time.NewTicker(1 * time.Minute)
		defer ticker.Stop()
		for {
			select {
			case <-ticker.C:
				if err := s.expirarPendientes(); err != nil {
					log.Println("[EXPIRACION] Error:", err)
				} else {
					log.Println("[EXPIRACION] Revisión completada")
				}
			case <-s.stopCh:
				log.Println("[EXPIRACION] Servicio detenido")
				return
			}
		}
	}()
}

// Detener
//
// Detiene el proceso de expiracion en segundo plano cerrando el canal de control.
// Debe llamarse al apagar la aplicacion para liberar la goroutine.
func (s *ExpiracionService) Detener() {
	close(s.stopCh)
}

// ExpirarReservacionesDeUsuario
//
// Expira todas las reservaciones pendientes de un usuario especifico que
// hayan superado su tiempo limite. Itera sobre las reservaciones obtenidas
// y llama a expirarUna para cada una, registrando en log los errores
// individuales sin interrumpir el proceso.
//
// Parametros:
//   - usuarioID: identificador del usuario cuyas reservaciones se deben revisar
//
// Retorna:
//   - error: si falla la consulta de reservaciones pendientes del usuario en BD
func (s *ExpiracionService) ExpirarReservacionesDeUsuario(usuarioID int) error {
	pendientes, err := s.repo.ObtenerPendientesConDetalles(usuarioID)
	if err != nil {
		return err
	}
	for _, res := range pendientes {
		if err := s.expirarUna(res.ID, res.Detalles); err != nil {
			log.Printf("[EXPIRACION] Error expirando reservacion %d del usuario %d: %v", res.ID, usuarioID, err)
		}
	}
	return nil
}

// expirarPendientes
//
// Obtiene todos los IDs de reservaciones pendientes expiradas a nivel global
// y llama a expirarUna para cada una. Los errores individuales se registran
// en log sin interrumpir el proceso.
//
// Retorna:
//   - error: si falla la consulta de IDs pendientes expirados en BD
func (s *ExpiracionService) expirarPendientes() error {
	ids, err := s.repo.ObtenerIDsPendientesExpirados()
	if err != nil {
		return err
	}
	for _, id := range ids {
		detalles, err := s.repo.ObtenerDetallesDeReservacion(id)
		if err != nil {
			log.Printf("[EXPIRACION] Error obteniendo detalles de reservacion %d: %v", id, err)
			continue
		}
		if err := s.expirarUna(id, detalles); err != nil {
			log.Printf("[EXPIRACION] Error expirando reservacion %d: %v", id, err)
		}
	}
	return nil
}

// expirarUna
//
// Expira una reservacion especifica notificando a cada proveedor externo
// involucrado y luego actualizando el estado de los detalles y la reservacion
// en la base de datos local. Los errores al notificar proveedores se registran
// en log pero no detienen el proceso de expiracion en BD.
//
// Parametros:
//   - reservacionID: identificador de la reservacion a expirar
//   - detalles: lista de detalles con informacion de cada proveedor involucrado
//
// Retorna:
//   - error: si falla la actualizacion de detalles o de la reservacion en BD
func (s *ExpiracionService) expirarUna(reservacionID int, detalles []dto.DetalleProveedor) error {
	for _, d := range detalles {
		if err := s.llamarExpirarProveedor(d.URLAPI, d.TokenEntrada, d.IDReservaProveedor, d.TipoDetalleID); err != nil {
			log.Printf("[EXPIRACION] Error expirando en proveedor %d reserva %s: %v", d.ProveedorID, d.IDReservaProveedor, err)
		}
	}

	if err := s.repo.ExpirarDetalles(reservacionID); err != nil {
		log.Printf("[EXPIRACION] Error expirando detalles de reservacion %d: %v", reservacionID, err)
		return err
	}

	if err := s.repo.ExpirarReservacion(reservacionID); err != nil {
		log.Printf("[EXPIRACION] Error expirando reservacion %d: %v", reservacionID, err)
		return err
	}

	return nil
}

// llamarExpirarProveedor
//
// Realiza la llamada HTTP POST al endpoint de expiracion del proveedor externo
// correspondiente segun el tipo de detalle. Construye la URL correcta para
// aerolineas o hoteleras y envia el token de autenticacion.
//
// Parametros:
//   - urlAPI: URL base del API del proveedor
//   - token: token de autenticacion de agencia (X-Agencia-Token)
//   - idReservaProveedor: identificador de la reservacion en el sistema del proveedor
//   - tipoDetalleID: tipo de proveedor (1=aerolinea, 2=hotelera)
//
// Retorna:
//   - error: si el tipo es desconocido, falla la peticion o el proveedor retorna error HTTP
func (s *ExpiracionService) llamarExpirarProveedor(urlAPI, token, idReservaProveedor string, tipoDetalleID int) error {
	var url string
	switch tipoDetalleID {
	case TipoDetalleVuelo:
		url = fmt.Sprintf("%s/api/reservaciones-agencia/%s/expirar", urlAPI, idReservaProveedor)
	case TipoDetalleHotel:
		url = fmt.Sprintf("%s/agencia/reservaciones/%s/expirar", urlAPI, idReservaProveedor)
	default:
		return fmt.Errorf("tipo de detalle desconocido: %d", tipoDetalleID)
	}

	req, err := http.NewRequest(http.MethodPost, url, bytes.NewBuffer(nil))
	if err != nil {
		return err
	}
	req.Header.Set("X-Agencia-Token", token)

	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		return err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("proveedor respondió con status %d", resp.StatusCode)
	}
	return nil
}
