package org.example.services;

import org.example.data.DatabaseManager;
import org.example.dtos.BusquedaRequestDTO;
import org.example.dtos.HotelResultadoDTO;
import org.example.helpers.PasswordHelper;
import org.example.repositories.BusquedaRepository;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integracion para BusquedaService.
 * Conecta a Oracle real para verificar el flujo completo de busqueda de hoteles:
 * validacion de ciudad, registro de busqueda en la tabla Busqueda y enriquecimiento
 * de resultados con imagenes y amenidades reales.
 * Requiere que Oracle este corriendo en localhost:1521/XEPDB1.
 */
@DisplayName("Integracion: BusquedaService - Flujo de Busqueda de Hoteles")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BusquedaServiceIntegrationTest {

    private BusquedaService busquedaService;
    private int usuarioIdInsertado;
    private String ciudadReal;
    private String paisReal;

    /**
     * Inicializa el service con el repositorio real, inserta un usuario de prueba
     * y obtiene los nombres de una ciudad y pais existentes en Oracle para las busquedas.
     */
    @BeforeEach
    void setUp() {
        busquedaService = new BusquedaService(new BusquedaRepository());

        usuarioIdInsertado = DatabaseManager.executeInsertReturnId(
                "INSERT INTO Usuario (Username, Correo, Contrasena, Rol_ID, Pasaporte) " +
                        "VALUES (?, ?, ?, 2, 'IT-BUSQUEDA')",
                "ID",
                "test_busqueda_integration",
                "test_busqueda_integration@hotel.com",
                PasswordHelper.hashear("TestPass123")
        );

        // Obtiene una ciudad y pais reales que existan en Oracle para usar en los casos
        List<String[]> ciudades = DatabaseManager.executeQuery(
                "SELECT c.Nombre AS Ciudad, p.Nombre AS Pais " +
                        "FROM Ciudad c JOIN Pais p ON c.Pais_ID = p.ID WHERE ROWNUM = 1",
                rs -> new String[]{rs.getString("Ciudad"), rs.getString("Pais")}
        );
        Assumptions.assumeTrue(!ciudades.isEmpty(), "No hay ciudades en Oracle, se omite la prueba");
        ciudadReal = ciudades.get(0)[0];
        paisReal   = ciudades.get(0)[1];
    }

    /**
     * Elimina el usuario de prueba y las busquedas registradas por los casos de prueba.
     */
    @AfterEach
    void tearDown() {
        DatabaseManager.executeUpdate(
                "DELETE FROM Busqueda WHERE UsuarioID = ?", usuarioIdInsertado);
        DatabaseManager.executeUpdate(
                "DELETE FROM Usuario WHERE ID = ?", usuarioIdInsertado);
    }

    /**
     * Construye un BusquedaRequestDTO con la ciudad y pais reales obtenidos de Oracle.
     * @return request listo para pasar al service.
     */
    private BusquedaRequestDTO requestConCiudadReal() {
        BusquedaRequestDTO req = new BusquedaRequestDTO();
        req.setCiudad(ciudadReal);
        req.setPais(paisReal);
        req.setFechaCheckIn("2027-06-01");
        req.setFechaCheckOut("2027-06-05");
        req.setCantidadPersonas(2);
        return req;
    }

    /**
     * Verifica que la busqueda con una ciudad real de Oracle retorne una lista no nula,
     * y que cada hotel del resultado tenga imagenes y amenidades asignadas.
     */
    @Test
    @Order(1)
    @DisplayName("1. Busqueda con ciudad real retorna hoteles enriquecidos con imagenes y amenidades")
    void busquedaConCiudadRealRetornaHoteles() {

        List<HotelResultadoDTO> resultado = busquedaService.buscar(requestConCiudadReal(), usuarioIdInsertado);

        assertNotNull(resultado, "La lista de hoteles no debe ser null");

        for (HotelResultadoDTO hotel : resultado) {
            assertNotNull(hotel.getImagenesIds(),  "Cada hotel debe tener la lista de imagenes inicializada");
            assertNotNull(hotel.getAmenidades(),   "Cada hotel debe tener la lista de amenidades inicializada");
            assertNotNull(hotel.getTiposHabitacion(), "Cada hotel debe tener los tipos de habitacion inicializados");
        }
    }

    /**
     * Verifica que la busqueda registre un nuevo registro en la tabla Busqueda de Oracle
     * con el usuarioId, ciudad y fechas correctas.
     */
    @Test
    @Order(2)
    @DisplayName("2. Busqueda autenticada registra el evento en la tabla Busqueda de Oracle")
    void busquedaRegistraEventoEnOracle() {

        busquedaService.buscar(requestConCiudadReal(), usuarioIdInsertado);

        List<Integer> busquedas = DatabaseManager.executeQuery(
                "SELECT ID FROM Busqueda WHERE UsuarioID = ?",
                rs -> rs.getInt("ID"), usuarioIdInsertado
        );
        assertFalse(busquedas.isEmpty(),
                "Debe haberse registrado al menos una busqueda en Oracle con el usuarioId del test");
    }

    /**
     * Verifica que la busqueda anonima (sin sesion activa) registre el evento
     * en Oracle con UsuarioID null.
     */
    @Test
    @Order(3)
    @DisplayName("3. Busqueda anonima registra el evento en Oracle con UsuarioID null")
    void busquedaAnonimaRegistraEventoConUsuarioNull() {

        busquedaService.buscar(requestConCiudadReal(), null);

        // Verifica que se inserto una fila reciente sin UsuarioID
        List<Integer> busquedas = DatabaseManager.executeQuery(
                "SELECT ID FROM Busqueda WHERE UsuarioID IS NULL " +
                        "AND Fecha >= SYSDATE - 1/24",
                rs -> rs.getInt("ID")
        );
        assertFalse(busquedas.isEmpty(),
                "Debe haberse registrado una busqueda anonima en Oracle");
    }

    /**
     * Verifica que se lanza IllegalArgumentException cuando la ciudad no existe
     * en Oracle y que no se registra ninguna busqueda en la tabla Busqueda.
     */
    @Test
    @Order(4)
    @DisplayName("4. Lanza excepcion con ciudad inexistente y Oracle no registra ninguna busqueda")
    void busquedaFallaCiudadInexistente() {

        BusquedaRequestDTO req = requestConCiudadReal();
        req.setCiudad("CiudadQueNoExisteJamas99");

        assertThrows(
                IllegalArgumentException.class,
                () -> busquedaService.buscar(req, usuarioIdInsertado)
        );

        List<Integer> busquedas = DatabaseManager.executeQuery(
                "SELECT ID FROM Busqueda WHERE UsuarioID = ?",
                rs -> rs.getInt("ID"), usuarioIdInsertado
        );
        assertTrue(busquedas.isEmpty(),
                "No debe haberse registrado ninguna busqueda en Oracle cuando la ciudad no existe");
    }
}