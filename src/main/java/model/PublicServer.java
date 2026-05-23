package model;

import java.time.LocalDate;
import jakarta.persistence.*;

/**
 * Entidad central del sistema de Talento Humano.
 *
 * Campos marcados con el RF que los requiere:
 *   - Datos personales básicos ........... RF-01
 *   - Datos laborales (cargo, dependencia) RF-01
 *   - active, vacationDaysPerYear ........ RF-02, RF-03
 *   - Perfil sociodemográfico ............. RF-05
 */
@Entity
public class PublicServer {

    // =========================================================
    // IDENTIFICACIÓN
    // =========================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Cédula de ciudadanía — debe ser única en todo el sistema. */
    @Column(unique = true, nullable = false)
    private String idNumber;

    // =========================================================
    // DATOS PERSONALES — RF-01
    // =========================================================

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private LocalDate birthDate;
    private String gender;           // Masculino / Femenino / No binario / Prefiero no decir
    private String maritalStatus;    // Soltero/a, Casado/a, Unión libre, Divorciado/a, Viudo/a
    private String bloodType;        // A+, A-, B+, B-, AB+, AB-, O+, O-

    private String personalEmail;
    private String institutionalEmail;
    private String phone;

    // RF-05: dirección para perfil sociodemográfico
    private String address;

    // =========================================================
    // DATOS LABORALES — RF-01
    // =========================================================

    /**
     * Cargo asignado al servidor.
     * Se relaciona con Position, que ya contiene código y grado.
     * FetchType.LAZY: Hibernate no carga el cargo hasta que se acceda a él,
     * evitando consultas innecesarias al listar muchos servidores.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private Position position;

    /**
     * Dependencia a la que pertenece el servidor.
     * Relación directa para facilitar filtros por área.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dependency_id")
    private Dependency dependency;

    /** Tipo de vinculación: Planta, Contrato, Provisional, Libre nombramiento. */
    private String employmentType;

    /** Fecha de ingreso a la entidad — base para calcular vacaciones (RF-03). */
    private LocalDate dateOfEntry;

    /** Asignación mensual en pesos colombianos. */
    private Double monthlySalary;

    /**
     * Indica si el servidor está activo en la planta actual.
     * false = retirado, en comisión permanente, etc.
     * Usado en RF-01 para "consultar planta activa" y en RF-07 para reportes.
     */
    @Column(nullable = false)
    private Boolean active = true;

    /**
     * Días de vacaciones por año según su régimen laboral.
     * Por defecto 15 (régimen general colombiano Ley 909).
     * Puede ser 30 para cargos con régimen especial.
     */
    @Column(nullable = false)
    private Integer vacationDaysPerYear = 15;

    // =========================================================
    // PERFIL SOCIODEMOGRÁFICO — RF-05 (Seguridad y Salud)
    // =========================================================

    /** Nivel de formación: Bachiller, Técnico, Tecnólogo, Profesional, Especialista, etc. */
    private String educationLevel;

    /** Número de personas a cargo (hijos, padres, etc.). */
    private Integer numberOfDependents;

    /** Tipo de vivienda: Propia, Arrendada, Familiar, Otro. */
    private String housingType;

    /** Estrato socioeconómico 1–6. */
    private Integer socioEconStrata;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public PublicServer() {
    }

    // =========================================================
    // GETTERS Y SETTERS
    // =========================================================

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

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

    public String getMaritalStatus() { return maritalStatus; }

    public void setMaritalStatus(String maritalStatus) { this.maritalStatus = maritalStatus; }

    public String getBloodType() { return bloodType; }

    public void setBloodType(String bloodType) { this.bloodType = bloodType; }

    public String getPersonalEmail() { return personalEmail; }

    public void setPersonalEmail(String personalEmail) { this.personalEmail = personalEmail; }

    public String getInstitutionalEmail() { return institutionalEmail; }

    public void setInstitutionalEmail(String institutionalEmail) {
        this.institutionalEmail = institutionalEmail;
    }

    public String getPhone() { return phone; }

    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }

    public void setAddress(String address) { this.address = address; }

    public Position getPosition() { return position; }

    public void setPosition(Position position) { this.position = position; }

    public Dependency getDependency() { return dependency; }

    public void setDependency(Dependency dependency) { this.dependency = dependency; }

    public String getEmploymentType() { return employmentType; }

    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }

    public LocalDate getDateOfEntry() { return dateOfEntry; }

    public void setDateOfEntry(LocalDate dateOfEntry) { this.dateOfEntry = dateOfEntry; }

    public Double getMonthlySalary() { return monthlySalary; }

    public void setMonthlySalary(Double monthlySalary) { this.monthlySalary = monthlySalary; }

    public Boolean getActive() { return active; }

    public void setActive(Boolean active) { this.active = active; }

    public Integer getVacationDaysPerYear() { return vacationDaysPerYear; }

    public void setVacationDaysPerYear(Integer vacationDaysPerYear) {
        this.vacationDaysPerYear = vacationDaysPerYear;
    }

    public String getEducationLevel() { return educationLevel; }

    public void setEducationLevel(String educationLevel) { this.educationLevel = educationLevel; }

    public Integer getNumberOfDependents() { return numberOfDependents; }

    public void setNumberOfDependents(Integer numberOfDependents) {
        this.numberOfDependents = numberOfDependents;
    }

    public String getHousingType() { return housingType; }

    public void setHousingType(String housingType) { this.housingType = housingType; }

    public Integer getSocioEconStrata() { return socioEconStrata; }

    public void setSocioEconStrata(Integer socioEconStrata) {
        this.socioEconStrata = socioEconStrata;
    }

    // =========================================================
    // MÉTODO CALCULADO — no se persiste en la BD
    // =========================================================

    /**
     * Calcula los años de servicio desde la fecha de ingreso hasta hoy.
     * Útil para RF-03 (cálculo de vacaciones) y RF-04 (incentivo por tiempo de servicio).
     *
     * @Transient le indica a Hibernate que NO cree una columna para este método.
     */
    @Transient
    public int getYearsOfService() {
        if (dateOfEntry == null) return 0;
        return java.time.Period.between(dateOfEntry, LocalDate.now()).getYears();
    }

    /**
     * Nombre completo — útil para mostrar en tablas y reportes.
     */
    @Transient
    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return getFullName() + " (" + idNumber + ")";
    }
}