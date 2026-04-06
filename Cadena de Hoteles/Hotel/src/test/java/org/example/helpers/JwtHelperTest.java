package org.example.helpers;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para JwtHelper.
 * Verifica la generacion, verificacion y extraccion de claims en tokens JWT.
 */
class JwtHelperTest {

    // -- generarToken

    /**
     * Verifica que generarToken retorna un token no nulo y no vacio.
     */
    @Test
    void generarToken_retornaTokenNoNulo() {
        String token = JwtHelper.generarToken(1, "user", 1);

        assertNotNull(token);
        assertFalse(token.isBlank(), "El token no debe estar vacio");
    }

    /**
     * Verifica que el token generado tiene estructura JWT con tres segmentos separados por puntos.
     */
    @Test
    void generarToken_tieneEstructuraJwt() {
        String token = JwtHelper.generarToken(10, "admin", 2);

        String[] partes = token.split("\\.");
        assertEquals(3, partes.length, "Un token JWT compacto debe tener 3 partes");
    }

    // -- verificarToken

    /**
     * Verifica que verificarToken retorna Claims no nulos y con el subject correcto
     * cuando el token es valido.
     */
    @Test
    void verificarToken_tokenValido_retornaClaims() {
        String token = JwtHelper.generarToken(5, "admin", 2);
        Claims claims = JwtHelper.verificarToken(token);

        assertNotNull(claims);
        assertEquals("5", claims.getSubject());
    }

    /**
     * Verifica que verificarToken lanza una excepcion cuando el token esta malformado.
     */
    @Test
    void verificarToken_tokenInvalido_lanzaExcepcion() {
        assertThrows(Exception.class, () -> JwtHelper.verificarToken("token.basura.xyz"));
    }

    // -- getUsuarioId

    /**
     * Verifica que getUsuarioId retorna el ID de usuario correcto desde los claims.
     */
    @Test
    void getUsuarioId_retornaIdCorrecto() {
        String token = JwtHelper.generarToken(42, "u", 1);
        Claims claims = JwtHelper.verificarToken(token);

        assertEquals(42, JwtHelper.getUsuarioId(claims));
    }

    /**
     * Verifica que getUsuarioId retorna el ID correcto para un valor de borde.
     */
    @Test
    void getUsuarioId_idBorde_retornaIdCorrecto() {
        String token = JwtHelper.generarToken(1, "user", 1);
        Claims claims = JwtHelper.verificarToken(token);

        assertEquals(1, JwtHelper.getUsuarioId(claims));
    }

    // -- getUsername

    /**
     * Verifica que getUsername retorna el nombre de usuario correcto desde los claims.
     */
    @Test
    void getUsername_retornaUsernameCorrecto() {
        String token = JwtHelper.generarToken(1, "testUser", 1);
        Claims claims = JwtHelper.verificarToken(token);

        assertEquals("testUser", JwtHelper.getUsername(claims));
    }

    /**
     * Verifica que getUsername distingue entre distintos nombres de usuario.
     */
    @Test
    void getUsername_usernameDistinto_retornaValorCorrecto() {
        String token = JwtHelper.generarToken(2, "otroUsuario", 1);
        Claims claims = JwtHelper.verificarToken(token);

        assertEquals("otroUsuario", JwtHelper.getUsername(claims));
    }

    // -- getRolId

    /**
     * Verifica que getRolId retorna el ID de rol correcto desde los claims.
     */
    @Test
    void getRolId_retornaRolCorrecto() {
        String token = JwtHelper.generarToken(1, "u", 3);
        Claims claims = JwtHelper.verificarToken(token);

        assertEquals(3, JwtHelper.getRolId(claims));
    }

    /**
     * Verifica que getRolId distingue entre distintos IDs de rol.
     */
    @Test
    void getRolId_rolDistinto_retornaValorCorrecto() {
        String token = JwtHelper.generarToken(1, "u", 5);
        Claims claims = JwtHelper.verificarToken(token);

        assertEquals(5, JwtHelper.getRolId(claims));
    }

    // -- esValido

    /**
     * Verifica que esValido retorna true para un token recien generado.
     */
    @Test
    void esValido_tokenValido_retornaTrue() {
        String token = JwtHelper.generarToken(1, "u", 1);

        assertTrue(JwtHelper.esValido(token));
    }

    /**
     * Verifica que esValido retorna false para un token con formato invalido.
     */
    @Test
    void esValido_tokenInvalido_retornaFalse() {
        assertFalse(JwtHelper.esValido("tokenbasura.abc.xyz"));
    }
}
