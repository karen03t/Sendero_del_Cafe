package co.edu.autonoma.turismo.service;

import co.edu.autonoma.turismo.api.dto.LoginRequest;
import co.edu.autonoma.turismo.api.dto.LoginResponse;
import co.edu.autonoma.turismo.config.Rol;
import co.edu.autonoma.turismo.repository.OperadorRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoginOperadorService {

    private final OperadorRepository operadores;
    private final PasswordEncoder encoder;
    private final TokenService tokenService;

    public LoginOperadorService(OperadorRepository operadores,
                                PasswordEncoder encoder,
                                TokenService tokenService) {
        this.operadores = operadores;
        this.encoder = encoder;
        this.tokenService = tokenService;
    }

    public Optional<LoginResponse> autenticar(LoginRequest request) {
        return operadores.findByCorreoIgnoreCase(request.correo().trim())
            .filter(o -> "ACTIVO".equals(o.getEstado()))
            .filter(o -> encoder.matches(request.password(), o.getPasswordHash()))
            .map(o -> new LoginResponse(
                tokenService.generar(o.getId(), o.getCorreo(), Rol.OPERADOR),
                "Bearer",
                Rol.OPERADOR.name()));
    }
}