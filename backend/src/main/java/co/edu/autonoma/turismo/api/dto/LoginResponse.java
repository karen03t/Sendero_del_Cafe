package co.edu.autonoma.turismo.api.dto;

public record LoginResponse(
        String token,
        String tipo,
        String rol
) {
}