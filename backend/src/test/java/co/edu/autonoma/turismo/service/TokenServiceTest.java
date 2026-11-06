package co.edu.autonoma.turismo.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    @Test
    void generaTokenConTresSegmentos() {
        TokenService servicio = new TokenService("secreto-de-pruebas-con-mas-de-32-bytes-ok", 60);
        String token = servicio.generar("uuid-123", "ana@ejemplo.com");
        assertNotNull(token);
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void rechazaSecretoCorto() {
        assertThrows(IllegalStateException.class, () -> new TokenService("corto", 60));
    }
}
