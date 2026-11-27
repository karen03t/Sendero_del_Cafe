package co.edu.autonoma.turismo.service.ruta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Grafo disperso de lugares para calcular caminos minimos con Dijkstra.
 *
 * Cada lugar se conecta con sus k vecinos mas cercanos (grafo no completo),
 * de modo que entre dos puntos lejanos el camino puede pasar por otros lugares.
 * Con un grafo completo la distancia de Dijkstra coincidiria con la linea recta
 * y el algoritmo no aportaria nada; por eso se trabaja con k vecinos.
 */
final class GrafoRutas {

    private static final double RADIO_TIERRA_KM = 6371.0;

    record Punto(double lat, double lng) {
    }

    record Resultado(double[] distancias, int[] previos) {
    }

    private final List<Punto> puntos;
    private final List<List<Integer>> vecinos = new ArrayList<>();
    private final List<List<Double>> pesos = new ArrayList<>();

    GrafoRutas(List<Punto> puntos, int kVecinos) {

        this.puntos = List.copyOf(puntos);
        int n = puntos.size();

        for (int i = 0; i < n; i++) {
            vecinos.add(new ArrayList<>());
            pesos.add(new ArrayList<>());
        }

        for (int i = 0; i < n; i++) {

            final int actual = i;

            List<Integer> orden = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                if (j != i) {
                    orden.add(j);
                }
            }

            orden.sort(Comparator.comparingDouble(j -> distancia(actual, j)));

            for (int m = 0; m < Math.min(kVecinos, orden.size()); m++) {
                int j = orden.get(m);
                conectar(i, j);
            }
        }
    }

    private void conectar(int a, int b) {

        double peso = distancia(a, b);

        if (!vecinos.get(a).contains(b)) {
            vecinos.get(a).add(b);
            pesos.get(a).add(peso);
        }

        if (!vecinos.get(b).contains(a)) {
            vecinos.get(b).add(a);
            pesos.get(b).add(peso);
        }
    }

    /** Matriz de distancias minimas entre todos los nodos. */
    double[][] todasLasDistancias() {

        int n = puntos.size();
        double[][] matriz = new double[n][];

        for (int i = 0; i < n; i++) {
            matriz[i] = dijkstra(i).distancias();
        }

        return matriz;
    }

    Resultado dijkstra(int origen) {

        int n = puntos.size();

        double[] dist = new double[n];
        int[] previo = new int[n];

        Arrays.fill(dist, Double.POSITIVE_INFINITY);
        Arrays.fill(previo, -1);
        dist[origen] = 0;

        PriorityQueue<double[]> cola =
                new PriorityQueue<>(Comparator.comparingDouble(a -> a[0]));

        cola.add(new double[]{0, origen});

        while (!cola.isEmpty()) {

            double[] actual = cola.poll();
            int u = (int) actual[1];

            if (actual[0] > dist[u] + 1e-12) {
                continue;
            }

            for (int k = 0; k < vecinos.get(u).size(); k++) {

                int v = vecinos.get(u).get(k);
                double candidato = dist[u] + pesos.get(u).get(k);

                if (candidato < dist[v] - 1e-12) {
                    dist[v] = candidato;
                    previo[v] = u;
                    cola.add(new double[]{candidato, v});
                }
            }
        }

        return new Resultado(dist, previo);
    }

    /** Camino reconstruido desde el origen hasta el destino. */
    List<Integer> camino(int[] previos, int destino) {

        List<Integer> camino = new ArrayList<>();

        for (int actual = destino; actual != -1; actual = previos[actual]) {
            camino.add(0, actual);
        }

        return camino;
    }

    private double distancia(int a, int b) {

        Punto p = puntos.get(a);
        Punto q = puntos.get(b);

        double dLat = Math.toRadians(q.lat() - p.lat());
        double dLng = Math.toRadians(q.lng() - p.lng());

        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(p.lat()))
                * Math.cos(Math.toRadians(q.lat()))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        return 2 * RADIO_TIERRA_KM * Math.asin(Math.sqrt(h));
    }
}
