package co.edu.autonoma.turismo.repository;

import co.edu.autonoma.turismo.domain.Turista;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TuristaRepository
        extends JpaRepository<Turista, String> {

    boolean existsByCorreoIgnoreCase(String correo);

    Optional<Turista> findByCorreoIgnoreCase(String correo);

    // RNF-02 / Ley 1581: cuentas sin actividad en el periodo de retencion.
    @Query("""
        SELECT t
        FROM Turista t
        WHERE t.fechaEliminacion IS NULL
        AND COALESCE(t.fechaUltimaActividad, t.fechaCreacion) < :limite
    """)
    List<Turista> buscarParaAnonimizar(@Param("limite") LocalDateTime limite);
}