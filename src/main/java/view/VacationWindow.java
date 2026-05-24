package view;

import dao.PublicServerDAO;
import dao.VacationPeriodDAO;
import model.PublicServer;
import model.VacationPeriod;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.List;

/**
 * RF-03 — Ventana de Control de Vacaciones
 *
 * Tres zonas:
 *   NORTE  — búsqueda de servidor + panel de resumen (días causados / usados / pendientes)
 *   CENTRO — tabla historial (izquierda) | formulario nuevo registro (derecha)
 *   SUR    — tabla de alertas: servidores que adeudan > 1 período
 */
public class VacationWindow extends JFrame {

    // ── DAOs ──────────────────────────────────────────────────────────────
    private final VacationPeriodDAO  vacationDAO = new VacationPeriodDAO();
    private final PublicServerDAO    serverDAO   = new PublicServerDAO();

    // ── Tabla historial ───────────────────────────────────────────────────
    private JTable            tableHistory;
    private DefaultTableModel modelHistory;

    // ── Tabla alertas ─────────────────────────────────────────────────────
    private JTable            tableAlerts;
    private DefaultTableModel modelAlerts;

    // ── Formulario ────────────────────────────────────────────────────────
    private JTextField                        txtServerId;
    private JSpinner                          spinYear;
    private JSpinner                          spinAccumulated;
    private JSpinner                          spinUsed;
    private com.toedter.calendar.JDateChooser dcLastVacation;
    private JTextArea                         txtNotes;

    // ── Resumen ───────────────────────────────────────────────────────────
    private JLabel lblServerName;
    private JLabel lblAdmission;
    private JLabel lblYearsService;
    private JLabel lblTotalAccum;
    private JLabel lblTotalUsed;
    private JLabel lblTotalPending;
    private JLabel lblAlert;

    // ── Estado ────────────────────────────────────────────────────────────
    private PublicServer currentServer;

    // ─────────────────────────────────────────────────────────────────────
    public VacationWindow() {
        setTitle("RF-03: Control de Vacaciones");
        setSize(1100, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));

        add(buildNorthPanel(),  BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildSouthPanel(),  BorderLayout.SOUTH);

