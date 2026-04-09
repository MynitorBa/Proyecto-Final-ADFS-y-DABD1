// # Package services
//
// Servicios de negocio de la agencia de viajes. Este paquete contiene la logica
// central para reservaciones, busquedas, autenticacion, catalogos y comunicacion
// con proveedores externos (aerolineas y hoteleras).
package services

import (
	"log"
	"time"
)

// CatalogoSchedulerService
//
// Servicio encargado de actualizar automaticamente el catalogo de la agencia
// cada 7 dias en segundo plano. Utiliza el CatalogoService para ejecutar
// la sincronizacion con todos los proveedores activos registrados en BD.
// El proceso puede detenerse de forma controlada mediante Detener.
type CatalogoSchedulerService struct {
	service *CatalogoService
	stopCh  chan struct{}
}

// NewCatalogoSchedulerService
//
// Crea e inicializa una nueva instancia de CatalogoSchedulerService con el servicio
// de catalogo necesario para ejecutar la actualizacion y el canal de control
// para detener el proceso en segundo plano.
//
// Parametros:
//   - service: instancia del servicio de catalogo a utilizar en cada ciclo
//
// Retorna:
//   - *CatalogoSchedulerService: instancia lista para usar, aun no iniciada
func NewCatalogoSchedulerService(service *CatalogoService) *CatalogoSchedulerService {
	return &CatalogoSchedulerService{
		service: service,
		stopCh:  make(chan struct{}),
	}
}

// Iniciar
//
// Lanza en segundo plano una goroutine que actualiza el catalogo completo
// de la agencia cada 7 dias llamando al CatalogoService. El proceso puede
// detenerse llamando a Detener.
//
// Notas:
//   - Registra en log cada actualizacion completada, el numero de proveedores
//     procesados o cualquier error ocurrido durante el ciclo
func (s *CatalogoSchedulerService) Iniciar() {
	go func() {
		ticker := time.NewTicker(7 * 24 * time.Hour)
		defer ticker.Stop()
		for {
			select {
			case <-ticker.C:
				// 1. Ejecutar actualizacion completa del catalogo
				resultados, err := s.service.ActualizarCatalogo()
				if err != nil {
					log.Println("[CATALOGO] Error en actualización automática:", err)
				} else {
					log.Printf("[CATALOGO] Actualización automática completada: %d proveedores procesados", len(resultados))
				}
			case <-s.stopCh:
				log.Println("[CATALOGO] Scheduler detenido")
				return
			}
		}
	}()
}

// Detener
//
// Detiene el proceso de actualizacion automatica en segundo plano cerrando
// el canal de control. Debe llamarse al apagar la aplicacion para liberar
// la goroutine.
func (s *CatalogoSchedulerService) Detener() {
	close(s.stopCh)
}