
package co.edu.autonoma.turismo.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// Clase que representa la auditoría de cambios en los lugares turísticos respecto a los operadores municipales.
@Entity
@Table(name = "auditoria_lugar")
public class AuditoriaLugar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lugar_id", nullable = false, length = 36)
    private String lugarId;

    @Column(name = "operador_id", nullable = false, length = 36)
    private String operadorId;

    @Column(nullable = false, length = 20)
    private String accion;

    @Column(name = "dato_modificado", nullable = false,
            columnDefinition = "JSON")
    private String datoModificado;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    protected AuditoriaLugar() {
    }

    public AuditoriaLugar(String lugarId, String operadorId, String accion, String datoModificado) {
        this.lugarId = lugarId;
        this.operadorId = operadorId;
        this.accion = accion;
        this.datoModificado = datoModificado;
    }

    public Long getId() { return id; }
    public String getLugarId() { return lugarId; }
    public String getOperadorId() { return operadorId; }
    public String getAccion() { return accion; }
    public String getDatoModificado() { return datoModificado; }
    public LocalDateTime getFecha() { return fecha; }
}
