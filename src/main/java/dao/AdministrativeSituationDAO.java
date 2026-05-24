package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import model.AdministrativeSituation;
import model.AdministrativeSituation.SituationType;
import model.PublicServer;
import service.OverlappingSituationException;
import util.JPAUtil;
import view.AdministrativeSituationWindow;
import java.time.LocalDate;
import java.util.List;

/**
 * RF-02 — DAO de Situaciones Administrativas
 *
 * Sobre la versión anterior se agregan:
 *   - findActiveToday()        → todos los servidores con situación activa hoy (RF-07)
 *   - countActiveToday()       → conteo rápido para el dashboard (RF-07)
 *   - countByTypeToday()       → cuántos están en vacaciones / permiso hoy (RF-07)
 *   - findCurrentByServer()    → situación activa de un servidor en una fecha dada
 *
 * La lógica existente (findOverlapping, saveWithValidation, findByServer)
 * no se modificó: sigue funcionando exactamente igual.
 */
public class AdministrativeSituationDAO extends GenericDao<AdministrativeSituation> {

    public AdministrativeSituationDAO() {
        super(AdministrativeSituation.class);
    }

    // ─────────────────────────────────────────────────────────────────────
    // LÓGICA EXISTENTE (sin cambios)
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Busca situaciones del servidor que se solapen con el rango dado.
     * Retorna lista vacía si no hay conflicto.
     */
    public List<AdministrativeSituation> findOverlapping(
            PublicServer server, LocalDate start, LocalDate end) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<AdministrativeSituation> q = em.createQuery(
                    "SELECT a FROM AdministrativeSituation a " +
                            "WHERE a.server = :server " +
                            "AND a.startDate <= :endDate AND a.endDate >= :startDate",
                    AdministrativeSituation.class);
            q.setParameter("server",    server);
            q.setParameter("startDate", start);
            q.setParameter("endDate",   end);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * RF-02 — REGLA CRÍTICA: guarda solo si no hay solapamiento de fechas.
     * Lanza OverlappingSituationException si existe conflicto.
     */
    public void saveWithValidation(AdministrativeSituation situation) {
        if (situation == null
                || situation.getServer()    == null
                || situation.getStartDate() == null
                || situation.getEndDate()   == null) {
            throw new IllegalArgumentException("Situación, servidor y fechas son obligatorios.");
        }
        if (situation.getEndDate().isBefore(situation.getStartDate())) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la de inicio.");
        }
        if (situation.getType() == null) {
            throw new IllegalArgumentException("El tipo de situación es obligatorio.");
        }

        List<AdministrativeSituation> overlaps = findOverlapping(
                situation.getServer(), situation.getStartDate(), situation.getEndDate());

        // Si es edición, excluir el propio registro
        overlaps.removeIf(s -> s.getId() != null && s.getId().equals(situation.getId()));

        if (!overlaps.isEmpty()) {
            throw new OverlappingSituationException(
                    "Existe otra situación administrativa que se solapa con las fechas indicadas.\n" +
                            "El servidor ya tiene registrada: " + overlaps.get(0).getType().name() +
                            " del " + overlaps.get(0).getStartDate() + " al " + overlaps.get(0).getEndDate());
        }

        this.save(situation);
    }

    /**
     * Historial completo de un servidor, del más reciente al más antiguo.
     */
    public List<AdministrativeSituation> findByServer(PublicServer server) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<AdministrativeSituation> q = em.createQuery(
                    "SELECT a FROM AdministrativeSituation a " +
                            "JOIN FETCH a.server " +
                            "WHERE a.server = :server ORDER BY a.startDate DESC",
                    AdministrativeSituation.class);
            q.setParameter("server", server);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // MÉTODOS NUEVOS para RF-07 Dashboard
    // ─────────────────────────────────────────────────────────────────────

    /**
     * RF-07: Situación activa de un servidor en una fecha específica.
     * Retorna null si el servidor está en actividad normal.
     *
     * Uso: findCurrentByServer(server, LocalDate.now())
     */
    public AdministrativeSituation findCurrentByServer(PublicServer server, LocalDate date) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            List<AdministrativeSituation> list = em.createQuery(
                            "SELECT a FROM AdministrativeSituation a " +
                                    "WHERE a.server = :server " +
                                    "AND a.startDate <= :date AND a.endDate >= :date",
                            AdministrativeSituation.class)
                    .setParameter("server", server)
                    .setParameter("date", date)
                    .getResultList();
            return list.isEmpty() ? null : list.get(0);
        } finally {
            em.close();
        }
    }

    /**
     * RF-07: Todas las situaciones activas hoy (de todos los servidores).
     * Usado por el dashboard para mostrar quiénes están ausentes.
     */
    public List<AdministrativeSituation> findActiveToday() {
        LocalDate hoy = LocalDate.now();
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT a FROM AdministrativeSituation a " +
                                    "JOIN FETCH a.server " +
                                    "WHERE a.startDate <= :hoy AND a.endDate >= :hoy " +
                                    "ORDER BY a.type, a.server.lastName",
                            AdministrativeSituation.class)
                    .setParameter("hoy", hoy)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * RF-07: Conteo total de situaciones activas hoy.
     * Más eficiente que findActiveToday().size() porque no carga objetos.
     */
    public long countActiveToday() {
        LocalDate hoy = LocalDate.now();
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT COUNT(a) FROM AdministrativeSituation a " +
                                    "WHERE a.startDate <= :hoy AND a.endDate >= :hoy",
                            Long.class)
                    .setParameter("hoy", hoy)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    /**
     * RF-07: Conteo de servidores con un tipo específico de situación hoy.
     *
     * Ejemplos de uso:
     *   countByTypeToday(SituationType.VACATION)         → cuántos en vacaciones
     *   countByTypeToday(SituationType.PERMISSION_1_DAY) → cuántos en permiso de 1 día
     */
    public long countByTypeToday(SituationType type) {
        LocalDate hoy = LocalDate.now();
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT COUNT(a) FROM AdministrativeSituation a " +
                                    "WHERE a.type = :tipo " +
                                    "AND a.startDate <= :hoy AND a.endDate >= :hoy",
                            Long.class)
                    .setParameter("tipo", type)
                    .setParameter("hoy", hoy)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    /**
     * RF-07: Conteo de permisos activos hoy (suma de 1 día y 2-3 días).
     * Atajo conveniente para el dashboard.
     */
    public long countPermissionsToday() {
        LocalDate hoy = LocalDate.now();
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT COUNT(a) FROM AdministrativeSituation a " +
                                    "WHERE a.type IN (:p1, :p2) " +
                                    "AND a.startDate <= :hoy AND a.endDate >= :hoy",
                            Long.class)
                    .setParameter("p1",  SituationType.PERMISSION_1_DAY)
                    .setParameter("p2",  SituationType.PERMISSION_2_3_DAYS)
                    .setParameter("hoy", hoy)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }
}