package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.TimerTask;
import view.PublicServerWindow;
import dao.PublicServerDAO;
import model.PublicServer;
/**
 * DashboardWindow - Dashboard moderno en Swing
 *
 * - Sidebar minimalista con iconos (emoji) y selección activa
 * - Header con búsqueda, notificaciones y perfil
 * - Cards (estadísticas), area de gráficos de ejemplo y tabla de "recientes"
 * - Efectos de hover suaves (animación con Swing Timer)
 *
 * Diseñado para pegar en: src/main/java/view/DashboardWindow.java
 */
public class DashboardWindow extends JFrame {

    private SidebarButton btnInicio;
    private SidebarButton btnEstadisticas;
    private SidebarButton btnUsuarios;
    private SidebarButton btnReportes;
    private SidebarButton btnConfiguracion;
    private SidebarButton btnPerfil;

    private JPanel contentPanel; // panel central donde se muestra contenido
    // Búsqueda rápida
    private JTextField searchField;
    private JPopupMenu searchPopup;
    private javax.swing.Timer searchDebounceTimer;
    private PublicServerDAO publicServerDAO = new PublicServerDAO();

    public DashboardWindow() {
        setTitle("Dashboard - Talento Humano");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(900, 600));

        // Crear contentPanel PRIMERO (antes de sidebar)
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);

        // Ahora crear sidebar (que usa setActiveButton que necesita contentPanel)
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // Main area (header + content)
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(245, 247, 250));
        main.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Header
        JPanel header = createHeader();
        main.add(header, BorderLayout.NORTH);

        // Añadir contentPanel (ya está inicializado)
        contentPanel.add(createDashboardContent(), BorderLayout.CENTER);
        main.add(contentPanel, BorderLayout.CENTER);

        add(main, BorderLayout.CENTER);
    }

    // ------------------------------
    // Sidebar
    // ------------------------------
    private JPanel createSidebar() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(240, 0));
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(17, 24, 39)); // Fondo oscuro elegante

        // Logo / App name
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(panel.getBackground());
        top.setBorder(new EmptyBorder(18, 18, 18, 18));
        JLabel logo = new JLabel("TalentoHumano");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        top.add(new JLabel("⚙️")); // pequeño icono
        top.add(Box.createHorizontalStrut(8));
        top.add(logo);
        panel.add(top, BorderLayout.NORTH);

        // Menu (center)
        JPanel menu = new JPanel();
        menu.setBackground(panel.getBackground());
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(new EmptyBorder(8, 8, 8, 8));

        btnInicio = new SidebarButton("Inicio", "🏠");
        btnEstadisticas = new SidebarButton("Estadísticas", "📈");
        btnUsuarios = new SidebarButton("Usuarios", "👥");
        btnReportes = new SidebarButton("Reportes", "📊");
        btnConfiguracion = new SidebarButton("Configuración", "⚙️");
        btnPerfil = new SidebarButton("Perfil", "👤");

        // Añadir botones
        menu.add(btnInicio);
        menu.add(btnEstadisticas);
        menu.add(btnUsuarios);
        menu.add(btnReportes);
        menu.add(Box.createVerticalStrut(10));
        menu.add(btnConfiguracion);
        menu.add(Box.createVerticalStrut(12));
        menu.add(btnPerfil);
        menu.add(Box.createVerticalGlue());

        // Acciones: seleccionar y cambiar vista
        btnInicio.addActionListener(e -> setActiveButton(btnInicio));
        btnEstadisticas.addActionListener(e -> setActiveButton(btnEstadisticas));
        btnUsuarios.addActionListener(e -> setActiveButton(btnUsuarios));
        btnReportes.addActionListener(e -> setActiveButton(btnReportes));
        btnConfiguracion.addActionListener(e -> setActiveButton(btnConfiguracion));
        btnPerfil.addActionListener(e -> setActiveButton(btnPerfil));

        // Preseleccionar inicio
        setActiveButton(btnInicio);

        panel.add(menu, BorderLayout.CENTER);

        return panel;
    }

    private void setActiveButton(SidebarButton active) {
        // Desactivar todos
        btnInicio.setActive(false);
        btnEstadisticas.setActive(false);
        btnUsuarios.setActive(false);
        btnReportes.setActive(false);
        btnConfiguracion.setActive(false);
        btnPerfil.setActive(false);

        // Activar seleccionado
        active.setActive(true);

        // Cambiar contenido principal según activo
        String title = active.getTextLabel();

        contentPanel.removeAll();

        if (title.equals("Inicio")) {
            contentPanel.add(createDashboardContent(), BorderLayout.CENTER);
        } else if (title.equals("Estadísticas")) {
            contentPanel.add(createEstadisticasContent(), BorderLayout.CENTER);
        } else if (title.equals("Usuarios")) {
            contentPanel.add(createUsuariosContent(), BorderLayout.CENTER);
        } else if (title.equals("Reportes")) {
            contentPanel.add(createReportesContent(), BorderLayout.CENTER);
        } else if (title.equals("Configuración")) {
            contentPanel.add(createConfiguracionContent(), BorderLayout.CENTER);
        } else if (title.equals("Perfil")) {
            contentPanel.add(createPerfilContent(), BorderLayout.CENTER);
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel createEstadisticasContent() {
        JPanel root = new JPanel();
        root.setOpaque(false);
        root.setLayout(new BorderLayout(16, 16));

        JPanel cards = new JPanel(new GridLayout(2, 2, 16, 16));
        cards.setOpaque(false);

        cards.add(createStatCard("Tasa de Ausentismo", "6.2%", "▼ 0.8%"));
        cards.add(createStatCard("Rotación Anual", "4.9%", "▲ 1.2%"));
        cards.add(createStatCard("Satisfacción", "87%", "▲ 3.1%"));
        cards.add(createStatCard("Retención", "95.1%", "▲ 2.4%"));

        root.add(cards, BorderLayout.CENTER);
        return root;
    }

    private JPanel createUsuariosContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        // Contenido centrado con mensaje y botón
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel lbl = new JLabel("Módulo de Usuarios - Gestiona la Planta de Personal");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(new Color(33, 37, 41));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        center.add(Box.createVerticalGlue());
        center.add(lbl);
        center.add(Box.createVerticalStrut(16));

        JButton btnOpen = new JButton("Abrir Planta de Personal");
        btnOpen.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnOpen.setPreferredSize(new Dimension(220, 40));
        btnOpen.addActionListener(e -> SwingUtilities.invokeLater(() -> new PublicServerWindow().setVisible(true)));
        center.add(btnOpen);

        center.add(Box.createVerticalGlue());

        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createReportesContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel lbl = new JLabel("Módulo de Reportes (en desarrollo)", SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(new Color(75, 85, 99));
        panel.add(lbl, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createConfiguracionContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel lbl = new JLabel("Configuración (en desarrollo)", SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(new Color(75, 85, 99));
        panel.add(lbl, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPerfilContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel lbl = new JLabel("Perfil de usuario (en desarrollo)", SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(new Color(75, 85, 99));
        panel.add(lbl, BorderLayout.CENTER);
        return panel;
    }

    // ------------------------------
    // Header
    // ------------------------------
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(8, 8, 8, 8));

        // Search bar (izquierda)
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        left.setOpaque(false);

        // Campo de búsqueda reutilizable
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(350, 36));
        searchField.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        searchField.setBackground(Color.WHITE);
        searchField.setMaximumSize(new Dimension(400, 36));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setToolTipText("Buscar por cédula, nombre o dependencia...");
        left.add(searchField);

        header.add(left, BorderLayout.WEST);

        // Right: notifications + profile
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        // Notifications button
        JButton btnNotify = new JButton("🔔");
        btnNotify.setPreferredSize(new Dimension(44, 36));
        styleIconButton(btnNotify);

        // Profile (avatar + name)
        JPanel profile = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        profile.setOpaque(false);
        JLabel avatar = new JLabel("🧑");
        JLabel userName = new JLabel("Admin");
        userName.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userName.setForeground(new Color(33, 37, 41));
        profile.add(avatar);
        profile.add(userName);

        right.add(btnNotify);
        right.add(profile);

        header.add(right, BorderLayout.EAST);

        // ---- Popup y debounce ----
        searchPopup = new JPopupMenu();
        searchPopup.setFocusable(false);

        // Timer debounce 300ms
        searchDebounceTimer = new javax.swing.Timer(300, e -> performSearch());
        searchDebounceTimer.setRepeats(false);

        // Document listener para disparar búsqueda con debounce
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { scheduleSearch(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { scheduleSearch(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { scheduleSearch(); }
            private void scheduleSearch() {
                searchDebounceTimer.restart();
            }
        });

        // Enter: si hay resultados abre el primero; si no, dispara búsqueda
        searchField.addActionListener(evt -> {
            if (searchPopup.isVisible() && searchPopup.getComponentCount() > 0) {
                Component comp = searchPopup.getComponent(0);
                if (comp instanceof JScrollPane) {
                    JList<?> list = (JList<?>) ((JScrollPane) comp).getViewport().getView();
                    if (list.getModel().getSize() > 0) {
                        Object selected = list.getModel().getElementAt(list.getSelectedIndex() >= 0 ? list.getSelectedIndex() : 0);
                        openResult(selected);
                    }
                }
            } else {
                performSearch();
            }
        });

        // ESC para cerrar popup
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE && searchPopup.isVisible()) {
                    searchPopup.setVisible(false);
                }
            }
        });

        return header;
    }

    // Ejecuta la búsqueda en background (SwingWorker)
    private void performSearch() {
        String q = searchField.getText().trim();
        if (q.isEmpty()) {
            searchPopup.setVisible(false);
            return;
        }

        new SwingWorker<java.util.List<PublicServer>, Void>() {
            @Override
            protected java.util.List<PublicServer> doInBackground() throws Exception {
                // Primero buscar por nombre/apellido (DAO debe devolver lista, implementado en tu proyecto)
                java.util.List<PublicServer> results = publicServerDAO.findByName(q);
                // Si no hay resultados, probar búsqueda exacta por cédula
                if ((results == null || results.isEmpty())) {
                    PublicServer byId = publicServerDAO.findByIdNumber(q);
                    if (byId != null) {
                        results = java.util.List.of(byId);
                    }
                }
                return results;
            }

            @Override
            protected void done() {
                try {
                    java.util.List<PublicServer> results = get();
                    showSearchResults(results);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    searchPopup.setVisible(false);
                }
            }
        }.execute();
    }

    private void showSearchResults(java.util.List<PublicServer> results) {
        searchPopup.removeAll();

        if (results == null || results.isEmpty()) {
            JMenuItem none = new JMenuItem("No se encontraron resultados");
            none.setEnabled(false);
            searchPopup.add(none);
        } else {
            DefaultListModel<PublicServer> model = new DefaultListModel<>();
            for (PublicServer p : results) model.addElement(p);

            JList<PublicServer> list = new JList<>(model);
            list.setCellRenderer((list1, value, index, isSelected, cellHasFocus) -> {
                JPanel row = new JPanel(new BorderLayout());
                row.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
                row.setBackground(isSelected ? new Color(230, 238, 255) : Color.WHITE);
                String text = String.format("<html><b>%s</b> — %s %s<br/><small>%s</small></html>",
                        value.getIdNumber(),
                        value.getFirstName() != null ? value.getFirstName() : "",
                        value.getLastName() != null ? value.getLastName() : "",
                        value.getDependency() != null ? value.getDependency().getName() : "");
                JLabel l = new JLabel(text);
                l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                row.add(l, BorderLayout.CENTER);
                return row;
            });

            // Doble clic o ENTER -> abrir resultado
            list.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        Object sel = list.getSelectedValue();
                        openResult(sel);
                    }
                }
            });
            list.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        Object sel = list.getSelectedValue();
                        openResult(sel);
                    }
                }
            });

            // Seleccionar primer elemento por defecto
            if (!model.isEmpty()) list.setSelectedIndex(0);

            JScrollPane sp = new JScrollPane(list);
            sp.setPreferredSize(new Dimension(Math.max(300, searchField.getWidth()), Math.min(240, results.size() * 56)));
            searchPopup.add(sp);
        }

        // Mostrar popup justo debajo del campo de búsqueda
        searchPopup.show(searchField, 0, searchField.getHeight());
        searchField.requestFocusInWindow();
    }

    // Acción al seleccionar un resultado
    private void openResult(Object result) {
        if (result instanceof PublicServer) {
            PublicServer p = (PublicServer) result;
            // Abre la ventana de gestión y carga el servidor por id (se asegura de que exista método)
            PublicServerWindow win = new PublicServerWindow();
            // Si implementas loadServer en PublicServerWindow lo llamamos:
            try {
                win.loadServer(p.getIdNumber());
            } catch (NoSuchMethodError | AbstractMethodError ignored) {
                // si no existe loadServer() la ventana seguirá abriéndose sin cargar los datos
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            win.setVisible(true);
            searchPopup.setVisible(false);
        } else {
            searchPopup.setVisible(false);
        }
    }

    private void styleIconButton(JButton btn) {
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // ------------------------------
    // Dashboard content (cards, charts, tables)
    // ------------------------------
    private JPanel createDashboardContent() {
        JPanel root = new JPanel();
        root.setOpaque(false);
        root.setLayout(new BorderLayout(16, 16));

        // Cards on top
        JPanel cards = new JPanel(new GridLayout(1, 4, 16, 16));
        cards.setOpaque(false);

        cards.add(createStatCard("Servidores Activos", "1,245", "▲ 4.2%"));
        cards.add(createStatCard("En Vacaciones", "34", "▼ 0.4%"));
        cards.add(createStatCard("Permisos Hoy", "12", "▲ 1.1%"));
        cards.add(createStatCard("Alertas Salud", "3", "▲ 20%"));

        root.add(cards, BorderLayout.NORTH);

        // Middle: charts + recent table
        JPanel middle = new JPanel(new GridBagLayout());
        middle.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.BOTH;

        // Mock chart panel (left, larger)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.66;
        gbc.weighty = 0.65;
        JPanel chartPanel = new CardPanel();
        chartPanel.setLayout(new BorderLayout());
        chartPanel.add(new JLabel("Tendencia de ausentismo (últimos 12 meses)", SwingConstants.LEFT), BorderLayout.NORTH);
        chartPanel.add(new MockChartPanel(), BorderLayout.CENTER);
        middle.add(chartPanel, gbc);

        // Right: small metrics + mini charts
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.34;
        gbc.weighty = 0.65;
        JPanel rightCol = new JPanel();
        rightCol.setOpaque(false);
        rightCol.setLayout(new GridLayout(3, 1, 12, 12));
        rightCol.add(createMiniCard("Promedio antigüedad", "7.2 años"));
        rightCol.add(createMiniCard("Satisfacción", "87%"));
        rightCol.add(createMiniCard("Prom. Salario", "$1,240"));
        middle.add(rightCol, gbc);

        // Bottom: recent table
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.weighty = 0.35;
        JPanel tableCard = new CardPanel();
        tableCard.setLayout(new BorderLayout());
        tableCard.add(new JLabel("Acciones recientes", SwingConstants.LEFT), BorderLayout.NORTH);
        tableCard.add(createRecentTable(), BorderLayout.CENTER);
        middle.add(tableCard, gbc);

        root.add(middle, BorderLayout.CENTER);

        return root;
    }

    // ------------------------------
    // UI building blocks
    // ------------------------------
    private JPanel createStatCard(String title, String value, String delta) {
        CardPanel card = new CardPanel();
        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(200, 120));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTitle.setForeground(new Color(75, 85, 99));
        card.add(lblTitle, BorderLayout.NORTH);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblValue.setForeground(new Color(17, 24, 39));
        lblValue.setBorder(new EmptyBorder(8, 0, 8, 0));
        card.add(lblValue, BorderLayout.CENTER);

        JLabel lblDelta = new JLabel(delta);
        lblDelta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDelta.setForeground(new Color(34, 197, 94)); // verde
        card.add(lblDelta, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createMiniCard(String title, String value) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(255, 255, 255, 230));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.setOpaque(true);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTitle.setForeground(new Color(75, 85, 99));
        panel.add(lblTitle, BorderLayout.NORTH);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblValue.setForeground(new Color(17, 24, 39));
        panel.add(lblValue, BorderLayout.CENTER);

        return panel;
    }

    private JScrollPane createRecentTable() {
        String[] cols = {"Fecha", "Acción", "Usuario", "Detalle"};
        Object[][] data = {
                {"2026-05-23", "Registro", "mrojas", "Nuevo servidor añadido"},
                {"2026-05-22", "Baja", "jlopez", "Servidor retirado (jubilación)"},
                {"2026-05-20", "Permiso", "crodriguez", "Permiso por 2 días"},
                {"2026-05-18", "Evaluación", "admin", "Evaluación anual ingresada"},
                {"2026-05-17", "Vacaciones", "amartinez", "Periodo aprobado"}
        };
        DefaultTableModel model = new DefaultTableModel(data, cols);
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setShowGrid(false);
        table.setFillsViewportHeight(true);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(226, 232, 240));
        table.getTableHeader().setReorderingAllowed(false);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        return scroll;
    }

    // ------------------------------
    // Small custom components
    // ------------------------------
    // Panel con sombra y esquinas redondeadas
    private static class CardPanel extends JPanel {
        public CardPanel() {
            setOpaque(false);
            setBackground(new Color(255, 255, 255));
            setBorder(new EmptyBorder(12, 12, 12, 12));
        }

        @Override
        protected void paintComponent(Graphics g) {
            int arc = 16;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Sombra suave
            int shadowGap = 6;
            Color shadow = new Color(0, 0, 0, 30);
            g2.setColor(shadow);
            g2.fillRoundRect(shadowGap, shadowGap, getWidth() - shadowGap * 2, getHeight() - shadowGap * 2, arc, arc);

            // Fondo
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth() - shadowGap, getHeight() - shadowGap, arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        public boolean isOpaque() {
            return false;
        }
    }

    // MockChartPanel: dibuja un gráfico de líneas simple (placeholder)
    private static class MockChartPanel extends JPanel {
        public MockChartPanel() {
            setPreferredSize(new Dimension(100, 220));
            setBackground(new Color(255, 255, 255, 0));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Grid
            g2.setColor(new Color(229, 231, 235));
            for (int i = 0; i < 5; i++) {
                int y = (int) ((i / 5.0) * h);
                g2.drawLine(0, y, w, y);
            }

            // Mock line
            int[] xs = new int[w];
            int[] ys = new int[w];
            for (int i = 0; i < 12; i++) {
                xs[i] = (int) (i * (w / 12.0) + 20);
            }
            int[] pointsY = new int[]{80, 70, 90, 60, 50, 80, 100, 90, 70, 60, 50, 40};
            int prevX = 20, prevY = pointsY[0] + 40;
            g2.setStroke(new BasicStroke(3));
            g2.setColor(new Color(59, 130, 246));
            for (int i = 0; i < 12; i++) {
                int x = 20 + i * Math.max(1, (w - 40) / 12);
                int y = pointsY[i] + 40;
                g2.drawLine(prevX, prevY, x, y);
                prevX = x;
                prevY = y;
            }

            // Area bajo la curva (suave)
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f));
            g2.setColor(new Color(59, 130, 246));
            // Simplificado: rect debajo
            g2.fillRoundRect(20, 80, w - 40, h - 100, 10, 10);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }
    }

    // SidebarButton: botón personalizado con animación de hover y indicador activo
    private static class SidebarButton extends JPanel {
        private final JLabel lblIcon;
        private final JLabel lblText;
        private boolean active = false;
        private Color base = new Color(17, 24, 39);
        private Color hover = new Color(30, 41, 59);
        private Color current = base;
        private Timer animTimer;
        private float animProgress = 0f;
        private final JPanel indicator;

        public SidebarButton(String text, String iconEmoji) {
            setLayout(new BorderLayout());
            setMaximumSize(new Dimension(10000, 56));
            setPreferredSize(new Dimension(200, 56));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setOpaque(false);

            indicator = new JPanel();
            indicator.setPreferredSize(new Dimension(6, 56));
            indicator.setBackground(new Color(99, 102, 241));
            indicator.setVisible(false);
            add(indicator, BorderLayout.WEST);

            JPanel inner = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
            inner.setOpaque(false);

            lblIcon = new JLabel(iconEmoji);
            lblIcon.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            lblIcon.setForeground(new Color(226, 232, 240));

            lblText = new JLabel(text);
            lblText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lblText.setForeground(new Color(226, 232, 240));

            inner.add(lblIcon);
            inner.add(lblText);
            add(inner, BorderLayout.CENTER);

            // Mouse events para hover y click
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    startHoverAnimation(true);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    startHoverAnimation(false);
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    // propagar acción a los listeners
                    for (ActionListener al : getActionListeners()) {
                        al.actionPerformed(new ActionEvent(SidebarButton.this, ActionEvent.ACTION_PERFORMED, "click"));
                    }
                }
            });
        }

        // Lista simple de listeners
        public void addActionListener(ActionListener al) {
            listenerList.add(ActionListener.class, al);
        }

        public ActionListener[] getActionListeners() {
            return listenerList.getListeners(ActionListener.class);
        }

        public String getTextLabel() {
            return lblText.getText();
        }

        public void setActive(boolean active) {
            this.active = active;
            indicator.setVisible(active);
            if (active) {
                lblText.setForeground(Color.WHITE);
                lblIcon.setForeground(Color.WHITE);
                setBackgroundColor(new Color(26, 32, 44));
            } else {
                lblText.setForeground(new Color(226, 232, 240));
                lblIcon.setForeground(new Color(226, 232, 240));
                setBackgroundColor(base);
            }
            repaint();
        }

        private void setBackgroundColor(Color c) {
            this.current = c;
            repaint();
        }

        private void startHoverAnimation(boolean entering) {
            if (animTimer != null) animTimer.stop();
            animProgress = entering ? 0f : 1f;
            animTimer = new Timer(12, null);
            animTimer.addActionListener(e -> {
                if (entering) {
                    animProgress += 0.08f;
                    if (animProgress >= 1f) {
                        animProgress = 1f;
                        animTimer.stop();
                    }
                } else {
                    animProgress -= 0.08f;
                    if (animProgress <= 0f) {
                        animProgress = 0f;
                        animTimer.stop();
                    }
                }
                // interpolar color
                float t = animProgress;
                Color c = blendColors(base, hover, t);
                setBackgroundColor(c);
            });
            animTimer.start();
        }

        private static Color blendColors(Color a, Color b, float t) {
            t = Math.max(0f, Math.min(1f, t));
            int r = (int) (a.getRed() + (b.getRed() - a.getRed()) * t);
            int g = (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t);
            int bl = (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t);
            return new Color(r, g, bl);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // Pintar fondo redondeado con color current
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Shape round = new RoundRectangle2D.Float(6, 6, getWidth() - 12, getHeight() - 12, 12, 12);
            g2.setColor(current);
            g2.fill(round);
            g2.dispose();
        }
    }

    // ------------------------------
    // Lanzador para pruebas rápidas
    // ------------------------------
    public static void main(String[] args) {
        // Opcional: aplicar "look and feel" moderno por defecto
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            DashboardWindow w = new DashboardWindow();
            w.setVisible(true);
        });
    }
}