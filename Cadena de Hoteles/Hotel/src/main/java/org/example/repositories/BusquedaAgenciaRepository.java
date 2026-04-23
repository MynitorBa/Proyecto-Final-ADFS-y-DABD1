package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.AmenidadHotelDTO;
import org.example.dtos.HotelResultadoDTO;
import org.example.dtos.TipoHabitacionResultadoDTO;
import org.example.dtos.HabitacionResumenDTO;

import java.sql.Date;
import java.util.List;

/**
 * Repository para la busqueda de hoteles y habitaciones disponibles desde el canal de agencias.
 * Maneja consultas de disponibilidad, descuentos, imagenes y registro de busquedas.
 */
public class BusquedaAgenciaRepository {

    /**
     * Retorna el porcentaje de descuento de la agencia activa asociada a un usuario webservice.
     * @param usuarioId ID del usuario webservice vinculado a la agencia.
     * @return porcentaje de descuento como Double, o null si no tiene agencia activa.
     */
    public Double obtenerDescuentoAgencia(int usuarioId) {
        String sql = "SELECT PorcentajeDescuento FROM Agencia " +
                "WHERE UsuarioWEBIs_ID = ? AND EstadoID = " +
                "(SELECT ID FROM EstadoAgencia WHERE LOWER(Estado) = 'activa')";
        List<Double> result = DatabaseManager.executeQuery(
                sql, rs -> rs.getDouble("PorcentajeDescuento"), usuarioId
        );
        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * Busca el ID de una ciudad comparando nombre de ciudad y nombre de pais sin distincion de mayusculas.
     * @param nombreCiudad nombre de la ciudad a buscar.
     * @param nombrePais   nombre del pais al que pertenece la ciudad.
     * @return ID de la ciudad si existe, o null si no se encuentra.
     */
    public Integer buscarCiudadId(String nombreCiudad, String nombrePais) {
        String sql = "SELECT c.ID FROM Ciudad c JOIN Pais p ON c.Pais_ID = p.ID " +
                "WHERE LOWER(TRIM(c.Nombre)) = LOWER(TRIM(?)) AND LOWER(TRIM(p.Nombre)) = LOWER(TRIM(?))";
        List<Integer> result = DatabaseManager.executeQuery(
                sql, rs -> rs.getInt("ID"), nombreCiudad, nombrePais
        );
        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * Registra una busqueda realizada por un usuario webservice autenticado (TipoBusquedaID = 2).
     * @param ciudadId        ID de la ciudad consultada.
     * @param fechaCheckIn    fecha de entrada solicitada.
     * @param fechaCheckOut   fecha de salida solicitada.
     * @param cantidadPersonas numero de personas para la busqueda.
     * @param usuarioId       ID del usuario webservice que realiza la busqueda.
     */
    public void guardarBusqueda(int ciudadId, Date fechaCheckIn, Date fechaCheckOut,
                                int cantidadPersonas, int usuarioId) {
        String sql = "INSERT INTO Busqueda " +
                "(CiudadID, FechaCheckIn, FechaCheckOut, CantidadPersonas, UsuarioID, AgenciaID, TipoBusquedaID, Fecha) " +
                "VALUES (?, ?, ?, ?, ?, NULL, 2, SYSDATE)";
        DatabaseManager.executeUpdate(sql, ciudadId, fechaCheckIn, fechaCheckOut, cantidadPersonas, usuarioId);
    }

    /**
     * Retorna los hoteles activos ubicados en una ciudad especifica.
     * @param ciudadId ID de la ciudad donde se buscan los hoteles.
     * @return lista de HotelResultadoDTO con los hoteles activos encontrados.
     */
    public List<HotelResultadoDTO> buscarHotelesPorCiudad(int ciudadId) {
        String sql = "SELECT h.ID, h.Nombre, h.Direccion, h.Descripcion, h.Rating, " +
                "e.Estado, c.Nombre AS Ciudad, p.Nombre AS Pais " +
                "FROM Hotel h " +
                "JOIN Estado  e ON h.EstadoID = e.ID " +
                "JOIN Ciudad  c ON h.CiudadID = c.ID " +
                "JOIN Pais    p ON c.Pais_ID  = p.ID " +
                "WHERE h.CiudadID = ? AND LOWER(TRIM(e.Estado)) = 'activo'";

        return DatabaseManager.executeQuery(sql, rs -> {
            HotelResultadoDTO dto = new HotelResultadoDTO();
            dto.setId(rs.getInt("ID"));
            dto.setNombre(rs.getString("Nombre"));
            dto.setDireccion(rs.getString("Direccion"));
            dto.setDescripcion(rs.getString("Descripcion"));
            dto.setRating(rs.getDouble("Rating"));
            dto.setEstado(rs.getString("Estado"));
            dto.setCiudad(rs.getString("Ciudad"));
            dto.setPais(rs.getString("Pais"));
            return dto;
        }, ciudadId);
    }

    /**
     * Retorna los IDs de las imagenes asociadas a un hotel.
     * @param hotelId ID del hotel del que se quieren obtener las imagenes.
     * @return lista de IDs de imagenes del hotel.
     */
    public List<Integer> buscarImagenesHotel(int hotelId) {
        String sql = "SELECT ID FROM ImagenHotel WHERE HotelID = ?";
        return DatabaseManager.executeQuery(sql, rs -> rs.getInt("ID"), hotelId);
    }

    /**
     * Retorna las amenidades registradas para un hotel junto con su descripcion y nombre.
     * @param hotelId ID del hotel del que se quieren obtener las amenidades.
     * @return lista de AmenidadHotelDTO con los datos de cada amenidad.
     */
    public List<AmenidadHotelDTO> buscarAmenidadesHotel(int hotelId) {
        String sql = "SELECT ha.ID AS HotelAmenidadId, ha.AmenidadID, ha.Descripcion, " +
                "a.nombre AS NombreAmenidad " +
                "FROM HotelAmenidad ha " +
                "JOIN Amenidad a ON ha.AmenidadID = a.ID " +
                "WHERE ha.HotelID = ?";

        return DatabaseManager.executeQuery(sql, rs -> {
            AmenidadHotelDTO dto = new AmenidadHotelDTO();
            dto.setHotelAmenidadId(rs.getInt("HotelAmenidadId"));
            dto.setAmenidadId(rs.getInt("AmenidadID"));
            dto.setNombre(rs.getString("NombreAmenidad"));
            dto.setDescripcion(rs.getString("Descripcion"));
            return dto;
        }, hotelId);
    }

    /**
     * Retorna los IDs de las imagenes asociadas a una amenidad de hotel.
     * @param hotelAmenidadId ID del registro HotelAmenidad del que se buscan las imagenes.
     * @return lista de IDs de imagenes de la amenidad.
     */
    public List<Integer> buscarImagenesAmenidad(int hotelAmenidadId) {
        String sql = "SELECT ID FROM ImagenHotelAmenidad WHERE HotelAmenidadID = ?";
        return DatabaseManager.executeQuery(sql, rs -> rs.getInt("ID"), hotelAmenidadId);
    }

    /**
     * Retorna los tipos de habitacion disponibles en un hotel para un rango de fechas y capacidad minima.
     * Excluye habitaciones con reservaciones activas (Pendiente o Confirmada) que se traslapen con el rango solicitado.
     * El precio y tipo de cama se obtienen desde TipoHabitacion, no desde Habitacion.
     * @param hotelId         ID del hotel donde se busca disponibilidad.
     * @param capacidadMinima capacidad minima requerida por tipo de habitacion.
     * @param fechaCheckIn    fecha de entrada solicitada.
     * @param fechaCheckOut   fecha de salida solicitada.
     * @return lista de TipoHabitacionResultadoDTO con los tipos de habitacion disponibles.
     */
    public List<TipoHabitacionResultadoDTO> buscarTiposHabitacionDisponibles(
            int hotelId, int capacidadMinima, Date fechaCheckIn, Date fechaCheckOut) {

        String sql = "SELECT t.ID AS TipoID, t.NOMBRE AS TipoHabitacion, " +
                "t.PRECIOPERSONA, t.PRECIONOCHE, t.CAPACIDADMAXIMA, " +
                "t.METROSCUADRADOS, c.TIPO_DE_CLASE AS TipoCama " +
                "FROM TipoHabitacion t " +
                "JOIN Cama c ON t.TIPOCAMAID = c.ID " +
                "WHERE t.CAPACIDADMAXIMA >= ? " +
                "AND EXISTS (" +
                "  SELECT 1 FROM Habitacion h " +
                "  JOIN EstadoHabitacion e ON h.ESTADO_ID = e.ID " +
                "  WHERE h.TIPOHABITACIONID = t.ID " +
                "  AND h.HOTELID = ? " +
                "  AND LOWER(TRIM(e.TIPO_DE_CLASE)) = 'activa' " +
                "  AND h.ID NOT IN (" +
                "    SELECT dr.HabitacionID FROM DetallesReservacion dr " +
                "    JOIN Reservacion r ON dr.ReservacionID = r.ID " +
                "    JOIN EstadoReserva er ON r.EstadoID = er.ID " +
                "    WHERE LOWER(TRIM(er.Estado)) IN ('pendiente', 'confirmada') " +
                "    AND dr.FechaCheckIn < ? AND dr.FechaCheckOut > ?" +
                "  )" +
                ")";

        return DatabaseManager.executeQuery(sql, rs -> {
            TipoHabitacionResultadoDTO dto = new TipoHabitacionResultadoDTO();
            dto.setTipoHabitacionId(rs.getInt("TipoID"));
            dto.setTipoHabitacion(rs.getString("TipoHabitacion"));
            dto.setPrecioPorPersona(rs.getDouble("PRECIOPERSONA"));
            dto.setPrecioPorNoche(rs.getDouble("PRECIONOCHE"));
            dto.setCapacidadMaxima(rs.getInt("CAPACIDADMAXIMA"));
            dto.setMetrosCuadrados(rs.getDouble("METROSCUADRADOS"));
            dto.setTipoCama(rs.getString("TipoCama"));
            return dto;
        }, capacidadMinima, hotelId, fechaCheckOut, fechaCheckIn);
    }

    /**
     * Retorna las habitaciones concretas disponibles de un tipo especifico en un hotel para un rango de fechas.
     * Excluye habitaciones con reservaciones activas que se traslapen con el rango solicitado.
     * @param hotelId          ID del hotel donde se busca disponibilidad.
     * @param tipoHabitacionId ID del tipo de habitacion requerido.
     * @param fechaCheckIn     fecha de entrada solicitada.
     * @param fechaCheckOut    fecha de salida solicitada.
     * @return lista de HabitacionResumenDTO con el ID y numero de cada habitacion disponible.
     */
    public List<HabitacionResumenDTO> buscarHabitacionesResumenPorTipo(
            int hotelId, int tipoHabitacionId, Date fechaCheckIn, Date fechaCheckOut) {

        String sql = "SELECT h.ID, h.NUMEROHABITACION " +
                "FROM Habitacion h " +
                "WHERE h.HOTELID = ? " +
                "AND h.TIPOHABITACIONID = ? " +
                "AND h.ESTADO_ID = 1 " +
                "AND h.ID NOT IN (" +
                "  SELECT dr.HabitacionID FROM DetallesReservacion dr " +
                "  JOIN Reservacion r ON dr.ReservacionID = r.ID " +
                "  JOIN EstadoReserva er ON r.EstadoID = er.ID " +
                "  WHERE LOWER(TRIM(er.Estado)) IN ('pendiente', 'confirmada') " +
                "  AND dr.FechaCheckIn < ? AND dr.FechaCheckOut > ?" +
                ")";

        return DatabaseManager.executeQuery(sql, rs -> {
            HabitacionResumenDTO dto = new HabitacionResumenDTO();
            dto.setId(rs.getInt("ID"));
            dto.setNumeroHabitacion(rs.getString("NUMEROHABITACION"));
            return dto;
        }, hotelId, tipoHabitacionId, fechaCheckOut, fechaCheckIn);
    }

    /**
     * Retorna los IDs de las imagenes asociadas a una habitacion especifica.
     * @param habitacionId ID de la habitacion de la que se quieren obtener las imagenes.
     * @return lista de IDs de imagenes de la habitacion.
     */
    public List<Integer> buscarImagenesHabitacion(int habitacionId) {
        String sql = "SELECT ID FROM ImagenHabitacion WHERE HabitacionID = ?";
        return DatabaseManager.executeQuery(sql, rs -> rs.getInt("ID"), habitacionId);
    }

    /**
     * Registra una busqueda anonima sin usuario autenticado (TipoBusquedaID = 2).
     * @param ciudadId         ID de la ciudad consultada.
     * @param fechaCheckIn     fecha de entrada solicitada.
     * @param fechaCheckOut    fecha de salida solicitada.
     * @param cantidadPersonas numero de personas para la busqueda.
     */
    public void guardarBusquedaSinUsuario(int ciudadId, Date fechaCheckIn, Date fechaCheckOut,
                                          int cantidadPersonas) {
        String sql = "INSERT INTO Busqueda " +
                "(CiudadID, FechaCheckIn, FechaCheckOut, CantidadPersonas, UsuarioID, AgenciaID, TipoBusquedaID, Fecha) " +
                "VALUES (?, ?, ?, ?, NULL, NULL, 2, SYSDATE)";
        DatabaseManager.executeUpdate(sql, ciudadId, fechaCheckIn, fechaCheckOut, cantidadPersonas);
    }

    /**
     * Retorna el porcentaje de descuento de la agencia activa identificada por su token de entrada.
     * @param token hash del token de entrada asociado a la agencia.
     * @return porcentaje de descuento como Double, o null si no se encuentra una agencia activa con ese token.
     */
    public Double obtenerDescuentoAgenciaPorToken(String token) {
        String sql = "SELECT PorcentajeDescuento FROM Agencia " +
                "WHERE Token_HASH_Entrada = ? AND EstadoID = " +
                "(SELECT ID FROM EstadoAgencia WHERE LOWER(Estado) = 'activa')";
        List<Double> result = DatabaseManager.executeQuery(
                sql, rs -> rs.getDouble("PorcentajeDescuento"), token
        );
        return result.isEmpty() ? null : result.get(0);
    }
}