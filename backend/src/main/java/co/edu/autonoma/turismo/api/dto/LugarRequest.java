
package co.edu.autonoma.turismo.api.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

## DTO para la solicitud de creación o actualización de un lugar turístico.
public record LugarRequest(

    @NotBlank(message = "NOMBRE_OBLIGATORIO")
    @Size(max = 200)
    String nombre,

    @NotBlank(message = "DESCRIPCION_OBLIGATORIA")
    String descripcion,

    @NotBlank(message = "CATEGORIA_OBLIGATORIA")
    @Size(max = 100)
    String categoria,

    @NotBlank(message = "DIRECCION_OBLIGATORIA")
    @Size(max = 255)
    String direccion,

    @NotBlank(message = "MUNICIPIO_OBLIGATORIO")
    @Size(max = 100)
    String municipio,

    @NotNull(message = "LATITUD_OBLIGATORIA")
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    BigDecimal latitud,

    @NotNull(message = "LONGITUD_OBLIGATORIA")
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    BigDecimal longitud

) {}
