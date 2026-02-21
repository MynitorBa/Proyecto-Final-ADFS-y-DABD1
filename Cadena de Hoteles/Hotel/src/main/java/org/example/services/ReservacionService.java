package org.example.services;

import org.example.dtos.HabitacionReservaRequestDTO;
import org.example.dtos.ReservacionRequestDTO;
import org.example.dtos.ReservacionResponseDTO;
import org.example.repositories.ReservacionRepository;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class ReservacionService {

    private final ReservacionRepository reservacionRepository = new ReservacionRepository();

    public ReservacionResponseDTO crearReservacion(ReservacionRequestDTO request, int usuarioId) {

        if (request.getHabitaciones() == null || request.getHabitaciones().isEmpty()) {
            throw new IllegalArgumentException("Debe incluir al menos una habitación");
        }

        // Verificar traslapes y calcular totales
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

            // Verificar que la habitación no tenga traslape con otra reservación
            boolean hayTraslape = reservacionRepository.existeTraslape(
                    item.getHabitacionId(), fechaCheckIn, fechaCheckOut
            );
            if (hayTraslape) {
                throw new IllegalArgumentException(
                        "La habitación " + item.getHabitacionId() +
                                " no está disponible para las fechas seleccionadas"
                );
            }

            double[] precios        = reservacionRepository.obtenerPrecios(item.getHabitacionId());
            double precioPorNoche   = precios[0];
            double precioPorPersona = precios[1];

            totalGeneral += (dias * precioPorNoche) +
                    (dias * item.getCantidadPersonas() * precioPorPersona);
        }

        // Generar número de reservación único
        String noReservacion = "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // fechas
        Timestamp fechaCreacion   = Timestamp.valueOf(LocalDateTime.now());
        Timestamp fechaExpiracion = Timestamp.valueOf(LocalDateTime.now().plusMinutes(10));

        // rear la reservación con estado Pendiente (ID=1)
        int reservacionId = reservacionRepository.crearReservacion(
                noReservacion, totalGeneral, usuarioId, fechaCreacion, fechaExpiracion
        );

        //  Insertar cada detalle
        for (HabitacionReservaRequestDTO item : request.getHabitaciones()) {

            LocalDate checkIn  = LocalDate.parse(item.getFechaCheckIn());
            LocalDate checkOut = LocalDate.parse(item.getFechaCheckOut());
            long dias = ChronoUnit.DAYS.between(checkIn, checkOut);

            double[] precios        = reservacionRepository.obtenerPrecios(item.getHabitacionId());
            double precioPorNoche   = precios[0];
            double precioPorPersona = precios[1];

            double totalHabitacion = (dias * precioPorNoche) +
                    (dias * item.getCantidadPersonas() * precioPorPersona);

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
}