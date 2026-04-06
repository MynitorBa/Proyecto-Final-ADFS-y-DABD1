package org.example.helpers;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para CombinacionHelper.
 * Verifica la generacion de combinaciones de habitaciones para alojar N personas,
 * respetando el limite de 3 habitaciones por combinacion y el stock disponible.
 */
class CombinacionHelperTest {

    // -- calcular

    /**
     * Verifica que calcular retorna lista vacia cuando el mapa de stock esta vacio.
     */
    @Test
    void calcular_stockVacio_retornaListaVacia() {
        List<List<Integer>> result = CombinacionHelper.calcular(4, Map.of());

        assertTrue(result.isEmpty(), "Sin stock no puede haber combinaciones");
    }

    /**
     * Verifica que calcular retorna lista vacia cuando no hay combinaciones validas
     * porque todas requieren mas de 3 habitaciones.
     * Con n=4 y stock={1:1} la unica particion seria [1,1,1,1] que necesita 4 habitaciones,
     * superando el limite de 3 por combinacion.
     */
    @Test
    void calcular_sinCombinacionesValidas_retornaVacio() {
        Map<Integer, Integer> stock = Map.of(1, 1);
        List<List<Integer>> result = CombinacionHelper.calcular(4, stock);

        assertTrue(result.isEmpty(), "No debe haber combinaciones si todas superan el limite de 3 habitaciones");
    }

    /**
     * Verifica que calcular encuentra la combinacion correcta de dos habitaciones
     * cuando el stock lo permite.
     */
    @Test
    void calcular_combinacionDosHabitaciones() {
        Map<Integer, Integer> stock = Map.of(2, 2);
        List<List<Integer>> result = CombinacionHelper.calcular(4, stock);

        assertEquals(1, result.size(), "Debe haber exactamente una combinacion valida");
        assertEquals(List.of(2, 2), result.get(0), "La combinacion correcta es [2, 2]");
    }

    /**
     * Verifica que calcular elimina la combinacion de una sola habitacion exacta
     * porque ya es cubierta por la busqueda individual.
     */
    @Test
    void calcular_eliminaCombinacionExactaUnaHabitacion() {
        Map<Integer, Integer> stock = Map.of(3, 1, 2, 1, 1, 1);
        List<List<Integer>> result = CombinacionHelper.calcular(3, stock);

        boolean contiene3Solo = result.stream().anyMatch(c -> c.size() == 1 && c.get(0) == 3);
        assertFalse(contiene3Solo, "La combinacion exacta de una habitacion [3] debe eliminarse");
    }

    /**
     * Verifica que calcular no devuelve mas de 3 combinaciones aunque el stock
     * genere mas opciones posibles.
     */
    @Test
    void calcular_retornaMaximo3Combinaciones() {
        Map<Integer, Integer> stock = Map.of(1, 3, 2, 3, 3, 3, 4, 3, 5, 3);
        List<List<Integer>> result = CombinacionHelper.calcular(6, stock);

        assertTrue(result.size() <= 3, "No debe retornar mas de 3 combinaciones");
    }

    /**
     * Verifica que calcular incluye la combinacion [2, 1] cuando n=3 y hay
     * stock de habitaciones de capacidad 2 y 1.
     */
    @Test
    void calcular_conMultiplesCapacidades_retornaCombinaciones() {
        Map<Integer, Integer> stock = Map.of(2, 2, 1, 2);
        List<List<Integer>> result = CombinacionHelper.calcular(3, stock);

        boolean contieneDos1 = result.stream().anyMatch(c -> c.equals(List.of(2, 1)));
        assertTrue(contieneDos1, "El resultado debe incluir la combinacion [2, 1]");
    }
}
