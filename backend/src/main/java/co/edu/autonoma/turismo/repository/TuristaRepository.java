package co.edu.autonoma.turismo.repository;

import co.edu.autonoma.turismo.domain.Turista;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TuristaRepository extends JpaRepository<Turista, String> {
    boolean existsByCorreoIgnoreCase(String correo);
    Optional<Turista> findByCorreoIgnoreCase(String correo);
}
