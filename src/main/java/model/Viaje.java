package model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "viajes")
public class Viaje {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    private LocalDate dia;

    @Column(name = "dia_fin")
    private LocalDate diaFin;

    private LocalTime hora;

    @Column(name = "hora_finalizacion")
    private LocalTime horaFinalizacion;

    private String puntodejada;
    private String puntorecogida;
    private String telefonocliente;

    // ── Coordenadas de recogida ───────────────────────────────────────────────
    @Column(name = "lat_recogida")
    private Double latRecogida;

    @Column(name = "lng_recogida")
    private Double lngRecogida;

    // ── Coordenadas de dejada ─────────────────────────────────────────────────
    @Column(name = "lat_dejada")
    private Double latDejada;

    @Column(name = "lng_dejada")
    private Double lngDejada;

    @ManyToOne
    @JoinColumn(name = "conductor_id")
    private Conductor conductor;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    public Viaje() {
        this.id = UUID.randomUUID();
    }

    // ── Getters / Setters existentes ──────────────────────────────────────────

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public LocalDate getDia() { return dia; }
    public void setDia(LocalDate dia) { this.dia = dia; }

    public LocalDate getDiaFin() { return diaFin; }
    public void setDiaFin(LocalDate diaFin) { this.diaFin = diaFin; }

    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }

    public LocalTime getHoraFinalizacion() { return horaFinalizacion; }
    public void setHoraFinalizacion(LocalTime horaFinalizacion) { this.horaFinalizacion = horaFinalizacion; }

    public String getPuntodejada() { return puntodejada; }
    public void setPuntodejada(String puntodejada) { this.puntodejada = puntodejada; }

    public String getPuntorecogida() { return puntorecogida; }
    public void setPuntorecogida(String puntorecogida) { this.puntorecogida = puntorecogida; }

    public String getTelefonocliente() { return telefonocliente; }
    public void setTelefonocliente(String telefonocliente) { this.telefonocliente = telefonocliente; }

    public Conductor getConductor() { return conductor; }
    public void setConductor(Conductor conductor) { this.conductor = conductor; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    // ── Getters / Setters coordenadas (nuevos) ────────────────────────────────

    public Double getLatRecogida() { return latRecogida; }
    public void setLatRecogida(Double latRecogida) { this.latRecogida = latRecogida; }

    public Double getLngRecogida() { return lngRecogida; }
    public void setLngRecogida(Double lngRecogida) { this.lngRecogida = lngRecogida; }

    public Double getLatDejada() { return latDejada; }
    public void setLatDejada(Double latDejada) { this.latDejada = latDejada; }

    public Double getLngDejada() { return lngDejada; }
    public void setLngDejada(Double lngDejada) { this.lngDejada = lngDejada; }

    // ── Utilidades ────────────────────────────────────────────────────────────

    public boolean cruzaMedianoche() {
        return diaFin != null && diaFin.isAfter(dia);
    }
}