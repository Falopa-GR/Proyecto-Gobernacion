package view;

import dao.AdministrativeSituationDAO;
import dao.PublicServerDAO;
import model.AdministrativeSituation;
import model.AdministrativeSituation.SituationType;
import model.PublicServer;
import service.OverlappingSituationException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;


/**
 * RF-02 — Situaciones Administrativas — estandarizado con UITheme
 */
public class AdministrativeSituationWindow extends JFrame {

    private final AdministrativeSituationDAO situationDAO    = new AdministrativeSituationDAO();
    private final PublicServerDAO            publicServerDAO = new PublicServerDAO();

    private JTable            table;
    private DefaultTableModel tableModel;

    private JTextField               txtServerId;
    private JComboBox<SituationType> comboTipo;
    private com.toedter.calendar.JDateChooser dcStart;
    private com.toedter.calendar.JDateChooser dcEnd;
    private JTextField               txtAct;
    private JTextArea                txtNotes;

    private PublicServer currentServer;
    private JLabel       lblSituacionActual;

    // ─────────────────────────────────────────────────────────────────────
    private static String labelFor(SituationType t) {
        return switch (t) {
            case VACATION            -> "Vacaciones";
            case PERMISSION_1_DAY    -> "Permiso 1 día";
            case PERMISSION_2_3_DAYS -> "Permiso 2-3 días";
            case LICENSE_PAID        -> "Licencia remunerada";
            case LICENSE_UNPAID      -> "Licencia no remunerada";
            case MATERNITY           -> "Licencia maternidad";
            case PATERNITY           -> "Licencia paternidad";
            case ILLNESS             -> "Licencia por enfermedad";
            case ASSIGNMENT          -> "Encargo";
            case TRANSFER            -> "Traslado";
            case COMMISSION          -> "Comisión";
        };
    }

    // ─────────────────────────────────────────────────────────────────────
    public AdministrativeSituationWindow() {
        UITheme.applyGlobal();
        setTitle("RF-02: Situaciones Administrativas");
        setSize(1050, 660);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(UITheme.BG_SOFT);

        add(buildNorthPanel(),  BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildSouthPanel(),  BorderLayout.SOUTH);
    }

    // ─────────────────────────────────────────────────────────────────────
    // NORTE
    // ─────────────────────────────────────────────────────────────────────
    private JPanel buildNorthPanel() {
        JPanel header = UITheme.windowHeader("RF-02 — Situaciones Administrativas",
                "Registro y consulta de situaciones por servidor");

        // Barra de búsqueda embebida en el header
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

        // Situación actual
        lblSituacionActual = new JLabel("  Situación actual: —");
        lblSituacionActual.setForeground(new Color(0xbb, 0xe5, 0xc4));
        lblSituacionActual.setFont(UITheme.FONT_CAPTION);
        header.add(lblSituacionActual, BorderLayout.SOUTH);

        return header;
    }

    // ─────────────────────────────────────────────────────────────────────
    // CENTRO
    // ─────────────────────────────────────────────────────────────────────
    private JSplitPane buildCenterPanel() {
        // ── Tabla ──
        tableModel = new DefaultTableModel(
                new String[]{"ID", "Tipo", "Inicio", "Fin", "Acto administrativo", "Notas"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = UITheme.styledTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setMaxWidth(55);

        // Colores de filas
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                if (!sel) {
                    Object fin = tableModel.getValueAt(row, 3);
                    if (fin instanceof LocalDate endDate) {
                        Object ini = tableModel.getValueAt(row, 2);
                        LocalDate hoy = LocalDate.now();
                        if (ini instanceof LocalDate sd && !hoy.isBefore(sd) && !hoy.isAfter(endDate))
                            c.setBackground(UITheme.SUCCESS_LIGHT);
                        else if (endDate.isBefore(hoy))
                            c.setBackground(UITheme.BG_SOFT);
                        else
                            c.setBackground(UITheme.WARNING_LIGHT);
                    } else {
                        c.setBackground(row % 2 == 0 ? UITheme.BG : UITheme.ROW_ALT);
                    }
                }
                return c;
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0)
                fillFormFromRow(table.getSelectedRow());
        });

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(UITheme.BG_SOFT);
        tableWrapper.setBorder(new EmptyBorder(UITheme.PAD, UITheme.PAD, UITheme.PAD, UITheme.PAD_SM));
        tableWrapper.add(UITheme.tableScroll(table), BorderLayout.CENTER);

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

        // Título de sección
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.weightx = 1;
        formCard.add(UITheme.sectionLabel("Registrar / ver situación"), gbc);
        gbc.gridwidth = 1;
        row++;

