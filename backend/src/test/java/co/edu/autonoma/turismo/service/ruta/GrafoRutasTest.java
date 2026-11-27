package co.edu.autonoma.turismo.service.ruta;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Dijkstra sobre el grafo disperso: con k=1 los lugares se encadenan y el
 * camino entre los extremos pasa por los intermedios, no por linea recta.
 */
class GrafoRutasTest {

    @Test
    void encuentraElCaminoMasCortoPasandoPorOtrosLugares() {

        // Cadena A-B-C-D: A->B 1.11 km, B->C 0.60 km, C->D 0.80 km.
        // La linea recta A->D es ~2.00 km, pero no hay arista directa.
        List<GrafoRutas.Punto> puntos = List.of(
                new GrafoRutas.Punto(0.0000, 0.0000),
                new GrafoRutas.Punto(0.0000, 0.0100),
                new GrafoRutas.Punto(0.0054, 0.0100),
                new GrafoRutas.Punto(0.0054, 0.0172));

        GrafoRutas grafo = new GrafoRutas(puntos, 1);

        GrafoRutas.Resultado resultado = grafo.dijkstra(0);

        assertThat(resultado.distancias()[3])
                .isCloseTo(2.51, within(0.08))
                .isGreaterThan(2.0);

        assertThat(grafo.camino(resultado.previos(), 3))
                .containsExactly(0, 1, 2, 3);
    }

    @Test
    void todosLosNodosAlcanzanAlRestoEnUnGrafoConectado() {

        List<GrafoRutas.Punto> puntos = List.of(
                new GrafoRutas.Punto(4.6370, -75.5710),
                new GrafoRutas.Punto(4.6390, -75.5690),
                new GrafoRutas.Punto(4.6410, -75.5740),
                new GrafoRutas.Punto(4.6350, -75.5760),
                new GrafoRutas.Punto(4.6330, -75.5800));

        GrafoRutas grafo = new GrafoRutas(puntos, 4);
        double[][] distancias = grafo.todasLasDistancias();

        for (int i = 0; i < puntos.size(); i++) {
            assertThat(distancias[i][i]).isZero();
            for (int j = 0; j < puntos.size(); j++) {
                if (i != j) {
                    assertThat(distancias[i][j]).isGreaterThan(0.0);
                    assertThat(Double.isInfinite(distancias[i][j])).isFalse();
                }
            }
        }
    }
}
