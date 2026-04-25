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
     * Cambia el estado del hotel a Cerrado (EstadoID = 2).
     * No elimina ningun registro; el hotel sigue en BD pero deja de aparecer
     * en las busquedas publicas (BusquedaRepository filtra por EstadoID = 1).
     * @param hotelId ID del hotel a cerrar.
     */
    public void cerrarHotel(int hotelId) {
        DatabaseManager.executeUpdate("UPDATE Hotel SET EstadoID = 2 WHERE ID = ?", hotelId);
    }

    /**
     * Reactiva un hotel cerrado cambiando su EstadoID a 1 (Activo).
     * El hotel vuelve a aparecer en las busquedas publicas.
     * @param hotelId ID del hotel a reactivar.
     */
    public void reactivarHotel(int hotelId) {
        DatabaseManager.executeUpdate("UPDATE Hotel SET EstadoID = 1 WHERE ID = ?", hotelId);
    }

    /**
     * Cuenta las reservaciones activas (Pendiente=1 o Confirmada=2) de todas las
     * habitaciones del hotel. Se usa para validar si el hotel puede eliminarse.
     * @param hotelId ID del hotel a verificar.
     * @return cantidad de reservaciones en proceso del hotel.
     */
    public int contarReservasActivasHotel(int hotelId) {
        List<Integer> r = DatabaseManager.executeQuery(
                "SELECT COUNT(*) " +
                        "FROM DetallesReservacion dr " +
                        "JOIN Habitacion h  ON dr.HabitacionID   = h.ID " +
                        "JOIN Reservacion re ON dr.ReservacionID = re.ID " +
                        "WHERE h.HotelID = ? AND re.EstadoID IN (1, 2)",
                rs -> rs.getInt(1), hotelId);
        return r.isEmpty() ? 0 : r.get(0);
    }

    /**
     * Retorna los datos de cada reservacion activa del hotel necesarios para
     * cancelarlas y notificar a los usuarios por correo.
     * Columnas: reservacionId, noReservacion, correo, nombreCompleto, total.
     * @param hotelId ID del hotel.
     * @return lista de Object[] con los datos de cada reservacion activa.
     */
    public List<Object[]> obtenerReservacionesActivasHotel(int hotelId) {
        return DatabaseManager.executeQuery(
                "SELECT DISTINCT re.ID, re.No_Reservacion, " +
                        "       u.Correo, " +
                        "       u.Nombre || ' ' || u.Apellido AS NombreCompleto, " +
                        "       re.Total " +
                        "FROM DetallesReservacion dr " +
                        "JOIN Habitacion h   ON dr.HabitacionID  = h.ID " +
                        "JOIN Reservacion re ON dr.ReservacionID = re.ID " +
                        "JOIN Usuario     u  ON re.Usuario_ID    = u.ID " +
                        "WHERE h.HotelID = ? AND re.EstadoID IN (1, 2)",
                rs -> new Object[]{
                        rs.getInt("ID"),
                        rs.getString("No_Reservacion"),
                        rs.getString("Correo"),
                        rs.getString("NombreCompleto"),
                        rs.getDouble("Total")
                }, hotelId);
    }

    /**
     * Cancela todas las reservaciones activas del hotel poniendo EstadoID = 4.
     * @param hotelId ID del hotel cuyas reservaciones se cancelan.
     * @param motivo  texto del motivo de cancelacion.
     */
    public void cancelarReservacionesActivasHotel(int hotelId, String motivo) {
        DatabaseManager.executeUpdate(
                "UPDATE Reservacion re " +
                        "SET re.EstadoID = 4, re.Fecha_Cancelacion = SYSDATE, re.Motivo_Cancelacion = ? " +
                        "WHERE re.EstadoID IN (1, 2) " +
                        "  AND re.ID IN ( " +
                        "    SELECT DISTINCT dr.ReservacionID " +
                        "    FROM DetallesReservacion dr " +
                        "    JOIN Habitacion h ON dr.HabitacionID = h.ID " +
                        "    WHERE h.HotelID = ? " +
                        "  )",
                motivo, hotelId);
    }

    /**
     * Elimina un hotel y todos sus registros dependientes en cascada.
     * Borra en orden: habitaciones, imagenes de amenidades, amenidades del hotel,
     * imagenes del hotel y finalmente el hotel.
     * Las imagenes de habitacion NO se eliminan porque pertenecen al TipoHabitacion,
     * no a la habitacion fisica.
     * @param hotelId ID del hotel a eliminar.
     */
    public void eliminarHotel(int hotelId) {
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
                "SELECT COUNT(*) FROM Habitacion WHERE HotelID=? AND ESTADO_ID = 1", rs -> rs.getInt(1), hotelId);
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
     * Cuenta las reservaciones activas (Pendiente=1 o Confirmada=2) que incluyen la habitacion.
     * Se usa para validar si una habitacion puede eliminarse de forma segura.
     * @param habitacionId ID de la habitacion a verificar.
     * @return cantidad de reservaciones en proceso que referencian esta habitacion.
     */
    public int contarReservasActivasHabitacion(int habitacionId) {
        List<Integer> r = DatabaseManager.executeQuery(
                "SELECT COUNT(*) " +
                        "FROM DetallesReservacion dr " +
                        "JOIN Reservacion re ON dr.ReservacionID = re.ID " +
                        "WHERE dr.HabitacionID = ? AND re.EstadoID IN (1, 2)",
                rs -> rs.getInt(1), habitacionId);
        return r.isEmpty() ? 0 : r.get(0);
    }

    /**
     * Cambia el estado de la habitacion a Cerrada (ESTADO_ID = 2).
     * @param habitacionId ID de la habitacion a cerrar.
     */
    public void cerrarHabitacion(int habitacionId) {
        DatabaseManager.executeUpdate("UPDATE Habitacion SET ESTADO_ID = 2 WHERE ID = ?", habitacionId);
    }

    /**
     * Reactiva una habitacion cerrada cambiando su ESTADO_ID a 1 (Activa).
     * @param habitacionId ID de la habitacion a reactivar.
     */
    public void reactivarHabitacion(int habitacionId) {
        DatabaseManager.executeUpdate("UPDATE Habitacion SET ESTADO_ID = 1 WHERE ID = ?", habitacionId);
    }

    /**
     * Retorna los datos de cada reservacion activa de una habitacion especifica,
     * necesarios para cancelarlas y notificar a los usuarios por correo.
     * @param habitacionId ID de la habitacion.
     * @return lista de Object[] con los datos de cada reservacion activa.
     */
    public List<Object[]> obtenerReservacionesActivasHabitacion(int habitacionId) {
        return DatabaseManager.executeQuery(
                "SELECT DISTINCT re.ID, re.No_Reservacion, " +
                        "       u.Correo, " +
                        "       u.Nombre || ' ' || u.Apellido AS NombreCompleto, " +
                        "       re.Total " +
                        "FROM DetallesReservacion dr " +
                        "JOIN Reservacion re ON dr.ReservacionID = re.ID " +
                        "JOIN Usuario     u  ON re.Usuario_ID    = u.ID " +
                        "WHERE dr.HabitacionID = ? AND re.EstadoID IN (1, 2)",
                rs -> new Object[]{
                        rs.getInt("ID"),
                        rs.getString("No_Reservacion"),
                        rs.getString("Correo"),
                        rs.getString("NombreCompleto"),
                        rs.getDouble("Total")
                }, habitacionId);
    }

    /**
     * Cancela todas las reservaciones activas de una habitacion especifica
     * poniendo EstadoID = 4 (Cancelada).
     * @param habitacionId ID de la habitacion cuyas reservaciones se cancelan.
     * @param motivo       texto del motivo de cancelacion.
     */
    public void cancelarReservacionesActivasHabitacion(int habitacionId, String motivo) {
        DatabaseManager.executeUpdate(
                "UPDATE Reservacion re " +
                        "SET re.EstadoID = 4, re.Fecha_Cancelacion = SYSDATE, re.Motivo_Cancelacion = ? " +
                        "WHERE re.EstadoID IN (1, 2) " +
                        "  AND re.ID IN ( " +
                        "    SELECT DISTINCT dr.ReservacionID " +
                        "    FROM DetallesReservacion dr " +
                        "    WHERE dr.HabitacionID = ? " +
                        "  )",
                motivo, habitacionId);
    }

    /**
     * Elimina una habitacion fisica.
     * Las imagenes NO se eliminan porque pertenecen al TipoHabitacion, no a la habitacion fisica.
     * @param habitacionId ID de la habitacion a eliminar.
     */
    public void eliminarHabitacion(int habitacionId) {
        DatabaseManager.executeUpdate("DELETE FROM Habitacion WHERE ID=?", habitacionId);
    }

    /**
     * Retorna los IDs de las imagenes del tipo de habitacion al que pertenece la habitacion fisica,
     * resolviendo internamente el TipoHabitacionID a partir del HabitacionID recibido.
     * @param habitacionId ID de la habitacion fisica.
     * @return lista de IDs de imagenes del tipo de habitacion correspondiente.
     */
    public List<Integer> obtenerImagenesHabitacionIds(int habitacionId) {
        String sql = "SELECT ih.ID FROM ImagenHabitacion ih " +
                "JOIN Habitacion h ON h.TIPOHABITACIONID = ih.TipoHabitacionID " +
                "WHERE h.ID = ? ORDER BY ih.ID";
        return DatabaseManager.executeQuery(sql, rs -> rs.getInt("ID"), habitacionId);
    }

    /**
     * Agrega una imagen al tipo de habitacion al que pertenece la habitacion fisica recibida.
     * Resuelve internamente el TipoHabitacionID a partir del HabitacionID,
     * de modo que el service y el controller no necesitan conocer el tipo.
     * @param habitacionId ID de la habitacion fisica.
     * @param imagen       bytes de la imagen a guardar.
     * @return ID de la imagen recien insertada.
     * @throws IllegalArgumentException si la habitacion no existe.
     */
    public int agregarImagenHabitacion(int habitacionId, byte[] imagen) {
        List<Integer> tipo = DatabaseManager.executeQuery(
                "SELECT TIPOHABITACIONID FROM Habitacion WHERE ID = ?",
                rs -> rs.getInt("TIPOHABITACIONID"), habitacionId);
        if (tipo.isEmpty())
            throw new IllegalArgumentException("Habitacion no encontrada: " + habitacionId);
        int tipoHabitacionId = tipo.get(0);

        return DatabaseManager.executeInsertReturnId(
                "INSERT INTO ImagenHabitacion (TipoHabitacionID, Imagen) VALUES (?, ?)",
                "ID", tipoHabitacionId, imagen);
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
            row.put("checkIn",        rs.getDate("CheckIn")  != null ? rs.getDate("CheckIn").toString()  : null);
            row.put("checkOut",       rs.getDate("CheckOut") != null ? rs.getDate("CheckOut").toString() : null);
            row.put("fechaCreacion",  rs.getTimestamp("Fecha_Creacion") != null ? rs.getTimestamp("Fecha_Creacion").toString() : null);
            return row;
        });
    }

    /**
     * Retorna un mapa con las metricas generales del sistema para el panel de administracion.
     * @return mapa con las claves: totalUsuarios, hotelesActivos, reservasActivas, reservasTotales,
     *         ingresosTotales, reservasPorEstado y hotesTotales.
     */
    public java.util.Map<String, Object> obtenerMetricas() {
        java.util.Map<String, Object> metricas = new java.util.LinkedHashMap<>();

        List<Integer> totalUsuarios = DatabaseManager.executeQuery(
                "SELECT COUNT(*) AS total FROM Usuario", rs -> rs.getInt("total"));
        metricas.put("totalUsuarios", totalUsuarios.isEmpty() ? 0 : totalUsuarios.get(0));

        List<Integer> hotelesActivos = DatabaseManager.executeQuery(
                "SELECT COUNT(*) AS total FROM Hotel h JOIN Estado e ON h.EstadoID = e.ID WHERE LOWER(e.Estado) = 'activo'",
                rs -> rs.getInt("total"));
        metricas.put("hotelesActivos", hotelesActivos.isEmpty() ? 0 : hotelesActivos.get(0));

        List<Integer> reservasActivas = DatabaseManager.executeQuery(
                "SELECT COUNT(*) AS total FROM Reservacion r JOIN EstadoReserva er ON r.EstadoID = er.ID WHERE LOWER(TRIM(er.Estado)) = 'confirmada'",
                rs -> rs.getInt("total"));
        metricas.put("reservasActivas", reservasActivas.isEmpty() ? 0 : reservasActivas.get(0));

        List<Integer> reservasTotales = DatabaseManager.executeQuery(
                "SELECT COUNT(*) AS total FROM Reservacion", rs -> rs.getInt("total"));
        metricas.put("reservasTotales", reservasTotales.isEmpty() ? 0 : reservasTotales.get(0));

        List<Double> ingresosTotales = DatabaseManager.executeQuery(
                "SELECT NVL(SUM(r.Total), 0) AS total FROM Reservacion r JOIN EstadoReserva er ON r.EstadoID = er.ID WHERE LOWER(TRIM(er.Estado)) = 'confirmada'",
                rs -> rs.getDouble("total"));
        metricas.put("ingresosTotales", ingresosTotales.isEmpty() ? 0.0 : ingresosTotales.get(0));

        List<java.util.Map<String, Object>> porEstado = DatabaseManager.executeQuery(
                "SELECT LOWER(TRIM(er.Estado)) AS estado, COUNT(*) AS total FROM Reservacion r JOIN EstadoReserva er ON r.EstadoID = er.ID GROUP BY LOWER(TRIM(er.Estado))",
                rs -> {
                    java.util.Map<String, Object> e = new java.util.LinkedHashMap<>();
                    e.put("estado", rs.getString("estado"));
                    e.put("total",  rs.getInt("total"));
                    return e;
                });
        metricas.put("reservasPorEstado", porEstado);

        List<Integer> hotTotales = DatabaseManager.executeQuery(
                "SELECT COUNT(*) AS total FROM Hotel", rs -> rs.getInt("total"));
        metricas.put("hotesTotales", hotTotales.isEmpty() ? 0 : hotTotales.get(0));

        return metricas;
    }




    // -------------------------------------------------------------------------
    // Tipos de habitacion - admin
    // Agregar estos metodos al final de HotelRepository, antes del cierre de clase
    // -------------------------------------------------------------------------

    /**
     * Retorna todos los tipos de habitacion registrados en el sistema,
     * incluyendo el nombre del tipo de cama, ordenados por ID.
     * @return lista de TipoHabitacionAdminDTO con los datos completos de cada tipo.
     */
    public List<org.example.dtos.TipoHabitacionAdminDTO> listarTiposHabitacion() {
        String sql = """
                SELECT t.ID, t.NOMBRE, t.PRECIOPERSONA, t.PRECIONOCHE,
                       t.CAPACIDADMAXIMA, t.TIPOCAMAID, c.TIPO_DE_CLASE AS TipoCama,
                       t.METROSCUADRADOS
                FROM TipoHabitacion t
                JOIN Cama c ON t.TIPOCAMAID = c.ID
                ORDER BY t.ID
                """;
        return DatabaseManager.executeQuery(sql, rs -> {
            org.example.dtos.TipoHabitacionAdminDTO dto = new org.example.dtos.TipoHabitacionAdminDTO();
            dto.setId(rs.getInt("ID"));
            dto.setNombre(rs.getString("NOMBRE"));
            dto.setPrecioPorPersona(rs.getDouble("PRECIOPERSONA"));
            dto.setPrecioPorNoche(rs.getDouble("PRECIONOCHE"));
            dto.setCapacidadMaxima(rs.getInt("CAPACIDADMAXIMA"));
            dto.setTipoCamaId(rs.getInt("TIPOCAMAID"));
            dto.setTipoCama(rs.getString("TipoCama"));
            dto.setMetrosCuadrados(rs.getDouble("METROSCUADRADOS"));
            return dto;
        });
    }

    /**
     * Verifica si un tipo de habitacion existe en la base de datos.
     * @param tipoHabitacionId ID del tipo a verificar.
     * @return true si existe, false en caso contrario.
     */
    public boolean existeTipoHabitacion(int tipoHabitacionId) {
        List<Integer> r = DatabaseManager.executeQuery(
                "SELECT COUNT(*) FROM TipoHabitacion WHERE ID = ?",
                rs -> rs.getInt(1), tipoHabitacionId);
        return !r.isEmpty() && r.get(0) > 0;
    }

    /**
     * Actualiza unicamente los precios de un tipo de habitacion.
     * No modifica nombre, capacidad, tipo de cama ni metros cuadrados.
     * @param tipoHabitacionId ID del tipo a actualizar.
     * @param precioPorPersona nuevo precio por persona adicional.
     * @param precioPorNoche   nuevo precio base por noche.
     */
    public void actualizarPreciosTipoHabitacion(int tipoHabitacionId,
                                                double precioPorPersona,
                                                double precioPorNoche) {
        DatabaseManager.executeUpdate(
                "UPDATE TipoHabitacion SET PRECIOPERSONA = ?, PRECIONOCHE = ? WHERE ID = ?",
                precioPorPersona, precioPorNoche, tipoHabitacionId);
    }

    /**
     * Retorna los IDs de las imagenes asociadas a un tipo de habitacion, ordenados por ID.
     * @param tipoHabitacionId ID del tipo de habitacion.
     * @return lista de IDs de imagenes del tipo.
     */
    public List<Integer> obtenerImagenesTipoHabitacionIds(int tipoHabitacionId) {
        return DatabaseManager.executeQuery(
                "SELECT ID FROM ImagenHabitacion WHERE TipoHabitacionID = ? ORDER BY ID",
                rs -> rs.getInt("ID"), tipoHabitacionId);
    }

    /**
     * Agrega una imagen directamente a un tipo de habitacion y retorna el ID generado.
     * @param tipoHabitacionId ID del tipo de habitacion al que se agrega la imagen.
     * @param imagen           bytes de la imagen a guardar.
     * @return ID de la imagen recien insertada.
     */
    public int agregarImagenTipoHabitacion(int tipoHabitacionId, byte[] imagen) {
        return DatabaseManager.executeInsertReturnId(
                "INSERT INTO ImagenHabitacion (TipoHabitacionID, Imagen) VALUES (?, ?)",
                "ID", tipoHabitacionId, imagen);
    }

    /**
     * Elimina una imagen de tipo de habitacion por su ID.
     * @param imagenId ID de la imagen a eliminar.
     */
    public void eliminarImagenTipoHabitacion(int imagenId) {
        DatabaseManager.executeUpdate("DELETE FROM ImagenHabitacion WHERE ID = ?", imagenId);
    }
}