package co.edu.autonoma.turismo;

import co.edu.autonoma.turismo.domain.Operador;
import co.edu.autonoma.turismo.repository.OperadorRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba de integracion del modulo administrativo (RF-05) y de la consulta
 * publica (RF-02). Necesita una base MySQL real: se salta si no hay DB_PASS,
 * de modo que `mvn test` no falla en una maquina sin base.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnabledIfEnvironmentVariable(named = "DB_PASS", matches = ".+")
class LugarFlowTest {

    private static final String CORREO = "operador@test.co";
    private static final String CLAVE = "Operador2026*Segura";

    @Value("${local.server.port}")
    private int puerto;

    @Autowired private JdbcTemplate jdbc;
    @Autowired private OperadorRepository operadores;
    @Autowired private PasswordEncoder encoder;

    private RestClient cliente;
    private String tokenOperador;

    @BeforeEach
    void preparar() {
        cliente = RestClient.builder()
                .baseUrl("http://127.0.0.1:" + puerto)
                .defaultStatusHandler(HttpStatusCode::isError, (req, res) -> { })
                .build();

        jdbc.execute("DELETE FROM auditoria_lugar");
        jdbc.execute("DELETE FROM lugar");
        jdbc.execute("DELETE FROM turista_interes");
        jdbc.execute("DELETE FROM turista");
        jdbc.execute("DELETE FROM operador_municipal");

        operadores.save(new Operador("Operador de Prueba", CORREO, encoder.encode(CLAVE)));

        ResponseEntity<Map<String, Object>> login = cliente.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("correo", CORREO, "password", CLAVE))
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {});

        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
        tokenOperador = (String) login.getBody().get("token");
        assertThat(tokenOperador).isNotBlank();
    }

    @Test
    void flujoCompletoCrearEditarInactivarYAuditar() {
        // CA-05: crear
        ResponseEntity<Map<String, Object>> creado = intercambiar(
                "/api/v1/lugares", HttpMethod.POST, lugar("Mirador de Salento"), tokenOperador);
        assertThat(creado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String id = (String) creado.getBody().get("id");
        assertThat(id).isNotBlank();

        // RF-02: aparece en el listado publico
        assertThat(filtrar("")).hasSize(1);

        // CA-06: editar
        ResponseEntity<Map<String, Object>> editado = intercambiar(
                "/api/v1/lugares/" + id, HttpMethod.PUT, lugar("Mirador de Salento (renovado)"), tokenOperador);
        assertThat(editado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(editado.getBody().get("nombre")).isEqualTo("Mirador de Salento (renovado)");

        // RF-02: filtros exactos por categoria y municipio
        assertThat(filtrar("?categoria=Ecológico")).hasSize(1);
        assertThat(filtrar("?categoria=Gastronómico")).isEmpty();
        assertThat(filtrar("?municipio=Salento")).hasSize(1);

        // Auditoria: dos acciones registradas (la mas reciente primero)
        assertThat(auditoria(id)).containsExactly("EDITAR", "CREAR");

        // CA-08: inactivar
        ResponseEntity<Map<String, Object>> inactivado = intercambiar(
                "/api/v1/lugares/" + id + "/inactivar", HttpMethod.PATCH, null, tokenOperador);
        assertThat(inactivado.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Ya no aparece en listados ni en detalle publico
        assertThat(filtrar("")).isEmpty();
        ResponseEntity<Map<String, Object>> detalle = cliente.get()
                .uri("/api/v1/lugares/" + id)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {});
        assertThat(detalle.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // Auditoria: tres acciones (la mas reciente primero)
        assertThat(auditoria(id)).containsExactly("INACTIVAR", "EDITAR", "CREAR");
    }

    @Test
    void rechazaMunicipiosFueraDelEjeCafetero() {

        Map<String, Object> bogota = new HashMap<>(lugar("Lugar en Bogotá"));
        bogota.put("municipio", "Bogotá");

        ResponseEntity<Map<String, Object>> intento = intercambiar(
                "/api/v1/lugares", HttpMethod.POST, bogota, tokenOperador);

        assertThat(intento.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(intento.getBody().get("codigo")).isEqualTo("MUNICIPIO_FUERA_COBERTURA");

        // Actualizar hacia un municipio fuera de cobertura tampoco se permite.
        Map<String, Object> crear = new HashMap<>(lugar("Mirador"));
        String id = (String) intercambiar("/api/v1/lugares", HttpMethod.POST, crear, tokenOperador)
                .getBody().get("id");

        Map<String, Object> medellin = new HashMap<>(lugar("Mirador"));
        medellin.put("municipio", "Medellín");

        ResponseEntity<Map<String, Object>> edicion = intercambiar(
                "/api/v1/lugares/" + id, HttpMethod.PUT, medellin, tokenOperador);

        assertThat(edicion.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(edicion.getBody().get("codigo")).isEqualTo("MUNICIPIO_FUERA_COBERTURA");
    }

    @Test
    void normalizaElNombreDelMunicipio() {

        Map<String, Object> sinTildes = new HashMap<>(lugar("Finca cafetera"));
        sinTildes.put("municipio", "salento");

        ResponseEntity<Map<String, Object>> creado = intercambiar(
                "/api/v1/lugares", HttpMethod.POST, sinTildes, tokenOperador);

        assertThat(creado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(creado.getBody().get("municipio")).isEqualTo("Salento");
    }

    @Test
    void unTuristaNoPuedeAdministrarLugares() {
        ResponseEntity<Map<String, Object>> registro = cliente.post()
                .uri("/api/v1/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("nombreCompleto", "Turista Prueba",
                        "correo", "turista@test.co",
                        "password", "Turista2026*Segura",
                        "intereses", List.of(1),
                        "proveedor", "LOCAL",
                        "aceptaTratamientoDatos", true))
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {});
        assertThat(registro.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        String tokenTurista = (String) registro.getBody().get("tokenSesion");
        assertThat(tokenTurista).isNotBlank();

        ResponseEntity<Map<String, Object>> intento = intercambiar(
                "/api/v1/lugares", HttpMethod.POST, lugar("Lugar"), tokenTurista);
        assertThat(intento.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void sinTokenNoSePuedeAdministrarLugares() {
        ResponseEntity<Map<String, Object>> intento = intercambiar(
                "/api/v1/lugares", HttpMethod.POST, lugar("Lugar"), null);

        assertThat(intento.getStatusCode().is4xxClientError()).isTrue();
        assertThat(intento.getStatusCode().value()).isIn(401, 403);
    }

    @Test
    void elRegistroPublicoDeOperadoresEstaBloqueado() {
        ResponseEntity<Map<String, Object>> intento = cliente.post()
                .uri("/api/v1/auth/registro/operador")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("nombreCompleto", "Intruso",
                        "correo", "intruso@test.co",
                        "password", "Intruso2026*"))
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {});

        assertThat(intento.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(operadores.count()).isEqualTo(1);
    }

    private ResponseEntity<Map<String, Object>> intercambiar(
            String ruta, HttpMethod metodo, Object cuerpo, String token) {

        RestClient.RequestBodySpec peticion = cliente.method(metodo)
                .uri(ruta)
                .contentType(MediaType.APPLICATION_JSON);

        if (token != null) {
            peticion = peticion.header("Authorization", "Bearer " + token);
        }
        if (cuerpo != null) {
            peticion = peticion.body(cuerpo);
        }

        return peticion.retrieve().toEntity(new ParameterizedTypeReference<>() {});
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> filtrar(String query) {

        String separador = query.contains("?") ? "&" : "?";

        ResponseEntity<Map<String, Object>> respuesta = cliente.get()
                .uri("/api/v1/lugares" + query + separador + "tamano=100")
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {});

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        return (List<Map<String, Object>>) respuesta.getBody().get("content");
    }

    private List<String> auditoria(String id) {
        ResponseEntity<List<Map<String, Object>>> respuesta = cliente.get()
                .uri("/api/v1/lugares/" + id + "/auditoria")
                .header("Authorization", "Bearer " + tokenOperador)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {});

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        return respuesta.getBody().stream()
                .map(a -> (String) a.get("accion"))
                .toList();
    }

    private Map<String, Object> lugar(String nombre) {
        return Map.of("nombre", nombre, "descripcion", "Vista del valle de Cocora",
                "categoria", "Ecológico", "direccion", "Cra 5 #4-12",
                "municipio", "Salento", "latitud", 4.6371, "longitud", -75.5706);
    }
}
