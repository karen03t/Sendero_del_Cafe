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

    // V5 / Ley 1581: ciclo de vida de la cuenta.
    @Column(name = "fecha_ultima_actividad")
    private LocalDateTime fechaUltimaActividad;

    @Column(name = "fecha_eliminacion")
    private LocalDateTime fechaEliminacion;

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
    public LocalDateTime getFechaUltimaActividad() { return fechaUltimaActividad; }
    public LocalDateTime getFechaEliminacion() { return fechaEliminacion; }

    /** Marca actividad para que la cuenta no caiga en la purga por inactividad. */
    public void registrarActividad() {
        this.fechaUltimaActividad = LocalDateTime.now();
    }

    /**
     * Ley 1581: conserva la fila por integridad referencial (rutas guardadas)
     * pero elimina los datos personales y deshabilita el acceso.
     */
    public void anonimizar(LocalDateTime momento) {
        this.nombreCompleto = "Cuenta eliminada";
        this.correo = "eliminado+" + id + "@anonimo.local";
        this.passwordHash = null;
        this.proveedorId = null;
        this.estado = EstadoTurista.INACTIVO;
        this.fechaEliminacion = momento;
    }
}
