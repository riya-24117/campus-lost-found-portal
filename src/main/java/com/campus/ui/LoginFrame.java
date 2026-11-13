
package com.campus.ui;

import com.campus.database.Database;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class LoginFrame extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;

    public LoginFrame() {

        setTitle("Campus Lost & Found Portal");
        setSize(500, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(240, 245, 255));

        JPanel formPanel = new JPanel(null);
        formPanel.setBackground(Color.WHITE);
        formPanel.setPreferredSize(new Dimension(380, 350));

        JLabel title = new JLabel("Campus Lost & Found");
        title.setFont(new Font("Arial", Font.BOLD, 23));
        title.setBounds(55, 25, 300, 35);

        JLabel subtitle = new JLabel("Login to your account");
        subtitle.setBounds(115, 65, 200, 25);

        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setBounds(35, 115, 300, 25);

        emailField = new JTextField();
        emailField.setBounds(35, 145, 310, 38);

        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setBounds(35, 200, 300, 25);

        passwordField = new JPasswordField();
        passwordField.setBounds(35, 230, 310, 38);

        JButton loginButton = new JButton("LOGIN");
        loginButton.setBounds(35, 290, 310, 40);
        loginButton.setBackground(new Color(45, 90, 180));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);

        JButton signupButton = new JButton("Create New Account");
        signupButton.setBounds(90, 330, 200, 25);
        signupButton.setBorderPainted(false);
        signupButton.setContentAreaFilled(false);
        signupButton.setForeground(new Color(45, 90, 180));

        loginButton.addActionListener(e -> loginUser());

        signupButton.addActionListener(e -> {
            SignupFrame signup = new SignupFrame();
            signup.setVisible(true);
        });

        formPanel.add(title);
        formPanel.add(subtitle);
        formPanel.add(emailLabel);
        formPanel.add(emailField);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);
        formPanel.add(loginButton);
        formPanel.add(signupButton);

        mainPanel.add(formPanel);
        add(mainPanel);
    }

    private void loginUser() {

        String email = emailField.getText().trim().toLowerCase();
        char[] password = passwordField.getPassword();

        if (email.isEmpty() || password.length == 0) {

            JOptionPane.showMessageDialog(this,
                    "Please enter email and password.");

            Arrays.fill(password, '\0');
            return;
        }

        String sql = """
                SELECT id, full_name, password_hash, role
                FROM users
                WHERE email = ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, email);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {

                    String name = result.getString("full_name");
                    String storedHash =
                            result.getString("password_hash");

                    String role = result.getString("role");

                    if (verifyPassword(password, storedHash)) {

                        int userId = result.getInt("id");

                        JOptionPane.showMessageDialog(this,
                                "Welcome, " + name
                                        + "!\nLogin successful.");

                        dispose();

                        if ("ADMIN".equalsIgnoreCase(role)) {

                            AdminDashboard dashboard =
                                    new AdminDashboard();

                            dashboard.setVisible(true);

                        } else {

                            StudentDashboard dashboard =
                                    new StudentDashboard(userId, name);

                            dashboard.setVisible(true);
                        }

                    } else {

                        JOptionPane.showMessageDialog(this,
                                "Incorrect email or password.");
                    }

                } else {

                    JOptionPane.showMessageDialog(this,
                            "Incorrect email or password.");
                }
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(this,
                    "Login error: " + e.getMessage());

        } finally {

            Arrays.fill(password, '\0');
            passwordField.setText("");
        }
    }

    private boolean verifyPassword(char[] password, String storedHash)
            throws Exception {

        String[] parts = storedHash.split("\\$");

        if (parts.length != 4 || !parts[0].equals("PBKDF2")) {
            return false;
        }

        int iterations = Integer.parseInt(parts[1]);

        byte[] salt = Base64.getDecoder().decode(parts[2]);
        byte[] expectedHash = Base64.getDecoder().decode(parts[3]);

        PBEKeySpec spec = new PBEKeySpec(
                password,
                salt,
                iterations,
                expectedHash.length * 8
        );

        try {

            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance(
                            "PBKDF2WithHmacSHA256");

            byte[] actualHash =
                    factory.generateSecret(spec).getEncoded();

            return MessageDigest.isEqual(
                    expectedHash, actualHash);

        } finally {
            spec.clearPassword();
        }
    }
}
