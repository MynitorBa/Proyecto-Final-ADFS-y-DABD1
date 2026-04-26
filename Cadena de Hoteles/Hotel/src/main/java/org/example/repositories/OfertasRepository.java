package org.example.repositories;

import org.example.data.DatabaseManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository para el servicio de envio de ofertas por correo.
 * Obtiene los usuarios que optaron por recibir ofertas y los hoteles
 * activos con sus tipos de habitacion para armar el email promocional.
 */
public class OfertasRepository {

    /**
     * DTO interno con los datos minimos del usuario para enviar la oferta.
     */
    public static class UsuarioOferta {
        public final int    id;
        public final String nombre;
        public final String correo;
        public final String preferencias;

        public UsuarioOferta(int id, String nombre, String correo, String preferencias) {
            this.id           = id;
            this.nombre       = nombre;
            this.correo       = correo;
            this.preferencias = preferencias;
        }
    }

    /**
     * DTO interno con los datos de un tipo de habitacion disponible en un hotel.
     */
    public static class HabitacionOferta {
        public final String nombre;
        public final String ciudad;
        public final String pais;
        public final String tipoHabitacion;
        public final String tipoCama;
        public final int    capacidadMaxima;
        public final double precioPorNoche;
        public final double precioPorPersona;
        public final double metrosCuadrados;

        public HabitacionOferta(String nombre, String ciudad, String pais,
                                String tipoHabitacion, String tipoCama,
                                int capacidadMaxima, double precioPorNoche,
                                double precioPorPersona, double metrosCuadrados) {
            this.nombre          = nombre;
            this.ciudad          = ciudad;
            this.pais            = pais;
            this.tipoHabitacion  = tipoHabitacion;
            this.tipoCama        = tipoCama;
            this.capacidadMaxima = capacidadMaxima;
            this.precioPorNoche  = precioPorNoche;
            this.precioPorPersona = precioPorPersona;
            this.metrosCuadrados = metrosCuadrados;
        }
    }

    /**
     * Retorna todos los usuarios que tienen preferencias de ofertas guardadas
     * (campo Preferencias_Oferta no nulo y no vacio).
     *
     * @return lista de UsuarioOferta con id, nombre, correo y preferencias JSON.
     */
    public List<UsuarioOferta> obtenerUsuariosConPreferencias() {
        String sql = "SELECT u.ID, u.Nombre, u.Correo, u.Preferencias_Oferta " +
                     "FROM Usuario u " +
                     "WHERE u.Preferencias_Oferta IS NOT NULL " +
                     "AND TRIM(u.Preferencias_Oferta) != ''";

        return DatabaseManager.executeQuery(sql, rs -> new UsuarioOferta(
                rs.getInt("ID"),
                rs.getString("Nombre"),
                rs.getString("Correo"),
                rs.getString("Preferencias_Oferta")
        ));
    }

    /**
     * Retorna hasta 12 tipos de habitacion de hoteles activos, incluyendo datos
     * del hotel, ciudad y precios. Se usa para construir el cuerpo del email de ofertas.
     *
     * @return lista de HabitacionOferta con datos del hotel y tipo de habitacion.
     */
    public List<HabitacionOferta> obtenerHabitacionesDisponibles() {
        String sql = "SELECT * FROM (" +
                     "  SELECT h.Nombre AS NombreHotel, " +
                     "         c.Nombre AS Ciudad, " +
                     "         p.Nombre AS Pais, " +
                     "         th.Nombre AS TipoHabitacion, " +
                     "         th.TipoCama, " +
                     "         th.CapacidadMaxima, " +
                     "         th.PrecioPorNoche, " +
                     "         th.PrecioPorPersona, " +
                     "         th.MetrosCuadrados " +
                     "  FROM TipoHabitacion th " +
                     "  JOIN Hotel h  ON th.HotelID   = h.ID " +
                     "  JOIN Estado e ON h.EstadoID   = e.ID " +
                     "  JOIN Ciudad c ON h.CiudadID   = c.ID " +
                     "  JOIN Pais   p ON c.Pais_ID    = p.ID " +
                     "  WHERE LOWER(TRIM(e.Estado)) = 'activo' " +
                     "  ORDER BY th.PrecioPorNoche ASC" +
                     ") WHERE ROWNUM <= 12";

        return DatabaseManager.executeQuery(sql, rs -> new HabitacionOferta(
                rs.getString("NombreHotel"),
                rs.getString("Ciudad"),
                rs.getString("Pais"),
                rs.getString("TipoHabitacion"),
                rs.getString("TipoCama"),
                rs.getInt("CapacidadMaxima"),
                rs.getDouble("PrecioPorNoche"),
                rs.getDouble("PrecioPorPersona"),
                rs.getDouble("MetrosCuadrados")
        ));
    }

    /**
     * Obtiene los datos minimos de un usuario especifico para el envio inmediato
     * de ofertas tras el registro, solo si tiene preferencias guardadas.
     *
     * @param usuarioId ID del usuario recien registrado.
     * @return UsuarioOferta o null si el usuario no tiene preferencias.
     */
    public UsuarioOferta obtenerUsuarioPorId(int usuarioId) {
        String sql = "SELECT u.ID, u.Nombre, u.Correo, u.Preferencias_Oferta " +
                     "FROM Usuario u " +
                     "WHERE u.ID = ? AND u.Preferencias_Oferta IS NOT NULL";

        List<UsuarioOferta> result = DatabaseManager.executeQuery(sql, rs -> new UsuarioOferta(
                rs.getInt("ID"),
                rs.getString("Nombre"),
                rs.getString("Correo"),
                rs.getString("Preferencias_Oferta")
        ), usuarioId);

        return result.isEmpty() ? null : result.get(0);
    }
}
