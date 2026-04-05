package org.example.helpers;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Helper para la generacion y validacion de tokens JWT.
 * Maneja la firma con HMAC-SHA, la extraccion de claims y la verificacion
 * de tokens en las peticiones autenticadas. El secreto se lee de la variable
 * de entorno JWT_SECRET y tiene una duracion de 8 horas por token.
 */
public class JwtHelper {

    // IMPORTANTE: mover JWT_SECRET a variable de entorno en produccion
    private static final String SECRET =
            System.getenv().getOrDefault("JWT_SECRET", "Sabrina_es_la_Best_Bruja_Bonita!");

    /** Duracion del token: 8 horas en milisegundos. */
    private static final long DURACION_MS = 8L * 60 * 60 * 1000;

    /**
     * Construye la clave HMAC-SHA a partir del secreto configurado.
     *
     * @return clave secreta lista para firmar o verificar tokens.
     */
    private static SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Genera un token JWT firmado con los datos de identidad del usuario.
     * El token incluye el ID como subject y username y rolId como claims adicionales.
     *
     * @param usuarioId ID del usuario autenticado.
     * @param username  nombre de usuario.
     * @param rolId     ID del rol del usuario.
     * @return token JWT compacto y firmado.
     */
    public static String generarToken(int usuarioId, String username, int rolId) {
        return Jwts.builder()
                .subject(String.valueOf(usuarioId))
                .claim("username", username)
                .claim("rolId", rolId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + DURACION_MS))
                .signWith(getKey())
                .compact();
    }

    /**
     * Verifica la firma del token y retorna los claims si es valido.
     *
     * @param token token JWT a verificar.
     * @return claims extraidos del payload del token.
     * @throws JwtException si el token esta malformado, expirado o la firma no coincide.
     */
    public static Claims verificarToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extrae el ID del usuario desde los claims del token.
     *
     * @param claims claims obtenidos de un token verificado.
     * @return ID del usuario como entero.
     */
    public static int getUsuarioId(Claims claims) {
        return Integer.parseInt(claims.getSubject());
    }

    /**
     * Extrae el username desde los claims del token.
     *
     * @param claims claims obtenidos de un token verificado.
     * @return nombre de usuario.
     */
    public static String getUsername(Claims claims) {
        return claims.get("username", String.class);
    }

    /**
     * Extrae el ID de rol desde los claims del token.
     *
     * @param claims claims obtenidos de un token verificado.
     * @return ID del rol del usuario.
     */
    public static int getRolId(Claims claims) {
        return claims.get("rolId", Integer.class);
    }

    /**
     * Valida un token JWT sin lanzar excepciones.
     * Util para verificar rapidamente si un token es usable antes de procesarlo.
     *
     * @param token token JWT a validar.
     * @return true si el token es valido y no ha expirado; false en cualquier otro caso.
     */
    public static boolean esValido(String token) {
        try {
            verificarToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}