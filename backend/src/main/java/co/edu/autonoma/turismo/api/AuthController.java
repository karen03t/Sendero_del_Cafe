package co.edu.autonoma.turismo.api;

import co.edu.autonoma.turismo.api.dto.LoginRequest;
import co.edu.autonoma.turismo.api.dto.RegistroRequest;
import co.edu.autonoma.turismo.api.dto.RegistroResponse;
import co.edu.autonoma.turismo.service.LoginOperadorService;
import co.edu.autonoma.turismo.service.RegistroService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegistroService registroService;
    private final LoginOperadorService loginOperadorService;

    public AuthController(RegistroService registroService,
                          LoginOperadorService loginOperadorService) {
        this.registroService = registroService;
        this.loginOperadorService = loginOperadorService;
    }

    @PostMapping("/registro")
    public ResponseEntity<RegistroResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registroService.registrar(request));
    }

    @PostMapping("/operador/login")
    public ResponseEntity<?> loginOperador(@Valid @RequestBody LoginRequest request) {
        return loginOperadorService.autenticar(request)
            .<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "codigo", "CREDENCIALES_INVALIDAS",
                "mensaje", "Correo o contraseña incorrectos")));
    }
}