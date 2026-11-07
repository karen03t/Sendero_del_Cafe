package co.edu.autonoma.turismo.service.Login;

import co.edu.autonoma.turismo.service.TokenService;
import co.edu.autonoma.turismo.api.dto.Login.LoginRequest;
import co.edu.autonoma.turismo.api.dto.Login.LoginResponse;
import co.edu.autonoma.turismo.config.Rol;
import co.edu.autonoma.turismo.domain.Operador;
import co.edu.autonoma.turismo.domain.Turista;
import co.edu.autonoma.turismo.repository.OperadorRepository;
import co.edu.autonoma.turismo.repository.TuristaRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoginService {

        private final TuristaRepository turistas;
        private final OperadorRepository operadores;
        private final PasswordEncoder passwordEncoder;
        private final TokenService tokenService;

        public LoginService(
                TuristaRepository turistas,
                OperadorRepository operadores,
                PasswordEncoder passwordEncoder,
                TokenService tokenService
        ) {
                this.turistas = turistas;
                this.operadores = operadores;
                this.passwordEncoder = passwordEncoder;
                this.tokenService = tokenService;
        }

        public Optional<LoginResponse> autenticar(LoginRequest request) {

                String correo = request.correo().trim();

                // 1. Buscar primero al turista.
                Optional<Turista> turistaEncontrado =
                        turistas.findByCorreoIgnoreCase(correo);

                if (turistaEncontrado.isPresent()) {

                Turista turista = turistaEncontrado.get();

                // Verificar que esté activo.
                if (turista.getEstado() != Turista.EstadoTurista.ACTIVO) {
                        return Optional.empty();
                }

                // El login con contraseña solo aplica a cuentas locales.
                if (turista.getProveedor() != Turista.Proveedor.LOCAL) {
                        return Optional.empty();
                }

                if (turista.getPasswordHash() == null ||
                        !passwordEncoder.matches(
                                request.password(),
                                turista.getPasswordHash()
                        )) {
                        return Optional.empty();
                }

                // Generar token de turista.
                String token = tokenService.generar(
                        turista.getId(),
                        turista.getCorreo(),
                        Rol.TURISTA
                );

                return Optional.of(
                        new LoginResponse(
                                token,
                                "Bearer",
                                Rol.TURISTA.name()
                        )
                );
                }

                // 2. Si no existe como turista, buscar como operador.
                Optional<Operador> operadorEncontrado =
                        operadores.findByCorreoIgnoreCase(correo);

                if (operadorEncontrado.isPresent()) {

                Operador operador = operadorEncontrado.get();

                if (!"ACTIVO".equalsIgnoreCase(operador.getEstado())) {
                        return Optional.empty();
                }

                if (operador.getPasswordHash() == null ||
                        !passwordEncoder.matches(
                                request.password(),
                                operador.getPasswordHash()
                        )) {
                        return Optional.empty();
                }

                // Generar token de operador.
                String token = tokenService.generar(
                        operador.getId(),
                        operador.getCorreo(),
                        Rol.OPERADOR
                );

                return Optional.of(
                        new LoginResponse(
                                token,
                                "Bearer",
                                Rol.OPERADOR.name()
                        )
                );
                }

                // 3. No existe ningún usuario con ese correo.
                return Optional.empty();
        }
        }