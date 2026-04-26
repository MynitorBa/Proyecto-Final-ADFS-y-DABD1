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
	"bytes"
	"database/sql"
	"encoding/json"
	"errors"
	"fmt"
	"math"
	"net/http"
	"strconv"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
)

const (
	TipoDetalleVuelo = 1
	TipoDetalleHotel = 2

	TipoReservaAerolinea = 1
	TipoReservaHotelera  = 2
	TipoReservaPaquete   = 3
)

// DetalleReservacionService
//
// Servicio encargado de agregar detalles de vuelo y hotel a reservaciones
// existentes en estado pendiente. Coordina la reserva con el proveedor externo,
// aplica el margen de ganancia configurado, almacena el detalle en BD y
// recalcula el total de la reservacion. Tambien gestiona el alta de pasajeros
// en el sistema de la aerolinea.
type DetalleReservacionService struct {
	repo      *repositories.DetalleReservacionRepository
	logSesion *LogSesionService
}

// NewDetalleReservacionService
//
// Crea e inicializa una nueva instancia de DetalleReservacionService con su
// repositorio de detalle de reservacion.
//
// Parametros:
//   - db:        conexion activa a la base de datos SQL
//   - logSesion: servicio de auditoria para registrar eventos REST salientes
//
// Retorna:
//   - *DetalleReservacionService: instancia lista para usar
func NewDetalleReservacionService(db *sql.DB, logSesion *LogSesionService) *DetalleReservacionService {
	return &DetalleReservacionService{
		repo:      repositories.NewDetalleReservacionRepository(db),
		logSesion: logSesion,
	}
}

