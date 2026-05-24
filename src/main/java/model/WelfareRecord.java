package model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "welfare_records")
public class WelfareRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "server_id")
    private PublicServer server;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private IncentiveType type; // BIRTHDAY, SERVICE_TIME, RECOGNITION, TRAINING

    @Column(nullable = false)
    private LocalDate date;

    @Column(length = 255)
    private String description;

    @Column(length = 500)
    private String details;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PublicServer getServer() { return server; }
    public void setServer(PublicServer server) { this.server = server; }

    public IncentiveType getType() { return type; }
    public void setType(IncentiveType type) { this.type = type; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    // Enum para tipo de incentivo
    public enum IncentiveType {
        BIRTHDAY,        // Celebra la Vida
        SERVICE_TIME,    // Tiempo de servicio
        RECOGNITION,     // Reconocimientos
        TRAINING         // Capacitaciones
    }
}
