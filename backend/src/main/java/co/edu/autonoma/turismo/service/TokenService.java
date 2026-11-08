package co.edu.autonoma.turismo.service;

import co.edu.autonoma.turismo.config.Rol;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class TokenService {

        private final SecretKey clave;
        private final long minutosExpiracion;

        public TokenService(
                @Value("${app.jwt.secret}") String secreto,
                @Value("${app.jwt.ttl-minutes}") long minutosExpiracion
        ) {

                byte[] bytes = secreto.getBytes(StandardCharsets.UTF_8);

                if (bytes.length < 64) {
                                throw new IllegalStateException(
                                                "El secreto JWT debe tener al menos 64 bytes para HS512"
                                );
                }

                this.clave = Keys.hmacShaKeyFor(bytes);
                        this.minutosExpiracion = minutosExpiracion;
                }

        // Por defecto, el registro genera un token de turista.
        public String generar(String usuarioId, String correo) {
                return generar(usuarioId, correo, Rol.TURISTA);
        }

        // Generación de token para cualquiera de los dos roles.
        public String generar(
                String usuarioId,
                String correo,
                Rol rol
        ) {

                Date ahora = new Date();

                return Jwts.builder()
                        .subject(usuarioId)
                        .claim("correo", correo)
                        .claim("rol", rol.name())
                        .issuedAt(ahora)
                        .expiration(
                                new Date(
                                        ahora.getTime()
                                                + minutosExpiracion * 60_000L
                                )
                        )
                        .signWith(clave, Jwts.SIG.HS512)
                        .compact();
        }

        public Claims validarYObtenerClaims(String token) {

                return Jwts.parser()
                        .verifyWith(clave)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
        }
        }