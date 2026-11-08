
package co.edu.autonoma.turismo.api.dto.lugar;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record LugarRequest(

    @NotBlank(message = "NOMBRE_OBLIGATORIO")
    @Size(max = 200, message = "NOMBRE_MAXIMO_200")
    String nombre,

    @NotBlank(message = "DESCRIPCION_OBLIGATORIA")
    @Size(max = 5000, message = "DESCRIPCION_MAXIMO_5000")
    String descripcion,

    @NotBlank(message = "CATEGORIA_OBLIGATORIA")
    @Size(max = 100, message = "CATEGORIA_MAXIMO_100")
    String categoria,

    @NotBlank(message = "DIRECCION_OBLIGATORIA")
    @Size(max = 255, message = "DIRECCION_MAXIMO_255")
    String direccion,

    @NotBlank(message = "MUNICIPIO_OBLIGATORIO")
    @Size(max = 100, message = "MUNICIPIO_MAXIMO_100")
    String municipio,

    @NotNull(message = "LATITUD_OBLIGATORIA")
    @DecimalMin(value = "-90.0", message = "LATITUD_MINIMA")
    @DecimalMax(value = "90.0", message = "LATITUD_MAXIMA")
    @Digits(integer = 3, fraction = 7, message = "LATITUD_PRECISION_INVALIDA")
    BigDecimal latitud,

    @NotNull(message = "LONGITUD_OBLIGATORIA")
    @DecimalMin(value = "-180.0", message = "LONGITUD_MINIMA")
    @DecimalMax(value = "180.0", message = "LONGITUD_MAXIMA")
    @Digits(integer = 3, fraction = 7, message = "LONGITUD_PRECISION_INVALIDA")
    BigDecimal longitud

) {}
