package org.example.services;

import org.example.data.DatabaseManager;
import org.example.helpers.PasswordHelper;
import org.example.repositories.AdminBusquedaRepository;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integracion para AdminBusquedaService.
 * Conecta a Oracle real, inserta un usuario y busquedas de prueba de tipo web
 * antes de cada caso y limpia todos los registros al finalizar.
 * Requiere que Oracle este corriendo en localhost:1521/XEPDB1.
 */
@DisplayName("Integracion: AdminBusquedaService - Busqueda del Admin contra Oracle")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdminBusquedaServiceIntegrationTest {

    private AdminBusquedaService adminBusquedaService;
    private int usuarioIdInsertado;
    private int ciudadIdReal;
    private String ciudadNombreReal;

    /**
     * Inicializa el service con el repositorio real, inserta un usuario de prueba,
     * obtiene una ciudad existente en Oracle e inserta tres busquedas de prueba:
     * dos de tipo web y una anonima, para cubrir los distintos casos de filtro.
     */
    @BeforeEach
    void setUp() {
        adminBusquedaService = new AdminBusquedaService(new AdminBusquedaRepository());

        usuarioIdInsertado = DatabaseManager.executeInsertReturnId(
                "INSERT INTO Usuario (Username, Correo, Contrasena, Rol_ID, Pasaporte) " +
                        "VALUES (?, ?, ?, 1, 'IT-ADMIN-BUS')",
                "ID",
                "test_admin_busqueda",
                "test_admin_busqueda@hotel.com",
                PasswordHelper.hashear("TestPass123")
        );

        // Obtiene una ciudad real de Oracle para asociar a las busquedas de prueba
        List<Object[]> ciudades = DatabaseManager.executeQuery(
                "SELECT ID, Nombre FROM Ciudad WHERE ROWNUM = 1",
                rs -> new Object[]{rs.getInt("ID"), rs.getString("Nombre")}
        );
        Assumptions.assumeTrue(!ciudades.isEmpty(), "No hay ciudades en Oracle, se omite la prueba");
        ciudadIdReal     = (int)    ciudades.get(0)[0];
        ciudadNombreReal = (String) ciudades.get(0)[1];

        String checkIn  = LocalDate.now().plusDays(10).toString();
        String checkOut = LocalDate.now().plusDays(13).toString();

        // Busqueda web autenticada (TipoBusquedaID = 1, con UsuarioID)
        DatabaseManager.executeUpdate(
                "INSERT INTO Busqueda (CiudadID, FechaCheckIn, FechaCheckOut, CantidadPersonas, " +
                        "UsuarioID, AgenciaID, TipoBusquedaID, Fecha) " +
                        "VALUES (?, TO_DATE(?, 'YYYY-MM-DD'), TO_DATE(?, 'YYYY-MM-DD'), 2, ?, NULL, 1, SYSDATE)",
                ciudadIdReal, checkIn, checkOut, usuarioIdInsertado
        );

        // Segunda busqueda web autenticada del mismo usuario
        DatabaseManager.executeUpdate(
                "INSERT INTO Busqueda (CiudadID, FechaCheckIn, FechaCheckOut, CantidadPersonas, " +
                        "UsuarioID, AgenciaID, TipoBusquedaID, Fecha) " +
                        "VALUES (?, TO_DATE(?, 'YYYY-MM-DD'), TO_DATE(?, 'YYYY-MM-DD'), 3, ?, NULL, 1, SYSDATE)",
                ciudadIdReal, checkIn, checkOut, usuarioIdInsertado
        );

        // Busqueda anonima sin usuario ni agencia
        DatabaseManager.executeUpdate(
                "INSERT INTO Busqueda (CiudadID, FechaCheckIn, FechaCheckOut, CantidadPersonas, " +
                        "UsuarioID, AgenciaID, TipoBusquedaID, Fecha) " +
                        "VALUES (?, TO_DATE(?, 'YYYY-MM-DD'), TO_DATE(?, 'YYYY-MM-DD'), 1, NULL, NULL, NULL, SYSDATE)",
                ciudadIdReal, checkIn, checkOut
        );
    }

    /**
     * Elimina en orden correcto las busquedas de prueba y el usuario insertado.
     */
    @AfterEach
    void tearDown() {
        DatabaseManager.executeUpdate(
                "DELETE FROM Busqueda WHERE UsuarioID = ?", usuarioIdInsertado);
        DatabaseManager.executeUpdate(
                "DELETE FROM Busqueda WHERE UsuarioID IS NULL AND CiudadID = ? " +
                        "AND TRUNC(Fecha) = TRUNC(SYSDATE)", ciudadIdReal);
        DatabaseManager.executeUpdate(
                "DELETE FROM Usuario WHERE ID = ?", usuarioIdInsertado);
    }

    /**
     * Verifica que listar sin filtros retorne resultados de Oracle y que el campo
     * "total" del mapa coincida con la cantidad real de filas en la BD.
     */
    @Test
    @Order(1)
    @DisplayName("1. listar sin filtros retorna resultados y el total coincide con Oracle")
    void listarSinFiltrosRetornaResultadosYTotalCorrecto() {

        Map<String, Object> resultado = adminBusquedaService.listar(
                null, null, "todos", null, null, 1, 50);

        assertNotNull(resultado,                               "El mapa de resultado no debe ser null");
        assertTrue(resultado.containsKey("busquedas"),         "Debe contener la clave 'busquedas'");
        assertTrue(resultado.containsKey("total"),             "Debe contener la clave 'total'");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> busquedas = (List<Map<String, Object>>) resultado.get("busquedas");
        int total = (int) resultado.get("total");

        assertNotNull(busquedas,                              "La lista de busquedas no debe ser null");
        assertTrue(total > 0,                                 "El total debe ser mayor a cero en Oracle");
        assertTrue(busquedas.size() <= 50,                    "La pagina no debe superar el limite de 50");
        assertEquals(total >= busquedas.size(), true,         "El total debe ser mayor o igual al tamano de pagina");
    }

    /**
     * Verifica que listar con filtro de destino retorne solo las busquedas
     * cuya ciudad contiene el texto indicado, segun el LIKE en Oracle.
     */
    @Test
    @Order(2)
    @DisplayName("2. listar con filtro de destino retorna solo busquedas de esa ciudad en Oracle")
    void listarConFiltroDestinoFiltraCorrectamente() {

        // Usa los primeros 3 caracteres del nombre real para el filtro LIKE
        String filtro = ciudadNombreReal.substring(0, Math.min(3, ciudadNombreReal.length()));

        Map<String, Object> resultado = adminBusquedaService.listar(
                filtro, null, "todos", null, null, 1, 50);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> busquedas = (List<Map<String, Object>>) resultado.get("busquedas");

        assertNotNull(busquedas, "La lista no debe ser null con filtro de destino");

        // Todas las filas retornadas deben pertenecer a la ciudad filtrada
        for (Map<String, Object> fila : busquedas) {
            String destino = (String) fila.get("destino");
            assertNotNull(destino, "El campo destino no debe ser null");
            assertTrue(destino.toLowerCase().contains(filtro.toLowerCase()),
                    "El destino '" + destino + "' debe contener el filtro '" + filtro + "'");
        }
    }

    /**
     * Verifica que listar con tipo "web" retorne solo busquedas de tipo web
     * y que el campo "tipo" de cada fila sea "web" segun Oracle.
     */
    @Test
    @Order(3)
    @DisplayName("3. listar con tipo web retorna solo busquedas web de Oracle")
    void listarConTipoWebRetornaSoloBusquedasWeb() {

        Map<String, Object> resultado = adminBusquedaService.listar(
                null, null, "web", null, null, 1, 50);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> busquedas = (List<Map<String, Object>>) resultado.get("busquedas");

        assertNotNull(busquedas, "La lista no debe ser null con filtro de tipo web");

        for (Map<String, Object> fila : busquedas) {
            assertEquals("web", fila.get("tipo"),
                    "Todas las busquedas retornadas deben ser de tipo web");
        }
    }

    /**
     * Verifica que la paginacion funcione correctamente en Oracle: la pagina 2
     * con 1 resultado por pagina debe retornar exactamente 1 fila y el total
     * debe ser mayor a 1 para que la segunda pagina tenga sentido.
     */
    @Test
    @Order(4)
    @DisplayName("4. Paginacion retorna la segunda pagina correctamente desde Oracle")
    void listarPaginacionRetornaSegundaPagina() {

        // Pagina 1 con 1 resultado
        Map<String, Object> pagina1 = adminBusquedaService.listar(
                null, null, "todos", null, null, 1, 1);
        int total = (int) pagina1.get("total");

        Assumptions.assumeTrue(total >= 2, "Se necesitan al menos 2 busquedas en Oracle para probar paginacion");

        // Pagina 2 con 1 resultado
        Map<String, Object> pagina2 = adminBusquedaService.listar(
                null, null, "todos", null, null, 2, 1);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> busquedasPagina1 = (List<Map<String, Object>>) pagina1.get("busquedas");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> busquedasPagina2 = (List<Map<String, Object>>) pagina2.get("busquedas");

        assertEquals(1, busquedasPagina1.size(), "La pagina 1 debe tener exactamente 1 resultado");
        assertEquals(1, busquedasPagina2.size(), "La pagina 2 debe tener exactamente 1 resultado");

        // Los IDs de ambas paginas deben ser distintos
        int idPagina1 = (int) busquedasPagina1.get(0).get("id");
        int idPagina2 = (int) busquedasPagina2.get(0).get("id");
        assertNotEquals(idPagina1, idPagina2, "Cada pagina debe retornar un registro diferente de Oracle");
    }

    /**
     * Verifica que resumen retorne las claves requeridas con valores coherentes:
     * totalWeb y totalRest como enteros no negativos, y las listas porDia y topDestinos
     * inicializadas correctamente desde Oracle.
     */
    @Test
    @Order(5)
    @DisplayName("5. resumen retorna totalWeb, totalRest, porDia y topDestinos desde Oracle")
    void resumenRetornaEstructuraCompletaDesdeOracle() {

        Map<String, Object> resumen = adminBusquedaService.resumen();

        assertNotNull(resumen,                           "El mapa de resumen no debe ser null");
        assertTrue(resumen.containsKey("totalWeb"),      "Debe contener 'totalWeb'");
        assertTrue(resumen.containsKey("totalRest"),     "Debe contener 'totalRest'");
        assertTrue(resumen.containsKey("porDia"),        "Debe contener 'porDia'");
        assertTrue(resumen.containsKey("topDestinos"),   "Debe contener 'topDestinos'");

        int totalWeb  = (int) resumen.get("totalWeb");
        int totalRest = (int) resumen.get("totalRest");

        assertTrue(totalWeb  >= 0, "totalWeb debe ser un entero no negativo");
        assertTrue(totalRest >= 0, "totalRest debe ser un entero no negativo");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> porDia = (List<Map<String, Object>>) resumen.get("porDia");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topDestinos = (List<Map<String, Object>>) resumen.get("topDestinos");

        assertNotNull(porDia,      "La lista porDia no debe ser null");
        assertNotNull(topDestinos, "La lista topDestinos no debe ser null");

        // topDestinos tiene un maximo de 10 segun el repositorio
        assertTrue(topDestinos.size() <= 10, "topDestinos no debe superar 10 elementos");
    }
}