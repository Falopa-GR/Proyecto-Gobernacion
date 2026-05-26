package org.example;

import service.AuthService;
import view.LoginWindow;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Crear usuarios por defecto si la BD está vacía
        AuthService.seedAdminIfEmpty();

        // Arrancar con la pantalla de login
        SwingUtilities.invokeLater(() -> new LoginWindow().setVisible(true));
    }
}