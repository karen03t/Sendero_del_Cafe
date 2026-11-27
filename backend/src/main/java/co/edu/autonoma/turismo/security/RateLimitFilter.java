package co.edu.autonoma.turismo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Limita los intentos de autenticacion y registro por IP.
 * Evita fuerza bruta sobre el login y creacion masiva de cuentas.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Set<String> RUTAS_LIMITADAS = Set.of(
            "/api/v1/auth/login",
            "/api/v1/auth/registro",
            "/api/v1/auth/operador/login",
            "/api/v1/auth/registro/operador");

    private final LimiteIntentos limite;

    public RateLimitFilter(LimiteIntentos limite) {
        this.limite = limite;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        boolean aplica = "POST".equalsIgnoreCase(request.getMethod())
                && RUTAS_LIMITADAS.contains(request.getRequestURI());

        if (aplica && !limite.permitir(request.getRemoteAddr() + "|" + request.getRequestURI())) {

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write(
                    "{\"codigo\":\"DEMASIADAS_SOLICITUDES\","
                    + "\"mensaje\":\"Demasiados intentos. Espere un minuto y vuelva a intentarlo\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