// AgregarDetalleVuelo
//
// Agrega un detalle de vuelo a una reservacion existente. Valida que la
// reservacion pertenezca al usuario, este en estado pendiente y no sea
// exclusivamente de tipo hotelera. Llama al proveedor aerolinea para crear
// la reserva, calcula el precio con ganancia por boleto y guarda el detalle
// en BD, actualizando el total de la reservacion.
//
// Parametros:
//   - usuarioID: identificador del usuario dueno de la reservacion
//   - req: datos del detalle incluyendo ReservacionID, ProveedorID y lista de vuelos
//
// Retorna:
//   - interface{}: mapa con mensaje, IDs, total base, total con ganancia y detalle del proveedor
//   - error: si la reservacion no existe, no es valida, falla el proveedor o la BD
func (s *DetalleReservacionService) AgregarDetalleVuelo(c *gin.Context, usuarioID int, req dto.AgregarDetalleVueloRequest) (interface{}, error) {

	reservacion, err := s.repo.ObtenerReservacionParaDetalle(req.ReservacionID, usuarioID)
	if err != nil {
		return nil, err
	}
	if reservacion == nil {
		return nil, errors.New("reservación no encontrada o no pertenece al usuario")
	}
	if reservacion.EstadoID != 1 {
		return nil, errors.New("la reservación no está en estado pendiente")
	}
	if reservacion.TipoReservaID == TipoReservaHotelera {
		return nil, errors.New("esta reservación es solo de tipo hotelera, no admite vuelos")
	}

	urlAPI, tokenEntrada, porcentajeGanancia, err := s.repo.ObtenerDatosProveedorPorTipo(req.ProveedorID, TipoDetalleVuelo)
	if err != nil {
		return nil, err
	}

	uid := usuarioID
	respAerolinea, err := s.llamarReservacionAerolinea(c, &uid, urlAPI, tokenEntrada, req.Vuelos)
	if err != nil {
		return nil, fmt.Errorf("error al reservar en aerolínea: %w", err)
	}

	// Calcular totalConGanancia por grupo de dirección (ida / regreso) para que el resultado
	// coincida exactamente con lo que la búsqueda mostró al usuario.
	// La búsqueda aplica el markup a precioTuristaTotal de cada dirección por separado:
	//   round(precioTuristaTotal_ida * mult) + round(precioTuristaTotal_regreso * mult)
	// Si aplicamos el markup al total combinado el orden de redondeo puede dar ±0.01.
	// Solución: el frontend etiqueta cada vuelo con grupoId (0=ida, 1=regreso).
	// Aquí sumamos los precios de boletos de Broom por grupo → obtenemos precioTuristaTotal
	// de cada dirección → aplicamos markup por grupo → sumamos.

	// Extraer precio por vueloId desde los boletos de la respuesta de Broom
	boletosPorVuelo := map[string]float64{} // vueloId (string) → precio por boleto (= round(precio*factor))
	if boletosList, ok := respAerolinea["boletos"].([]interface{}); ok {
		for _, b := range boletosList {
			if boleto, ok := b.(map[string]interface{}); ok {
				numVuelo, _ := boleto["numeroVuelo"].(string)
				precio, _ := boleto["precio"].(float64)
				if numVuelo != "" {
					boletosPorVuelo[numVuelo] = precio
				}
			}
		}
	}

	// Agrupar vuelos por grupoId
	grupoVuelos := map[int][]dto.SeleccionVuelo{}
	for _, v := range req.Vuelos {
		grupoVuelos[v.GrupoID] = append(grupoVuelos[v.GrupoID], v)
	}

	multiplicador := 1 + porcentajeGanancia/100
	var totalConGanancia float64

	for _, vuelosGrupo := range grupoVuelos {
		cantPax := vuelosGrupo[0].CantidadPasajeros
		if cantPax <= 0 {
			cantPax = 1
		}
		// Sumar precios de boletos de este grupo → totalBaseGrupo = precioTuristaTotal_direccion * cantPax
		var totalBaseGrupo float64
		for _, v := range vuelosGrupo {
			vueloIdStr := strconv.Itoa(v.VueloId)
			if precio, ok := boletosPorVuelo[vueloIdStr]; ok {
				totalBaseGrupo += precio * float64(cantPax)
			}
		}
		if totalBaseGrupo == 0 {
			// Fallback: usar proporción del total global si no hay boletos detallados
			totalBaseGrupo = respAerolinea["total"].(float64) / float64(len(grupoVuelos))
		}
		// precioPersonaGrupo = precioTuristaTotal_direccion (mismo valor que usó aplicarGanancia en búsqueda)
		precioPersonaGrupo := totalBaseGrupo / float64(cantPax)
		precioPersonaConGanancia := math.Round(precioPersonaGrupo*multiplicador*100) / 100
		totalConGanancia += math.Round(precioPersonaConGanancia*float64(cantPax)*100) / 100
	}
	totalConGanancia = math.Round(totalConGanancia*100) / 100

	idReservaProveedor := fmt.Sprintf("%v", respAerolinea["reservacionId"])
	err = s.repo.InsertarDetalle(
		req.ReservacionID,
		req.ProveedorID,
		TipoDetalleVuelo,
		idReservaProveedor,
		totalConGanancia,
		respAerolinea,
	)
	if err != nil {
		return nil, errors.New("error guardando detalle de reservación")
	}

	err = s.repo.RecalcularTotalReservacion(req.ReservacionID)
	if err != nil {
		return nil, errors.New("error recalculando total de reservación")
	}

	return map[string]interface{}{
		"mensaje":            "detalle de vuelo agregado exitosamente",
		"reservacion_id":     req.ReservacionID,
		"total_base":         respAerolinea["total"],
		"total_con_ganancia": totalConGanancia,
		"detalle":            respAerolinea,
	}, nil
}

