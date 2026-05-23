package dao;

import jakarta.persistence.EntityManager;
import model.PublicServer;
import util.JPAUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * RF-01 — DAO de Planta de Personal
 *
 * Extiende GenericDao (que ya tiene save, findAll, delete)
 * y agrega las consultas específicas del módulo de Planta.
 *
 * Patrón de doble BD: cada consulta se ejecuta en PostgreSQL
 * y MySQL, y los resultados se combinan (igual que en tu
 * GenericDao existente).
 */
public class PublicServerDAO extends GenericDao<PublicServer> {

    public PublicServerDAO() {
        super(PublicServer.class);
    }

    // ----------------------------------------------------------------
    // RF-01: Consultar planta activa
    // ----------------------------------------------------------------

    /**
     * Retorna SOLO los servidores con active = true.
     * RF-01: "El sistema debe permitir consultar la planta actual
     * (servidores activos) en cualquier momento."
     *
     * Usa JOIN FETCH para traer Position y Dependency en la misma
     * consulta, evitando LazyInitializationException al mostrarlos
     * en la tabla JavaFX después de cerrar el EntityManager.
     */
    public List<PublicServer> findAllActive() {
        List<PublicServer> result = new ArrayList<>();

        // PostgreSQL
        EntityManager em = JPAUtil.getEntityManagerPostgres();
        try {
            result.addAll(em.createQuery(
                    "SELECT s FROM PublicServer s " +
                            "LEFT JOIN FETCH s.position " +
                            "LEFT JOIN FETCH s.dependency " +
                            "WHERE s.active = true " +
                            "ORDER BY s.lastName, s.firstName",
                    PublicServer.class
            ).getResultList());
        } finally {
            em.close();
        }

        // MySQL
        EntityManager em2 = JPAUtil.getEntityManagerMysql();
        try {
            result.addAll(em2.createQuery(
                    "SELECT s FROM PublicServer s " +
                            "LEFT JOIN FETCH s.position " +
                            "LEFT JOIN FETCH s.dependency " +
                            "WHERE s.active = true " +
                            "ORDER BY s.lastName, s.firstName",
                    PublicServer.class
            ).getResultList());
        } finally {
            em2.close();
        }

        return result;
    }

    // ----------------------------------------------------------------
    // RF-01: Buscar por cédula
    // ----------------------------------------------------------------

    /**
     * Busca un servidor por su número de cédula.
     * Retorna null si no existe.
     * Se consulta primero PostgreSQL; si no está, se busca en MySQL.
     */
    public PublicServer findByIdNumber(String idNumber) {
        // PostgreSQL primero
        EntityManager em = JPAUtil.getEntityManagerPostgres();
        try {
            List<PublicServer> list = em.createQuery(
                            "SELECT s FROM PublicServer s " +
                                    "LEFT JOIN FETCH s.position " +
                                    "LEFT JOIN FETCH s.dependency " +
                                    "WHERE s.idNumber = :idNumber",
                            PublicServer.class
                    ).setParameter("idNumber", idNumber)
                    .getResultList();

            if (!list.isEmpty()) return list.get(0);
        } finally {
            em.close();
        }

        // MySQL como respaldo
        EntityManager em2 = JPAUtil.getEntityManagerMysql();
        try {
            List<PublicServer> list2 = em2.createQuery(
                            "SELECT s FROM PublicServer s " +
                                    "LEFT JOIN FETCH s.position " +
                                    "LEFT JOIN FETCH s.dependency " +
                                    "WHERE s.idNumber = :idNumber",
                            PublicServer.class
                    ).setParameter("idNumber", idNumber)
                    .getResultList();

            return list2.isEmpty() ? null : list2.get(0);
        } finally {
            em2.close();
        }
    }

    // ----------------------------------------------------------------
    // RF-01: Filtrar por dependencia
    // ----------------------------------------------------------------

    /**
     * Retorna los servidores activos de una dependencia específica.
     * Útil para el dashboard de jefes de área (RF-07).
     *
     * @param dependencyId ID de la dependencia a filtrar
     */
    public List<PublicServer> findActiveByDependency(Long dependencyId) {
        List<PublicServer> result = new ArrayList<>();

        EntityManager em = JPAUtil.getEntityManagerPostgres();
        try {
            result.addAll(em.createQuery(
                            "SELECT s FROM PublicServer s " +
                                    "LEFT JOIN FETCH s.position " +
                                    "LEFT JOIN FETCH s.dependency " +
                                    "WHERE s.active = true AND s.dependency.id = :depId " +
                                    "ORDER BY s.lastName",
                            PublicServer.class
                    ).setParameter("depId", dependencyId)
                    .getResultList());
        } finally {
            em.close();
        }

        EntityManager em2 = JPAUtil.getEntityManagerMysql();
        try {
            result.addAll(em2.createQuery(
                            "SELECT s FROM PublicServer s " +
                                    "LEFT JOIN FETCH s.position " +
                                    "LEFT JOIN FETCH s.dependency " +
                                    "WHERE s.active = true AND s.dependency.id = :depId " +
                                    "ORDER BY s.lastName",
                            PublicServer.class
                    ).setParameter("depId", dependencyId)
                    .getResultList());
        } finally {
            em2.close();
        }

        return result;
    }

    // ----------------------------------------------------------------
    // RF-01: Filtrar por tipo de vinculación
    // ----------------------------------------------------------------

