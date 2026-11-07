package co.edu.autonoma.turismo.config;

import co.edu.autonoma.turismo.security.JwtAuthenticationFilter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.http.HttpMethod;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                    session.sessionCreationPolicy(
                            SessionCreationPolicy.STATELESS
                    )
            )


.authorizeHttpRequests(auth -> auth

    // Registro y login públicos.
    .requestMatchers(
        "/api/v1/auth/registro",
        "/api/v1/auth/registro/operador",
        "/api/v1/auth/login",
        "/api/v1/auth/operador/login"
    ).permitAll()

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

    // Administración de lugares.
    .requestMatchers(
        HttpMethod.POST,
        "/api/v1/lugares"
    ).hasRole("OPERADOR")

    .requestMatchers(
        HttpMethod.PUT,
        "/api/v1/lugares/**"
    ).hasRole("OPERADOR")

    .requestMatchers(
        HttpMethod.PATCH,
        "/api/v1/lugares/**"
    ).hasRole("OPERADOR")

    .requestMatchers(
        HttpMethod.DELETE,
        "/api/v1/lugares/**"
    ).hasRole("OPERADOR")

    .anyRequest().authenticated()
)
            .addFilterBefore(
                    jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
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
