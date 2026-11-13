
package com.campus.database;

import java.sql.*;
import java.security.SecureRandom;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Arrays;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class AdminInitializer {

    public static void createDefaultAdmin() throws SQLException {

        String email = "admin@campus.edu";

        try (Connection connection = Database.getConnection();
             PreparedStatement check = connection.prepareStatement(
                     "SELECT id FROM users WHERE role = 'ADMIN' LIMIT 1");
             ResultSet result = check.executeQuery()) {

            if (result.next()) {
                System.out.println("Admin account already exists.");
                return;
            }
        }

        char[] password = "Admin@12345".toCharArray();
        String hashedPassword;

        try {
            hashedPassword = hashPassword(password);
        } finally {
            Arrays.fill(password, '\0');
        }

        String sql = """
            INSERT INTO users
            (full_name, email, password_hash, role)
            VALUES (?, ?, ?, 'ADMIN')
        """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, "Campus Administrator");
            statement.setString(2, email);
            statement.setString(3, hashedPassword);

            statement.executeUpdate();

            System.out.println("Admin account created successfully!");
        }
    }

    private static String hashPassword(char[] password)
            throws SQLException {

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

        } catch (GeneralSecurityException e) {
            throw new SQLException("Unable to secure admin password.", e);
        } finally {
            spec.clearPassword();
        }
    }
}
