package model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "positions")
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name; // ej: "Analista", "Directivo"

    @Column(length = 50)
    private String grade; // ej: "A1", "A2"

    @Column(length = 255)
    private String description;

    // Relación: Un cargo puede ser ocupado por muchos servidores
    @OneToMany(mappedBy = "position", cascade = CascadeType.ALL)
    private List<PublicServer> servers;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<PublicServer> getServers() { return servers; }
    public void setServers(List<PublicServer> servers) { this.servers = servers; }
}