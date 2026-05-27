package view;

import dao.PublicServerDAO;
import dao.VacationPeriodDAO;
import model.PublicServer;
import model.VacationPeriod;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.List;

/**
 * RF-03 — Control de Vacaciones — estandarizado con UITheme
 */
public class VacationWindow extends JFrame {

    private final VacationPeriodDAO vacationDAO = new VacationPeriodDAO();
    private final PublicServerDAO   serverDAO   = new PublicServerDAO();

    private JTable            tableHistory;
    private DefaultTableModel modelHistory;
    private JTable            tableAlerts;
    private DefaultTableModel modelAlerts;

    private JTextField                         txtServerId;
    private JSpinner                           spinYear;
    private JSpinner                           spinAccumulated;
    private JSpinner                           spinUsed;
    private com.toedter.calendar.JDateChooser  dcLastVacation;
    private JTextArea                          txtNotes;

    private JLabel lblServerName, lblAdmission, lblYearsService;
    private JLabel lblTotalAccum, lblTotalUsed, lblTotalPending, lblAlert;

    private PublicServer currentServer;

    // ─────────────────────────────────────────────────────────────────────
    public VacationWindow() {
        UITheme.applyGlobal();
        setTitle("RF-03: Control de Vacaciones");
        setSize(1100, 740);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(UITheme.BG_SOFT);

        add(buildNorthPanel(),  BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildSouthPanel(),  BorderLayout.SOUTH);

        loadAlerts();
    }

    // ─────────────────────────────────────────────────────────────────────
    // NORTE — header + resumen
    // ─────────────────────────────────────────────────────────────────────
    private JPanel buildNorthPanel() {
        JPanel header = UITheme.windowHeader("RF-03 — Control de Vacaciones",
                "Historial y alertas de períodos vacacionales");

        // Búsqueda
        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        busqueda.setOpaque(false);

        JLabel lbl = new JLabel("Cédula:");
        lbl.setFont(UITheme.FONT_H3);
        lbl.setForeground(Color.WHITE);
        busqueda.add(lbl);

        txtServerId = new JTextField(14);
        UITheme.applyInputStyle(txtServerId);
        txtServerId.setBackground(new Color(0x2f, 0x6e, 0x3e));
        txtServerId.setForeground(Color.WHITE);
        txtServerId.setCaretColor(Color.WHITE);
        txtServerId.setPreferredSize(new Dimension(160, UITheme.INPUT_H));
        txtServerId.addActionListener(e -> loadServerById());
        busqueda.add(txtServerId);

        JButton btnLoad = UITheme.secondaryButton("Buscar servidor");
        btnLoad.setForeground(Color.BLACK);
        btnLoad.addActionListener(e -> loadServerById());
        busqueda.add(btnLoad);

        header.add(busqueda, BorderLayout.CENTER);

        // Panel de resumen con métricas
        JPanel resumen = new JPanel(new GridLayout(1, 6, 16, 0));
        resumen.setOpaque(false);
        resumen.setBorder(new EmptyBorder(8, 0, 0, 0));

        lblServerName   = makeMetricVal("—", Color.WHITE);
        lblAdmission    = makeMetricVal("—", new Color(0xbb, 0xe5, 0xc4));
        lblYearsService = makeMetricVal("—", new Color(0xbb, 0xe5, 0xc4));
        lblTotalAccum   = makeMetricVal("—", Color.WHITE);
        lblTotalUsed    = makeMetricVal("—", new Color(0xbb, 0xe5, 0xc4));
        lblTotalPending = makeMetricVal("—", Color.WHITE);

        resumen.add(makeMetricGroup("Servidor",           lblServerName));
        resumen.add(makeMetricGroup("Fecha de ingreso",   lblAdmission));
        resumen.add(makeMetricGroup("Años de servicio",   lblYearsService));
        resumen.add(makeMetricGroup("Días acumulados",    lblTotalAccum));
        resumen.add(makeMetricGroup("Días disfrutados",   lblTotalUsed));
        resumen.add(makeMetricGroup("Días pendientes",    lblTotalPending));

        // Alerta
        lblAlert = new JLabel(" ");
        lblAlert.setForeground(new Color(0xff, 0xeb, 0x96));
        lblAlert.setFont(UITheme.FONT_SMALL.deriveFont(Font.BOLD));

        JPanel resumenOuter = new JPanel(new BorderLayout(0, 4));
        resumenOuter.setOpaque(false);
        resumenOuter.add(resumen,  BorderLayout.CENTER);
        resumenOuter.add(lblAlert, BorderLayout.SOUTH);

        header.add(busqueda, BorderLayout.CENTER);
        header.add(resumenOuter, BorderLayout.SOUTH);

        return header;
    }

