package co.edu.autonoma.turismo.service;

import co.edu.autonoma.turismo.config.Rol;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    // Secreto exclusivo para pruebas: 64 bytes.
        private static final String SECRETO_PRUEBAS =
                "0123456789012345678901234567890123456789012345678901234567890123";

        @Test
        void generaTokenConTresSegmentos() {

        TokenService servicio = new TokenService(SECRETO_PRUEBAS, 60);

        String token = servicio.generar("uuid-123", "ana@ejemplo.com");

        assertNotNull(token);
        assertEquals(3, token.split("\\.").length);
        }

        @Test
        void rechazaSecretoCorto() {

        assertThrows(
                IllegalStateException.class,
                () -> new TokenService("corto", 60)
        );
        }

        @Test
        void tokenDeTuristaLlevaRolTurista() {

        TokenService servicio = new TokenService(SECRETO_PRUEBAS, 60);

        String token = servicio.generar("uuid-123", "ana@ejemplo.com");

        assertEquals("TURISTA",
                servicio.validarYObtenerClaims(token).get("rol", String.class));
        }

        @Test
        void tokenDeOperadorLlevaRolOperador() {

        TokenService servicio = new TokenService(SECRETO_PRUEBAS, 60);

        String token = servicio.generar("uuid-456", "op@ejemplo.com", Rol.OPERADOR);

        assertEquals("OPERADOR",
                servicio.validarYObtenerClaims(token).get("rol", String.class));
        }
}