package co.edu.autonoma.turismo.service.Registro;

import co.edu.autonoma.turismo.service.TokenService;
import co.edu.autonoma.turismo.api.dto.Login.LoginResponse;
import co.edu.autonoma.turismo.config.Rol;
import co.edu.autonoma.turismo.domain.Operador;
import co.edu.autonoma.turismo.api.dto.Registro.RegistroOperadorRequest;
import co.edu.autonoma.turismo.repository.OperadorRepository;
import co.edu.autonoma.turismo.repository.TuristaRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class RegistroOperadorService {

    private final OperadorRepository operadorRepository;
    private final TuristaRepository turistaRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public RegistroOperadorService(
            OperadorRepository operadorRepository,
            TuristaRepository turistaRepository,
            PasswordEncoder passwordEncoder,
            TokenService tokenService) {

        this.operadorRepository = operadorRepository;
        this.turistaRepository = turistaRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional
    public LoginResponse registrar(RegistroOperadorRequest request) {

        // 1. Normalizar el correo
        String correo = request.correo()
                .trim()
                .toLowerCase(Locale.ROOT);

        // 2. Verificar que no exista como turista ni operador
        if (turistaRepository.existsByCorreoIgnoreCase(correo)
                || operadorRepository.existsByCorreoIgnoreCase(correo)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El correo ya se encuentra registrado"
            );
        }

        // 3. Encriptar la contraseña
        String passwordHash = passwordEncoder.encode(request.password());

        // 4. Crear el operador
        Operador operador = new Operador(
                request.nombreCompleto().trim(),
                correo,
                passwordHash
        );

        // 5. Guardar en la base de datos
        Operador operadorGuardado = operadorRepository.save(operador);

        // 6. Generar token con rol OPERADOR
        String token = tokenService.generar(
                operadorGuardado.getId(),
                operadorGuardado.getCorreo(),
                Rol.OPERADOR
        );

        // 7. Retornar respuesta
        return new LoginResponse(
                token,
                "Bearer",
                Rol.OPERADOR.name()
        );
    }
}