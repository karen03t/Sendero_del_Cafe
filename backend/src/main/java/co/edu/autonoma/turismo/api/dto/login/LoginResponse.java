package co.edu.autonoma.turismo.api.dto.login;

public record LoginResponse(
        String token,
        String tipo,
        String rol
) {
}