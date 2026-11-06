
package co.edu.autonoma.turismo.repository;

import co.edu.autonoma.turismo.domain.Lugar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LugarRepository extends JpaRepository<Lugar, String> {

    List<Lugar> findByEstadoOrderByNombreAsc(String estado);

    Optional<Lugar> findByIdAndEstado(String id, String estado);
}
