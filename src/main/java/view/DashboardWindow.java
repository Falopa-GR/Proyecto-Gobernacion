package view;

import dao.AdministrativeSituationDAO;
import dao.PublicServerDAO;
import dao.VacationPeriodDAO;
import model.AdministrativeSituation;
import model.AdministrativeSituation.SituationType;
import model.PublicServer;
import model.User;
import service.AuthService;
import service.ExportService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * RF-07 — Dashboard principal (hub de la aplicación)
 *
 * Mantiene el diseño visual original (sidebar oscuro, cards, animaciones hover).
 * Cambios sobre la versión anterior:
 *   1. Cards de Inicio: datos reales de la BD (no hardcodeados)
 *   2. Sección "Situaciones Hoy": tabla con todos los servidores ausentes
 *   3. Sección "Vacaciones Deuda": alertas RF-03
 *   4. Sección "Historial": búsqueda por servidor + tabla situaciones
 *   5. Sección "Reportes": botones de exportar a Excel y PDF
 *   6. Sidebar conecta RF-01 y RF-02 existentes; RF-03 si existe VacationWindow
 *   7. Los datos se cargan en background (SwingWorker) para no bloquear la UI
 */
public class DashboardWindow extends JFrame {

    // ── Sidebar buttons ────────────────────────────────────────────────
    private SidebarButton btnInicio;
    private SidebarButton btnSituaciones;
    private SidebarButton btnVacaciones;
    private SidebarButton btnHistorial;
    private SidebarButton btnReportes;
    private SidebarButton btnPlanta;

    private JPanel contentPanel;

    // ── Búsqueda global ────────────────────────────────────────────────
    private JTextField      searchField;
    private JPopupMenu      searchPopup;
    private javax.swing.Timer searchDebounceTimer;

    // ── DAOs ──────────────────────────────────────────────────────────
    private final PublicServerDAO            serverDAO    = new PublicServerDAO();
    private final AdministrativeSituationDAO situationDAO = new AdministrativeSituationDAO();
    private final VacationPeriodDAO          vacationDAO  = new VacationPeriodDAO();

    // ── Cards del inicio (referencias para actualizar) ─────────────────
    private JLabel valActivos;
    private JLabel valVacaciones;
    private JLabel valPermisos;
    private JLabel valDeudaVac;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private User currentUser;

    // ─────────────────────────────────────────────────────────────────────
    public DashboardWindow(User user) {
        this.currentUser = user;
        AuthService.setCurrentUser(user);
        setTitle("Talento Humano — Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(900, 600));

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);

        add(createSidebar(),  BorderLayout.WEST);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(245, 247, 250));
        main.setBorder(new EmptyBorder(16, 16, 16, 16));
        main.add(createHeader(),    BorderLayout.NORTH);
        main.add(contentPanel,      BorderLayout.CENTER);
        add(main, BorderLayout.CENTER);

