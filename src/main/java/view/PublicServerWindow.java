package view;

import com.toedter.calendar.JDateChooser;
import dao.PublicServerDAO;
import model.PublicServer;
import model.Position;
import model.Dependency;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public class PublicServerWindow extends JFrame {

    private JTable tableServers;
    private DefaultTableModel tableModel;
    private PublicServerDAO serverDAO;

    // Componentes del formulario
    private JTextField txtIdNumber;
    private JTextField txtFirstName;
    private JTextField txtLastName;
    private JDateChooser dateChooserBirth;
    private JComboBox<String> comboGender;
    private JComboBox<String> comboCivilStatus;
    private JComboBox<String> comboBloodType;
    private JTextField txtPhone;
    private JTextField txtEmail;
    private JTextField txtDependency;
    private JTextField txtPosition;
    private JTextField txtPositionCode;
    private JTextField txtVinculationType;
    private JDateChooser dateChooserAdmission;
    private JTextField txtMonthlySalary;

    public PublicServerWindow() {
        serverDAO = new PublicServerDAO();

        setTitle("RF-01: Planta de Personal");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel superior con botones de acción
        JPanel panelTop = new JPanel();
        panelTop.setBackground(new Color(52, 152, 219));
        JButton btnRefresh = new JButton("Refrescar");
        btnRefresh.addActionListener(e -> cargarServidores());
        JButton btnNuevo = new JButton("Nuevo Servidor");
        btnNuevo.addActionListener(e -> limpiarFormulario());
        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.addActionListener(e -> guardarServidor());
        JButton btnEliminar = new JButton("Dar de Baja");
        btnEliminar.addActionListener(e -> darDeBaja());

        panelTop.add(btnRefresh);
        panelTop.add(btnNuevo);
        panelTop.add(btnGuardar);
        panelTop.add(btnEliminar);
        add(panelTop, BorderLayout.NORTH);

        // Panel central: tabla de servidores
        JPanel panelCenter = new JPanel(new BorderLayout());

        // Tabla
        tableModel = new DefaultTableModel(
                new String[]{"Cédula", "Nombre", "Apellido", "Dependencia", "Cargo", "Teléfono", "Email", "Estado"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabla de solo lectura
            }
        };

        tableServers = new JTable(tableModel);
        tableServers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableServers.setRowHeight(25);
        tableServers.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableServers.getSelectedRow() >= 0) {
                cargarFormularioDesdeTabla();
            }
        });
        tableServers.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && javax.swing.SwingUtilities.isLeftMouseButton(e)) {
                    int row = tableServers.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        tableServers.setRowSelectionInterval(row, row);
                        String idNumber = (String) tableModel.getValueAt(row, 0);
                        model.PublicServer server = serverDAO.findByIdNumber(idNumber);
                        if (server != null) {
                            new ServerProfileWindow(server).setVisible(true);
                        } else {
                            JOptionPane.showMessageDialog(null,
                                    "No se encontro el servidor con cedula: " + idNumber,
                                    "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tableServers);
        panelCenter.add(scrollPane, BorderLayout.CENTER);

        // Formulario (lado izquierdo del panel central en un JSplitPane)
        JPanel panelFormBase = crearPanelFormulario();
        JScrollPane scrollForm = new JScrollPane(panelFormBase);
        scrollForm.setPreferredSize(new Dimension(400, 600));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollForm, panelCenter);
        splitPane.setDividerLocation(400);
        add(splitPane, BorderLayout.CENTER);

        // Panel inferior con información
        JPanel panelBottom = new JPanel();
        panelBottom.setBackground(Color.LIGHT_GRAY);
        JLabel labelInfo = new JLabel("Total de servidores activos: 0");
        panelBottom.add(labelInfo);
        add(panelBottom, BorderLayout.SOUTH);

        // Cargar datos inicialmente
        cargarServidores();
    }

    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Datos del Servidor"));

        // Fila 1: Cédula
        panel.add(crearFilaFormulario("Cédula:", txtIdNumber = new JTextField(15)));

        // Fila 2: Nombre
        panel.add(crearFilaFormulario("Nombre:", txtFirstName = new JTextField(15)));

        // Fila 3: Apellido
        panel.add(crearFilaFormulario("Apellido:", txtLastName = new JTextField(15)));

        // Fila 4: Fecha de Nacimiento
        dateChooserBirth = new JDateChooser();
        panel.add(crearFilaFormularioConLabel("Fecha Nacimiento:", dateChooserBirth));

        // Fila 5: Género
        comboGender = new JComboBox<>(new String[]{"M", "F"});
        panel.add(crearFilaFormulario("Género:", comboGender));

        // Fila 6: Estado Civil
        comboCivilStatus = new JComboBox<>(new String[]{"Soltero", "Casado", "Divorciado", "Viudo"});
        panel.add(crearFilaFormulario("Estado Civil:", comboCivilStatus));

        // Fila 7: Tipo de Sangre
        comboBloodType = new JComboBox<>(new String[]{"O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-"});
        panel.add(crearFilaFormulario("Tipo de Sangre:", comboBloodType));

        // Fila 8: Teléfono
        panel.add(crearFilaFormulario("Teléfono:", txtPhone = new JTextField(15)));

        // Fila 9: Email
        panel.add(crearFilaFormulario("Email:", txtEmail = new JTextField(15)));

        // Fila 10: Dependencia
        panel.add(crearFilaFormulario("Dependencia:", txtDependency = new JTextField(15)));

        // Fila 11: Cargo
        panel.add(crearFilaFormulario("Cargo:", txtPosition = new JTextField(15)));

        // Fila 12: Código Cargo
        panel.add(crearFilaFormulario("Código Cargo:", txtPositionCode = new JTextField(15)));

        // Fila 13: Tipo de Vinculación
        panel.add(crearFilaFormulario("Tipo Vinculación:", txtVinculationType = new JTextField(15)));

        // Fila 14: Fecha de Ingreso
        dateChooserAdmission = new JDateChooser();
        panel.add(crearFilaFormularioConLabel("Fecha Ingreso:", dateChooserAdmission));

        // Fila 15: Salario Mensual
        panel.add(crearFilaFormulario("Salario Mensual:", txtMonthlySalary = new JTextField(15)));

        // Retornar el panel envuelto en JScrollPane (crearemos el scroll en el llamante)
        return panel;
    }

    private JPanel crearFilaFormulario(String label, JComponent campo) {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fila.add(new JLabel(label));
        fila.add(campo);
        return fila;
    }

    private JPanel crearFilaFormularioConLabel(String label, JComponent campo) {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fila.add(new JLabel(label));
        fila.add(campo);
        return fila;
    }

    private void cargarServidores() {
        tableModel.setRowCount(0); // Limpiar tabla

        try {
            List<PublicServer> servidores = serverDAO.findAllActive();
            for (PublicServer servidor : servidores) {
                String dependencyName = servidor.getDependency() != null ? servidor.getDependency().getName() : "-";
                String positionName = servidor.getPosition() != null ? servidor.getPosition().getName() : "-";

                tableModel.addRow(new Object[]{
                        servidor.getIdNumber(),
                        servidor.getFirstName(),
                        servidor.getLastName(),
                        dependencyName,
                        positionName,
                        servidor.getPhone(),
                        servidor.getEmail(),
                        servidor.getActive() ? "Activo" : "Inactivo"
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar servidores: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void cargarFormularioDesdeTabla() {
        int row = tableServers.getSelectedRow();
        if (row >= 0) {
            String idNumber = (String) tableModel.getValueAt(row, 0);
            try {
                PublicServer servidor = serverDAO.findByIdNumber(idNumber);
                if (servidor != null) {
                    txtIdNumber.setText(servidor.getIdNumber());
                    txtFirstName.setText(servidor.getFirstName());
                    txtLastName.setText(servidor.getLastName());
                    if (servidor.getBirthDate() != null) {
                        dateChooserBirth.setDate(java.sql.Date.valueOf(servidor.getBirthDate()));
                    }
                    if (servidor.getGender() != null) comboGender.setSelectedItem(servidor.getGender());
                    if (servidor.getCivilStatus() != null) comboCivilStatus.setSelectedItem(servidor.getCivilStatus());
                    if (servidor.getBloodType() != null) comboBloodType.setSelectedItem(servidor.getBloodType());
                    txtPhone.setText(servidor.getPhone() != null ? servidor.getPhone() : "");
                    txtEmail.setText(servidor.getEmail() != null ? servidor.getEmail() : "");
                    txtDependency.setText(servidor.getDependency() != null ? servidor.getDependency().getName() : "");
                    txtPosition.setText(servidor.getPosition() != null ? servidor.getPosition().getName() : "");
                    txtPositionCode.setText(servidor.getPositionCode() != null ? servidor.getPositionCode() : "");
                    txtVinculationType.setText(servidor.getVinculationType() != null ? servidor.getVinculationType() : "");
                    if (servidor.getAdmissionDate() != null) {
                        dateChooserAdmission.setDate(java.sql.Date.valueOf(servidor.getAdmissionDate()));
                    }
                    txtMonthlySalary.setText(servidor.getMonthlySalary() != null ? servidor.getMonthlySalary().toString() : "");
                    txtIdNumber.setEditable(false); // No permitir editar cédula
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al cargar servidor: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Carga un servidor en el formulario por su número de cédula.
     * Útil para abrir la ventana desde la búsqueda.
     */
    public void loadServer(String idNumber) {
        if (idNumber == null || idNumber.trim().isEmpty()) return;
        try {
            PublicServer servidor = serverDAO.findByIdNumber(idNumber);
            if (servidor != null) {
                txtIdNumber.setText(servidor.getIdNumber());
                txtIdNumber.setEditable(false);
                txtFirstName.setText(servidor.getFirstName() != null ? servidor.getFirstName() : "");
                txtLastName.setText(servidor.getLastName() != null ? servidor.getLastName() : "");
                if (servidor.getBirthDate() != null) dateChooserBirth.setDate(java.sql.Date.valueOf(servidor.getBirthDate()));
                if (servidor.getGender() != null) comboGender.setSelectedItem(servidor.getGender());
                if (servidor.getCivilStatus() != null) comboCivilStatus.setSelectedItem(servidor.getCivilStatus());
                if (servidor.getBloodType() != null) comboBloodType.setSelectedItem(servidor.getBloodType());
                txtPhone.setText(servidor.getPhone() != null ? servidor.getPhone() : "");
                txtEmail.setText(servidor.getEmail() != null ? servidor.getEmail() : "");
                txtDependency.setText(servidor.getDependency() != null ? servidor.getDependency().getName() : "");
                txtPosition.setText(servidor.getPosition() != null ? servidor.getPosition().getName() : "");
                txtPositionCode.setText(servidor.getPositionCode() != null ? servidor.getPositionCode() : "");
                txtVinculationType.setText(servidor.getVinculationType() != null ? servidor.getVinculationType() : "");
                if (servidor.getAdmissionDate() != null) dateChooserAdmission.setDate(java.sql.Date.valueOf(servidor.getAdmissionDate()));
                txtMonthlySalary.setText(servidor.getMonthlySalary() != null ? servidor.getMonthlySalary().toString() : "");
            } else {
                JOptionPane.showMessageDialog(this, "Servidor no encontrado: " + idNumber, "Información", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar servidor: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void limpiarFormulario() {
        txtIdNumber.setText("");
        txtIdNumber.setEditable(true);
        txtFirstName.setText("");
        txtLastName.setText("");
        dateChooserBirth.setDate(null);
        comboGender.setSelectedIndex(0);
        comboCivilStatus.setSelectedIndex(0);
        comboBloodType.setSelectedIndex(0);
        txtPhone.setText("");
        txtEmail.setText("");
        txtDependency.setText("");
        txtPosition.setText("");
        txtPositionCode.setText("");
        txtVinculationType.setText("");
        dateChooserAdmission.setDate(null);
        txtMonthlySalary.setText("");
        tableServers.clearSelection();
    }

    private void guardarServidor() {
        try {
            // Validaciones básicas
            if (txtIdNumber.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "La cédula es obligatoria", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (txtFirstName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre es obligatorio", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            PublicServer servidor = new PublicServer();
            servidor.setIdNumber(txtIdNumber.getText().trim());
            servidor.setFirstName(txtFirstName.getText().trim());
            servidor.setLastName(txtLastName.getText().trim());
            servidor.setGender((String) comboGender.getSelectedItem());
            servidor.setCivilStatus((String) comboCivilStatus.getSelectedItem());
            servidor.setBloodType((String) comboBloodType.getSelectedItem());
            servidor.setPhone(txtPhone.getText().trim());
            servidor.setEmail(txtEmail.getText().trim());
            servidor.setPositionCode(txtPositionCode.getText().trim());
            servidor.setVinculationType(txtVinculationType.getText().trim());
            servidor.setActive(true);

            if (dateChooserBirth.getDate() != null) {
                servidor.setBirthDate(dateChooserBirth.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            }
            if (dateChooserAdmission.getDate() != null) {
                servidor.setAdmissionDate(dateChooserAdmission.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            }
            if (!txtMonthlySalary.getText().isEmpty()) {
                try {
                    servidor.setMonthlySalary(Double.parseDouble(txtMonthlySalary.getText().trim()));
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Salario debe ser un número válido", "Validación", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            serverDAO.save(servidor);
            JOptionPane.showMessageDialog(this, "Servidor guardado correctamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            limpiarFormulario();
            cargarServidores();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void darDeBaja() {
        int row = tableServers.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un servidor", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String idNumber = (String) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Dar de baja al servidor con cédula " + idNumber + "?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                serverDAO.deactivate(idNumber);
                JOptionPane.showMessageDialog(this, "Servidor dado de baja", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarFormulario();
                cargarServidores();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al dar de baja: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}