    /**
     * Retorna servidores activos filtrados por tipo de vinculación.
     * Ej: findActiveByEmploymentType("Planta") trae solo los de planta.
     *
     * @param employmentType "Planta" | "Contrato" | "Provisional" | "Libre nombramiento"
     */
    public List<PublicServer> findActiveByEmploymentType(String employmentType) {
        List<PublicServer> result = new ArrayList<>();

        EntityManager em = JPAUtil.getEntityManagerPostgres();
        try {
            result.addAll(em.createQuery(
                            "SELECT s FROM PublicServer s " +
                                    "LEFT JOIN FETCH s.position " +
                                    "LEFT JOIN FETCH s.dependency " +
                                    "WHERE s.active = true AND s.employmentType = :type " +
                                    "ORDER BY s.lastName",
                            PublicServer.class
                    ).setParameter("type", employmentType)
                    .getResultList());
        } finally {
            em.close();
        }

        EntityManager em2 = JPAUtil.getEntityManagerMysql();
        try {
            result.addAll(em2.createQuery(
                            "SELECT s FROM PublicServer s " +
                                    "LEFT JOIN FETCH s.position " +
                                    "LEFT JOIN FETCH s.dependency " +
                                    "WHERE s.active = true AND s.employmentType = :type " +
                                    "ORDER BY s.lastName",
                            PublicServer.class
                    ).setParameter("type", employmentType)
                    .getResultList());
        } finally {
            em2.close();
        }

        return result;
    }

    // ----------------------------------------------------------------
    // RF-01: Búsqueda por nombre o apellido
    // ----------------------------------------------------------------

    /**
     * Búsqueda libre por nombre o apellido (insensible a mayúsculas).
     * El % en el LIKE actúa como comodín: busca "JUAN" en cualquier
     * posición del nombre o apellido.
     *
     * Uso: findByName("juan") → devuelve Juan García, Ana Juanita, etc.
     *
     * @param keyword texto a buscar (sin %)
     */
    public List<PublicServer> findByName(String keyword) {
        List<PublicServer> result = new ArrayList<>();
        String pattern = "%" + keyword.toUpperCase() + "%";

        EntityManager em = JPAUtil.getEntityManagerPostgres();
        try {
            result.addAll(em.createQuery(
                            "SELECT s FROM PublicServer s " +
                                    "LEFT JOIN FETCH s.position " +
                                    "LEFT JOIN FETCH s.dependency " +
                                    "WHERE s.active = true " +
                                    "AND (UPPER(s.firstName) LIKE :kw OR UPPER(s.lastName) LIKE :kw) " +
                                    "ORDER BY s.lastName",
                            PublicServer.class
                    ).setParameter("kw", pattern)
                    .getResultList());
        } finally {
            em.close();
        }

        EntityManager em2 = JPAUtil.getEntityManagerMysql();
        try {
            result.addAll(em2.createQuery(
                            "SELECT s FROM PublicServer s " +
                                    "LEFT JOIN FETCH s.position " +
                                    "LEFT JOIN FETCH s.dependency " +
                                    "WHERE s.active = true " +
                                    "AND (UPPER(s.firstName) LIKE :kw OR UPPER(s.lastName) LIKE :kw) " +
                                    "ORDER BY s.lastName",
                            PublicServer.class
                    ).setParameter("kw", pattern)
                    .getResultList());
        } finally {
            em2.close();
        }

        return result;
    }

    // ----------------------------------------------------------------
    // RF-01: Dar de baja (retiro lógico)
    // ----------------------------------------------------------------

    /**
     * Marca un servidor como inactivo (active = false) sin borrarlo.
     * RF-01: el historial debe conservarse para reportes y auditoría.
     *
     * NUNCA se debe borrar físicamente un servidor de la BD.
     * Usa delete() de GenericDao solo para datos de prueba.
     *
     * @param idNumber cédula del servidor a retirar
     */
    public void deactivate(String idNumber) {
        // PostgreSQL
        EntityManager em = JPAUtil.getEntityManagerPostgres();
        try {
            em.getTransaction().begin();
            em.createQuery(
                            "UPDATE PublicServer s SET s.active = false " +
                                    "WHERE s.idNumber = :idNumber"
                    ).setParameter("idNumber", idNumber)
                    .executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }

        // MySQL
        EntityManager em2 = JPAUtil.getEntityManagerMysql();
        try {
            em2.getTransaction().begin();
            em2.createQuery(
                            "UPDATE PublicServer s SET s.active = false " +
                                    "WHERE s.idNumber = :idNumber"
                    ).setParameter("idNumber", idNumber)
                    .executeUpdate();
            em2.getTransaction().commit();
        } catch (Exception e) {
            if (em2.getTransaction().isActive()) em2.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            em2.close();
        }
    }

    // ----------------------------------------------------------------
    // RF-07: Conteo para el dashboard
    // ----------------------------------------------------------------

    /**
     * Retorna el número total de servidores activos en la planta.
     * Usado por el dashboard (RF-07) sin cargar todos los objetos.
     */
    public long countActive() {
        EntityManager em = JPAUtil.getEntityManagerPostgres();
        try {
            return em.createQuery(
                    "SELECT COUNT(s) FROM PublicServer s WHERE s.active = true",
                    Long.class
            ).getSingleResult();
        } finally {
            em.close();
        }
    }
}