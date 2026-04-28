package model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "admins")
public class Admin {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    private String email;

    @Transient
    private String accessToken;

    @OneToMany(mappedBy = "cond_admin")
    private List<Conductor> listaConductores = new ArrayList<>();

    public Admin() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public List<Conductor> getListaConductores() { return listaConductores; }
    public void setListaConductores(List<Conductor> lista) { this.listaConductores = lista; }
}