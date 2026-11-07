package co.edu.autonoma.turismo.api;

import co.edu.autonoma.turismo.api.dto.Registro.RegistroOperadorRequest;
import co.edu.autonoma.turismo.service.Registro.RegistroOperadorService;
import co.edu.autonoma.turismo.api.dto.Login.LoginResponse;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/auth")
public class RegistroOperadorController {

    private final RegistroOperadorService registroOperadorService;

    public RegistroOperadorController(
            RegistroOperadorService registroOperadorService) {

        this.registroOperadorService = registroOperadorService;
    }

@PostMapping("/registro/operador")
@PreAuthorize("hasRole('OPERADOR')")
@ResponseStatus(HttpStatus.CREATED)
    public LoginResponse registrar(
            @Valid @RequestBody RegistroOperadorRequest request) {

        return registroOperadorService.registrar(request);
    }
}