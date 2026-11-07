package co.edu.autonoma.turismo.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "turista")
public class Turista {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "nombre_completo", length = 200, nullable = false)
    private String nombreCompleto;

    @Column(name = "correo", length = 255, nullable = false, unique = true)
    private String correo;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "proveedor", length = 10, nullable = false)
    private Proveedor proveedor = Proveedor.LOCAL;

    @Column(name = "proveedor_id", length = 255)
    private String proveedorId;

    @Column(name = "acepta_tratamiento", nullable = false)
    private boolean aceptaTratamiento;

    @Column(name = "fecha_consentimiento")
    private LocalDateTime fechaConsentimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 10, nullable = false)
    private EstadoTurista estado = EstadoTurista.ACTIVO;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public enum Proveedor { LOCAL, GOOGLE, APPLE }

    public enum EstadoTurista { ACTIVO, INACTIVO }

    protected Turista() {
    }

    public Turista(String id, String nombreCompleto, String correo, String passwordHash,
                   Proveedor proveedor, String proveedorId, boolean aceptaTratamiento) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.correo = correo;
        this.passwordHash = passwordHash;
        this.proveedor = proveedor;
        this.proveedorId = proveedorId;
        this.aceptaTratamiento = aceptaTratamiento;
        this.fechaConsentimiento = aceptaTratamiento ? LocalDateTime.now() : null;
    }

    public String getId() { return id; }
    public String getNombreCompleto() { return nombreCompleto; }
    public String getCorreo() { return correo; }
    public String getPasswordHash() { return passwordHash; }
    public Proveedor getProveedor() { return proveedor; }
    public String getProveedorId() { return proveedorId; }
    public boolean isAceptaTratamiento() { return aceptaTratamiento; }
    public LocalDateTime getFechaConsentimiento() { return fechaConsentimiento; }
    public EstadoTurista getEstado() { return estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
}
