package dao;

import jakarta.persistence.EntityManager;
import model.PublicServer;
import util.JPAUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * RF-01 — DAO de Planta de Personal
 *
 * Usa solo PostgreSQL (talentoPU)
 */
public class PublicServerDAO extends GenericDao<PublicServer> {

    public PublicServerDAO() {
        super(PublicServer.class);
    }

    // ----------------------------------------------------------------
    // RF-01: Consultar planta activa
    // ----------------------------------------------------------------
    public List<PublicServer> findAllActive() {
        List<PublicServer> result = new ArrayList<>();

        EntityManager em = JPAUtil.getEntityManager();
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

        return result;
    }

    // ----------------------------------------------------------------
    // RF-01: Buscar por cédula
    // ----------------------------------------------------------------
    public PublicServer findByIdNumber(String idNumber) {
        EntityManager em = JPAUtil.getEntityManager();
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
            return null;
        } finally {
            em.close();
        }
    }

    // ----------------------------------------------------------------
    // RF-01: Filtrar por dependencia
    // ----------------------------------------------------------------
    public List<PublicServer> findActiveByDependency(Long dependencyId) {
        List<PublicServer> result = new ArrayList<>();

        EntityManager em = JPAUtil.getEntityManager();
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

        return result;
    }

    // ----------------------------------------------------------------
    // RF-01: Filtrar por tipo de vinculación (corrige nombre de campo)
    // ----------------------------------------------------------------
    public List<PublicServer> findActiveByEmploymentType(String employmentType) {
        List<PublicServer> result = new ArrayList<>();

        EntityManager em = JPAUtil.getEntityManager();
        try {
            result.addAll(em.createQuery(
                            "SELECT s FROM PublicServer s " +
                                    "LEFT JOIN FETCH s.position " +
                                    "LEFT JOIN FETCH s.dependency " +
                                    "WHERE s.active = true AND s.vinculationType = :type " +
                                    "ORDER BY s.lastName",
                            PublicServer.class
                    ).setParameter("type", employmentType)
                    .getResultList());
        } finally {
            em.close();
        }

        return result;
    }

    // ----------------------------------------------------------------
    // RF-01: Búsqueda por nombre o apellido
    // ----------------------------------------------------------------
    public List<PublicServer> findByName(String keyword) {
        List<PublicServer> result = new ArrayList<>();
        String pattern = "%" + keyword.toUpperCase() + "%";

        EntityManager em = JPAUtil.getEntityManager();
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

        return result;
    }

    // ----------------------------------------------------------------
    // RF-01: Dar de baja (retiro lógico)
    // ----------------------------------------------------------------
    public void deactivate(String idNumber) {
        EntityManager em = JPAUtil.getEntityManager();
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
    }

    // ----------------------------------------------------------------
    // RF-07: Conteo para el dashboard
    // ----------------------------------------------------------------
    public long countActive() {
        EntityManager em = JPAUtil.getEntityManager();
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