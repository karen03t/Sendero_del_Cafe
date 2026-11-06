
package co.edu.autonoma.turismo.api.dto;

import co.edu.autonoma.turismo.domain.Lugar;
import java.math.BigDecimal;

## DTO para la respuesta de un lugar turístico O SEA, para enviar la información de un lugar turístico al cliente.
public record LugarResponse(
    String id,
    String nombre,
    String descripcion,
    String categoria,
    String direccion,
    String municipio,
    BigDecimal latitud,
    BigDecimal longitud,
    String estado
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
            lugar.getEstado()
        );
    }
}
