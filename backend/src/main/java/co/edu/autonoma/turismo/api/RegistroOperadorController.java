
package co.edu.autonoma.turismo.api;

import co.edu.autonoma.turismo.api.dto.Registro.RegistroOperadorRequest;
import co.edu.autonoma.turismo.api.dto.Login.LoginResponse;
import co.edu.autonoma.turismo.service.Registro.RegistroOperadorService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class RegistroOperadorController {

    private final RegistroOperadorService registroOperadorService;

    public RegistroOperadorController(
            RegistroOperadorService registroOperadorService
    ) {
        this.registroOperadorService = registroOperadorService;
    }

    // Registro público de operadores.
    @PostMapping("/registro/operador")
    public ResponseEntity<LoginResponse> registrar(
            @Valid @RequestBody RegistroOperadorRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(registroOperadorService.registrar(request));
    }
}
