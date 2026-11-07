
package co.edu.autonoma.turismo.api;

import co.edu.autonoma.turismo.api.dto.Lugar.LugarRequest;
import co.edu.autonoma.turismo.api.dto.Lugar.LugarResponse;
import co.edu.autonoma.turismo.api.dto.Auditoria.AuditoriaResponse;

import co.edu.autonoma.turismo.service.Lugar.LugarService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/lugares")
public class LugarController {

    private final LugarService lugarService;

    public LugarController(LugarService lugarService) {
        this.lugarService = lugarService;
    }

    // =====================================================
    // RF-02: EXPLORAR Y CONSULTAR SITIOS DE INTERES
    // =====================================================

    // Consultar y filtrar exclusivamente lugares activos.
    @GetMapping
    public List<LugarResponse> listar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String municipio) {

        return lugarService.buscarActivos(
            nombre,
            categoria,
            municipio
        );
    }

    // Consultar el detalle de un lugar activo.
    @GetMapping("/{id}")
    public LugarResponse obtener(@PathVariable String id) {
        return lugarService.obtenerActivo(id);
    }

    // =====================================================
    // RF-05: ADMINISTRACION DE LUGARES
    // =====================================================

    // CA-05: registrar lugar.
    @PostMapping
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<LugarResponse> registrar(
            @Valid @RequestBody LugarRequest request,
            Authentication authentication) {

        LugarResponse respuesta = lugarService.registrar(
            request,
            authentication.getName()
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(respuesta);
    }

    // RN-07: editar lugar.
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<LugarResponse> actualizar(
            @PathVariable String id,
            @Valid @RequestBody LugarRequest request,
            Authentication authentication) {

        LugarResponse respuesta = lugarService.actualizar(
            id,
            request,
            authentication.getName()
        );

        return ResponseEntity.ok(respuesta);
    }

    // CA-08: inactivar lugar.
    @PatchMapping("/{id}/inactivar")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<Map<String, String>> inactivar(
            @PathVariable String id,
            Authentication authentication) {

        lugarService.inactivar(
            id,
            authentication.getName()
        );

        return ResponseEntity.ok(
            Map.of(
                "codigo", "LUGAR_INACTIVADO",
                "mensaje", "El lugar fue inactivado correctamente"
            )
        );
    }

    // D-04: consultar auditoría.
    @GetMapping("/{id}/auditoria")
    @PreAuthorize("hasRole('OPERADOR')")
    public List<AuditoriaResponse> auditoria(
            @PathVariable String id) {

        return lugarService.consultarAuditoria(id);
    }
}
