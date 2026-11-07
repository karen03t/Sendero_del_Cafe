package co.edu.autonoma.turismo.repository;

import co.edu.autonoma.turismo.domain.TuristaInteres;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TuristaInteresRepository extends JpaRepository<TuristaInteres, TuristaInteres.TuristaInteresId> {
    List<TuristaInteres> findByTuristaId(String turistaId);
}