// AgregarDetalleHotel
//
// Agrega un detalle de hotel a una reservacion existente. Valida que la
// reservacion pertenezca al usuario, este en estado pendiente y no sea
// exclusivamente de tipo aerea. Llama al proveedor hotelera para crear
// la reserva, calcula el precio con ganancia por noche (con soporte para
// personas extra) y guarda el detalle en BD, actualizando el total.
//
// Parametros:
//   - usuarioID: identificador del usuario dueno de la reservacion
//   - req: datos del detalle incluyendo ReservacionID, ProveedorID y lista de habitaciones
//
// Retorna:
//   - interface{}: mapa con mensaje, IDs, total base, total con ganancia y detalle del proveedor
//   - error: si la reservacion no existe, no es valida, falla el proveedor o la BD
func (s *DetalleReservacionService) AgregarDetalleHotel(c *gin.Context, usuarioID int, req dto.AgregarDetalleHotelRequest) (interface{}, error) {

	reservacion, err := s.repo.ObtenerReservacionParaDetalle(req.ReservacionID, usuarioID)
	if err != nil {
		return nil, err
	}
	if reservacion == nil {
		return nil, errors.New("reservación no encontrada o no pertenece al usuario")
	}
	if reservacion.EstadoID != 1 {
		return nil, errors.New("la reservación no está en estado pendiente")
	}
	if reservacion.TipoReservaID == TipoReservaAerolinea {
		return nil, errors.New("esta reservación es solo de tipo aérea, no admite hoteles")
	}

	urlAPI, tokenEntrada, porcentajeGanancia, err := s.repo.ObtenerDatosProveedorPorTipo(req.ProveedorID, TipoDetalleHotel)
	if err != nil {
		return nil, err
	}

	uid := usuarioID
	respHotel, err := s.llamarReservacionHotel(c, &uid, urlAPI, tokenEntrada, req.Habitaciones)
	if err != nil {
		return nil, fmt.Errorf("error al reservar en hotelera: %w", err)
	}

	totalBase := respHotel["total"].(float64)
	var totalConGanancia float64

	if habitaciones, ok := respHotel["habitaciones"].([]interface{}); ok && len(habitaciones) > 0 {
		for _, h := range habitaciones {
			if hab, ok := h.(map[string]interface{}); ok {
				precio, okP := hab["precioPorNoche"].(float64)
				noches, okN := hab["noches"].(float64)
				precioPorPersona, _ := hab["precioPorPersona"].(float64)
				personasExtra, _ := hab["personasExtra"].(float64)
				if okP && okN && noches > 0 {
					precioNocheMarkup := math.Round(precio*(1+porcentajeGanancia/100)*100) / 100
					precioPersonaMarkup := math.Round(precioPorPersona*(1+porcentajeGanancia/100)*100) / 100
					totalConGanancia += (precioNocheMarkup + personasExtra*precioPersonaMarkup) * noches
				}
			}
		}
		totalConGanancia = math.Round(totalConGanancia*100) / 100
	} else {
		var totalNoches int
		for _, hab := range req.Habitaciones {
			checkIn, errCI := time.Parse("2006-01-02", hab.FechaCheckIn)
			checkOut, errCO := time.Parse("2006-01-02", hab.FechaCheckOut)
			if errCI == nil && errCO == nil {
				n := int(checkOut.Sub(checkIn).Hours() / 24)
				if n > 0 {
					totalNoches += n
				}
			}
		}
		if totalNoches > 0 {
			precioBaseNoche := totalBase / float64(totalNoches)
			precioNocheConGanancia := math.Round(precioBaseNoche*(1+porcentajeGanancia/100)*100) / 100
			totalConGanancia = math.Round(precioNocheConGanancia*float64(totalNoches)*100) / 100
		} else {
			totalConGanancia = math.Round(totalBase*(1+porcentajeGanancia/100)*100) / 100
		}
	}

	idReservaProveedor := fmt.Sprintf("%v", respHotel["id"])
	err = s.repo.InsertarDetalle(
		req.ReservacionID,
		req.ProveedorID,
		TipoDetalleHotel,
		idReservaProveedor,
		totalConGanancia,
		respHotel,
	)
	if err != nil {
		return nil, errors.New("error guardando detalle de reservación")
	}

	err = s.repo.RecalcularTotalReservacion(req.ReservacionID)
	if err != nil {
		return nil, errors.New("error recalculando total de reservación")
	}

	return map[string]interface{}{
		"mensaje":            "detalle de hotel agregado exitosamente",
		"reservacion_id":     req.ReservacionID,
		"total_base":         totalBase,
		"total_con_ganancia": totalConGanancia,
		"detalle":            respHotel,
	}, nil
}

