
package co.edu.autonoma.turismo.security;

import co.edu.autonoma.turismo.config.Rol;
import co.edu.autonoma.turismo.domain.Operador;
import co.edu.autonoma.turismo.repository.OperadorRepository;
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
        private final OperadorRepository operadorRepository;

        public JwtAuthenticationFilter(
                TokenService tokenService,
                OperadorRepository operadorRepository
        ) {
                this.tokenService = tokenService;
                this.operadorRepository = operadorRepository;
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

                        // 1. Validar firma y expiración del JWT.
                        Claims claims =
                                tokenService.validarYObtenerClaims(token);

                        String usuarioId = claims.getSubject();
                        String rol = claims.get("rol", String.class);

                        if (usuarioId != null &&
                                rol != null &&
                                SecurityContextHolder.getContext()
                                        .getAuthentication() == null) {

                        // 2. Comprobar que el rol sea válido.
                        Rol rolValidado = Rol.valueOf(rol);

                        // 3. Comprobar que el operador siga activo.
                        boolean usuarioActivo = true;

                        if (rolValidado == Rol.OPERADOR) {

                                Operador operador = operadorRepository
                                        .findById(usuarioId)
                                        .orElse(null);

                                usuarioActivo = operador != null
                                        && "ACTIVO".equalsIgnoreCase(
                                                operador.getEstado()
                                        );
                        }

                        // 4. Solo autenticar si el usuario está autorizado.
                        if (usuarioActivo) {

                                SimpleGrantedAuthority autoridad =
                                        new SimpleGrantedAuthority(
                                                "ROLE_" + rolValidado.name()
                                        );

                                UsernamePasswordAuthenticationToken autenticacion =
                                        new UsernamePasswordAuthenticationToken(
                                                usuarioId,
                                                null,
                                                List.of(autoridad)
                                        );

                                SecurityContextHolder.getContext()
                                        .setAuthentication(autenticacion);
                        } else {

                                // Operador inexistente o inactivo.
                                SecurityContextHolder.clearContext();
                        }
                        }

                } catch (JwtException | IllegalArgumentException ex) {

                        // Token inválido, expirado o con rol desconocido.
                        SecurityContextHolder.clearContext();
                }
                }

                filterChain.doFilter(request, response);
        }
}
