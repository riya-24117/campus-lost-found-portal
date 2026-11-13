
package com.campus.ui;

import com.campus.database.Database;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AdminDashboard extends JFrame {

    public AdminDashboard() {

        setTitle("Campus Lost & Found - Admin Dashboard");
        setSize(850, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(35, 55, 100));
        header.setBorder(
                BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("Admin Dashboard");
        title.setFont(new Font("Arial", Font.BOLD, 25));
        title.setForeground(Color.WHITE);

        JButton logout = new JButton("Logout");

        logout.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        header.add(title, BorderLayout.WEST);
        header.add(logout, BorderLayout.EAST);

        JLabel subtitle = new JLabel(
                "Manage students, items and claims",
                SwingConstants.CENTER
        );
        subtitle.setFont(new Font("Arial", Font.BOLD, 23));

        JPanel buttons = new JPanel(new GridLayout(3, 1, 20, 20));
        buttons.setBackground(new Color(240, 245, 255));
        buttons.setBorder(
                BorderFactory.createEmptyBorder(35, 180, 35, 180));

        JButton studentsButton = createButton("View All Students");
        JButton itemsButton = createButton("Manage Lost & Found Items");
        JButton claimsButton = createButton("Manage Claims");

        studentsButton.addActionListener(e -> showStudents());
        itemsButton.addActionListener(e -> manageItems());
        claimsButton.addActionListener(e -> manageClaims());

        buttons.add(studentsButton);
        buttons.add(itemsButton);
        buttons.add(claimsButton);

        JPanel center = new JPanel(new BorderLayout());
        center.add(subtitle, BorderLayout.NORTH);
        center.add(buttons, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    private JButton createButton(String text) {

        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setBackground(new Color(45, 90, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);

        return button;
    }

    private void showStudents() {

        String[] columns = {
                "ID", "Full Name", "Email"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0);

        String sql = """
            SELECT id, full_name, email
            FROM users
            WHERE role = 'STUDENT'
            ORDER BY id DESC
        """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("email")
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Unable to load students: " + e.getMessage());
            return;
        }

        JTable table = new JTable(model);
        table.setRowHeight(25);

        JOptionPane.showMessageDialog(
                this,
                new JScrollPane(table),
                "Registered Students",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void manageItems() {

        String[] columns = {
                "ID", "Type", "Item Name",
                "Category", "Location", "Date", "Status"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(25);

        JButton editButton = new JButton("Edit Selected");
        JButton deleteButton = new JButton("Delete Selected");
        JButton closeButton = new JButton("Close");

        JDialog dialog = new JDialog(
                this, "Manage Items", true);

        Runnable refreshItems = () -> {

            model.setRowCount(0);

            String sql = """
                SELECT id, item_type, item_name,
                       category, location, item_date, status
                FROM items
                ORDER BY id DESC
            """;

            try (Connection connection = Database.getConnection();
                 PreparedStatement statement =
                         connection.prepareStatement(sql);
                 ResultSet rs = statement.executeQuery()) {

                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("item_type"),
                            rs.getString("item_name"),
                            rs.getString("category"),
                            rs.getString("location"),
                            rs.getString("item_date"),
                            rs.getString("status")
                    });
                }

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(dialog,
                        "Unable to load items: " + e.getMessage());
            }
        };

        editButton.addActionListener(e -> {

            int row = table.getSelectedRow();

            if (row == -1) {
                JOptionPane.showMessageDialog(dialog,
                        "Select an item first.");
                return;
            }

            int itemId = (int) model.getValueAt(row, 0);

            JTextField nameField = new JTextField(
                    model.getValueAt(row, 2).toString());

            JTextField categoryField = new JTextField(
                    model.getValueAt(row, 3).toString());

            JTextField locationField = new JTextField(
                    model.getValueAt(row, 4).toString());

            JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));

            form.add(new JLabel("Item Name:"));
            form.add(nameField);

            form.add(new JLabel("Category:"));
            form.add(categoryField);

            form.add(new JLabel("Location:"));
            form.add(locationField);

            int choice = JOptionPane.showConfirmDialog(
                    dialog, form,
                    "Update Item",
                    JOptionPane.OK_CANCEL_OPTION
            );

            if (choice != JOptionPane.OK_OPTION) {
                return;
            }

            String sql = """
                UPDATE items
                SET item_name = ?, category = ?, location = ?
                WHERE id = ?
            """;

            try (Connection connection = Database.getConnection();
                 PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(1, nameField.getText().trim());
                statement.setString(2, categoryField.getText().trim());
                statement.setString(3, locationField.getText().trim());
                statement.setInt(4, itemId);

                statement.executeUpdate();

                JOptionPane.showMessageDialog(dialog,
                        "Item updated successfully!");

                refreshItems.run();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Update failed: " + ex.getMessage());
            }
        });

        deleteButton.addActionListener(e -> {

            int row = table.getSelectedRow();

            if (row == -1) {
                JOptionPane.showMessageDialog(dialog,
                        "Select an item first.");
                return;
            }

            int itemId = (int) model.getValueAt(row, 0);

            int choice = JOptionPane.showConfirmDialog(
                    dialog,
                    "Delete selected item and its related claims?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION
            );

            if (choice != JOptionPane.YES_OPTION) {
                return;
            }

            try (Connection connection = Database.getConnection()) {

                connection.setAutoCommit(false);

                try {
                    try (PreparedStatement claims =
                                 connection.prepareStatement(
                                         "DELETE FROM claims WHERE item_id = ?")) {

                        claims.setInt(1, itemId);
                        claims.executeUpdate();
                    }

                    try (PreparedStatement item =
                                 connection.prepareStatement(
                                         "DELETE FROM items WHERE id = ?")) {

                        item.setInt(1, itemId);
                        item.executeUpdate();
                    }

                    connection.commit();

                    JOptionPane.showMessageDialog(dialog,
                            "Item deleted successfully!");

                    refreshItems.run();

                } catch (SQLException ex) {
                    connection.rollback();
                    throw ex;
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Delete failed: " + ex.getMessage());
            }
        });

        closeButton.addActionListener(e -> dialog.dispose());

        JPanel controls = new JPanel();
        controls.add(editButton);
        controls.add(deleteButton);
        controls.add(closeButton);

        dialog.setLayout(new BorderLayout());
        dialog.add(new JScrollPane(table), BorderLayout.CENTER);
        dialog.add(controls, BorderLayout.SOUTH);
        dialog.setSize(850, 400);
        dialog.setLocationRelativeTo(this);

        refreshItems.run();
        dialog.setVisible(true);
    }

    private void manageClaims() {

        String[] columns = {
                "Claim ID", "Item Name",
                "Student Name", "Message", "Status"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(25);

        JButton approveButton = new JButton("Approve");
        JButton rejectButton = new JButton("Reject");
        JButton closeButton = new JButton("Close");

        JDialog dialog = new JDialog(
                this, "Manage Claims", true);

        Runnable refreshClaims = () -> {

            model.setRowCount(0);

            String sql = """
                SELECT c.id, i.item_name,
                       u.full_name, c.message, c.status
                FROM claims c
                JOIN items i ON c.item_id = i.id
                JOIN users u ON c.claimant_id = u.id
                ORDER BY c.id DESC
            """;

            try (Connection connection = Database.getConnection();
                 PreparedStatement statement =
                         connection.prepareStatement(sql);
                 ResultSet rs = statement.executeQuery()) {

                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("item_name"),
                            rs.getString("full_name"),
                            rs.getString("message"),
                            rs.getString("status")
                    });
                }

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(dialog,
                        "Unable to load claims: " + e.getMessage());
            }
        };

        java.util.function.Consumer<String> updateClaim = status -> {

            int row = table.getSelectedRow();

            if (row == -1) {
                JOptionPane.showMessageDialog(dialog,
                        "Select a claim first.");
                return;
            }

            int claimId = (int) model.getValueAt(row, 0);

            String sql =
                    "UPDATE claims SET status = ? WHERE id = ?";

            try (Connection connection = Database.getConnection();
                 PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(1, status);
                statement.setInt(2, claimId);

                statement.executeUpdate();

                JOptionPane.showMessageDialog(dialog,
                        "Claim " + status.toLowerCase() + "!");

                refreshClaims.run();

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(dialog,
                        "Unable to update claim: " + e.getMessage());
            }
        };

        approveButton.addActionListener(
                e -> updateClaim.accept("APPROVED"));

        rejectButton.addActionListener(
                e -> updateClaim.accept("REJECTED"));

        closeButton.addActionListener(e -> dialog.dispose());

        JPanel controls = new JPanel();
        controls.add(approveButton);
        controls.add(rejectButton);
        controls.add(closeButton);

        dialog.setLayout(new BorderLayout());
        dialog.add(new JScrollPane(table), BorderLayout.CENTER);
        dialog.add(controls, BorderLayout.SOUTH);
        dialog.setSize(850, 400);
        dialog.setLocationRelativeTo(this);

        refreshClaims.run();
        dialog.setVisible(true);
    }
}
