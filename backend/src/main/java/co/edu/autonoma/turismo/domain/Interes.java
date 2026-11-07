package co.edu.autonoma.turismo.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "interes")
public class Interes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "nombre", length = 100, nullable = false, unique = true)
    private String nombre;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    protected Interes() {
    }

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public boolean isActivo() { return activo; }
}
