package model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "vacation_periods")
public class VacationPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "server_id")
    private PublicServer server;

    @Column(nullable = false)
    private Integer year; // Año del período

    @Column(nullable = false)
    private Integer accumulatedDays; // Días acumulados

    @Column(nullable = false)
    private Integer usedDays; // Días utilizados

    @Column
    private LocalDate lastVacationDate; // Última vez que disfrutó vacaciones

    @Column(length = 500)
    private String notes;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PublicServer getServer() { return server; }
    public void setServer(PublicServer server) { this.server = server; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Integer getAccumulatedDays() { return accumulatedDays; }
    public void setAccumulatedDays(Integer accumulatedDays) { this.accumulatedDays = accumulatedDays; }

    public Integer getUsedDays() { return usedDays; }
    public void setUsedDays(Integer usedDays) { this.usedDays = usedDays; }

    public LocalDate getLastVacationDate() { return lastVacationDate; }
    public void setLastVacationDate(LocalDate lastVacationDate) { this.lastVacationDate = lastVacationDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    // Método auxiliar para calcular días pendientes
    public Integer getPendingDays() {
        return accumulatedDays - usedDays;
    }
}