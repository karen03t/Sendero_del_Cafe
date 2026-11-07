
package co.edu.autonoma.turismo.service.Lugar;

import co.edu.autonoma.turismo.api.dto.Lugar.LugarRequest;
import co.edu.autonoma.turismo.api.dto.Lugar.LugarResponse;
import co.edu.autonoma.turismo.api.dto.Auditoria.AuditoriaResponse;

import co.edu.autonoma.turismo.domain.Lugar;
import co.edu.autonoma.turismo.domain.AuditoriaLugar;

import co.edu.autonoma.turismo.exception.ApiException;

import co.edu.autonoma.turismo.repository.LugarRepository;
import co.edu.autonoma.turismo.repository.AuditoriaLugarRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class LugarService {

    private final LugarRepository lugares;
    private final AuditoriaLugarRepository auditorias;
    private final ObjectMapper objectMapper;

    public LugarService(
            LugarRepository lugares,
            AuditoriaLugarRepository auditorias,
            ObjectMapper objectMapper) {

        this.lugares = lugares;
        this.auditorias = auditorias;
        this.objectMapper = objectMapper;
    }

    // =====================================================
    // RF-05: ADMINISTRACION DE LUGARES
    // =====================================================

    // CA-05: registrar un lugar turístico.
    @Transactional
    public LugarResponse registrar(
            LugarRequest req,
            String operadorId) {

        validarOperador(operadorId);

        String id = UUID.randomUUID().toString();

        Lugar lugar = new Lugar(
            id,
            req.nombre().trim(),
            req.descripcion().trim(),
            req.categoria().trim(),
            req.direccion().trim(),
            req.municipio().trim(),
            req.latitud(),
            req.longitud(),
            operadorId
        );

        lugares.save(lugar);

        Map<String, Object> datos = new LinkedHashMap<>();

        datos.put("nombre", lugar.getNombre());
        datos.put("descripcion", lugar.getDescripcion());
        datos.put("categoria", lugar.getCategoria());
        datos.put("direccion", lugar.getDireccion());
        datos.put("municipio", lugar.getMunicipio());
        datos.put("latitud", lugar.getLatitud());
        datos.put("longitud", lugar.getLongitud());
        datos.put("estado", lugar.getEstado());

        registrarAuditoria(id, operadorId, "CREACION", datos);

        return LugarResponse.desde(lugar);
    }

    // =====================================================
    // RF-02: EXPLORAR Y CONSULTAR SITIOS DE INTERES
    // =====================================================

    // RN-01: listar exclusivamente lugares activos.
    @Transactional(readOnly = true)
    public List<LugarResponse> listarActivos() {

        return lugares.findByEstadoOrderByNombreAsc("ACTIVO")
            .stream()
            .map(LugarResponse::desde)
            .toList();
    }

    // RF-02: buscar lugares por nombre, categoría y municipio.
    // Todos los filtros son opcionales y pueden combinarse.
    @Transactional(readOnly = true)
    public List<LugarResponse> buscarActivos(
            String nombre,
            String categoria,
            String municipio) {

        nombre = normalizarFiltro(nombre);
        categoria = normalizarFiltro(categoria);
        municipio = normalizarFiltro(municipio);

        return lugares.buscarActivos(
                nombre,
                categoria,
                municipio
            )
            .stream()
            .map(LugarResponse::desde)
            .toList();
    }

    // Consultar el detalle de un lugar activo.
    @Transactional(readOnly = true)
    public LugarResponse obtenerActivo(String id) {

        Lugar lugar = buscarActivo(id);

        return LugarResponse.desde(lugar);
    }

    // Normalizar filtros de búsqueda.
    private String normalizarFiltro(String valor) {

        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }

    // =====================================================
    // RF-05: ACTUALIZAR LUGARES
    // =====================================================

    // CA-05 / RN-07: actualizar un lugar.
    @Transactional
    public LugarResponse actualizar(
            String id,
            LugarRequest req,
            String operadorId) {

        validarOperador(operadorId);

        Lugar lugar = buscarActivo(id);

        Map<String, Object> cambios = new LinkedHashMap<>();

        comparar(cambios, "nombre",
            lugar.getNombre(), req.nombre().trim());

        comparar(cambios, "descripcion",
            lugar.getDescripcion(), req.descripcion().trim());

        comparar(cambios, "categoria",
            lugar.getCategoria(), req.categoria().trim());

        comparar(cambios, "direccion",
            lugar.getDireccion(), req.direccion().trim());

        comparar(cambios, "municipio",
            lugar.getMunicipio(), req.municipio().trim());

        comparar(cambios, "latitud",
            lugar.getLatitud(), req.latitud());

        comparar(cambios, "longitud",
            lugar.getLongitud(), req.longitud());

        if (cambios.isEmpty()) {
            throw new ApiException(
                HttpStatus.BAD_REQUEST,
                "SIN_CAMBIOS",
                "No se encontraron modificaciones en el lugar"
            );
        }

        lugar.actualizar(
            req.nombre().trim(),
            req.descripcion().trim(),
            req.categoria().trim(),
            req.direccion().trim(),
            req.municipio().trim(),
            req.latitud(),
            req.longitud()
        );

        registrarAuditoria(id, operadorId, "EDICION", cambios);

        return LugarResponse.desde(lugar);
    }

    // CA-08: inactivar un lugar.
    @Transactional
    public void inactivar(String id, String operadorId) {

        validarOperador(operadorId);

        Lugar lugar = buscarActivo(id);

        lugar.inactivar();

        Map<String, Object> cambios = new LinkedHashMap<>();

        cambios.put("estado_anterior", "ACTIVO");
        cambios.put("estado_nuevo", "INACTIVO");

        registrarAuditoria(
            id,
            operadorId,
            "INACTIVACION",
            cambios
        );
    }

    // =====================================================
    // RF-05: AUDITORIA
    // =====================================================

    // D-04: consultar la trazabilidad de un lugar.
    @Transactional(readOnly = true)
    public List<AuditoriaResponse> consultarAuditoria(
            String lugarId) {

        if (!lugares.existsById(lugarId)) {
            throw new ApiException(
                HttpStatus.NOT_FOUND,
                "LUGAR_NO_EXISTE",
                "El lugar turístico no existe"
            );
        }

        return auditorias
            .findByLugarIdOrderByFechaDesc(lugarId)
            .stream()
            .map(AuditoriaResponse::desde)
            .toList();
    }

    // =====================================================
    // METODOS AUXILIARES
    // =====================================================

    // Buscar un lugar exclusivamente si está activo.
    private Lugar buscarActivo(String id) {

        return lugares.findByIdAndEstado(id, "ACTIVO")
            .orElseThrow(() -> new ApiException(
                HttpStatus.NOT_FOUND,
                "LUGAR_NO_DISPONIBLE",
                "El lugar no existe o está inactivo"
            ));
    }

    // Validar la identificación del operador.
    private void validarOperador(String operadorId) {

        if (operadorId == null || operadorId.isBlank()) {
            throw new ApiException(
                HttpStatus.UNAUTHORIZED,
                "OPERADOR_NO_AUTENTICADO",
                "No se pudo identificar al operador"
            );
        }
    }

    // Comparar valores para registrar modificaciones.
    private void comparar(
            Map<String, Object> cambios,
            String campo,
            Object anterior,
            Object nuevo) {

        if (anterior == null ? nuevo != null :
                !anterior.equals(nuevo)) {

            Map<String, Object> valores = new LinkedHashMap<>();

            valores.put("anterior", anterior);
            valores.put("nuevo", nuevo);

            cambios.put(campo, valores);
        }
    }

    // Registrar auditoría en formato JSON.
    private void registrarAuditoria(
            String lugarId,
            String operadorId,
            String accion,
            Map<String, Object> datos) {

        try {
            String json = objectMapper.writeValueAsString(datos);

            auditorias.save(
                new AuditoriaLugar(
                    lugarId,
                    operadorId,
                    accion,
                    json
                )
            );

        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(
                "No fue posible generar la auditoría",
                ex
            );
        }
    }
}
