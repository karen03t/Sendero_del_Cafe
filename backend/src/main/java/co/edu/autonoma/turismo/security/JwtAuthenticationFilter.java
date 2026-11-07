package co.edu.autonoma.turismo.security;

import co.edu.autonoma.turismo.config.Rol;
import co.edu.autonoma.turismo.service.TokenService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    public JwtAuthenticationFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String encabezado = request.getHeader("Authorization");

        if (encabezado != null && encabezado.startsWith("Bearer ")) {

            String token = encabezado.substring(7);

            try {

                Claims claims =
                        tokenService.validarYObtenerClaims(token);

                String usuarioId = claims.getSubject();
                String rol = claims.get("rol", String.class);

                if (usuarioId != null &&
                        rol != null &&
                        SecurityContextHolder.getContext()
                                .getAuthentication() == null) {

                    // Comprobar que el rol exista.
                    Rol.valueOf(rol);

                    // Spring Security espera ROLE_OPERADOR o ROLE_TURISTA.
                    SimpleGrantedAuthority autoridad =
                            new SimpleGrantedAuthority("ROLE_" + rol);

                    UsernamePasswordAuthenticationToken autenticacion =
                            new UsernamePasswordAuthenticationToken(
                                    usuarioId,
                                    null,
                                    List.of(autoridad)
                            );

                    SecurityContextHolder.getContext()
                            .setAuthentication(autenticacion);
                }

            } catch (JwtException | IllegalArgumentException ex) {

                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}