    private JLabel makeMetricVal(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setForeground(color);
        l.setFont(UITheme.FONT_H3);
        return l;
    }

    private JPanel makeMetricGroup(String title, JLabel valor) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setForeground(new Color(0xbb, 0xe5, 0xc4));
        t.setFont(UITheme.FONT_SMALL);
        p.add(t, BorderLayout.NORTH);
        p.add(valor, BorderLayout.CENTER);
        return p;
    }

    // ─────────────────────────────────────────────────────────────────────
    // CENTRO — tabla historial | formulario
    // ─────────────────────────────────────────────────────────────────────
    private JSplitPane buildCenterPanel() {
        // ── Tabla historial ──
        modelHistory = new DefaultTableModel(
                new String[]{"ID", "Año", "Acumulados", "Usados", "Pendientes", "Última vacación", "Notas"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableHistory = UITheme.styledTable(modelHistory);
        tableHistory.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableHistory.getColumnModel().getColumn(0).setMaxWidth(50);
        tableHistory.getColumnModel().getColumn(1).setMaxWidth(60);
        tableHistory.getColumnModel().getColumn(2).setMaxWidth(90);
        tableHistory.getColumnModel().getColumn(3).setMaxWidth(70);
        tableHistory.getColumnModel().getColumn(4).setMaxWidth(90);

        tableHistory.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                if (!sel) {
                    Object pend = modelHistory.getValueAt(row, 4);
                    int dias = pend instanceof Integer i ? i : 0;
                    c.setBackground(dias > VacationPeriodDAO.DIAS_POR_ANIO
                            ? UITheme.ERROR_LIGHT
                            : dias > 0
                              ? UITheme.WARNING_LIGHT
                              : UITheme.SUCCESS_LIGHT);
                }
                return c;
            }
        });

        tableHistory.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableHistory.getSelectedRow() >= 0)
                fillFormFromRow(tableHistory.getSelectedRow());
        });

        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBackground(UITheme.BG_SOFT);
        historyPanel.setBorder(new EmptyBorder(UITheme.PAD, UITheme.PAD, UITheme.PAD, UITheme.PAD_SM));

        JLabel histTitle = UITheme.sectionLabel("Historial de períodos");
        historyPanel.add(histTitle, BorderLayout.NORTH);
        historyPanel.add(Box.createVerticalStrut(UITheme.PAD_SM), BorderLayout.CENTER); // spacer
        historyPanel.add(UITheme.tableScroll(tableHistory), BorderLayout.CENTER);

        JPanel histWrapper = new JPanel(new BorderLayout(0, UITheme.PAD_SM));
        histWrapper.setBackground(UITheme.BG_SOFT);
        histWrapper.setBorder(new EmptyBorder(UITheme.PAD, UITheme.PAD, UITheme.PAD, UITheme.PAD_SM));
        histWrapper.add(UITheme.sectionLabel("Historial de períodos"), BorderLayout.NORTH);
        histWrapper.add(UITheme.tableScroll(tableHistory), BorderLayout.CENTER);

        // ── Formulario ──
        JPanel formOuter = new JPanel(new BorderLayout());
        formOuter.setBackground(UITheme.BG_SOFT);
        formOuter.setBorder(new EmptyBorder(UITheme.PAD, UITheme.PAD_SM, UITheme.PAD, UITheme.PAD));

        UITheme.Card formCard = new UITheme.Card(UITheme.PAD);
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, UITheme.PAD_SM, 0);
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.weightx = 1;
        formCard.add(UITheme.sectionLabel("Registrar / actualizar período"), gbc);
        gbc.gridwidth = 1;
        row++;

        // Año
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formCard.add(UITheme.fieldLabel("Año del período"), gbc);
        spinYear = new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear() - 1, 2000, 2100, 1));
        ((JSpinner.DefaultEditor) spinYear.getEditor()).getTextField().setColumns(6);
        gbc.gridx = 1; gbc.weightx = 1;
        formCard.add(spinYear, gbc);
        row++;

        // Días acumulados
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formCard.add(UITheme.fieldLabel("Días acumulados"), gbc);
        spinAccumulated = new JSpinner(new SpinnerNumberModel(VacationPeriodDAO.DIAS_POR_ANIO, 0, 365, 1));
        ((JSpinner.DefaultEditor) spinAccumulated.getEditor()).getTextField().setColumns(5);
        JPanel accumRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        accumRow.setOpaque(false);
        accumRow.add(spinAccumulated);
        JLabel noteAccum = new JLabel("  (15 = régimen general)");
        noteAccum.setForeground(UITheme.TEXT_HINT);
        noteAccum.setFont(UITheme.FONT_CAPTION);
        accumRow.add(noteAccum);
        gbc.gridx = 1; gbc.weightx = 1;
        formCard.add(accumRow, gbc);
        row++;

        // Días usados
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formCard.add(UITheme.fieldLabel("Días disfrutados"), gbc);
        spinUsed = new JSpinner(new SpinnerNumberModel(0, 0, 365, 1));
        ((JSpinner.DefaultEditor) spinUsed.getEditor()).getTextField().setColumns(5);
        gbc.gridx = 1; gbc.weightx = 1;
        formCard.add(spinUsed, gbc);
        row++;

        // Última fecha de vacación
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formCard.add(UITheme.fieldLabel("Última vacación"), gbc);
        dcLastVacation = new com.toedter.calendar.JDateChooser();
        dcLastVacation.setFont(UITheme.FONT_BODY);
        dcLastVacation.setBackground(UITheme.BG);
        dcLastVacation.setPreferredSize(new Dimension(dcLastVacation.getPreferredSize().width, UITheme.INPUT_H));
        dcLastVacation.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));
        gbc.gridx = 1; gbc.weightx = 1;
        formCard.add(dcLastVacation, gbc);
        row++;

        // Notas
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.anchor = GridBagConstraints.NORTHWEST;
        formCard.add(UITheme.fieldLabel("Notas"), gbc);
        txtNotes = UITheme.styledTextArea(3);
        gbc.gridx = 1; gbc.weightx = 1; gbc.weighty = 1; gbc.fill = GridBagConstraints.BOTH;
        formCard.add(UITheme.textAreaScroll(txtNotes), gbc);
        row++;

        // Botones
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.weighty = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor  = GridBagConstraints.EAST;
        gbc.insets  = new Insets(UITheme.PAD, 0, 0, 0);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        JButton btnLimpiar  = UITheme.ghostButton("Limpiar");
        JButton btnEliminar = UITheme.dangerButton("Eliminar");
        JButton btnGuardar  = UITheme.primaryButton("Guardar");
        btnLimpiar.addActionListener(e  -> clearForm());
        btnEliminar.addActionListener(e -> deleteSelected());
        btnGuardar.addActionListener(e  -> savePeriod());
        btnPanel.add(btnLimpiar); btnPanel.add(btnEliminar); btnPanel.add(btnGuardar);
        formCard.add(btnPanel, gbc);

        formOuter.add(formCard, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, histWrapper, formOuter);
        split.setDividerLocation(580);
        split.setResizeWeight(0.6);
        split.setBorder(null);
        return split;
    }

    // ─────────────────────────────────────────────────────────────────────
    // SUR — tabla de alertas
    // ─────────────────────────────────────────────────────────────────────
    private JPanel buildSouthPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, UITheme.PAD_SM));
        panel.setBackground(UITheme.BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER),
                new EmptyBorder(UITheme.PAD_SM, UITheme.PAD, UITheme.PAD_SM, UITheme.PAD)));
        panel.setPreferredSize(new Dimension(0, 170));

        JPanel alertHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        alertHeader.setOpaque(false);
        alertHeader.add(UITheme.sectionLabel("⚠  Alertas — Servidores con más de 1 período pendiente"));
        panel.add(alertHeader, BorderLayout.NORTH);

        modelAlerts = new DefaultTableModel(
                new String[]{"Cédula", "Nombre", "Fecha ingreso", "Años",
                        "Días acum.", "Días usados", "Pendientes", "Períodos"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableAlerts = UITheme.styledTable(modelAlerts);
        tableAlerts.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                if (!sel) {
                    Object p = modelAlerts.getValueAt(row, 7);
                    int per = p instanceof Integer i ? i : 0;
                    c.setBackground(per >= 3 ? UITheme.ERROR_LIGHT : UITheme.WARNING_LIGHT);
                }
                return c;
            }
        });
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

        JLabel hint = UITheme.bodyLabel("  Doble clic en una fila para cargar el historial arriba.");
        hint.setFont(UITheme.FONT_CAPTION);
        hint.setForeground(UITheme.TEXT_HINT);

        panel.add(UITheme.tableScroll(tableAlerts), BorderLayout.CENTER);
        panel.add(hint, BorderLayout.SOUTH);
        return panel;
    }

    // ─────────────────────────────────────────────────────────────────────
    // LÓGICA
    // ─────────────────────────────────────────────────────────────────────
    private void loadServerById() {
        String cedula = txtServerId.getText().trim();
        if (cedula.isEmpty()) { UITheme.showWarning(this, "Ingresa una cédula."); return; }
        currentServer = serverDAO.findByIdNumber(cedula);
        if (currentServer == null) {
            showDialog(this, "No encontrado", "No se encontró servidor con cédula: " + cedula);
            clearResumen(); return;
        }
        setTitle("RF-03: Vacaciones — " + currentServer.getFirstName() + " " + currentServer.getLastName());
        refreshResumen(); refreshHistory(); clearForm();
        spinYear.setValue(LocalDate.now().getYear() - 1);
        spinAccumulated.setValue(VacationPeriodDAO.DIAS_POR_ANIO);
    }

    private void refreshResumen() {
        if (currentServer == null) return;
        int acum = vacationDAO.totalAccumulatedDays(currentServer);
        int usado = vacationDAO.totalUsedDays(currentServer);
        int pend  = vacationDAO.totalPendingDays(currentServer);
        int per   = vacationDAO.pendingPeriods(currentServer);

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

        if (per > 1) {
            lblTotalPending.setForeground(new Color(0xff, 0xb4, 0x64));
            lblAlert.setText("⚠  ALERTA: " + per + " período(s) sin disfrutar. Debe programar vacaciones.");
        } else if (pend > 0) {
            lblTotalPending.setForeground(new Color(0xff, 0xeb, 0x96));
            lblAlert.setText("Tiene días pendientes del período actual.");
        } else {
            lblTotalPending.setForeground(UITheme.PRIMARY_LIGHT);
            lblAlert.setText("✓  Al día en vacaciones.");
        }
    }

    private void clearResumen() {
        lblServerName.setText("—"); lblAdmission.setText("—");
        lblYearsService.setText("—"); lblTotalAccum.setText("—");
        lblTotalUsed.setText("—"); lblTotalPending.setText("—");
        lblAlert.setText(" ");
        setTitle("RF-03: Control de Vacaciones");
    }

    private void refreshHistory() {
        modelHistory.setRowCount(0);
        if (currentServer == null) return;
        for (VacationPeriod vp : vacationDAO.findByServer(currentServer)) {
            modelHistory.addRow(new Object[]{
                    vp.getId(), vp.getYear(), vp.getAccumulatedDays(),
                    vp.getUsedDays(), vp.getPendingDays(),
                    vp.getLastVacationDate() != null ? vp.getLastVacationDate().toString() : "—",
                    nvl(vp.getNotes())
            });
        }
    }

    private void loadAlerts() {
        modelAlerts.setRowCount(0);
        List<PublicServer> activos = serverDAO.findAllActive();
        for (PublicServer s : vacationDAO.findServersInDebt(activos)) {
            int acum = vacationDAO.totalAccumulatedDays(s);
            int usado = vacationDAO.totalUsedDays(s);
            int pend  = vacationDAO.totalPendingDays(s);
            int per   = vacationDAO.pendingPeriods(s);
            int anios = s.getAdmissionDate() != null
                    ? Period.between(s.getAdmissionDate(), LocalDate.now()).getYears() : 0;
            modelAlerts.addRow(new Object[]{
                    s.getIdNumber(), s.getFirstName() + " " + s.getLastName(),
                    s.getAdmissionDate() != null ? s.getAdmissionDate().toString() : "—",
                    anios, acum, usado, pend, per
            });
        }
    }

    private void savePeriod() {
        if (currentServer == null) { UITheme.showWarning(this, "Carga primero un servidor."); return; }
        VacationPeriod vp = new VacationPeriod();
        vp.setServer(currentServer);
        vp.setYear((Integer) spinYear.getValue());
        vp.setAccumulatedDays((Integer) spinAccumulated.getValue());
        vp.setUsedDays((Integer) spinUsed.getValue());
        if (dcLastVacation.getDate() != null)
            vp.setLastVacationDate(dcLastVacation.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        vp.setNotes(txtNotes.getText().trim());
        try {
            vacationDAO.saveWithValidation(vp);
            UITheme.showSuccess(this, "Período guardado correctamente.");
            refreshHistory(); refreshResumen(); loadAlerts(); clearForm();
        } catch (IllegalArgumentException ex) {
            UITheme.showError(this, ex.getMessage());
        } catch (Exception ex) {
            UITheme.showError(this, "Error inesperado: " + ex.getMessage());
        }
    }

    private void deleteSelected() {
        int row = tableHistory.getSelectedRow();
        if (row < 0) { UITheme.showWarning(this, "Selecciona un período de la tabla."); return; }
        Long id = (Long) modelHistory.getValueAt(row, 0);
        Integer yr = (Integer) modelHistory.getValueAt(row, 1);
        if (UITheme.showConfirm(this, "¿Eliminar el registro de vacaciones del año " + yr + "?\nEsta acción no se puede deshacer.")) {
            try {
                vacationDAO.delete(id);
                refreshHistory(); refreshResumen(); loadAlerts(); clearForm();
                UITheme.showSuccess(this, "Período eliminado.");
            } catch (Exception ex) {
                UITheme.showError(this, "Error al eliminar: " + ex.getMessage());
            }
        }
    }

    private void fillFormFromRow(int row) {
        Long id = (Long) modelHistory.getValueAt(row, 0);
        if (id == null || currentServer == null) return;
        vacationDAO.findByServer(currentServer).stream()
                .filter(v -> id.equals(v.getId()))
                .findFirst()
                .ifPresent(v -> {
                    if (v.getYear()            != null) spinYear.setValue(v.getYear());
                    if (v.getAccumulatedDays() != null) spinAccumulated.setValue(v.getAccumulatedDays());
                    if (v.getUsedDays()        != null) spinUsed.setValue(v.getUsedDays());
                    dcLastVacation.setDate(v.getLastVacationDate() != null
                            ? java.sql.Date.valueOf(v.getLastVacationDate()) : null);
                    txtNotes.setText(nvl(v.getNotes()));
                });
    }

    private void clearForm() {
        spinYear.setValue(LocalDate.now().getYear() - 1);
        spinAccumulated.setValue(VacationPeriodDAO.DIAS_POR_ANIO);
        spinUsed.setValue(0);
        dcLastVacation.setDate(null);
        txtNotes.setText("");
        tableHistory.clearSelection();
    }

    private void showDialog(Component parent, String title, String msg) {
        JOptionPane.showMessageDialog(parent, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private String nvl(String s) { return s != null ? s : ""; }
}