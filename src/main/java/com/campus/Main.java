
package com.campus;

import com.campus.database.Database;
import com.campus.database.AdminInitializer;
import com.campus.ui.LoginFrame;

import javax.swing.SwingUtilities;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {

        try {
            Database.initializeDatabase();

            AdminInitializer.createDefaultAdmin();

            System.out.println("Campus Lost & Found Portal");
            System.out.println("Application started successfully!");

            SwingUtilities.invokeLater(() -> {
                LoginFrame login = new LoginFrame();
                login.setVisible(true);
            });

        } catch (SQLException e) {

            System.out.println("Database connection failed!");
            e.printStackTrace();
        }
    }
}
