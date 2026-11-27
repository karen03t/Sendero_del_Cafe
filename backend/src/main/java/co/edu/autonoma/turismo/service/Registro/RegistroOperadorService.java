
package co.edu.autonoma.turismo.service.registro;

import co.edu.autonoma.turismo.api.dto.login.LoginResponse;
import co.edu.autonoma.turismo.api.dto.registro.RegistroOperadorRequest;
import co.edu.autonoma.turismo.config.Rol;
import co.edu.autonoma.turismo.domain.Operador;
import co.edu.autonoma.turismo.repository.OperadorRepository;
import co.edu.autonoma.turismo.repository.TuristaRepository;
import co.edu.autonoma.turismo.service.TokenService;
import co.edu.autonoma.turismo.exception.ApiException;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class RegistroOperadorService {

    private final OperadorRepository operadores;
    private final TuristaRepository turistas;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public RegistroOperadorService(
            OperadorRepository operadores,
            TuristaRepository turistas,
            PasswordEncoder passwordEncoder,
            TokenService tokenService
    ) {
        this.operadores = operadores;
        this.turistas = turistas;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional
    public LoginResponse registrar(RegistroOperadorRequest request) {

        String correo = request.correo()
                .trim()
                .toLowerCase(Locale.ROOT);

        String nombre = request.nombreCompleto().trim();

        // Evitar correos duplicados en ambas entidades.
        if (operadores.existsByCorreoIgnoreCase(correo)
                || turistas.existsByCorreoIgnoreCase(correo)) {

            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "CORREO_DUPLICADO",
                    "Ya existe una cuenta con este correo"
            );
        }

        // Encriptar contraseña.
        String passwordHash =
                passwordEncoder.encode(request.password());

        // Crear y guardar operador.
        Operador operador = new Operador(
                nombre,
                correo,
                passwordHash
        );

        operador = operadores.save(operador);

        // Generar JWT con rol OPERADOR.
        String token = tokenService.generar(
                operador.getId(),
                operador.getCorreo(),
                Rol.OPERADOR
        );

        return new LoginResponse(
                token,
                "Bearer",
                Rol.OPERADOR.name()
        );
    }
}
