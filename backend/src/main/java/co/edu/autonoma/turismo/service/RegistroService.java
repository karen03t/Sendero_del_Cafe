package co.edu.autonoma.turismo.service;

import co.edu.autonoma.turismo.api.dto.RegistroRequest;
import co.edu.autonoma.turismo.api.dto.RegistroResponse;
import co.edu.autonoma.turismo.domain.Interes;
import co.edu.autonoma.turismo.domain.Turista;
import co.edu.autonoma.turismo.domain.TuristaInteres;
import co.edu.autonoma.turismo.exception.ApiException;
import co.edu.autonoma.turismo.repository.InteresRepository;
import co.edu.autonoma.turismo.repository.TuristaInteresRepository;
import co.edu.autonoma.turismo.repository.TuristaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Registro de turista — RF-01 / RN-06 / CA-01 / CA-02 / RNF-02.
 * Toda validación ocurre antes de escribir: si algo falla no se crea ni modifica nada (CP-04).
 */
@Service
public class RegistroService {

    private final TuristaRepository turistas;
    private final InteresRepository intereses;
    private final TuristaInteresRepository turistaInteres;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public RegistroService(TuristaRepository turistas,
                           InteresRepository intereses,
                           TuristaInteresRepository turistaInteres,
                           PasswordEncoder passwordEncoder,
                           TokenService tokenService) {
        this.turistas = turistas;
        this.intereses = intereses;
        this.turistaInteres = turistaInteres;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional
    public RegistroResponse registrar(RegistroRequest req) {
        String correo = req.correo().trim().toLowerCase(Locale.ROOT);
        String nombre = req.nombreCompleto().trim();

        List<Integer> idsIntereses = validarIntereses(req.intereses());
        validarConsentimiento(req.aceptaTratamientoDatos());
        Turista.Proveedor proveedor = validarProveedor(req.proveedor());
        String hash = validarCredenciales(proveedor, req.password(), req.idTokenExterno());

        if (turistas.existsByCorreoIgnoreCase(correo)) {
            throw new ApiException(HttpStatus.CONFLICT, "CORREO_DUPLICADO", "Ya existe una cuenta con este correo");
        }

        Map<Integer, Interes> catalogo = intereses.findAllById(idsIntereses).stream()
                .collect(Collectors.toMap(Interes::getId, Function.identity()));
        boolean todosValidos = idsIntereses.stream()
                .allMatch(id -> catalogo.containsKey(id) && catalogo.get(id).isActivo());
        if (!todosValidos) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INTERES_INEXISTENTE",
                    "Uno o más intereses no existen o están inactivos");
        }

        String id = UUID.randomUUID().toString();
        Turista turista = new Turista(id, nombre, correo, hash, proveedor, req.idTokenExterno(), true);
        turistas.save(turista);
        idsIntereses.forEach(interesId -> turistaInteres.save(new TuristaInteres(id, interesId)));

        String token = tokenService.generar(id, correo);
        return new RegistroResponse(id, nombre, correo, idsIntereses, token);
    }

    private List<Integer> validarIntereses(List<Integer> entrada) {
        if (entrada == null || entrada.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "SIN_INTERESES",
                    "Elija al menos un interés para continuar");
        }
        List<Integer> limpios = List.copyOf(new LinkedHashSet<>(entrada));
        if (limpios.stream().anyMatch(id -> id == null)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INTERES_INEXISTENTE",
                    "Uno o más intereses no existen o están inactivos");
        }
        return limpios;
    }

    private void validarConsentimiento(boolean acepta) {
        if (!acepta) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "CONSENTIMIENTO_REQUERIDO",
                    "Debe aceptar el tratamiento de datos — Ley 1581 de 2012");
        }
    }

    private Turista.Proveedor validarProveedor(String valor) {
        if (valor == null || valor.isBlank()) {
            return Turista.Proveedor.LOCAL;
        }
        try {
            return Turista.Proveedor.valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PROVEEDOR_INVALIDO",
                    "El proveedor debe ser LOCAL, GOOGLE o APPLE");
        }
    }

    private String validarCredenciales(Turista.Proveedor proveedor, String password, String idTokenExterno) {
        if (proveedor == Turista.Proveedor.LOCAL) {
            if (password == null || password.isBlank()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "PASSWORD_OBLIGATORIO",
                        "La contraseña es obligatoria para el registro local");
            }
            if (password.length() < 8) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "PASSWORD_MINIMO",
                        "La contraseña debe tener al menos 8 caracteres");
            }
            return passwordEncoder.encode(password);
        }
        if (idTokenExterno == null || idTokenExterno.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "TOKEN_EXTERNO_OBLIGATORIO",
                    "El token del proveedor externo es obligatorio");
        }
        return null;
    }
}
