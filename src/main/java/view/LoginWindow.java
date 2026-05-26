package view;

import model.User;
import service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Pantalla de inicio de sesión.
 *
 * Fix centrado: los campos estaban dentro de BoxLayout Y_AXIS con LEFT_ALIGNMENT,
 * lo que los empujaba a la izquierda. Solución: panel contenedor de ancho fijo
 * centrado con GridBagLayout (que centra su contenido por defecto).
 */
public class LoginWindow extends JFrame {

    private JTextField     txtUser;
    private JPasswordField txtPass;
    private JLabel         lblError;
    private JButton        btnLogin;

    public LoginWindow() {
        setTitle("Talento Humano — Iniciar sesión");
        setSize(440, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setUndecorated(true);
        setShape(new RoundRectangle2D.Double(0, 0, 440, 520, 24, 24));
        setResizable(false);

        // ── Fondo degradado ──────────────────────────────────────────────
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, new Color(17,24,39), 0, getHeight(), new Color(30,41,59)));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
            }
        };
        root.setOpaque(false);
        setContentPane(root);

        // Arrastrar ventana
        Point[] drag = {new Point()};
        root.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { drag[0] = e.getPoint(); }
        });
        root.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point l = getLocation();
                setLocation(l.x + e.getX() - drag[0].x, l.y + e.getY() - drag[0].y);
            }
        });

        // ── Botón cerrar ─────────────────────────────────────────────────
        JButton btnClose = new JButton("✕");
        btnClose.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnClose.setForeground(new Color(148,163,184));
        btnClose.setContentAreaFilled(false); btnClose.setBorderPainted(false); btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> System.exit(0));
        btnClose.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnClose.setForeground(Color.WHITE); }
            public void mouseExited(MouseEvent e)  { btnClose.setForeground(new Color(148,163,184)); }
        });
        JPanel closeBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        closeBar.setOpaque(false);
        closeBar.add(btnClose);
        root.add(closeBar, BorderLayout.NORTH);

        // ── Contenedor central con GridBagLayout → centra el card automáticamente
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setOpaque(false);
        root.add(outer, BorderLayout.CENTER);

        // ── Card del formulario — ancho fijo 320 px ──────────────────────
        JPanel card = new JPanel();
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        // Tamaño fijo: GridBagLayout respeta el preferredSize del componente
        card.setPreferredSize(new Dimension(320, 430));
        outer.add(card);   // sin constraints → queda perfectamente centrado

        // Logo Gobernación de Boyacá
        JLabel logoLabel = new JLabel();
        try {
            java.net.URL logoUrl = getClass().getClassLoader().getResource("logo_boyaca.png");
            if (logoUrl != null) {
                ImageIcon raw = new ImageIcon(logoUrl);
                // Escalar a 260x152 manteniendo proporciones (original 1200x700)
                Image scaled = raw.getImage().getScaledInstance(260, 152, Image.SCALE_SMOOTH);
                logoLabel.setIcon(new ImageIcon(scaled));
            }
        } catch (Exception ignored) {}
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(logoLabel);
        card.add(Box.createVerticalStrut(10));

        JLabel lblSub = new JLabel("Sistema de Gestión de Personal", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(148,163,184));
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblSub);
        card.add(Box.createVerticalStrut(28));

        // ── Campos — todos con setMaximumSize(320) y CENTER_ALIGNMENT ────
        card.add(fieldLabel("Usuario"));
        card.add(Box.createVerticalStrut(4));
        txtUser = makeField(false);
        txtUser.setText("admin");
        card.add(txtUser);
        card.add(Box.createVerticalStrut(14));

        card.add(fieldLabel("Contraseña"));
        card.add(Box.createVerticalStrut(4));
        txtPass = (JPasswordField) makeField(true);
        txtPass.setText("admin123");
        card.add(txtPass);
        card.add(Box.createVerticalStrut(10));

        // Error
        lblError = new JLabel(" ", SwingConstants.CENTER);
        lblError.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblError.setForeground(new Color(252,165,165));
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblError);
        card.add(Box.createVerticalStrut(8));

        // Botón
        btnLogin = new JButton("Ingresar") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed()  ? new Color(67,56,202)
                        : getModel().isRollover() ? new Color(99,102,241)
                          : new Color(79,70,229));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setContentAreaFilled(false); btnLogin.setBorderPainted(false); btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setMaximumSize(new Dimension(320, 44));
        btnLogin.setPreferredSize(new Dimension(320, 44));
        btnLogin.addActionListener(e -> doLogin());
        card.add(btnLogin);
        card.add(Box.createVerticalStrut(16));

        // Hint
        JLabel hint = new JLabel(
                "<html><center><font color='#475569'>Por defecto: </font>"
                        + "<font color='#94a3b8'><b>admin / admin123</b></font></center></html>",
                SwingConstants.CENTER);
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(hint);

        // Enter en cualquier campo
        KeyAdapter enter = new KeyAdapter() {
            public void keyPressed(KeyEvent e) { if (e.getKeyCode() == KeyEvent.VK_ENTER) doLogin(); }
        };
        txtUser.addKeyListener(enter);
        txtPass.addKeyListener(enter);
    }

    // ─────────────────────────────────────────────────────────────────────
    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(new Color(203,213,225));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private JTextField makeField(boolean password) {
        JTextField f = password ? new JPasswordField() : new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setBackground(new Color(30,41,59));
        // Tamaño fijo = mismo ancho que el botón
        f.setMaximumSize(new Dimension(320, 44));
        f.setPreferredSize(new Dimension(320, 44));
        f.setAlignmentX(Component.CENTER_ALIGNMENT);  // ← clave para el centrado
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(71,85,105), 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(99,102,241), 2, true),
                        BorderFactory.createEmptyBorder(9,13,9,13)));
            }
            public void focusLost(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(71,85,105), 1, true),
                        BorderFactory.createEmptyBorder(10,14,10,14)));
            }
        });
        return f;
    }

    // ─────────────────────────────────────────────────────────────────────
    private void doLogin() {
        String user = txtUser.getText().trim();
        String pass = new String(txtPass.getPassword());
        if (user.isEmpty() || pass.isEmpty()) {
            lblError.setText("Usuario y contraseña son obligatorios.");
            shake(); return;
        }
        btnLogin.setEnabled(false);
        btnLogin.setText("Verificando...");
        lblError.setText(" ");

        new SwingWorker<User, Void>() {
            protected User doInBackground() { return AuthService.login(user, pass); }
            protected void done() {
                try {
                    User u = get();
                    if (u == null) {
                        lblError.setText("Credenciales incorrectas o usuario inactivo.");
                        shake();
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Ingresar");
                    } else {
                        dispose();
                        SwingUtilities.invokeLater(() -> new DashboardWindow(u).setVisible(true));
                    }
                } catch (Exception ex) {
                    lblError.setText("Error de conexión.");
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Ingresar");
                }
            }
        }.execute();
    }

    private void shake() {
        Point orig = getLocation();
        int[] offs = {-8, 8, -6, 6, -4, 4, 0};
        int[] step = {0};
        javax.swing.Timer t = new javax.swing.Timer(35, null);
        t.addActionListener(e -> {
            if (step[0] < offs.length) setLocation(orig.x + offs[step[0]++], orig.y);
            else { setLocation(orig); t.stop(); }
        });
        t.start();
    }
}