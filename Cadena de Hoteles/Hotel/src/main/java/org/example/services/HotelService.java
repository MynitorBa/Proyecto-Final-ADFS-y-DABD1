package org.example.services;

import org.example.clients.MoventClient;
import org.example.dtos.*;
import org.example.helpers.EmailHelper;
import org.example.repositories.CiudadRepository;
import org.example.repositories.HotelRepository;
import org.example.repositories.PaisRepository;

import java.time.Year;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service principal para la gestion de hoteles desde el panel de administracion.
 * Cubre catalogos, hoteles, amenidades, habitaciones, imagenes y metricas.
 */
public class HotelService {

    private static final Logger LOG = Logger.getLogger(HotelService.class.getName());

    private final HotelRepository                 hotelRepository;
    private final CiudadRepository               ciudadRepository;
    private final PaisRepository                 paisRepository;
    private final AgenciaNotificadorExternoService agenciaNotificador;

    /**
     * Crea una instancia de HotelService con sus dependencias inyectadas.
     */
    public HotelService(HotelRepository hotelRepository,
                        CiudadRepository ciudadRepository,
                        PaisRepository paisRepository,
                        AgenciaNotificadorExternoService agenciaNotificador) {
        this.hotelRepository   = hotelRepository;
        this.ciudadRepository  = ciudadRepository;
        this.paisRepository    = paisRepository;
        this.agenciaNotificador = agenciaNotificador;
    }

    /**
     * Retorna todas las amenidades disponibles en el catalogo.
     * @return lista de amenidades.
     */
    public List<AmenidadDTO> listarAmenidades() { return hotelRepository.listarAmenidades(); }

    /**
     * Retorna todos los paises registrados en el sistema.
     * @return lista de paises.
     */
    public List<PaisDTO> listarPaises() { return paisRepository.listarPaises(); }

    /**
     * Retorna todas las ciudades registradas en el sistema.
     * @return lista de ciudades.
     */
    public List<CiudadDTO> listarCiudades() { return paisRepository.listarCiudades(); }