        loadAlerts(); // cargar alertas al abrir
    }

    // ─────────────────────────────────────────────────────────────────────
    // NORTE — búsqueda + resumen del servidor
    // ─────────────────────────────────────────────────────────────────────
    private JPanel buildNorthPanel() {
        JPanel outer = new JPanel(new BorderLayout(0, 6));
        outer.setBackground(new Color(39, 174, 96));
        outer.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        // ── Fila de búsqueda ──
        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        busqueda.setOpaque(false);

        JLabel lbl = new JLabel("Cédula:");
        lbl.setForeground(Color.WHITE);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        busqueda.add(lbl);

        txtServerId = new JTextField(14);
        txtServerId.addActionListener(e -> loadServerById());
        busqueda.add(txtServerId);

        JButton btnLoad = new JButton("Cargar servidor");
        btnLoad.addActionListener(e -> loadServerById());
        busqueda.add(btnLoad);

        outer.add(busqueda, BorderLayout.NORTH);

        // ── Panel de resumen ──
        JPanel resumen = new JPanel(new GridLayout(2, 4, 20, 2));
        resumen.setOpaque(false);

        lblServerName   = makeResumenLabel("—");
        lblAdmission    = makeResumenLabel("—");
        lblYearsService = makeResumenLabel("—");
        lblTotalAccum   = makeResumenLabel("—");
        lblTotalUsed    = makeResumenLabel("—");
        lblTotalPending = makeResumenLabel("—");
        lblAlert        = new JLabel(" ");
        lblAlert.setForeground(new Color(255, 235, 150));
        lblAlert.setFont(lblAlert.getFont().deriveFont(Font.BOLD, 12f));

        resumen.add(makeResumenGroup("Servidor",          lblServerName));
        resumen.add(makeResumenGroup("Fecha de ingreso",  lblAdmission));
        resumen.add(makeResumenGroup("Años de servicio",  lblYearsService));
        resumen.add(makeResumenGroup("Días acumulados",   lblTotalAccum));
        resumen.add(makeResumenGroup("Días disfrutados",  lblTotalUsed));
        resumen.add(makeResumenGroup("Días pendientes",   lblTotalPending));
        resumen.add(lblAlert);

        outer.add(resumen, BorderLayout.CENTER);
        return outer;
    }

    private JLabel makeResumenLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Color.WHITE);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 14f));
        return l;
    }

    private JPanel makeResumenGroup(String titulo, JLabel valor) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setOpaque(false);
        JLabel t = new JLabel(titulo);
        t.setForeground(new Color(200, 255, 200));
        t.setFont(t.getFont().deriveFont(Font.PLAIN, 11f));
        p.add(t,     BorderLayout.NORTH);
        p.add(valor, BorderLayout.CENTER);
        return p;
    }

    // ─────────────────────────────────────────────────────────────────────
    // CENTRO — tabla historial | formulario nuevo período
    // ─────────────────────────────────────────────────────────────────────
    private JSplitPane buildCenterPanel() {

        // ── Tabla historial ──
        modelHistory = new DefaultTableModel(
                new String[]{"ID", "Año", "Acumulados", "Usados", "Pendientes", "Última vacación", "Notas"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableHistory = new JTable(modelHistory);
        tableHistory.setRowHeight(26);
        tableHistory.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableHistory.getColumnModel().getColumn(0).setMaxWidth(50);
        tableHistory.getColumnModel().getColumn(1).setMaxWidth(60);
        tableHistory.getColumnModel().getColumn(2).setMaxWidth(90);
        tableHistory.getColumnModel().getColumn(3).setMaxWidth(70);
        tableHistory.getColumnModel().getColumn(4).setMaxWidth(90);

        // Colorear filas: rojo si tiene días pendientes, verde si está al día
        tableHistory.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                if (!sel) {
                    Object pending = modelHistory.getValueAt(row, 4);
                    int dias = pending instanceof Integer i ? i : 0;
                    c.setBackground(dias > VacationPeriodDAO.DIAS_POR_ANIO
                            ? new Color(255, 220, 220)   // rojo: en deuda
                            : dias > 0
                              ? new Color(255, 243, 205) // amarillo: tiene pendientes
                              : new Color(212, 237, 218) // verde: al día
                    );
                }
                return c;
            }
        });

        // Al seleccionar fila cargar formulario
        tableHistory.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableHistory.getSelectedRow() >= 0)
                fillFormFromRow(tableHistory.getSelectedRow());
        });

        JScrollPane spHistory = new JScrollPane(tableHistory);
        spHistory.setBorder(BorderFactory.createTitledBorder("Historial de períodos"));

        // ── Formulario ──
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Registrar / actualizar período"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 2, 10);
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Año del período
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(new JLabel("Año del período:"), gbc);
        spinYear = new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear() - 1, 2000, 2100, 1));
        ((JSpinner.DefaultEditor) spinYear.getEditor()).getTextField().setColumns(6);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(spinYear, gbc);
        row++;

        // Días acumulados ese año
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        JLabel lblAccHelp = new JLabel("Días acumulados:");
        form.add(lblAccHelp, gbc);
        spinAccumulated = new JSpinner(new SpinnerNumberModel(VacationPeriodDAO.DIAS_POR_ANIO, 0, 365, 1));
        ((JSpinner.DefaultEditor) spinAccumulated.getEditor()).getTextField().setColumns(5);
        gbc.gridx = 1; gbc.weightx = 1;
        JPanel accumPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        accumPanel.setOpaque(false);
        accumPanel.add(spinAccumulated);
        JLabel lblAccNote = new JLabel("  (15 = régimen general)");
        lblAccNote.setForeground(Color.GRAY);
        lblAccNote.setFont(lblAccNote.getFont().deriveFont(Font.ITALIC, 11f));
        accumPanel.add(lblAccNote);
        form.add(accumPanel, gbc);
        row++;

        // Días usados
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(new JLabel("Días disfrutados:"), gbc);
        spinUsed = new JSpinner(new SpinnerNumberModel(0, 0, 365, 1));
        ((JSpinner.DefaultEditor) spinUsed.getEditor()).getTextField().setColumns(5);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(spinUsed, gbc);
        row++;

        // Última fecha de vacación
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(new JLabel("Última vacación:"), gbc);
        dcLastVacation = new com.toedter.calendar.JDateChooser();
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(dcLastVacation, gbc);
        row++;

        // Notas
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("Notas:"), gbc);
        txtNotes = new JTextArea(3, 18);
        txtNotes.setLineWrap(true);
        txtNotes.setWrapStyleWord(true);
        gbc.gridx = 1; gbc.weightx = 1; gbc.weighty = 1; gbc.fill = GridBagConstraints.BOTH;
        form.add(new JScrollPane(txtNotes), gbc);
        row++;

        // Botones
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.weighty = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(12, 10, 8, 10);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

        JButton btnLimpiar = new JButton("Limpiar");
        btnLimpiar.addActionListener(e -> clearForm());

        JButton btnEliminar = new JButton("Eliminar");
        btnEliminar.setForeground(new Color(180, 30, 30));
        btnEliminar.addActionListener(e -> deleteSelected());

        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setBackground(new Color(39, 174, 96));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setOpaque(true);
        btnGuardar.addActionListener(e -> savePeriod());

        btnPanel.add(btnLimpiar);
        btnPanel.add(btnEliminar);
        btnPanel.add(btnGuardar);
        form.add(btnPanel, gbc);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, spHistory, form);
        split.setDividerLocation(580);
        split.setResizeWeight(0.6);
        return split;
    }

    // ─────────────────────────────────────────────────────────────────────
    // SUR — tabla de alertas RF-03
    // ─────────────────────────────────────────────────────────────────────
    private JPanel buildSouthPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                "⚠  Servidores con más de 1 período pendiente (deben programar vacaciones)"));
        panel.setPreferredSize(new Dimension(0, 160));

        modelAlerts = new DefaultTableModel(
                new String[]{"Cédula", "Nombre", "Fecha ingreso", "Años servicio",
                        "Días acumulados", "Días usados", "Días pendientes", "Períodos pendientes"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableAlerts = new JTable(modelAlerts);
        tableAlerts.setRowHeight(24);
        tableAlerts.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                if (!sel) {
                    Object periods = modelAlerts.getValueAt(row, 7);
                    int p = periods instanceof Integer i ? i : 0;
                    c.setBackground(p >= 3 ? new Color(255, 200, 200) : new Color(255, 235, 150));
                }
                return c;
            }
        });

        // Doble clic en una alerta: carga ese servidor en el formulario
        tableAlerts.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && tableAlerts.getSelectedRow() >= 0) {
                    String cedula = (String) modelAlerts.getValueAt(tableAlerts.getSelectedRow(), 0);
                    txtServerId.setText(cedula);
                    loadServerById();
                }
            }
        });

        panel.add(new JScrollPane(tableAlerts), BorderLayout.CENTER);

        JLabel hint = new JLabel("  Doble clic en un servidor para cargar su historial arriba.");
        hint.setFont(hint.getFont().deriveFont(Font.ITALIC, 11f));
        hint.setForeground(Color.GRAY);
        panel.add(hint, BorderLayout.SOUTH);

        return panel;
    }

    // ─────────────────────────────────────────────────────────────────────
    // LÓGICA: cargar servidor por cédula
    // ─────────────────────────────────────────────────────────────────────
    private void loadServerById() {
        String cedula = txtServerId.getText().trim();
        if (cedula.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa una cédula.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        currentServer = serverDAO.findByIdNumber(cedula);

        if (currentServer == null) {
            JOptionPane.showMessageDialog(this,
                    "No se encontró servidor con cédula: " + cedula,
                    "No encontrado", JOptionPane.INFORMATION_MESSAGE);
            clearResumen();
            return;
        }

        setTitle("RF-03: Vacaciones — " + currentServer.getFirstName() + " " + currentServer.getLastName());
        refreshResumen();
        refreshHistory();
        clearForm();

        // Pre-llenar el año sugerido en el spinner con el año anterior al actual
        spinYear.setValue(LocalDate.now().getYear() - 1);

        // Pre-llenar días acumulados basado en antigüedad
        int diasSugeridos = vacationDAO.calcularDiasAcumuladosTotales(currentServer);
        if (diasSugeridos > 0) {
            // Para un año concreto siempre es 15 (o el régimen del servidor)
            spinAccumulated.setValue(VacationPeriodDAO.DIAS_POR_ANIO);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // LÓGICA: actualizar panel de resumen
    // ─────────────────────────────────────────────────────────────────────
    private void refreshResumen() {
        if (currentServer == null) return;

        int acum    = vacationDAO.totalAccumulatedDays(currentServer);
        int usado   = vacationDAO.totalUsedDays(currentServer);
        int pend    = vacationDAO.totalPendingDays(currentServer);
        int periods = vacationDAO.pendingPeriods(currentServer);

        lblServerName.setText(currentServer.getFirstName() + " " + currentServer.getLastName()
                + "  (" + currentServer.getIdNumber() + ")");

        if (currentServer.getAdmissionDate() != null) {
            lblAdmission.setText(currentServer.getAdmissionDate().toString());
            int anios = Period.between(currentServer.getAdmissionDate(), LocalDate.now()).getYears();
            lblYearsService.setText(anios + " año(s)");
        } else {
            lblAdmission.setText("Sin fecha de ingreso");
            lblYearsService.setText("—");
        }

        lblTotalAccum.setText(acum + " días");
        lblTotalUsed.setText(usado + " días");
        lblTotalPending.setText(pend + " días");

        // Colorear días pendientes y mostrar alerta si aplica
        if (periods > 1) {
            lblTotalPending.setForeground(new Color(255, 180, 100));
            lblAlert.setText("⚠  ALERTA: " + periods + " período(s) sin disfrutar. Debe programar vacaciones.");
        } else if (pend > 0) {
            lblTotalPending.setForeground(new Color(255, 235, 150));
            lblAlert.setText("Tiene días pendientes del período actual.");
        } else {
            lblTotalPending.setForeground(new Color(200, 255, 200));
            lblAlert.setText("✓  Al día en vacaciones.");
        }
    }

    private void clearResumen() {
        lblServerName.setText("—");
        lblAdmission.setText("—");
        lblYearsService.setText("—");
        lblTotalAccum.setText("—");
        lblTotalUsed.setText("—");
        lblTotalPending.setText("—");
        lblAlert.setText(" ");
        setTitle("RF-03: Control de Vacaciones");
    }

    // ─────────────────────────────────────────────────────────────────────
    // LÓGICA: refrescar tabla de historial
    // ─────────────────────────────────────────────────────────────────────
    private void refreshHistory() {
        modelHistory.setRowCount(0);
        if (currentServer == null) return;

        for (VacationPeriod vp : vacationDAO.findByServer(currentServer)) {
            modelHistory.addRow(new Object[]{
                    vp.getId(),
                    vp.getYear(),
                    vp.getAccumulatedDays(),
                    vp.getUsedDays(),
                    vp.getPendingDays(),                           // calculado en el modelo
                    vp.getLastVacationDate() != null
                            ? vp.getLastVacationDate().toString() : "—",
                    vp.getNotes() != null ? vp.getNotes() : ""
            });
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // LÓGICA: refrescar tabla de alertas (todos los servidores)
    // ─────────────────────────────────────────────────────────────────────
    private void loadAlerts() {
        modelAlerts.setRowCount(0);
        List<PublicServer> activos = serverDAO.findAllActive();
        List<PublicServer> enDeuda = vacationDAO.findServersInDebt(activos);

        for (PublicServer s : enDeuda) {
            int acum    = vacationDAO.totalAccumulatedDays(s);
            int usado   = vacationDAO.totalUsedDays(s);
            int pend    = vacationDAO.totalPendingDays(s);
            int periods = vacationDAO.pendingPeriods(s);
            int anios   = s.getAdmissionDate() != null
                    ? Period.between(s.getAdmissionDate(), LocalDate.now()).getYears() : 0;

            modelAlerts.addRow(new Object[]{
                    s.getIdNumber(),
                    s.getFirstName() + " " + s.getLastName(),
                    s.getAdmissionDate() != null ? s.getAdmissionDate().toString() : "—",
                    anios,
                    acum,
                    usado,
                    pend,
                    periods
            });
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // LÓGICA: guardar período
    // ─────────────────────────────────────────────────────────────────────
    private void savePeriod() {
        if (currentServer == null) {
            JOptionPane.showMessageDialog(this, "Carga primero un servidor con su cédula.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        VacationPeriod vp = new VacationPeriod();
        vp.setServer(currentServer);
        vp.setYear((Integer) spinYear.getValue());
        vp.setAccumulatedDays((Integer) spinAccumulated.getValue());
        vp.setUsedDays((Integer) spinUsed.getValue());

        if (dcLastVacation.getDate() != null) {
            vp.setLastVacationDate(
                    dcLastVacation.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }
        vp.setNotes(txtNotes.getText().trim());

        try {
            vacationDAO.saveWithValidation(vp);
            JOptionPane.showMessageDialog(this, "Período de vacaciones guardado correctamente.",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
            refreshHistory();
            refreshResumen();
            loadAlerts();
            clearForm();

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Error de validación", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error inesperado: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // LÓGICA: eliminar período seleccionado
    // ─────────────────────────────────────────────────────────────────────
    private void deleteSelected() {
        int row = tableHistory.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un período de la tabla.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long id   = (Long)    modelHistory.getValueAt(row, 0);
        Integer yr = (Integer) modelHistory.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Eliminar el registro de vacaciones del año " + yr + "?\n"
                        + "Esta acción no se puede deshacer.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                vacationDAO.delete(id);
                refreshHistory();
                refreshResumen();
                loadAlerts();
                clearForm();
                JOptionPane.showMessageDialog(this, "Período eliminado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al eliminar: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // LÓGICA: llenar formulario al seleccionar fila
    // ─────────────────────────────────────────────────────────────────────
    private void fillFormFromRow(int row) {
        Long id = (Long) modelHistory.getValueAt(row, 0);
        if (id == null || currentServer == null) return;

        vacationDAO.findByServer(currentServer).stream()
                .filter(v -> id.equals(v.getId()))
                .findFirst()
                .ifPresent(v -> {
                    if (v.getYear() != null)            spinYear.setValue(v.getYear());
                    if (v.getAccumulatedDays() != null) spinAccumulated.setValue(v.getAccumulatedDays());
                    if (v.getUsedDays() != null)        spinUsed.setValue(v.getUsedDays());
                    if (v.getLastVacationDate() != null)
                        dcLastVacation.setDate(java.sql.Date.valueOf(v.getLastVacationDate()));
                    else
                        dcLastVacation.setDate(null);
                    txtNotes.setText(v.getNotes() != null ? v.getNotes() : "");
                });
    }

    // ─────────────────────────────────────────────────────────────────────
    // LÓGICA: limpiar formulario
    // ─────────────────────────────────────────────────────────────────────
    private void clearForm() {
        spinYear.setValue(LocalDate.now().getYear() - 1);
        spinAccumulated.setValue(VacationPeriodDAO.DIAS_POR_ANIO);
        spinUsed.setValue(0);
        dcLastVacation.setDate(null);
        txtNotes.setText("");
        tableHistory.clearSelection();
    }
}