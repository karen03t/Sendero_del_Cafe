package co.edu.autonoma.turismo.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "turista_interes")
@IdClass(TuristaInteres.TuristaInteresId.class)
public class TuristaInteres {

    @Id
    @Column(name = "turista_id", length = 36, nullable = false)
    private String turistaId;

    @Id
    @Column(name = "interes_id", nullable = false)
    private Integer interesId;

    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDateTime fechaAsignacion = LocalDateTime.now();

    protected TuristaInteres() {
    }

    public TuristaInteres(String turistaId, Integer interesId) {
        this.turistaId = turistaId;
        this.interesId = interesId;
    }

    public String getTuristaId() { return turistaId; }
    public Integer getInteresId() { return interesId; }
    public LocalDateTime getFechaAsignacion() { return fechaAsignacion; }

    public static class TuristaInteresId implements Serializable {
        private String turistaId;
        private Integer interesId;

        public TuristaInteresId() {
        }

        public TuristaInteresId(String turistaId, Integer interesId) {
            this.turistaId = turistaId;
            this.interesId = interesId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TuristaInteresId that)) return false;
            return Objects.equals(turistaId, that.turistaId) && Objects.equals(interesId, that.interesId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(turistaId, interesId);
        }
    }
}
