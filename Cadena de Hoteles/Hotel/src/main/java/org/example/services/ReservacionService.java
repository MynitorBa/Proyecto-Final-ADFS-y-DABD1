package org.example.services;

import org.example.dtos.HabitacionReservaRequestDTO;
import org.example.dtos.ReservacionDetalleDTO;
import org.example.dtos.ReservacionRequestDTO;
import org.example.dtos.ReservacionResponseDTO;
import org.example.repositories.ReservacionRepository;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

public class ReservacionService {

    private final ReservacionRepository reservacionRepository = new ReservacionRepository();

    public ReservacionResponseDTO crearReservacion(ReservacionRequestDTO request, int usuarioId) {

        if (request.getHabitaciones() == null || request.getHabitaciones().isEmpty()) {
            throw new IllegalArgumentException("Debe incluir al menos una habitación");
        }

        double totalGeneral = 0;
        for (HabitacionReservaRequestDTO item : request.getHabitaciones()) {

            LocalDate checkIn  = LocalDate.parse(item.getFechaCheckIn());
            LocalDate checkOut = LocalDate.parse(item.getFechaCheckOut());
            LocalDate hoy      = LocalDate.now();

            // Fechas no pueden ser en el pasado
            if (checkIn.isBefore(hoy)) {
                throw new IllegalArgumentException(
                        "La fecha de check-in no puede ser anterior a hoy"
                );
            }
            if (checkOut.isBefore(hoy)) {
                throw new IllegalArgumentException(
                        "La fecha de check-out no puede ser anterior a hoy"
                );
            }

            // Check-out debe ser al menos 1 día después del check-in
            long dias = ChronoUnit.DAYS.between(checkIn, checkOut);
            if (dias < 1) {
                throw new IllegalArgumentException(
                        "La fecha de check-out debe ser al menos 1 día después del check-in"
                );
            }

            Date fechaCheckIn  = Date.valueOf(checkIn);
            Date fechaCheckOut = Date.valueOf(checkOut);

            if (reservacionRepository.existeTraslape(item.getHabitacionId(), fechaCheckIn, fechaCheckOut)) {
                throw new IllegalArgumentException(
                        "La habitación " + item.getHabitacionId() +
                                " no está disponible para las fechas seleccionadas"
                );
            }

            // precios[0] = precioPorNoche, precios[1] = precioPorPersona, precios[2] = capacidadMaxima
            double[] precios = reservacionRepository.obtenerPrecios(item.getHabitacionId());
            double precioPorNoche   = precios[0];
            double precioPorPersona = precios[1];
            int capacidadMaxima     = (int) precios[2];

            int personasSolicitadas = item.getCantidadPersonas();

            // Validar que no exceda capacidad + 1 (máximo 1 persona extra por habitación)
            if (personasSolicitadas > capacidadMaxima + 1) {
                throw new IllegalArgumentException(
                        "La habitación " + item.getHabitacionId() +
                                " tiene capacidad máxima de " + capacidadMaxima +
                                " personas (+1 extra). No se pueden alojar " + personasSolicitadas + " personas."
                );
            }

            // Calcular personas extra (las que exceden la capacidad base)
            int personasExtra = Math.max(0, personasSolicitadas - capacidadMaxima);

            // Total = (noches * precioPorNoche) + (personasExtra * noches * precioPorPersona)
            totalGeneral += (dias * precioPorNoche) + (personasExtra * dias * precioPorPersona);
        }

        String noReservacion      = "MIKU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Timestamp fechaCreacion   = Timestamp.valueOf(LocalDateTime.now());
        Timestamp fechaExpiracion = Timestamp.valueOf(LocalDateTime.now().plusMinutes(10));

        int reservacionId = reservacionRepository.crearReservacion(
                noReservacion, totalGeneral, usuarioId, fechaCreacion, fechaExpiracion
        );

        reservacionRepository.expirarPendientesDeUsuario(usuarioId, reservacionId);

        for (HabitacionReservaRequestDTO item : request.getHabitaciones()) {
            LocalDate checkIn  = LocalDate.parse(item.getFechaCheckIn());
            LocalDate checkOut = LocalDate.parse(item.getFechaCheckOut());
            long dias = ChronoUnit.DAYS.between(checkIn, checkOut);

            double[] precios        = reservacionRepository.obtenerPrecios(item.getHabitacionId());
            double precioPorNoche   = precios[0];
            double precioPorPersona = precios[1];
            int capacidadMaxima     = (int) precios[2];

            int personasSolicitadas = item.getCantidadPersonas();
            int personasExtra       = Math.max(0, personasSolicitadas - capacidadMaxima);

            double totalHabitacion = (dias * precioPorNoche) + (personasExtra * dias * precioPorPersona);

            reservacionRepository.crearDetalle(
                    reservacionId,
                    item.getHabitacionId(),
                    Date.valueOf(checkIn),
                    Date.valueOf(checkOut),
                    item.getCantidadPersonas(),
                    totalHabitacion
            );
        }

        Object[] datos = reservacionRepository.obtenerReservacion(reservacionId);

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
        List<ReservacionDetalleDTO> reservaciones = reservacionRepository.obtenerReservacionesDeUsuario(usuarioId);

        for (ReservacionDetalleDTO dto : reservaciones) {
            dto.setImagenesHotelIds(reservacionRepository.obtenerImagenesHotel(dto.getHotelId()));
            dto.setImagenesHabitacionIds(reservacionRepository.obtenerImagenesHabitacion(dto.getHabitacionId()));
        }

        return reservaciones;
    }
}