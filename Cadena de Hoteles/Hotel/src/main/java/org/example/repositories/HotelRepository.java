package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.AmenidadDTO;
import org.example.dtos.HabitacionAdminDTO;
import org.example.dtos.HotelAdminDTO;
import org.example.dtos.HotelAmenidadDTO;

import java.util.List;

/**
 * Repository para la gestion completa de hoteles desde el panel de administracion.
 * Cubre amenidades, habitaciones, imagenes, reservaciones y metricas del sistema.
 */
public class HotelRepository {

    /**
     * Retorna el catalogo completo de amenidades disponibles, ordenadas por ID.
     * @return lista de AmenidadDTO con el ID y nombre de cada amenidad.
     */
    public List<AmenidadDTO> listarAmenidades() {
        return DatabaseManager.executeQuery(
                "SELECT ID, Nombre FROM Amenidad ORDER BY ID",
                rs -> {
                    AmenidadDTO d = new AmenidadDTO();
                    d.setId(rs.getInt("ID"));
                    d.setNombre(rs.getString("Nombre"));
                    return d;
                }
        );
    }

    /**
     * Crea una nueva amenidad en el catalogo y retorna el ID generado.
     * @param nombre nombre de la amenidad a crear.
     * @return ID de la amenidad recien insertada.
     */
    public int crearAmenidad(String nombre) {
        return DatabaseManager.executeInsertReturnId(
                "INSERT INTO Amenidad (Nombre) VALUES (?)",
                "ID", nombre.trim()
        );
    }

    /**
     * Retorna todos los hoteles registrados con su estado y ubicacion, ordenados por ID.
     * @return lista de HotelAdminDTO con los datos completos de cada hotel.
     */
    public List<HotelAdminDTO> listarTodos() {
        String sql = """
                SELECT h.ID, h.Nombre, h.Direccion, h.Descripcion, h.Rating,
                       h.EstadoID, e.Estado,
                       c.Nombre AS Ciudad,
                       p.Nombre AS Pais
                FROM   Hotel  h
                JOIN   Estado e ON h.EstadoID  = e.ID
                JOIN   Ciudad c ON h.CiudadID  = c.ID
                JOIN   Pais   p ON c.Pais_ID   = p.ID
                ORDER BY h.ID
                """;
        return DatabaseManager.executeQuery(sql, rs -> {
            HotelAdminDTO dto = new HotelAdminDTO();
            dto.setId(rs.getInt("ID"));
            dto.setNombre(rs.getString("Nombre"));
            dto.setDireccion(rs.getString("Direccion"));
            dto.setDescripcion(rs.getString("Descripcion"));
            dto.setRating(rs.getDouble("Rating"));
            dto.setEstadoId(rs.getInt("EstadoID"));
            dto.setEstado(rs.getString("Estado"));
            dto.setCiudad(rs.getString("Ciudad"));
            dto.setPais(rs.getString("Pais"));
            return dto;
        });
    }

