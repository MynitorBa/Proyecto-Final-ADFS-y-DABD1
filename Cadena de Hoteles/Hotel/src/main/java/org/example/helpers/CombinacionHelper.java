package org.example.helpers;

import java.util.*;

public class CombinacionHelper {

    /**
     Genera combinaciones numéricas para N personas usando las capacidades disponibles.
     Verifica que haya suficiente stock de cada capacidad para formar la combinación
     Excluye [N] (ya está cubierto por habitaciones individuales)
     Máximo 3 sumandos por combinación
     Devuelve las 3 mejores (las de números más grandes primero)
     *
     * @param n                  cantidad de personas
     * @param stockPorCapacidad  mapa de capacidad -> cantidad de habitaciones disponibles
     */
    public static List<List<Integer>> calcular(int n, Map<Integer, Integer> stockPorCapacidad) {
        List<List<Integer>> todasLasCombinaciones = new ArrayList<>();
        List<Integer> caps = new ArrayList<>(new TreeSet<>(stockPorCapacidad.keySet()));

        generarParticiones(n, caps, new ArrayList<>(), todasLasCombinaciones, n, stockPorCapacidad);

        // Eliminar [N] exacto (ya cubierto por habitaciones individuales)
        todasLasCombinaciones.removeIf(c -> c.size() == 1);

        // Ordenar: primero las de número más grande, luego menor cantidad de habitaciones
        todasLasCombinaciones.sort((a, b) -> {
            int maxA = a.stream().mapToInt(Integer::intValue).max().orElse(0);
            int maxB = b.stream().mapToInt(Integer::intValue).max().orElse(0);
            if (maxB != maxA) return maxB - maxA;
            return a.size() - b.size();
        });

        return todasLasCombinaciones.subList(0, Math.min(3, todasLasCombinaciones.size()));
    }

    private static void generarParticiones(int restante, List<Integer> caps,
                                           List<Integer> actual, List<List<Integer>> resultado,
                                           int minCap, Map<Integer, Integer> stockPorCapacidad) {
        if (restante == 0) {
            resultado.add(new ArrayList<>(actual));
            return;
        }
        if (actual.size() >= 3) return;

        for (int i = caps.size() - 1; i >= 0; i--) {
            int cap = caps.get(i);
            if (cap > minCap) continue;
            if (cap > restante) continue;

            // Verificar que haya stock suficiente para esta capacidad
            long usadasEnActual = actual.stream().filter(c -> c == cap).count();
            int stockDisponible = stockPorCapacidad.getOrDefault(cap, 0);
            if (usadasEnActual >= stockDisponible) continue; // no hay suficientes

            actual.add(cap);
            generarParticiones(restante - cap, caps, actual, resultado, cap, stockPorCapacidad);
            actual.remove(actual.size() - 1);
        }
    }
}