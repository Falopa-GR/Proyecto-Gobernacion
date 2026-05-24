package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import model.PublicServer;
import model.VacationPeriod;
import util.JPAUtil;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

/**
 * RF-03 — DAO de Control de Vacaciones
 *
 * El modelo VacationPeriod almacena por año:
 *   - accumulatedDays : días causados ese año (calculados al crear el registro)
 *   - usedDays        : días ya disfrutados ese año
 *   - getPendingDays(): accumulatedDays - usedDays (calculado en el modelo)
 *
 * Este DAO agrega:
 *   1. Guardar con validación (no gastar más días de los disponibles)
 *   2. Buscar historial por servidor
 *   3. Calcular totales acumulados de toda la vida laboral
 *   4. Detectar servidores en deuda (>1 período pendiente) — RF-07 dashboard
 */
public class VacationPeriodDAO extends GenericDao<VacationPeriod> {

    /** Días de vacaciones por año según Ley 909 de 2004 (régimen general). */
    public static final int DIAS_POR_ANIO = 15;

    public VacationPeriodDAO() {
        super(VacationPeriod.class);
    }

    // ─────────────────────────────────────────────────────────────────────
    // GUARDAR CON VALIDACIÓN
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Guarda un período de vacaciones validando que no se gasten más días
     * de los disponibles para ese año.
     *
     * @throws IllegalArgumentException con mensaje claro si hay error
     */
    public void saveWithValidation(VacationPeriod vp) {
        if (vp.getServer()           == null) throw new IllegalArgumentException("El servidor es obligatorio.");
        if (vp.getYear()             == null) throw new IllegalArgumentException("El año del período es obligatorio.");
        if (vp.getAccumulatedDays()  == null) throw new IllegalArgumentException("Los días acumulados son obligatorios.");
        if (vp.getUsedDays()         == null) throw new IllegalArgumentException("Los días utilizados son obligatorios.");
        if (vp.getUsedDays() < 0)             throw new IllegalArgumentException("Los días utilizados no pueden ser negativos.");
        if (vp.getUsedDays() > vp.getAccumulatedDays())
            throw new IllegalArgumentException(
                    "Los días utilizados (" + vp.getUsedDays() + ") no pueden superar "
                            + "los acumulados (" + vp.getAccumulatedDays() + ") para el año " + vp.getYear() + ".");

        // Verificar si ya existe un registro para ese año y servidor
        VacationPeriod existing = findByServerAndYear(vp.getServer(), vp.getYear());
        if (existing != null && !existing.getId().equals(vp.getId())) {
            throw new IllegalArgumentException(
                    "Ya existe un registro de vacaciones para el año " + vp.getYear()
                            + ". Selecciónalo de la tabla para editarlo.");
        }

        this.save(vp);
    }

    // ─────────────────────────────────────────────────────────────────────
    // CONSULTAS
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Historial completo de vacaciones de un servidor, del más reciente al más antiguo.
     */
    public List<VacationPeriod> findByServer(PublicServer server) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<VacationPeriod> q = em.createQuery(
                    "SELECT v FROM VacationPeriod v " +
                            "JOIN FETCH v.server " +
                            "WHERE v.server = :server ORDER BY v.year DESC",
                    VacationPeriod.class);
            q.setParameter("server", server);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Registro de vacaciones de un servidor para un año específico.
     * Retorna null si no existe.
     */
    public VacationPeriod findByServerAndYear(PublicServer server, int year) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            List<VacationPeriod> list = em.createQuery(
                            "SELECT v FROM VacationPeriod v " +
                                    "WHERE v.server = :server AND v.year = :year",
                            VacationPeriod.class)
                    .setParameter("server", server)
                    .setParameter("year",   year)
                    .getResultList();
            return list.isEmpty() ? null : list.get(0);
        } finally {
            em.close();
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // CÁLCULOS DE RESUMEN — RF-03
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Total de días acumulados en toda la vida laboral del servidor
     * sumando todos sus registros de VacationPeriod.
     */
    public int totalAccumulatedDays(PublicServer server) {
        List<VacationPeriod> lista = findByServer(server);
        return lista.stream().mapToInt(v -> v.getAccumulatedDays() != null ? v.getAccumulatedDays() : 0).sum();
    }

    /**
     * Total de días ya disfrutados en toda la vida laboral.
     */
    public int totalUsedDays(PublicServer server) {
        List<VacationPeriod> lista = findByServer(server);
        return lista.stream().mapToInt(v -> v.getUsedDays() != null ? v.getUsedDays() : 0).sum();
    }

    /**
     * Días pendientes totales = acumulados - usados.
     * Un valor > DIAS_POR_ANIO significa que el servidor está en deuda.
     */
    public int totalPendingDays(PublicServer server) {
        return Math.max(0, totalAccumulatedDays(server) - totalUsedDays(server));
    }

    /**
     * Períodos completos pendientes (cada 15 días = 1 período).
     * RF-03: alerta cuando pendingPeriods > 1.
     */
    public int pendingPeriods(PublicServer server) {
        return totalPendingDays(server) / DIAS_POR_ANIO;
    }

    /**
     * Calcula cuántos días debería tener acumulados un servidor
     * basado únicamente en su fecha de ingreso (admissionDate).
     *
     * Útil para pre-llenar el campo "Días acumulados" al crear un nuevo registro.
     * Ej: servidor con 3 años → 3 × 15 = 45 días totales causados.
     */
    public int calcularDiasAcumuladosTotales(PublicServer server) {
        if (server.getAdmissionDate() == null) return 0;
        int anios = Period.between(server.getAdmissionDate(), LocalDate.now()).getYears();
        return anios * DIAS_POR_ANIO;
    }

    /**
     * RF-07: Lista de servidores activos con más de 1 período pendiente.
     * Recibe la lista de servidores activos para evitar una consulta extra.
     *
     * @param servidoresActivos resultado de PublicServerDAO.findAllActive()
     */
    public List<PublicServer> findServersInDebt(List<PublicServer> servidoresActivos) {
        return servidoresActivos.stream()
                .filter(s -> pendingPeriods(s) > 1)
                .toList();
    }

    /**
     * RF-07: Conteo rápido de servidores en deuda para el dashboard.
     */
    public long countServersInDebt(List<PublicServer> servidoresActivos) {
        return findServersInDebt(servidoresActivos).size();
    }
}