
package co.edu.autonoma.turismo.api.dto;

import co.edu.autonoma.turismo.domain.Lugar;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LugarResponse(

    String id,
    String nombre,
    String descripcion,
    String categoria,
    String direccion,
    String municipio,
    BigDecimal latitud,
    BigDecimal longitud,
    String estado,
    LocalDateTime fechaCreacion,
    LocalDateTime fechaActualizacion

) {

    public static LugarResponse desde(Lugar lugar) {

        return new LugarResponse(
            lugar.getId(),
            lugar.getNombre(),
            lugar.getDescripcion(),
            lugar.getCategoria(),
            lugar.getDireccion(),
            lugar.getMunicipio(),
            lugar.getLatitud(),
            lugar.getLongitud(),
            lugar.getEstado(),
            lugar.getFechaCreacion(),
            lugar.getFechaActualizacion()
        );
    }
}
