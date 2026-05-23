package view;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame{
    public MainWindow(){
        setTitle("Sistema de Gestión - Talento Humano");
        setSize(650, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
    }
}