// llamarReservacionAerolinea
//
// Realiza la llamada HTTP POST al endpoint de reservaciones de una aerolinea
// proveedora enviando la seleccion de vuelos. Retorna la respuesta completa
// del proveedor como mapa generico.
//
// Parametros:
//   - urlAPI: URL base del API del proveedor aerolinea
//   - token: token de autenticacion de agencia (X-Agencia-Token)
//   - vuelos: lista de vuelos seleccionados con sus pasajeros
//
// Retorna:
//   - map[string]interface{}: respuesta del proveedor con reservacionId, total y detalle
//   - error: si la serializacion falla, la peticion HTTP falla o la respuesta es invalida
func (s *DetalleReservacionService) llamarReservacionAerolinea(c *gin.Context, usuarioID *int, urlAPI, token string, vuelos []dto.SeleccionVuelo) (map[string]interface{}, error) {
	// TODO: agregar timeout al http.DefaultClient (deuda técnica identificada)
	body, err := json.Marshal(map[string]interface{}{
		"vuelos": vuelos,
	})
	if err != nil {
		return nil, fmt.Errorf("error serializando request: %w", err)
	}

	req, err := http.NewRequest(http.MethodPost, urlAPI+"/api/reservaciones-agencia", bytes.NewBuffer(body))
	if err != nil {
		return nil, err
	}
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("X-Agencia-Token", token)

	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		s.logSesion.Registrar(c, helpers.TipoOutReservaVueloProveedorFallida, usuarioID, "reserva-vuelo-proveedor",
			fmt.Sprintf("Broom status=ERR msg='%s'", err.Error()))
		return nil, err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK && resp.StatusCode != http.StatusCreated {
		msg := fmt.Sprintf("Broom status=%d msg='%s'", resp.StatusCode, helpers.ParseErrorProveedor(resp))
		s.logSesion.Registrar(c, helpers.TipoOutReservaVueloProveedorFallida, usuarioID, "reserva-vuelo-proveedor", msg)
		return nil, fmt.Errorf("aerolínea respondió con status %d", resp.StatusCode)
	}

	var resultado map[string]interface{}
	if err := json.NewDecoder(resp.Body).Decode(&resultado); err != nil {
		s.logSesion.Registrar(c, helpers.TipoOutReservaVueloProveedorFallida, usuarioID, "reserva-vuelo-proveedor",
			fmt.Sprintf("Broom decode_error='%s'", err.Error()))
		return nil, errors.New("respuesta inválida de la aerolínea")
	}

	s.logSesion.Registrar(c, helpers.TipoOutReservaVueloProveedorExitosa, usuarioID, "reserva-vuelo-proveedor",
		fmt.Sprintf("Broom: reservacionId=%v total=%v", resultado["reservacionId"], resultado["total"]))

	return resultado, nil
}

// llamarReservacionHotel
//
// Realiza la llamada HTTP POST al endpoint de reservaciones de una hotelera
// proveedora enviando la seleccion de habitaciones. Retorna la respuesta
// completa del proveedor como mapa generico.
//
// Parametros:
//   - urlAPI: URL base del API del proveedor hotelera
//   - token: token de autenticacion de agencia (X-Agencia-Token)
//   - habitaciones: lista de habitaciones seleccionadas con fechas y opciones
//
// Retorna:
//   - map[string]interface{}: respuesta del proveedor con id, total y detalle de habitaciones
//   - error: si la serializacion falla, la peticion HTTP falla o la respuesta es invalida
func (s *DetalleReservacionService) llamarReservacionHotel(c *gin.Context, usuarioID *int, urlAPI, token string, habitaciones []dto.SeleccionHabitacion) (map[string]interface{}, error) {
	// TODO: agregar timeout al http.DefaultClient (deuda técnica identificada)
	body, err := json.Marshal(map[string]interface{}{
		"habitaciones": habitaciones,
	})
	if err != nil {
		return nil, fmt.Errorf("error serializando request: %w", err)
	}

	req, err := http.NewRequest(http.MethodPost, urlAPI+"/agencia/reservaciones", bytes.NewBuffer(body))
	if err != nil {
		return nil, err
	}
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("X-Agencia-Token", token)

	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		s.logSesion.Registrar(c, helpers.TipoOutReservaHotelProveedorFallida, usuarioID, "reserva-hotel-proveedor",
			fmt.Sprintf("Miku status=ERR msg='%s'", err.Error()))
		return nil, err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK && resp.StatusCode != http.StatusCreated {
		msg := fmt.Sprintf("Miku status=%d msg='%s'", resp.StatusCode, helpers.ParseErrorProveedor(resp))
		s.logSesion.Registrar(c, helpers.TipoOutReservaHotelProveedorFallida, usuarioID, "reserva-hotel-proveedor", msg)
		return nil, fmt.Errorf("hotelera respondió con status %d", resp.StatusCode)
	}

	var resultado map[string]interface{}
	if err := json.NewDecoder(resp.Body).Decode(&resultado); err != nil {
		s.logSesion.Registrar(c, helpers.TipoOutReservaHotelProveedorFallida, usuarioID, "reserva-hotel-proveedor",
			fmt.Sprintf("Miku decode_error='%s'", err.Error()))
		return nil, errors.New("respuesta inválida de la hotelera")
	}

	s.logSesion.Registrar(c, helpers.TipoOutReservaHotelProveedorExitosa, usuarioID, "reserva-hotel-proveedor",
		fmt.Sprintf("Miku: id=%v total=%v", resultado["id"], resultado["total"]))

	return resultado, nil
}

