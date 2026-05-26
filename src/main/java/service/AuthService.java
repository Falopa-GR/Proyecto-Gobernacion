package service;

import jakarta.persistence.EntityManager;
import model.User;
import model.User.Role;
import util.JPAUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;

/**
 * Servicio de autenticación y gestión de usuarios.
 *
 * - hash()         : SHA-256 de la contraseña
 * - login()        : valida credenciales, retorna el User o null
 * - createUser()   : crea un nuevo usuario (solo ADMIN puede llamar esto)
 * - seedAdmin()    : crea el usuario admin por defecto si la tabla está vacía
 * - canEdit()      : el rol puede crear/editar registros
 * - canAdmin()     : el rol es ADMIN
 */
public class AuthService {

    /** Usuario actualmente logueado — se guarda al hacer login exitoso. */
    private static User currentUser = null;

    public static User getCurrentUser()     { return currentUser; }
    public static void setCurrentUser(User u){ currentUser = u; }
    public static void logout()             { currentUser = null; }

    // ─── Permisos por rol ────────────────────────────────────────────────
    public static boolean canEdit() {
        return currentUser != null &&
                (currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.GESTOR);
    }

    public static boolean canAdmin() {
        return currentUser != null && currentUser.getRole() == Role.ADMIN;
    }

    // ─── Hash SHA-256 ────────────────────────────────────────────────────
    public static String hash(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(plain.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (Exception e) {
            throw new RuntimeException("Error al hashear contraseña", e);
        }
    }

    // ─── Login ───────────────────────────────────────────────────────────
    /**
     * Valida credenciales contra la BD.
     * @return el User si las credenciales son correctas y el usuario está activo,
     *         null en caso contrario.
     */
    public static User login(String username, String password) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            List<User> result = em.createQuery(
                            "SELECT u FROM User u WHERE u.username = :usr AND u.passwordHash = :pwd AND u.active = true",
                            User.class)
                    .setParameter("usr", username.trim())
                    .setParameter("pwd", hash(password))
                    .getResultList();

            if (result.isEmpty()) return null;
            currentUser = result.get(0);
            return currentUser;
        } finally {
            em.close();
        }
    }

    // ─── Crear usuario ───────────────────────────────────────────────────
    public static void createUser(String username, String password, Role role, String fullName) {
        User u = new User(username.trim(), hash(password), role, fullName.trim());
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(u);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    // ─── Seed: crear admin por defecto si no hay usuarios ────────────────
    /**
     * Llamar desde Main.java al arrancar.
     * Si la tabla users está vacía crea:
     *   usuario: admin | contraseña: admin123 | rol: ADMIN
     */
    public static void seedAdminIfEmpty() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            long count = em.createQuery("SELECT COUNT(u) FROM User u", Long.class).getSingleResult();
            if (count == 0) {
                em.getTransaction().begin();
                em.persist(new User("admin", hash("admin123"), Role.ADMIN, "Administrador"));
                em.persist(new User("gestor", hash("gestor123"), Role.GESTOR, "Gestor Talento Humano"));
                em.persist(new User("consulta", hash("consulta123"), Role.CONSULTA, "Usuario Consulta"));
                em.getTransaction().commit();
                System.out.println("[AuthService] Usuarios por defecto creados: admin/admin123, gestor/gestor123, consulta/consulta123");
            }
        } finally {
            em.close();
        }
    }
}