package co.edu.autonoma.turismo.service.ruta;

import co.edu.autonoma.turismo.api.dto.ruta.RutaDtos.*;
import co.edu.autonoma.turismo.domain.CoberturaEjeCafetero;
import co.edu.autonoma.turismo.domain.Lugar;
import co.edu.autonoma.turismo.domain.Ruta;
import co.edu.autonoma.turismo.domain.RutaGuardada;
import co.edu.autonoma.turismo.domain.RutaLugar;
import co.edu.autonoma.turismo.domain.Turista;
import co.edu.autonoma.turismo.exception.ApiException;
import co.edu.autonoma.turismo.repository.LugarRepository;
import co.edu.autonoma.turismo.repository.RutaGuardadaRepository;
import co.edu.autonoma.turismo.repository.RutaRepository;
import co.edu.autonoma.turismo.repository.TuristaRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class RutaService {

    /** Velocidad de caminata asumida para estimar desplazamientos. */
    private static final double VELOCIDAD_KMH = 4.5;

    /** Tiempo de visita por parada. */
    private static final int VISITA_MIN = 30;

    private static final int MAX_PARADAS = 12;

    private static final double RADIO_TIERRA_KM = 6371.0;

    private final LugarRepository lugares;
    private final RutaRepository rutas;
    private final RutaGuardadaRepository guardadas;
    private final TuristaRepository turistas;

    public RutaService(
            LugarRepository lugares,
            RutaRepository rutas,
            RutaGuardadaRepository guardadas,
            TuristaRepository turistas) {

        this.lugares = lugares;
        this.rutas = rutas;
        this.guardadas = guardadas;
        this.turistas = turistas;
    }

    // =====================================================
    // RF-03: GENERAR PROPUESTA
    // =====================================================

    /**
     * Genera una propuesta sin persistirla. La ubicacion del turista solo se
     * usa para calcular (RN-02) y no se almacena en ningun momento.
     */
    @Transactional(readOnly = true)
    public PropuestaResponse proponer(PropuestaRequest req) {

        String municipio = CoberturaEjeCafetero.canonico(req.municipio().trim())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "MUNICIPIO_FUERA_COBERTURA",
                        "El municipio debe pertenecer al Eje Cafetero (Quindío, Risaralda o Caldas)"));

        List<Lugar> candidatos = filtrarPorIntereses(
                lugares.buscarActivos(null, null, municipio),
                req.intereses());

        if (candidatos.isEmpty()) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    "SIN_LUGARES",
                    "No hay lugares activos que cumplan el municipio y los intereses");
        }

        GrafoRutas.Punto inicio = req.origen() != null
                ? new GrafoRutas.Punto(req.origen().lat(), req.origen().lng())
                : centroide(candidatos);

        // Grafo disperso (4 vecinos) + Dijkstra: las distancias entre paradas
        // pueden pasar por otros lugares en vez de ser linea recta.
        List<GrafoRutas.Punto> nodos = new ArrayList<>();
        for (Lugar candidato : candidatos) {
            nodos.add(punto(candidato));
        }
        int indiceOrigen = nodos.size();
        nodos.add(inicio);

        double[][] distancias = new GrafoRutas(nodos, 4).todasLasDistancias();

        List<Integer> seleccionados = seleccionarPorTiempo(
                candidatos,
                indiceOrigen,
                distancias,
                req.tiempoDisponibleMin());

        if (seleccionados.isEmpty()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "TIEMPO_INSUFICIENTE",
                    "El tiempo disponible no alcanza para visitar un lugar");
        }

        List<Integer> ordenados = mejorar2Opt(seleccionados, indiceOrigen, distancias);

        return construirPropuesta(
                municipio,
                candidatos,
                ordenados,
                indiceOrigen,
                distancias,
                req.origen() != null);
    }

    // =====================================================
    // RF-03: GUARDAR, CONSULTAR Y EDITAR
    // =====================================================

    @Transactional
    public RutaResponse guardar(String turistaId, GuardarRutaRequest req) {

        Turista turista = buscarTurista(turistaId);

        List<Lugar> seleccionados =
                resolverParadas(req.paradas(), req.municipio());

        Ruta ruta = new Ruta(
                UUID.randomUUID().toString(),
                req.nombre().trim(),
                null,
                seleccionados.get(0).getMunicipio());

        aplicarParadas(ruta, seleccionados);

        rutas.save(ruta);

        RutaGuardada guardada = new RutaGuardada(
                turista,
                ruta,
                req.nombre().trim());

        guardadas.save(guardada);

        return respuesta(ruta, guardada);
    }

    @Transactional(readOnly = true)
    public List<RutaResponse> listar(String turistaId) {

        return guardadas
                .findByIdTuristaIdAndEstadoOrderByFechaGuardadoDesc(turistaId, "ACTIVA")
                .stream()
                .map(g -> respuesta(g.getRuta(), g))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RutaResponse> listarInactivas(String turistaId) {

        return guardadas
                .findByIdTuristaIdAndEstadoOrderByFechaGuardadoDesc(turistaId, "INACTIVA")
                .stream()
                .map(g -> respuesta(g.getRuta(), g))
                .toList();
    }

    @Transactional(readOnly = true)
    public RutaResponse obtener(String turistaId, String rutaId) {

        RutaGuardada guardada = buscarGuardada(turistaId, rutaId);

        return respuesta(guardada.getRuta(), guardada);
    }

    /** RF-03: anadir, quitar o reordenar paradas de una ruta guardada. */
    @Transactional
    public RutaResponse editar(String turistaId, String rutaId, EditarRutaRequest req) {

        RutaGuardada guardada = buscarGuardada(turistaId, rutaId);

        List<Lugar> seleccionados = resolverParadas(req.paradas(), null);

        Ruta ruta = guardada.getRuta();

        // Vaciar y vaciar en disco antes de reinsertar: si Hibernate intenta
        // insertar el nuevo orden antes de borrar el anterior, choca con la
        // PK (ruta_id, lugar_id) o con uq_ruta_orden.
        ruta.getParadas().clear();
        rutas.saveAndFlush(ruta);

        aplicarParadas(ruta, seleccionados);
        rutas.saveAndFlush(ruta);

        if (req.nombre() != null && !req.nombre().isBlank()) {
            ruta.renombrar(req.nombre().trim());
            guardada.renombrar(req.nombre().trim());
        }

        guardada.tocar();

        return respuesta(ruta, guardada);
    }

    // =====================================================
    // RF-06: INACTIVAR Y RECUPERAR (CA-03)
    // =====================================================

    @Transactional
    public RutaResponse inactivar(String turistaId, String rutaId) {

        RutaGuardada guardada = buscarGuardada(turistaId, rutaId);

        guardada.inactivar();

        return respuesta(guardada.getRuta(), guardada);
    }

    @Transactional
    public RutaResponse reactivar(String turistaId, String rutaId) {

        RutaGuardada guardada = buscarGuardada(turistaId, rutaId);

        guardada.reactivar();

        return respuesta(guardada.getRuta(), guardada);
    }

    // =====================================================
    // RF-04: SINCRONIZACION
    // =====================================================

    /**
     * Devuelve lo que cambio desde una marca de tiempo. El cliente guarda el
     * resultado localmente (RN-04) y lo aplica cuando recupera conexion.
     */
    @Transactional(readOnly = true)
    public SincronizacionResponse sincronizar(String turistaId, LocalDateTime desde) {

        LocalDateTime referencia = desde != null
                ? desde
                : LocalDateTime.now().minusDays(30);

        List<RutaGuardada> cambios = guardadas
                .findByIdTuristaIdAndFechaActualizacionAfterOrderByFechaActualizacionAsc(
                        turistaId,
                        referencia);

        List<RutaResponse> rutas = cambios.stream()
                .map(g -> respuesta(g.getRuta(), g))
                .toList();

        List<String> eliminadas = cambios.stream()
                .filter(g -> "INACTIVA".equals(g.getEstado()))
                .map(g -> g.getRuta().getId())
                .toList();

        return new SincronizacionResponse(
                referencia,
                LocalDateTime.now(),
                rutas,
                eliminadas);
    }

    // =====================================================
    // CALCULO DE LA RUTA
    // =====================================================

    private List<Lugar> filtrarPorIntereses(List<Lugar> candidatos, List<String> intereses) {

        if (intereses == null || intereses.isEmpty()) {
            return candidatos;
        }

        Set<String> filtros = new HashSet<>();

        for (String interes : intereses) {
            if (interes != null && !interes.isBlank()) {
                filtros.add(interes.trim().toLowerCase(Locale.ROOT));
            }
        }

        if (filtros.isEmpty()) {
            return candidatos;
        }

        return candidatos.stream()
                .filter(l -> filtros.contains(l.getCategoria().toLowerCase(Locale.ROOT)))
                .toList();
    }

    /**
     * Vecino mas cercano con presupuesto usando las distancias de Dijkstra:
     * agrega la parada mas proxima que quepa en el tiempo disponible.
     */
    private List<Integer> seleccionarPorTiempo(
            List<Lugar> candidatos,
            int indiceOrigen,
            double[][] distancias,
            int tiempoDisponibleMin) {

        List<Integer> seleccionados = new ArrayList<>();
        Set<Integer> usados = new HashSet<>();

        int actual = indiceOrigen;
        int tiempoAcumulado = 0;

        while (seleccionados.size() < MAX_PARADAS) {

            Integer mejor = null;
            double mejorDistancia = Double.MAX_VALUE;
            int mejorTiempo = 0;

            for (int i = 0; i < candidatos.size(); i++) {

                if (usados.contains(i)) {
                    continue;
                }

                double distancia = distancias[actual][i];

                if (Double.isInfinite(distancia)) {
                    continue;
                }

                int minutos = (int) Math.round(distancia / VELOCIDAD_KMH * 60);

                if (tiempoAcumulado + minutos + VISITA_MIN > tiempoDisponibleMin) {
                    continue;
                }

                if (distancia < mejorDistancia) {
                    mejorDistancia = distancia;
                    mejorTiempo = minutos;
                    mejor = i;
                }
            }

            if (mejor == null) {
                break;
            }

            seleccionados.add(mejor);
            usados.add(mejor);
            actual = mejor;
            tiempoAcumulado += mejorTiempo + VISITA_MIN;
        }

        return seleccionados;
    }

    /** Mejora 2-opt sobre el orden elegido. n <= 12, coste despreciable. */
    private List<Integer> mejorar2Opt(
            List<Integer> paradas,
            int indiceOrigen,
            double[][] distancias) {

        if (paradas.size() < 3) {
            return new ArrayList<>(paradas);
        }

        List<Integer> mejor = new ArrayList<>(paradas);
        boolean huboMejora = true;
        int iteraciones = 0;

        while (huboMejora && iteraciones++ < 50) {

            huboMejora = false;
            double costoActual = costo(mejor, indiceOrigen, distancias);

            for (int i = 1; i < mejor.size() - 1; i++) {
                for (int j = i + 1; j < mejor.size(); j++) {

                    List<Integer> candidata = new ArrayList<>(mejor);
                    Collections.reverse(candidata.subList(i, j + 1));

                    double costoCandidato = costo(candidata, indiceOrigen, distancias);

                    if (costoCandidato + 1e-9 < costoActual) {
                        mejor = candidata;
                        huboMejora = true;
                        costoActual = costoCandidato;
                    }
                }
            }
        }

        return mejor;
    }

    private double costo(
            List<Integer> paradas,
            int indiceOrigen,
            double[][] distancias) {

        double total = 0;
        int anterior = indiceOrigen;

        for (int parada : paradas) {
            total += distancias[anterior][parada];
            anterior = parada;
        }

        return total;
    }

    private PropuestaResponse construirPropuesta(
            String municipio,
            List<Lugar> candidatos,
            List<Integer> paradas,
            int indiceOrigen,
            double[][] distancias,
            boolean conOrigen) {

        List<ParadaResponse> respuestas = new ArrayList<>();

        int anterior = indiceOrigen;
        double distanciaTotal = 0;
        int duracionTotal = 0;

        for (int i = 0; i < paradas.size(); i++) {

            int indice = paradas.get(i);
            Lugar lugar = candidatos.get(indice);

            double tramo = distancias[anterior][indice];
            int minutosTramo = (int) Math.round(tramo / VELOCIDAD_KMH * 60);

            distanciaTotal += tramo;
            duracionTotal += minutosTramo + VISITA_MIN;

            int desdeAnterior = (i == 0 && !conOrigen) ? 0 : minutosTramo;

            respuestas.add(new ParadaResponse(
                    i + 1,
                    lugar.getId(),
                    lugar.getNombre(),
                    lugar.getCategoria(),
                    lugar.getLatitud().doubleValue(),
                    lugar.getLongitud().doubleValue(),
                    desdeAnterior));

            anterior = indice;
        }

        return new PropuestaResponse(
                municipio,
                redondear(distanciaTotal),
                duracionTotal,
                respuestas);
    }

    // =====================================================
    // PERSISTENCIA DE PARADAS
    // =====================================================

    private void aplicarParadas(Ruta ruta, List<Lugar> seleccionados) {

        List<RutaLugar> paradas = new ArrayList<>();

        List<GrafoRutas.Punto> nodos = new ArrayList<>();
        for (Lugar lugar : seleccionados) {
            nodos.add(punto(lugar));
        }

        double[][] distancias = new GrafoRutas(nodos, 4).todasLasDistancias();

        double distanciaTotal = 0;
        int duracionTotal = 0;

        for (int i = 0; i < seleccionados.size(); i++) {

            int minutosTramo = 0;

            if (i > 0) {
                double tramo = distancias[i - 1][i];
                distanciaTotal += tramo;
                minutosTramo = (int) Math.round(tramo / VELOCIDAD_KMH * 60);
            }

            duracionTotal += minutosTramo + VISITA_MIN;

            paradas.add(new RutaLugar(seleccionados.get(i), minutosTramo));
        }

        ruta.reemplazarParadas(paradas);

        ruta.actualizarMetricas(
                BigDecimal.valueOf(redondear(distanciaTotal)).setScale(2, RoundingMode.HALF_UP),
                duracionTotal);
    }

    private List<Lugar> resolverParadas(List<String> ids, String municipioEsperado) {

        if (ids == null || ids.isEmpty()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "PARADAS_VACIAS",
                    "La ruta debe tener al menos una parada");
        }

        if (new HashSet<>(ids).size() != ids.size()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "PARADAS_DUPLICADAS",
                    "No se puede visitar el mismo lugar dos veces en una ruta");
        }

        List<Lugar> resultado = new ArrayList<>();

        for (String id : ids) {

            Lugar lugar = lugares.findById(id)
                    .orElseThrow(() -> new ApiException(
                            HttpStatus.NOT_FOUND,
                            "LUGAR_NO_EXISTE",
                            "El lugar " + id + " no existe"));

            if (!"ACTIVO".equals(lugar.getEstado())) {
                throw new ApiException(
                        HttpStatus.CONFLICT,
                        "LUGAR_INACTIVO",
                        "El lugar " + lugar.getNombre() + " esta inactivo");
            }

            if (!resultado.isEmpty() && !mismoMunicipio(resultado.get(0), lugar)) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "MUNICIPIO_INCONSISTENTE",
                        "Todos los lugares de una ruta deben pertenecer al mismo municipio");
            }

            resultado.add(lugar);
        }

        if (municipioEsperado != null
                && !municipioEsperado.isBlank()
                && !resultado.get(0).getMunicipio().equalsIgnoreCase(municipioEsperado.trim())) {

            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "MUNICIPIO_INCONSISTENTE",
                    "El municipio de la ruta no coincide con el de sus lugares");
        }

        return resultado;
    }

    private boolean mismoMunicipio(Lugar a, Lugar b) {
        return a.getMunicipio().equalsIgnoreCase(b.getMunicipio());
    }

    private RutaGuardada buscarGuardada(String turistaId, String rutaId) {

        return guardadas.findByIdTuristaIdAndIdRutaId(turistaId, rutaId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "RUTA_NO_ENCONTRADA",
                        "La ruta no existe o no pertenece al turista"));
    }

    private Turista buscarTurista(String turistaId) {

        return turistas.findById(turistaId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.UNAUTHORIZED,
                        "TURISTA_NO_AUTENTICADO",
                        "No se pudo identificar al turista"));
    }

    private RutaResponse respuesta(Ruta ruta, RutaGuardada guardada) {

        List<ParadaResponse> paradas = ruta.getParadas().stream()
                .map(p -> new ParadaResponse(
                        p.getOrden(),
                        p.getLugar().getId(),
                        p.getLugar().getNombre(),
                        p.getLugar().getCategoria(),
                        p.getLugar().getLatitud().doubleValue(),
                        p.getLugar().getLongitud().doubleValue(),
                        p.getTiempoEstimadoMin() == null ? 0 : p.getTiempoEstimadoMin()))
                .toList();

        String nombre = guardada.getNombreCustom() != null
                ? guardada.getNombreCustom()
                : ruta.getNombre();

        return new RutaResponse(
                ruta.getId(),
                nombre,
                ruta.getMunicipio(),
                ruta.getDistanciaKm() == null ? 0 : ruta.getDistanciaKm().doubleValue(),
                ruta.getDuracionMin() == null ? 0 : ruta.getDuracionMin(),
                guardada.getEstado(),
                guardada.getFechaGuardado(),
                paradas);
    }

    // =====================================================
    // GEOGRAFIA
    // =====================================================

    private GrafoRutas.Punto punto(Lugar lugar) {
        return new GrafoRutas.Punto(
                lugar.getLatitud().doubleValue(),
                lugar.getLongitud().doubleValue());
    }

    private GrafoRutas.Punto centroide(List<Lugar> lugares) {

        double lat = 0;
        double lng = 0;

        for (Lugar lugar : lugares) {
            lat += lugar.getLatitud().doubleValue();
            lng += lugar.getLongitud().doubleValue();
        }

        return new GrafoRutas.Punto(lat / lugares.size(), lng / lugares.size());
    }

    private double distanciaKm(GrafoRutas.Punto a, GrafoRutas.Punto b) {

        double dLat = Math.toRadians(b.lat() - a.lat());
        double dLng = Math.toRadians(b.lng() - a.lng());

        double ha = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(a.lat()))
                * Math.cos(Math.toRadians(b.lat()))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        return 2 * RADIO_TIERRA_KM * Math.asin(Math.sqrt(ha));
    }

    private double redondear(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