// AgregarPasajerosVuelo
//
// Registra los datos de los pasajeros en el sistema de la aerolinea para
// una reservacion existente. Valida que cada pasajero tenga un numero de
// pasaporte con solo digitos antes de enviar la solicitud al proveedor.
//
// Parametros:
//   - usuarioID: identificador del usuario dueno de la reservacion
//   - req: datos de la solicitud incluyendo ReservacionID, ProveedorID y lista de pasajeros
//
// Retorna:
//   - error: si el pasaporte es invalido, falla la obtencion del detalle o falla el proveedor
func (s *DetalleReservacionService) AgregarPasajerosVuelo(
	c *gin.Context,
	usuarioID int,
	req dto.AgregarPasajerosVueloRequest,
) error {

	for _, p := range req.Pasajeros {
		if strings.TrimSpace(p.Pasaporte) == "" {
			return errors.New("el número de pasaporte es obligatorio")
		}
		for _, c := range p.Pasaporte {
			if c < '0' || c > '9' {
				return fmt.Errorf(
					"el pasaporte de %s %s debe contener solo números",
					p.Nombre, p.Apellido,
				)
			}
		}
	}

	idReservaAerolinea, urlAPI, token, err := s.repo.ObtenerDetalleAerolineaPorProveedor(
		req.ReservacionID, usuarioID, req.ProveedorID,
	)
	if err != nil {
		return err
	}

	reservacionAerolineaID, err := strconv.Atoi(idReservaAerolinea)
	if err != nil {
		return fmt.Errorf("id de reservación de aerolínea inválido: %w", err)
	}

	uid := usuarioID
	return s.llamarPasajerosAerolinea(c, &uid, urlAPI, token, reservacionAerolineaID, req.Pasajeros)
}

// llamarPasajerosAerolinea
//
// Realiza la llamada HTTP POST al endpoint de pasajeros de la aerolinea
// proveedora, enviando el ID de reservacion en el sistema externo y la
// lista de pasajeros con sus datos personales y de pasaporte.
//
// Parametros:
//   - urlAPI: URL base del API del proveedor aerolinea
//   - token: token de autenticacion de agencia (X-Agencia-Token)
//   - reservacionID: ID de la reservacion en el sistema de la aerolinea
//   - pasajeros: lista de pasajeros con nombre, apellido, pasaporte y otros datos
//
// Retorna:
//   - error: si la serializacion falla, la peticion HTTP falla o la aerolinea rechaza la solicitud
func (s *DetalleReservacionService) llamarPasajerosAerolinea(
	c *gin.Context,
	usuarioID *int,
	urlAPI, token string,
	reservacionID int,
	pasajeros []dto.PasajeroVueloDTO,
) error {
	// TODO: agregar timeout al http.DefaultClient (deuda técnica identificada)
	bodyReq := dto.AgregarPasajerosVueloAerolineaBody{
		ReservacionID: reservacionID,
		Pasajeros:     pasajeros,
	}

	bodyBytes, err := json.Marshal(bodyReq)
	if err != nil {
		return fmt.Errorf("error serializando pasajeros: %w", err)
	}

	req, err := http.NewRequest(
		http.MethodPost,
		urlAPI+"/api/reservaciones-agencia/pasajeros",
		bytes.NewBuffer(bodyBytes),
	)
	if err != nil {
		return err
	}
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("X-Agencia-Token", token)

	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		s.logSesion.Registrar(c, helpers.TipoOutPasajerosProveedorFallida, usuarioID, "pasajeros-proveedor",
			fmt.Sprintf("Broom status=ERR msg='%s'", err.Error()))
		return fmt.Errorf("error contactando aerolínea: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK && resp.StatusCode != http.StatusCreated {
		msg := fmt.Sprintf("Broom status=%d msg='%s'", resp.StatusCode, helpers.ParseErrorProveedor(resp))
		s.logSesion.Registrar(c, helpers.TipoOutPasajerosProveedorFallida, usuarioID, "pasajeros-proveedor", msg)
		return fmt.Errorf("aerolínea respondió con status %d", resp.StatusCode)
	}

	s.logSesion.Registrar(c, helpers.TipoOutPasajerosProveedorExitosa, usuarioID, "pasajeros-proveedor",
		fmt.Sprintf("Broom: %d pasajero(s) registrados, reservaId=%d", len(pasajeros), reservacionID))

	return nil
}
