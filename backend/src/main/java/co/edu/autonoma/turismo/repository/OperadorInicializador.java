package co.edu.autonoma.turismo.repository;

import co.edu.autonoma.turismo.domain.Operador;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class OperadorInicializador implements CommandLineRunner {

    private final OperadorRepository operadores;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.operador-inicial.nombre:}")
    private String nombre;

    @Value("${app.operador-inicial.correo:}")
    private String correo;

    @Value("${app.operador-inicial.password:}")
    private String password;

    public OperadorInicializador(
            OperadorRepository operadores,
            PasswordEncoder passwordEncoder
    ) {
        this.operadores = operadores;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        // Si no se configuraron credenciales, no se crea nada.
        if (nombre.isBlank() ||
                correo.isBlank() ||
                password.isBlank()) {

            return;
        }

        String correoNormalizado = correo.trim().toLowerCase();

        // Evitar crear el mismo operador dos veces.
        if (operadores.existsByCorreoIgnoreCase(correoNormalizado)) {
            return;
        }

        String hash = passwordEncoder.encode(password);

        Operador operador = new Operador(
                nombre.trim(),
                correoNormalizado,
                hash
        );

        operadores.save(operador);

        System.out.println("Operador inicial creado correctamente.");
    }
}