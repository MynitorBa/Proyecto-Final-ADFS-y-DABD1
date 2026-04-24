package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.ComentarioResponseDTO;
import org.example.helpers.PasswordHelper;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integracion para {@link ComentarioRepository}.
 * <p>
 * Conecta a Oracle real, inserta un usuario y un hotel de prueba antes de cada
 * caso y elimina todos los registros creados al finalizar, garantizando aislamiento
 * completo entre ejecuciones. Los tests estan ordenados de forma que los
 * comentarios creados en pasos anteriores puedan ser referenciados en pasos
 * posteriores a traves de la variable de instancia {@code firstCommentId}.
 * </p>
 * <p>
 * Requiere que Oracle este corriendo en localhost:1521/XEPDB1 con las tablas
 * {@code Ciudad}, {@code Estado}, {@code Usuario}, {@code Hotel} y
 * {@code Comentario} accesibles.
 * </p>
 */
@DisplayName("Integracion: ComentarioRepository - Gestion de comentarios y resenas contra Oracle")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ComentarioRepositoryIntegrationTest {

    /** Repositorio bajo prueba. */
    private ComentarioRepository comentarioRepository;

    /** ID del usuario insertado en {@code @BeforeEach}, usado para limpieza y operaciones. */
    private int usuarioId;

    /** ID del hotel insertado en {@code @BeforeEach}, usado para limpieza y operaciones. */
    private int hotelId;

    /**
     * ID del primer comentario con resena creado en {@link #crearComentario_conResena_retornaIdPositivo()}.
     * Compartido entre tests de @Order(2) en adelante para pruebas de respuesta y lectura.
     */
    private static int firstCommentId;

    /** Username fijo del usuario de prueba. */
    private static final String USERNAME  = "test_com_repo";

    /** Correo fijo del usuario de prueba. */
    private static final String CORREO    = "test_com_repo@hotel.com";

    /** Pasaporte fijo del usuario de prueba. */
    private static final String PASAPORTE = "IT-COM-001";

    /** Nombre del hotel de prueba. */
    private static final String HOTEL_NOMBRE = "Hotel Test Comentario";

    /**
     * Inicializa el repositorio, obtiene una ciudad y un estado existentes en Oracle,
     * y los usa para insertar un usuario y un hotel de prueba antes de cada caso.
     * Si no existen ciudades o estados en la base de datos se omite la prueba.
     */
    @BeforeEach
    void setUp() {
        comentarioRepository = new ComentarioRepository();

        // Obtiene una ciudad existente en Oracle
        List<Integer> ciudades = DatabaseManager.executeQuery(
                "SELECT ID FROM Ciudad WHERE ROWNUM = 1",
                rs -> rs.getInt("ID")
        );
        Assumptions.assumeTrue(!ciudades.isEmpty(),
                "No hay ciudades en Oracle, se omite la prueba");
        int ciudadId = ciudades.get(0);

        // Obtiene un estado existente en Oracle (para el hotel)
        List<Integer> estados = DatabaseManager.executeQuery(
                "SELECT ID FROM Estado WHERE ROWNUM = 1",
                rs -> rs.getInt("ID")
        );
        Assumptions.assumeTrue(!estados.isEmpty(),
                "No hay estados en Oracle, se omite la prueba");
        int estadoId = estados.get(0);

        // Inserta el usuario de prueba
        usuarioId = DatabaseManager.executeInsertReturnId(
                "INSERT INTO Usuario (Username, Correo, Contrasena, Rol_ID, Pasaporte) " +
                        "VALUES (?, ?, ?, 1, ?)",
                "ID",
                USERNAME,
                CORREO,
                PasswordHelper.hashear("TestPass123"),
                PASAPORTE
        );
        Assumptions.assumeTrue(usuarioId > 0,
                "El INSERT de Usuario no retorno un ID valido; se omite la prueba");

        // Inserta el hotel de prueba asociado a la ciudad y estado reales
        hotelId = DatabaseManager.executeInsertReturnId(
                "INSERT INTO Hotel (Nombre, Direccion, Descripcion, Rating, EstadoID, CiudadID) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                "ID",
                HOTEL_NOMBRE,
                "Calle Integracion 123",
                "Hotel creado por pruebas de integracion de ComentarioRepository",
                0.0,
                estadoId,
                ciudadId
        );
        Assumptions.assumeTrue(hotelId > 0,
                "El INSERT de Hotel no retorno un ID valido; se omite la prueba");
    }

    /**
     * Elimina en orden FK-inverso: primero los comentarios del usuario de prueba,
     * luego el hotel y finalmente el usuario, para evitar violaciones de restricciones.
     */
    @AfterEach
    void tearDown() {
        DatabaseManager.executeUpdate(
                "DELETE FROM Comentario WHERE Usuario_ID = ?", usuarioId);
        DatabaseManager.executeUpdate(
                "DELETE FROM Hotel WHERE ID = ?", hotelId);
        DatabaseManager.executeUpdate(
                "DELETE FROM Usuario WHERE ID = ?", usuarioId);
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    /**
     * Verifica que {@code existeComentarioConResena} retorne {@code false} cuando
     * el usuario no tiene ningun comentario con resena registrado para el hotel.
     * <p>
     * Este es el estado inicial antes de cualquier insercion en la tabla Comentario.
     * </p>
     */
    @Test
    @Order(1)
    @DisplayName("1. existeComentarioConResena sin comentarios retorna false")
    void existeComentarioConResena_sinComentarios_retornaFalse() {
        boolean existe = comentarioRepository.existeComentarioConResena(usuarioId, hotelId);

        assertFalse(existe,
                "Sin comentarios previos, existeComentarioConResena debe retornar false");
    }

    /**
     * Verifica que {@code crearComentario} con una resena numerica inserte el registro
     * en Oracle y retorne un ID positivo mayor a cero.
     * <p>
     * El ID generado se almacena en {@code firstCommentId} para ser reutilizado
     * en los tests de ordenes 3, 4, 6, 7 y 8.
     * </p>
     */
    @Test
    @Order(2)
    @DisplayName("2. crearComentario con resena retorna ID positivo")
    void crearComentario_conResena_retornaIdPositivo() {
        int id = comentarioRepository.crearComentario(
                usuarioId, hotelId, null, 5, "Excelente servicio y habitaciones muy limpias");

        assertTrue(id > 0,
                "crearComentario con resena debe retornar un ID positivo mayor a cero");

        firstCommentId = id;
    }

    /**
     * Verifica que {@code existeComentarioConResena} retorne {@code true} despues de
     * haber insertado un comentario con resena en el test anterior.
     * <p>
     * Requiere que {@link #crearComentario_conResena_retornaIdPositivo()} se haya
     * ejecutado previamente y haya registrado al menos un comentario con resena.
     * </p>
     */
    @Test
    @Order(3)
    @DisplayName("3. existeComentarioConResena despues de crear retorna true")
    void existeComentarioConResena_despuesDeCrear_retornaTrue() {
        // Asegura que existe al menos un comentario con resena antes de verificar
        comentarioRepository.crearComentario(
                usuarioId, hotelId, null, 4, "Muy buena ubicacion y personal amable");

        boolean existe = comentarioRepository.existeComentarioConResena(usuarioId, hotelId);

        assertTrue(existe,
                "Tras insertar un comentario con resena, existeComentarioConResena debe retornar true");
    }

    /**
     * Verifica que {@code crearComentario} sin resena (respuesta a otro comentario)
     * inserte correctamente el registro usando el ID del comentario padre y retorne
     * un ID positivo distinto al del comentario padre.
     * <p>
     * Siempre crea el comentario padre dentro de la sesion actual del test, ya que
     * {@code firstCommentId} puede apuntar a un comentario eliminado por el {@code @AfterEach}
     * de una ejecucion anterior.
     * </p>
     */
    @Test
    @Order(4)
    @DisplayName("4. crearComentario sin resena como respuesta a otro retorna ID positivo")
    void crearComentario_sinResena_esRespuestaAOtro() {
        // Siempre crea el padre en la sesion actual (firstCommentId de otro test ya fue borrado en tearDown)
        int padreId = comentarioRepository.crearComentario(
                usuarioId, hotelId, null, 5, "Comentario padre de referencia");
        assertTrue(padreId > 0, "El comentario padre debe insertarse correctamente");

        int replyId = comentarioRepository.crearComentario(
                usuarioId, hotelId, padreId, null, "Gracias por su comentario!");

        assertTrue(replyId > 0,
                "crearComentario de respuesta debe retornar un ID positivo");
        assertNotEquals(padreId, replyId,
                "El ID de la respuesta debe ser distinto al ID del comentario padre");
    }

    /**
     * Verifica que {@code actualizarRatingHotel} se ejecute sin lanzar ninguna excepcion
     * cuando el hotel tiene al menos una resena registrada en Oracle.
     * <p>
     * La ausencia de excepcion confirma que el UPDATE en la tabla Hotel se completo
     * correctamente segun el AVG de resenas en Comentario.
     * </p>
     */
    @Test
    @Order(5)
    @DisplayName("5. actualizarRatingHotel con resena no lanza excepcion")
    void actualizarRatingHotel_conResena_noLanzaExcepcion() {
        // Inserta una resena para que el AVG tenga datos sobre los que operar
        comentarioRepository.crearComentario(
                usuarioId, hotelId, null, 4, "Buen hotel, lo recomiendo");

        assertDoesNotThrow(
                () -> comentarioRepository.actualizarRatingHotel(hotelId),
                "actualizarRatingHotel no debe lanzar excepcion cuando hay resenas en Oracle");
    }

    /**
     * Verifica que {@code obtenerComentario} retorne un {@link ComentarioResponseDTO}
     * con todos sus campos principales correctamente poblados desde Oracle.
     * <p>
     * Se comprueba primero que el DTO no es nulo y luego que los campos individuales
     * coinciden con los valores insertados en el test de @Order(2).
     * </p>
     */
    @Test
    @Order(6)
    @DisplayName("6. obtenerComentario con comentario existente retorna DTO con datos correctos")
    void obtenerComentario_comentarioExistente_retornaDtoConDatos() {
        // Asegura que existe un comentario que obtener
        int idParaObtener = comentarioRepository.crearComentario(
                usuarioId, hotelId, null, 5, "Habitaciones amplias y muy bien equipadas");

        ComentarioResponseDTO dto = comentarioRepository.obtenerComentario(idParaObtener);

        assertNotNull(dto,
                "obtenerComentario debe retornar un DTO no nulo para un ID existente");
        assertEquals(idParaObtener, dto.getId(),
                "El ID del DTO debe coincidir con el ID del comentario solicitado");
        assertEquals(usuarioId, dto.getUsuarioId(),
                "El usuarioId del DTO debe coincidir con el usuario que creo el comentario");
        assertNotNull(dto.getUsername(),
                "El username del DTO no debe ser null");
        assertEquals(USERNAME, dto.getUsername(),
                "El username del DTO debe coincidir con el username insertado en Oracle");
        assertEquals(hotelId, dto.getHotelId(),
                "El hotelId del DTO debe coincidir con el hotel del comentario");
        assertNotNull(dto.getResena(),
                "La resena del DTO no debe ser null para un comentario con resena");
        assertEquals(5, dto.getResena(),
                "La resena del DTO debe ser 5 segun el valor insertado");
        assertEquals(0, dto.getDowns(),
                "Los downs de un comentario recien insertado deben ser 0");
    }

    /**
     * Verifica que {@code obtenerComentariosPorUsuario} retorne una lista no nula
     * con al menos un elemento despues de insertar un comentario para el usuario de prueba.
     * <p>
     * Tambien comprueba que cada elemento de la lista tenga un ID positivo y el
     * username del usuario correcto.
     * </p>
     */
    @Test
    @Order(7)
    @DisplayName("7. obtenerComentariosPorUsuario retorna lista con al menos un elemento")
    void obtenerComentariosPorUsuario_retornaListaConAlMenosUno() {
        // Inserta un comentario para garantizar que la lista no este vacia
        comentarioRepository.crearComentario(
                usuarioId, hotelId, null, 3, "Hotel correcto para una estancia de negocios");

        List<ComentarioResponseDTO> lista = comentarioRepository.obtenerComentariosPorUsuario(usuarioId);

        assertNotNull(lista,
                "La lista de comentarios por usuario no debe ser null");
        assertFalse(lista.isEmpty(),
                "La lista de comentarios por usuario debe contener al menos un elemento");

        ComentarioResponseDTO primero = lista.get(0);
        assertNotNull(primero,
                "El primer elemento de la lista no debe ser null");
        assertTrue(primero.getId() > 0,
                "El ID del comentario retornado debe ser positivo");
        assertEquals(USERNAME, primero.getUsername(),
                "El username del comentario debe coincidir con el del usuario de prueba");
    }

    /**
     * Verifica que {@code obtenerComentariosPorHotel} retorne una lista no nula
     * con al menos un elemento despues de insertar un comentario para el hotel de prueba.
     * <p>
     * Tambien comprueba que cada elemento de la lista tenga el hotelId correcto
     * y un ID positivo.
     * </p>
     */
    @Test
    @Order(8)
    @DisplayName("8. obtenerComentariosPorHotel retorna lista con al menos un elemento")
    void obtenerComentariosPorHotel_retornaListaConAlMenosUno() {
        // Inserta un comentario para garantizar que la lista no este vacia
        comentarioRepository.crearComentario(
                usuarioId, hotelId, null, 4, "La piscina y el restaurante son excelentes");

        List<ComentarioResponseDTO> lista = comentarioRepository.obtenerComentariosPorHotel(hotelId);

        assertNotNull(lista,
                "La lista de comentarios por hotel no debe ser null");
        assertFalse(lista.isEmpty(),
                "La lista de comentarios por hotel debe contener al menos un elemento");

        ComentarioResponseDTO primero = lista.get(0);
        assertNotNull(primero,
                "El primer elemento de la lista no debe ser null");
        assertTrue(primero.getId() > 0,
                "El ID del comentario retornado debe ser positivo");
        assertEquals(hotelId, primero.getHotelId(),
                "El hotelId del comentario debe coincidir con el hotel de prueba");
    }
}