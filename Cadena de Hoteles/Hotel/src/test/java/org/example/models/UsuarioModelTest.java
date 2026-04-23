package org.example.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el modelo Usuario.
 * Valida setters, getters y comportamiento con valores especiales.
 */
@DisplayName("Usuario (Model) - Pruebas de POJO")
class UsuarioModelTest {

    @Test
    @DisplayName("constructor vacio crea instancia no nula")
    void constructorVacio_creaInstanciaNoNula() {
        Usuario usuario = new Usuario();
        assertNotNull(usuario);
    }

    @Test
    @DisplayName("setters y getters de campos de texto funcionan")
    void settersYGetters_camposTexto_funcionan() {
        Usuario usuario = new Usuario();
        usuario.setUsername("miku_inn");
        usuario.setCorreo("miku@hotel.com");
        usuario.setContrasena("$2a$10$hashBcrypt");
        usuario.setPasaporte("GT123456");
        usuario.setNombre("Miku");
        usuario.setApellido("Inn");
        usuario.setTelefono("55551234");

        assertEquals("miku_inn",         usuario.getUsername());
        assertEquals("miku@hotel.com",   usuario.getCorreo());
        assertEquals("$2a$10$hashBcrypt",usuario.getContrasena());
        assertEquals("GT123456",         usuario.getPasaporte());
        assertEquals("Miku",             usuario.getNombre());
        assertEquals("Inn",              usuario.getApellido());
        assertEquals("55551234",         usuario.getTelefono());
    }

    @Test
    @DisplayName("setters y getters de campos numericos funcionan")
    void settersYGetters_camposNumericos_funcionan() {
        Usuario usuario = new Usuario();
        usuario.setId(42);
        usuario.setRolId(2);
        usuario.setCiudadId(5);

        assertEquals(42, usuario.getId());
        assertEquals(2,  usuario.getRolId());
        assertEquals(5,  usuario.getCiudadId());
    }

    @Test
    @DisplayName("setters y getters de fechaNacimiento funcionan")
    void setterYGetter_fechaNacimiento_funciona() {
        Usuario usuario = new Usuario();
        LocalDate fecha = LocalDate.of(2000, 3, 14);
        usuario.setFechaNacimiento(fecha);

        assertEquals(fecha,          usuario.getFechaNacimiento());
        assertEquals(2000,           usuario.getFechaNacimiento().getYear());
        assertEquals(3,              usuario.getFechaNacimiento().getMonthValue());
        assertEquals(14,             usuario.getFechaNacimiento().getDayOfMonth());
    }

    @Test
    @DisplayName("ciudadId acepta null (campo opcional)")
    void ciudadId_aceptaNull() {
        Usuario usuario = new Usuario();
        usuario.setCiudadId(null);
        assertNull(usuario.getCiudadId());
    }

    @Test
    @DisplayName("valores por defecto son null o 0 en constructor vacio")
    void valoresPorDefecto_sonNullOCero() {
        Usuario usuario = new Usuario();
        assertEquals(0,    usuario.getId());
        assertNull(usuario.getUsername());
        assertNull(usuario.getCorreo());
        assertNull(usuario.getContrasena());
        assertNull(usuario.getPasaporte());
        assertNull(usuario.getNombre());
        assertNull(usuario.getApellido());
        assertNull(usuario.getTelefono());
        assertEquals(0,    usuario.getRolId());
        assertNull(usuario.getFechaNacimiento());
        assertNull(usuario.getCiudadId());
    }

    @Test
    @DisplayName("se puede reasignar username sin perder otros campos")
    void reasignarUsername_noAfectaOtrosCampos() {
        Usuario usuario = new Usuario();
        usuario.setId(1);
        usuario.setUsername("user_v1");
        usuario.setCorreo("user@test.com");

        usuario.setUsername("user_v2");

        assertEquals(1,              usuario.getId());
        assertEquals("user_v2",      usuario.getUsername());
        assertEquals("user@test.com",usuario.getCorreo());
    }

    @Test
    @DisplayName("ciudadId con Integer distinto de null se almacena correctamente")
    void ciudadId_conValor_seAlmacenaCorrectamente() {
        Usuario usuario = new Usuario();
        usuario.setCiudadId(Integer.valueOf(10));
        assertEquals(Integer.valueOf(10), usuario.getCiudadId());
    }
}
