
package com.campus.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

public class Database {

    private static final String URL = "jdbc:sqlite:database/campus.db";

    public static Connection getConnection() throws SQLException {

        try {
            Files.createDirectories(Path.of("database"));
        } catch (IOException e) {
            throw new SQLException("Database folder error", e);
        }

        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() throws SQLException {

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    full_name TEXT NOT NULL,
                    email TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    role TEXT NOT NULL DEFAULT 'STUDENT'
                )
            """);

            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    item_type TEXT NOT NULL,
                    item_name TEXT NOT NULL,
                    category TEXT NOT NULL,
                    description TEXT,
                    location TEXT NOT NULL,
                    item_date TEXT NOT NULL,
                    status TEXT DEFAULT 'OPEN',
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
            """);

            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS claims (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    item_id INTEGER NOT NULL,
                    claimant_id INTEGER NOT NULL,
                    message TEXT,
                    status TEXT DEFAULT 'PENDING',
                    FOREIGN KEY (item_id) REFERENCES items(id),
                    FOREIGN KEY (claimant_id) REFERENCES users(id)
                )
            """);

            System.out.println("Database created successfully!");
        }
    }
}