    /**
     * Crea una nueva amenidad en el catalogo.
     * @param nombre nombre de la amenidad, no puede ser nulo ni vacio.
     * @return mapa con el ID, nombre y mensaje de confirmacion.
     * @throws IllegalArgumentException si el nombre es nulo o vacio.
     */
    public Map<String, Object> crearAmenidad(String nombre) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre de la amenidad es obligatorio");
        int id = hotelRepository.crearAmenidad(nombre.trim());
        return Map.of("id", id, "nombre", nombre.trim(), "mensaje", "Amenidad creada correctamente");
    }

    /**
     * Retorna todos los hoteles con su cantidad de habitaciones e IDs de imagenes.
     * @return lista de hoteles con datos enriquecidos para el panel de admin.
     */
    public List<HotelAdminDTO> listarTodos() {
        List<HotelAdminDTO> hoteles = hotelRepository.listarTodos();
        for (HotelAdminDTO hotel : hoteles) {
            hotel.setCantidadHabitaciones(hotelRepository.contarHabitaciones(hotel.getId()));
            hotel.setImagenesIds(hotelRepository.obtenerImagenesIds(hotel.getId()));
        }
        return hoteles;
    }

    /**
     * Crea un nuevo hotel, buscando o creando el pais y ciudad si no existen.
     * @param req datos del hotel: nombre, rating, estado, ciudad y pais.
     * @return mapa con el ID del hotel creado y mensaje de confirmacion.
     * @throws IllegalArgumentException si algun campo obligatorio es invalido.
     */
    public Map<String, Object> crearHotel(CrearHotelRequestDTO req) {
        validarCamposHotel(req.getNombre(), req.getRating(), req.getEstadoId());
        if (req.getCiudad() == null || req.getCiudad().isBlank())
            throw new IllegalArgumentException("El nombre de la ciudad es obligatorio");
        if (req.getPaisNombre() == null || req.getPaisNombre().isBlank())
            throw new IllegalArgumentException("El nombre del pais es obligatorio");

        int paisId   = paisRepository.buscarOCrearPorNombre(req.getPaisNombre().trim());
        int ciudadId = ciudadRepository.buscarOCrearPorNombre(req.getCiudad().trim(), paisId);

        int hotelId = hotelRepository.crearHotel(
                req.getNombre().trim(), safe(req.getDireccion()),
                safe(req.getDescripcion()), req.getRating(),
                req.getEstadoId(), ciudadId);
        return Map.of("id", hotelId, "mensaje", "Hotel creado correctamente");
    }

    /**
     * Edita los datos de un hotel existente.
     * @param hotelId ID del hotel a editar.
     * @param req     datos actualizados del hotel.
     * @throws IllegalArgumentException si el hotel no existe o los datos son invalidos.
     */
    public void editarHotel(int hotelId, EditarHotelRequestDTO req) {
        if (!hotelRepository.existe(hotelId))
            throw new IllegalArgumentException("Hotel no encontrado: " + hotelId);
        validarCamposHotel(req.getNombre(), req.getRating(), req.getEstadoId());
        hotelRepository.actualizarHotel(hotelId, req.getNombre().trim(),
                safe(req.getDireccion()), safe(req.getDescripcion()),
                req.getRating(), req.getEstadoId());
    }

    /**
     * Elimina un hotel del sistema.
     * @param hotelId ID del hotel a eliminar.
     * @throws IllegalArgumentException si el hotel no existe.
     */
    public void eliminarHotel(int hotelId) {
        if (!hotelRepository.existe(hotelId))
            throw new IllegalArgumentException("Hotel no encontrado: " + hotelId);
        int activas = hotelRepository.contarReservasActivasHotel(hotelId);
        if (activas > 0)
            throw new IllegalArgumentException(
                "Este hotel tiene " + activas +
                " reservacion(es) en proceso (Pendientes o Confirmadas). " +
                "Usa la opcion 'Cancelar reservas y eliminar' para cerrar el hotel.");
        hotelRepository.eliminarHotel(hotelId);
    }

    /**
     * Retorna el recuento y los datos de las reservaciones activas del hotel.
     * Se usa en el frontend para mostrar al admin que reservas se afectarian antes de cerrar.
     * @param hotelId ID del hotel a consultar.
     * @return mapa con { count, reservaciones: lista de { id, noReservacion, correo, nombre, total } }
     */
    public Map<String, Object> obtenerReservasActivasHotel(int hotelId) {
        if (!hotelRepository.existe(hotelId))
            throw new IllegalArgumentException("Hotel no encontrado: " + hotelId);
        List<Object[]> filas = hotelRepository.obtenerReservacionesActivasHotel(hotelId);
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Object[] f : filas) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("id",            f[0]);
            r.put("noReservacion", f[1]);
            r.put("correo",        f[2]);
            r.put("nombre",        f[3]);
            r.put("total",         f[4]);
            lista.add(r);
        }
        return Map.of("count", filas.size(), "reservaciones", lista);
    }

    /**
     * Reactiva un hotel previamente cerrado (EstadoID = 2 → 1).
     * El hotel vuelve a aparecer en las busquedas publicas.
     * @param hotelId ID del hotel a reactivar.
     * @throws IllegalArgumentException si el hotel no existe.
     */
    public void reactivarHotel(int hotelId) {
        if (!hotelRepository.existe(hotelId))
            throw new IllegalArgumentException("Hotel no encontrado: " + hotelId);
        hotelRepository.reactivarHotel(hotelId);
    }

    /**
     * Cancela todas las reservaciones activas del hotel, notifica a cada usuario por correo
     * y, segun el flag, elimina el hotel fisicamente o lo deja como Cerrado (EstadoID = 2).
     * El envio de correos y la notificacion a agencias son best-effort.
     * @param hotelId            ID del hotel a operar.
     * @param hotelNombre        nombre del hotel (para el correo y el webhook).
     * @param eliminarDefinitivo si true, elimina el hotel de la BD; si false, solo lo cierra.
     * @return mapa con { cancelaciones, emailsEnviados, emailsFallidos }
     */
    public Map<String, Object> cerrarHotelConCancelaciones(int hotelId, String hotelNombre,
                                                           boolean eliminarDefinitivo) {
        if (!hotelRepository.existe(hotelId))
            throw new IllegalArgumentException("Hotel no encontrado: " + hotelId);

        // Leer reservas activas ANTES de cancelar para poder notificar
        List<Object[]> reservas = hotelRepository.obtenerReservacionesActivasHotel(hotelId);

        // Cancelar todas en un solo UPDATE
        String motivo = "Hotel cerrado por el establecimiento. Las reservaciones han sido canceladas.";
        hotelRepository.cancelarReservacionesActivasHotel(hotelId, motivo);

        // Notificar a cada usuario por correo y a cada agencia via su endpoint externo (best-effort)
        int emailsEnviados  = 0;
        int emailsFallidos  = 0;
        List<Map<String, Object>> reservasAgencia = new ArrayList<>();

        for (Object[] f : reservas) {
            int    reservacionId = ((Number) f[0]).intValue();
            String noReservacion = (String) f[1];
            String correo        = (String) f[2];
            String nombreUsuario = (String) f[3];
            double total         = ((Number) f[4]).doubleValue();

            // Correo al usuario
            try {
                EmailHelper.enviar(
                        correo,
                        "Blink Hotels – Reservacion " + noReservacion + " Cancelada por cierre de hotel",
                        construirCorreoCierreHotel(nombreUsuario, noReservacion, total, hotelNombre)
                );
                emailsEnviados++;
            } catch (Exception ex) {
                emailsFallidos++;
                LOG.log(Level.WARNING,
                        "Error al enviar correo de cierre de hotel. Reservacion=" + noReservacion, ex);
            }

            // Notificacion a la agencia duena de esta reservacion (si aplica)
            try {
                var resultado = agenciaNotificador.notificarCancelacion(reservacionId, motivo);
                if (resultado.isEsReservaDeAgencia()) {
                    Map<String, Object> r = new LinkedHashMap<>();
                    r.put("noReservacion", noReservacion);
                    r.put("correo",        correo);
                    r.put("total",         total);
                    reservasAgencia.add(r);
                }
            } catch (Exception ex) {
                LOG.log(Level.WARNING,
                        "Error notificando agencia para reservacion " + reservacionId, ex);
            }
        }

        // Webhook de resumen a Movent (best-effort, solo reservas que venian de agencia)
        MoventClient.notificarHotelCerrado(hotelNombre, reservasAgencia);

        // Finalizar: eliminar fisicamente o cambiar estado a Cerrado segun el flag
        if (eliminarDefinitivo) {
            hotelRepository.eliminarHotel(hotelId);
        } else {
            hotelRepository.cerrarHotel(hotelId);
        }

        return Map.of(
                "cancelaciones",   reservas.size(),
                "emailsEnviados",  emailsEnviados,
                "emailsFallidos",  emailsFallidos
        );
    }

    // -------------------------------------------------------------------------
    // HTML del correo de cierre de hotel
    // -------------------------------------------------------------------------

    private static String construirCorreoCierreHotel(
            String nombreUsuario,
            String noReservacion,
            double total,
            String hotelNombre) {

        int anio = Year.now().getValue();

        return """
                <!DOCTYPE html>
                <html lang="es">
                <head><meta charset="UTF-8"><title>Cancelacion por cierre de hotel</title></head>
                <body style="margin:0;padding:0;background:#F4F6F8;font-family:'Segoe UI',Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0"
                         style="background:#F4F6F8;padding:40px 0;">
                    <tr><td align="center">
                      <table width="600" cellpadding="0" cellspacing="0"
                             style="background:#ffffff;border-radius:8px;
                                    box-shadow:0 4px 20px rgba(0,0,0,.08);overflow:hidden;">

                        <!-- Cabecera -->
                        <tr>
                          <td style="background:#1A3C5E;padding:28px 40px;text-align:center;">
                            <h1 style="margin:0;font-size:22px;font-weight:300;color:#ffffff;
                                       letter-spacing:3px;">
                              BLINK
                              <span style="color:#F0A500;font-weight:700;">HOTELS</span>
                            </h1>
                          </td>
                        </tr>

                        <!-- Cuerpo -->
                        <tr>
                          <td style="padding:32px 40px;">
                            <p style="margin:0 0 8px;font-size:16px;color:#1A3C5E;font-weight:600;">
                              Hola, %s:
                            </p>
                            <p style="margin:0 0 24px;font-size:14px;color:#555;">
                              Lamentamos informarte que el hotel <strong>%s</strong> ha decidido
                              cerrar sus operaciones y, como consecuencia, tu reservacion ha sido
                              cancelada automaticamente.
                            </p>

                            <!-- Resumen -->
                            <table width="100%%" cellpadding="8" cellspacing="0"
                                   style="background:#F4F6F8;border-radius:6px;margin-bottom:24px;">
                              <tr>
                                <td style="font-size:12px;color:#888;width:40%%;">N° Reservacion</td>
                                <td style="font-size:14px;font-weight:700;color:#1A3C5E;
                                           font-family:monospace;">%s</td>
                              </tr>
                              <tr>
                                <td style="font-size:12px;color:#888;">Total</td>
                                <td style="font-size:14px;font-weight:700;">$%.2f</td>
                              </tr>
                            </table>

                            <!-- Nota -->
                            <div style="background:#FFF8E1;border-left:4px solid #F0A500;
                                        padding:14px 18px;border-radius:4px;margin-bottom:24px;">
                              <p style="margin:0 0 4px;font-size:12px;color:#F0A500;font-weight:700;">
                                Informacion sobre reembolsos
                              </p>
                              <p style="margin:0;font-size:14px;color:#333;">
                                Para consultas sobre reembolsos o compensaciones, contacta directamente
                                al hotel. Lamentamos los inconvenientes causados.
                              </p>
                            </div>

                            <p style="margin:0;font-size:13px;color:#777;">
                              Si tienes alguna pregunta adicional, no dudes en contactarnos.
                            </p>
                          </td>
                        </tr>

                        <!-- Pie -->
                        <tr>
                          <td style="background:#1A3C5E;padding:16px 40px;text-align:center;">
                            <p style="margin:0;font-size:11px;color:#90A4AE;">
                              &copy; %d Blink Hotels — Todos los derechos reservados
                            </p>
                          </td>
                        </tr>

                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(nombreUsuario, hotelNombre, noReservacion, total, anio);
    }

    /**
     * Retorna las amenidades de un hotel con sus IDs de imagenes.
     * @param hotelId ID del hotel.
     * @return lista de amenidades del hotel.
     * @throws IllegalArgumentException si el hotel no existe.
     */
    public List<HotelAmenidadDTO> listarAmenidadesHotel(int hotelId) {
        if (!hotelRepository.existe(hotelId))
            throw new IllegalArgumentException("Hotel no encontrado: " + hotelId);
        List<HotelAmenidadDTO> lista = hotelRepository.listarAmenidadesHotel(hotelId);
        for (HotelAmenidadDTO a : lista)
            a.setImagenesIds(hotelRepository.obtenerImagenesAmenidadIds(a.getId()));
        return lista;
    }

    /**
     * Agrega una amenidad del catalogo a un hotel.
     * Valida que el hotel exista y que no tenga ya esa amenidad asignada.
     * @param hotelId ID del hotel.
     * @param req     datos con el ID de amenidad y descripcion opcional.
     * @return mapa con el ID del registro creado y mensaje de confirmacion.
     * @throws IllegalArgumentException si el hotel no existe, la amenidad es invalida
     *                                  o el hotel ya tiene esa amenidad.
     */
    public Map<String, Object> agregarAmenidadHotel(int hotelId, AgregarAmenidadRequestDTO req) {
        if (!hotelRepository.existe(hotelId))
            throw new IllegalArgumentException("Hotel no encontrado: " + hotelId);
        if (req.getAmenidadId() <= 0)
            throw new IllegalArgumentException("Amenidad invalida");
        if (hotelRepository.tieneAmenidad(hotelId, req.getAmenidadId()))
            throw new IllegalArgumentException("Este hotel ya tiene esa amenidad asignada.");
        int id = hotelRepository.agregarAmenidadHotel(hotelId, req.getAmenidadId(), safe(req.getDescripcion()));
        return Map.of("id", id, "mensaje", "Amenidad agregada");
    }

    /**
     * Actualiza la descripcion de una amenidad asignada a un hotel.
     * @param hotelAmenidadId ID del registro hotel-amenidad.
     * @param req             datos con la nueva descripcion.
     */
    public void actualizarAmenidadHotel(int hotelAmenidadId, AgregarAmenidadRequestDTO req) {
        hotelRepository.actualizarAmenidadHotel(hotelAmenidadId, safe(req.getDescripcion()));
    }

    /**
     * Elimina una amenidad asignada a un hotel.
     * @param hotelAmenidadId ID del registro hotel-amenidad a eliminar.
     */
    public void eliminarAmenidadHotel(int hotelAmenidadId) {
        hotelRepository.eliminarAmenidadHotel(hotelAmenidadId);
    }

    /**
     * Agrega una imagen a una amenidad de hotel decodificando el base64 recibido.
     * @param hotelAmenidadId ID del registro hotel-amenidad.
     * @param base64          imagen codificada en base64, con o sin prefijo data URI.
     * @return mapa con el ID de la imagen creada y mensaje de confirmacion.
     */
    public Map<String, Object> agregarImagenAmenidad(int hotelAmenidadId, String base64) {
        int nuevoId = hotelRepository.agregarImagenAmenidad(hotelAmenidadId, decodeBase64(base64));
        return Map.of("id", nuevoId, "mensaje", "Imagen de amenidad agregada");
    }

    /**
     * Elimina una imagen de amenidad por su ID.
     * @param imagenId ID de la imagen a eliminar.
     */
    public void eliminarImagenAmenidad(int imagenId) {
        hotelRepository.eliminarImagenAmenidad(imagenId);
    }

    /**
     * Agrega una imagen al hotel decodificando el base64 recibido.
     * @param hotelId ID del hotel.
     * @param base64  imagen codificada en base64.
     * @return mapa con el ID de la imagen creada y mensaje de confirmacion.
     * @throws IllegalArgumentException si el hotel no existe.
     */
    public Map<String, Object> agregarImagenHotel(int hotelId, String base64) {
        if (!hotelRepository.existe(hotelId))
            throw new IllegalArgumentException("Hotel no encontrado: " + hotelId);
        int nuevoId = hotelRepository.agregarImagenHotel(hotelId, decodeBase64(base64));
        return Map.of("id", nuevoId, "mensaje", "Imagen agregada");
    }

    /**
     * Elimina una imagen de hotel por su ID.
     * @param imagenId ID de la imagen a eliminar.
     */
    public void eliminarImagenHotel(int imagenId) {
        hotelRepository.eliminarImagenHotel(imagenId);
    }

    /**
     * Retorna las habitaciones de un hotel con sus IDs de imagenes.
     * @param hotelId ID del hotel.
     * @return lista de habitaciones con datos para el panel de admin.
     * @throws IllegalArgumentException si el hotel no existe.
     */
    public List<HabitacionAdminDTO> listarHabitaciones(int hotelId) {
        if (!hotelRepository.existe(hotelId))
            throw new IllegalArgumentException("Hotel no encontrado: " + hotelId);
        List<HabitacionAdminDTO> lista = hotelRepository.listarHabitacionesPorHotel(hotelId);
        for (HabitacionAdminDTO h : lista)
            h.setImagenesIds(hotelRepository.obtenerImagenesHabitacionIds(h.getId()));
        return lista;
    }

    /**
     * Crea una nueva habitacion en un hotel.
     * El numero de habitacion se auto-asigna en el repositorio.
     * @param req datos de la habitacion: hotelId, tipo, descripcion y estado.
     * @return mapa con el ID de la habitacion creada y mensaje de confirmacion.
     * @throws IllegalArgumentException si el hotel no existe, el tipo es invalido o el estado no es valido.
     */
    public Map<String, Object> crearHabitacion(CrearHabitacionRequestDTO req) {
        if (!hotelRepository.existe(req.getHotelId()))
            throw new IllegalArgumentException("Hotel no encontrado: " + req.getHotelId());
        if (req.getTipoHabitacionId() <= 0)
            throw new IllegalArgumentException("Tipo de habitacion invalido");
        validarEstadoHabitacion(req.getEstadoId());

        int id = hotelRepository.crearHabitacion(
                req.getHotelId(),
                req.getTipoHabitacionId(),
                safe(req.getDescripcion()),
                req.getEstadoId()
        );
        return Map.of("id", id, "mensaje", "Habitacion creada correctamente");
    }

    /**
     * Edita los datos de una habitacion existente.
     * @param habitacionId ID de la habitacion a editar.
     * @param req          datos actualizados de la habitacion.
     * @throws IllegalArgumentException si la habitacion no existe, el tipo es invalido o el estado no es valido.
     */
    public void editarHabitacion(int habitacionId, EditarHabitacionRequestDTO req) {
        if (!hotelRepository.existeHabitacion(habitacionId))
            throw new IllegalArgumentException("Habitacion no encontrada: " + habitacionId);
        if (req.getTipoHabitacionId() <= 0)
            throw new IllegalArgumentException("Tipo de habitacion invalido");
        validarEstadoHabitacion(req.getEstadoId());
        hotelRepository.actualizarHabitacion(habitacionId,
                req.getTipoHabitacionId(),
                safe(req.getNumeroHabitacion()),
                safe(req.getDescripcion()),
                req.getEstadoId());
    }

    /**
     * Elimina una habitacion del sistema.
     * @param habitacionId ID de la habitacion a eliminar.
     * @throws IllegalArgumentException si la habitacion no existe.
     */
    public void eliminarHabitacion(int habitacionId) {
        if (!hotelRepository.existeHabitacion(habitacionId))
            throw new IllegalArgumentException("Habitacion no encontrada: " + habitacionId);
        int activas = hotelRepository.contarReservasActivasHabitacion(habitacionId);
        if (activas > 0)
            throw new IllegalArgumentException(
                "Esta habitacion tiene " + activas +
                " reservacion(es) en proceso (Pendientes o Confirmadas). " +
                "Cancela o espera a que se completen antes de eliminarla.");
        hotelRepository.eliminarHabitacion(habitacionId);
    }

    /**
     * Retorna el recuento y los datos de las reservaciones activas de una habitacion.
     * Se usa en el frontend para mostrar al admin que reservas se afectarian antes de cerrar.
     * @param habitacionId ID de la habitacion a consultar.
     * @return mapa con { count, reservaciones: lista de { id, noReservacion, correo, nombre, total } }
     */
    public Map<String, Object> obtenerReservasActivasHabitacion(int habitacionId) {
        if (!hotelRepository.existeHabitacion(habitacionId))
            throw new IllegalArgumentException("Habitacion no encontrada: " + habitacionId);
        List<Object[]> filas = hotelRepository.obtenerReservacionesActivasHabitacion(habitacionId);
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Object[] f : filas) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("id",            f[0]);
            r.put("noReservacion", f[1]);
            r.put("correo",        f[2]);
            r.put("nombre",        f[3]);
            r.put("total",         f[4]);
            lista.add(r);
        }
        return Map.of("count", filas.size(), "reservaciones", lista);
    }

    /**
     * Reactiva una habitacion cerrada (ESTADO_ID = 2 → 1).
     * @param habitacionId ID de la habitacion a reactivar.
     * @throws IllegalArgumentException si la habitacion no existe.
     */
    public void reactivarHabitacion(int habitacionId) {
        if (!hotelRepository.existeHabitacion(habitacionId))
            throw new IllegalArgumentException("Habitacion no encontrada: " + habitacionId);
        hotelRepository.reactivarHabitacion(habitacionId);
    }

    /**
     * Cancela todas las reservaciones activas de la habitacion, notifica a cada usuario por correo
     * y, segun el flag, elimina la habitacion fisicamente o la deja como Cerrada (ESTADO_ID = 2).
     * El envio de correos y la notificacion a agencias son best-effort.
     * @param habitacionId       ID de la habitacion a operar.
     * @param nombreHabitacion   identificador legible de la habitacion (para el correo).
     * @param eliminarDefinitivo si true, elimina la habitacion de la BD; si false, solo la cierra.
     * @return mapa con { cancelaciones, emailsEnviados, emailsFallidos }
     */
    public Map<String, Object> cerrarHabitacionConCancelaciones(int habitacionId,
                                                                String nombreHabitacion,
                                                                boolean eliminarDefinitivo) {
        if (!hotelRepository.existeHabitacion(habitacionId))
            throw new IllegalArgumentException("Habitacion no encontrada: " + habitacionId);

        List<Object[]> reservas = hotelRepository.obtenerReservacionesActivasHabitacion(habitacionId);

        String motivo = eliminarDefinitivo
                ? "La habitacion fue eliminada por el establecimiento. La reservacion ha sido cancelada."
                : "La habitacion fue cerrada temporalmente por el establecimiento. La reservacion ha sido cancelada.";

        hotelRepository.cancelarReservacionesActivasHabitacion(habitacionId, motivo);

        int emailsEnviados = 0;
        int emailsFallidos = 0;
        List<Map<String, Object>> reservasAgencia = new ArrayList<>();

        for (Object[] f : reservas) {
            int    reservacionId = ((Number) f[0]).intValue();
            String noReservacion = (String) f[1];
            String correo        = (String) f[2];
            String nombreUsuario = (String) f[3];
            double total         = ((Number) f[4]).doubleValue();

            try {
                EmailHelper.enviar(
                        correo,
                        "Blink Hotels – Reservacion " + noReservacion + " Cancelada por cierre de habitacion",
                        construirCorreoCierreHabitacion(nombreUsuario, noReservacion, total, nombreHabitacion, eliminarDefinitivo)
                );
                emailsEnviados++;
            } catch (Exception ex) {
                emailsFallidos++;
                LOG.log(Level.WARNING,
                        "Error al enviar correo de cierre de habitacion. Reservacion=" + noReservacion, ex);
            }

            try {
                var resultado = agenciaNotificador.notificarCancelacion(reservacionId, motivo);
                if (resultado.isEsReservaDeAgencia()) {
                    Map<String, Object> r = new LinkedHashMap<>();
                    r.put("noReservacion", noReservacion);
                    r.put("correo",        correo);
                    r.put("total",         total);
                    reservasAgencia.add(r);
                }
            } catch (Exception ex) {
                LOG.log(Level.WARNING,
                        "Error notificando agencia para reservacion " + reservacionId, ex);
            }
        }

        MoventClient.notificarHabitacionCerrada(habitacionId, reservasAgencia);

        if (eliminarDefinitivo) {
            hotelRepository.eliminarHabitacion(habitacionId);
        } else {
            hotelRepository.cerrarHabitacion(habitacionId);
        }

        return Map.of(
                "cancelaciones",  reservas.size(),
                "emailsEnviados", emailsEnviados,
                "emailsFallidos", emailsFallidos
        );
    }

    // -------------------------------------------------------------------------
    // HTML del correo de cierre de habitacion
    // -------------------------------------------------------------------------

    private static String construirCorreoCierreHabitacion(
            String nombreUsuario,
            String noReservacion,
            double total,
            String nombreHabitacion,
            boolean eliminada) {

        int anio = Year.now().getValue();
        String accion = eliminada ? "eliminada" : "cerrada temporalmente";

        return """
                <!DOCTYPE html>
                <html lang="es">
                <head><meta charset="UTF-8"><title>Cancelacion por cierre de habitacion</title></head>
                <body style="margin:0;padding:0;background:#F4F6F8;font-family:'Segoe UI',Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0"
                         style="background:#F4F6F8;padding:40px 0;">
                    <tr><td align="center">
                      <table width="600" cellpadding="0" cellspacing="0"
                             style="background:#ffffff;border-radius:8px;
                                    box-shadow:0 4px 20px rgba(0,0,0,.08);overflow:hidden;">

                        <!-- Cabecera -->
                        <tr>
                          <td style="background:#1A3C5E;padding:28px 40px;text-align:center;">
                            <h1 style="margin:0;font-size:22px;font-weight:300;color:#ffffff;
                                       letter-spacing:3px;">
                              BLINK
                              <span style="color:#F0A500;font-weight:700;">HOTELS</span>
                            </h1>
                          </td>
                        </tr>

                        <!-- Cuerpo -->
                        <tr>
                          <td style="padding:32px 40px;">
                            <p style="margin:0 0 8px;font-size:16px;color:#1A3C5E;font-weight:600;">
                              Hola, %s:
                            </p>
                            <p style="margin:0 0 24px;font-size:14px;color:#555;">
                              Lamentamos informarte que la habitacion <strong>%s</strong> ha sido
                              %s y, como consecuencia, tu reservacion ha sido
                              cancelada automaticamente.
                            </p>

                            <!-- Resumen -->
                            <table width="100%%" cellpadding="8" cellspacing="0"
                                   style="background:#F4F6F8;border-radius:6px;margin-bottom:24px;">
                              <tr>
                                <td style="font-size:12px;color:#888;width:40%%;">N° Reservacion</td>
                                <td style="font-size:14px;font-weight:700;color:#1A3C5E;
                                           font-family:monospace;">%s</td>
                              </tr>
                              <tr>
                                <td style="font-size:12px;color:#888;">Total</td>
                                <td style="font-size:14px;font-weight:700;">$%.2f</td>
                              </tr>
                            </table>

                            <!-- Nota -->
                            <div style="background:#FFF8E1;border-left:4px solid #F0A500;
                                        padding:14px 18px;border-radius:4px;margin-bottom:24px;">
                              <p style="margin:0 0 4px;font-size:12px;color:#F0A500;font-weight:700;">
                                Informacion sobre reembolsos
                              </p>
                              <p style="margin:0;font-size:14px;color:#333;">
                                Para consultas sobre reembolsos o compensaciones, contacta directamente
                                al hotel. Lamentamos los inconvenientes causados.
                              </p>
                            </div>

                            <p style="margin:0;font-size:13px;color:#777;">
                              Si tienes alguna pregunta adicional, no dudes en contactarnos.
                            </p>
                          </td>
                        </tr>

                        <!-- Pie -->
                        <tr>
                          <td style="background:#1A3C5E;padding:16px 40px;text-align:center;">
                            <p style="margin:0;font-size:11px;color:#90A4AE;">
                              &copy; %d Blink Hotels — Todos los derechos reservados
                            </p>
                          </td>
                        </tr>

                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(nombreUsuario, nombreHabitacion, accion, noReservacion, total, anio);
    }

    /**
     * Agrega una imagen a una habitacion decodificando el base64 recibido.
     * @param habitacionId ID de la habitacion.
     * @param base64       imagen codificada en base64.
     * @return mapa con el ID de la imagen creada y mensaje de confirmacion.
     * @throws IllegalArgumentException si la habitacion no existe.
     */
    public Map<String, Object> agregarImagenHabitacion(int habitacionId, String base64) {
        if (!hotelRepository.existeHabitacion(habitacionId))
            throw new IllegalArgumentException("Habitacion no encontrada: " + habitacionId);
        int nuevoId = hotelRepository.agregarImagenHabitacion(habitacionId, decodeBase64(base64));
        return Map.of("id", nuevoId, "mensaje", "Imagen agregada");
    }

    /**
     * Elimina una imagen de habitacion por su ID.
     * @param imagenId ID de la imagen a eliminar.
     */
    public void eliminarImagenHabitacion(int imagenId) {
        hotelRepository.eliminarImagenHabitacion(imagenId);
    }

    /**
     * Valida los campos basicos de un hotel: nombre, rating y estado.
     * @throws IllegalArgumentException si alguno de los valores es invalido.
     */
    private void validarCamposHotel(String nombre, double rating, int estadoId) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre del hotel no puede estar vacio");
        if (rating < 0 || rating > 5)
            throw new IllegalArgumentException("El rating debe estar entre 0 y 5");
        if (estadoId != 1 && estadoId != 2)
            throw new IllegalArgumentException("Estado invalido: use 1 (Activo) o 2 (Cerrado)");
    }

    /**
     * Valida que el estado de una habitacion sea 1 (Activa) o 2 (Cerrada).
     * @throws IllegalArgumentException si el estado no es valido.
     */
    private void validarEstadoHabitacion(int estadoId) {
        if (estadoId != 1 && estadoId != 2)
            throw new IllegalArgumentException("Estado invalido: use 1 (Activa) o 2 (Cerrada)");
    }

    /**
     * Decodifica una imagen en base64 a bytes.
     * Soporta strings con o sin prefijo data URI (ej. data:image/png;base64,...).
     * @param base64 imagen en base64.
     * @return array de bytes de la imagen.
     * @throws IllegalArgumentException si el string es nulo o vacio.
     */
    private byte[] decodeBase64(String base64) {
        if (base64 == null || base64.isBlank())
            throw new IllegalArgumentException("La imagen no puede estar vacia");
        String datos = base64.contains(",") ? base64.split(",", 2)[1] : base64;
        return Base64.getDecoder().decode(datos);
    }

    /**
     * Retorna el string recortado o vacio si es null.
     * Evita insertar nulls en la base de datos para campos opcionales.
     */
    private String safe(String s) { return s != null ? s.trim() : ""; }

    /**
     * Retorna todas las reservaciones del sistema para el panel de admin.
     * @return lista de reservaciones con sus datos principales.
     */
    public List<Map<String, Object>> listarTodasReservaciones() {
        return hotelRepository.listarTodasReservaciones();
    }

    /**
     * Retorna metricas generales del sistema: reservaciones, ingresos, ocupacion, etc.
     * @return mapa con los valores de cada metrica.
     */
    public Map<String, Object> obtenerMetricas() {
        return hotelRepository.obtenerMetricas();
    }
}