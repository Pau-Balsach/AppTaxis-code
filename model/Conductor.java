package model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "conductores")
public class Conductor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id; 

    private String matricula;
    private String nombre;
    
    @Column(name = "cond_admin", columnDefinition = "uuid")
    private UUID cond_admin; 

    @OneToMany(mappedBy = "conductor", cascade = CascadeType.ALL)
    private List<Viaje> listaViajes = new ArrayList<>();

    public Conductor() {
    }

    
    public Conductor(int id, String matricula, String nombre, UUID cond_admin) {
        this.id = id;
        this.matricula = matricula;
        this.nombre = nombre;
        this.cond_admin = cond_admin;
    }
    

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMatricula() { return matricula; }
    public String getNombre() { return nombre; }

    public void setMatricula(String matricula) { this.matricula = matricula; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public UUID getCond_admin() { return cond_admin; }
    public void setCond_admin(UUID cond_admin) { this.cond_admin = cond_admin; }

    public List<Viaje> getListaViajes() { return listaViajes; }
    public void setListaViajes(List<Viaje> listaViajes) { this.listaViajes = listaViajes; }
}