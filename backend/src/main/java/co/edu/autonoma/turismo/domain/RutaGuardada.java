package co.edu.autonoma.turismo.domain;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * RF-03 / RF-06: ruta guardada por un turista.
 * El estado (ACTIVA/INACTIVA) vive aqui, no en la ruta: dos turistas pueden
 * guardar la misma ruta y solo uno inactivarla (CA-03).
 */
@Entity
@Table(name = "ruta_guardada")
public class RutaGuardada {

    @Embeddable
    public static class Id implements Serializable {

        @Column(name = "turista_id", length = 36, nullable = false)
        private String turistaId;

        @Column(name = "ruta_id", length = 36, nullable = false)
        private String rutaId;

        protected Id() {
        }

        public Id(String turistaId, String rutaId) {
            this.turistaId = turistaId;
            this.rutaId = rutaId;
        }

        public String getTuristaId() { return turistaId; }
        public String getRutaId() { return rutaId; }

        @Override
        public boolean equals(Object otro) {
            if (this == otro) return true;
            if (!(otro instanceof Id id)) return false;
            return Objects.equals(turistaId, id.turistaId) && Objects.equals(rutaId, id.rutaId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(turistaId, rutaId);
        }
    }

    @EmbeddedId
    private Id id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("turistaId")
    @JoinColumn(name = "turista_id", nullable = false)
    private Turista turista;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("rutaId")
    @JoinColumn(name = "ruta_id", nullable = false)
    private Ruta ruta;

    @Column(name = "nombre_custom", length = 200)
    private String nombreCustom;

    @Column(nullable = false, length = 10)
    private String estado = "ACTIVA";

    @Column(name = "fecha_guardado", nullable = false)
    private LocalDateTime fechaGuardado = LocalDateTime.now();

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion = LocalDateTime.now();

    protected RutaGuardada() {
    }

    public RutaGuardada(Turista turista, Ruta ruta, String nombreCustom) {
        this.turista = turista;
        this.ruta = ruta;
        this.id = new Id(turista.getId(), ruta.getId());
        this.nombreCustom = nombreCustom;
        this.estado = "ACTIVA";
        this.fechaGuardado = LocalDateTime.now();
        this.fechaActualizacion = this.fechaGuardado;
    }

    public void inactivar() {
        this.estado = "INACTIVA";
        this.fechaActualizacion = LocalDateTime.now();
    }

    public void reactivar() {
        this.estado = "ACTIVA";
        this.fechaActualizacion = LocalDateTime.now();
    }

    public void renombrar(String nombre) {
        this.nombreCustom = nombre;
        this.fechaActualizacion = LocalDateTime.now();
    }

    public void tocar() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    public Id getId() { return id; }
    public Turista getTurista() { return turista; }
    public Ruta getRuta() { return ruta; }
    public String getNombreCustom() { return nombreCustom; }
    public String getEstado() { return estado; }
    public LocalDateTime getFechaGuardado() { return fechaGuardado; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
}
