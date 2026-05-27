package view;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Sistema de diseño unificado — Gobernación de Boyacá
 *
 * Paleta:
 *   PRIMARY       #318c45  verde institucional
 *   PRIMARY_DARK  #236332  hover / activo
 *   PRIMARY_LIGHT #e8f5ec  fondos suaves
 *   ACCENT        #2a7a3b  variante media
 *   BG            #ffffff  fondo principal
 *   BG_SOFT       #f5f7f5  fondo secundario
 *   SURFACE       #ffffff  tarjetas / paneles
 *   BORDER        #d1e8d5  bordes sutiles
 *   TEXT_MAIN     #1a2e1d  texto principal
 *   TEXT_SUB      #6b7c6e  texto secundario
 *   ERROR         #dc2626  errores
 *   WARNING       #d97706  advertencias
 *   SUCCESS       #318c45  éxito (same as primary)
 *   SIDEBAR_BG    #1a2e1d  sidebar oscuro
 *   SIDEBAR_HOVER #243828  hover sidebar
 */
public class UITheme {

    // ─── Paleta ───────────────────────────────────────────────────────────
    public static final Color PRIMARY        = new Color(0x31, 0x8c, 0x45);
    public static final Color PRIMARY_DARK   = new Color(0x23, 0x63, 0x32);
    public static final Color PRIMARY_LIGHT  = new Color(0xe8, 0xf5, 0xec);
    public static final Color ACCENT         = new Color(0x2a, 0x7a, 0x3b);
    public static final Color BG             = new Color(0xff, 0xff, 0xff);
    public static final Color BG_SOFT        = new Color(0xf5, 0xf7, 0xf5);
    public static final Color SURFACE        = new Color(0xff, 0xff, 0xff);
    public static final Color BORDER         = new Color(0xd1, 0xe8, 0xd5);
    public static final Color TEXT_MAIN      = new Color(0x1a, 0x2e, 0x1d);
    public static final Color TEXT_SUB       = new Color(0x6b, 0x7c, 0x6e);
    public static final Color TEXT_HINT      = new Color(0xa0, 0xb0, 0xa3);
    public static final Color ERROR          = new Color(0xdc, 0x26, 0x26);
    public static final Color ERROR_LIGHT    = new Color(0xfe, 0xf2, 0xf2);
    public static final Color WARNING        = new Color(0xd9, 0x77, 0x06);
    public static final Color WARNING_LIGHT  = new Color(0xff, 0xf7, 0xed);
    public static final Color SUCCESS_LIGHT  = new Color(0xec, 0xfd, 0xf1);
    public static final Color ROW_ALT        = new Color(0xf9, 0xfc, 0xf9);
    public static final Color SIDEBAR_BG     = new Color(0x1a, 0x2e, 0x1d);
    public static final Color SIDEBAR_HOVER  = new Color(0x24, 0x38, 0x28);
    public static final Color SIDEBAR_ACTIVE = new Color(0x31, 0x8c, 0x45);

