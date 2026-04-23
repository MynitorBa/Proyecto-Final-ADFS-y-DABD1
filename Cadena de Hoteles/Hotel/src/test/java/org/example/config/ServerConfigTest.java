package org.example.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de humo para ServerConfig.
 * Valida que la clase existe y expone los metodos publicos esperados.
 * No levanta el servidor Javalin real para mantener las pruebas rapidas.
 */
@DisplayName("ServerConfig - Pruebas de humo (smoke tests)")
class ServerConfigTest {

    @Test
    @DisplayName("clase ServerConfig existe y es publica")
    void claseServerConfig_existe_esPublica() {
        assertNotNull(ServerConfig.class);
        assertTrue(Modifier.isPublic(ServerConfig.class.getModifiers()));
    }

    @Test
    @DisplayName("metodo createServer existe y es publico estatico")
    void metodoCreateServer_existe_esPublicoEstatico() throws NoSuchMethodException {
        Method m = ServerConfig.class.getDeclaredMethod("createServer");
        assertTrue(Modifier.isPublic(m.getModifiers()));
        assertTrue(Modifier.isStatic(m.getModifiers()));
    }
}
