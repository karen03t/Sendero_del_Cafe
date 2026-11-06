package co.edu.autonoma.turismo.api.dto;

import java.util.List;

public record RegistroResponse(
        String id,
        String nombreCompleto,
        String correo,
        List<Integer> intereses,
        String tokenSesion
) {
}
