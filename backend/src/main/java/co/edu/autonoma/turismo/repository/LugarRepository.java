package co.edu.autonoma.turismo.repository;

import co.edu.autonoma.turismo.domain.Lugar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LugarRepository extends JpaRepository<Lugar, String> {

    List<Lugar> findByEstadoOrderByNombreAsc(String estado);

    Optional<Lugar> findByIdAndEstado(String id, String estado);

    // RF-02: búsqueda y filtros de lugares turísticos activos.
    @Query("""
        SELECT l
        FROM Lugar l
        WHERE l.estado = 'ACTIVO'
        AND (
            :nombre IS NULL
            OR LOWER(l.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))
        )
        AND (
            :categoria IS NULL
            OR LOWER(l.categoria) LIKE LOWER(CONCAT('%', :categoria, '%'))
        )
        AND (
            :municipio IS NULL
            OR LOWER(l.municipio) LIKE LOWER(CONCAT('%', :municipio, '%'))
        )
        ORDER BY l.nombre ASC
    """)
    List<Lugar> buscarActivos(
        @Param("nombre") String nombre,
        @Param("categoria") String categoria,
        @Param("municipio") String municipio
    );
}