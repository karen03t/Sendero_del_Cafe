
package co.edu.autonoma.turismo.repository;

import co.edu.autonoma.turismo.domain.Operador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OperadorRepository extends JpaRepository<Operador, String> {

    Optional<Operador> findByCorreoIgnoreCase(String correo);
}
