package co.edu.autonoma.turismo.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * RF-03: ruta turistica generada y guardada por un turista.
 * Las paradas viven en ruta_lugar y se ordenan por el campo orden.
 */
@Entity
@Table(name = "ruta")
public class Ruta {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, length = 100)
    private String municipio;

    @Column(name = "distancia_km", precision = 8, scale = 2)
    private BigDecimal distanciaKm;

    @Column(name = "duracion_min")
    private Integer duracionMin;

    @Column(nullable = false, length = 10)
    private String estado = "ACTIVO";

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @OneToMany(mappedBy = "ruta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<RutaLugar> paradas = new ArrayList<>();

    protected Ruta() {
    }

    public Ruta(String id, String nombre, String descripcion, String municipio) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.municipio = municipio;
        this.estado = "ACTIVO";
        this.fechaCreacion = LocalDateTime.now();
    }

    /** Reemplaza las paradas conservando el orden recibido. */
    public void reemplazarParadas(List<RutaLugar> nuevas) {
        paradas.clear();
        for (int i = 0; i < nuevas.size(); i++) {
            RutaLugar parada = nuevas.get(i);
            parada.asignarOrden(i + 1);
            parada.asignarRuta(this);
            paradas.add(parada);
        }
    }

    public void actualizarMetricas(BigDecimal distanciaKm, int duracionMin) {
        this.distanciaKm = distanciaKm;
        this.duracionMin = duracionMin;
    }

    public void renombrar(String nombre) {
        this.nombre = nombre;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getMunicipio() { return municipio; }
    public BigDecimal getDistanciaKm() { return distanciaKm; }
    public Integer getDuracionMin() { return duracionMin; }
    public String getEstado() { return estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public List<RutaLugar> getParadas() { return paradas; }
}
