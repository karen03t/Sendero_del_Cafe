
package co.edu.autonoma.turismo.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// Clase que representa un operador municipal en el sistema de turismo. Es aquel que tiene la responsabilidad de gestionar los lugares turísticos en su municipio.
@Entity
@Table(name = "operador_municipal")
public class Operador {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "nombre_completo", nullable = false, length = 200)
    private String nombreCompleto;

    @Column(nullable = false, unique = true, length = 255)
    private String correo;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 10)
    private String estado = "ACTIVO";

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    protected Operador() {
    }

    public String getId() { return id; }
    public String getNombreCompleto() { return nombreCompleto; }
    public String getCorreo() { return correo; }
    public String getPasswordHash() { return passwordHash; }
    public String getEstado() { return estado; }
}
