package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.*;

import java.sql.Date;
import java.util.List;

/**
 * Repository para autenticacion y busqueda de hoteles desde el canal de aerolineas aliadas.
 * Tambien gestiona el proceso de handshake con aerolineas externas, incluyendo
 * la busqueda por URL y la persistencia de tokens de autenticacion.
 */
public class AerolineaAliadaRepository {

    /**
     * Retorna la identidad de la aerolinea activa que corresponde al token dado.
     *
     * @param token hash del token de la aerolinea aliada.
     * @return AerolineaIdentidadDTO si existe y esta activa, null si no.
     */
    public AerolineaIdentidadDTO obtenerAerolineaPorToken(String token) {
        String sql = "SELECT a.ID, a.Nombre, a.URLParaUsuario " +
                "FROM AerolineaAliado a " +
                "JOIN EstadoAliado e ON a.EstadoID = e.ID " +
                "WHERE a.TokenHASH = ? AND LOWER(TRIM(e.Estado)) = 'activo'";

        List<AerolineaIdentidadDTO> result = DatabaseManager.executeQuery(sql, rs ->
                new AerolineaIdentidadDTO(
                        rs.getInt("ID"),
                        rs.getString("Nombre"),
                        rs.getString("URLParaUsuario")
                ), token
        );
        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * Retorna el porcentaje de descuento de la aerolinea activa identificada por su token.
     *
     * @param token hash del token de la aerolinea aliada.
     * @return porcentaje de descuento como Double, o null si no se encuentra.
     */
    public Double obtenerDescuentoAerolinea(String token) {
        String sql = "SELECT a.PorcentajeDescuento " +
                "FROM AerolineaAliado a " +
                "JOIN EstadoAliado e ON a.EstadoID = e.ID " +
                "WHERE a.TokenHASH = ? AND LOWER(TRIM(e.Estado)) = 'activo'";

        List<Double> result = DatabaseManager.executeQuery(
                sql, rs -> rs.getDouble("PorcentajeDescuento"), token
        );
        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * Busca el ID de una ciudad comparando nombre de ciudad y pais sin distincion de mayusculas.
     *
     * @param nombreCiudad nombre de la ciudad a buscar.
     * @param nombrePais   nombre del pais al que pertenece la ciudad.
     * @return ID de la ciudad, o null si no se encuentra.
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
     * Registra una busqueda realizada desde una aerolinea aliada (TipoBusquedaID = 3).
     * Se guarda sin usuario porque la sesion pertenece al sistema de la aerolinea.
     *
     * @param ciudadId         ID de la ciudad destino de la busqueda.
     * @param fechaCheckIn     fecha de entrada solicitada.
     * @param fechaCheckOut    fecha de salida solicitada.
     * @param cantidadPersonas numero de personas para la busqueda.
     */
    public void guardarBusqueda(int ciudadId, Date fechaCheckIn, Date fechaCheckOut,
                                int cantidadPersonas) {
        String sql = "INSERT INTO Busqueda " +
                "(CiudadID, FechaCheckIn, FechaCheckOut, CantidadPersonas, UsuarioID, AgenciaID, TipoBusquedaID, Fecha) " +
                "VALUES (?, ?, ?, ?, NULL, NULL, 3, SYSDATE)";
        DatabaseManager.executeUpdate(sql, ciudadId, fechaCheckIn, fechaCheckOut, cantidadPersonas);
    }

    // ── Busqueda de hoteles — identicos a BusquedaAgenciaRepository ───────────

    /**
     * Busca hoteles activos en la ciudad indicada.
     *
     * @param ciudadId ID de la ciudad a buscar.
     * @return lista de HotelResultadoDTO con los hoteles disponibles.
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
     *
     * @param hotelId ID del hotel.
     * @return lista de IDs de imagenes.
     */
    public List<Integer> buscarImagenesHotel(int hotelId) {
        String sql = "SELECT ID FROM ImagenHotel WHERE HotelID = ?";
        return DatabaseManager.executeQuery(sql, rs -> rs.getInt("ID"), hotelId);
    }

    /**
     * Retorna las amenidades registradas para un hotel con sus descripciones.
     *
     * @param hotelId ID del hotel.
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
     *
     * @param hotelAmenidadId ID del registro HotelAmenidad.
     * @return lista de IDs de imagenes.
     */
    public List<Integer> buscarImagenesAmenidad(int hotelAmenidadId) {
        String sql = "SELECT ID FROM ImagenHotelAmenidad WHERE HotelAmenidadID = ?";
        return DatabaseManager.executeQuery(sql, rs -> rs.getInt("ID"), hotelAmenidadId);
    }

    /**
     * Busca tipos de habitacion disponibles en un hotel segun fechas y capacidad minima.
     *
     * @param hotelId          ID del hotel.
     * @param capacidadMinima  numero minimo de personas que debe aceptar el tipo de habitacion.
     * @param fechaCheckIn     fecha de inicio de la estancia.
     * @param fechaCheckOut    fecha de fin de la estancia.
     * @return lista de TipoHabitacionResultadoDTO disponibles para el rango de fechas.
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
     * Busca habitaciones disponibles de un tipo especifico en un hotel y rango de fechas.
     *
     * @param hotelId          ID del hotel.
     * @param tipoHabitacionId ID del tipo de habitacion.
     * @param fechaCheckIn     fecha de inicio de la estancia.
     * @param fechaCheckOut    fecha de fin de la estancia.
     * @return lista de HabitacionResumenDTO con ID y numero de habitacion disponibles.
     */
    public List<HabitacionResumenDTO> buscarHabitacionesResumenPorTipo(
            int hotelId, int tipoHabitacionId, Date fechaCheckIn, Date fechaCheckOut) {

        String sql = "SELECT h.ID, h.NUMEROHABITACION " +
                "FROM Habitacion h " +
                "JOIN EstadoHabitacion e ON h.ESTADO_ID = e.ID " +
                "WHERE h.HOTELID = ? " +
                "AND h.TIPOHABITACIONID = ? " +
                "AND LOWER(TRIM(e.TIPO_DE_CLASE)) = 'activa' " +
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
     * Retorna los IDs de las imagenes asociadas a una habitacion.
     *
     * @param habitacionId ID de la habitacion.
     * @return lista de IDs de imagenes.
     */
    public List<Integer> buscarImagenesHabitacion(int tipoHabitacionId) {
        String sql = "SELECT ID FROM ImagenHabitacion WHERE TipoHabitacionID = ?";
        return DatabaseManager.executeQuery(sql, rs -> rs.getInt("ID"), tipoHabitacionId);
    }

    // ── Handshake con aerolineas aliadas ──────────────────────────────────────

    /**
     * Busca el ID de una aerolinea aliada a partir de su URL registrada en la base de datos.
     * Se usa durante el handshake para identificar a la aerolinea que se esta autenticando.
     *
     * @param urlAerolinea URL unica asociada a la aerolinea aliada (campo URL en AerolineaAliado).
     * @return ID de la aerolinea aliada, o null si no se encuentra ninguna con esa URL.
     */
    public Integer obtenerAerolineaIdPorURL(String urlAerolinea) {
        String sql = "SELECT ID FROM AerolineaAliado WHERE URL = ?";
        List<Integer> result = DatabaseManager.executeQuery(
                sql, rs -> rs.getInt("ID"), urlAerolinea);
        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * Busca el ID y porcentaje de descuento de una aerolinea aliada a partir de su URL registrada.
     * Se usa durante el handshake para obtener el porcentaje de ganancia configurado.
     *
     * @param urlAerolinea URL unica asociada a la aerolinea aliada.
     * @return AerolineaDTO con ID y PorcentajeDescuento, o null si no se encuentra.
     */
    public AerolineaDTO obtenerAerolineaConPorcentajePorURL(String urlAerolinea) {
        String sql = "SELECT ID, PorcentajeDescuento FROM AerolineaAliado WHERE URL = ?";
        List<AerolineaDTO> result = DatabaseManager.executeQuery(sql, rs -> {
            AerolineaDTO dto = new AerolineaDTO();
            dto.setId(rs.getInt("ID"));
            dto.setPorcentajeDescuento(rs.getDouble("PorcentajeDescuento"));
            return dto;
        }, urlAerolinea);
        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * Persiste el token de sesion del handshake en el registro de la aerolinea aliada.
     *
     * Se guarda el token de SALIDA (generado por el hotel) y NO el de entrada.
     * De esta forma ambas bases de datos quedan con el mismo token:
     *   - Oracle  (hotel)     : TokenHASH = tokenSalida  (guardado aqui)
     *   - SQL Server (aerolinea): TokenHASH = tokenSalida  (guardado por HandshakeHotelService)
     *
     * Cuando la aerolinea llame al hotel en requests futuros enviara ese tokenSalida,
     * y el hotel podra validarlo buscando en AerolineaAliado WHERE TokenHASH = tokenSalida.
     *
     * @param aerolineaId  ID del registro AerolineaAliado a actualizar.
     * @param tokenEntrada token enviado por la aerolinea (no se persiste, ya no es necesario).
     * @param tokenSalida  token generado por el hotel; se guarda en ambas BDs para que coincidan.
     * @return true si se actualizo al menos un registro, false si no se encontro la aerolinea.
     */
    public boolean guardarTokensAerolinea(int aerolineaId, String tokenEntrada, String tokenSalida) {
        // Se persiste tokenSalida (generado por el hotel) para que coincida con lo que
        // guarda la aerolinea en su propia BD despues de recibir la respuesta del handshake
        String sql = "UPDATE AerolineaAliado SET TokenHASH = ? WHERE ID = ?";
        return DatabaseManager.executeUpdate(sql, tokenSalida, aerolineaId) > 0;
    }
}