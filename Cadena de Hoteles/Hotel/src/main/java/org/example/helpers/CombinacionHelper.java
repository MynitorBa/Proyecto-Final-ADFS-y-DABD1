package org.example.helpers;

import java.util.*;

/**
 * Helper para calcular combinaciones de habitaciones que cubren un numero de personas.
 * Genera particiones numericas usando las capacidades disponibles en stock,
 * limitando a un maximo de 3 habitaciones por combinacion.
 */
public class CombinacionHelper {

    /**
     * Genera las mejores combinaciones de habitaciones para alojar a N personas.
     * Usa las capacidades disponibles en stock para formar particiones validas,
     * descarta la combinacion de una sola habitacion exacta (ya cubierta por busqueda
     * individual) y retorna las 3 mejores priorizando capacidades mas grandes.
     *
     * @param n                 cantidad de personas a alojar.
     * @param stockPorCapacidad mapa de capacidad -> cantidad de habitaciones disponibles de esa capacidad.
     * @return lista de hasta 3 combinaciones, donde cada combinacion es una lista de capacidades que suman N.
     */
    public static List<List<Integer>> calcular(int n, Map<Integer, Integer> stockPorCapacidad) {
        List<List<Integer>> todasLasCombinaciones = new ArrayList<>();
        List<Integer> caps = new ArrayList<>(new TreeSet<>(stockPorCapacidad.keySet()));

        generarParticiones(n, caps, new ArrayList<>(), todasLasCombinaciones, n, stockPorCapacidad);

        // Eliminar [N] exacto (ya cubierto por habitaciones individuales)
        todasLasCombinaciones.removeIf(c -> c.size() == 1);

        // Ordenar: primero las de numero mas grande, luego menor cantidad de habitaciones
        todasLasCombinaciones.sort((a, b) -> {
            int maxA = a.stream().mapToInt(Integer::intValue).max().orElse(0);
            int maxB = b.stream().mapToInt(Integer::intValue).max().orElse(0);
            if (maxB != maxA) return maxB - maxA;
            return a.size() - b.size();
        });

        return todasLasCombinaciones.subList(0, Math.min(3, todasLasCombinaciones.size()));
    }

    /**
     * Genera recursivamente todas las particiones validas que suman el valor restante.
     * En cada paso verifica que haya stock suficiente para la capacidad elegida
     * y que no se supere el limite de 3 sumandos por combinacion.
     *
     * @param restante          personas que aun faltan cubrir en la combinacion actual.
     * @param caps              lista de capacidades disponibles ordenadas de menor a mayor.
     * @param actual            combinacion parcial en construccion.
     * @param resultado         lista acumuladora donde se agregan las combinaciones completas.
     * @param minCap            capacidad maxima permitida en este paso (evita combinaciones repetidas).
     * @param stockPorCapacidad mapa de capacidad -> stock disponible para validar uso repetido.
     */
    private static void generarParticiones(int restante, List<Integer> caps,
                                           List<Integer> actual, List<List<Integer>> resultado,
                                           int minCap, Map<Integer, Integer> stockPorCapacidad) {
        if (restante == 0) {
            resultado.add(new ArrayList<>(actual));
            return;
        }

        // Cortar si ya se alcanzo el maximo de 3 habitaciones por combinacion
        if (actual.size() >= 3) return;

        for (int i = caps.size() - 1; i >= 0; i--) {
            int cap = caps.get(i);
            if (cap > minCap) continue;
            if (cap > restante) continue;

            // Verificar que haya stock suficiente para reutilizar esta capacidad
            long usadasEnActual = actual.stream().filter(c -> c == cap).count();
            int stockDisponible = stockPorCapacidad.getOrDefault(cap, 0);
            if (usadasEnActual >= stockDisponible) continue;

            actual.add(cap);
            generarParticiones(restante - cap, caps, actual, resultado, cap, stockPorCapacidad);
            actual.remove(actual.size() - 1);
        }
    }
}