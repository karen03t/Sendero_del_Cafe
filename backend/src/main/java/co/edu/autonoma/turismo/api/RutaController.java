package co.edu.autonoma.turismo.api;

import co.edu.autonoma.turismo.api.dto.ruta.RutaDtos.*;
import co.edu.autonoma.turismo.service.ruta.RutaService;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/rutas")
@PreAuthorize("hasRole('TURISTA')")
public class RutaController {

    private final RutaService rutaService;

    public RutaController(RutaService rutaService) {
        this.rutaService = rutaService;
    }

    // =====================================================
    // RF-03: GENERAR Y GUARDAR
    // =====================================================

    @PostMapping("/propuesta")
    public PropuestaResponse proponer(@Valid @RequestBody PropuestaRequest request) {
        return rutaService.proponer(request);
    }

    @PostMapping
    public ResponseEntity<RutaResponse> guardar(
            @Valid @RequestBody GuardarRutaRequest request,
            Authentication authentication) {

        RutaResponse respuesta = rutaService.guardar(
                authentication.getName(),
                request);

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    // =====================================================
    // RF-03: CONSULTAR Y PERSONALIZAR
    // =====================================================

    @GetMapping
    public List<RutaResponse> listar(Authentication authentication) {
        return rutaService.listar(authentication.getName());
    }

    @GetMapping("/inactivas")
    public List<RutaResponse> listarInactivas(Authentication authentication) {
        return rutaService.listarInactivas(authentication.getName());
    }

    @GetMapping("/{id}")
    public RutaResponse obtener(
            @PathVariable String id,
            Authentication authentication) {

        return rutaService.obtener(authentication.getName(), id);
    }

    @PutMapping("/{id}")
    public RutaResponse editar(
            @PathVariable String id,
            @Valid @RequestBody EditarRutaRequest request,
            Authentication authentication) {

        return rutaService.editar(
                authentication.getName(),
                id,
                request);
    }

    // =====================================================
    // RF-06: INACTIVAR Y RECUPERAR (CA-03)
    // =====================================================

    @PatchMapping("/{id}/inactivar")
    public RutaResponse inactivar(
            @PathVariable String id,
            Authentication authentication) {

        return rutaService.inactivar(authentication.getName(), id);
    }

    @PatchMapping("/{id}/reactivar")
    public RutaResponse reactivar(
            @PathVariable String id,
            Authentication authentication) {

        return rutaService.reactivar(authentication.getName(), id);
    }

    /** CA-03: "eliminar" de la ruta la marca inactiva; no borra el registro. */
    @DeleteMapping("/{id}")
    public Map<String, String> eliminar(
            @PathVariable String id,
            Authentication authentication) {

        RutaResponse ruta = rutaService.inactivar(authentication.getName(), id);

        return Map.of(
                "codigo", "RUTA_INACTIVADA",
                "mensaje", "La ruta se marco como inactiva y puede recuperarse",
                "rutaId", ruta.id());
    }

    // =====================================================
    // RF-04: SINCRONIZACION
    // =====================================================

    @GetMapping("/sincronizar")
    public SincronizacionResponse sincronizar(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime desde,
            Authentication authentication) {

        return rutaService.sincronizar(authentication.getName(), desde);
    }
}
