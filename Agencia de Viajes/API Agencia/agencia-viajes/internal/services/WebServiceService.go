// # Package services
//
// Servicios de negocio de la agencia de viajes. Cada servicio
// encapsula la logica de aplicacion y delega el acceso a datos
// al repositorio correspondiente.
package services

import "agencia-viajes/internal/repositories"

// WebServiceService
//
// Servicio que encapsula la logica de negocio del panel operacional
// del WebService. Delega todas las consultas al WebServiceRepository.
type WebServiceService struct {
	repo *repositories.WebServiceRepository
}

// NewWebServiceService
//
// Crea e inicializa una nueva instancia de WebServiceService.
//
// Parametros:
//   - repo: instancia del repositorio WebService
//
// Retorna:
//   - *WebServiceService: instancia lista para usar
func NewWebServiceService(repo *repositories.WebServiceRepository) *WebServiceService {
	return &WebServiceService{repo: repo}
}

// ObtenerEstado
//
// Retorna el estado operacional del WebService: lista de proveedores
// con su flag de handshake configurado y conteo de eventos recientes
// de handshake y actualizacion de catalogo.
//
// Retorna:
//   - []repositories.ProveedorWS: proveedores con flag handshake_configurado
//   - repositories.EventosWS:     conteo de eventos por tipo
//   - error: si alguna consulta de BD falla
func (s *WebServiceService) ObtenerEstado() ([]repositories.ProveedorWS, repositories.EventosWS, error) {
	proveedores, err := s.repo.ObtenerProveedores()
	if err != nil {
		return nil, repositories.EventosWS{}, err
	}

	eventos, err := s.repo.ObtenerEventosRecientes()
	if err != nil {
		return nil, repositories.EventosWS{}, err
	}

	return proveedores, eventos, nil
}

// ObtenerNotificaciones
//
// Retorna las ultimas 50 notificaciones generadas por proveedores
// en todas las reservaciones del sistema, sin filtrar por usuario.
//
// Retorna:
//   - []repositories.NotificacionDTO: notificaciones ordenadas por fecha DESC
//   - error: si la consulta de BD falla
func (s *WebServiceService) ObtenerNotificaciones() ([]repositories.NotificacionDTO, error) {
	return s.repo.ObtenerNotificaciones()
}
