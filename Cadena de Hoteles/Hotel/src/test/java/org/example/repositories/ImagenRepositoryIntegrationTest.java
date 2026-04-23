package org.example.repositories;

import org.example.data.DatabaseManager;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integracion para {@link ImagenRepository}.
 * <p>
 * Conecta a Oracle real para verificar la recuperacion y eliminacion de imagenes
 * de hotel, habitacion y amenidad. Inserta imagenes minimas de prueba (1 byte)
 * para verificar el ciclo completo de lectura y borrado.
 * </p>
 * <p>
 * El {@code @BeforeEach} obtiene un hotel y una habitacion existentes de Oracle
 * para poder insertar imagenes asociadas a ellos. El {@code @AfterEach}
 * elimina las imagenes de prueba por ID.
 * </p>
 * <p>
 * Requiere que Oracle este corriendo en {@code localhost:1521/XEPDB1} con las
 * tablas {@code Hotel}, {@code Habitacion}, {@code ImagenHotel} e
 * {@code ImagenHabitacion} accesibles.
 * </p>
 */
@DisplayName("Integracion: ImagenRepository - Imagenes de hotel y habitacion contra Oracle")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ImagenRepositoryIntegrationTest {

    /** Repositorio bajo prueba. */
    private ImagenRepository imagenRepository;

    /** ID del hotel existente obtenido de Oracle en {@code @BeforeEach}. */
    private int hotelId;

    /** ID de la habitacion existente obtenida de Oracle en {@code @BeforeEach}. */
    private int habitacionId;

    /** ID de la imagen de hotel insertada en {@code @BeforeEach}. */
    private int imagenHotelId;

    /** ID de la imagen de habitacion insertada en {@code @BeforeEach}. */
    private int imagenHabitacionId;

    /** Bytes de imagen minima usados para las pruebas. */
    private static final byte[] IMAGEN_BYTES = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};

    /**
     * Inicializa el repositorio, obtiene hotel y habitacion existentes de Oracle
     * e inserta imagenes de prueba minimas en {@code ImagenHotel} e
     * {@code ImagenHabitacion}.
     */
    @BeforeEach
    void setUp() {
        imagenRepository = new ImagenRepository();

        // 1. Obtiene un hotel existente en Oracle
        List<Integer> hoteles = DatabaseManager.executeQuery(
                "SELECT ID FROM Hotel WHERE ROWNUM = 1", rs -> rs.getInt("ID"));
        Assumptions.assumeTrue(!hoteles.isEmpty(),
                "No hay hoteles en Oracle — se omite la prueba");
        hotelId = hoteles.get(0);

        // 2. Obtiene una habitacion existente en Oracle
        List<Integer> habitaciones = DatabaseManager.executeQuery(
                "SELECT ID FROM Habitacion WHERE ROWNUM = 1", rs -> rs.getInt("ID"));
        Assumptions.assumeTrue(!habitaciones.isEmpty(),
                "No hay habitaciones en Oracle — se omite la prueba");
        habitacionId = habitaciones.get(0);

        // 3. Inserta imagen de hotel de prueba (3 bytes)
        imagenHotelId = DatabaseManager.executeInsertReturnId(
                "INSERT INTO ImagenHotel (HotelID, Imagen) VALUES (?, ?)",
                "ID", hotelId, IMAGEN_BYTES
        );
        Assumptions.assumeTrue(imagenHotelId > 0,
                "No se pudo insertar imagen de hotel de prueba — se omite la prueba");

        // 4. Inserta imagen de habitacion de prueba (3 bytes)
        imagenHabitacionId = DatabaseManager.executeInsertReturnId(
                "INSERT INTO ImagenHabitacion (HabitacionID, Imagen) VALUES (?, ?)",
                "ID", habitacionId, IMAGEN_BYTES
        );
        Assumptions.assumeTrue(imagenHabitacionId > 0,
                "No se pudo insertar imagen de habitacion de prueba — se omite la prueba");
    }

    /**
     * Elimina las imagenes de prueba insertadas en el setup.
     */
    @AfterEach
    void tearDown() {
        if (imagenHotelId > 0) {
            DatabaseManager.executeUpdate(
                    "DELETE FROM ImagenHotel WHERE ID = ?", imagenHotelId);
        }
        if (imagenHabitacionId > 0) {
            DatabaseManager.executeUpdate(
                    "DELETE FROM ImagenHabitacion WHERE ID = ?", imagenHabitacionId);
        }
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    /**
     * Verifica que {@link ImagenRepository#obtenerImagenHotel} retorne los bytes
     * correctos para la imagen de hotel insertada en el setup.
     */
    @Test
    @Order(1)
    @DisplayName("1. obtenerImagenHotel con ID existente retorna bytes no nulos")
    void obtenerImagenHotel_idExistente_retornaBytes() {
        byte[] imagen = imagenRepository.obtenerImagenHotel(imagenHotelId);

        assertNotNull(imagen,
                "obtenerImagenHotel no debe retornar null para una imagen existente");
        assertTrue(imagen.length > 0,
                "Los bytes de la imagen no deben estar vacios");
    }

    /**
     * Verifica que {@link ImagenRepository#obtenerImagenHotel} retorne
     * {@code null} para un ID que no existe en la base de datos.
     */
    @Test
    @Order(2)
    @DisplayName("2. obtenerImagenHotel con ID inexistente retorna null")
    void obtenerImagenHotel_idInexistente_retornaNull() {
        byte[] imagen = imagenRepository.obtenerImagenHotel(-999);

        assertNull(imagen,
                "obtenerImagenHotel debe retornar null para un ID que no existe");
    }

    /**
     * Verifica que {@link ImagenRepository#obtenerImagenHabitacion} retorne los
     * bytes correctos para la imagen de habitacion insertada en el setup.
     */
    @Test
    @Order(3)
    @DisplayName("3. obtenerImagenHabitacion con ID existente retorna bytes no nulos")
    void obtenerImagenHabitacion_idExistente_retornaBytes() {
        byte[] imagen = imagenRepository.obtenerImagenHabitacion(imagenHabitacionId);

        assertNotNull(imagen,
                "obtenerImagenHabitacion no debe retornar null para una imagen existente");
        assertTrue(imagen.length > 0,
                "Los bytes de la imagen no deben estar vacios");
    }

    /**
     * Verifica que {@link ImagenRepository#obtenerImagenHabitacion} retorne
     * {@code null} para un ID que no existe en la base de datos.
     */
    @Test
    @Order(4)
    @DisplayName("4. obtenerImagenHabitacion con ID inexistente retorna null")
    void obtenerImagenHabitacion_idInexistente_retornaNull() {
        byte[] imagen = imagenRepository.obtenerImagenHabitacion(-999);

        assertNull(imagen,
                "obtenerImagenHabitacion debe retornar null para un ID que no existe");
    }

    /**
     * Verifica que {@link ImagenRepository#eliminarImagenHotel} elimine
     * correctamente la imagen de hotel de Oracle, de modo que una consulta
     * posterior retorne {@code null}.
     */
    @Test
    @Order(5)
    @DisplayName("5. eliminarImagenHotel elimina la imagen y obtenerImagenHotel retorna null")
    void eliminarImagenHotel_eliminaImagen_noObtenible() {
        imagenRepository.eliminarImagenHotel(imagenHotelId);
        imagenHotelId = 0; // previene doble borrado en tearDown

        byte[] imagen = imagenRepository.obtenerImagenHotel(imagenHotelId);

        assertNull(imagen,
                "obtenerImagenHotel debe retornar null tras eliminar la imagen");
    }

    /**
     * Verifica que {@link ImagenRepository#eliminarImagenHabitacion} elimine
     * correctamente la imagen de habitacion de Oracle, de modo que una consulta
     * posterior retorne {@code null}.
     */
    @Test
    @Order(6)
    @DisplayName("6. eliminarImagenHabitacion elimina la imagen y obtenerImagenHabitacion retorna null")
    void eliminarImagenHabitacion_eliminaImagen_noObtenible() {
        imagenRepository.eliminarImagenHabitacion(imagenHabitacionId);
        imagenHabitacionId = 0; // previene doble borrado en tearDown

        byte[] imagen = imagenRepository.obtenerImagenHabitacion(imagenHabitacionId);

        assertNull(imagen,
                "obtenerImagenHabitacion debe retornar null tras eliminar la imagen");
    }

    /**
     * Verifica que {@link ImagenRepository#obtenerImagenAmenidad} retorne
     * {@code null} para un ID que no existe en la base de datos.
     */
    @Test
    @Order(7)
    @DisplayName("7. obtenerImagenAmenidad con ID inexistente retorna null")
    void obtenerImagenAmenidad_idInexistente_retornaNull() {
        byte[] imagen = imagenRepository.obtenerImagenAmenidad(-999);

        assertNull(imagen,
                "obtenerImagenAmenidad debe retornar null para un ID que no existe");
    }
}
