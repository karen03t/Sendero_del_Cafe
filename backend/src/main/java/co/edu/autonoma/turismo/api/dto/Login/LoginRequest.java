package co.edu.autonoma.turismo.api.dto.Login;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "CORREO_OBLIGATORIO")
        String correo,

        @NotBlank(message = "PASSWORD_OBLIGATORIA")
        String password

) {
}