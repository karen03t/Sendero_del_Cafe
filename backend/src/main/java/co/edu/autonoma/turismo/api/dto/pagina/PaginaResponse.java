package co.edu.autonoma.turismo.api.dto.pagina;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Envoltorio de paginacion propio.
 *
 * Serializar PageImpl directamente no garantiza un JSON estable entre
 * versiones de Spring Data (aviso de Spring). Este DTO conserva las mismas
 * claves que ya consumen las colecciones (content, number, size,
 * totalElements, totalPages) sin exponer detalles internos.
 */
public record PaginaResponse<T>(
        List<T> content,
        int number,
        int size,
        long totalElements,
        int totalPages) {

    public static <T> PaginaResponse<T> desde(Page<T> pagina) {
        return new PaginaResponse<>(
                pagina.getContent(),
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages());
    }
}
