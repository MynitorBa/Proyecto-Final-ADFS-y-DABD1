package org.example.services;

import org.example.dtos.HabitacionAgenciaResponseDTO;
import org.example.dtos.HabitacionReservaRequestDTO;
import org.example.dtos.ReservacionAgenciaResponseDTO;
import org.example.dtos.ReservacionDetalleDTO;
import org.example.dtos.ReservacionRequestDTO;
import org.example.repositories.ReservacionAgenciaRepository;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReservacionAgenciaService {

    private final ReservacionAgenciaRepository repository = new ReservacionAgenciaRepository();

    public ReservacionAgenciaResponseDTO crearReservacion(ReservacionRequestDTO request, int agenciaId) {

        int[] datosAgencia = repository.obtenerDatosAgencia(agenciaId);
        if (datosAgencia == null)
            throw new IllegalArgumentException("La agencia no está activa");

        int    usuarioWebisId      = datosAgencia[0];
        double porcentajeDescuento = repository.obtenerDescuentoAgencia(agenciaId);

        if (request.getHabitaciones() == null || request.getHabitaciones().isEmpty())
            throw new IllegalArgumentException("Debe incluir al menos una habitación");

        double factor = 1.0 - (porcentajeDescuento / 100.0);

        double totalGeneral = 0;
        List<HabitacionAgenciaResponseDTO> desglose = new ArrayList<>();

        for (HabitacionReservaRequestDTO item : request.getHabitaciones()) {
            LocalDate checkIn  = LocalDate.parse(item.getFechaCheckIn());
            LocalDate checkOut = LocalDate.parse(item.getFechaCheckOut());
            long dias = ChronoUnit.DAYS.between(checkIn, checkOut);

            if (dias <= 0)
                throw new IllegalArgumentException(
                        "Las fechas de la habitación " + item.getHabitacionId() + " son inválidas");

            Date fechaCheckIn  = Date.valueOf(checkIn);
            Date fechaCheckOut = Date.valueOf(checkOut);

            if (repository.existeTraslape(item.getHabitacionId(), fechaCheckIn, fechaCheckOut))
                throw new IllegalArgumentException(
                        "La habitación " + item.getHabitacionId() +
                                " no está disponible para las fechas seleccionadas");

            double[] precios = repository.obtenerPrecios(item.getHabitacionId());

            double precioPorNoche   = Math.round(precios[0] * factor * 100.0) / 100.0;
            double precioPorPersona = Math.round(precios[1] * factor * 100.0) / 100.0;
            int    capacidadMaxima  = precios.length > 2 ? (int) precios[2] : Integer.MAX_VALUE;
            int    personasExtra    = Math.max(0, item.getCantidadPersonas() - capacidadMaxima);

            double totalHab = Math.round(
                    ((dias * precioPorNoche) + (personasExtra * dias * precioPorPersona)) * 100.0
            ) / 100.0;

            totalGeneral += totalHab;

            HabitacionAgenciaResponseDTO habDTO = new HabitacionAgenciaResponseDTO();
            habDTO.setHabitacionId(item.getHabitacionId());
            habDTO.setPrecioPorNoche(precioPorNoche);
            habDTO.setPrecioPorPersona(precioPorPersona);
            habDTO.setPersonasExtra(personasExtra);
            habDTO.setNoches((int) dias);
            habDTO.setTotal(totalHab);
            desglose.add(habDTO);
        }

        totalGeneral = Math.round(totalGeneral * 100.0) / 100.0;

        String noReservacion      = "MIKU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Timestamp fechaCreacion   = Timestamp.valueOf(LocalDateTime.now());
        Timestamp fechaExpiracion = Timestamp.valueOf(LocalDateTime.now().plusMinutes(15));

        int reservacionId = repository.crearReservacion(
                noReservacion, totalGeneral, usuarioWebisId, fechaCreacion, fechaExpiracion
        );

        for (int i = 0; i < desglose.size(); i++) {
            HabitacionAgenciaResponseDTO hab = desglose.get(i);
            HabitacionReservaRequestDTO item = request.getHabitaciones().get(i);
            repository.crearDetalle(
                    reservacionId,
                    hab.getHabitacionId(),
                    Date.valueOf(LocalDate.parse(item.getFechaCheckIn())),
                    Date.valueOf(LocalDate.parse(item.getFechaCheckOut())),
                    item.getCantidadPersonas(),
                    hab.getTotal()
            );
        }

        Object[] datos = repository.obtenerReservacion(reservacionId);

        ReservacionAgenciaResponseDTO response = new ReservacionAgenciaResponseDTO();
        response.setId((int) datos[0]);
        response.setNoReservacion((String) datos[1]);
        response.setTotal((double) datos[2]);
        response.setFechaCreacion((String) datos[3]);
        response.setFechaExpiracion((String) datos[4]);
        response.setEstado((String) datos[5]);
        response.setHabitaciones(desglose);
        return response;
    }

    public List<ReservacionDetalleDTO> obtenerReservaciones(int agenciaId) {
        return repository.obtenerReservacionesDeAgencia(agenciaId);
    }

    public void expirarReservacion(int reservacionId, int agenciaId) {
        boolean valida = repository.perteneceAAgenciaYEstaPendiente(reservacionId, agenciaId);

        if (!valida)
            throw new IllegalArgumentException(
                    "La reservación no existe, no pertenece a esta agencia, o no está en estado pendiente");

        repository.expirarReservacion(reservacionId);
    }

    public List<ReservacionDetalleDTO> obtenerDetalleReservacion(int reservacionId, int agenciaId) {
        List<ReservacionDetalleDTO> detalles = repository.obtenerDetalleReservacionAgencia(reservacionId, agenciaId);

        if (detalles == null || detalles.isEmpty())
            throw new IllegalArgumentException("Reservación no encontrada o no pertenece a esta agencia");

        for (ReservacionDetalleDTO dto : detalles) {
            dto.setImagenesHotelIds(repository.obtenerImagenesHotel(dto.getHotelId()));
            dto.setImagenesHabitacionIds(repository.obtenerImagenesHabitacion(dto.getHabitacionId()));
        }

        return detalles;
    }
}