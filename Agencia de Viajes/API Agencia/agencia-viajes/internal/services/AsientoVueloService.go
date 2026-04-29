// # Package services
//
// Servicios de negocio de la agencia de viajes. Este paquete contiene la logica
// central para reservaciones, busquedas, autenticacion, catalogos y comunicacion
// con proveedores externos (aerolineas y hoteleras).
package services

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/helpers"
	"agencia-viajes/internal/repositories"
	"database/sql"
	"encoding/json"
	"errors"
	"fmt"
	"net/http"

	"github.com/gin-gonic/gin"

	"bytes"
)

// AsientoVueloService
//
// Servicio encargado de gestionar los asientos de vuelo asociados a reservaciones
// de aerolinea. Permite consultar los asientos disponibles y realizar cambios
// de asiento para boletos especificos de una reservacion.
type AsientoVueloService struct {
	repo      *repositories.DetalleReservacionRepository
	logSesion *LogSesionService
}

// NewAsientoVueloService
//
// Crea e inicializa una nueva instancia de AsientoVueloService con su repositorio
// de detalle de reservacion.
//
// Parametros:
//   - db:        conexion activa a la base de datos SQL
//   - logSesion: servicio de auditoria para registrar eventos REST salientes
//
// Retorna:
//   - *AsientoVueloService: instancia lista para usar
func NewAsientoVueloService(db *sql.DB, logSesion *LogSesionService) *AsientoVueloService {
	return &AsientoVueloService{
		repo:      repositories.NewDetalleReservacionRepository(db),
		logSesion: logSesion,
	}
}

// ObtenerAsientosVuelo
//
// Consulta los asientos de vuelo disponibles para una reservacion especifica,
// obteniendo primero los datos de conexion del proveedor aerolinea desde la BD
// y luego llamando a su API externa.
//
// Parametros:
//   - usuarioID: identificador del usuario dueno de la reservacion
//   - req: datos de la solicitud incluyendo ReservacionID y ProveedorID
//
// Retorna:
//   - *dto.AsientosVueloResponse: lista de vuelos con sus boletos y asientos
//   - error: si la reservacion no existe, no pertenece al usuario o falla la API del proveedor
func (s *AsientoVueloService) ObtenerAsientosVuelo(
	c *gin.Context,
	usuarioID int,
	req dto.ObtenerAsientosVueloRequest,
) (*dto.AsientosVueloResponse, error) {

	idReservaAerolinea, urlAPI, token, err := s.repo.ObtenerDetalleAerolineaPorProveedor(
		req.ReservacionID, usuarioID, req.ProveedorID,
	)
	if err != nil {
		return nil, err
	}

	uid := usuarioID
	return s.llamarGetAsientos(c, &uid, urlAPI, token, idReservaAerolinea)
}

// llamarGetAsientos
//
// Realiza la llamada HTTP GET al endpoint de asientos de la aerolinea proveedora,
// usando el token de autenticacion de agencia para obtener los asientos
// asociados a la reservacion en el sistema externo.
//
// Parametros:
//   - urlAPI: URL base del API del proveedor aerolinea
//   - token: token de autenticacion de agencia (X-Agencia-Token)
//   - idReservaProveedor: identificador de la reservacion en el sistema del proveedor
//
// Retorna:
//   - *dto.AsientosVueloResponse: respuesta con la lista de vuelos y sus asientos
//   - error: si la peticion HTTP falla o la respuesta tiene formato incompatible
//
// llamarGetAsientos realiza el GET al proveedor y registra eventos 56/57 (Flujo D).
// Solo debe llamarse desde ObtenerAsientosVuelo.
func (s *AsientoVueloService) llamarGetAsientos(
	c *gin.Context,
	usuarioID *int,
	urlAPI, token, idReservaProveedor string,
) (*dto.AsientosVueloResponse, error) {
	// TODO: agregar timeout al http.DefaultClient (deuda técnica identificada)
	resultado, err := s.llamarGetAsientosInterno(urlAPI, token, idReservaProveedor)
	if err != nil {
		s.logSesion.Registrar(c, helpers.TipoOutAsientosCargarFallida, usuarioID, "asientos-cargar",
			fmt.Sprintf("Broom status=ERR msg='%s'", err.Error()))
		return nil, err
	}
	s.logSesion.Registrar(c, helpers.TipoOutAsientosCargarExitosa, usuarioID, "asientos-cargar",
		fmt.Sprintf("Broom: mapa cargado para reservaId=%s", idReservaProveedor))
	return resultado, nil
}

