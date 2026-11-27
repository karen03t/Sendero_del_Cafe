
package co.edu.autonoma.turismo.config;

import co.edu.autonoma.turismo.security.JwtAuthenticationFilter;
import co.edu.autonoma.turismo.security.RateLimitFilter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitFilter rateLimitFilter;

    @Value("${seguridad.cors.origenes:http://localhost:5173,http://localhost:3000}")
    private String origenesPermitidos;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RateLimitFilter rateLimitFilter
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.rateLimitFilter = rateLimitFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .cors(Customizer.withDefaults())

            .headers(headers -> headers
                .frameOptions(frame -> frame.deny())
                .contentTypeOptions(Customizer.withDefaults())
            )

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .authorizeHttpRequests(auth -> auth

                // Registro público de turistas y login.
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/v1/auth/registro",
                    "/api/v1/auth/login",
                    "/api/v1/auth/operador/login"
                ).permitAll()

                // Prohibir el registro público de operadores.
                .requestMatchers(
                    "/api/v1/auth/registro/operador"
                ).denyAll()

                // Consulta pública de intereses.
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/v1/intereses",
                    "/api/v1/intereses/**"
                ).permitAll()

                // Auditoría exclusiva de operadores.
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/v1/lugares/*/auditoria"
                ).hasRole("OPERADOR")

                // Consultas públicas de lugares.
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/v1/lugares",
                    "/api/v1/lugares/*"
                ).permitAll()

                // Crear lugares: solo operadores.
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/v1/lugares"
                ).hasRole("OPERADOR")

                // Modificar lugares.
                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/v1/lugares/**"
                ).hasRole("OPERADOR")

                // Inactivación lógica.
                .requestMatchers(
                    HttpMethod.PATCH,
                    "/api/v1/lugares/**"
                ).hasRole("OPERADOR")

                // Eliminar lugares.
                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/v1/lugares/**"
                ).hasRole("OPERADOR")

                // Rutas turisticas: solo el turista autenticado.
                .requestMatchers(
                    "/api/v1/rutas",
                    "/api/v1/rutas/**"
                ).hasRole("TURISTA")

                // Cualquier otra ruta requiere autenticación.
                .anyRequest().authenticated()
            )

            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            )

            // Se registra despues del JWT para quedar antes que el en la cadena:
            // ambas inserciones usan el mismo ancla y la ultima va delante.
            .addFilterBefore(
                rateLimitFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuracion = new CorsConfiguration();

        configuracion.setAllowedOrigins(
            Arrays.stream(origenesPermitidos.split(","))
                .map(String::trim)
                .filter(origen -> !origen.isEmpty())
                .toList()
        );

        configuracion.setAllowedMethods(
            List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        configuracion.setAllowedHeaders(
            List.of("Authorization", "Content-Type", "Accept"));

        configuracion.setExposedHeaders(List.of("Location"));
        configuracion.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource fuente =
            new UrlBasedCorsConfigurationSource();

        fuente.registerCorsConfiguration("/**", configuracion);

        return fuente;
    }

    @Bean
    public FilterRegistrationBean<RateLimitFilter>
            rateLimitFilterRegistration() {

        FilterRegistrationBean<RateLimitFilter> registration =
            new FilterRegistrationBean<>(rateLimitFilter);

        registration.setEnabled(false);

        return registration;
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter>
            jwtAuthenticationFilterRegistration() {

        FilterRegistrationBean<JwtAuthenticationFilter> registration =
            new FilterRegistrationBean<>(jwtAuthenticationFilter);

        registration.setEnabled(false);

        return registration;
    }
}
