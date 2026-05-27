package view;

import com.toedter.calendar.JDateChooser;
import dao.PublicServerDAO;
import model.PublicServer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.ZoneId;
import java.util.List;

/**
 * RF-01: Planta de Personal — estandarizado con UITheme
 */
public class PublicServerWindow extends JFrame {

    private JTable            tableServers;
    private DefaultTableModel tableModel;
    private PublicServerDAO   serverDAO;

    private JTextField              txtIdNumber;
    private JTextField              txtFirstName;
    private JTextField              txtLastName;
    private JDateChooser            dateChooserBirth;
    private JComboBox<String>       comboGender;
    private JComboBox<String>       comboCivilStatus;
    private JComboBox<String>       comboBloodType;
    private JTextField              txtPhone;
    private JTextField              txtEmail;
    private JTextField              txtDependency;
    private JTextField              txtPosition;
    private JTextField              txtPositionCode;
    private JTextField              txtVinculationType;
    private JDateChooser            dateChooserAdmission;
    private JTextField              txtMonthlySalary;

    private boolean modoEdicion = false; // true cuando se edita un servidor existente

    public PublicServerWindow() {
        serverDAO = new PublicServerDAO();
        UITheme.applyGlobal();

        setTitle("RF-01: Planta de Personal");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.BG_SOFT);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        cargarServidores();
    }

    // ─────────────────────────────────────────────────────────────────────
    // HEADER
    // ─────────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = UITheme.windowHeader("RF-01 — Planta de Personal", "Gestión de servidores públicos activos");

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        JButton btnNuevo    = UITheme.secondaryButton("Nuevo");
        JButton btnGuardar  = UITheme.primaryButton("Guardar");
        JButton btnBaja     = UITheme.dangerButton("Dar de baja");
        JButton btnRefresh  = UITheme.ghostButton(" Actualizar");

        btnNuevo.setForeground(Color.BLACK);
        btnNuevo.addActionListener(e -> limpiarFormulario());
        btnGuardar.addActionListener(e -> guardarServidor());
        btnBaja.addActionListener(e -> darDeBaja());
        btnRefresh.setForeground(new Color(57, 89, 63));
        btnRefresh.addActionListener(e -> cargarServidores());

        btnPanel.add(btnRefresh);
        btnPanel.add(btnNuevo);
        btnPanel.add(btnGuardar);
        btnPanel.add(btnBaja);
        header.add(btnPanel, BorderLayout.EAST);

        return header;
    }

    // ─────────────────────────────────────────────────────────────────────
    // CONTENIDO PRINCIPAL
    // ─────────────────────────────────────────────────────────────────────
    private JSplitPane buildContent() {
        JScrollPane scrollForm = new JScrollPane(buildForm());
        scrollForm.setBorder(null);
        scrollForm.setPreferredSize(new Dimension(400, 600));
        scrollForm.getVerticalScrollBar().setUI(new UITheme.MinimalScrollBarUI());

        JPanel tablePanel = new JPanel(new BorderLayout(0, UITheme.PAD_SM));
        tablePanel.setBackground(UITheme.BG_SOFT);
        tablePanel.setBorder(new EmptyBorder(UITheme.PAD, UITheme.PAD, UITheme.PAD, UITheme.PAD));

        tableModel = new DefaultTableModel(
                new String[]{"Cédula", "Nombre", "Apellido", "Dependencia", "Cargo", "Teléfono", "Email", "Estado"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tableServers = UITheme.styledTable(tableModel);
        tableServers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableServers.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableServers.getSelectedRow() >= 0)
                cargarFormularioDesdeTabla();
        });
        tableServers.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    int row = tableServers.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        tableServers.setRowSelectionInterval(row, row);
                        String idNumber = (String) tableModel.getValueAt(row, 0);
                        model.PublicServer server = serverDAO.findByIdNumber(idNumber);
                        if (server != null) {
                            new ServerProfileWindow(server).setVisible(true);
                        } else {
                            UITheme.showError(null, "No se encontró el servidor con cédula: " + idNumber);
                        }
                    }
                }
            }
        });

        JLabel hint = UITheme.bodyLabel("  Doble clic en una fila para ver el perfil completo del servidor");
        hint.setForeground(UITheme.TEXT_SUB);
        hint.setFont(UITheme.FONT_CAPTION);

        tablePanel.add(UITheme.tableScroll(tableServers), BorderLayout.CENTER);
        tablePanel.add(hint, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollForm, tablePanel);
        split.setDividerLocation(420);
        split.setBorder(null);
        split.setBackground(UITheme.BG_SOFT);
        return split;
    }

    // ─────────────────────────────────────────────────────────────────────
    // FORMULARIO
    // ─────────────────────────────────────────────────────────────────────
    private JPanel buildForm() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(UITheme.BG_SOFT);
        outer.setBorder(new EmptyBorder(UITheme.PAD, UITheme.PAD, UITheme.PAD, UITheme.PAD_SM));

        UITheme.Card card = new UITheme.Card(UITheme.PAD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        card.add(UITheme.sectionLabel("Datos del Servidor"));
        card.add(Box.createVerticalStrut(UITheme.PAD));

        txtIdNumber        = UITheme.styledInput("");
        txtFirstName       = UITheme.styledInput("");
        txtLastName        = UITheme.styledInput("");
        txtPhone           = UITheme.styledInput("");
        txtEmail           = UITheme.styledInput("");
        txtDependency      = UITheme.styledInput("");
        txtPosition        = UITheme.styledInput("");
        txtPositionCode    = UITheme.styledInput("");
        txtVinculationType = UITheme.styledInput("");
        txtMonthlySalary   = UITheme.styledInput("");

        comboGender      = UITheme.styledCombo(new String[]{"M", "F"});
        comboCivilStatus = UITheme.styledCombo(new String[]{"Soltero", "Casado", "Divorciado", "Viudo"});
        comboBloodType   = UITheme.styledCombo(new String[]{"O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-"});

        dateChooserBirth     = new JDateChooser(); styleDate(dateChooserBirth);
        dateChooserAdmission = new JDateChooser(); styleDate(dateChooserAdmission);

        addFormField(card, "Cédula *",            txtIdNumber);
        addFormField(card, "Nombre *",            txtFirstName);
        addFormField(card, "Apellido",            txtLastName);
        addFormField(card, "Fecha de nacimiento", dateChooserBirth);
        addFormField(card, "Género",              comboGender);
        addFormField(card, "Estado civil",        comboCivilStatus);
        addFormField(card, "Tipo de sangre",      comboBloodType);
        addFormField(card, "Teléfono",            txtPhone);
        addFormField(card, "Email",               txtEmail);
        addFormField(card, "Dependencia",         txtDependency);
        addFormField(card, "Cargo",               txtPosition);
        addFormField(card, "Código cargo",        txtPositionCode);
        addFormField(card, "Tipo de vinculación", txtVinculationType);
        addFormField(card, "Fecha de ingreso",    dateChooserAdmission);
        addFormField(card, "Salario mensual",     txtMonthlySalary);

        outer.add(card, BorderLayout.NORTH);
        return outer;
    }

    private void addFormField(JPanel parent, String label, JComponent field) {
        parent.add(UITheme.formField(label, field));
        parent.add(Box.createVerticalStrut(UITheme.PAD_SM));
    }

    private void styleDate(JDateChooser dc) {
        dc.setFont(UITheme.FONT_BODY);
        dc.setBackground(UITheme.BG);
        dc.setPreferredSize(new Dimension(dc.getPreferredSize().width, UITheme.INPUT_H));
        dc.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));
    }

    // ─────────────────────────────────────────────────────────────────────
    // FOOTER
    // ─────────────────────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, UITheme.PAD, 6));
        p.setBackground(UITheme.BG);
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));
        p.add(UITheme.badge("Activo", UITheme.PRIMARY_LIGHT, UITheme.PRIMARY_DARK));
        p.add(UITheme.bodyLabel("  Total de servidores activos se actualiza al cargar la lista"));
        return p;
    }

    // ─────────────────────────────────────────────────────────────────────
    // LÓGICA
    // ─────────────────────────────────────────────────────────────────────
    private void cargarServidores() {
        tableModel.setRowCount(0);
        try {
            List<PublicServer> servidores = serverDAO.findAllActive();
            for (PublicServer srv : servidores) {
                String dep = srv.getDependency() != null ? srv.getDependency().getName() : "—";
                String pos = srv.getPosition()   != null ? srv.getPosition().getName()   : "—";
                tableModel.addRow(new Object[]{
                        srv.getIdNumber(), srv.getFirstName(), srv.getLastName(),
                        dep, pos, srv.getPhone(), srv.getEmail(),
                        Boolean.TRUE.equals(srv.getActive()) ? "Activo" : "Inactivo"
                });
            }
        } catch (Exception e) {
            UITheme.showError(this, "Error al cargar servidores: " + e.getMessage());
        }
    }

    private void cargarFormularioDesdeTabla() {
        int row = tableServers.getSelectedRow();
        if (row < 0) return;
        String id = (String) tableModel.getValueAt(row, 0);
        try {
            PublicServer srv = serverDAO.findByIdNumber(id);
            if (srv != null) fillForm(srv);
        } catch (Exception e) {
            UITheme.showError(this, "Error al cargar servidor: " + e.getMessage());
        }
    }

    public void loadServer(String idNumber) {
        if (idNumber == null || idNumber.trim().isEmpty()) return;
        try {
            PublicServer srv = serverDAO.findByIdNumber(idNumber);
            if (srv != null) fillForm(srv);
            else showDialog(this, "Información", "Servidor no encontrado: " + idNumber);
        } catch (Exception e) {
            UITheme.showError(this, "Error al cargar servidor: " + e.getMessage());
        }
    }

    private void fillForm(PublicServer srv) {
        modoEdicion = true;
        txtIdNumber.setText(srv.getIdNumber());
        txtIdNumber.setEditable(false);
        txtFirstName.setText(nvl(srv.getFirstName()));
        txtLastName.setText(nvl(srv.getLastName()));
        if (srv.getBirthDate()    != null) dateChooserBirth.setDate(java.sql.Date.valueOf(srv.getBirthDate()));
        if (srv.getGender()       != null) comboGender.setSelectedItem(srv.getGender());
        if (srv.getCivilStatus()  != null) comboCivilStatus.setSelectedItem(srv.getCivilStatus());
        if (srv.getBloodType()    != null) comboBloodType.setSelectedItem(srv.getBloodType());
        txtPhone.setText(nvl(srv.getPhone()));
        txtEmail.setText(nvl(srv.getEmail()));
        txtDependency.setText(srv.getDependency() != null ? srv.getDependency().getName() : "");
        txtPosition.setText(srv.getPosition()     != null ? srv.getPosition().getName()   : "");
        txtPositionCode.setText(nvl(srv.getPositionCode()));
        txtVinculationType.setText(nvl(srv.getVinculationType()));
        if (srv.getAdmissionDate() != null) dateChooserAdmission.setDate(java.sql.Date.valueOf(srv.getAdmissionDate()));
        txtMonthlySalary.setText(srv.getMonthlySalary() != null ? srv.getMonthlySalary().toString() : "");
    }

    private void limpiarFormulario() {
        modoEdicion = false;
        txtIdNumber.setText(""); txtIdNumber.setEditable(true);
        txtFirstName.setText(""); txtLastName.setText("");
        dateChooserBirth.setDate(null);
        comboGender.setSelectedIndex(0); comboCivilStatus.setSelectedIndex(0); comboBloodType.setSelectedIndex(0);
        txtPhone.setText(""); txtEmail.setText("");
        txtDependency.setText(""); txtPosition.setText("");
        txtPositionCode.setText(""); txtVinculationType.setText("");
        dateChooserAdmission.setDate(null); txtMonthlySalary.setText("");
        tableServers.clearSelection();
    }

    private void guardarServidor() {
        try {
            if (txtIdNumber.getText().trim().isEmpty()) {
                UITheme.showWarning(this, "La cédula es obligatoria."); return;
            }
            if (txtFirstName.getText().trim().isEmpty()) {
                UITheme.showWarning(this, "El nombre es obligatorio."); return;
            }

            if (modoEdicion) {
                // ── MODO EDICIÓN: cargar entidad existente y actualizarla ──
                PublicServer srv = serverDAO.findByIdNumber(txtIdNumber.getText().trim());
                if (srv == null) {
                    UITheme.showError(this, "No se encontró el servidor para actualizar."); return;
                }
                srv.setFirstName(txtFirstName.getText().trim());
                srv.setLastName(txtLastName.getText().trim());
                srv.setGender((String) comboGender.getSelectedItem());
                srv.setCivilStatus((String) comboCivilStatus.getSelectedItem());
                srv.setBloodType((String) comboBloodType.getSelectedItem());
                srv.setPhone(txtPhone.getText().trim());
                srv.setEmail(txtEmail.getText().trim());
                srv.setPositionCode(txtPositionCode.getText().trim());
                srv.setVinculationType(txtVinculationType.getText().trim());
                if (dateChooserBirth.getDate() != null)
                    srv.setBirthDate(dateChooserBirth.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                if (dateChooserAdmission.getDate() != null)
                    srv.setAdmissionDate(dateChooserAdmission.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                if (!txtMonthlySalary.getText().isEmpty()) {
                    try { srv.setMonthlySalary(Double.parseDouble(txtMonthlySalary.getText().trim())); }
                    catch (NumberFormatException ex) { UITheme.showWarning(this, "El salario debe ser un número válido."); return; }
                }
                serverDAO.update(srv);
                UITheme.showSuccess(this, "Servidor actualizado correctamente.");

            } else {
                // ── MODO NUEVO: verificar que no exista y persistir ──
                if (serverDAO.findByIdNumber(txtIdNumber.getText().trim()) != null) {
                    UITheme.showWarning(this, "Ya existe un servidor con esa cédula. Selecciónalo de la tabla para editarlo."); return;
                }
                PublicServer srv = new PublicServer();
                srv.setIdNumber(txtIdNumber.getText().trim());
                srv.setFirstName(txtFirstName.getText().trim());
                srv.setLastName(txtLastName.getText().trim());
                srv.setGender((String) comboGender.getSelectedItem());
                srv.setCivilStatus((String) comboCivilStatus.getSelectedItem());
                srv.setBloodType((String) comboBloodType.getSelectedItem());
                srv.setPhone(txtPhone.getText().trim());
                srv.setEmail(txtEmail.getText().trim());
                srv.setPositionCode(txtPositionCode.getText().trim());
                srv.setVinculationType(txtVinculationType.getText().trim());
                srv.setActive(true);
                if (dateChooserBirth.getDate() != null)
                    srv.setBirthDate(dateChooserBirth.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                if (dateChooserAdmission.getDate() != null)
                    srv.setAdmissionDate(dateChooserAdmission.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                if (!txtMonthlySalary.getText().isEmpty()) {
                    try { srv.setMonthlySalary(Double.parseDouble(txtMonthlySalary.getText().trim())); }
                    catch (NumberFormatException ex) { UITheme.showWarning(this, "El salario debe ser un número válido."); return; }
                }
                serverDAO.save(srv);
                UITheme.showSuccess(this, "Servidor guardado correctamente.");
            }

            limpiarFormulario();
            cargarServidores();

        } catch (Exception e) {
            UITheme.showError(this, "Error al guardar: " + e.getMessage());
        }
    }

    private void darDeBaja() {
        int row = tableServers.getSelectedRow();
        if (row < 0) { UITheme.showWarning(this, "Selecciona un servidor de la tabla."); return; }
        String id = (String) tableModel.getValueAt(row, 0);
        if (UITheme.showConfirm(this, "¿Dar de baja al servidor con cédula " + id + "?")) {
            try {
                serverDAO.deactivate(id);
                UITheme.showSuccess(this, "Servidor dado de baja correctamente.");
                limpiarFormulario();
                cargarServidores();
            } catch (Exception e) {
                UITheme.showError(this, "Error al dar de baja: " + e.getMessage());
            }
        }
    }

    private void showDialog(Component parent, String title, String msg) {
        JOptionPane.showMessageDialog(parent, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private String nvl(String s) { return s != null ? s : ""; }
}