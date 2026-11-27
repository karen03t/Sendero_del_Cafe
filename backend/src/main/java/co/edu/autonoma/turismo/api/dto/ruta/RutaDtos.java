package co.edu.autonoma.turismo.api.dto.ruta;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

/** Contratos de RF-03, RF-04 y RF-06. */
public final class RutaDtos {

    private RutaDtos() {
    }

    public record Coordenada(
            @NotNull @Min(-90) @Max(90) Double lat,
            @NotNull @Min(-180) @Max(180) Double lng) {
    }

    public record PropuestaRequest(
            @NotBlank String municipio,
            List<String> intereses,
            @NotNull @Min(60) @Max(1440) Integer tiempoDisponibleMin,
            Coordenada origen) {
    }

    public record ParadaResponse(
            int orden,
            String lugarId,
            String nombre,
            String categoria,
            double lat,
            double lng,
            int minutosDesdeAnterior) {
    }

    public record PropuestaResponse(
            String municipio,
            double distanciaKm,
            int duracionMin,
            List<ParadaResponse> paradas) {
    }

    public record GuardarRutaRequest(
            @NotBlank @Size(max = 200) String nombre,
            String municipio,
            @NotEmpty @Size(max = 12) List<String> paradas) {
    }

    public record EditarRutaRequest(
            @Size(max = 200) String nombre,
            @NotEmpty @Size(max = 12) List<String> paradas) {
    }

    public record RutaResponse(
            String id,
            String nombre,
            String municipio,
            double distanciaKm,
            int duracionMin,
            String estado,
            LocalDateTime fechaGuardado,
            List<ParadaResponse> paradas) {
    }

    public record SincronizacionResponse(
            LocalDateTime desde,
            LocalDateTime servidor,
            List<RutaResponse> rutas,
            List<String> eliminadas) {
    }
}
