package org.example.repositories;

import org.example.data.DatabaseManager;

import java.sql.Timestamp;
import java.util.List;

/**
 * Repository para la creacion y consulta de tokens de alianza entre aerolineas y hoteles.
 */
public class TokenAerolineaRepository {

    /**
     * Retorna el ID de la aerolinea aliada activa que corresponde al token HASH dado.
     * @param tokenHash hash del token enviado en el header por la aerolinea.
     * @return ID de la aerolinea si existe y esta activa, null si no.
     */
    public Integer obtenerAerolineaIdPorToken(String tokenHash) {
        String sql = "SELECT a.ID FROM AerolineaAliado a " +
                "JOIN EstadoAliado e ON a.EstadoID = e.ID " +
                "WHERE a.TokenHASH = ? AND LOWER(TRIM(e.Estado)) = 'activo'";

        List<Integer> result = DatabaseManager.executeQuery(
                sql, rs -> rs.getInt("ID"), tokenHash
        );
        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * Busca el ID de una ciudad por su nombre y el nombre de su pais,
     * sin distincion de mayusculas ni espacios.
     * @param nombreCiudad nombre de la ciudad destino.
     * @param nombrePais   nombre del pais al que pertenece la ciudad.
     * @return ID de la ciudad si existe, null si no se encuentra.
     */
    public Integer buscarCiudadId(String nombreCiudad, String nombrePais) {
        String sql = "SELECT c.ID FROM Ciudad c " +
                "JOIN Pais p ON c.Pais_ID = p.ID " +
                "WHERE LOWER(TRIM(c.Nombre)) = LOWER(TRIM(?)) " +
                "AND LOWER(TRIM(p.Nombre))   = LOWER(TRIM(?))";

        List<Integer> result = DatabaseManager.executeQuery(
                sql, rs -> rs.getInt("ID"), nombreCiudad, nombrePais
        );
        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * Inserta un nuevo token de alianza en la base de datos.
     * El token se crea como no usado, sin fecha de uso, sin reservacion,
     * y con expiracion a 15 minutos desde el momento de creacion.
     * @param aerolineaAliadoId ID de la aerolinea aliada que genera el token.
     * @param ciudadId          ID de la ciudad destino del pasajero.
     * @param token             string unico generado para este token de alianza.
     * @param fechaExpiracion   timestamp calculado como now() mas 15 minutos.
     */
    public void insertarToken(int aerolineaAliadoId, int ciudadId,
                              String token, Timestamp fechaExpiracion) {
        String sql = "INSERT INTO TokenAerolinea " +
                "(Usado, FechaUso, FechaExpiracion, Token, AerolineaAliadoID, CiudadID, ReservacionID) " +
                "VALUES (0, NULL, ?, ?, ?, ?, NULL)";

        DatabaseManager.executeUpdate(sql, fechaExpiracion, token, aerolineaAliadoId, ciudadId);
    }
}