    // ─── Tipografía ───────────────────────────────────────────────────────
    public static final Font FONT_H1      = new Font("Segoe UI", Font.BOLD,   22);
    public static final Font FONT_H2      = new Font("Segoe UI", Font.BOLD,   16);
    public static final Font FONT_H3      = new Font("Segoe UI", Font.BOLD,   13);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN,  13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN,  11);
    public static final Font FONT_CAPTION = new Font("Segoe UI", Font.ITALIC, 11);
    public static final Font FONT_MONO    = new Font("Consolas",  Font.PLAIN,  12);
    public static final Font FONT_TABLE_H = new Font("Segoe UI", Font.BOLD,   12);
    public static final Font FONT_BADGE   = new Font("Segoe UI", Font.BOLD,   10);
    public static final Font FONT_BTN     = new Font("Segoe UI", Font.BOLD,   13);

    // ─── Dimensiones ─────────────────────────────────────────────────────
    public static final int  RADIUS       = 10;
    public static final int  RADIUS_SM    = 6;
    public static final int  INPUT_H      = 38;
    public static final int  BTN_H        = 38;
    public static final int  ROW_H        = 30;
    public static final int  PAD          = 16;
    public static final int  PAD_SM       = 8;

    // ─────────────────────────────────────────────────────────────────────
    // BOTONES
    // ─────────────────────────────────────────────────────────────────────

    /** Botón primario verde — acción principal */
    public static JButton primaryButton(String text) {
        return new RoundButton(text, PRIMARY, PRIMARY_DARK, Color.WHITE);
    }

    /** Botón secundario — borde verde, fondo blanco */
    public static JButton secondaryButton(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? PRIMARY_LIGHT : BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), RADIUS_SM, RADIUS_SM);
                g2.setColor(PRIMARY);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, RADIUS_SM, RADIUS_SM);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        styleBaseBtn(b);
        b.setForeground(PRIMARY);
        b.setPreferredSize(new Dimension(b.getPreferredSize().width, BTN_H));
        return b;
    }

    /** Botón de peligro — rojo */
    public static JButton dangerButton(String text) {
        return new RoundButton(text, ERROR, new Color(0xb9,0x1c,0x1c), Color.WHITE);
    }

    /** Botón ghost — sin fondo, solo texto con hover sutil */
    public static JButton ghostButton(String text) {
        JButton b = new JButton(text);
        b.setFont(FONT_BODY);
        b.setForeground(TEXT_SUB);
        b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setForeground(PRIMARY); }
            public void mouseExited(MouseEvent e)  { b.setForeground(TEXT_SUB); }
        });
        return b;
    }

    /** Botón de exportar (Excel/PDF) compacto */
    public static JButton exportButton(String text) {
        JButton b = new JButton(text);
        b.setFont(FONT_SMALL);
        b.setForeground(TEXT_MAIN);
        b.setBackground(BG_SOFT);
        b.setOpaque(true);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(PRIMARY_LIGHT); b.setForeground(PRIMARY); }
            public void mouseExited(MouseEvent e)  { b.setBackground(BG_SOFT); b.setForeground(TEXT_MAIN); }
        });
        return b;
    }

    private static void styleBaseBtn(JButton b) {
        b.setFont(FONT_BTN);
        b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
    }

    private static class RoundButton extends JButton {
        private final Color bg, bgHover, fg;
        RoundButton(String text, Color bg, Color bgHover, Color fg) {
            super(text);
            this.bg = bg; this.bgHover = bgHover; this.fg = fg;
            styleBaseBtn(this);
            setForeground(fg);
            setPreferredSize(new Dimension(getPreferredSize().width, BTN_H));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isPressed() ? bgHover.darker()
                    : getModel().isRollover() ? bgHover : bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), RADIUS_SM, RADIUS_SM);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // INPUTS
    // ─────────────────────────────────────────────────────────────────────

    public static JTextField styledInput(String placeholder) {
        JTextField f = new JTextField();
        applyInputStyle(f);
        f.putClientProperty("JTextField.placeholderText", placeholder);
        return f;
    }

    public static JPasswordField styledPassword() {
        JPasswordField f = new JPasswordField();
        applyInputStyle(f);
        return f;
    }

    public static <T> JComboBox<T> styledCombo(T[] items) {
        JComboBox<T> c = new JComboBox<>(items);
        c.setFont(FONT_BODY);
        c.setBackground(BG);
        c.setForeground(TEXT_MAIN);
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        c.setPreferredSize(new Dimension(c.getPreferredSize().width, INPUT_H));
        return c;
    }

    public static void applyInputStyle(JTextField f) {
        f.setFont(FONT_BODY);
        f.setForeground(TEXT_MAIN);
        f.setBackground(BG);
        f.setCaretColor(PRIMARY);
        f.setPreferredSize(new Dimension(f.getPreferredSize().width, INPUT_H));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, INPUT_H));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMARY, 2, true),
                        BorderFactory.createEmptyBorder(5, 9, 5, 9)));
            }
            public void focusLost(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER, 1, true),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            }
        });
    }

    public static JTextArea styledTextArea(int rows) {
        JTextArea a = new JTextArea(rows, 0);
        a.setFont(FONT_BODY);
        a.setForeground(TEXT_MAIN);
        a.setBackground(BG);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        return a;
    }

    public static JScrollPane textAreaScroll(JTextArea a) {
        JScrollPane sp = new JScrollPane(a);
        sp.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));
        return sp;
    }

    // ─────────────────────────────────────────────────────────────────────
    // TABLAS
    // ─────────────────────────────────────────────────────────────────────

    public static JTable styledTable(DefaultTableModel model) {
        JTable t = new JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row))
                    c.setBackground(row % 2 == 0 ? BG : ROW_ALT);
                return c;
            }
        };
        t.setFont(FONT_BODY);
        t.setForeground(TEXT_MAIN);
        t.setRowHeight(ROW_H);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFillsViewportHeight(true);
        t.setSelectionBackground(PRIMARY_LIGHT);
        t.setSelectionForeground(PRIMARY_DARK);
        t.setFocusable(false);
        t.getTableHeader().setReorderingAllowed(false);
        t.getTableHeader().setFont(FONT_TABLE_H);
        t.getTableHeader().setBackground(BG_SOFT);
        t.getTableHeader().setForeground(TEXT_SUB);
        t.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER));
        t.getTableHeader().setPreferredSize(new Dimension(0, 36));
        return t;
    }

    public static JScrollPane tableScroll(JTable t) {
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));
        sp.setBackground(BG);
        sp.getViewport().setBackground(BG);
        // Scrollbar minimalista
        sp.getVerticalScrollBar().setUI(new MinimalScrollBarUI());
        sp.getHorizontalScrollBar().setUI(new MinimalScrollBarUI());
        return sp;
    }

    // ─────────────────────────────────────────────────────────────────────
    // CARDS / SURFACES
    // ─────────────────────────────────────────────────────────────────────

    /** Panel card con sombra sutil y bordes redondeados */
    public static class Card extends JPanel {
        public Card() { this(PAD); }
        public Card(int padding) {
            setOpaque(false);
            setBorder(new EmptyBorder(padding, padding, padding, padding));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Sombra suave
            g2.setColor(new Color(0x31, 0x8c, 0x45, 15));
            g2.fillRoundRect(3, 3, getWidth()-3, getHeight()-3, RADIUS, RADIUS);
            // Fondo
            g2.setColor(SURFACE);
            g2.fillRoundRect(0, 0, getWidth()-3, getHeight()-3, RADIUS, RADIUS);
            // Borde
            g2.setColor(BORDER);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, getWidth()-4, getHeight()-4, RADIUS, RADIUS);
            g2.dispose();
            super.paintComponent(g);
        }
        @Override public boolean isOpaque() { return false; }
    }

    /** Card de métrica con valor grande y etiqueta pequeña */
    public static Card metricCard(String label, JLabel valueLabel, String icon, Color accent) {
        Card c = new Card(PAD);
        c.setLayout(new BorderLayout(0, 6));
        // Icono + label
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        top.setOpaque(false);
        JLabel ico = new JLabel(icon);
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(TEXT_SUB);
        top.add(ico); top.add(lbl);
        c.add(top, BorderLayout.NORTH);
        // Valor
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(accent);
        c.add(valueLabel, BorderLayout.CENTER);
        return c;
    }

    // ─────────────────────────────────────────────────────────────────────
    // LABELS Y TIPOGRAFÍA
    // ─────────────────────────────────────────────────────────────────────

    public static JLabel pageTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_H2);
        l.setForeground(TEXT_MAIN);
        return l;
    }

    public static JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_H3);
        l.setForeground(PRIMARY);
        l.setBorder(new MatteBorder(0, 3, 0, 0, PRIMARY));
        l.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 3, 0, 0, PRIMARY),
                new EmptyBorder(0, 6, 0, 0)));
        return l;
    }

    public static JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(TEXT_SUB);
        return l;
    }

    public static JLabel bodyLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_BODY);
        l.setForeground(TEXT_MAIN);
        return l;
    }

    // ─────────────────────────────────────────────────────────────────────
    // ALERTAS / BADGES
    // ─────────────────────────────────────────────────────────────────────

    public enum AlertType { SUCCESS, WARNING, ERROR, INFO }

    public static JPanel alert(String text, AlertType type) {
        Color bg, fg, border;
        String prefix;
        switch (type) {
            case SUCCESS -> { bg=SUCCESS_LIGHT; fg=PRIMARY_DARK;              border=BORDER;                prefix="✓  "; }
            case WARNING -> { bg=WARNING_LIGHT; fg=new Color(0x92,0x40,0x09); border=new Color(0xfb,0xd3,0x8d); prefix="⚠  "; }
            case ERROR   -> { bg=ERROR_LIGHT;   fg=new Color(0x99,0x1b,0x1b); border=new Color(0xfc,0xa5,0xa5); prefix="✕  "; }
            default      -> { bg=new Color(0xef,0xf6,0xff); fg=new Color(0x1e,0x40,0xaf); border=new Color(0xbe,0xdb,0xfe); prefix="ℹ  "; }
        }
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(bg);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border, 1, true),
                new EmptyBorder(8, 12, 8, 12)));
        JLabel l = new JLabel(prefix + text);
        l.setFont(FONT_BODY);
        l.setForeground(fg);
        p.add(l);
        return p;
    }

    public static JLabel badge(String text, Color bg, Color fg) {
        JLabel l = new JLabel("  " + text + "  ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setFont(FONT_BADGE);
        l.setForeground(fg);
        l.setBackground(bg);
        l.setOpaque(false);
        return l;
    }

    // ─────────────────────────────────────────────────────────────────────
    // SEPARADOR
    // ─────────────────────────────────────────────────────────────────────

    public static JSeparator separator() {
        JSeparator s = new JSeparator();
        s.setForeground(BORDER);
        s.setBackground(BG);
        return s;
    }

    // ─────────────────────────────────────────────────────────────────────
    // PANEL DE ENCABEZADO DE VENTANA (header interno, no el del dashboard)
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Header verde para ventanas secundarias (RF-01, RF-02, RF-03).
     * Muestra título + subtítulo + barra de búsqueda opcional.
     */
    public static JPanel windowHeader(String title, String subtitle) {
        JPanel p = new JPanel(new BorderLayout(0, 2)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, PRIMARY, getWidth(), 0, PRIMARY_DARK));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        p.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel t = new JLabel(title);
        t.setFont(FONT_H2);
        t.setForeground(Color.WHITE);
        p.add(t, BorderLayout.NORTH);

        if (subtitle != null && !subtitle.isEmpty()) {
            JLabel s = new JLabel(subtitle);
            s.setFont(FONT_SMALL);
            s.setForeground(new Color(0xbb, 0xe5, 0xc4));
            p.add(s, BorderLayout.CENTER);
        }
        return p;
    }

    // ─────────────────────────────────────────────────────────────────────
    // DIÁLOGOS (reemplaza JOptionPane)
    // ─────────────────────────────────────────────────────────────────────

    public static void showSuccess(Component parent, String msg) {
        showDialog(parent, "Operación exitosa", msg, AlertType.SUCCESS);
    }
    public static void showError(Component parent, String msg) {
        showDialog(parent, "Error", msg, AlertType.ERROR);
    }
    public static void showWarning(Component parent, String msg) {
        showDialog(parent, "Advertencia", msg, AlertType.WARNING);
    }
    public static boolean showConfirm(Component parent, String msg) {
        return JOptionPane.showConfirmDialog(parent, msg, "Confirmar",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    private static void showDialog(Component parent, String title, String msg, AlertType type) {
        int jType = switch(type) {
            case SUCCESS, INFO -> JOptionPane.INFORMATION_MESSAGE;
            case WARNING       -> JOptionPane.WARNING_MESSAGE;
            case ERROR         -> JOptionPane.ERROR_MESSAGE;
        };
        JOptionPane.showMessageDialog(parent, msg, title, jType);
    }

    // ─────────────────────────────────────────────────────────────────────
    // SCROLLBAR MINIMALISTA
    // ─────────────────────────────────────────────────────────────────────

    public static class MinimalScrollBarUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            thumbColor = new Color(0xc8, 0xe6, 0xce);
            trackColor = BG_SOFT;
        }
        @Override protected JButton createDecreaseButton(int o) { return zeroBtn(); }
        @Override protected JButton createIncreaseButton(int o) { return zeroBtn(); }
        private JButton zeroBtn() {
            JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); return b;
        }
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isDragging ? PRIMARY : thumbColor);
            g2.fillRoundRect(r.x+2, r.y+2, r.width-4, r.height-4, 6, 6);
            g2.dispose();
        }
        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            g.setColor(trackColor); g.fillRect(r.x, r.y, r.width, r.height);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // UTILIDADES
    // ─────────────────────────────────────────────────────────────────────

    /** Fila de formulario: label arriba, campo abajo */
    public static JPanel formField(String labelText, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.add(fieldLabel(labelText), BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    /** Panel con padding estándar y fondo BG_SOFT */
    public static JPanel contentPanel() {
        JPanel p = new JPanel();
        p.setBackground(BG_SOFT);
        p.setBorder(new EmptyBorder(PAD, PAD, PAD, PAD));
        return p;
    }

    /** Aplica LAF mínimo: antialiasing, fondo blanco global */
    public static void applyGlobal() {
        try {
            UIManager.put("Panel.background",       BG_SOFT);
            UIManager.put("OptionPane.background",  BG);
            UIManager.put("Label.foreground",       TEXT_MAIN);
            UIManager.put("TextField.background",   BG);
            UIManager.put("TextField.foreground",   TEXT_MAIN);
            UIManager.put("ComboBox.background",    BG);
            UIManager.put("ComboBox.foreground",    TEXT_MAIN);
            UIManager.put("Table.background",       BG);
            UIManager.put("Table.foreground",       TEXT_MAIN);
            UIManager.put("ScrollPane.background",  BG);
            UIManager.put("TabbedPane.selected",    PRIMARY_LIGHT);
            UIManager.put("TabbedPane.foreground",  TEXT_MAIN);
        } catch (Exception ignored) {}
    }
}