    /**
     * Inserta un nuevo hotel en la base de datos y retorna el ID generado.
     * @param nombre      nombre del hotel.
     * @param direccion   direccion fisica del hotel.
     * @param descripcion descripcion general del hotel.
     * @param rating      calificacion inicial del hotel.
     * @param estadoId    ID del estado inicial del hotel.
     * @param ciudadId    ID de la ciudad donde se ubica el hotel.
     * @return ID del hotel recien insertado.
     */
    public int crearHotel(String nombre, String direccion, String descripcion,
                          double rating, int estadoId, int ciudadId) {
        String sql = "INSERT INTO Hotel (Nombre, Direccion, Descripcion, Rating, EstadoID, CiudadID) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        return DatabaseManager.executeInsertReturnId(sql, "ID",
                nombre, direccion, descripcion, rating, estadoId, ciudadId);
    }

    /**
     * Actualiza los datos principales de un hotel existente.
     * @param hotelId     ID del hotel a actualizar.
     * @param nombre      nuevo nombre del hotel.
     * @param direccion   nueva direccion del hotel.
     * @param descripcion nueva descripcion del hotel.
     * @param rating      nuevo rating del hotel.
     * @param estadoId    nuevo ID de estado del hotel.
     */
    public void actualizarHotel(int hotelId, String nombre, String direccion,
                                String descripcion, double rating, int estadoId) {
        DatabaseManager.executeUpdate(
                "UPDATE Hotel SET Nombre=?, Direccion=?, Descripcion=?, Rating=?, EstadoID=? WHERE ID=?",
                nombre, direccion, descripcion, rating, estadoId, hotelId);
    }

    /**
     * Elimina un hotel y todos sus registros dependientes en cascada.
     * Borra en orden: imagenes de habitaciones, habitaciones, imagenes de amenidades,
     * amenidades del hotel, imagenes del hotel y finalmente el hotel.
     * @param hotelId ID del hotel a eliminar.
     */
    public void eliminarHotel(int hotelId) {
        DatabaseManager.executeUpdate(
                "DELETE FROM ImagenHabitacion WHERE HabitacionID IN (SELECT ID FROM Habitacion WHERE HotelID=?)",
                hotelId);
        DatabaseManager.executeUpdate("DELETE FROM Habitacion WHERE HotelID=?", hotelId);
        DatabaseManager.executeUpdate(
                "DELETE FROM ImagenHotelAmenidad WHERE HotelAmenidadID IN (SELECT ID FROM HotelAmenidad WHERE HotelID=?)",
                hotelId);
        DatabaseManager.executeUpdate("DELETE FROM HotelAmenidad WHERE HotelID=?", hotelId);
        DatabaseManager.executeUpdate("DELETE FROM ImagenHotel WHERE HotelID=?", hotelId);
        DatabaseManager.executeUpdate("DELETE FROM Hotel WHERE ID=?", hotelId);
    }

    /**
     * Verifica si un hotel existe en la base de datos.
     * @param hotelId ID del hotel a verificar.
     * @return true si el hotel existe, false en caso contrario.
     */
    public boolean existe(int hotelId) {
        List<Integer> r = DatabaseManager.executeQuery(
                "SELECT COUNT(*) FROM Hotel WHERE ID=?", rs -> rs.getInt(1), hotelId);
        return !r.isEmpty() && r.get(0) > 0;
    }

    /**
     * Retorna la cantidad de habitaciones registradas para un hotel.
     * @param hotelId ID del hotel a consultar.
     * @return numero de habitaciones del hotel, o 0 si no tiene ninguna.
     */
    public int contarHabitaciones(int hotelId) {
        List<Integer> r = DatabaseManager.executeQuery(
                "SELECT COUNT(*) FROM Habitacion WHERE HotelID=?", rs -> rs.getInt(1), hotelId);
        return r.isEmpty() ? 0 : r.get(0);
    }

    /**
     * Retorna las amenidades asignadas a un hotel con su descripcion y nombre, ordenadas por ID.
     * @param hotelId ID del hotel del que se quieren obtener las amenidades.
     * @return lista de HotelAmenidadDTO con los datos de cada amenidad del hotel.
     */
    public List<HotelAmenidadDTO> listarAmenidadesHotel(int hotelId) {
        String sql = """
                SELECT ha.ID, ha.HotelID, ha.AmenidadID, a.Nombre AS AmenidadNombre, ha.Descripcion
                FROM   HotelAmenidad ha
                JOIN   Amenidad      a  ON ha.AmenidadID = a.ID
                WHERE  ha.HotelID = ?
                ORDER BY ha.ID
                """;
        return DatabaseManager.executeQuery(sql, rs -> {
            HotelAmenidadDTO d = new HotelAmenidadDTO();
            d.setId(rs.getInt("ID"));
            d.setHotelId(rs.getInt("HotelID"));
            d.setAmenidadId(rs.getInt("AmenidadID"));
            d.setAmenidadNombre(rs.getString("AmenidadNombre"));
            d.setDescripcion(rs.getString("Descripcion"));
            return d;
        }, hotelId);
    }

    /**
     * Verifica si un hotel ya tiene asignada una amenidad especifica.
     * @param hotelId    ID del hotel a verificar.
     * @param amenidadId ID de la amenidad a verificar.
     * @return true si la amenidad ya esta asignada al hotel, false en caso contrario.
     */
    public boolean tieneAmenidad(int hotelId, int amenidadId) {
        List<Integer> r = DatabaseManager.executeQuery(
                "SELECT COUNT(*) FROM HotelAmenidad WHERE HotelID=? AND AmenidadID=?",
                rs -> rs.getInt(1), hotelId, amenidadId);
        return !r.isEmpty() && r.get(0) > 0;
    }

    /**
     * Asigna una amenidad a un hotel con una descripcion personalizada y retorna el ID generado.
     * @param hotelId     ID del hotel al que se agrega la amenidad.
     * @param amenidadId  ID de la amenidad a agregar.
     * @param descripcion descripcion particular de la amenidad en ese hotel.
     * @return ID del registro HotelAmenidad recien insertado.
     */
    public int agregarAmenidadHotel(int hotelId, int amenidadId, String descripcion) {
        return DatabaseManager.executeInsertReturnId(
                "INSERT INTO HotelAmenidad (HotelID, AmenidadID, Descripcion) VALUES (?, ?, ?)",
                "ID", hotelId, amenidadId, descripcion);
    }

    /**
     * Actualiza la descripcion de una amenidad asignada a un hotel.
     * @param hotelAmenidadId ID del registro HotelAmenidad a actualizar.
     * @param descripcion     nueva descripcion de la amenidad.
     */
    public void actualizarAmenidadHotel(int hotelAmenidadId, String descripcion) {
        DatabaseManager.executeUpdate(
                "UPDATE HotelAmenidad SET Descripcion=? WHERE ID=?",
                descripcion, hotelAmenidadId);
    }

    /**
     * Elimina una amenidad de un hotel junto con todas sus imagenes asociadas.
     * @param hotelAmenidadId ID del registro HotelAmenidad a eliminar.
     */
    public void eliminarAmenidadHotel(int hotelAmenidadId) {
        DatabaseManager.executeUpdate(
                "DELETE FROM ImagenHotelAmenidad WHERE HotelAmenidadID=?", hotelAmenidadId);
        DatabaseManager.executeUpdate("DELETE FROM HotelAmenidad WHERE ID=?", hotelAmenidadId);
    }

    /**
     * Retorna los IDs de las imagenes asociadas a una amenidad de hotel, ordenados por ID.
     * @param hotelAmenidadId ID del registro HotelAmenidad del que se buscan las imagenes.
     * @return lista de IDs de imagenes de la amenidad.
     */
    public List<Integer> obtenerImagenesAmenidadIds(int hotelAmenidadId) {
        return DatabaseManager.executeQuery(
                "SELECT ID FROM ImagenHotelAmenidad WHERE HotelAmenidadID=? ORDER BY ID",
                rs -> rs.getInt("ID"), hotelAmenidadId);
    }

    /**
     * Agrega una imagen a una amenidad de hotel y retorna el ID generado.
     * @param hotelAmenidadId ID del registro HotelAmenidad al que se agrega la imagen.
     * @param imagen          bytes de la imagen a guardar.
     * @return ID de la imagen recien insertada.
     */
    public int agregarImagenAmenidad(int hotelAmenidadId, byte[] imagen) {
        return DatabaseManager.executeInsertReturnId(
                "INSERT INTO ImagenHotelAmenidad (HotelAmenidadID, Imagen) VALUES (?, ?)",
                "ID", hotelAmenidadId, imagen);
    }

    /**
     * Elimina una imagen de amenidad de hotel por su ID.
     * @param imagenId ID de la imagen a eliminar.
     */
    public void eliminarImagenAmenidad(int imagenId) {
        DatabaseManager.executeUpdate("DELETE FROM ImagenHotelAmenidad WHERE ID=?", imagenId);
    }

    /**
     * Retorna los IDs de las imagenes asociadas a un hotel, ordenados por ID.
     * @param hotelId ID del hotel del que se quieren obtener los IDs de imagenes.
     * @return lista de IDs de imagenes del hotel.
     */
    public List<Integer> obtenerImagenesIds(int hotelId) {
        return DatabaseManager.executeQuery(
                "SELECT ID FROM ImagenHotel WHERE HotelID=? ORDER BY ID",
                rs -> rs.getInt("ID"), hotelId);
    }

    /**
     * Agrega una imagen a un hotel y retorna el ID generado.
     * @param hotelId ID del hotel al que se agrega la imagen.
     * @param imagen  bytes de la imagen a guardar.
     * @return ID de la imagen recien insertada.
     */
    public int agregarImagenHotel(int hotelId, byte[] imagen) {
        return DatabaseManager.executeInsertReturnId(
                "INSERT INTO ImagenHotel (HotelID, Imagen) VALUES (?, ?)",
                "ID", hotelId, imagen);
    }

    /**
     * Elimina una imagen de hotel por su ID.
     * @param imagenId ID de la imagen a eliminar.
     */
    public void eliminarImagenHotel(int imagenId) {
        DatabaseManager.executeUpdate("DELETE FROM ImagenHotel WHERE ID=?", imagenId);
    }

    /**
     * Retorna todas las habitaciones de un hotel con sus datos de tipo, cama, precio y estado.
     * @param hotelId ID del hotel del que se quieren listar las habitaciones.
     * @return lista de HabitacionAdminDTO con los datos completos de cada habitacion.
     */
    public List<HabitacionAdminDTO> listarHabitacionesPorHotel(int hotelId) {
        String sql = """
                SELECT h.ID,
                       h.HotelID,
                       h.TIPOHABITACIONID,
                       t.NOMBRE          AS TipoHabitacion,
                       h.NUMEROHABITACION,
                       t.TIPOCAMAID      AS CamaID,
                       c.TIPO_DE_CLASE   AS TipoCama,
                       t.PRECIOPERSONA   AS Precio_por_Persona,
                       t.PRECIONOCHE     AS Precio_por_Noche,
                       t.CAPACIDADMAXIMA AS Capacidad_Maxima,
                       t.METROSCUADRADOS AS Metros_Cuadrados,
                       h.Descripcion,
                       h.ESTADO_ID,
                       e.TIPO_DE_CLASE   AS Estado
                FROM   Habitacion       h
                JOIN   TipoHabitacion   t ON h.TIPOHABITACIONID = t.ID
                JOIN   Cama             c ON t.TIPOCAMAID       = c.ID
                JOIN   EstadoHabitacion e ON h.ESTADO_ID        = e.ID
                WHERE  h.HotelID = ?
                ORDER BY h.ID
                """;
        return DatabaseManager.executeQuery(sql, rs -> {
            HabitacionAdminDTO d = new HabitacionAdminDTO();
            d.setId(rs.getInt("ID"));
            d.setHotelId(rs.getInt("HotelID"));
            d.setTipoHabitacionId(rs.getInt("TIPOHABITACIONID"));
            d.setTipoHabitacion(rs.getString("TipoHabitacion"));
            d.setNumeroHabitacion(rs.getString("NUMEROHABITACION"));
            d.setTipoCama(rs.getString("TipoCama"));
            d.setPrecioPorPersona(rs.getDouble("Precio_por_Persona"));
            d.setPrecioPorNoche(rs.getDouble("Precio_por_Noche"));
            d.setCapacidadMaxima(rs.getInt("Capacidad_Maxima"));
            d.setMetrosCuadrados(rs.getDouble("Metros_Cuadrados"));
            d.setDescripcion(rs.getString("Descripcion"));
            d.setEstadoId(rs.getInt("ESTADO_ID"));
            d.setEstado(rs.getString("Estado"));
            return d;
        }, hotelId);
    }

    /**
     * Crea una nueva habitacion en un hotel y auto-asigna el numero correlativo
     * basado en la cantidad de habitaciones ya existentes en ese hotel.
     * @param hotelId          ID del hotel donde se crea la habitacion.
     * @param tipoHabitacionId ID del tipo de habitacion a asignar.
     * @param descripcion      descripcion particular de la habitacion.
     * @param estadoId         ID del estado inicial de la habitacion.
     * @return ID de la habitacion recien insertada.
     */
    public int crearHabitacion(int hotelId, int tipoHabitacionId,
                               String descripcion, int estadoId) {
        // Contar habitaciones actuales del hotel para asignar numero correlativo
        List<Integer> cnt = DatabaseManager.executeQuery(
                "SELECT COUNT(*) FROM Habitacion WHERE HotelID=?",
                rs -> rs.getInt(1), hotelId);
        int numero = (cnt.isEmpty() ? 0 : cnt.get(0)) + 1;

        String sql = "INSERT INTO Habitacion " +
                "(HotelID, TIPOHABITACIONID, NUMEROHABITACION, Descripcion, ESTADO_ID) " +
                "VALUES (?, ?, ?, ?, ?)";
        return DatabaseManager.executeInsertReturnId(sql, "ID",
                hotelId, tipoHabitacionId, String.valueOf(numero), descripcion, estadoId);
    }

    /**
     * Verifica si una habitacion existe en la base de datos.
     * @param habitacionId ID de la habitacion a verificar.
     * @return true si la habitacion existe, false en caso contrario.
     */
    public boolean existeHabitacion(int habitacionId) {
        List<Integer> r = DatabaseManager.executeQuery(
                "SELECT COUNT(*) FROM Habitacion WHERE ID=?", rs -> rs.getInt(1), habitacionId);
        return !r.isEmpty() && r.get(0) > 0;
    }

    /**
     * Actualiza los datos de una habitacion existente.
     * @param habitacionId      ID de la habitacion a actualizar.
     * @param tipoHabitacionId  nuevo ID del tipo de habitacion.
     * @param numeroHabitacion  nuevo numero de habitacion.
     * @param descripcion       nueva descripcion de la habitacion.
     * @param estadoId          nuevo ID de estado de la habitacion.
     */
    public void actualizarHabitacion(int habitacionId, int tipoHabitacionId,
                                     String numeroHabitacion, String descripcion, int estadoId) {
        DatabaseManager.executeUpdate(
                "UPDATE Habitacion " +
                        "SET TIPOHABITACIONID=?, NUMEROHABITACION=?, Descripcion=?, ESTADO_ID=? " +
                        "WHERE ID=?",
                tipoHabitacionId, numeroHabitacion, descripcion, estadoId, habitacionId);
    }

    /**
     * Elimina una habitacion y todas sus imagenes asociadas.
     * @param habitacionId ID de la habitacion a eliminar.
     */
    public void eliminarHabitacion(int habitacionId) {
        DatabaseManager.executeUpdate(
                "DELETE FROM ImagenHabitacion WHERE HabitacionID=?", habitacionId);
        DatabaseManager.executeUpdate("DELETE FROM Habitacion WHERE ID=?", habitacionId);
    }

    /**
     * Retorna los IDs de las imagenes asociadas a una habitacion, ordenados por ID.
     * @param habitacionId ID de la habitacion de la que se quieren obtener los IDs de imagenes.
     * @return lista de IDs de imagenes de la habitacion.
     */
    public List<Integer> obtenerImagenesHabitacionIds(int habitacionId) {
        return DatabaseManager.executeQuery(
                "SELECT ID FROM ImagenHabitacion WHERE HabitacionID=? ORDER BY ID",
                rs -> rs.getInt("ID"), habitacionId);
    }

    /**
     * Agrega una imagen a una habitacion y retorna el ID generado.
     * @param habitacionId ID de la habitacion a la que se agrega la imagen.
     * @param imagen       bytes de la imagen a guardar.
     * @return ID de la imagen recien insertada.
     */
    public int agregarImagenHabitacion(int habitacionId, byte[] imagen) {
        return DatabaseManager.executeInsertReturnId(
                "INSERT INTO ImagenHabitacion (HabitacionID, Imagen) VALUES (?, ?)",
                "ID", habitacionId, imagen);
    }

    /**
     * Elimina una imagen de habitacion por su ID.
     * @param imagenId ID de la imagen a eliminar.
     */
    public void eliminarImagenHabitacion(int imagenId) {
        DatabaseManager.executeUpdate("DELETE FROM ImagenHabitacion WHERE ID=?", imagenId);
    }

    /**
     * Retorna todas las reservaciones del sistema con datos del usuario, hotel y fechas,
     * ordenadas por fecha de creacion descendente.
     * @return lista de mapas con los datos resumidos de cada reservacion.
     */
    public List<java.util.Map<String, Object>> listarTodasReservaciones() {
        String sql = """
                SELECT r.ID, r.No_Reservacion, r.Total,
                       r.Fecha_Creacion, r.Fecha_Expiracion,
                       er.Estado,
                       u.Nombre AS UsuarioNombre, u.Apellido AS UsuarioApellido,
                       u.Username,
                       hot.Nombre AS HotelNombre,
                       MIN(dr.FechaCheckIn)  AS CheckIn,
                       MAX(dr.FechaCheckOut) AS CheckOut
                FROM Reservacion r
                JOIN EstadoReserva er ON r.EstadoID     = er.ID
                JOIN Usuario       u  ON r.Usuario_ID   = u.ID
                JOIN DetallesReservacion dr ON dr.ReservacionID = r.ID
                JOIN Habitacion    h   ON dr.HabitacionID = h.ID
                JOIN Hotel         hot ON h.HotelID       = hot.ID
                GROUP BY r.ID, r.No_Reservacion, r.Total,
                         r.Fecha_Creacion, r.Fecha_Expiracion,
                         er.Estado,
                         u.Nombre, u.Apellido, u.Username,
                         hot.Nombre
                ORDER BY r.Fecha_Creacion DESC
                """;
        return DatabaseManager.executeQuery(sql, rs -> {
            java.util.Map<String, Object> row = new java.util.LinkedHashMap<>();
            row.put("id",             rs.getInt("ID"));
            row.put("noReservacion",  rs.getString("No_Reservacion"));
            row.put("total",          rs.getDouble("Total"));
            row.put("estado",         rs.getString("Estado").toLowerCase());
            row.put("usuario",        rs.getString("Username"));
            row.put("nombreCompleto", rs.getString("UsuarioNombre") + " " + rs.getString("UsuarioApellido"));
            row.put("hotel",          rs.getString("HotelNombre"));
            // Convierte fechas a String, o null si no tienen valor
            row.put("checkIn",        rs.getDate("CheckIn")  != null ? rs.getDate("CheckIn").toString()  : null);
            row.put("checkOut",       rs.getDate("CheckOut") != null ? rs.getDate("CheckOut").toString() : null);
            row.put("fechaCreacion",  rs.getTimestamp("Fecha_Creacion") != null ? rs.getTimestamp("Fecha_Creacion").toString() : null);
            return row;
        });
    }

    /**
     * Retorna un mapa con las metricas generales del sistema para el panel de administracion.
     * Incluye totales de usuarios, hoteles, reservaciones por estado e ingresos confirmados.
     * @return mapa con las claves: totalUsuarios, hotelesActivos, reservasActivas, reservasTotales,
     *         ingresosTotales, reservasPorEstado y hotesTotales.
     */
    public java.util.Map<String, Object> obtenerMetricas() {
        java.util.Map<String, Object> metricas = new java.util.LinkedHashMap<>();

        // Total de usuarios registrados en el sistema
        List<Integer> totalUsuarios = DatabaseManager.executeQuery(
                "SELECT COUNT(*) AS total FROM Usuario", rs -> rs.getInt("total"));
        metricas.put("totalUsuarios", totalUsuarios.isEmpty() ? 0 : totalUsuarios.get(0));

        // Hoteles con estado activo
        List<Integer> hotelesActivos = DatabaseManager.executeQuery(
                "SELECT COUNT(*) AS total FROM Hotel h JOIN Estado e ON h.EstadoID = e.ID WHERE LOWER(e.Estado) = 'activo'",
                rs -> rs.getInt("total"));
        metricas.put("hotelesActivos", hotelesActivos.isEmpty() ? 0 : hotelesActivos.get(0));

        // Reservaciones en estado Confirmada
        List<Integer> reservasActivas = DatabaseManager.executeQuery(
                "SELECT COUNT(*) AS total FROM Reservacion r JOIN EstadoReserva er ON r.EstadoID = er.ID WHERE LOWER(TRIM(er.Estado)) = 'confirmada'",
                rs -> rs.getInt("total"));
        metricas.put("reservasActivas", reservasActivas.isEmpty() ? 0 : reservasActivas.get(0));

        // Total de reservaciones sin filtro de estado
        List<Integer> reservasTotales = DatabaseManager.executeQuery(
                "SELECT COUNT(*) AS total FROM Reservacion", rs -> rs.getInt("total"));
        metricas.put("reservasTotales", reservasTotales.isEmpty() ? 0 : reservasTotales.get(0));

        // Suma de ingresos de reservaciones confirmadas
        List<Double> ingresosTotales = DatabaseManager.executeQuery(
                "SELECT NVL(SUM(r.Total), 0) AS total FROM Reservacion r JOIN EstadoReserva er ON r.EstadoID = er.ID WHERE LOWER(TRIM(er.Estado)) = 'confirmada'",
                rs -> rs.getDouble("total"));
        metricas.put("ingresosTotales", ingresosTotales.isEmpty() ? 0.0 : ingresosTotales.get(0));

        // Conteo de reservaciones agrupado por estado
        List<java.util.Map<String, Object>> porEstado = DatabaseManager.executeQuery(
                "SELECT LOWER(TRIM(er.Estado)) AS estado, COUNT(*) AS total FROM Reservacion r JOIN EstadoReserva er ON r.EstadoID = er.ID GROUP BY LOWER(TRIM(er.Estado))",
                rs -> {
                    java.util.Map<String, Object> e = new java.util.LinkedHashMap<>();
                    e.put("estado", rs.getString("estado"));
                    e.put("total",  rs.getInt("total"));
                    return e;
                });
        metricas.put("reservasPorEstado", porEstado);

        // Total de hoteles sin filtro de estado
        List<Integer> hotTotales = DatabaseManager.executeQuery(
                "SELECT COUNT(*) AS total FROM Hotel", rs -> rs.getInt("total"));
        metricas.put("hotesTotales", hotTotales.isEmpty() ? 0 : hotTotales.get(0));

        return metricas;
    }
}