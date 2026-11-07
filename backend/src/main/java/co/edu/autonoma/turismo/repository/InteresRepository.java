package co.edu.autonoma.turismo.repository;

import co.edu.autonoma.turismo.domain.Interes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InteresRepository extends JpaRepository<Interes, Integer> {
    List<Interes> findByActivoTrueOrderByNombreAsc();
}
