
package co.edu.autonoma.turismo.api.dto.auditoria;

import co.edu.autonoma.turismo.domain.AuditoriaLugar;
import java.time.LocalDateTime;

// DTO para la respuesta de una auditoría de cambios en un lugar turístico, O SEA, para enviar la información de una auditoría al cliente.
public record AuditoriaResponse(
    Long id,
    String lugarId,
    String operadorId,
    String accion,
    String datoModificado,
    LocalDateTime fecha
) {

    public static AuditoriaResponse desde(AuditoriaLugar auditoria) {
        return new AuditoriaResponse(
            auditoria.getId(),
            auditoria.getLugarId(),
            auditoria.getOperadorId(),
            auditoria.getAccion(),
            auditoria.getDatoModificado(),
            auditoria.getFecha()
        );
    }
}
