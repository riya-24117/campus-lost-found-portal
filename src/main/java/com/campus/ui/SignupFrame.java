
package com.campus.ui;

import com.campus.database.Database;

import javax.swing.*;
import java.awt.*;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class SignupFrame extends JFrame {

    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    public SignupFrame() {

        setTitle("Create New Account");
        setSize(500, 580);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setBackground(new Color(240, 245, 255));
        panel.setLayout(null);

        JLabel title = new JLabel("Create Account");
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setBounds(140, 25, 250, 35);

        JLabel nameLabel = new JLabel("Full Name");
        nameLabel.setBounds(65, 90, 300, 25);

        nameField = new JTextField();
        nameField.setBounds(65, 115, 350, 35);

        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setBounds(65, 160, 300, 25);

        emailField = new JTextField();
        emailField.setBounds(65, 185, 350, 35);

        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setBounds(65, 230, 300, 25);

        passwordField = new JPasswordField();
        passwordField.setBounds(65, 255, 350, 35);

        JLabel confirmLabel = new JLabel("Confirm Password");
        confirmLabel.setBounds(65, 300, 300, 25);

        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setBounds(65, 325, 350, 35);

        JButton registerButton = new JButton("REGISTER");
        registerButton.setBounds(65, 390, 350, 40);
        registerButton.setBackground(new Color(45, 90, 180));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);

        JButton backButton = new JButton("Back to Login");
        backButton.setBounds(145, 445, 180, 30);

        registerButton.addActionListener(e -> registerUser());

        backButton.addActionListener(e -> dispose());

        panel.add(title);
        panel.add(nameLabel);
        panel.add(nameField);
        panel.add(emailLabel);
        panel.add(emailField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(confirmLabel);
        panel.add(confirmPasswordField);
        panel.add(registerButton);
        panel.add(backButton);

        add(panel);
    }

    private void registerUser() {

        String name = nameField.getText().trim();
        String email = emailField.getText().trim().toLowerCase();

        char[] password = passwordField.getPassword();
        char[] confirmPassword = confirmPasswordField.getPassword();

        if (name.isEmpty() || email.isEmpty()
                || password.length == 0 || confirmPassword.length == 0) {

            JOptionPane.showMessageDialog(this,
                    "Please fill all fields.");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid email address.");
            return;
        }

        if (password.length < 8) {
            JOptionPane.showMessageDialog(this,
                    "Password must contain at least 8 characters.");
            return;
        }

        if (!java.util.Arrays.equals(password, confirmPassword)) {
            JOptionPane.showMessageDialog(this,
                    "Passwords do not match.");
            return;
        }

        try {
            String hashedPassword = hashPassword(password);

            String sql = """
                INSERT INTO users (full_name, email, password_hash)
                VALUES (?, ?, ?)
            """;

            try (Connection connection = Database.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setString(1, name);
                statement.setString(2, email);
                statement.setString(3, hashedPassword);

                statement.executeUpdate();
            }

            JOptionPane.showMessageDialog(this,
                    "Account created successfully!");

            dispose();

        } catch (SQLException e) {

            if (e.getMessage() != null
                    && e.getMessage().toLowerCase().contains("unique")) {

                JOptionPane.showMessageDialog(this,
                        "This email is already registered.");

            } else {
                JOptionPane.showMessageDialog(this,
                        "Registration failed: " + e.getMessage());
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Password security error: " + e.getMessage());
        }
    }

    private String hashPassword(char[] password) throws Exception {

        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        int iterations = 120000;

        PBEKeySpec spec = new PBEKeySpec(
                password, salt, iterations, 256);

        try {
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            byte[] hash = factory.generateSecret(spec).getEncoded();

            return "PBKDF2$" + iterations + "$"
                    + Base64.getEncoder().encodeToString(salt) + "$"
                    + Base64.getEncoder().encodeToString(hash);

        } finally {
            spec.clearPassword();
        }
    }
}
