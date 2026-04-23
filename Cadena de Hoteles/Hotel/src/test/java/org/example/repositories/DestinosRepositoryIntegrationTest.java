package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.HotelResultadoDTO;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integracion para {@link DestinosRepository}.
 * <p>
 * Conecta a Oracle real para verificar la consulta de todos los hoteles activos
 * y la recuperacion de imagenes de hotel. Todas las operaciones son de solo
 * lectura; no se insertan ni eliminan datos de prueba.
 * </p>
 * <p>
 * Requiere que Oracle este corriendo en {@code localhost:1521/XEPDB1} con las
 * tablas {@code Hotel}, {@code Estado}, {@code Ciudad}, {@code Pais} e
 * {@code ImagenHotel} accesibles.
 * </p>
 */
@DisplayName("Integracion: DestinosRepository - Consulta de destinos contra Oracle")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DestinosRepositoryIntegrationTest {

    /** Repositorio bajo prueba. */
    private DestinosRepository destinosRepository;

    /**
     * Inicializa el repositorio antes de cada prueba.
     */
    @BeforeEach
    void setUp() {
        destinosRepository = new DestinosRepository();
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    /**
     * Verifica que {@link DestinosRepository#obtenerTodosLosHoteles} retorne
     * una lista no nula. La lista puede estar vacia si no hay hoteles activos,
     * pero nunca debe ser {@code null}.
     */
    @Test
    @Order(1)
    @DisplayName("1. obtenerTodosLosHoteles retorna lista no nula")
    void obtenerTodosLosHoteles_retornaListaNoNula() {
        List<HotelResultadoDTO> lista = destinosRepository.obtenerTodosLosHoteles();

        assertNotNull(lista,
                "obtenerTodosLosHoteles no debe retornar null aunque no haya hoteles activos");
    }

    /**
     * Verifica que los hoteles retornados por
     * {@link DestinosRepository#obtenerTodosLosHoteles} tengan ID positivo,
     * nombre no nulo y estado no nulo. Solo se valida si hay al menos un hotel.
     */
    @Test
    @Order(2)
    @DisplayName("2. obtenerTodosLosHoteles con hoteles activos retorna DTOs validos")
    void obtenerTodosLosHoteles_conHotelesActivos_retornaDtosValidos() {
        List<HotelResultadoDTO> lista = destinosRepository.obtenerTodosLosHoteles();

        assertNotNull(lista);
        // Si hay hoteles activos, valida el primer DTO
        if (!lista.isEmpty()) {
            HotelResultadoDTO primero = lista.get(0);
            assertTrue(primero.getId() > 0,
                    "El ID del hotel debe ser positivo");
            assertNotNull(primero.getNombre(),
                    "El nombre del hotel no debe ser null");
            assertNotNull(primero.getEstado(),
                    "El estado del hotel no debe ser null");
        }
    }

    /**
     * Verifica que {@link DestinosRepository#obtenerImagenesHotel} retorne
     * una lista no nula para un hotel existente en Oracle. La lista puede estar
     * vacia si el hotel no tiene imagenes, pero nunca debe ser {@code null}.
     */
    @Test
    @Order(3)
    @DisplayName("3. obtenerImagenesHotel con hotel existente retorna lista no nula")
    void obtenerImagenesHotel_hotelExistente_retornaListaNoNula() {
        // Obtiene cualquier hotel existente en Oracle
        List<Integer> hoteles = DatabaseManager.executeQuery(
                "SELECT ID FROM Hotel WHERE ROWNUM = 1",
                rs -> rs.getInt("ID")
        );
        Assumptions.assumeTrue(!hoteles.isEmpty(),
                "No hay hoteles en Oracle — se omite la prueba");
        int hotelId = hoteles.get(0);

        List<Integer> imagenes = destinosRepository.obtenerImagenesHotel(hotelId);

        assertNotNull(imagenes,
                "obtenerImagenesHotel no debe retornar null para un hotel existente");
    }

    /**
     * Verifica que {@link DestinosRepository#obtenerImagenesHotel} retorne
     * una lista vacia para un hotel con ID que no existe en Oracle.
     */
    @Test
    @Order(4)
    @DisplayName("4. obtenerImagenesHotel con hotel inexistente retorna lista vacia")
    void obtenerImagenesHotel_hotelInexistente_retornaListaVacia() {
        List<Integer> imagenes = destinosRepository.obtenerImagenesHotel(-999);

        assertNotNull(imagenes,
                "obtenerImagenesHotel no debe retornar null para ID inexistente");
        assertTrue(imagenes.isEmpty(),
                "La lista de imagenes debe estar vacia para un hotel que no existe");
    }
}
