package model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "dependencies")
public class Dependency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name; // ej: "Recursos Humanos", "Finanzas"

    @Column(length = 255)
    private String description;

    // Relación: Una dependencia tiene muchos servidores
    @OneToMany(mappedBy = "dependency", cascade = CascadeType.ALL)
    private List<PublicServer> servers;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<PublicServer> getServers() { return servers; }
    public void setServers(List<PublicServer> servers) { this.servers = servers; }
}