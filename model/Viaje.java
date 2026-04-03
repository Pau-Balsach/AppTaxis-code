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
    private UUID id; // PK tipo UUID según tu imagen

    private LocalDate dia;
    private LocalTime hora;
    private String puntodejada;
    private String puntorecogida;
    private String telefonocliente;

    @ManyToOne
    @JoinColumn(name = "conductor_id") 
    private Conductor conductor;

    public Viaje() {
        this.id = UUID.randomUUID(); 
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public LocalDate getDia() { return dia; }
    public void setDia(LocalDate dia) { this.dia = dia; }

    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }

    public String getPuntodejada() { return puntodejada; }
    public void setPuntodejada(String puntodejada) { this.puntodejada = puntodejada; }

    public String getPuntorecogida() { return puntorecogida; }
    public void setPuntorecogida(String puntorecogida) { this.puntorecogida = puntorecogida; }

    public String getTelefonocliente() { return telefonocliente; }
    public void setTelefonocliente(String telefonocliente) { this.telefonocliente = telefonocliente; }

    public Conductor getConductor() { return conductor; }
    public void setConductor(Conductor conductor) { this.conductor = conductor; }
}