        // Mostrar inicio y cargar datos reales
        showInicio();
    }

    // ─────────────────────────────────────────────────────────────────────
    // SIDEBAR
    // ─────────────────────────────────────────────────────────────────────
    private JPanel createSidebar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(240, 0));
        panel.setBackground(new Color(17, 24, 39));

        // Logo Gobernación de Boyacá
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(panel.getBackground());
        top.setBorder(new EmptyBorder(12, 12, 8, 12));

        JLabel logoSidebar = new JLabel();
        logoSidebar.setHorizontalAlignment(SwingConstants.CENTER);
        try {
            java.net.URL logoUrl = getClass().getClassLoader().getResource("logo_boyaca.png");
            if (logoUrl != null) {
                ImageIcon raw = new ImageIcon(logoUrl);
                // En el sidebar: 200x116 (sidebar es 240px, con padding queda 216px disponibles)
                Image scaled = raw.getImage().getScaledInstance(200, 116, Image.SCALE_SMOOTH);
                logoSidebar.setIcon(new ImageIcon(scaled));
            }
        } catch (Exception ignored) {}
        top.add(logoSidebar, BorderLayout.CENTER);

        // Línea separadora bajo el logo
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(55, 65, 81));
        top.add(sep, BorderLayout.SOUTH);
        panel.add(top, BorderLayout.NORTH);

        // Menú
        JPanel menu = new JPanel();
        menu.setBackground(panel.getBackground());
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(new EmptyBorder(8, 8, 8, 8));

        btnInicio      = new SidebarButton("Inicio",             "🏠");
        btnSituaciones = new SidebarButton("Situaciones Hoy",    "📋");
        btnVacaciones  = new SidebarButton("Alertas Vacaciones", "🏖");
        btnHistorial   = new SidebarButton("Historial",          "📜");
        btnReportes    = new SidebarButton("Reportes",           "📊");
        btnPlanta      = new SidebarButton("Planta de Personal", "👥");

        menu.add(btnInicio);
        menu.add(btnSituaciones);
        menu.add(btnVacaciones);
        menu.add(btnHistorial);
        menu.add(btnReportes);
        menu.add(Box.createVerticalStrut(10));
        menu.add(btnPlanta);
        menu.add(Box.createVerticalGlue());

        btnInicio.addActionListener(e      -> showInicio());
        btnSituaciones.addActionListener(e -> showSituacionesHoy());
        btnVacaciones.addActionListener(e  -> showAlertasVacaciones());
        btnHistorial.addActionListener(e   -> showHistorial());
        btnReportes.addActionListener(e    -> showReportes());
        btnPlanta.addActionListener(e -> {
            setActive(btnPlanta);
            SwingUtilities.invokeLater(() -> new PublicServerWindow().setVisible(true));
        });

        panel.add(menu, BorderLayout.CENTER);

        // Accesos directos RF-02 y RF-03 en la parte inferior
        JPanel bottom = new JPanel();
        bottom.setBackground(panel.getBackground());
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBorder(new EmptyBorder(0, 8, 16, 8));

        JButton btnGoSit = makeBottomLink("  Nueva situación administrativa",
                e -> new AdministrativeSituationWindow().setVisible(true));
        JButton btnGoVac = makeBottomLink("  Registrar vacaciones",
                e -> openVacationWindow());
        bottom.add(btnGoSit);
        bottom.add(btnGoVac);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private JButton makeBottomLink(String text, ActionListener al) {
        JButton btn = new JButton(text);
        btn.setForeground(new Color(148, 163, 184));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.addActionListener(al);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setForeground(Color.WHITE); }
            public void mouseExited(MouseEvent e)  { btn.setForeground(new Color(148, 163, 184)); }
        });
        return btn;
    }

    private void setActive(SidebarButton active) {
        for (SidebarButton b : new SidebarButton[]{btnInicio, btnSituaciones,
                btnVacaciones, btnHistorial, btnReportes, btnPlanta})
            b.setActive(false);
        active.setActive(true);
    }

    private void openVacationWindow() {
        try {
            Class<?> c = Class.forName("view.VacationWindow");
            JFrame w = (JFrame) c.getDeclaredConstructor().newInstance();
            w.setVisible(true);
        } catch (ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "Módulo de Vacaciones aún en desarrollo.",
                    "Próximamente", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // HEADER (búsqueda global — sin cambios sobre el original)
    // ─────────────────────────────────────────────────────────────────────
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 12, 0));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(340, 36));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219)),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.putClientProperty("JTextField.placeholderText", "Buscar servidor por nombre o cédula...");
        left.add(searchField);
        header.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        JButton btnRefresh = new JButton("  Actualizar");
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRefresh.addActionListener(e -> refreshDashboardCards());
        right.add(btnRefresh);

        JLabel avatar = new JLabel("  " + (currentUser != null ? currentUser.getFullName() : "Usuario"));
        avatar.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Badge de rol
        String roleTxt = currentUser != null ? switch(currentUser.getRole()){
            case ADMIN   -> "ADMIN";
            case GESTOR  -> "GESTOR";
            case CONSULTA-> "CONSULTA";
        } : "";
        Color roleColor = currentUser != null && currentUser.getRole() == User.Role.ADMIN
                ? new Color(239,68,68)
                : currentUser != null && currentUser.getRole() == User.Role.GESTOR
                  ? new Color(245,158,11) : new Color(107,114,128);
        JLabel roleBadge = new JLabel("  " + roleTxt + "  ");
        roleBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        roleBadge.setOpaque(true);
        roleBadge.setBackground(roleColor);
        roleBadge.setForeground(Color.WHITE);
        roleBadge.setBorder(BorderFactory.createEmptyBorder(2,6,2,6));

        JButton btnLogout = new JButton("Salir");
        btnLogout.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> {
            AuthService.logout();
            dispose();
            new LoginWindow().setVisible(true);
        });

        right.add(avatar);
        right.add(roleBadge);
        right.add(btnLogout);
        header.add(right, BorderLayout.EAST);

        // Debounce search
        searchPopup = new JPopupMenu();
        searchPopup.setFocusable(false);
        searchDebounceTimer = new javax.swing.Timer(300, e -> performSearch());
        searchDebounceTimer.setRepeats(false);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { searchDebounceTimer.restart(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { searchDebounceTimer.restart(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { searchDebounceTimer.restart(); }
        });

        return header;
    }

    // ─────────────────────────────────────────────────────────────────────
    // SECCIÓN: INICIO — cards con datos reales
    // ─────────────────────────────────────────────────────────────────────
    private void showInicio() {
        setActive(btnInicio);
        contentPanel.removeAll();

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setOpaque(false);

        // ── Cards superiores ──
        JPanel cards = new JPanel(new GridLayout(1, 4, 16, 0));
        cards.setOpaque(false);

        valActivos   = new JLabel("...");
        valVacaciones= new JLabel("...");
        valPermisos  = new JLabel("...");
        valDeudaVac  = new JLabel("...");

        cards.add(createLiveCard("Servidores Activos",    valActivos,    "👥", new Color(59, 130, 246)));
        cards.add(createLiveCard("En Vacaciones Hoy",     valVacaciones, "🏖", new Color(16, 185, 129)));
        cards.add(createLiveCard("Permisos Hoy",          valPermisos,   "📋", new Color(245, 158, 11)));
        cards.add(createLiveCard("Deuda de Vacaciones",   valDeudaVac,   "⚠", new Color(239, 68, 68)));
        root.add(cards, BorderLayout.NORTH);

        // ── Gráfico ausentismo ──
        JPanel middle = new JPanel(new GridBagLayout());
        middle.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(0, 0, 0, 16);
        gbc.fill    = GridBagConstraints.BOTH;
        gbc.weightx = 0.65; gbc.weighty = 1;
        gbc.gridx = 0;

        CardPanel chartCard = new CardPanel();
        chartCard.setLayout(new BorderLayout(0, 8));
        JLabel chartTitle = new JLabel("Ausentismo últimos 12 meses");
        chartTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        chartCard.add(chartTitle, BorderLayout.NORTH);
        chartCard.add(new AusentismoChartPanel(), BorderLayout.CENTER);
        middle.add(chartCard, gbc);

        gbc.gridx = 1; gbc.weightx = 0.35; gbc.insets = new Insets(0, 0, 0, 0);
        JPanel rightCol = new JPanel(new GridLayout(3, 1, 0, 12));
        rightCol.setOpaque(false);
        rightCol.add(createMiniCard("Módulo RF-01",  "Planta de Personal",        () -> new PublicServerWindow().setVisible(true)));
        rightCol.add(createMiniCard("Módulo RF-02",  "Situaciones Administrativas",() -> new AdministrativeSituationWindow().setVisible(true)));
        rightCol.add(createMiniCard("Módulo RF-03",  "Control de Vacaciones",     this::openVacationWindow));
        middle.add(rightCol, gbc);

        root.add(middle, BorderLayout.CENTER);

        contentPanel.add(root, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();

        // Cargar datos reales en background
        refreshDashboardCards();
    }

    /** Carga los 4 valores de las cards en un SwingWorker para no bloquear la UI */
    private void refreshDashboardCards() {
        new SwingWorker<long[], Void>() {
            @Override
            protected long[] doInBackground() {
                long activos    = serverDAO.countActive();
                long vacHoy     = situationDAO.countByTypeToday(SituationType.VACATION);
                long permHoy    = situationDAO.countPermissionsToday();
                long deuda      = vacationDAO.countServersInDebt(serverDAO.findAllActive());
                return new long[]{activos, vacHoy, permHoy, deuda};
            }
            @Override
            protected void done() {
                try {
                    long[] v = get();
                    if (valActivos    != null) valActivos.setText(String.valueOf(v[0]));
                    if (valVacaciones != null) valVacaciones.setText(String.valueOf(v[1]));
                    if (valPermisos   != null) valPermisos.setText(String.valueOf(v[2]));
                    if (valDeudaVac   != null) valDeudaVac.setText(String.valueOf(v[3]));
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    // ─────────────────────────────────────────────────────────────────────
    // SECCIÓN: SITUACIONES HOY — RF-07
    // ─────────────────────────────────────────────────────────────────────
    private void showSituacionesHoy() {
        setActive(btnSituaciones);
        contentPanel.removeAll();

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setOpaque(false);

        JLabel title = new JLabel("Servidores con situación administrativa activa hoy  —  " + LocalDate.now().format(FMT));
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        root.add(title, BorderLayout.NORTH);

        String[] headers = ExportService.HEADERS_SITUACIONES;
        DefaultTableModel model = new DefaultTableModel(headers, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = buildStyledTable(model);

        // Botones de exportar
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);
        JButton btnXls = exportBtn(" Excel", () -> {
            List<String[]> rows = buildSituacionesRows(model);
            ExportService.exportToExcel(this, "situaciones_hoy", headers, rows);
        });
        JButton btnPdf = exportBtn(" PDF", () -> {
            List<String[]> rows = buildSituacionesRows(model);
            ExportService.exportToPdf(this, "situaciones_hoy",
                    "Situaciones Administrativas Activas — " + LocalDate.now().format(FMT), headers, rows);
        });
        btnBar.add(btnXls);
        btnBar.add(btnPdf);

        CardPanel card = new CardPanel();
        card.setLayout(new BorderLayout(0, 8));
        card.add(btnBar, BorderLayout.NORTH);
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        root.add(card, BorderLayout.CENTER);

        contentPanel.add(root, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();

        // Cargar datos en background
        new SwingWorker<List<AdministrativeSituation>, Void>() {
            protected List<AdministrativeSituation> doInBackground() {
                return situationDAO.findActiveToday();
            }
            protected void done() {
                try {
                    for (AdministrativeSituation a : get())
                        model.addRow(ExportService.situacionToRow(a));
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    private List<String[]> buildSituacionesRows(DefaultTableModel m) {
        List<String[]> rows = new ArrayList<>();
        for (int r = 0; r < m.getRowCount(); r++) {
            String[] row = new String[m.getColumnCount()];
            for (int c = 0; c < m.getColumnCount(); c++)
                row[c] = m.getValueAt(r, c) != null ? m.getValueAt(r, c).toString() : "";
            rows.add(row);
        }
        return rows;
    }

    // ─────────────────────────────────────────────────────────────────────
    // SECCIÓN: ALERTAS VACACIONES — RF-07
    // ─────────────────────────────────────────────────────────────────────
    private void showAlertasVacaciones() {
        setActive(btnVacaciones);
        contentPanel.removeAll();

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setOpaque(false);

        JLabel title = new JLabel("  Servidores con deuda de vacaciones (> 1 período pendiente)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(new Color(239, 68, 68));
        root.add(title, BorderLayout.NORTH);

        String[] headers = ExportService.HEADERS_VACACIONES_DEUDA;
        DefaultTableModel model = new DefaultTableModel(headers, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = buildStyledTable(model);
        // Filas en rojo
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) {
                    Object p = model.getValueAt(r, 7);
                    int periods = p instanceof Number n ? n.intValue() : 0;
                    comp.setBackground(periods >= 3 ? new Color(254, 226, 226) : new Color(254, 243, 199));
                }
                return comp;
            }
        });

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);
        btnBar.add(exportBtn(" Excel", () -> ExportService.exportToExcel(
                this, "deuda_vacaciones", headers, buildSituacionesRows(model))));
        btnBar.add(exportBtn(" PDF", () -> ExportService.exportToPdf(
                this, "deuda_vacaciones",
                "Servidores con Deuda de Vacaciones — " + LocalDate.now().format(FMT),
                headers, buildSituacionesRows(model))));

        CardPanel card = new CardPanel();
        card.setLayout(new BorderLayout(0, 8));
        card.add(btnBar, BorderLayout.NORTH);
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        root.add(card, BorderLayout.CENTER);

        contentPanel.add(root, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();

        new SwingWorker<List<PublicServer>, Void>() {
            protected List<PublicServer> doInBackground() {
                List<PublicServer> activos = serverDAO.findAllActive();
                return vacationDAO.findServersInDebt(activos);
            }
            protected void done() {
                try {
                    for (PublicServer s : get()) {
                        int acum    = vacationDAO.totalAccumulatedDays(s);
                        int usado   = vacationDAO.totalUsedDays(s);
                        int pend    = vacationDAO.totalPendingDays(s);
                        int periods = vacationDAO.pendingPeriods(s);
                        int anios   = s.getAdmissionDate() != null
                                ? Period.between(s.getAdmissionDate(), LocalDate.now()).getYears() : 0;
                        model.addRow(new String[]{
                                s.getIdNumber(),
                                s.getFirstName() + " " + s.getLastName(),
                                s.getAdmissionDate() != null ? s.getAdmissionDate().format(FMT) : "—",
                                String.valueOf(anios),
                                String.valueOf(acum),
                                String.valueOf(usado),
                                String.valueOf(pend),
                                String.valueOf(periods)
                        });
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    // ─────────────────────────────────────────────────────────────────────
    // SECCIÓN: HISTORIAL POR SERVIDOR — RF-07
    // ─────────────────────────────────────────────────────────────────────
    private void showHistorial() {
        setActive(btnHistorial);
        contentPanel.removeAll();

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setOpaque(false);

        // Barra de búsqueda
        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        busqueda.setOpaque(false);
        busqueda.add(new JLabel("Cédula:"));
        JTextField txtCedula = new JTextField(14);
        busqueda.add(txtCedula);
        JButton btnBuscar = new JButton("Buscar historial");
        busqueda.add(btnBuscar);
        JLabel lblNombre = new JLabel(" ");
        lblNombre.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblNombre.setForeground(new Color(75, 85, 99));
        busqueda.add(lblNombre);
        root.add(busqueda, BorderLayout.NORTH);

        String[] headers = ExportService.HEADERS_SITUACIONES;
        DefaultTableModel model = new DefaultTableModel(headers, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = buildStyledTable(model);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);
        btnBar.add(exportBtn(" Excel", () -> ExportService.exportToExcel(
                this, "historial_" + txtCedula.getText().trim(), headers, buildSituacionesRows(model))));
        btnBar.add(exportBtn(" PDF", () -> ExportService.exportToPdf(
                this, "historial_" + txtCedula.getText().trim(),
                "Historial Situaciones — " + lblNombre.getText(), headers, buildSituacionesRows(model))));

        CardPanel card = new CardPanel();
        card.setLayout(new BorderLayout(0, 8));
        card.add(btnBar, BorderLayout.NORTH);
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        root.add(card, BorderLayout.CENTER);

        Runnable buscarFn = () -> {
            String cedula = txtCedula.getText().trim();
            if (cedula.isEmpty()) return;
            model.setRowCount(0);
            lblNombre.setText("Buscando...");
            new SwingWorker<List<AdministrativeSituation>, Void>() {
                String nombre = "";
                protected List<AdministrativeSituation> doInBackground() {
                    PublicServer s = serverDAO.findByIdNumber(cedula);
                    if (s == null) return List.of();
                    nombre = s.getFirstName() + " " + s.getLastName();
                    return situationDAO.findByServer(s);
                }
                protected void done() {
                    try {
                        List<AdministrativeSituation> lista = get();
                        if (lista.isEmpty()) {
                            lblNombre.setText("Sin resultados para cédula: " + cedula);
                        } else {
                            lblNombre.setText(nombre + "  —  " + lista.size() + " registro(s)");
                            for (AdministrativeSituation a : lista)
                                model.addRow(ExportService.situacionToRow(a));
                        }
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
            }.execute();
        };

        btnBuscar.addActionListener(e -> buscarFn.run());
        txtCedula.addActionListener(e -> buscarFn.run());

        contentPanel.add(root, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // ─────────────────────────────────────────────────────────────────────
    // SECCIÓN: REPORTES — RF-07
    // ─────────────────────────────────────────────────────────────────────
    private void showReportes() {
        setActive(btnReportes);
        contentPanel.removeAll();

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setOpaque(false);

        JLabel title = new JLabel("Reportes — Exportar a Excel o PDF");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        root.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 16, 16));
        grid.setOpaque(false);

        grid.add(createReportCard(
                "  Planta Activa",
                "Todos los servidores activos con cargo, dependencia y salario.",
                () -> {
                    List<PublicServer> lista = serverDAO.findAllActive();
                    List<String[]> rows = new ArrayList<>();
                    lista.forEach(s -> rows.add(ExportService.serverToRow(s)));
                    ExportService.exportToExcel(this, "planta_activa", ExportService.HEADERS_PLANTA, rows);
                },
                () -> {
                    List<PublicServer> lista = serverDAO.findAllActive();
                    List<String[]> rows = new ArrayList<>();
                    lista.forEach(s -> rows.add(ExportService.serverToRow(s)));
                    ExportService.exportToPdf(this, "planta_activa", "Planta Activa de Personal",
                            ExportService.HEADERS_PLANTA, rows);
                }
        ));

        grid.add(createReportCard(
                "  Situaciones Hoy",
                "Servidores con situación administrativa activa en la fecha actual.",
                () -> {
                    List<AdministrativeSituation> lista = situationDAO.findActiveToday();
                    List<String[]> rows = new ArrayList<>();
                    lista.forEach(a -> rows.add(ExportService.situacionToRow(a)));
                    ExportService.exportToExcel(this, "situaciones_hoy", ExportService.HEADERS_SITUACIONES, rows);
                },
                () -> {
                    List<AdministrativeSituation> lista = situationDAO.findActiveToday();
                    List<String[]> rows = new ArrayList<>();
                    lista.forEach(a -> rows.add(ExportService.situacionToRow(a)));
                    ExportService.exportToPdf(this, "situaciones_hoy",
                            "Situaciones Activas — " + LocalDate.now().format(FMT),
                            ExportService.HEADERS_SITUACIONES, rows);
                }
        ));

        grid.add(createReportCard(
                "  Deuda de Vacaciones",
                "Servidores con más de un período de vacaciones sin disfrutar.",
                () -> {
                    List<PublicServer> deuda = vacationDAO.findServersInDebt(serverDAO.findAllActive());
                    List<String[]> rows = buildDeudaRows(deuda);
                    ExportService.exportToExcel(this, "deuda_vacaciones", ExportService.HEADERS_VACACIONES_DEUDA, rows);
                },
                () -> {
                    List<PublicServer> deuda = vacationDAO.findServersInDebt(serverDAO.findAllActive());
                    List<String[]> rows = buildDeudaRows(deuda);
                    ExportService.exportToPdf(this, "deuda_vacaciones", "Servidores con Deuda de Vacaciones",
                            ExportService.HEADERS_VACACIONES_DEUDA, rows);
                }
        ));

        grid.add(createReportCard(
                "  Ausentismo",
                "Resumen de todas las situaciones del año en curso por tipo.",
                () -> {
                    List<AdministrativeSituation> lista = situationDAO.findActiveToday();
                    List<String[]> rows = new ArrayList<>();
                    lista.forEach(a -> rows.add(ExportService.situacionToRow(a)));
                    ExportService.exportToExcel(this, "ausentismo", ExportService.HEADERS_SITUACIONES, rows);
                },
                () -> {
                    List<AdministrativeSituation> lista = situationDAO.findActiveToday();
                    List<String[]> rows = new ArrayList<>();
                    lista.forEach(a -> rows.add(ExportService.situacionToRow(a)));
                    ExportService.exportToPdf(this, "ausentismo", "Estadísticas de Ausentismo",
                            ExportService.HEADERS_SITUACIONES, rows);
                }
        ));

        root.add(grid, BorderLayout.CENTER);
        contentPanel.add(root, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private List<String[]> buildDeudaRows(List<PublicServer> deuda) {
        List<String[]> rows = new ArrayList<>();
        for (PublicServer s : deuda) {
            int anios = s.getAdmissionDate() != null
                    ? Period.between(s.getAdmissionDate(), LocalDate.now()).getYears() : 0;
            rows.add(new String[]{
                    s.getIdNumber(),
                    s.getFirstName() + " " + s.getLastName(),
                    s.getAdmissionDate() != null ? s.getAdmissionDate().format(FMT) : "—",
                    String.valueOf(anios),
                    String.valueOf(vacationDAO.totalAccumulatedDays(s)),
                    String.valueOf(vacationDAO.totalUsedDays(s)),
                    String.valueOf(vacationDAO.totalPendingDays(s)),
                    String.valueOf(vacationDAO.pendingPeriods(s))
            });
        }
        return rows;
    }

    // ─────────────────────────────────────────────────────────────────────
    // BÚSQUEDA GLOBAL (igual que el original)
    // ─────────────────────────────────────────────────────────────────────
    private void performSearch() {
        String q = searchField.getText().trim();
        if (q.isEmpty()) { searchPopup.setVisible(false); return; }
        new SwingWorker<List<PublicServer>, Void>() {
            protected List<PublicServer> doInBackground() {
                List<PublicServer> res = serverDAO.findByName(q);
                if (res == null || res.isEmpty()) {
                    PublicServer s = serverDAO.findByIdNumber(q);
                    if (s != null) res = List.of(s);
                }
                return res != null ? res : List.of();
            }
            protected void done() {
                try { showSearchResults(get()); }
                catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    private void showSearchResults(List<PublicServer> results) {
        searchPopup.removeAll();
        if (results.isEmpty()) {
            searchPopup.add(new JMenuItem("Sin resultados")).setEnabled(false);
        } else {
            DefaultListModel<PublicServer> m = new DefaultListModel<>();
            results.forEach(m::addElement);
            JList<PublicServer> list = new JList<>(m);
            list.setCellRenderer((l, v, i, sel, foc) -> {
                JPanel p = new JPanel(new BorderLayout());
                p.setBorder(new EmptyBorder(6, 10, 6, 10));
                p.setBackground(sel ? new Color(230, 238, 255) : Color.WHITE);
                p.add(new JLabel(String.format("<html><b>%s</b> — %s %s</html>",
                        v.getIdNumber(), v.getFirstName(), v.getLastName())));
                return p;
            });
            list.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) openServerResult(list.getSelectedValue());
                }
            });
            if (!m.isEmpty()) list.setSelectedIndex(0);
            JScrollPane sp = new JScrollPane(list);
            sp.setPreferredSize(new Dimension(Math.max(300, searchField.getWidth()),
                    Math.min(200, results.size() * 52)));
            searchPopup.add(sp);
        }
        searchPopup.show(searchField, 0, searchField.getHeight());
        searchField.requestFocusInWindow();
    }

    private void openServerResult(PublicServer s) {
        if (s == null) return;
        searchPopup.setVisible(false);
        // Abrir perfil integrado RF-08
        new ServerProfileWindow(s).setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────────────
    // HELPERS DE UI
    // ─────────────────────────────────────────────────────────────────────

    /** Card con valor en vivo (el JLabel se actualiza desde refreshDashboardCards) */
    private CardPanel createLiveCard(String title, JLabel valueLabel, String icon, Color accent) {
        CardPanel card = new CardPanel();
        card.setLayout(new BorderLayout(0, 4));
        card.setPreferredSize(new Dimension(200, 110));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        top.setOpaque(false);
        JLabel ico = new JLabel(icon);
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        top.add(ico);
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(75, 85, 99));
        top.add(lbl);
        card.add(top, BorderLayout.NORTH);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(accent);
        valueLabel.setBorder(new EmptyBorder(4, 0, 0, 0));
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    /** Mini card con botón de acceso rápido */
    private CardPanel createMiniCard(String badge, String label, Runnable action) {
        CardPanel card = new CardPanel();
        card.setLayout(new BorderLayout(0, 6));

        JLabel lblBadge = new JLabel(badge);
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblBadge.setForeground(new Color(99, 102, 241));
        card.add(lblBadge, BorderLayout.NORTH);

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        card.add(lblLabel, BorderLayout.CENTER);

        JButton btn = new JButton("Abrir →");
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(99, 102, 241));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> action.run());
        card.add(btn, BorderLayout.SOUTH);
        return card;
    }

    /** Card de reporte con dos botones (Excel y PDF) */
    private CardPanel createReportCard(String title, String desc,
                                       Runnable onExcel, Runnable onPdf) {
        CardPanel card = new CardPanel();
        card.setLayout(new BorderLayout(0, 10));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        card.add(lblTitle, BorderLayout.NORTH);

        JLabel lblDesc = new JLabel("<html>" + desc + "</html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDesc.setForeground(new Color(107, 114, 128));
        card.add(lblDesc, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btns.setOpaque(false);
        btns.add(exportBtn(" Excel", onExcel::run));
        btns.add(exportBtn(" PDF",   onPdf::run));
        card.add(btns, BorderLayout.SOUTH);
        return card;
    }

    private JButton exportBtn(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private JTable buildStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(226, 232, 240));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        return table;
    }

    // ─────────────────────────────────────────────────────────────────────
    // GRÁFICO DE AUSENTISMO (visual mejorado sobre el original)
    // ─────────────────────────────────────────────────────────────────────
    private static class AusentismoChartPanel extends JPanel {
        // Meses abreviados en español
        private static final String[] MESES = {"Jun","Jul","Ago","Sep","Oct","Nov","Dic","Ene","Feb","Mar","Abr","May"};
        // Datos placeholder — en una versión futura leer de la BD
        private static final int[]    VALS  = {8, 12, 7, 15, 10, 9, 6, 14, 11, 8, 7, 5};

        public AusentismoChartPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(0, 180));
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int pad = 36, barW = (w - pad * 2) / VALS.length - 6;
            int maxVal = 20;

            // Guías horizontales
            g2.setColor(new Color(229, 231, 235));
            for (int i = 0; i <= 4; i++) {
                int y = pad + (h - pad * 2) * i / 4;
                g2.drawLine(pad, y, w - pad, y);
            }

            // Barras
            for (int i = 0; i < VALS.length; i++) {
                int x   = pad + i * ((w - pad * 2) / VALS.length) + 3;
                int bh  = (int) ((double) VALS[i] / maxVal * (h - pad * 2));
                int y   = h - pad - bh;

                // Sombra suave
                g2.setColor(new Color(59, 130, 246, 40));
                g2.fillRoundRect(x + 2, y + 2, barW, bh, 6, 6);

                // Barra principal con degradado
                GradientPaint gp = new GradientPaint(x, y, new Color(99, 102, 241),
                        x, h - pad, new Color(59, 130, 246));
                g2.setPaint(gp);
                g2.fillRoundRect(x, y, barW, bh, 6, 6);

                // Etiqueta mes
                g2.setColor(new Color(107, 114, 128));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2.drawString(MESES[i], x + barW / 2 - 9, h - pad + 14);

                // Valor encima
                g2.setColor(new Color(55, 65, 81));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g2.drawString(String.valueOf(VALS[i]), x + barW / 2 - 4, y - 4);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // CardPanel y SidebarButton — idénticos al original
    // ─────────────────────────────────────────────────────────────────────
    static class CardPanel extends JPanel {
        public CardPanel() {
            setOpaque(false);
            setBorder(new EmptyBorder(14, 14, 14, 14));
        }
        @Override protected void paintComponent(Graphics g) {
            int arc = 16, sg = 5;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 25));
            g2.fillRoundRect(sg, sg, getWidth() - sg * 2, getHeight() - sg * 2, arc, arc);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth() - sg, getHeight() - sg, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
        @Override public boolean isOpaque() { return false; }
    }

    private static class SidebarButton extends JPanel {
        private final JLabel lblIcon, lblText;
        private final JPanel indicator;
        private Color current = new Color(17, 24, 39);
        private final Color base = new Color(17, 24, 39), hoverC = new Color(30, 41, 59);
        private javax.swing.Timer animTimer;
        private float animProg = 0f;

        public SidebarButton(String text, String icon) {
            setLayout(new BorderLayout());
            setMaximumSize(new Dimension(10000, 52));
            setPreferredSize(new Dimension(200, 52));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setOpaque(false);

            indicator = new JPanel();
            indicator.setPreferredSize(new Dimension(4, 52));
            indicator.setBackground(new Color(99, 102, 241));
            indicator.setVisible(false);
            add(indicator, BorderLayout.WEST);

            JPanel inner = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
            inner.setOpaque(false);
            lblIcon = new JLabel(icon);
            lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 17));
            lblIcon.setForeground(new Color(226, 232, 240));
            lblText = new JLabel(text);
            lblText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lblText.setForeground(new Color(226, 232, 240));
            inner.add(lblIcon); inner.add(lblText);
            add(inner, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { animTo(true); }
                public void mouseExited(MouseEvent e)  { animTo(false); }
                public void mouseClicked(MouseEvent e) {
                    for (ActionListener al : listenerList.getListeners(ActionListener.class))
                        al.actionPerformed(new ActionEvent(SidebarButton.this, ActionEvent.ACTION_PERFORMED, "click"));
                }
            });
        }

        public void addActionListener(ActionListener al) { listenerList.add(ActionListener.class, al); }
        public String getTextLabel() { return lblText.getText(); }

        public void setActive(boolean a) {
            indicator.setVisible(a);
            Color fg = a ? Color.WHITE : new Color(226, 232, 240);
            lblText.setForeground(fg); lblIcon.setForeground(fg);
            current = a ? new Color(26, 32, 44) : base;
            repaint();
        }

        private void animTo(boolean in) {
            if (animTimer != null) animTimer.stop();
            animTimer = new javax.swing.Timer(12, null);
            animTimer.addActionListener(e -> {
                animProg = in ? Math.min(1f, animProg + 0.1f) : Math.max(0f, animProg - 0.1f);
                float t = animProg;
                current = new Color(
                        (int)(base.getRed()   + (hoverC.getRed()   - base.getRed())   * t),
                        (int)(base.getGreen() + (hoverC.getGreen() - base.getGreen()) * t),
                        (int)(base.getBlue()  + (hoverC.getBlue()  - base.getBlue())  * t));
                repaint();
                if (animProg == 0f || animProg == 1f) animTimer.stop();
            });
            animTimer.start();
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(current);
            g2.fill(new RoundRectangle2D.Float(6, 4, getWidth() - 12, getHeight() - 8, 10, 10));
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new LoginWindow().setVisible(true));
    }
}