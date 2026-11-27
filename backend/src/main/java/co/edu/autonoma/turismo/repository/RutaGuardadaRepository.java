package co.edu.autonoma.turismo.repository;

import co.edu.autonoma.turismo.domain.RutaGuardada;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RutaGuardadaRepository
        extends JpaRepository<RutaGuardada, RutaGuardada.Id> {

    List<RutaGuardada>
        findByIdTuristaIdAndEstadoOrderByFechaGuardadoDesc(
            String turistaId,
            String estado
        );

    Optional<RutaGuardada> findByIdTuristaIdAndIdRutaId(
        String turistaId,
        String rutaId
    );

    // RF-04: rutas creadas o modificadas despues de una marca de tiempo.
    List<RutaGuardada>
        findByIdTuristaIdAndFechaActualizacionAfterOrderByFechaActualizacionAsc(
            String turistaId,
            LocalDateTime desde
        );
}
