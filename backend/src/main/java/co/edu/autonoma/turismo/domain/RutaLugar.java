package co.edu.autonoma.turismo.domain;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * Parada de una ruta: que lugar se visita y en que orden.
 * PK compuesta (ruta_id, lugar_id) tal como la define la V4.
 */
@Entity
@Table(name = "ruta_lugar")
public class RutaLugar {

    @Embeddable
    public static class Id implements Serializable {

        @Column(name = "ruta_id", length = 36, nullable = false)
        private String rutaId;

        @Column(name = "lugar_id", length = 36, nullable = false)
        private String lugarId;

        protected Id() {
        }

        public Id(String rutaId, String lugarId) {
            this.rutaId = rutaId;
            this.lugarId = lugarId;
        }

        public String getRutaId() { return rutaId; }
        public String getLugarId() { return lugarId; }

        @Override
        public boolean equals(Object otro) {
            if (this == otro) return true;
            if (!(otro instanceof Id id)) return false;
            return Objects.equals(rutaId, id.rutaId) && Objects.equals(lugarId, id.lugarId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(rutaId, lugarId);
        }
    }

    @EmbeddedId
    private Id id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("rutaId")
    @JoinColumn(name = "ruta_id", nullable = false)
    private Ruta ruta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("lugarId")
    @JoinColumn(name = "lugar_id", nullable = false)
    private Lugar lugar;

    @Column(nullable = false)
    private Integer orden;

    @Column(name = "tiempo_estimado_min")
    private Integer tiempoEstimadoMin;

    protected RutaLugar() {
    }

    public RutaLugar(Lugar lugar, int tiempoEstimadoMin) {
        this.lugar = lugar;
        this.tiempoEstimadoMin = tiempoEstimadoMin;
        this.id = new Id(null, lugar.getId());
    }

    void asignarRuta(Ruta ruta) {
        this.ruta = ruta;
        this.id = new Id(ruta.getId(), lugar.getId());
    }

    void asignarOrden(int orden) {
        this.orden = orden;
    }

    public Id getId() { return id; }
    public Ruta getRuta() { return ruta; }
    public Lugar getLugar() { return lugar; }
    public Integer getOrden() { return orden; }
    public Integer getTiempoEstimadoMin() { return tiempoEstimadoMin; }
}
