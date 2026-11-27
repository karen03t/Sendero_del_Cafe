
package co.edu.autonoma.turismo.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lugar")
public class Lugar {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, length = 100)
    private String categoria;

    @Column(nullable = false, length = 255)
    private String direccion;

    @Column(nullable = false, length = 100)
    private String municipio;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitud;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitud;

    @Column(nullable = false, length = 10)
    private String estado = "ACTIVO";

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "operador_creacion_id", nullable = false)
    private String operadorCreacionId;

    protected Lugar() {
    }

    public Lugar(
            String id,
            String nombre,
            String descripcion,
            String categoria,
            String direccion,
            String municipio,
            BigDecimal latitud,
            BigDecimal longitud,
            String operadorCreacionId) {

        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.direccion = direccion;
        this.municipio = municipio;
        this.latitud = latitud;
        this.longitud = longitud;
        this.operadorCreacionId = operadorCreacionId;
        this.estado = "ACTIVO";
    }

    @PrePersist
    protected void antesDeGuardar() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void antesDeActualizar() {
        fechaActualizacion = LocalDateTime.now();
    }

    public void actualizar(
            String nombre,
            String descripcion,
            String categoria,
            String direccion,
            String municipio,
            BigDecimal latitud,
            BigDecimal longitud) {

        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.direccion = direccion;
        this.municipio = municipio;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public void inactivar() {
        this.estado = "INACTIVO";
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getCategoria() { return categoria; }
    public String getDireccion() { return direccion; }
    public String getMunicipio() { return municipio; }
    public BigDecimal getLatitud() { return latitud; }
    public BigDecimal getLongitud() { return longitud; }
    public String getEstado() { return estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public String getOperadorCreacionId() { return operadorCreacionId; }
}
