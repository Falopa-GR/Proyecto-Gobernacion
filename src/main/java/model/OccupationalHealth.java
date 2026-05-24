package model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "occupational_health")
public class OccupationalHealth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "server_id")
    private PublicServer server;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private RecordType type; // MEDICAL_EVALUATION, ACCIDENT, INCIDENT

    @Column(nullable = false)
    private LocalDate date;

    @Column(length = 50)
    @Enumerated(EnumType.STRING)
    private MedicalConcept concept; // APTO, APTO_CON_RESTRICCIONES, NO_APTO (solo si es evaluación médica)

    @Column(length = 500)
    private String description;

    @Column(length = 500)
    private String medicalRestrictions; // Restricciones médicas relevantes

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PublicServer getServer() { return server; }
    public void setServer(PublicServer server) { this.server = server; }

    public RecordType getType() { return type; }
    public void setType(RecordType type) { this.type = type; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public MedicalConcept getConcept() { return concept; }
    public void setConcept(MedicalConcept concept) { this.concept = concept; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMedicalRestrictions() { return medicalRestrictions; }
    public void setMedicalRestrictions(String medicalRestrictions) { this.medicalRestrictions = medicalRestrictions; }

    // Enums
    public enum RecordType {
        MEDICAL_EVALUATION,
        ACCIDENT,
        INCIDENT
    }

    public enum MedicalConcept {
        APTO,
        APTO_CON_RESTRICCIONES,
        NO_APTO
    }
}