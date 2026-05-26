package view;

import dao.AdministrativeSituationDAO;
import dao.PublicServerDAO;
import dao.VacationPeriodDAO;
import model.AdministrativeSituation;
import model.PublicServer;
import model.VacationPeriod;
import service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ServerProfileWindow extends JFrame {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PublicServerDAO            serverDAO    = new PublicServerDAO();
    private final AdministrativeSituationDAO situationDAO = new AdministrativeSituationDAO();
    private final VacationPeriodDAO          vacationDAO  = new VacationPeriodDAO();

    private final PublicServer server;

    // ── Constructor por cédula ───────────────────────────────────────────
    public ServerProfileWindow(String idNumber) {
        this(new PublicServerDAO().findByIdNumber(idNumber));
    }

    // ── Constructor por objeto ───────────────────────────────────────────
    public ServerProfileWindow(PublicServer server) {
        this.server = server;

        setTitle("Perfil del Servidor — " +
                (server != null ? server.getFirstName() + " " + server.getLastName() : "No encontrado"));
        setSize(860, 640);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(700, 500));

        if (server == null) {
            JLabel msg = new JLabel("Servidor no encontrado.", SwingConstants.CENTER);
            msg.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            add(msg);
            return;
        }

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(245, 247, 250));
        setContentPane(root);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildTabs(),   BorderLayout.CENTER);
    }

    // ─────────────────────────────────────────────────────────────────────
    // HEADER
    // ─────────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0,0,new Color(17,24,39), getWidth(),0,new Color(30,58,100)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Avatar con iniciales
        JPanel avatar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(99, 102, 241));
                g2.fillOval(0, 0, 74, 74);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 26));
                String ini = initials();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(ini, (74 - fm.stringWidth(ini)) / 2, 74/2 + fm.getAscent()/2 - 2);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(74, 74); }
        };
        avatar.setOpaque(false);
        header.add(avatar, BorderLayout.WEST);

        // Info
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel lblName = new JLabel(server.getFirstName() + " " + server.getLastName());
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblName.setForeground(Color.WHITE);
        info.add(lblName);

        JLabel lblSub = new JLabel(
                "C.C. " + server.getIdNumber()
                        + "   |   " + nvl(server.getVinculationType())
                        + "   |   " + (server.getDependency() != null ? server.getDependency().getName() : "—"));
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(new Color(148, 163, 184));
        info.add(lblSub);
        info.add(Box.createVerticalStrut(8));

        // Badge situación actual — en SwingWorker para no bloquear
        JLabel badge = new JLabel("  Cargando situación...  ");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badge.setOpaque(true);
        badge.setBackground(new Color(71, 85, 105));
        badge.setForeground(Color.WHITE);
        badge.setBorder(new EmptyBorder(4, 10, 4, 10));
        info.add(badge);
        header.add(info, BorderLayout.CENTER);

        new SwingWorker<AdministrativeSituation, Void>() {
            protected AdministrativeSituation doInBackground() {
                return situationDAO.findCurrentByServer(server, LocalDate.now());
            }
            protected void done() {
                try {
                    AdministrativeSituation actual = get();
                    if (actual == null) {
                        badge.setText("    En actividad normal  ");
                        badge.setBackground(new Color(16, 185, 129));
                    } else {
                        badge.setText("  ⚠  " + labelSituation(actual.getType())
                                + " hasta " + actual.getEndDate().format(FMT) + "  ");
                        badge.setBackground(new Color(245, 158, 11));
                    }
                } catch (Exception ex) { badge.setText("  —  "); }
            }
        }.execute();

        // Botón editar
        if (AuthService.canEdit()) {
            JButton btnEdit = new JButton("  Editar");
            btnEdit.setForeground(Color.WHITE);
            btnEdit.setBackground(new Color(99, 102, 241));
            btnEdit.setOpaque(true);
            btnEdit.setBorderPainted(false);
            btnEdit.setFocusPainted(false);
            btnEdit.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnEdit.addActionListener(e -> {
                PublicServerWindow w = new PublicServerWindow();
                try { w.loadServer(server.getIdNumber()); } catch (Exception ignored) {}
                w.setVisible(true);
            });
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            right.setOpaque(false);
            right.add(btnEdit);
            header.add(right, BorderLayout.EAST);
        }

        return header;
    }

    // ─────────────────────────────────────────────────────────────────────
    // TABS
    // ─────────────────────────────────────────────────────────────────────
    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabs.add("  Datos personales", buildTabPersonal());
        tabs.add("  Datos laborales",  buildTabLaboral());
        tabs.add("  Situaciones",      buildTabSituaciones());
        tabs.add("  Vacaciones",       buildTabVacaciones());
        return tabs;
    }

    // ── Pestaña 1: Datos personales ──────────────────────────────────────
    private JPanel buildTabPersonal() {
        // gbRow LOCAL para esta pestaña — no compartido con otras
        int[] row = {0};
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(14, 18, 14, 18));

        addSection(p, row, "Información personal");
        addRow(p, row, "Cédula",              server.getIdNumber());
        addRow(p, row, "Nombre completo",     server.getFirstName() + " " + server.getLastName());
        String edad = "—";
        if (server.getBirthDate() != null) {
            int a = Period.between(server.getBirthDate(), LocalDate.now()).getYears();
            edad = server.getBirthDate().format(FMT) + "  (" + a + " años)";
        }
        addRow(p, row, "Fecha de nacimiento", edad);
        addRow(p, row, "Género",              nvl(server.getGender()));
        addRow(p, row, "Estado civil",        nvl(server.getCivilStatus()));
        addRow(p, row, "Grupo sanguíneo",     nvl(server.getBloodType()));

        addSection(p, row, "Contacto");
        addRow(p, row, "Teléfono", nvl(server.getPhone()));
        addRow(p, row, "Correo",   nvl(server.getEmail()));

        // Relleno para empujar todo hacia arriba
        GridBagConstraints fill = new GridBagConstraints();
        fill.gridx = 0; fill.gridy = row[0]; fill.gridwidth = 2;
        fill.weighty = 1; fill.fill = GridBagConstraints.VERTICAL;
        p.add(Box.createVerticalGlue(), fill);

        return scrollWrap(p);
    }

    // ── Pestaña 2: Datos laborales ───────────────────────────────────────
    private JPanel buildTabLaboral() {
        int[] row = {0};
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(14, 18, 14, 18));

        addSection(p, row, "Vínculo laboral");
        addRow(p, row, "Cargo",        server.getPosition() != null ? server.getPosition().getName() : "—");
        addRow(p, row, "Código cargo", nvl(server.getPositionCode()));
        addRow(p, row, "Dependencia",  server.getDependency() != null ? server.getDependency().getName() : "—");
        addRow(p, row, "Vinculación",  nvl(server.getVinculationType()));

        String ingreso = "—", antiguedad = "—";
        if (server.getAdmissionDate() != null) {
            ingreso    = server.getAdmissionDate().format(FMT);
            int anios  = Period.between(server.getAdmissionDate(), LocalDate.now()).getYears();
            int meses  = Period.between(server.getAdmissionDate(), LocalDate.now()).getMonths();
            antiguedad = anios + " año(s), " + meses + " mes(es)";
        }
        addRow(p, row, "Fecha de ingreso", ingreso);
        addRow(p, row, "Antigüedad",       antiguedad);
        addRow(p, row, "Salario mensual",  server.getMonthlySalary() != null
                ? String.format("$ %,.0f", server.getMonthlySalary()) : "—");
        addRow(p, row, "Estado",           Boolean.TRUE.equals(server.getActive()) ? "Activo ✓" : "Inactivo");

        GridBagConstraints fill = new GridBagConstraints();
        fill.gridx = 0; fill.gridy = row[0]; fill.gridwidth = 2;
        fill.weighty = 1; fill.fill = GridBagConstraints.VERTICAL;
        p.add(Box.createVerticalGlue(), fill);

        return scrollWrap(p);
    }

    // ── Pestaña 3: Situaciones ───────────────────────────────────────────
    private JPanel buildTabSituaciones() {
        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(14, 14, 14, 14));

        // Banner situación actual
        JLabel banner = new JLabel("  Cargando...  ");
        banner.setFont(new Font("Segoe UI", Font.BOLD, 12));
        banner.setOpaque(true);
        banner.setBackground(new Color(71, 85, 105));
        banner.setForeground(Color.WHITE);
        banner.setBorder(new EmptyBorder(8, 14, 8, 14));
        JPanel bannerWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bannerWrap.setOpaque(false);
        bannerWrap.add(banner);
        root.add(bannerWrap, BorderLayout.NORTH);

        // Tabla
        String[] cols = {"Tipo", "Inicio", "Fin", "Días", "Acto administrativo"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        root.add(new JScrollPane(styledTable(model)), BorderLayout.CENTER);

        // Carga en background
        new SwingWorker<List<AdministrativeSituation>, Void>() {
            protected List<AdministrativeSituation> doInBackground() {
                return situationDAO.findByServer(server);
            }
            protected void done() {
                try {
                    List<AdministrativeSituation> lista = get();
                    // Actualizar banner
                    AdministrativeSituation actual = lista.stream()
                            .filter(a -> {
                                LocalDate hoy = LocalDate.now();
                                return a.getStartDate() != null && a.getEndDate() != null
                                        && !hoy.isBefore(a.getStartDate()) && !hoy.isAfter(a.getEndDate());
                            }).findFirst().orElse(null);

                    if (actual == null) {
                        banner.setText("    Situación actual: En actividad normal  ");
                        banner.setBackground(new Color(209, 250, 229));
                        banner.setForeground(new Color(6, 78, 59));
                    } else {
                        banner.setText("   Situación actual: " + labelSituation(actual.getType())
                                + "  del " + actual.getStartDate().format(FMT)
                                + "  al "  + actual.getEndDate().format(FMT) + "  ");
                        banner.setBackground(new Color(254, 243, 199));
                        banner.setForeground(new Color(92, 48, 9));
                    }

                    // Llenar tabla
                    for (AdministrativeSituation a : lista) {
                        long dias = (a.getStartDate() != null && a.getEndDate() != null)
                                ? a.getStartDate().until(a.getEndDate()).getDays() + 1 : 0;
                        model.addRow(new Object[]{
                                labelSituation(a.getType()),
                                a.getStartDate() != null ? a.getStartDate().format(FMT) : "—",
                                a.getEndDate()   != null ? a.getEndDate().format(FMT)   : "—",
                                dias,
                                nvl(a.getAdministrativeAct())
                        });
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();

        return root;
    }

    // ── Pestaña 4: Vacaciones ────────────────────────────────────────────
    private JPanel buildTabVacaciones() {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(14, 14, 14, 14));

        // Cards de resumen — se calculan en background
        JLabel lAccum = cardVal("...", new Color(59, 130, 246));
        JLabel lUsado = cardVal("...", new Color(16, 185, 129));
        JLabel lPend  = cardVal("...", new Color(245, 158, 11));
        JLabel lPer   = cardVal("...", new Color(107, 114, 128));

        JPanel summary = new JPanel(new GridLayout(1, 4, 12, 0));
        summary.setOpaque(false);
        summary.add(vacCard("Días acumulados",    lAccum));
        summary.add(vacCard("Días disfrutados",   lUsado));
        summary.add(vacCard("Días pendientes",    lPend));
        summary.add(vacCard("Períodos pendientes",lPer));
        root.add(summary, BorderLayout.NORTH);

        JLabel alerta = new JLabel(" ");
        alerta.setFont(new Font("Segoe UI", Font.BOLD, 12));
        alerta.setOpaque(true);
        alerta.setBorder(new EmptyBorder(8, 14, 8, 14));
        root.add(alerta, BorderLayout.CENTER);

        // Tabla
        String[] cols = {"Año", "Acumulados", "Usados", "Pendientes", "Última vacación", "Notas"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        root.add(new JScrollPane(styledTable(model)), BorderLayout.SOUTH);

        // Carga en background
        new SwingWorker<int[], Void>() {
            List<VacationPeriod> lista;
            protected int[] doInBackground() {
                lista = vacationDAO.findByServer(server);
                int acum  = vacationDAO.totalAccumulatedDays(server);
                int usado = vacationDAO.totalUsedDays(server);
                int pend  = vacationDAO.totalPendingDays(server);
                int per   = vacationDAO.pendingPeriods(server);
                return new int[]{acum, usado, pend, per};
            }
            protected void done() {
                try {
                    int[] v = get();
                    lAccum.setText(String.valueOf(v[0]));
                    lUsado.setText(String.valueOf(v[1]));
                    lPend.setText(String.valueOf(v[2]));
                    lPer.setText(String.valueOf(v[3]));
                    // Color dinámico según deuda
                    if (v[3] > 1) {
                        lPend.setForeground(new Color(239, 68, 68));
                        lPer.setForeground(new Color(239, 68, 68));
                        alerta.setText("  Adeuda " + v[3] + " período(s). Debe programar salida.  ");
                        alerta.setBackground(new Color(254, 226, 226));
                        alerta.setForeground(new Color(153, 27, 27));
                    } else {
                        alerta.setText("    Sin deuda de vacaciones  ");
                        alerta.setBackground(new Color(209, 250, 229));
                        alerta.setForeground(new Color(6, 78, 59));
                    }
                    for (VacationPeriod vp : lista) {
                        model.addRow(new Object[]{
                                vp.getYear(), vp.getAccumulatedDays(), vp.getUsedDays(),
                                vp.getPendingDays(),
                                vp.getLastVacationDate() != null ? vp.getLastVacationDate().format(FMT) : "—",
                                nvl(vp.getNotes())
                        });
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();

        return root;
    }

    // ─────────────────────────────────────────────────────────────────────
    // HELPERS DE LAYOUT — sin MigLayout
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Título de sección con línea inferior — ocupa las 2 columnas.
     * USA GridBagConstraints correctamente (no "span 2" de MigLayout).
     */
    private void addSection(JPanel p, int[] row, String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(new Color(99, 102, 241));
        l.setBorder(new MatteBorder(0, 0, 2, 0, new Color(99, 102, 241)));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = row[0];
        gc.gridwidth = 2;                         // ← correcto para GridBagLayout
        gc.fill   = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.insets = new Insets(row[0] == 0 ? 0 : 14, 0, 6, 0);
        p.add(l, gc);
        row[0]++;
    }

    /** Fila de etiqueta + valor, separadas por una línea tenue */
    private void addRow(JPanel p, int[] row, String label, String value) {
        GridBagConstraints gl = new GridBagConstraints();
        gl.gridx = 0; gl.gridy = row[0];
        gl.insets = new Insets(4, 0, 4, 24);
        gl.anchor = GridBagConstraints.WEST;

        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(100, 116, 139));
        lbl.setPreferredSize(new Dimension(160, 20));
        p.add(lbl, gl);

        GridBagConstraints gv = new GridBagConstraints();
        gv.gridx = 1; gv.gridy = row[0];
        gv.insets = new Insets(4, 0, 4, 0);
        gv.anchor = GridBagConstraints.WEST;
        gv.fill   = GridBagConstraints.HORIZONTAL;
        gv.weightx = 1;

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        val.setForeground(new Color(17, 24, 39));
        p.add(val, gv);
        row[0]++;

        // Separador
        GridBagConstraints gs = new GridBagConstraints();
        gs.gridx = 0; gs.gridy = row[0];
        gs.gridwidth = 2; gs.fill = GridBagConstraints.HORIZONTAL;
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(229, 231, 235));
        p.add(sep, gs);
        row[0]++;
    }

    private JPanel scrollWrap(JPanel p) {
        JScrollPane sp = new JScrollPane(p);
        sp.setBorder(null);
        sp.getViewport().setOpaque(false);
        sp.setOpaque(false);
        JPanel w = new JPanel(new BorderLayout());
        w.setOpaque(false);
        w.add(sp);
        return w;
    }

    private JLabel cardVal(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 28));
        l.setForeground(color);
        return l;
    }

    private JPanel vacCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235)),
                new EmptyBorder(12, 12, 12, 12)));
        JLabel lt = new JLabel(title);
        lt.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lt.setForeground(new Color(107, 114, 128));
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(lt,         BorderLayout.SOUTH);
        return card;
    }

    private JTable styledTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setRowHeight(28);
        t.setShowGrid(false);
        t.setFillsViewportHeight(true);
        t.setSelectionBackground(new Color(226, 232, 240));
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.getTableHeader().setBackground(new Color(248, 250, 252));
        t.getTableHeader().setReorderingAllowed(false);
        return t;
    }

    private String initials() {
        String fn = server.getFirstName() != null ? server.getFirstName() : "?";
        String ln = server.getLastName()  != null ? server.getLastName()  : "?";
        return ("" + fn.charAt(0) + ln.charAt(0)).toUpperCase();
    }

    private String nvl(String s) { return s != null ? s : "—"; }

    private String labelSituation(AdministrativeSituation.SituationType t) {
        if (t == null) return "—";
        return switch (t) {
            case VACATION            -> "Vacaciones";
            case PERMISSION_1_DAY    -> "Permiso 1 día";
            case PERMISSION_2_3_DAYS -> "Permiso 2-3 días";
            case LICENSE_PAID        -> "Licencia remunerada";
            case LICENSE_UNPAID      -> "Licencia no remunerada";
            case MATERNITY           -> "Licencia maternidad";
            case PATERNITY           -> "Licencia paternidad";
            case ILLNESS             -> "Licencia enfermedad";
            case ASSIGNMENT          -> "Encargo";
            case TRANSFER            -> "Traslado";
            case COMMISSION          -> "Comisión";
        };
    }
}