// llamarGetAsientosInterno realiza el GET al proveedor sin registrar eventos de auditoría.
// Usado internamente por CambiarAsientoVuelo para validar la pertenencia del boleto.
func (s *AsientoVueloService) llamarGetAsientosInterno(
	urlAPI, token, idReservaProveedor string,
) (*dto.AsientosVueloResponse, error) {

	url := fmt.Sprintf("%s/api/asientos-agencia/reservacion/%s", urlAPI, idReservaProveedor)

	req, err := http.NewRequest(http.MethodGet, url, nil)
	if err != nil {
		return nil, err
	}
	req.Header.Set("X-Agencia-Token", token)

	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("error contactando aerolínea: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("aerolínea respondió con status %d", resp.StatusCode)
	}

	var resultado dto.AsientosVueloResponse
	if err := json.NewDecoder(resp.Body).Decode(&resultado); err != nil {
		return nil, errors.New("el formato de respuesta de la aerolínea es incompatible")
	}
	return &resultado, nil
}

// CambiarAsientoVuelo — pegar después de ObtenerAsientosVuelo
func (s *AsientoVueloService) CambiarAsientoVuelo(
	c *gin.Context,
	usuarioID int,
	req dto.CambiarAsientoVueloRequest,
) error {

	idReservaAerolinea, urlAPI, token, err := s.repo.ObtenerDetalleAerolineaPorProveedor(
		req.ReservacionID, usuarioID, req.ProveedorID,
	)
	if err != nil {
		return err
	}

	uid := usuarioID
	asientos, err := s.llamarGetAsientosInterno(urlAPI, token, idReservaAerolinea)
	if err != nil {
		return err
	}

	boletoValido := false
	for _, vuelo := range *asientos {
		for _, b := range vuelo.BoletosAgencia {
			if b.BoletoID == req.BoletoID {
				boletoValido = true
				break
			}
		}
		if boletoValido {
			break
		}
	}

	if !boletoValido {
		return errors.New("el boleto no pertenece a esta reservación")
	}

	return s.llamarCambiarAsiento(c, &uid, urlAPI, token, req.BoletoID, req.NuevoAsiento)
}

// llamarCambiarAsiento
func (s *AsientoVueloService) llamarCambiarAsiento(
	c *gin.Context,
	usuarioID *int,
	urlAPI, token string,
	boletoID int,
	nuevoAsiento string,
) error {
	bodyReq := dto.CambiarAsientoAerolineaBody{NuevoAsiento: nuevoAsiento}
	bodyBytes, err := json.Marshal(bodyReq)
	if err != nil {
		return fmt.Errorf("error serializando request: %w", err)
	}

	url := fmt.Sprintf("%s/api/asientos-agencia/%d", urlAPI, boletoID)

	req, err := http.NewRequest(http.MethodPut, url, bytes.NewBuffer(bodyBytes))
	if err != nil {
		return err
	}
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("X-Agencia-Token", token)

	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		s.logSesion.Registrar(c, helpers.TipoOutAsientoCambiarFallida, usuarioID, "asiento-cambiar",
			fmt.Sprintf("Broom status=ERR boletoId=%d msg='%s'", boletoID, err.Error()))
		return fmt.Errorf("error contactando aerolínea: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK && resp.StatusCode != http.StatusNoContent {
		msg := fmt.Sprintf("Broom status=%d boletoId=%d msg='%s'", resp.StatusCode, boletoID, helpers.ParseErrorProveedor(resp))
		s.logSesion.Registrar(c, helpers.TipoOutAsientoCambiarFallida, usuarioID, "asiento-cambiar", msg)
		return fmt.Errorf("aerolínea respondió con status %d", resp.StatusCode)
	}

	s.logSesion.Registrar(c, helpers.TipoOutAsientoCambiarExitosa, usuarioID, "asiento-cambiar",
		fmt.Sprintf("Broom: asiento cambiado boletoId=%d → %s", boletoID, nuevoAsiento))

	return nil
}
