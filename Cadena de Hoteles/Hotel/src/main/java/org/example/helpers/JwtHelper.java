package org.example.helpers;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtHelper {

    //IMPORTANTE MOVER ESTO FUERA PARA PROTECCION
    private static final String SECRET =
            System.getenv().getOrDefault("JWT_SECRET", "Sabrina_es_la_Best_Bruja_Bonita!");
    private static final long DURACION_MS = 8L * 60 * 60 * 1000; // 8 horas en milisegundos
    private static SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    // -------------------------------------------Generar token--------------------------------------------

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

    //-------------------------------------Verificar y extraer claims----------------------------------------

    public static Claims verificarToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    //--------------------------------------Helpers para extraer datos del token-------------------------------

    public static int getUsuarioId(Claims claims) {
        return Integer.parseInt(claims.getSubject());
    }

    public static String getUsername(Claims claims) {
        return claims.get("username", String.class);
    }

    public static int getRolId(Claims claims) {
        return claims.get("rolId", Integer.class);
    }

    // ---------------------------------------Validar sin lanzar excepción------------------------------------
    public static boolean esValido(String token) {
        try {
            verificarToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}