        // Tipo
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formCard.add(UITheme.fieldLabel("Tipo de situación"), gbc);
        comboTipo = UITheme.styledCombo(SituationType.values());
        comboTipo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object val,
                                                          int idx, boolean sel, boolean focus) {
                super.getListCellRendererComponent(list, val, idx, sel, focus);
                if (val instanceof SituationType st) setText(labelFor(st));
                return this;
            }
        });
        gbc.gridx = 1; gbc.weightx = 1;
        formCard.add(comboTipo, gbc);
        row++;

        // Fechas
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formCard.add(UITheme.fieldLabel("Fecha inicio"), gbc);
        dcStart = new com.toedter.calendar.JDateChooser();
        styleDate(dcStart);
        gbc.gridx = 1; gbc.weightx = 1;
        formCard.add(dcStart, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formCard.add(UITheme.fieldLabel("Fecha fin"), gbc);
        dcEnd = new com.toedter.calendar.JDateChooser();
        styleDate(dcEnd);
        gbc.gridx = 1; gbc.weightx = 1;
        formCard.add(dcEnd, gbc);
        row++;

        // Acto administrativo
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formCard.add(UITheme.fieldLabel("Acto administrativo"), gbc);
        txtAct = UITheme.styledInput("Ej: Resolución 0045 de 2025");
        gbc.gridx = 1; gbc.weightx = 1;
        formCard.add(txtAct, gbc);
        row++;

        // Notas
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.anchor = GridBagConstraints.NORTHWEST;
        formCard.add(UITheme.fieldLabel("Notas"), gbc);
        txtNotes = UITheme.styledTextArea(4);
        JScrollPane spNotes = UITheme.textAreaScroll(txtNotes);
        gbc.gridx = 1; gbc.weightx = 1; gbc.weighty = 1; gbc.fill = GridBagConstraints.BOTH;
        formCard.add(spNotes, gbc);
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
        btnGuardar.addActionListener(e  -> saveSituation());
        btnPanel.add(btnLimpiar); btnPanel.add(btnEliminar); btnPanel.add(btnGuardar);
        formCard.add(btnPanel, gbc);

        formOuter.add(formCard, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableWrapper, formOuter);
        split.setDividerLocation(560);
        split.setResizeWeight(0.6);
        split.setBorder(null);
        return split;
    }

    // ─────────────────────────────────────────────────────────────────────
    // SUR — leyenda
    // ─────────────────────────────────────────────────────────────────────
    private JPanel buildSouthPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 6));
        p.setBackground(UITheme.BG);
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));
        p.add(colorLegend(UITheme.SUCCESS_LIGHT, UITheme.PRIMARY_DARK,  "Activa hoy"));
        p.add(colorLegend(UITheme.WARNING_LIGHT, UITheme.WARNING,       "Futura"));
        p.add(colorLegend(UITheme.BG_SOFT,       UITheme.TEXT_SUB,      "Finalizada"));
        return p;
    }

    private JPanel colorLegend(Color bg, Color fg, String label) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        JPanel sq = new JPanel();
        sq.setBackground(bg);
        sq.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        sq.setPreferredSize(new Dimension(14, 14));
        p.add(sq);
        JLabel l = UITheme.bodyLabel(label);
        l.setForeground(fg);
        l.setFont(UITheme.FONT_SMALL);
        p.add(l);
        return p;
    }

    // ─────────────────────────────────────────────────────────────────────
    // LÓGICA
    // ─────────────────────────────────────────────────────────────────────
    private void loadServerById() {
        String id = txtServerId.getText().trim();
        if (id.isEmpty()) { UITheme.showWarning(this, "Ingresa una cédula."); return; }
        currentServer = publicServerDAO.findByIdNumber(id);
        if (currentServer == null) {
            showDialog(this, "No encontrado", "No se encontró servidor con cédula: " + id);
            lblSituacionActual.setText("  Situación actual: —");
            return;
        }
        setTitle("RF-02: Situaciones — " + currentServer.getFirstName() + " " + currentServer.getLastName());
        refreshTable();
        refreshSituacionActual();
        clearForm();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        if (currentServer == null) return;
        for (AdministrativeSituation a : situationDAO.findByServer(currentServer)) {
            tableModel.addRow(new Object[]{
                    a.getId(),
                    a.getType() != null ? labelFor(a.getType()) : "(sin tipo)",
                    a.getStartDate(),
                    a.getEndDate(),
                    nvl(a.getAdministrativeAct()),
                    nvl(a.getNotes())
            });
        }
    }

    private void refreshSituacionActual() {
        if (currentServer == null) return;
        LocalDate hoy = LocalDate.now();
        AdministrativeSituation activa = situationDAO.findByServer(currentServer).stream()
                .filter(s -> s.getStartDate() != null && s.getEndDate() != null
                        && !hoy.isBefore(s.getStartDate()) && !hoy.isAfter(s.getEndDate()))
                .findFirst().orElse(null);
        if (activa == null) {
            lblSituacionActual.setText("  Situación actual: En actividad normal ");
            lblSituacionActual.setForeground(UITheme.PRIMARY_LIGHT);
        } else {
            lblSituacionActual.setText("  Situación actual: " + labelFor(activa.getType())
                    + "  |  " + activa.getStartDate() + " → " + activa.getEndDate()
                    + "  |  Acto: " + nvl(activa.getAdministrativeAct()));
            lblSituacionActual.setForeground(new Color(0xff, 0xeb, 0x96));
        }
    }

    private void saveSituation() {
        if (currentServer == null) { UITheme.showWarning(this, "Carga primero un servidor."); return; }
        if (comboTipo.getSelectedItem() == null) { UITheme.showWarning(this, "Selecciona el tipo de situación."); return; }
        if (dcStart.getDate() == null || dcEnd.getDate() == null) { UITheme.showWarning(this, "Las fechas de inicio y fin son obligatorias."); return; }
        if (txtAct.getText().trim().isEmpty()) { UITheme.showWarning(this, "El acto administrativo es obligatorio."); return; }

        AdministrativeSituation s = new AdministrativeSituation();
        s.setServer(currentServer);
        s.setType((SituationType) comboTipo.getSelectedItem());
        s.setStartDate(dcStart.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        s.setEndDate(dcEnd.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        s.setAdministrativeAct(txtAct.getText().trim());
        s.setNotes(txtNotes.getText().trim());

        try {
            situationDAO.saveWithValidation(s);
            UITheme.showSuccess(this, "Situación registrada correctamente.");
            refreshTable(); refreshSituacionActual(); clearForm();
        } catch (OverlappingSituationException ex) {
            UITheme.showError(this, "Conflicto de fechas:\n\n" + ex.getMessage()
                    + "\n\nVerifica las fechas existentes en la tabla.");
        } catch (IllegalArgumentException ex) {
            UITheme.showError(this, "Datos inválidos: " + ex.getMessage());
        } catch (Exception ex) {
            UITheme.showError(this, "Error inesperado: " + ex.getMessage());
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { UITheme.showWarning(this, "Selecciona una situación de la tabla."); return; }
        Long   id   = (Long)   tableModel.getValueAt(row, 0);
        String tipo = (String) tableModel.getValueAt(row, 1);
        if (UITheme.showConfirm(this, "¿Eliminar la situación \"" + tipo + "\" (ID " + id + ")?\nEsta acción no se puede deshacer.")) {
            try {
                situationDAO.delete(id);
                refreshTable(); refreshSituacionActual(); clearForm();
                UITheme.showSuccess(this, "Situación eliminada correctamente.");
            } catch (Exception ex) {
                UITheme.showError(this, "Error al eliminar: " + ex.getMessage());
            }
        }
    }

    private void fillFormFromRow(int row) {
        Long id = (Long) tableModel.getValueAt(row, 0);
        if (id == null) return;
        situationDAO.findByServer(currentServer).stream()
                .filter(s -> id.equals(s.getId()))
                .findFirst()
                .ifPresent(s -> {
                    if (s.getType() != null) comboTipo.setSelectedItem(s.getType());
                    if (s.getStartDate() != null) dcStart.setDate(java.sql.Date.valueOf(s.getStartDate()));
                    if (s.getEndDate()   != null) dcEnd.setDate(java.sql.Date.valueOf(s.getEndDate()));
                    txtAct.setText(nvl(s.getAdministrativeAct()));
                    txtNotes.setText(nvl(s.getNotes()));
                });
    }

    private void clearForm() {
        comboTipo.setSelectedIndex(0);
        dcStart.setDate(null); dcEnd.setDate(null);
        txtAct.setText(""); txtNotes.setText("");
        table.clearSelection();
    }

    private void styleDate(com.toedter.calendar.JDateChooser dc) {
        dc.setFont(UITheme.FONT_BODY);
        dc.setBackground(UITheme.BG);
        dc.setPreferredSize(new Dimension(dc.getPreferredSize().width, UITheme.INPUT_H));
        dc.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));
    }

    private void showDialog(Component parent, String title, String msg) {
        JOptionPane.showMessageDialog(parent, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private String nvl(String s) { return s != null ? s : ""; }
}