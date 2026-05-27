package view;

import model.User;
import service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * LoginWindow — estandarizado con UITheme (paleta verde #318c45)
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
        setShape(new RoundRectangle2D.Double(0, 0, 440, 520, 20, 20));
        setResizable(false);

        // ── Fondo con paleta verde institucional ─────────────────────────
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, UITheme.SIDEBAR_BG, 0, getHeight(),
                        new Color(0x1e, 0x35, 0x22)));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
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
        btnClose.setForeground(new Color(0xbb, 0xe5, 0xc4));
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> System.exit(0));
        btnClose.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnClose.setForeground(Color.WHITE); }
            public void mouseExited(MouseEvent e)  { btnClose.setForeground(new Color(0xbb, 0xe5, 0xc4)); }
        });
        JPanel closeBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        closeBar.setOpaque(false);
        closeBar.add(btnClose);
        root.add(closeBar, BorderLayout.NORTH);

        // ── Contenedor central centrado ──────────────────────────────────
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setOpaque(false);
        root.add(outer, BorderLayout.CENTER);

        JPanel card = new JPanel();
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(320, 430));
        outer.add(card);

        // ── Logo ─────────────────────────────────────────────────────────
        JLabel logoLabel = new JLabel();
        try {
            java.net.URL logoUrl = getClass().getClassLoader().getResource("logo_boyaca.png");
            if (logoUrl != null) {
                ImageIcon raw = new ImageIcon(logoUrl);
                Image scaled = raw.getImage().getScaledInstance(260, 152, Image.SCALE_SMOOTH);
                logoLabel.setIcon(new ImageIcon(scaled));
            }
        } catch (Exception ignored) {}
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(logoLabel);
        card.add(Box.createVerticalStrut(10));

        JLabel lblSub = new JLabel("Sistema de Gestión de Personal", SwingConstants.CENTER);
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(new Color(0xbb, 0xe5, 0xc4));
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblSub);
        card.add(Box.createVerticalStrut(28));

        // ── Campos ───────────────────────────────────────────────────────
        card.add(loginFieldLabel("Usuario"));
        card.add(Box.createVerticalStrut(4));
        txtUser = makeLoginField(false);
        txtUser.setText("admin");
        card.add(txtUser);
        card.add(Box.createVerticalStrut(14));

        card.add(loginFieldLabel("Contraseña"));
        card.add(Box.createVerticalStrut(4));
        txtPass = (JPasswordField) makeLoginField(true);
        txtPass.setText("admin123");
        card.add(txtPass);
        card.add(Box.createVerticalStrut(10));

        // Error
        lblError = new JLabel(" ", SwingConstants.CENTER);
        lblError.setFont(UITheme.FONT_CAPTION);
        lblError.setForeground(new Color(0xfc, 0xa5, 0xa5));
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblError);
        card.add(Box.createVerticalStrut(8));

        // ── Botón Ingresar (verde primario) ──────────────────────────────
        btnLogin = new JButton("Ingresar") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed()  ? UITheme.PRIMARY_DARK.darker()
                        : getModel().isRollover() ? UITheme.PRIMARY_DARK
                          : UITheme.PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), UITheme.RADIUS_SM, UITheme.RADIUS_SM);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnLogin.setFont(UITheme.FONT_BTN);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setContentAreaFilled(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setMaximumSize(new Dimension(320, UITheme.BTN_H + 6));
        btnLogin.setPreferredSize(new Dimension(320, UITheme.BTN_H + 6));
        btnLogin.addActionListener(e -> doLogin());
        card.add(btnLogin);
        card.add(Box.createVerticalStrut(16));

        JLabel hint = new JLabel(
                "<html><center><font color='#6b8c70'>Por defecto: </font>"
                        + "<font color='#bbe5c4'><b>admin / admin123</b></font></center></html>",
                SwingConstants.CENTER);
        hint.setFont(UITheme.FONT_SMALL);
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
    // Helpers de estilo para el login (fondo oscuro → colores invertidos)
    // ─────────────────────────────────────────────────────────────────────
    private JLabel loginFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(new Color(0xbb, 0xe5, 0xc4));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private JTextField makeLoginField(boolean password) {
        JTextField f = password ? new JPasswordField() : new JTextField();
        f.setFont(UITheme.FONT_BODY);
        f.setForeground(Color.WHITE);
        f.setCaretColor(UITheme.PRIMARY_LIGHT);
        f.setBackground(new Color(0x24, 0x38, 0x28));
        f.setMaximumSize(new Dimension(320, UITheme.INPUT_H + 6));
        f.setPreferredSize(new Dimension(320, UITheme.INPUT_H + 6));
        f.setAlignmentX(Component.CENTER_ALIGNMENT);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x3d, 0x5c, 0x43), 1, true),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UITheme.PRIMARY, 2, true),
                        BorderFactory.createEmptyBorder(7, 13, 7, 13)));
            }
            public void focusLost(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0x3d, 0x5c, 0x43), 1, true),
                        BorderFactory.createEmptyBorder(8, 14, 8, 14)));
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