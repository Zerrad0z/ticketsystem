package org.example.ui;

import javax.swing.*;
import net.miginfocom.swing.MigLayout;
import org.example.model.*;
import org.example.model.TicketTableModel;
import org.example.service.APIClient;
import org.example.service.DateCellRenderer;
import org.example.service.PriorityCellRenderer;
import org.example.service.StatusCellRenderer;
import org.jdesktop.swingx.JXDatePicker;

import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class TicketSystemClient extends JFrame {
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JPanel loginPanel;
    private JPanel mainPanel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private APIClient apiClient;
    private TicketTableModel ticketTableModel;
    private JTable ticketsTable;
    private UserDTO currentUser;
    private JButton changeStatusButton;
    private JButton addCommentButton;
    private JButton viewAuditLogButton;

    public TicketSystemClient() {
        apiClient = new APIClient("http://localhost:8080/api");
        setupFrame();

        // Initialize card layout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        createLoginPanel();

        contentPanel.add(loginPanel, "login");
        add(contentPanel);
        cardLayout.show(contentPanel, "login");
    }

    private void setupFrame() {
        setTitle("IT Support Ticket System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
    }

    private void createLoginPanel() {
        loginPanel = new JPanel(new MigLayout("fill, insets 20", "[grow]", "[][][]"));

        JLabel titleLabel = new JLabel("IT Support Ticket System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));

        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        JButton loginButton = new JButton("Login");

        loginPanel.add(titleLabel, "cell 0 0, center, gapbottom 30");
        loginPanel.add(new JLabel("Username:"), "cell 0 1");
        loginPanel.add(usernameField, "cell 0 1, growx");
        loginPanel.add(new JLabel("Password:"), "cell 0 2");
        loginPanel.add(passwordField, "cell 0 2, growx");
        loginPanel.add(loginButton, "cell 0 3, center, gaptop 20");

        loginButton.addActionListener(e -> handleLogin());

        KeyAdapter enterKeyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin();
                }
            }
        };
        usernameField.addKeyListener(enterKeyListener);
        passwordField.addKeyListener(enterKeyListener);
    }

    private void createMainPanel() {
        mainPanel = new JPanel(new MigLayout("fill, insets 10", "[grow]", "[][][][][grow]"));

        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new MigLayout("", "[][]", "[]"));
        JButton refreshButton = new JButton("Refresh");
        viewAuditLogButton = new JButton("View Audit Log");
        JButton logoutButton = new JButton("Logout");

        if (currentUser.isItSupport()) {
            // IT Support only gets refresh, audit log, and logout
            buttonsPanel.add(refreshButton);
            buttonsPanel.add(viewAuditLogButton);
            buttonsPanel.add(logoutButton);

            viewAuditLogButton.addActionListener(e -> showAuditLogDialog());
        } else {
            // Regular employees get create ticket, refresh, and logout
            JButton createTicketButton = new JButton("Create New Ticket");
            buttonsPanel.add(createTicketButton);
            buttonsPanel.add(refreshButton);
            buttonsPanel.add(logoutButton);

            createTicketButton.addActionListener(e -> showCreateTicketDialog());
        }

        // Add search panel
        JPanel searchPanel = new JPanel(new MigLayout("", "[][grow][]", "[]"));
        JTextField searchField = new JTextField(20);
        JComboBox<String> searchType = new JComboBox<>(new String[]{"ID", "Title"});
        JButton searchButton = new JButton("Search");

        searchField.setToolTipText("Enter search term");
        searchField.putClientProperty("JTextField.placeholderText", "Search tickets...");
        searchType.setToolTipText("Select search criteria");

        searchPanel.add(searchType);
        searchPanel.add(searchField, "growx");
        searchPanel.add(searchButton);

        // Add search functionality
        ActionListener searchAction = e -> {
            String searchText = searchField.getText().trim();
            if (searchText.isEmpty()) {
                ticketsTable.setRowSorter(null);
                return;
            }

            TableRowSorter<TicketTableModel> sorter = new TableRowSorter<>(ticketTableModel);
            if (searchType.getSelectedItem().equals("ID")) {
                try {
                    Long id = Long.parseLong(searchText);
                    sorter.setRowFilter(RowFilter.numberFilter(
                            RowFilter.ComparisonType.EQUAL, id, 0));
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Please enter a valid ID number");
                    return;
                }
            } else {
                sorter.setRowFilter(RowFilter.regexFilter(
                        "(?i)" + Pattern.quote(searchText), 1));
            }
            ticketsTable.setRowSorter(sorter);
        };

        searchButton.addActionListener(searchAction);
        searchField.addActionListener(searchAction); // Allow search on Enter key

        // Common action listeners
        refreshButton.addActionListener(e -> refreshTickets());
        logoutButton.addActionListener(e -> handleLogout());

        // Add components to main panel in correct order
        mainPanel.add(buttonsPanel, "wrap");
        mainPanel.add(searchPanel, "growx, wrap");  // Add search panel here

        // Add filter panel
        createFilterPanel();

        // Configure and add table
        configureTicketTable();
        mainPanel.add(new JScrollPane(ticketsTable), "grow");

        contentPanel.add(mainPanel, "main");
    }

    private void configureTicketTable() {
        ticketTableModel = new TicketTableModel();
        ticketsTable = new JTable(ticketTableModel);

        // Enable sorting
        TableRowSorter<TicketTableModel> sorter = new TableRowSorter<>(ticketTableModel);
        ticketsTable.setRowSorter(sorter);

        // Configure basic table properties
        ticketsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ticketsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        ticketsTable.getTableHeader().setReorderingAllowed(true);
        ticketsTable.setRowHeight(25);
        ticketsTable.getTableHeader().setToolTipText("Click to sort");

        // Configure columns
        TableColumnModel columnModel = ticketsTable.getColumnModel();

        // Set column widths
        columnModel.getColumn(0).setPreferredWidth(60);   // ID
        columnModel.getColumn(1).setPreferredWidth(200);  // Title
        columnModel.getColumn(2).setPreferredWidth(80);   // Priority
        columnModel.getColumn(3).setPreferredWidth(100);  // Category
        columnModel.getColumn(4).setPreferredWidth(100);  // Status
        columnModel.getColumn(5).setPreferredWidth(120);  // Created Date
        columnModel.getColumn(6).setPreferredWidth(120);  // Last Updated

        // Set custom renderers
        // Priority Column
        columnModel.getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof Priority priority && !isSelected) {
                    setHorizontalAlignment(CENTER);
                    switch (priority) {
                        case HIGH -> setBackground(new Color(255, 200, 200));
                        case MEDIUM -> setBackground(new Color(255, 255, 200));
                        case LOW -> setBackground(new Color(200, 255, 200));
                    }
                    setToolTipText("Priority: " + priority);
                }
                return c;
            }
        });

        // Status Column
        columnModel.getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof Status status && !isSelected) {
                    setHorizontalAlignment(CENTER);
                    switch (status) {
                        case NEW -> setBackground(new Color(200, 200, 255));
                        case IN_PROGRESS -> setBackground(new Color(255, 255, 200));
                        case RESOLVED -> setBackground(new Color(200, 255, 200));
                    }
                    setToolTipText("Status: " + status);
                }
                return c;
            }
        });

        // Date Columns (Created Date and Last Updated)
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        DefaultTableCellRenderer dateRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof LocalDateTime date) {
                    value = date.format(dateFormatter);
                    setToolTipText(date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy HH:mm:ss")));
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        };
        dateRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        columnModel.getColumn(5).setCellRenderer(dateRenderer);
        columnModel.getColumn(6).setCellRenderer(dateRenderer);

        // Title Column (with tooltip)
        columnModel.getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value != null) {
                    setToolTipText(value.toString());
                }
                return c;
            }
        });

        // Add double-click listener
        ticketsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = ticketsTable.getSelectedRow();
                    if (row != -1) {
                        row = ticketsTable.convertRowIndexToModel(row);
                        TicketDTO ticket = ticketTableModel.getTicketAt(row);
                        showTicketDetailsDialog(ticket);
                    }
                }
            }
        });

        // Add keyboard shortcut for opening ticket details (Enter key)
        ticketsTable.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "OpenTicket");
        ticketsTable.getActionMap().put("OpenTicket", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = ticketsTable.getSelectedRow();
                if (row != -1) {
                    row = ticketsTable.convertRowIndexToModel(row);
                    TicketDTO ticket = ticketTableModel.getTicketAt(row);
                    showTicketDetailsDialog(ticket);
                }
            }
        });
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both username and password",
                    "Login Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            LoginRequest loginRequest = new LoginRequest(username, password);
            currentUser = apiClient.login(loginRequest);

            if (currentUser != null) {
                // Clear sensitive data
                usernameField.setText("");
                passwordField.setText("");

                // Create main panel after knowing user role
                createMainPanel();

                // Switch to main panel
                cardLayout.show(contentPanel, "main");
                refreshTickets();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid credentials",
                        "Login Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Login failed: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void handleLogout() {
        currentUser = null;
        ticketTableModel.setTickets(null);
        contentPanel.remove(mainPanel);
        cardLayout.show(contentPanel, "login");
    }

    private void showCreateTicketDialog() {
        JDialog dialog = new JDialog(this, "Create New Ticket", true);
        dialog.setLayout(new MigLayout("fill, insets 20", "[right][grow]", "[][]"));

        JTextField titleField = new JTextField(30);
        JTextArea descriptionArea = new JTextArea(5, 30);
        JComboBox<Priority> priorityCombo = new JComboBox<>(Priority.values());
        JComboBox<Category> categoryCombo = new JComboBox<>(Category.values());

        dialog.add(new JLabel("Title:"), "cell 0 0");
        dialog.add(titleField, "cell 1 0, growx");
        dialog.add(new JLabel("Description:"), "cell 0 1");
        dialog.add(new JScrollPane(descriptionArea), "cell 1 1, growx");
        dialog.add(new JLabel("Priority:"), "cell 0 2");
        dialog.add(priorityCombo, "cell 1 2, growx");
        dialog.add(new JLabel("Category:"), "cell 0 3");
        dialog.add(categoryCombo, "cell 1 3, growx");

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> {
            try {
                if (titleField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please enter a title");
                    return;
                }

                TicketDTO ticket = new TicketDTO();
                ticket.setTitle(titleField.getText().trim());
                ticket.setDescription(descriptionArea.getText().trim());
                ticket.setPriority((Priority) priorityCombo.getSelectedItem());
                ticket.setCategory((Category) categoryCombo.getSelectedItem());

                apiClient.createTicket(ticket, currentUser.getId());
                dialog.dispose();
                refreshTickets();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Failed to create ticket: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(submitButton, "cell 1 4, right, gaptop 10");
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showChangeStatusDialog() {
        int selectedRow = ticketsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a ticket first");
            return;
        }

        selectedRow = ticketsTable.convertRowIndexToModel(selectedRow);
        TicketDTO ticket = ticketTableModel.getTicketAt(selectedRow);

        JDialog dialog = new JDialog(this, "Change Ticket Status", true);
        dialog.setLayout(new MigLayout("fill, insets 20", "[right][grow]", "[][]"));

        JComboBox<Status> statusCombo = new JComboBox<>(Status.values());
        statusCombo.setSelectedItem(ticket.getStatus());

        JButton submitButton = new JButton("Update Status");
        submitButton.addActionListener(e -> {
            try {
                Status newStatus = (Status) statusCombo.getSelectedItem();
                if (newStatus == ticket.getStatus()) {
                    JOptionPane.showMessageDialog(dialog, "Please select a different status");
                    return;
                }

                apiClient.updateTicketStatus(ticket.getId(), newStatus, currentUser.getId());
                dialog.dispose();
                refreshTickets();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Failed to update status: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(new JLabel("New Status:"), "cell 0 0");
        dialog.add(statusCombo, "cell 1 0, growx");
        dialog.add(submitButton, "cell 1 1, right, gaptop 10");

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showAddCommentDialog() {
        int selectedRow = ticketsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a ticket first");
            return;
        }

        selectedRow = ticketsTable.convertRowIndexToModel(selectedRow);
        TicketDTO ticket = ticketTableModel.getTicketAt(selectedRow);

        JDialog dialog = new JDialog(this, "Add Comment", true);
        dialog.setLayout(new MigLayout("fill, insets 20", "[right][grow]", "[][]"));

        JTextArea commentArea = new JTextArea(5, 30);
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);

        JButton submitButton = new JButton("Add Comment");
        submitButton.addActionListener(e -> {
            try {
                String comment = commentArea.getText().trim();
                if (comment.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please enter a comment");
                    return;
                }

                apiClient.addComment(ticket.getId(), comment, currentUser.getId());
                dialog.dispose();
                refreshTickets();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Failed to add comment: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(new JLabel("Comment:"), "cell 0 0");
        dialog.add(new JScrollPane(commentArea), "cell 1 0, grow");
        dialog.add(submitButton, "cell 1 1, right, gaptop 10");

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showTicketDetailsDialog(TicketDTO ticket) {
        JDialog dialog = new JDialog(this, "Ticket Details", true);
        dialog.setLayout(new MigLayout("fill, insets 20", "[right][grow]", "[][]"));

        // Basic ticket information
        dialog.add(new JLabel("ID:"), "cell 0 0");
        dialog.add(new JLabel(ticket.getId().toString()), "cell 1 0");

        dialog.add(new JLabel("Title:"), "cell 0 1");
        dialog.add(new JLabel(ticket.getTitle()), "cell 1 1");

        dialog.add(new JLabel("Description:"), "cell 0 2");
        JTextArea descArea = new JTextArea(ticket.getDescription());
        descArea.setEditable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setPreferredSize(new Dimension(300, 100));
        dialog.add(descScroll, "cell 1 2, grow");

        dialog.add(new JLabel("Priority:"), "cell 0 3");
        JLabel priorityLabel = new JLabel(ticket.getPriority().toString());
        priorityLabel.setForeground(getPriorityColor(ticket.getPriority()));
        dialog.add(priorityLabel, "cell 1 3");

        dialog.add(new JLabel("Category:"), "cell 0 4");
        dialog.add(new JLabel(ticket.getCategory().toString()), "cell 1 4");

        dialog.add(new JLabel("Status:"), "cell 0 5");
        JLabel statusLabel = new JLabel(ticket.getStatus().toString());
        statusLabel.setForeground(getStatusColor(ticket.getStatus()));
        dialog.add(statusLabel, "cell 1 5");

        dialog.add(new JLabel("Created Date:"), "cell 0 6");
        dialog.add(new JLabel(ticket.getCreatedDate().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))), "cell 1 6");

        // Creator information
        dialog.add(new JLabel("Created By:"), "cell 0 7");
        try {
            UserDTO creator = apiClient.getUser(ticket.getCreatedById());
            dialog.add(new JLabel(creator != null ? creator.getUsername() : "Unknown"), "cell 1 7");
        } catch (Exception e) {
            dialog.add(new JLabel("Unknown"), "cell 1 7");
        }

        // Comments section
        if (ticket.getComments() != null && !ticket.getComments().isEmpty()) {
            dialog.add(new JLabel("Comments:"), "cell 0 8");
            JTextArea commentsArea = new JTextArea(10, 30);
            commentsArea.setEditable(false);
            commentsArea.setLineWrap(true);
            commentsArea.setWrapStyleWord(true);

            for (CommentDTO comment : ticket.getComments()) {
                try {
                    UserDTO commentUser = apiClient.getUser(comment.getCreatedById());
                    String username = commentUser != null ? commentUser.getUsername() : "Unknown";
                    commentsArea.append(String.format("%s (%s):\n%s\n\n",
                            username,
                            comment.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                            comment.getContent()));
                } catch (Exception e) {
                    commentsArea.append(String.format("Unknown User (%s):\n%s\n\n",
                            comment.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                            comment.getContent()));
                }
            }
            dialog.add(new JScrollPane(commentsArea), "cell 1 8, grow");
        }

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // Show action buttons only for IT support users
        if (currentUser.isItSupport()) {
            JButton addCommentBtn = new JButton("Add Comment");
            JButton changeStatusBtn = new JButton("Change Status");

            addCommentBtn.addActionListener(e -> {
                dialog.dispose();
                showAddCommentDialog();
            });

            changeStatusBtn.addActionListener(e -> {
                dialog.dispose();
                showChangeStatusDialog();
            });

            buttonPanel.add(addCommentBtn);
            buttonPanel.add(changeStatusBtn);
        }

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeButton);

        dialog.add(buttonPanel, "cell 1 9, right");

        // Configure dialog
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setMinimumSize(new Dimension(500, 400));
        dialog.setVisible(true);
    }

    // Helper methods for colors
    private Color getPriorityColor(Priority priority) {
        return switch (priority) {
            case HIGH -> new Color(200, 0, 0);
            case MEDIUM -> new Color(200, 150, 0);
            case LOW -> new Color(0, 150, 0);
        };
    }

    private Color getStatusColor(Status status) {
        return switch (status) {
            case NEW -> new Color(0, 0, 200);
            case IN_PROGRESS -> new Color(200, 150, 0);
            case RESOLVED -> new Color(0, 150, 0);
        };
    }

    private void showAuditLogDialog() {
        JDialog dialog = new JDialog(this, "Audit Log", true);
        dialog.setLayout(new MigLayout("fill, insets 20", "[grow]", "[][grow][]"));

        // Add filter panel
        JPanel filterPanel = new JPanel(new MigLayout("", "[][]", "[]"));
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");

        filterPanel.add(new JLabel("Filter:"));
        filterPanel.add(searchField);
        filterPanel.add(searchButton);

        // Create table model for audit log
        String[] columns = {"Ticket ID", "Action", "Old Value", "New Value", "Performed By", "Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable auditTable = new JTable(model);
        auditTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Configure column widths
        TableColumnModel columnModel = auditTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(70);  // Ticket ID
        columnModel.getColumn(1).setPreferredWidth(100); // Action
        columnModel.getColumn(2).setPreferredWidth(100); // Old Value
        columnModel.getColumn(3).setPreferredWidth(100); // New Value
        columnModel.getColumn(4).setPreferredWidth(100); // Performed By
        columnModel.getColumn(5).setPreferredWidth(150); // Date

        try {
            List<AuditLogDTO> auditLogs = apiClient.getAuditLogs(currentUser.getId());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            for (AuditLogDTO log : auditLogs) {
                UserDTO user = apiClient.getUser(log.getPerformedById());
                String performedBy = user != null ? user.getUsername() : "Unknown";

                model.addRow(new Object[]{
                        log.getTicketId(),
                        log.getAction(),
                        log.getOldValue(),
                        log.getNewValue(),
                        performedBy,
                        log.getCreatedDate().format(formatter)
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load audit log: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        // Add search functionality
        searchButton.addActionListener(e -> {
            String searchText = searchField.getText().toLowerCase();
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            if (!searchText.trim().isEmpty()) {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
            } else {
                sorter.setRowFilter(null);
            }
            auditTable.setRowSorter(sorter);
        });

        // Add components to dialog
        dialog.add(filterPanel, "wrap");
        dialog.add(new JScrollPane(auditTable), "grow, wrap");

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        dialog.add(closeButton, "right");

        dialog.setSize(800, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    private void refreshTickets() {
        try {
            List<TicketDTO> tickets;
            if (currentUser.isItSupport()) {
                tickets = apiClient.getAllTickets(currentUser.getId());  // Pass the user ID
            } else {
                tickets = apiClient.getUserTickets(currentUser.getId());
            }
            ticketTableModel.setTickets(tickets);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to refresh tickets: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Add this method to TicketSystemClient
    private void createFilterPanel() {
        JPanel filterPanel = new JPanel(new MigLayout("", "[][][][][][]", "[]"));

        // Category filter
        JComboBox<Category> categoryFilter = new JComboBox<>(Category.values());
        categoryFilter.insertItemAt(null, 0);
        categoryFilter.setSelectedIndex(0);
        categoryFilter.setPrototypeDisplayValue(Category.SOFTWARE); // Set width

        // Priority filter
        JComboBox<Priority> priorityFilter = new JComboBox<>(Priority.values());
        priorityFilter.insertItemAt(null, 0);
        priorityFilter.setSelectedIndex(0);
        priorityFilter.setPrototypeDisplayValue(Priority.MEDIUM); // Set width

        // Status filter
        JComboBox<Status> statusFilter = new JComboBox<>(Status.values());
        statusFilter.insertItemAt(null, 0);
        statusFilter.setSelectedIndex(0);
        statusFilter.setPrototypeDisplayValue(Status.IN_PROGRESS); // Set width

        // Date picker for from date
        JXDatePicker fromDatePicker = new JXDatePicker();
        fromDatePicker.setFormats(new SimpleDateFormat("yyyy-MM-dd"));

        // Date picker for to date
        JXDatePicker toDatePicker = new JXDatePicker();
        toDatePicker.setFormats(new SimpleDateFormat("yyyy-MM-dd"));

        // Reset button
        JButton resetButton = new JButton("Reset Filters");

        // Add components to filter panel
        filterPanel.add(new JLabel("Category:"));
        filterPanel.add(categoryFilter);
        filterPanel.add(new JLabel("Priority:"));
        filterPanel.add(priorityFilter);
        filterPanel.add(new JLabel("Status:"));
        filterPanel.add(statusFilter);
        filterPanel.add(new JLabel("From:"));
        filterPanel.add(fromDatePicker);
        filterPanel.add(new JLabel("To:"));
        filterPanel.add(toDatePicker);
        filterPanel.add(resetButton, "gapleft 10");

        // Create filter listener
        ActionListener filterListener = e -> applyFilters(
                (Category) categoryFilter.getSelectedItem(),
                (Priority) priorityFilter.getSelectedItem(),
                (Status) statusFilter.getSelectedItem(),
                fromDatePicker.getDate(),
                toDatePicker.getDate()
        );

        // Add listeners
        categoryFilter.addActionListener(filterListener);
        priorityFilter.addActionListener(filterListener);
        statusFilter.addActionListener(filterListener);
        fromDatePicker.addActionListener(filterListener);
        toDatePicker.addActionListener(filterListener);

        // Reset button listener
        resetButton.addActionListener(e -> {
            categoryFilter.setSelectedIndex(0);
            priorityFilter.setSelectedIndex(0);
            statusFilter.setSelectedIndex(0);
            fromDatePicker.setDate(null);
            toDatePicker.setDate(null);
        });

        // Add filter panel to main panel
        mainPanel.add(filterPanel, "wrap");
    }

    private void applyFilters(Category category, Priority priority, Status status,
                              Date fromDate, Date toDate) {
        TableRowSorter<TicketTableModel> sorter = new TableRowSorter<>(ticketTableModel);

        List<RowFilter<TicketTableModel, Integer>> filters = new ArrayList<>();

        // Category filter
        if (category != null) {
            filters.add(RowFilter.regexFilter(category.toString(), 3));
        }

        // Priority filter
        if (priority != null) {
            filters.add(RowFilter.regexFilter(priority.toString(), 2));
        }

        // Status filter
        if (status != null) {
            filters.add(RowFilter.regexFilter(status.toString(), 4));
        }

        // Date filter
        if (fromDate != null || toDate != null) {
            filters.add(new RowFilter<TicketTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends TicketTableModel, ? extends Integer> entry) {
                    LocalDateTime ticketDate = (LocalDateTime) entry.getValue(5);
                    if (ticketDate == null) return false;

                    if (fromDate != null) {
                        LocalDateTime fromDateTime = fromDate.toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime();
                        if (ticketDate.isBefore(fromDateTime)) return false;
                    }

                    if (toDate != null) {
                        LocalDateTime toDateTime = toDate.toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime()
                                .plusDays(1); // Include the entire last day
                        if (ticketDate.isAfter(toDateTime)) return false;
                    }

                    return true;
                }
            });
        }

        if (!filters.isEmpty()) {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        } else {
            sorter.setRowFilter(null);
        }

        ticketsTable.setRowSorter(sorter);
    }

    private void createSearchPanel() {
        JPanel searchPanel = new JPanel(new MigLayout("", "[][grow][]", "[]"));
        JTextField searchField = new JTextField(20);
        JComboBox<String> searchType = new JComboBox<>(new String[]{"ID", "Title"});
        JButton searchButton = new JButton("Search");

        searchField.setToolTipText("Enter search term");
        searchType.setToolTipText("Select search criteria");
        searchButton.setToolTipText("Click to search");

        // Add search action
        Action searchAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String searchText = searchField.getText().trim();
                if (searchText.isEmpty()) {
                    ticketsTable.setRowSorter(null);
                    return;
                }

                TableRowSorter<TicketTableModel> sorter = new TableRowSorter<>(ticketTableModel);
                if (searchType.getSelectedItem().equals("ID")) {
                    try {
                        Long id = Long.parseLong(searchText);
                        sorter.setRowFilter(RowFilter.numberFilter(
                                RowFilter.ComparisonType.EQUAL, id, 0));
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(TicketSystemClient.this,
                                "Please enter a valid ID number");
                        return;
                    }
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter(
                            "(?i)" + Pattern.quote(searchText), 1));
                }
                ticketsTable.setRowSorter(sorter);
            }
        };

        // Add keyboard shortcut (Ctrl + F for search)
        searchField.getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK),
                "focusSearch"
        );
        searchField.getActionMap().put("focusSearch", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchField.requestFocusInWindow();
            }
        });

        // Add enter key listener
        searchField.addActionListener(searchAction);
        searchButton.addActionListener(searchAction);

        searchPanel.add(searchType);
        searchPanel.add(searchField, "growx");
        searchPanel.add(searchButton);

        // Add to main panel (update createMainPanel method)
        mainPanel.add(searchPanel, "wrap");
    }
}