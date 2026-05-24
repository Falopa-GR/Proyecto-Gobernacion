package model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "public_servers")
public class PublicServer {

    @Id
    @Column(length = 20)
    private String idNumber; // Cédula

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column
    private LocalDate birthDate;

    @Column(length = 10)
    private String gender; // M, F

    @Column(length = 50)
    private String civilStatus; // Soltero, Casado, Divorciado, Viudo

    @Column(length = 5)
    private String bloodType; // A, B, AB, O (+ Rh)

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    // Relaciones con datos laborales
    @ManyToOne
    @JoinColumn(name = "dependency_id")
    private Dependency dependency;

    @ManyToOne
    @JoinColumn(name = "position_id")
    private Position position;

    @Column(length = 50)
    private String positionCode; // Código del cargo

    @Column(length = 50)
    private String vinculationType; // Tipo de vinculación (planta, contrato, etc.)

    @Column
    private LocalDate admissionDate;

    @Column
    private Double monthlySalary;

    // Relaciones inversas (colecciones)
    @OneToMany(mappedBy = "server", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AdministrativeSituation> administrativeSituations;

    @OneToMany(mappedBy = "server", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VacationPeriod> vacationPeriods;

    @OneToMany(mappedBy = "server", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WelfareRecord> welfareRecords;

    @OneToMany(mappedBy = "server", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OccupationalHealth> occupationalHealth;

    @OneToMany(mappedBy = "server", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PerformanceEvaluation> performanceEvaluations;

    @Column
    private Boolean active = true; // Para controlar servidores activos

    // Getters y Setters (recomendación: usar IDE para generar todos)
    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getCivilStatus() { return civilStatus; }
    public void setCivilStatus(String civilStatus) { this.civilStatus = civilStatus; }

    public String getBloodType() { return bloodType; }
    public void setBloodType(String bloodType) { this.bloodType = bloodType; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Dependency getDependency() { return dependency; }
    public void setDependency(Dependency dependency) { this.dependency = dependency; }

    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }

    public String getPositionCode() { return positionCode; }
    public void setPositionCode(String positionCode) { this.positionCode = positionCode; }

    public String getVinculationType() { return vinculationType; }
    public void setVinculationType(String vinculationType) { this.vinculationType = vinculationType; }

    public LocalDate getAdmissionDate() { return admissionDate; }
    public void setAdmissionDate(LocalDate admissionDate) { this.admissionDate = admissionDate; }

    public Double getMonthlySalary() { return monthlySalary; }
    public void setMonthlySalary(Double monthlySalary) { this.monthlySalary = monthlySalary; }

    public List<AdministrativeSituation> getAdministrativeSituations() { return administrativeSituations; }
    public void setAdministrativeSituations(List<AdministrativeSituation> administrativeSituations) { this.administrativeSituations = administrativeSituations; }

    public List<VacationPeriod> getVacationPeriods() { return vacationPeriods; }
    public void setVacationPeriods(List<VacationPeriod> vacationPeriods) { this.vacationPeriods = vacationPeriods; }

    public List<WelfareRecord> getWelfareRecords() { return welfareRecords; }
    public void setWelfareRecords(List<WelfareRecord> welfareRecords) { this.welfareRecords = welfareRecords; }

    public List<OccupationalHealth> getOccupationalHealth() { return occupationalHealth; }
    public void setOccupationalHealth(List<OccupationalHealth> occupationalHealth) { this.occupationalHealth = occupationalHealth; }

    public List<PerformanceEvaluation> getPerformanceEvaluations() { return performanceEvaluations; }
    public void setPerformanceEvaluations(List<PerformanceEvaluation> performanceEvaluations) { this.performanceEvaluations = performanceEvaluations; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}