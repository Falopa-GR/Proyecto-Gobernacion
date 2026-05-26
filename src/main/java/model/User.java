package model;

import jakarta.persistence.*;

/**
 * Modelo de usuario del sistema con roles.
 *
 * Roles:
 *   ADMIN      → acceso total: crear usuarios, ver todo, exportar
 *   GESTOR     → puede registrar y editar servidores, situaciones, vacaciones
 *   CONSULTA   → solo lectura: ver planta, historial, exportar reportes
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    /** Contraseña almacenada como hash SHA-256 (nunca en texto plano). */
    @Column(nullable = false, length = 64)
    private String passwordHash;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false)
    private Boolean active = true;

    public enum Role {
        ADMIN,    // Administrador total
        GESTOR,   // Gestión operativa
        CONSULTA  // Solo lectura
    }

    public User() {}

    public User(String username, String passwordHash, Role role, String fullName) {
        this.username     = username;
        this.passwordHash = passwordHash;
        this.role         = role;
        this.fullName     = fullName;
    }

    // Getters y setters
    public Long getId()                  { return id; }
    public String getUsername()          { return username; }
    public void setUsername(String u)    { this.username = u; }
    public String getPasswordHash()      { return passwordHash; }
    public void setPasswordHash(String p){ this.passwordHash = p; }
    public Role getRole()                { return role; }
    public void setRole(Role r)          { this.role = r; }
    public String getFullName()          { return fullName; }
    public void setFullName(String n)    { this.fullName = n; }
    public Boolean getActive()           { return active; }
    public void setActive(Boolean a)     { this.active = a; }

    @Override public String toString()   { return fullName + " (" + username + ")"; }
}