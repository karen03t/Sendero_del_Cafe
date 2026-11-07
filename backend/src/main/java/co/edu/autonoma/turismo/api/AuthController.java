package co.edu.autonoma.turismo.api;

import co.edu.autonoma.turismo.api.dto.LoginRequest;
import co.edu.autonoma.turismo.api.dto.LoginResponse;
import co.edu.autonoma.turismo.api.dto.RegistroRequest;
import co.edu.autonoma.turismo.api.dto.RegistroResponse;

import co.edu.autonoma.turismo.service.LoginService;
import co.edu.autonoma.turismo.service.RegistroService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegistroService registroService;
    private final LoginService loginService;

    public AuthController(
            RegistroService registroService,
            LoginService loginService
    ) {
        this.registroService = registroService;
        this.loginService = loginService;
    }

    // Registro de turistas.
    @PostMapping("/registro")
    public ResponseEntity<RegistroResponse> registrar(
            @Valid @RequestBody RegistroRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(registroService.registrar(request));
    }

    // Login general: turistas y operadores.
    // Se conserva la ruta anterior por compatibilidad.
    @PostMapping({
            "/login",
            "/operador/login"
    })
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request
    ) {

        return loginService.autenticar(request)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .body(Map.of(
                                        "codigo", "CREDENCIALES_INVALIDAS",
                                        "mensaje", "Correo o contraseña incorrectos"
                                ))
                );
    }
}