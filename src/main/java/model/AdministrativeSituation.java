package model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "administrative_situations")
public class AdministrativeSituation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "server_id")
    private PublicServer server;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private SituationType type; // VACATION, PERMISSION_1_DAY, PERMISSION_2_3_DAYS, LICENSE_PAID, LICENSE_UNPAID, MATERNITY, PATERNITY, ILLNESS, ASSIGNMENT, TRANSFER, COMMISSION

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(length = 255)
    private String administrativeAct; // Acto administrativo que respalda

    @Column(length = 500)
    private String notes;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PublicServer getServer() { return server; }
    public void setServer(PublicServer server) { this.server = server; }

    public SituationType getType() { return type; }
    public void setType(SituationType type) { this.type = type; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getAdministrativeAct() { return administrativeAct; }
    public void setAdministrativeAct(String administrativeAct) { this.administrativeAct = administrativeAct; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    // Enum para tipo de situación
    public enum SituationType {
        VACATION,
        PERMISSION_1_DAY,
        PERMISSION_2_3_DAYS,
        LICENSE_PAID,
        LICENSE_UNPAID,
        MATERNITY,
        PATERNITY,
        ILLNESS,
        ASSIGNMENT,
        TRANSFER,
        COMMISSION
    }
}