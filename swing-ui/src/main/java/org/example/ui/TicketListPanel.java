package org.example.ui;

import net.miginfocom.swing.MigLayout;
import org.example.model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class TicketListPanel extends JPanel {
    private final MainFrame mainFrame;
    private final JTable ticketTable;
    private final DefaultTableModel tableModel;
    private final JComboBox<Status> statusFilter;

    public TicketListPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new MigLayout("fill, insets 20", "[grow]", "[][]20[][grow][]"));

        // Header
        JPanel headerPanel = new JPanel(new MigLayout("", "[]push[][]", "[]"));
        JLabel welcomeLabel = new JLabel("Welcome to IT Support Ticket System");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));

        JButton createTicketButton = new JButton("Create New Ticket");
        createTicketButton.addActionListener(e -> mainFrame.showCreateTicketPanel());

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> mainFrame.showLoginPanel());

        headerPanel.add(welcomeLabel);
        headerPanel.add(createTicketButton);
        headerPanel.add(logoutButton);

        add(headerPanel, "cell 0 0, growx");

        // Filters
        JPanel filterPanel = new JPanel(new MigLayout("", "[][]", "[]"));
        statusFilter = new JComboBox<>(Status.values());
        statusFilter.insertItemAt(null, 0);
        statusFilter.setSelectedIndex(0);
        statusFilter.addActionListener(e -> refreshTickets());

        filterPanel.add(new JLabel("Filter by Status:"));
        filterPanel.add(statusFilter);

        add(filterPanel, "cell 0 1, growx");

        // Ticket Table
        String[] columnNames = {"ID", "Title", "Priority", "Category", "Status", "Created Date"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        ticketTable = new JTable(tableModel);
        ticketTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ticketTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && ticketTable.getSelectedRow() != -1) {
                showTicketDetails(ticketTable.getSelectedRow());
            }
        });

        JScrollPane scrollPane = new JScrollPane(ticketTable);
        add(scrollPane, "cell 0 2, grow");
    }

    public void refreshTickets() {
        tableModel.setRowCount(0);
        // TODO: Implement actual API call
        // For now, adding sample data
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // Sample data
        addTicketRow(1L, "Network Issue", Priority.HIGH, Category.NETWORK, Status.NEW, "2024-02-21 10:00");
        addTicketRow(2L, "Software Installation", Priority.MEDIUM, Category.SOFTWARE, Status.IN_PROGRESS, "2024-02-21 11:30");
    }

    private void addTicketRow(Long id, String title, Priority priority, Category category, Status status, String date) {
        tableModel.addRow(new Object[]{id, title, priority, category, status, date});
    }

    private void showTicketDetails(int row) {
        Long ticketId = (Long) tableModel.getValueAt(row, 0);
        String title = (String) tableModel.getValueAt(row, 1);

        JDialog dialog = new JDialog(mainFrame, "Ticket Details", true);
        dialog.setLayout(new MigLayout("fill, insets 20", "[grow]", "[]20[]20[]"));

        JLabel titleLabel = new JLabel("Ticket #" + ticketId + ": " + title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        dialog.add(titleLabel, "cell 0 0");

        if (mainFrame.getCurrentUser().getRole() == Role.IT_SUPPORT) {
            JPanel statusPanel = new JPanel(new MigLayout("", "[][]", "[]"));
            JComboBox<Status> statusCombo = new JComboBox<>(Status.values());
            JButton updateButton = new JButton("Update Status");

            statusPanel.add(new JLabel("Change Status:"));
            statusPanel.add(statusCombo);
            statusPanel.add(updateButton);

            dialog.add(statusPanel, "cell 0 1");
        }

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        dialog.add(closeButton, "cell 0 2, center");

        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setVisible(true);
    }
}

