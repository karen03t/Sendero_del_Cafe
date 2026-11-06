
package co.edu.autonoma.turismo.repository;

import co.edu.autonoma.turismo.domain.AuditoriaLugar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditoriaLugarRepository
        extends JpaRepository<AuditoriaLugar, Long> {

    List<AuditoriaLugar> findByLugarIdOrderByFechaDesc(String lugarId);
}
