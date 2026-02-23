package org.example.services;

import org.example.dtos.HabitacionReservaRequestDTO;
import org.example.dtos.ReservacionDetalleDTO;
import org.example.dtos.ReservacionRequestDTO;
import org.example.dtos.ReservacionResponseDTO;
import org.example.repositories.ReservacionAgenciaRepository;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

public class ReservacionAgenciaService {

    private final ReservacionAgenciaRepository repository = new ReservacionAgenciaRepository();

    public ReservacionResponseDTO crearReservacion(ReservacionRequestDTO request, int usuarioId) {

        // Verificar agencia activa y obtener descuento
        Double porcentajeDescuento = repository.obtenerDescuentoAgencia(usuarioId);
        if (porcentajeDescuento == null) {
            throw new IllegalArgumentException("El usuario no tiene una agencia activa asociada");
        }

        if (request.getHabitaciones() == null || request.getHabitaciones().isEmpty()) {
            throw new IllegalArgumentException("Debe incluir al menos una habitación");
        }

        double factor = 1.0 - (porcentajeDescuento / 100.0);

        // Verificar traslapes y calcular total con descuento
        double totalGeneral = 0;
        for (HabitacionReservaRequestDTO item : request.getHabitaciones()) {

            LocalDate checkIn  = LocalDate.parse(item.getFechaCheckIn());
            LocalDate checkOut = LocalDate.parse(item.getFechaCheckOut());
            long dias = ChronoUnit.DAYS.between(checkIn, checkOut);

            if (dias <= 0) throw new IllegalArgumentException(
                    "Las fechas de la habitación " + item.getHabitacionId() + " son inválidas"
            );

            Date fechaCheckIn  = Date.valueOf(checkIn);
            Date fechaCheckOut = Date.valueOf(checkOut);

            if (repository.existeTraslape(item.getHabitacionId(), fechaCheckIn, fechaCheckOut)) {
                throw new IllegalArgumentException(
                        "La habitación " + item.getHabitacionId() +
                                " no está disponible para las fechas seleccionadas"
                );
            }

            double[] precios = repository.obtenerPrecios(item.getHabitacionId());
            double precioPorNoche   = precios[0] * factor;
            double precioPorPersona = precios[1] * factor;
            totalGeneral += (dias * precioPorNoche) + (dias * item.getCantidadPersonas() * precioPorPersona);
        }

        // Redondear total
        totalGeneral = Math.round(totalGeneral * 100.0) / 100.0;

        // Generar número único
        String noReservacion  = "MIKU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Timestamp fechaCreacion   = Timestamp.valueOf(LocalDateTime.now());
        Timestamp fechaExpiracion = Timestamp.valueOf(LocalDateTime.now().plusMinutes(10));

        // Crear reservación pendiente
        int reservacionId = repository.crearReservacion(
                noReservacion, totalGeneral, usuarioId, fechaCreacion, fechaExpiracion
        );

        // Expirar otras pendientes del mismo usuario
        repository.expirarPendientesDeUsuario(usuarioId, reservacionId);

        // Insertar detalles con precio con descuento
        for (HabitacionReservaRequestDTO item : request.getHabitaciones()) {
            LocalDate checkIn  = LocalDate.parse(item.getFechaCheckIn());
            LocalDate checkOut = LocalDate.parse(item.getFechaCheckOut());
            long dias = ChronoUnit.DAYS.between(checkIn, checkOut);

            double[] precios       = repository.obtenerPrecios(item.getHabitacionId());
            double precioPorNoche   = precios[0] * factor;
            double precioPorPersona = precios[1] * factor;
            double totalHabitacion  = Math.round(
                    ((dias * precioPorNoche) + (dias * item.getCantidadPersonas() * precioPorPersona)) * 100.0
            ) / 100.0;

            repository.crearDetalle(
                    reservacionId,
                    item.getHabitacionId(),
                    Date.valueOf(checkIn),
                    Date.valueOf(checkOut),
                    item.getCantidadPersonas(),
                    totalHabitacion
            );
        }

        Object[] datos = repository.obtenerReservacion(reservacionId);

        ReservacionResponseDTO response = new ReservacionResponseDTO();
        response.setId((int) datos[0]);
        response.setNoReservacion((String) datos[1]);
        response.setTotal((double) datos[2]);
        response.setFechaCreacion((String) datos[3]);
        response.setFechaExpiracion((String) datos[4]);
        response.setEstado((String) datos[5]);

        return response;
    }

    public List<ReservacionDetalleDTO> obtenerReservaciones(int usuarioId) {
        List<ReservacionDetalleDTO> reservaciones = repository.obtenerReservacionesDeUsuario(usuarioId);
        for (ReservacionDetalleDTO dto : reservaciones) {
            dto.setImagenesHotelIds(repository.obtenerImagenesHotel(dto.getHotelId()));
            dto.setImagenesHabitacionIds(repository.obtenerImagenesHabitacion(dto.getHabitacionId()));
        }
        return reservaciones;
    }
}