package view;

import dao.AdministrativeSituationDAO;
import dao.PublicServerDAO;
import model.AdministrativeSituation;
import model.AdministrativeSituation.SituationType;
import model.PublicServer;
import service.OverlappingSituationException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * RF-02 — Ventana de Situaciones Administrativas
 *
 * Correcciones aplicadas sobre la versión anterior:
 *   1. txtType (JTextField libre) → comboTipo (JComboBox<SituationType>)
 *      El usuario ya no puede escribir un tipo inválido.
 *   2. txtAct se declara como campo de instancia para poder leerlo en saveSituation().
 *   3. Etiquetas del ComboBox en español (renderer) pero el valor guardado
 *      sigue siendo el enum correcto que Hibernate espera.
 *   4. Panel superior muestra la situación activa actual del servidor cargado.
 *   5. Filas de la tabla se colorean: verde = activa hoy, gris = pasada.
 *   6. Botón "Limpiar" para resetear el formulario sin cerrar la ventana.
 *   7. Se captura OverlappingSituationException específicamente con mensaje claro.
 */
public class AdministrativeSituationWindow extends JFrame {

    // ── DAOs ──────────────────────────────────────────────────────────────
    private final AdministrativeSituationDAO situationDAO = new AdministrativeSituationDAO();
    private final PublicServerDAO            publicServerDAO = new PublicServerDAO();

    // ── Tabla ─────────────────────────────────────────────────────────────
    private JTable             table;
    private DefaultTableModel  tableModel;

    // ── Formulario ────────────────────────────────────────────────────────
    private JTextField                   txtServerId;
    private JComboBox<SituationType>     comboTipo;      // ← CORREGIDO: era JTextField libre
    private com.toedter.calendar.JDateChooser dcStart;
    private com.toedter.calendar.JDateChooser dcEnd;
    private JTextField                   txtAct;         // ← CORREGIDO: ahora es campo de instancia
    private JTextArea                    txtNotes;

    // ── Estado ────────────────────────────────────────────────────────────
    private PublicServer currentServer;
    private JLabel       lblSituacionActual;

    // ── Etiquetas en español para cada valor del enum ─────────────────────
    private static String labelFor(SituationType t) {
        return switch (t) {
            case VACATION          -> "Vacaciones";
            case PERMISSION_1_DAY  -> "Permiso 1 día";
            case PERMISSION_2_3_DAYS -> "Permiso 2-3 días";
            case LICENSE_PAID      -> "Licencia remunerada";
            case LICENSE_UNPAID    -> "Licencia no remunerada";
            case MATERNITY         -> "Licencia maternidad";
            case PATERNITY         -> "Licencia paternidad";
            case ILLNESS           -> "Licencia por enfermedad";
            case ASSIGNMENT        -> "Encargo";
            case TRANSFER          -> "Traslado";
            case COMMISSION        -> "Comisión";
        };
    }

    // ─────────────────────────────────────────────────────────────────────
    public AdministrativeSituationWindow() {
        setTitle("RF-02: Situaciones Administrativas");
        setSize(1050, 640);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        add(buildNorthPanel(),  BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildSouthPanel(),  BorderLayout.SOUTH);
    }

    // ─────────────────────────────────────────────────────────────────────
    // PANEL NORTE — búsqueda de servidor + situación actual
    // ─────────────────────────────────────────────────────────────────────
    private JPanel buildNorthPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(new Color(52, 152, 219));
        outer.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        // Fila superior: búsqueda
        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        busqueda.setOpaque(false);

        JLabel lbl = new JLabel("Cédula:");
        lbl.setForeground(Color.WHITE);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        busqueda.add(lbl);

        txtServerId = new JTextField(14);
        txtServerId.addActionListener(e -> loadServerById()); // Enter también carga
        busqueda.add(txtServerId);

        JButton btnLoad = new JButton("Cargar servidor");
        btnLoad.addActionListener(e -> loadServerById());
        busqueda.add(btnLoad);

        outer.add(busqueda, BorderLayout.WEST);

        // Fila inferior: situación activa actual
        lblSituacionActual = new JLabel("  Situación actual: —");
        lblSituacionActual.setForeground(new Color(236, 240, 241));
        lblSituacionActual.setFont(lblSituacionActual.getFont().deriveFont(Font.ITALIC, 12f));
        outer.add(lblSituacionActual, BorderLayout.SOUTH);

