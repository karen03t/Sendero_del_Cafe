package co.edu.autonoma.turismo;

import co.edu.autonoma.turismo.security.LimiteIntentos;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LimiteIntentosTest {

    @Test
    void permiteHastaElMaximoYDespuesBloquea() {

        LimiteIntentos limite = new LimiteIntentos(3, 60);

        assertThat(limite.permitir("127.0.0.1|/api/v1/auth/login")).isTrue();
        assertThat(limite.permitir("127.0.0.1|/api/v1/auth/login")).isTrue();
        assertThat(limite.permitir("127.0.0.1|/api/v1/auth/login")).isTrue();

        assertThat(limite.permitir("127.0.0.1|/api/v1/auth/login")).isFalse();
    }

    @Test
    void clavesDistintasSeCuentanPorSeparado() {

        LimiteIntentos limite = new LimiteIntentos(2, 60);

        assertThat(limite.permitir("ip-a|/login")).isTrue();
        assertThat(limite.permitir("ip-a|/login")).isTrue();
        assertThat(limite.permitir("ip-a|/login")).isFalse();

        // Otro IP o otra ruta no arrastran el bloqueo.
        assertThat(limite.permitir("ip-b|/login")).isTrue();
        assertThat(limite.permitir("ip-a|/registro")).isTrue();
    }
}
