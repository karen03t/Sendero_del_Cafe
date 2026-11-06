package co.edu.autonoma.turismo.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class TokenService {

    private final SecretKey clave;
    private final Duration vigencia;

    public TokenService(@Value("${app.jwt.secret}") String secreto,
                        @Value("${app.jwt.ttl-minutes:120}") long ttlMinutos) {
        if (secreto == null || secreto.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("app.jwt.secret debe tener al menos 32 bytes");
        }
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.vigencia = Duration.ofMinutes(ttlMinutos);
    }

    public String generar(String turistaId, String correo) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(turistaId)
                .claim("correo", correo)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(vigencia)))
                .signWith(clave)
                .compact();
    }
}
