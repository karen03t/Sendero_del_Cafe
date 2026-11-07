package co.edu.autonoma.turismo.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroOperadorRequest(

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 200, message = "El nombre no puede superar los 200 caracteres")
    String nombreCompleto,

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 255)
    String correo,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener mínimo 8 caracteres")
    String password

) {}