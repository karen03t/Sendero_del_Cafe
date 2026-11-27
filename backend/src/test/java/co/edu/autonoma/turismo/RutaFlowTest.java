package co.edu.autonoma.turismo;

import co.edu.autonoma.turismo.domain.Lugar;
import co.edu.autonoma.turismo.domain.Operador;
import co.edu.autonoma.turismo.repository.LugarRepository;
import co.edu.autonoma.turismo.repository.OperadorRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas de integracion de RF-03 (rutas), RF-06 (inactivar/recuperar) y
 * RF-04 (sincronizacion). Necesita MySQL real; se salta sin DB_PASS.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnabledIfEnvironmentVariable(named = "DB_PASS", matches = ".+")
class RutaFlowTest {

    private static final String CORREO = "turista@rutas.co";
    private static final String CLAVE = "Turista2026*Segura";
    private static final String CORREO_OPERADOR = "operador@rutas.co";
    private static final String CLAVE_OPERADOR = "Operador2026*Segura";

    @Value("${local.server.port}")
    private int puerto;

    @Autowired private JdbcTemplate jdbc;
    @Autowired private OperadorRepository operadores;
    @Autowired private LugarRepository lugares;
    @Autowired private PasswordEncoder encoder;

    private RestClient cliente;
    private String token;
    private String tokenOperador;
    private String calleReal;
    private String mirador;
    private String finca;
    private String plaza;

    @BeforeEach
    void preparar() {

        cliente = RestClient.builder()
                .baseUrl("http://127.0.0.1:" + puerto)
                .defaultStatusHandler(HttpStatusCode::isError, (req, res) -> { })
                .build();

        jdbc.execute("DELETE FROM ruta_guardada");
        jdbc.execute("DELETE FROM ruta_lugar");
        jdbc.execute("DELETE FROM ruta");
        jdbc.execute("DELETE FROM auditoria_lugar");
        jdbc.execute("DELETE FROM lugar");
        jdbc.execute("DELETE FROM turista_interes");
        jdbc.execute("DELETE FROM turista");
        jdbc.execute("DELETE FROM operador_municipal");

        Operador operador = operadores.save(
                new Operador("Operador Rutas", CORREO_OPERADOR, encoder.encode(CLAVE_OPERADOR)));

        calleReal = crearLugar(operador, "Calle Real", "local",
                new BigDecimal("4.6370000"), new BigDecimal("-75.5710000"));
        mirador = crearLugar(operador, "Mirador Alto de la Cruz", "ecológico",
                new BigDecimal("4.6390000"), new BigDecimal("-75.5690000"));
        finca = crearLugar(operador, "Finca Cafetera", "gastronómico",
                new BigDecimal("4.6410000"), new BigDecimal("-75.5740000"));
        plaza = crearLugar(operador, "Plaza de Bolívar", "local",
                new BigDecimal("4.6350000"), new BigDecimal("-75.5760000"));

        registrarTurista();
        token = login(CORREO, CLAVE);
        tokenOperador = login(CORREO_OPERADOR, CLAVE_OPERADOR);
    }

    @Test
    void propuestaRespetaTiempoYOrdenaConsecutivo() {

        ResponseEntity<Map<String, Object>> respuesta = intercambiar(
                "/api/v1/rutas/propuesta",
                HttpMethod.POST,
                Map.of("municipio", "Salento",
                        "intereses", List.of("local"),
                        "tiempoDisponibleMin", 240),
                token);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<Map<String, Object>> paradas = paradas(respuesta.getBody());

        assertThat(paradas).hasSize(2);
        assertThat(paradas.get(0).get("orden")).isEqualTo(1);
        assertThat(paradas.get(1).get("orden")).isEqualTo(2);
        assertThat(((Number) respuesta.getBody().get("duracionMin")).intValue())
                .isLessThanOrEqualTo(240);
    }

