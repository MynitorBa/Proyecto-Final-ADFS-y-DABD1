package services

import (
	"agencia-viajes/internal/repositories"
)

// NotificacionesService
//
// Servicio que expone las operaciones de notificaciones disponibles
// para el usuario autenticado. Deliberadamente delgado: toda la
// logica de acceso y validacion de pertenencia vive en el repositorio.
type NotificacionesService struct {
	repo *repositories.NotificacionesRepository
}

// NewNotificacionesService
//
// Crea e inicializa una nueva instancia del servicio.
//
// Parametros:
//   - repo: repositorio de notificaciones ya inicializado
//
// Retorna:
//   - *NotificacionesService: instancia lista para usar
func NewNotificacionesService(repo *repositories.NotificacionesRepository) *NotificacionesService {
	return &NotificacionesService{repo: repo}
}

// ObtenerTodas
//
// Retorna todas las notificaciones del usuario ordenadas de mas
// reciente a mas antigua.
//
// Parametros:
//   - usuarioID: ID del usuario autenticado
//
// Retorna:
//   - []repositories.NotificacionDTO: slice con todas las notificaciones
//   - error: si falla la consulta de BD
func (s *NotificacionesService) ObtenerTodas(usuarioID int) ([]repositories.NotificacionDTO, error) {
	return s.repo.ObtenerNotificacionesDeUsuario(usuarioID)
}

// MarcarComoLeida
//
// Marca una notificacion especifica como leida, validando que
// pertenezca al usuario autenticado.
//
// Parametros:
//   - notificacionID: ID de la notificacion a marcar
//   - usuarioID:      ID del usuario autenticado
//
// Retorna:
//   - error: si la notificacion no existe, no pertenece al usuario,
//     ya estaba leida, o falla la BD
func (s *NotificacionesService) MarcarComoLeida(notificacionID, usuarioID int) error {
	return s.repo.MarcarComoLeida(notificacionID, usuarioID)
}