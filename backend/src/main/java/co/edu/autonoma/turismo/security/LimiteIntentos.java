package co.edu.autonoma.turismo.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Limite de intentos en memoria por clave (IP + ruta).
 * Suficiente para una sola instancia; con varias instancias habria que
 * compartir el contador (Redis) o moverlo al balanceador.
 */
@Component
public class LimiteIntentos {

    private final int maximo;
    private final Duration ventana;
    private final Map<String, Deque<Long>> intentos = new ConcurrentHashMap<>();

    public LimiteIntentos(
            @Value("${seguridad.rate-limit.maximo:30}") int maximo,
            @Value("${seguridad.rate-limit.ventana-segundos:60}") long ventanaSegundos) {

        this.maximo = maximo;
        this.ventana = Duration.ofSeconds(ventanaSegundos);
    }

    /** Registra un intento y devuelve false si ya se supero el limite. */
    public boolean permitir(String clave) {

        long ahora = System.currentTimeMillis();

        Deque<Long> marcas = intentos.computeIfAbsent(
                clave,
                k -> new ConcurrentLinkedDeque<>());

        synchronized (marcas) {

            while (!marcas.isEmpty() && ahora - marcas.peekFirst() > ventana.toMillis()) {
                marcas.pollFirst();
            }

            if (marcas.size() >= maximo) {
                return false;
            }

            marcas.addLast(ahora);
            return true;
        }
    }

    public int getMaximo() {
        return maximo;
    }
}
