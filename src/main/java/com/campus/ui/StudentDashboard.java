
package com.campus.ui;

import com.campus.database.Database;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class StudentDashboard extends JFrame {

    private final int userId;
    private final String studentName;

    public StudentDashboard(int userId, String studentName) {

        this.userId = userId;
        this.studentName = studentName;

        setTitle("Campus Lost & Found - Student Dashboard");
        setSize(850, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(20, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(45, 90, 180));
        header.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel welcome = new JLabel("Welcome, " + studentName + "!");
        welcome.setFont(new Font("Arial", Font.BOLD, 24));
        welcome.setForeground(Color.WHITE);

        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        header.add(welcome, BorderLayout.WEST);
        header.add(logout, BorderLayout.EAST);

        JLabel heading = new JLabel("Student Dashboard", SwingConstants.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 25));

        JPanel center = new JPanel(new GridLayout(2, 2, 20, 20));
        center.setBorder(BorderFactory.createEmptyBorder(20, 40, 30, 40));
        center.setBackground(new Color(240, 245, 255));

        JButton lostButton = createButton("Report Lost Item");
        JButton foundButton = createButton("Report Found Item");
        JButton viewButton = createButton("View All Items");
        JButton claimsButton = createButton("My Claims");

        lostButton.addActionListener(e -> openReportForm("LOST"));
        foundButton.addActionListener(e -> openReportForm("FOUND"));
        viewButton.addActionListener(e -> showItems());
        claimsButton.addActionListener(e -> showMyClaims());

        center.add(lostButton);
        center.add(foundButton);
        center.add(viewButton);
        center.add(claimsButton);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(240, 245, 255));
        main.add(heading, BorderLayout.NORTH);
        main.add(center, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(main, BorderLayout.CENTER);
    }

    private JButton createButton(String text) {

        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setBackground(new Color(45, 90, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);

        return button;
    }

    private void openReportForm(String type) {

        JTextField nameField = new JTextField();
        JTextField categoryField = new JTextField();
        JTextField locationField = new JTextField();
        JTextField dateField = new JTextField(
                java.time.LocalDate.now().toString());

        JTextArea descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));

        form.add(new JLabel("Item Name:"));
        form.add(nameField);

        form.add(new JLabel("Category:"));
        form.add(categoryField);

        form.add(new JLabel("Location:"));
        form.add(locationField);

        form.add(new JLabel("Date (YYYY-MM-DD):"));
        form.add(dateField);

        form.add(new JLabel("Description:"));
        form.add(new JScrollPane(descriptionArea));

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                "Report " + type + " Item",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String name = nameField.getText().trim();
        String category = categoryField.getText().trim();
        String location = locationField.getText().trim();
        String date = dateField.getText().trim();
        String description = descriptionArea.getText().trim();

        if (name.isEmpty() || category.isEmpty()
                || location.isEmpty() || date.isEmpty()) {

            JOptionPane.showMessageDialog(this,
                    "Please fill all required fields.");
            return;
        }

        try {
            java.time.LocalDate.parse(date);
        } catch (java.time.format.DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,
                    "Enter date in YYYY-MM-DD format.");
            return;
        }

        String sql = """
            INSERT INTO items
            (user_id, item_type, item_name, category,
             description, location, item_date)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setString(2, type);
            statement.setString(3, name);
            statement.setString(4, category);
            statement.setString(5, description);
            statement.setString(6, location);
            statement.setString(7, date);

            statement.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    type + " item reported successfully!");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error: " + e.getMessage());
        }
    }

    private void showItems() {

        String[] columns = {
                "ID", "Type", "Item Name",
                "Category", "Location", "Date"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        String sql = """
            SELECT id, item_type, item_name,
                   category, location, item_date
            FROM items
            WHERE status = 'OPEN'
         ORDER BY id DESC
        """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("item_type"),
                        rs.getString("item_name"),
                        rs.getString("category"),
                        rs.getString("location"),
                        rs.getString("item_date")
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Unable to load items: " + e.getMessage());
            return;
        }

        JTable table = new JTable(model);
        table.setRowHeight(25);

        JButton claimButton = new JButton("Claim Selected Item");
        JButton closeButton = new JButton("Close");

        claimButton.addActionListener(e -> {
            int row = table.getSelectedRow();

            if (row == -1) {
                JOptionPane.showMessageDialog(this,
                        "Please select an item first.");
                return;
            }

            int itemId = (int) model.getValueAt(row, 0);
            submitClaim(itemId);
        });

        JPanel buttons = new JPanel();
        buttons.add(claimButton);
        buttons.add(closeButton);

        JDialog dialog = new JDialog(this, "Available Items", true);
        dialog.setSize(750, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.add(new JScrollPane(table), BorderLayout.CENTER);
        dialog.add(buttons, BorderLayout.SOUTH);

        closeButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    private void submitClaim(int itemId) {

        String message = JOptionPane.showInputDialog(
                this,
                "Why do you think this item belongs to you?"
        );

        if (message == null || message.trim().isEmpty()) {
            return;
        }

        try (Connection connection = Database.getConnection()) {

            String ownerQuery =
                    "SELECT user_id FROM items WHERE id = ?";

            try (PreparedStatement check =
                         connection.prepareStatement(ownerQuery)) {

                check.setInt(1, itemId);

                try (ResultSet rs = check.executeQuery()) {
                    if (!rs.next()) {
                        JOptionPane.showMessageDialog(this,
                                "Item not found.");
                        return;
                    }

                    if (rs.getInt("user_id") == userId) {
                        JOptionPane.showMessageDialog(this,
                                "You cannot claim your own reported item.");
                        return;
                    }
                }
            }

            String duplicateQuery = """
                SELECT id FROM claims
                WHERE item_id = ? AND claimant_id = ?
            """;

            try (PreparedStatement check =
                         connection.prepareStatement(duplicateQuery)) {

                check.setInt(1, itemId);
                check.setInt(2, userId);

                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) {
                        JOptionPane.showMessageDialog(this,
                                "You have already submitted a claim.");
                        return;
                    }
                }
            }

            String sql = """
                INSERT INTO claims (item_id, claimant_id, message)
                VALUES (?, ?, ?)
            """;

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setInt(1, itemId);
                statement.setInt(2, userId);
                statement.setString(3, message.trim());
                statement.executeUpdate();
            }

            JOptionPane.showMessageDialog(this,
                    "Claim submitted successfully!");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Claim error: " + e.getMessage());
        }
    }

    private void showMyClaims() {

        String[] columns = {
                "Claim ID", "Item Name",
                "Item Type", "Location", "Status"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        String sql = """
            SELECT c.id, i.item_name, i.item_type,
                   i.location, c.status
            FROM claims c
            JOIN items i ON c.item_id = i.id
            WHERE c.claimant_id = ?
           ORDER BY c.id DESC
        """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet rs = statement.executeQuery()) {

                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("item_name"),
                            rs.getString("item_type"),
                            rs.getString("location"),
                            rs.getString("status")
                    });
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Unable to load claims: " + e.getMessage());
            return;
        }

        JTable table = new JTable(model);
        table.setRowHeight(25);

        JOptionPane.showMessageDialog(
                this,
                new JScrollPane(table),
                "My Claims",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
