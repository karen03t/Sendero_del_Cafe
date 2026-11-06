package co.edu.autonoma.turismo.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * Contrato POST /api/v1/auth/registro (RF-01).
 */
public record RegistroRequest(
        @NotBlank(message = "NOMBRE_OBLIGATORIO") String nombreCompleto,
        @NotBlank(message = "CORREO_OBLIGATORIO") @Email(message = "CORREO_FORMATO_INVALIDO") String correo,
        List<Integer> intereses,
        String proveedor,
        String idTokenExterno,
        String password,
        boolean aceptaTratamientoDatos
) {
}
