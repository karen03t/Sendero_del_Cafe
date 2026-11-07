package co.edu.autonoma.turismo.exception;

import co.edu.autonoma.turismo.api.dto.Error.ErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Orden de prioridad para reportar un solo error (CP-03 espera el del correo). */
    private static final List<String> PRIORIDAD = List.of(
            "NOMBRE_OBLIGATORIO", "CORREO_OBLIGATORIO", "CORREO_FORMATO_INVALIDO");

    private static final Map<String, String> MENSAJES = Map.of(
            "NOMBRE_OBLIGATORIO", "El nombre completo es obligatorio",
            "CORREO_OBLIGATORIO", "El correo es obligatorio",
            "CORREO_FORMATO_INVALIDO", "El correo no tiene un formato válido");

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> manejarApi(ApiException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(new ErrorResponse(ex.getCodigo(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidacion(MethodArgumentNotValidException ex) {
        String codigo = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getDefaultMessage() == null ? "" : e.getDefaultMessage())
                .filter(MENSAJES::containsKey)
                .min(Comparator.comparingInt(c -> PRIORIDAD.indexOf(c)))
                .orElse("SOLICITUD_INVALIDA");
        String mensaje = MENSAJES.getOrDefault(codigo, "La solicitud no cumple el formato esperado");
        return ResponseEntity.badRequest().body(new ErrorResponse(codigo, mensaje));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> manejarCuerpoIlegible(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("SOLICITUD_INVALIDA", "El cuerpo de la solicitud no es un JSON válido"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> manejarIntegridad(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("CORREO_DUPLICADO", "Ya existe una cuenta con este correo"));
    }
}