        return outer;
    }

    // ─────────────────────────────────────────────────────────────────────
    // PANEL CENTRAL — tabla izquierda | formulario derecha
    // ─────────────────────────────────────────────────────────────────────
    private JSplitPane buildCenterPanel() {
        // ── Tabla ──
        tableModel = new DefaultTableModel(
                new String[]{"ID", "Tipo", "Inicio", "Fin", "Acto administrativo", "Notas"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setMaxWidth(55);

        // Color de filas: verde si la situación está activa hoy, gris si es pasada
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                if (!sel) {
                    Object fin = tableModel.getValueAt(row, 3); // columna "Fin"
                    if (fin instanceof LocalDate endDate) {
                        Object ini = tableModel.getValueAt(row, 2);
                        LocalDate hoy = LocalDate.now();
                        if (ini instanceof LocalDate startDate
                                && !hoy.isBefore(startDate) && !hoy.isAfter(endDate)) {
                            c.setBackground(new Color(212, 237, 218)); // verde claro = activa
                        } else if (endDate.isBefore(hoy)) {
                            c.setBackground(new Color(240, 240, 240)); // gris = pasada
                        } else {
                            c.setBackground(new Color(255, 243, 205)); // amarillo = futura
                        }
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        });

        // Al seleccionar fila, llenar formulario para ver/editar
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0)
                fillFormFromRow(table.getSelectedRow());
        });

        JScrollPane sp = new JScrollPane(table);

        // ── Formulario ──
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Registrar / ver situación"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 2, 8);
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Tipo — CORREGIDO: JComboBox con todos los valores del enum
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(new JLabel("Tipo de situación:"), gbc);

        comboTipo = new JComboBox<>(SituationType.values());
        // Renderer que muestra la etiqueta en español en lugar del nombre del enum
        comboTipo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object val, int idx, boolean sel, boolean focus) {
                super.getListCellRendererComponent(list, val, idx, sel, focus);
                if (val instanceof SituationType st) setText(labelFor(st));
                return this;
            }
        });
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(comboTipo, gbc);
        row++;

        // Fecha inicio
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(new JLabel("Fecha inicio:"), gbc);
        dcStart = new com.toedter.calendar.JDateChooser();
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(dcStart, gbc);
        row++;

        // Fecha fin
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(new JLabel("Fecha fin:"), gbc);
        dcEnd = new com.toedter.calendar.JDateChooser();
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(dcEnd, gbc);
        row++;

        // Acto administrativo — CORREGIDO: ahora es campo de instancia
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(new JLabel("Acto administrativo:"), gbc);
        txtAct = new JTextField();
        txtAct.setToolTipText("Ej: Resolución 0045 de 2025");
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(txtAct, gbc);
        row++;

        // Notas
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("Notas:"), gbc);
        txtNotes = new JTextArea(4, 20);
        txtNotes.setLineWrap(true);
        txtNotes.setWrapStyleWord(true);
        JScrollPane spNotes = new JScrollPane(txtNotes);
        gbc.gridx = 1; gbc.weightx = 1; gbc.weighty = 1; gbc.fill = GridBagConstraints.BOTH;
        form.add(spNotes, gbc);
        row++;

        // Botones
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.weighty = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(12, 8, 8, 8);

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
        btnGuardar.addActionListener(e -> saveSituation());

        btnPanel.add(btnLimpiar);
        btnPanel.add(btnEliminar);
        btnPanel.add(btnGuardar);
        form.add(btnPanel, gbc);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sp, form);
        split.setDividerLocation(560);
        split.setResizeWeight(0.6);
        return split;
    }

    // ─────────────────────────────────────────────────────────────────────
    // PANEL SUR — leyenda de colores
    // ─────────────────────────────────────────────────────────────────────
    private JPanel buildSouthPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 4));
        p.setBackground(new Color(236, 240, 241));

        p.add(colorLegend(new Color(212, 237, 218), "Activa hoy"));
        p.add(colorLegend(new Color(255, 243, 205), "Futura"));
        p.add(colorLegend(new Color(240, 240, 240), "Finalizada"));

        return p;
    }

    private JPanel colorLegend(Color c, String label) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        JPanel square = new JPanel();
        square.setBackground(c);
        square.setPreferredSize(new Dimension(14, 14));
        square.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        p.add(square);
        p.add(new JLabel(label));
        return p;
    }

    // ─────────────────────────────────────────────────────────────────────
    // LÓGICA: cargar servidor por cédula
    // ─────────────────────────────────────────────────────────────────────
    private void loadServerById() {
        String id = txtServerId.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa una cédula.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        currentServer = publicServerDAO.findByIdNumber(id);

        if (currentServer == null) {
            JOptionPane.showMessageDialog(this,
                    "No se encontró un servidor con cédula: " + id,
                    "No encontrado", JOptionPane.INFORMATION_MESSAGE);
            lblSituacionActual.setText("  Situación actual: —");
            return;
        }

        setTitle("RF-02: Situaciones — " + currentServer.getFirstName() + " " + currentServer.getLastName());
        refreshTable();
        refreshSituacionActual();
        clearForm();
    }

    // ─────────────────────────────────────────────────────────────────────
    // LÓGICA: refrescar tabla
    // ─────────────────────────────────────────────────────────────────────
    private void refreshTable() {
        tableModel.setRowCount(0);
        if (currentServer == null) return;

        List<AdministrativeSituation> list = situationDAO.findByServer(currentServer);
        for (AdministrativeSituation a : list) {
            tableModel.addRow(new Object[]{
                    a.getId(),
                    a.getType() != null ? labelFor(a.getType()) : "(sin tipo)",
                    a.getStartDate(),    // guardamos LocalDate real para el renderer
                    a.getEndDate(),
                    a.getAdministrativeAct() != null ? a.getAdministrativeAct() : "",
                    a.getNotes() != null ? a.getNotes() : ""
            });
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // LÓGICA: mostrar situación activa de hoy en el banner
    // ─────────────────────────────────────────────────────────────────────
    private void refreshSituacionActual() {
        if (currentServer == null) return;

        LocalDate hoy = LocalDate.now();
        List<AdministrativeSituation> lista = situationDAO.findByServer(currentServer);

        AdministrativeSituation activa = lista.stream()
                .filter(s -> s.getStartDate() != null && s.getEndDate() != null
                        && !hoy.isBefore(s.getStartDate())
                        && !hoy.isAfter(s.getEndDate()))
                .findFirst()
                .orElse(null);

        if (activa == null) {
            lblSituacionActual.setText("  Situación actual: En actividad normal ✓");
            lblSituacionActual.setForeground(new Color(200, 255, 200));
        } else {
            lblSituacionActual.setText(
                    "  Situación actual: " + labelFor(activa.getType())
                            + "  |  " + activa.getStartDate() + " → " + activa.getEndDate()
                            + "  |  Acto: " + (activa.getAdministrativeAct() != null ? activa.getAdministrativeAct() : "—")
            );
            lblSituacionActual.setForeground(new Color(255, 235, 150));
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // LÓGICA: guardar — CORREGIDO: lee del ComboBox, no de un JTextField
    // ─────────────────────────────────────────────────────────────────────
    private void saveSituation() {
        // Validaciones de campos
        if (currentServer == null) {
            JOptionPane.showMessageDialog(this,
                    "Carga primero un servidor con su cédula.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (comboTipo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Selecciona el tipo de situación.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (dcStart.getDate() == null || dcEnd.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Las fechas de inicio y fin son obligatorias.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (txtAct.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El acto administrativo es obligatorio.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        AdministrativeSituation s = new AdministrativeSituation();
        s.setServer(currentServer);
        s.setType((SituationType) comboTipo.getSelectedItem());  // ← enum correcto, nunca null
        s.setStartDate(dcStart.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        s.setEndDate(dcEnd.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        s.setAdministrativeAct(txtAct.getText().trim());
        s.setNotes(txtNotes.getText().trim());

        try {
            situationDAO.saveWithValidation(s);
            JOptionPane.showMessageDialog(this, "Situación registrada correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            refreshTable();
            refreshSituacionActual();
            clearForm();

        } catch (OverlappingSituationException ex) {
            // Mensaje claro y específico para la regla crítica de RF-02
            JOptionPane.showMessageDialog(this,
                    "No se puede registrar la situación:\n\n" + ex.getMessage()
                            + "\n\nVerifica las fechas en la tabla de situaciones del servidor.",
                    "Conflicto de fechas", JOptionPane.ERROR_MESSAGE);

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Datos inválidos: " + ex.getMessage(),
                    "Error de validación", JOptionPane.ERROR_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error inesperado al guardar:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // LÓGICA: eliminar fila seleccionada
    // ─────────────────────────────────────────────────────────────────────
    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una situación de la tabla.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long id = (Long) tableModel.getValueAt(row, 0);
        String tipo = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Eliminar la situación \"" + tipo + "\" (ID " + id + ")?\n"
                        + "Esta acción no se puede deshacer.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                situationDAO.delete(id);
                refreshTable();
                refreshSituacionActual();
                clearForm();
                JOptionPane.showMessageDialog(this, "Situación eliminada.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al eliminar: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // LÓGICA: llenar formulario al hacer clic en una fila
    // ─────────────────────────────────────────────────────────────────────
    private void fillFormFromRow(int row) {
        // El tipo en la tabla es el label en español; necesitamos el enum original.
        // Lo recuperamos directamente de la BD con el ID de la fila.
        Long id = (Long) tableModel.getValueAt(row, 0);
        if (id == null) return;

        List<AdministrativeSituation> lista = situationDAO.findByServer(currentServer);
        lista.stream()
                .filter(s -> id.equals(s.getId()))
                .findFirst()
                .ifPresent(s -> {
                    if (s.getType() != null) comboTipo.setSelectedItem(s.getType());
                    if (s.getStartDate() != null)
                        dcStart.setDate(java.sql.Date.valueOf(s.getStartDate()));
                    if (s.getEndDate() != null)
                        dcEnd.setDate(java.sql.Date.valueOf(s.getEndDate()));
                    txtAct.setText(s.getAdministrativeAct() != null ? s.getAdministrativeAct() : "");
                    txtNotes.setText(s.getNotes() != null ? s.getNotes() : "");
                });
    }

    // ─────────────────────────────────────────────────────────────────────
    // LÓGICA: limpiar formulario
    // ─────────────────────────────────────────────────────────────────────
    private void clearForm() {
        comboTipo.setSelectedIndex(0);
        dcStart.setDate(null);
        dcEnd.setDate(null);
        txtAct.setText("");
        txtNotes.setText("");
        table.clearSelection();
    }
}