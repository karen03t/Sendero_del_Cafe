package co.edu.autonoma.turismo.repository;

import co.edu.autonoma.turismo.domain.Lugar;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LugarRepository extends JpaRepository<Lugar, String> {

    List<Lugar> findByEstadoOrderByNombreAsc(String estado);

    Optional<Lugar> findByIdAndEstado(String id, String estado);

    // SCRUM-25: filtros + interes del turista + paginacion.
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
            OR l.categoria = :categoria
        )
        AND (
            :municipio IS NULL
            OR l.municipio = :municipio
        )
        AND (
            :interes IS NULL
            OR LOWER(l.categoria) = LOWER(:interes)
        )
        ORDER BY l.nombre ASC
    """)
    Page<Lugar> buscarActivosPaginado(
        @Param("nombre") String nombre,
        @Param("categoria") String categoria,
        @Param("municipio") String municipio,
        @Param("interes") String interes,
        Pageable pageable
    );

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
            OR l.categoria = :categoria
        )
        AND (
            :municipio IS NULL
            OR l.municipio = :municipio
        )
        ORDER BY l.nombre ASC
    """)
    List<Lugar> buscarActivos(
        @Param("nombre") String nombre,
        @Param("categoria") String categoria,
        @Param("municipio") String municipio
    );
}