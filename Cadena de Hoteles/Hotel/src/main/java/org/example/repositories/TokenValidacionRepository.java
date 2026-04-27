package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.TokenValidacionResponseDTO;

import java.util.List;

/**
 * Repository para la validacion de tokens de alianza recibidos desde aerolineas.
 */
public class TokenValidacionRepository {

    /**
     * Busca un token valido: que exista, no este usado y no haya expirado.
     * Retorna ciudad, pais y porcentaje de descuento si el token es valido.
     * @param token string UUID recibido desde la URL del usuario.
     * @return TokenValidacionResponseDTO con los datos del token, o null si no es valido.
     */
    public TokenValidacionResponseDTO buscarTokenValido(String token) {
        // 1. El SQL formatea las fechas a String (YYYY-MM-DD) para el frontend
        String sql = "SELECT c.Nombre AS Ciudad, p.Nombre AS Pais, " +
                "a.PorcentajeDescuento, " +
                "TO_CHAR(t.FechaExpiracion, 'YYYY-MM-DD HH24:MI:SS') AS FechaExpiracion, " +
                "TO_CHAR(t.FechaIda,        'YYYY-MM-DD') AS FechaIda, " +
                "TO_CHAR(t.FechaVuelta,     'YYYY-MM-DD') AS FechaVuelta " +
                "FROM TokenAerolinea t " +
                "JOIN Ciudad          c ON t.CiudadID          = c.ID " +
                "JOIN Pais            p ON c.Pais_ID           = p.ID " +
                "JOIN AerolineaAliado a ON t.AerolineaAliadoID = a.ID " +
                "WHERE t.Token = ? " +
                "AND t.Usado = 0 " +
                "AND t.FechaExpiracion > SYSDATE";

        // 2. Ejecutamos y mapeamos usando rs.getString para las fechas
        List<TokenValidacionResponseDTO> result = DatabaseManager.executeQuery(sql, rs ->
                        new TokenValidacionResponseDTO(
                                rs.getString("Ciudad"),
                                rs.getString("Pais"),
                                rs.getDouble("PorcentajeDescuento"),
                                rs.getString("FechaExpiracion"),
                                rs.getString("FechaIda"),    // CAMBIADO: A getString
                                rs.getString("FechaVuelta")  // CAMBIADO: A getString
                        ),
                token
        );

        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * Marca un token de alianza como usado, registra la fecha de uso
     * y vincula la reservacion que se genero con ese descuento.
     * @param token         string UUID del token a cerrar.
     * @param reservacionId ID de la reservacion pagada con este token.
     */
    public void marcarTokenUsado(String token, int reservacionId) {
        String sql = "UPDATE TokenAerolinea " +
                "SET Usado = 1, FechaUso = SYSDATE, ReservacionID = ? " +
                "WHERE Token = ?";
        DatabaseManager.executeUpdate(sql, reservacionId, token);
    }
}