    @Test
    void guardarListarEditarYConsultar() {

        ResponseEntity<Map<String, Object>> creada = intercambiar(
                "/api/v1/rutas",
                HttpMethod.POST,
                Map.of("nombre", "Café y cultura",
                        "paradas", List.of(calleReal, mirador, finca)),
                token);

        assertThat(creada.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(creada.getBody().get("estado")).isEqualTo("ACTIVA");
        assertThat(paradas(creada.getBody())).hasSize(3);

        String id = (String) creada.getBody().get("id");

        ResponseEntity<List<Map<String, Object>>> lista = cliente.get()
                .uri("/api/v1/rutas")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {});

        assertThat(lista.getBody()).hasSize(1);

        // Reordenar y quitar una parada.
        ResponseEntity<Map<String, Object>> editada = intercambiar(
                "/api/v1/rutas/" + id,
                HttpMethod.PUT,
                Map.of("nombre", "Café y plaza",
                        "paradas", List.of(plaza, calleReal)),
                token);

        assertThat(editada.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(editada.getBody().get("nombre")).isEqualTo("Café y plaza");

        List<Map<String, Object>> nuevas = paradas(editada.getBody());
        assertThat(nuevas).hasSize(2);
        assertThat(nuevas.get(0).get("lugarId")).isEqualTo(plaza);
        assertThat(nuevas.get(1).get("lugarId")).isEqualTo(calleReal);
    }

    @Test
    void inactivarOcultaYRecuperarDevuelve() {

        String id = guardarRuta(List.of(calleReal, mirador));

        ResponseEntity<Map<String, Object>> eliminada = intercambiar(
                "/api/v1/rutas/" + id, HttpMethod.DELETE, null, token);

        assertThat(eliminada.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(eliminada.getBody().get("codigo")).isEqualTo("RUTA_INACTIVADA");

        assertThat(listar("/api/v1/rutas")).isEmpty();
        assertThat(listar("/api/v1/rutas/inactivas")).hasSize(1);

        ResponseEntity<Map<String, Object>> recuperada = intercambiar(
                "/api/v1/rutas/" + id + "/reactivar", HttpMethod.PATCH, null, token);

        assertThat(recuperada.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(recuperada.getBody().get("estado")).isEqualTo("ACTIVA");
        assertThat(listar("/api/v1/rutas")).hasSize(1);
    }

    @Test
    void sincronizarDevuelveCambiosYeliminadas() {

        String id = guardarRuta(List.of(finca, plaza));

        String desde = LocalDateTime.now().minusMinutes(5).withNano(0).toString();

        ResponseEntity<Map<String, Object>> sincronizacion = cliente.get()
                .uri(uri -> uri.path("/api/v1/rutas/sincronizar")
                        .queryParam("desde", desde).build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {});

        assertThat(sincronizacion.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<Map<String, Object>> rutas =
                (List<Map<String, Object>>) sincronizacion.getBody().get("rutas");

        assertThat(rutas).extracting(r -> r.get("id")).contains(id);

        intercambiar("/api/v1/rutas/" + id, HttpMethod.DELETE, null, token);

        ResponseEntity<Map<String, Object>> despues = cliente.get()
                .uri(uri -> uri.path("/api/v1/rutas/sincronizar")
                        .queryParam("desde", desde).build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {});

        assertThat((List<String>) despues.getBody().get("eliminadas")).contains(id);
    }

    @Test
    void rechazaPropuestaFueraDelEjeCafetero() {

        ResponseEntity<Map<String, Object>> respuesta = intercambiar(
                "/api/v1/rutas/propuesta",
                HttpMethod.POST,
                Map.of("municipio", "Medellín",
                        "tiempoDisponibleMin", 120),
                token);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(respuesta.getBody().get("codigo")).isEqualTo("MUNICIPIO_FUERA_COBERTURA");
    }

    @Test
    void operadorNoPuedeUsarRutasDeTurista() {

        ResponseEntity<Map<String, Object>> respuesta = cliente.get()
                .uri("/api/v1/rutas")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenOperador)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {});

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void rechazaGuardarUnLugarInactivo() {

        Lugar inactivo = lugares.findById(mirador).orElseThrow();
        inactivo.inactivar();
        lugares.save(inactivo);

        ResponseEntity<Map<String, Object>> respuesta = intercambiar(
                "/api/v1/rutas",
                HttpMethod.POST,
                Map.of("nombre", "Ruta con inactivo",
                        "paradas", List.of(calleReal, mirador)),
                token);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(respuesta.getBody().get("codigo")).isEqualTo("LUGAR_INACTIVO");
    }

    // =====================================================
    // AUXILIARES
    // =====================================================

    private String crearLugar(
            Operador operador,
            String nombre,
            String categoria,
            BigDecimal latitud,
            BigDecimal longitud) {

        Lugar lugar = new Lugar(
                UUID.randomUUID().toString(),
                nombre,
                "Descripción de " + nombre,
                categoria,
                "Dirección " + nombre,
                "Salento",
                latitud,
                longitud,
                operador.getId());

        return lugares.save(lugar).getId();
    }

    private void registrarTurista() {

        ResponseEntity<Map<String, Object>> registro =
                (ResponseEntity) cliente.post()
                        .uri("/api/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Map.of(
                                "nombreCompleto", "Turista de Prueba",
                                "correo", CORREO,
                                "password", CLAVE,
                                "intereses", List.of(1),
                                "aceptaTratamientoDatos", true))
                        .retrieve()
                        .toEntity(Map.class);

        assertThat(registro.getStatusCode().is2xxSuccessful()).isTrue();
    }

    private String login(String correo, String clave) {

        ResponseEntity<Map<String, Object>> respuesta = (ResponseEntity) cliente.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("correo", correo, "password", clave))
                .retrieve()
                .toEntity(Map.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        return (String) respuesta.getBody().get("token");
    }

    private String guardarRuta(List<String> paradas) {

        ResponseEntity<Map<String, Object>> creada = intercambiar(
                "/api/v1/rutas",
                HttpMethod.POST,
                Map.of("nombre", "Ruta de prueba", "paradas", paradas),
                token);

        assertThat(creada.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return (String) creada.getBody().get("id");
    }

    private List<Map<String, Object>> listar(String ruta) {

        ResponseEntity<List<Map<String, Object>>> respuesta = cliente.get()
                .uri(ruta)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {});

        return respuesta.getBody();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> paradas(Map<String, Object> ruta) {
        return (List<Map<String, Object>>) ruta.get("paradas");
    }

    private ResponseEntity<Map<String, Object>> intercambiar(
            String ruta,
            HttpMethod metodo,
            Object cuerpo,
            String bearer) {

        RestClient.RequestBodySpec peticion = cliente.method(metodo)
                .uri(ruta)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer);

        if (cuerpo != null) {
            peticion.contentType(MediaType.APPLICATION_JSON).body(cuerpo);
        }

        return peticion.retrieve()
                .toEntity(new ParameterizedTypeReference<>() {});
    }
}
