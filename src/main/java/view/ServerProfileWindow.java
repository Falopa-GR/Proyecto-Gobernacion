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

/**
 * ServerProfileWindow — estandarizado con UITheme (paleta verde #318c45)
 */
public class ServerProfileWindow extends JFrame {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PublicServerDAO            serverDAO    = new PublicServerDAO();
    private final AdministrativeSituationDAO situationDAO = new AdministrativeSituationDAO();
    private final VacationPeriodDAO          vacationDAO  = new VacationPeriodDAO();

    private final PublicServer server;

    public ServerProfileWindow(String idNumber) {
        this(new PublicServerDAO().findByIdNumber(idNumber));
    }

    public ServerProfileWindow(PublicServer server) {
        this.server = server;
        UITheme.applyGlobal();

        setTitle("Perfil del Servidor — " +
                (server != null ? server.getFirstName() + " " + server.getLastName() : "No encontrado"));
        setSize(880, 660);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(700, 520));

        if (server == null) {
            JLabel msg = new JLabel("Servidor no encontrado.", SwingConstants.CENTER);
            msg.setFont(UITheme.FONT_H2);
            msg.setForeground(UITheme.TEXT_SUB);
            add(msg);
            return;
        }

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_SOFT);
        setContentPane(root);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildTabs(),   BorderLayout.CENTER);
    }

    // ─────────────────────────────────────────────────────────────────────
    // HEADER — gradiente verde institucional
    // ─────────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, UITheme.SIDEBAR_BG, getWidth(), 0, UITheme.PRIMARY_DARK));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setBorder(new EmptyBorder(20, 24, 20, 24));

        // ── Avatar con iniciales ──
        JPanel avatar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PRIMARY);
                g2.fillOval(0, 0, 74, 74);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 26));
                String ini = initials();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(ini, (74 - fm.stringWidth(ini)) / 2, 74 / 2 + fm.getAscent() / 2 - 2);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(74, 74); }
        };
        avatar.setOpaque(false);
        header.add(avatar, BorderLayout.WEST);

        // ── Info ──
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel lblName = new JLabel(server.getFirstName() + " " + server.getLastName());
        lblName.setFont(UITheme.FONT_H1);
        lblName.setForeground(Color.WHITE);
        info.add(lblName);

        JLabel lblSub = new JLabel(
                "C.C. " + server.getIdNumber()
                        + "   |   " + nvl(server.getVinculationType())
                        + "   |   " + (server.getDependency() != null ? server.getDependency().getName() : "—"));
        lblSub.setFont(UITheme.FONT_BODY);
        lblSub.setForeground(new Color(0xbb, 0xe5, 0xc4));
        info.add(lblSub);
        info.add(Box.createVerticalStrut(8));

        // Badge situación — cargado en background
        JLabel badge = new JLabel("  Cargando...  ");
        badge.setFont(UITheme.FONT_BADGE);
        badge.setOpaque(true);
        badge.setBackground(UITheme.SIDEBAR_HOVER);
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
                        badge.setBackground(UITheme.PRIMARY);
                        badge.setForeground(Color.WHITE);
                    } else {
                        badge.setText("  ⚠  " + labelSituation(actual.getType())
                                + " hasta " + actual.getEndDate().format(FMT) + "  ");
                        badge.setBackground(UITheme.WARNING);
                        badge.setForeground(Color.WHITE);
                    }
                } catch (Exception ex) { badge.setText("  —  "); }
            }
        }.execute();

        // ── Botón editar ──
        if (AuthService.canEdit()) {
            JButton btnEdit = UITheme.primaryButton("  ✎ Editar");
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
        tabs.setFont(UITheme.FONT_BODY);
        tabs.setBackground(UITheme.BG);
        tabs.add("  Datos personales",  buildTabPersonal());
        tabs.add("  Datos laborales",   buildTabLaboral());
        tabs.add("  Situaciones",       buildTabSituaciones());
        tabs.add("  Vacaciones",        buildTabVacaciones());
        return tabs;
    }

    // ── Pestaña 1: Datos personales ──────────────────────────────────────
    private JPanel buildTabPersonal() {
        int[] row = {0};
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UITheme.BG);
        p.setBorder(new EmptyBorder(UITheme.PAD, 20, UITheme.PAD, 20));

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
        p.setBackground(UITheme.BG);
        p.setBorder(new EmptyBorder(UITheme.PAD, 20, UITheme.PAD, 20));

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
        JPanel root = new JPanel(new BorderLayout(0, UITheme.PAD_SM));
        root.setBackground(UITheme.BG);
        root.setBorder(new EmptyBorder(UITheme.PAD, UITheme.PAD, UITheme.PAD, UITheme.PAD));

        // Banner
        JLabel banner = new JLabel("  Cargando...  ");
        banner.setFont(UITheme.FONT_H3);
        banner.setOpaque(true);
        banner.setBackground(UITheme.SIDEBAR_HOVER);
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
        root.add(UITheme.tableScroll(UITheme.styledTable(model)), BorderLayout.CENTER);

        new SwingWorker<List<AdministrativeSituation>, Void>() {
            protected List<AdministrativeSituation> doInBackground() {
                return situationDAO.findByServer(server);
            }
            protected void done() {
                try {
                    List<AdministrativeSituation> lista = get();
                    AdministrativeSituation actual = lista.stream()
                            .filter(a -> {
                                LocalDate hoy = LocalDate.now();
                                return a.getStartDate() != null && a.getEndDate() != null
                                        && !hoy.isBefore(a.getStartDate()) && !hoy.isAfter(a.getEndDate());
                            }).findFirst().orElse(null);

                    if (actual == null) {
                        banner.setText("    Situación actual: En actividad normal  ");
                        banner.setBackground(UITheme.PRIMARY_LIGHT);
                        banner.setForeground(UITheme.PRIMARY_DARK);
                    } else {
                        banner.setText("   Situación actual: " + labelSituation(actual.getType())
                                + "  del " + actual.getStartDate().format(FMT)
                                + "  al "  + actual.getEndDate().format(FMT) + "  ");
                        banner.setBackground(UITheme.WARNING_LIGHT);
                        banner.setForeground(new Color(0x92, 0x40, 0x09));
                    }

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
        JPanel root = new JPanel(new BorderLayout(0, UITheme.PAD));
        root.setBackground(UITheme.BG);
        root.setBorder(new EmptyBorder(UITheme.PAD, UITheme.PAD, UITheme.PAD, UITheme.PAD));

        // Cards de resumen
        JLabel lAccum = new JLabel("...");
        JLabel lUsado = new JLabel("...");
        JLabel lPend  = new JLabel("...");
        JLabel lPer   = new JLabel("...");

        JPanel summary = new JPanel(new GridLayout(1, 4, UITheme.PAD, 0));
        summary.setOpaque(false);
        summary.add(makeVacCard("Días acumulados",     lAccum, UITheme.PRIMARY));
        summary.add(makeVacCard("Días disfrutados",    lUsado, UITheme.ACCENT));
        summary.add(makeVacCard("Días pendientes",     lPend,  UITheme.WARNING));
        summary.add(makeVacCard("Períodos pendientes", lPer,   UITheme.TEXT_SUB));
        root.add(summary, BorderLayout.NORTH);

        // Alerta
        JLabel alerta = new JLabel(" ");
        alerta.setFont(UITheme.FONT_H3);
        alerta.setOpaque(true);
        alerta.setBorder(new EmptyBorder(8, 14, 8, 14));

        // Tabla
        String[] cols = {"Año", "Acumulados", "Usados", "Pendientes", "Última vacación", "Notas"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = UITheme.styledTable(model);

        JPanel center = new JPanel(new BorderLayout(0, UITheme.PAD_SM));
        center.setOpaque(false);
        center.add(alerta, BorderLayout.NORTH);
        center.add(UITheme.tableScroll(table), BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        new SwingWorker<int[], Void>() {
            List<VacationPeriod> lista;
            protected int[] doInBackground() {
                lista = vacationDAO.findByServer(server);
                return new int[]{
                        vacationDAO.totalAccumulatedDays(server),
                        vacationDAO.totalUsedDays(server),
                        vacationDAO.totalPendingDays(server),
                        vacationDAO.pendingPeriods(server)
                };
            }
            protected void done() {
                try {
                    int[] v = get();
                    styleMetricLabel(lAccum, String.valueOf(v[0]), UITheme.PRIMARY);
                    styleMetricLabel(lUsado, String.valueOf(v[1]), UITheme.ACCENT);
                    styleMetricLabel(lPend,  String.valueOf(v[2]), v[3] > 1 ? UITheme.ERROR : UITheme.WARNING);
                    styleMetricLabel(lPer,   String.valueOf(v[3]), v[3] > 1 ? UITheme.ERROR : UITheme.TEXT_SUB);

                    if (v[3] > 1) {
                        alerta.setText("  Adeuda " + v[3] + " período(s). Debe programar salida.  ");
                        alerta.setBackground(UITheme.ERROR_LIGHT);
                        alerta.setForeground(new Color(0x99, 0x1b, 0x1b));
                    } else {
                        alerta.setText("    Sin deuda de vacaciones  ");
                        alerta.setBackground(UITheme.SUCCESS_LIGHT);
                        alerta.setForeground(UITheme.PRIMARY_DARK);
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
    // HELPERS DE LAYOUT
    // ─────────────────────────────────────────────────────────────────────
    private void addSection(JPanel p, int[] row, String text) {
        JLabel l = UITheme.sectionLabel(text);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = row[0];
        gc.gridwidth = 2;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.insets = new Insets(row[0] == 0 ? 0 : 14, 0, 6, 0);
        p.add(l, gc);
        row[0]++;
    }

    private void addRow(JPanel p, int[] row, String label, String value) {
        GridBagConstraints gl = new GridBagConstraints();
        gl.gridx = 0; gl.gridy = row[0];
        gl.insets = new Insets(5, 0, 5, 24);
        gl.anchor = GridBagConstraints.WEST;

        JLabel lbl = UITheme.fieldLabel(label + ":");
        lbl.setPreferredSize(new Dimension(160, 20));
        p.add(lbl, gl);

        GridBagConstraints gv = new GridBagConstraints();
        gv.gridx = 1; gv.gridy = row[0];
        gv.insets = new Insets(5, 0, 5, 0);
        gv.anchor = GridBagConstraints.WEST;
        gv.fill = GridBagConstraints.HORIZONTAL;
        gv.weightx = 1;

        JLabel val = UITheme.bodyLabel(value);
        p.add(val, gv);
        row[0]++;

        // Separador
        GridBagConstraints gs = new GridBagConstraints();
        gs.gridx = 0; gs.gridy = row[0];
        gs.gridwidth = 2; gs.fill = GridBagConstraints.HORIZONTAL;
        p.add(UITheme.separator(), gs);
        row[0]++;
    }

    private JPanel scrollWrap(JPanel p) {
        JScrollPane sp = new JScrollPane(p);
        sp.setBorder(null);
        sp.getViewport().setBackground(UITheme.BG);
        sp.getVerticalScrollBar().setUI(new UITheme.MinimalScrollBarUI());
        JPanel w = new JPanel(new BorderLayout());
        w.setBackground(UITheme.BG);
        w.add(sp);
        return w;
    }

    private JPanel makeVacCard(String title, JLabel valueLabel, Color accent) {
        UITheme.Card card = new UITheme.Card(UITheme.PAD);
        card.setLayout(new BorderLayout(0, 6));
        JLabel lt = UITheme.fieldLabel(title);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(accent);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(lt,         BorderLayout.SOUTH);
        return card;
    }

    private void styleMetricLabel(JLabel l, String text, Color color) {
        l.setText(text);
        l.setForeground(color);
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