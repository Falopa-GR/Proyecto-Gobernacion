package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
/**
 *
 */
public class DashboardWindow extends JFrame {

    private SidebarButton btnInicio;
    private SidebarButton btnReportes;

    private JTextField      searchField;
    private JPopupMenu      searchPopup;
    private javax.swing.Timer searchDebounceTimer;

    public DashboardWindow() {
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(900, 600));

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);


        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(245, 247, 250));
        main.setBorder(new EmptyBorder(16, 16, 16, 16));
        main.add(contentPanel,      BorderLayout.CENTER);
        add(main, BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        panel.setPreferredSize(new Dimension(240, 0));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(panel.getBackground());
        top.setBorder(new EmptyBorder(18, 18, 18, 18));
        JLabel logo = new JLabel("TalentoHumano");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        top.add(Box.createHorizontalStrut(8));
        top.add(logo);
        panel.add(top, BorderLayout.NORTH);

        JPanel menu = new JPanel();
        menu.setBackground(panel.getBackground());
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(new EmptyBorder(8, 8, 8, 8));

        btnInicio      = new SidebarButton("Inicio",             "🏠");
        btnReportes    = new SidebarButton("Reportes",           "📊");

        menu.add(btnInicio);
        menu.add(btnReportes);
        menu.add(Box.createVerticalStrut(10));
        menu.add(Box.createVerticalGlue());


        panel.add(menu, BorderLayout.CENTER);

        return panel;
    }


        active.setActive(true);
    }

        }
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        left.setOpaque(false);
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        left.add(searchField);
        header.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);


        header.add(right, BorderLayout.EAST);

        searchPopup = new JPopupMenu();
        searchPopup.setFocusable(false);
        searchDebounceTimer = new javax.swing.Timer(300, e -> performSearch());
        searchDebounceTimer.setRepeats(false);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
        });

        return header;
    }

    }

            @Override
            }
            @Override
            protected void done() {
                try {
                }
            }
        }.execute();
    }

        searchPopup.removeAll();
        } else {
            });
            list.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                }
            });
            JScrollPane sp = new JScrollPane(list);
            searchPopup.add(sp);
        }
        searchPopup.show(searchField, 0, searchField.getHeight());
        searchField.requestFocusInWindow();
    }

        searchPopup.setVisible(false);
    }
    }

        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

        CardPanel card = new CardPanel();

        JLabel lblTitle = new JLabel(title);
        card.add(lblTitle, BorderLayout.NORTH);


        return card;
    }

    }

        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setShowGrid(false);
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(226, 232, 240));
        table.getTableHeader().setReorderingAllowed(false);
            }

        public CardPanel() {
            setOpaque(false);
        }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class SidebarButton extends JPanel {
        private final JPanel indicator;

            setLayout(new BorderLayout());
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setOpaque(false);

            indicator = new JPanel();
            indicator.setBackground(new Color(99, 102, 241));
            indicator.setVisible(false);
            add(indicator, BorderLayout.WEST);

            inner.setOpaque(false);
            lblIcon.setForeground(new Color(226, 232, 240));
            lblText = new JLabel(text);
            lblText.setForeground(new Color(226, 232, 240));
            add(inner, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                        al.actionPerformed(new ActionEvent(SidebarButton.this, ActionEvent.ACTION_PERFORMED, "click"));
                }
            });
        }


            repaint();
        }

            if (animTimer != null) animTimer.stop();
            animTimer.addActionListener(e -> {
            });
            animTimer.start();
        }

            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(current);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
    }
}