package co.edu.autonoma.turismo.api.dto.Login;

public record LoginResponse(
        String token,
        String tipo,
        String rol
) {
}