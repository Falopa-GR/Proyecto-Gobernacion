package view;

import javax.swing.*;
import java.awt.*;


public class MainWindow extends JFrame {

    public MainWindow() {
        setTitle("Sistema de Gestión - Talento Humano");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(true);

        // Panel superior con gradiente/diseño atractivo
        JPanel panelTop = crearPanelEncabezado();
        add(panelTop, BorderLayout.NORTH);

        // Panel central con módulos en grid moderno
        JPanel panelCenter = crearPanelModulos();
        add(panelCenter, BorderLayout.CENTER);

        // Panel inferior
        JPanel panelBottom = crearPanelPie();
        add(panelBottom, BorderLayout.SOUTH);
    }

    private JPanel crearPanelEncabezado() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradiente azul
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(41, 128, 185),
                        getWidth(), getHeight(), new Color(52, 152, 219)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        panel.setPreferredSize(new Dimension(1000, 100));
        panel.setLayout(new BorderLayout());
        panel.setOpaque(false);

        // Logo/Título principal
        JLabel labelTitle = new JLabel("🏢 SISTEMA DE GESTIÓN DE TALENTO HUMANO");
        labelTitle.setFont(new Font("Arial", Font.BOLD, 24));
        labelTitle.setForeground(Color.WHITE);
        labelTitle.setHorizontalAlignment(SwingConstants.CENTER);
        labelTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panel.add(labelTitle, BorderLayout.CENTER);

        // Subtítulo
        JLabel labelSubtitle = new JLabel("Gestión Integral de Recursos Humanos");
        labelSubtitle.setFont(new Font("Arial", Font.ITALIC, 12));
        labelSubtitle.setForeground(new Color(236, 240, 241));
        labelSubtitle.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel panelSubtitle = new JPanel();
        panelSubtitle.setOpaque(false);
        panelSubtitle.add(labelSubtitle);
        panel.add(panelSubtitle, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearPanelModulos() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(236, 240, 241)); // Gris claro
        panel.setLayout(new GridLayout(2, 4, 15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Array con datos de módulos (icono emoji, título, descripción)
        String[][] modulos = {
                {"👥", "Planta de Personal", "Registro y consulta de servidores"},
                {"📋", "Situaciones Admin.", "Vacaciones, permisos y licencias"},
                {"🏖️", "Control Vacaciones", "Cálculo y alertas de períodos"},
                {"🎁", "Plan Bienestar", "Beneficios e incentivos"},
                {"⚕️", "Seguridad y Salud", "Evaluaciones médicas y accidentes"},
                {"⭐", "Evaluación Desempeño", "Historial y reconocimientos"},
                {"📊", "Reportes y Dashboard", "Estadísticas en tiempo real"},
                {"👤", "Perfil Integrado", "Tarjeta de servicios del servidor"}
        };

        JButton[] botones = new JButton[8];

        for (int i = 0; i < modulos.length; i++) {
            botones[i] = crearBotonModulo(
                    modulos[i][0] + " " + modulos[i][1],
                    modulos[i][2],
                    i
            );
            panel.add(botones[i]);
        }

        // Acción del botón RF-01 (abre ventana de Planta de Personal)
        botones[0].addActionListener(e -> new PublicServerWindow().setVisible(true));

        // Acciones de los demás botones (mostrar mensaje "En desarrollo")
        for (int i = 1; i < botones.length; i++) {
            final int index = i;
            botones[i].addActionListener(e ->
                    JOptionPane.showMessageDialog(
                            this,
                            "RF-0" + (index + 1) + " - Módulo en desarrollo\n\n" + modulos[index][2],
                            "En Desarrollo",
                            JOptionPane.INFORMATION_MESSAGE
                    )
            );
        }

        return panel;
    }

    private JButton crearBotonModulo(String titulo, String descripcion, int indice) {
        JButton boton = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2d.setColor(new Color(44, 62, 80)); // Gris oscuro (al presionar)
                } else if (getModel().isArmed() || getModel().isSelected()) {
                    g2d.setColor(new Color(52, 73, 94)); // Gris medio (al pasar mouse)
                } else {
                    g2d.setColor(new Color(52, 152, 219)); // Azul (normal)
                }

                // Esquinas redondeadas
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Borde
                g2d.setColor(new Color(41, 128, 185));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

                super.paintComponent(g);
            }
        };

        boton.setLayout(new BoxLayout(boton, BoxLayout.Y_AXIS));
        boton.setFocusPainted(false);
        boton.setContentAreaFilled(false);
        boton.setBorderPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setPreferredSize(new Dimension(200, 120));

        // Título del módulo
        JLabel labelTitulo = new JLabel(titulo);
        labelTitulo.setFont(new Font("Arial", Font.BOLD, 14));
        labelTitulo.setForeground(Color.WHITE);
        labelTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Descripción
        JLabel labelDesc = new JLabel("<html><center>" + descripcion + "</center></html>");
        labelDesc.setFont(new Font("Arial", Font.PLAIN, 11));
        labelDesc.setForeground(new Color(236, 240, 241));
        labelDesc.setAlignmentX(Component.CENTER_ALIGNMENT);

        boton.add(Box.createVerticalStrut(10));
        boton.add(labelTitulo);
        boton.add(Box.createVerticalStrut(5));
        boton.add(labelDesc);
        boton.add(Box.createVerticalStrut(10));

        return boton;
    }

    private JPanel crearPanelPie() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(44, 62, 80)); // Gris oscuro
        panel.setPreferredSize(new Dimension(1000, 50));
        panel.setLayout(new BorderLayout());

        // Información izquierda
        JLabel labelInfo = new JLabel("© 2026 - Sistema de Gestión de Talento Humano");
        labelInfo.setForeground(Color.WHITE);
        labelInfo.setFont(new Font("Arial", Font.PLAIN, 11));
        labelInfo.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        panel.add(labelInfo, BorderLayout.WEST);

        // Información derecha
        JLabel labelVersion = new JLabel("v1.0 - PostgreSQL");
        labelVersion.setForeground(new Color(189, 195, 199));
        labelVersion.setFont(new Font("Arial", Font.PLAIN, 10));
        labelVersion.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
        panel.add(labelVersion, BorderLayout.EAST);

        return panel;
    }
}