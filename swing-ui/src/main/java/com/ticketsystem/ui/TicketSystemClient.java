package com.ticketsystem.ui;

import javax.swing.*;

import com.ticketsystem.model.*;
import net.miginfocom.swing.MigLayout;
import com.ticketsystem.service.APIClient;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
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

    // Color scheme
    private final Color PRIMARY_COLOR = new Color(41, 128, 185); // Blue
    private final Color SECONDARY_COLOR = new Color(52, 152, 219); // Lighter blue
    private final Color ACCENT_COLOR = new Color(46, 204, 113); // Green
    private final Color BACKGROUND_COLOR = new Color(245, 245, 245); // Light gray
    private final Color TEXT_COLOR = new Color(52, 73, 94); // Dark blue-gray
    private final Color ERROR_COLOR = new Color(231, 76, 60); // Red

    // Fonts
    private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 28);
    private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 12);

    public TicketSystemClient() {
        apiClient = new APIClient("http://localhost:8080/api");
        setupFrame();

        // Initialize card layout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BACKGROUND_COLOR);

        createLoginPanel();

        contentPanel.add(loginPanel, "login");
        add(contentPanel);
        cardLayout.show(contentPanel, "login");

        // Apply look and feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            System.out.println("Nimbus look and feel not available, using default");
        }
    }

    private void createLoginPanel() {
        loginPanel = new JPanel(new MigLayout("fill, insets 0", "[grow]", "[grow]"));
        loginPanel.setBackground(BACKGROUND_COLOR);

        // Create main content panel with shadow effect
        JPanel loginContentPanel = new JPanel(new MigLayout("fill, insets 40", "[grow]", "[]30[][]30[]"));
        loginContentPanel.setBackground(Color.WHITE);
        loginContentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 0, 20), 1),
                BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));

        // Add logo/image at the top
        ImageIcon logoIcon = createImageIcon("/icons/support_logo.png", "IT Support Logo");
        JLabel logoLabel = new JLabel(logoIcon);
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Create title with custom font and size
        JLabel titleLabel = new JLabel("IT Support Ticket System");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Create input fields with larger size and placeholder text
        usernameField = createStyledTextField("Username", 20);
        passwordField = createStyledPasswordField("Password", 20);

        // Add icon to text fields
        JPanel usernamePanel = new JPanel(new BorderLayout());
        usernamePanel.setBackground(Color.WHITE);
        JLabel userIcon = new JLabel(createImageIcon("/icons/user_icon.png", "User"));
        userIcon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 10));
        usernamePanel.add(userIcon, BorderLayout.WEST);
        usernamePanel.add(usernameField, BorderLayout.CENTER);

        JPanel passwordPanel = new JPanel(new BorderLayout());
        passwordPanel.setBackground(Color.WHITE);
        JLabel lockIcon = new JLabel(createImageIcon("/icons/lock_icon.png", "Lock"));
        lockIcon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 10));
        passwordPanel.add(lockIcon, BorderLayout.WEST);
        passwordPanel.add(passwordField, BorderLayout.CENTER);

        // Create buttons with custom styling
        JButton loginButton = createPrimaryButton("Login");
        JButton registerButton = createLinkButton("Create Account");

        // Add components to content panel
        loginContentPanel.add(logoLabel, "cell 0 0, center");
        loginContentPanel.add(titleLabel, "cell 0 1, center");
        loginContentPanel.add(usernamePanel, "cell 0 2, center, growx, width 300!");
        loginContentPanel.add(passwordPanel, "cell 0 3, center, growx, width 300!");
        loginContentPanel.add(loginButton, "cell 0 4, center, width 300!");
        loginContentPanel.add(registerButton, "cell 0 5, center, gaptop 15");

        // Add content panel to login panel with centering
        loginPanel.add(loginContentPanel, "cell 0 0, width 450!, center, height 550!, center");

        // Add login action
        loginButton.addActionListener(e -> handleLogin());

        // Add register action that opens a dialog
        registerButton.addActionListener(e -> showRegisterDialog());

        // Add enter key listener
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

    private JTextField createStyledTextField(String placeholder, int columns) {
        JTextField field = new JTextField(columns);
        field.setPreferredSize(new Dimension(250, 40));
        field.setFont(REGULAR_FONT);
        field.setForeground(TEXT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, SECONDARY_COLOR),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        field.putClientProperty("JTextField.placeholderText", placeholder);
        return field;
    }

    private JPasswordField createStyledPasswordField(String placeholder, int columns) {
        JPasswordField field = new JPasswordField(columns);
        field.setPreferredSize(new Dimension(250, 40));
        field.setFont(REGULAR_FONT);
        field.setForeground(TEXT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, SECONDARY_COLOR),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        field.putClientProperty("JTextField.placeholderText", placeholder);
        return field;
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(250, 45));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        return button;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(Color.WHITE);
        button.setForeground(PRIMARY_COLOR);
        button.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 2));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(250, 45));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        return button;
    }

    private JButton createLinkButton(String text) {
        JButton button = new JButton(text);
        button.setFont(SMALL_FONT);
        button.setForeground(SECONDARY_COLOR);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        return button;
    }

    private void showRegisterDialog() {
        JDialog dialog = new JDialog(this, "Create New Account", true);
        dialog.setLayout(new MigLayout("fillx, insets 30", "[right][grow]", "[]25[]15[]15[]25[]"));
        dialog.getContentPane().setBackground(Color.WHITE);

        JTextField regUsernameField = createStyledTextField("Username", 20);
        JPasswordField regPasswordField = createStyledPasswordField("Password", 20);
        JComboBox<Role> roleComboBox = new JComboBox<>(Role.values());
        roleComboBox.setPreferredSize(new Dimension(250, 40));
        roleComboBox.setFont(REGULAR_FONT);

        // Style components
        JLabel titleLabel = new JLabel("Create Account");
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);

        // Create labels with custom font
        JLabel usernameLabel = new JLabel("Username:");
        JLabel passwordLabel = new JLabel("Password:");
        JLabel roleLabel = new JLabel("Role:");
        usernameLabel.setFont(REGULAR_FONT);
        passwordLabel.setFont(REGULAR_FONT);
        roleLabel.setFont(REGULAR_FONT);

        // Create register button
        JButton submitButton = createPrimaryButton("Register");
        JButton cancelButton = createSecondaryButton("Cancel");

        cancelButton.addActionListener(e -> dialog.dispose());

        submitButton.addActionListener(e -> {
            try {
                String username = regUsernameField.getText();
                String password = new String(regPasswordField.getPassword());
                Role role = (Role) roleComboBox.getSelectedItem();

                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                            "Please enter both username and password",
                            "Registration Error",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                RegisterRequest request = new RegisterRequest(username, password, role);
                apiClient.register(request);

                // Show success dialog with animation
                showNotification("Registration successful! Please login.", ACCENT_COLOR);
                dialog.dispose();
            } catch (Exception ex) {
                showErrorDialog(dialog, "Registration failed", ex.getMessage());
            }
        });

        // Add components to dialog
        dialog.add(titleLabel, "span 2, center, gapbottom 20");
        dialog.add(usernameLabel, "cell 0 1, right");
        dialog.add(regUsernameField, "cell 1 1, growx");
        dialog.add(passwordLabel, "cell 0 2, right");
        dialog.add(regPasswordField, "cell 1 2, growx");
        dialog.add(roleLabel, "cell 0 3, right");
        dialog.add(roleComboBox, "cell 1 3, growx");

        JPanel buttonPanel = new JPanel(new MigLayout("insets 0", "[grow][grow]", "[]"));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(cancelButton, "cell 0 0, growx");
        buttonPanel.add(submitButton, "cell 1 0, growx");

        dialog.add(buttonPanel, "span 2, growx, gaptop 20");

        dialog.pack();
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showErrorDialog(this, "Login Error", "Please enter both username and password");
            return;
        }

        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            LoginRequest loginRequest = new LoginRequest(username, password);
            UserDTO user = apiClient.login(loginRequest);

            if (user != null) {
                currentUser = user;

                // Clear sensitive data
                usernameField.setText("");
                passwordField.setText("");

                // Create main panel after knowing user role
                createMainPanel();

                // Show welcome notification
                showNotification("Welcome, " + user.getUsername() + "!", ACCENT_COLOR);

                // Switch to main panel
                cardLayout.show(contentPanel, "main");
                refreshTickets();
            } else {
                showErrorDialog(this, "Login Error", "Invalid credentials");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showErrorDialog(this, "Login Error", ex.getMessage());
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

    private void showNotification(String message, Color bgColor) {
        JWindow notification = new JWindow(this);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(bgColor);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JLabel label = new JLabel(message);
        label.setForeground(Color.WHITE);
        label.setFont(REGULAR_FONT);
        panel.add(label, BorderLayout.CENTER);

        notification.setContentPane(panel);
        notification.pack();
        notification.setLocationRelativeTo(this);
        notification.setOpacity(0.9f);

        // Set up animation
        Timer fadeInTimer = new Timer(20, null);
        fadeInTimer.addActionListener(e -> {
            float opacity = notification.getOpacity();
            if (opacity < 0.9f) {
                notification.setOpacity(Math.min(opacity + 0.05f, 0.9f));
            } else {
                fadeInTimer.stop();

                // Set up fade out timer
                Timer timer = new Timer(2000, evt -> {
                    Timer fadeOutTimer = new Timer(20, null);
                    fadeOutTimer.addActionListener(fadeEvt -> {
                        float fadeOpacity = notification.getOpacity();
                        if (fadeOpacity > 0.0f) {
                            notification.setOpacity(Math.max(fadeOpacity - 0.05f, 0.0f));
                        } else {
                            fadeOutTimer.stop();
                            notification.dispose();
                        }
                    });
                    fadeOutTimer.start();
                });
                timer.setRepeats(false);
                timer.start();
            }
        });

        notification.setVisible(true);
        notification.setOpacity(0.0f);
        fadeInTimer.start();
    }

    private void showErrorDialog(Component parent, String title, String message) {
        JDialog errorDialog = new JDialog(this, title, true);
        errorDialog.setLayout(new MigLayout("fill, insets 20", "[center]", "[]20[]"));
        errorDialog.getContentPane().setBackground(Color.WHITE);

        // Error icon
        JLabel iconLabel = new JLabel(UIManager.getIcon("OptionPane.errorIcon"));

        // Message
        JLabel msgLabel = new JLabel("<html><body width='250'>" + message + "</body></html>");
        msgLabel.setFont(REGULAR_FONT);

        // OK button
        JButton okButton = createPrimaryButton("OK");
        okButton.setPreferredSize(new Dimension(100, 40));
        okButton.addActionListener(e -> errorDialog.dispose());

        errorDialog.add(iconLabel, "cell 0 0");
        errorDialog.add(msgLabel, "cell 0 1");
        errorDialog.add(okButton, "cell 0 2, width 100!");

        errorDialog.pack();
        errorDialog.setLocationRelativeTo(parent);
        errorDialog.setVisible(true);
    }
    // Continuing from TicketSystemClient class

    private void createMainPanel() {
        mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(BACKGROUND_COLOR);

        // Create sidebar
        JPanel sidebar = createSidebar();

        // Create main content area
        JPanel contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(BACKGROUND_COLOR);
        contentArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create header panel
        JPanel headerPanel = createHeaderPanel();

        // Create filter and search panel
        JPanel filterSearchPanel = createFilterSearchPanel();

        // Create ticket table panel
        JPanel tablePanel = createTicketTablePanel();

        // Add components to content area
        JPanel mainContentPanel = new JPanel(new BorderLayout(0, 15));
        mainContentPanel.setBackground(BACKGROUND_COLOR);
        mainContentPanel.add(headerPanel, BorderLayout.NORTH);
        mainContentPanel.add(filterSearchPanel, BorderLayout.CENTER);

        contentArea.add(mainContentPanel, BorderLayout.NORTH);
        contentArea.add(tablePanel, BorderLayout.CENTER);

        // Add sidebar and content area to main panel
        mainPanel.add(sidebar, BorderLayout.WEST);
        mainPanel.add(contentArea, BorderLayout.CENTER);

        contentPanel.add(mainPanel, "main");
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBackground(PRIMARY_COLOR);

        // Logo panel at the top
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setBackground(new Color(41, 128, 185));
        logoPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        JLabel appTitle = new JLabel("IT Support Desk");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        appTitle.setForeground(Color.WHITE);
        logoPanel.add(appTitle, BorderLayout.CENTER);

        // User info panel
        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setBackground(new Color(52, 152, 219));
        userPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel userLabel = new JLabel(currentUser.getUsername());
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        userLabel.setForeground(Color.WHITE);

        JLabel roleLabel = new JLabel(currentUser.getRole());
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleLabel.setForeground(new Color(236, 240, 241));

        JPanel userInfoPanel = new JPanel(new MigLayout("insets 0", "[]", "[]0[]"));
        userInfoPanel.setBackground(new Color(52, 152, 219));
        userInfoPanel.add(userLabel, "wrap");
        userInfoPanel.add(roleLabel);

        userPanel.add(userInfoPanel, BorderLayout.CENTER);

        // Menu items
        JPanel menuPanel = new JPanel(new MigLayout("fillx, insets 0", "[grow]", "[]0[]0[]"));
        menuPanel.setBackground(PRIMARY_COLOR);

        JButton dashboardButton = createSidebarButton("Dashboard", "/icons/dashboard.png");
        JButton ticketsButton = createSidebarButton("Tickets", "/icons/ticket.png");

        // Set tickets button as active
        ticketsButton.setBackground(new Color(36, 113, 163));
        ticketsButton.setIcon(createImageIcon("/icons/ticket_white.png", "Tickets"));

        JButton logoutButton = createSidebarButton("Logout", "/icons/logout.png");

        menuPanel.add(dashboardButton, "growx, wrap");
        menuPanel.add(ticketsButton, "growx, wrap");

        // If IT Support, add Reports button
        if (currentUser.isItSupport()) {
            JButton reportsButton = createSidebarButton("Audit Logs", "/icons/reports.png");
            menuPanel.add(reportsButton, "growx, wrap");

            reportsButton.addActionListener(e -> showAuditLogDialog());
        }

        menuPanel.add(logoutButton, "growx, wrap");

        // Add action to logout button
        logoutButton.addActionListener(e -> handleLogout());

        // Create the main sidebar content panel
        JPanel sidebarContent = new JPanel(new BorderLayout());
        sidebarContent.setBackground(PRIMARY_COLOR);
        sidebarContent.add(userPanel, BorderLayout.NORTH);
        sidebarContent.add(menuPanel, BorderLayout.CENTER);

        // Add logo and content to sidebar
        sidebar.add(logoPanel, BorderLayout.NORTH);
        sidebar.add(sidebarContent, BorderLayout.CENTER);

        return sidebar;
    }

    private JButton createSidebarButton(String text, String iconPath) {
        JButton button = new JButton(text);
        button.setFont(REGULAR_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY_COLOR);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIcon(createImageIcon(iconPath, text));
        button.setIconTextGap(10);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(200, 45));

        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.getBackground().equals(PRIMARY_COLOR)) {
                    button.setBackground(new Color(36, 113, 163));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!text.equals("Tickets")) {  // Keep Tickets button highlighted
                    button.setBackground(PRIMARY_COLOR);
                }
            }
        });

        return button;
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 218, 218), 1),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel titleLabel = new JLabel("Ticket Management");
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(TEXT_COLOR);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setBackground(Color.WHITE);

        // Add action buttons based on role
        if (!currentUser.isItSupport()) {
            JButton createTicketButton = new JButton("Create Ticket");
            createTicketButton.setFont(BUTTON_FONT);
            createTicketButton.setBackground(ACCENT_COLOR);
            createTicketButton.setForeground(Color.WHITE);
            createTicketButton.setBorderPainted(false);
            createTicketButton.setFocusPainted(false);
            createTicketButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            createTicketButton.setIcon(createImageIcon("/icons/add_ticket.png", "Create"));
            createTicketButton.setIconTextGap(10);

            createTicketButton.addActionListener(e -> showCreateTicketDialog());

            actionPanel.add(createTicketButton);
        } else {
            // Refresh button for IT support
            JButton refreshButton = new JButton("Refresh");
            refreshButton.setFont(BUTTON_FONT);
            refreshButton.setBackground(SECONDARY_COLOR);
            refreshButton.setForeground(Color.WHITE);
            refreshButton.setBorderPainted(false);
            refreshButton.setFocusPainted(false);
            refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            refreshButton.setIcon(createImageIcon("/icons/refresh.png", "Refresh"));
            refreshButton.setIconTextGap(10);

            refreshButton.addActionListener(e -> refreshTickets());

            actionPanel.add(refreshButton);
        }

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(actionPanel, BorderLayout.EAST);

        return headerPanel;
    }

    // Replace your existing createFilterSearchPanel method with this
    private JPanel createFilterSearchPanel() {
        JPanel filterSearchPanel = new JPanel(new MigLayout("fillx, insets 0", "[grow]", "[]"));
        filterSearchPanel.setBackground(BACKGROUND_COLOR);

        // Enhanced search panel that includes both search and filters
        JPanel enhancedSearchPanel = createEnhancedSearchPanel();

        // Add to filter search panel
        filterSearchPanel.add(enhancedSearchPanel, "cell 0 0, growx");

        return filterSearchPanel;
    }

    // Add this method to fix the missing icon issue
    private void fixMissingIcons() {
        // This method creates a helper class to handle missing icons
        // Call this in your constructor after initializing apiClient

        // Fix ticket_icon.png for frame icon
        if (getIconImage().getWidth(null) <= 0) {
            setIconImage(createDefaultIcon(16, 16, PRIMARY_COLOR));
        }
    }

    // Helper method to create default icons when image files aren't found
    private Image createDefaultIcon(int width, int height, Color color) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();
        return img;
    }

    // Update your createImageIcon method to provide fallback icons
    private ImageIcon createImageIcon(String path, String description) {
        java.net.URL imgURL = getClass().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            // Create a simple colored square as fallback
            int size = 16;
            Color iconColor = PRIMARY_COLOR;

            // Use different colors based on icon type
            if (path.contains("user")) {
                iconColor = new Color(52, 152, 219);
                size = 20;
            } else if (path.contains("lock")) {
                iconColor = new Color(231, 76, 60);
                size = 20;
            } else if (path.contains("search")) {
                iconColor = new Color(149, 165, 166);
            } else if (path.contains("clear")) {
                iconColor = new Color(189, 195, 199);
            } else if (path.contains("add")) {
                iconColor = ACCENT_COLOR;
            }

            return new ImageIcon(createDefaultIcon(size, size, iconColor), description);
        }
    }

    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new MigLayout("", "[][90][]90[]90[]", "[]"));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 218, 218), 1),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));

        // Category filter
        JComboBox<String> categoryFilter = new JComboBox<>(new String[]{"All Categories",
                "NETWORK", "HARDWARE", "SOFTWARE", "OTHER"});
        categoryFilter.setFont(SMALL_FONT);

        // Priority filter
        JComboBox<String> priorityFilter = new JComboBox<>(new String[]{"All Priorities",
                "LOW", "MEDIUM", "HIGH"});
        priorityFilter.setFont(SMALL_FONT);

        // Status filter
        JComboBox<String> statusFilter = new JComboBox<>(new String[]{"All Statuses",
                "NEW", "IN_PROGRESS", "RESOLVED"});
        statusFilter.setFont(SMALL_FONT);

        // Reset button
        JButton resetButton = new JButton("Reset");
        resetButton.setFont(SMALL_FONT);
        resetButton.setBackground(Color.WHITE);
        resetButton.setBorder(BorderFactory.createLineBorder(SECONDARY_COLOR));
        resetButton.setForeground(SECONDARY_COLOR);
        resetButton.setFocusPainted(false);
        resetButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add components to filter panel
        filterPanel.add(new JLabel("Filters:"), "gapright 5");
        filterPanel.add(categoryFilter, "growx");
        filterPanel.add(new JLabel("Priority:"), "gapright 5");
        filterPanel.add(priorityFilter, "growx");
        filterPanel.add(new JLabel("Status:"), "gapright 5");
        filterPanel.add(statusFilter, "growx");
        filterPanel.add(resetButton, "gapleft 10");

        // Create filter listener
        ActionListener filterListener = e -> {
            String categoryStr = (String) categoryFilter.getSelectedItem();
            String priorityStr = (String) priorityFilter.getSelectedItem();
            String statusStr = (String) statusFilter.getSelectedItem();

            Category category = categoryStr.equals("All Categories") ? null :
                    Category.valueOf(categoryStr);
            Priority priority = priorityStr.equals("All Priorities") ? null :
                    Priority.valueOf(priorityStr);
            Status status = statusStr.equals("All Statuses") ? null :
                    Status.valueOf(statusStr);

            applyFilters(category, priority, status, null, null);
        };

        // Add listeners
        categoryFilter.addActionListener(filterListener);
        priorityFilter.addActionListener(filterListener);
        statusFilter.addActionListener(filterListener);

        // Reset button listener
        resetButton.addActionListener(e -> {
            categoryFilter.setSelectedIndex(0);
            priorityFilter.setSelectedIndex(0);
            statusFilter.setSelectedIndex(0);
            ticketsTable.setRowSorter(null);
        });

        return filterPanel;
    }

    private JPanel createTicketTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 218, 218), 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        // Configure and create table
        configureTicketTable();

        JScrollPane scrollPane = new JScrollPane(ticketsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Add action panel at the bottom if IT Support
        if (currentUser.isItSupport()) {
            JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            actionPanel.setBackground(Color.WHITE);
            actionPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(218, 218, 218)));

            changeStatusButton = new JButton("Change Status");
            changeStatusButton.setFont(REGULAR_FONT);
            changeStatusButton.setBackground(SECONDARY_COLOR);
            changeStatusButton.setForeground(Color.WHITE);
            changeStatusButton.setBorderPainted(false);
            changeStatusButton.setFocusPainted(false);
            changeStatusButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            changeStatusButton.setEnabled(false);

            addCommentButton = new JButton("Add Comment");
            addCommentButton.setFont(REGULAR_FONT);
            addCommentButton.setBackground(ACCENT_COLOR);
            addCommentButton.setForeground(Color.WHITE);
            addCommentButton.setBorderPainted(false);
            addCommentButton.setFocusPainted(false);
            addCommentButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            addCommentButton.setEnabled(false);

            // Add action listeners
            changeStatusButton.addActionListener(e -> showChangeStatusDialog());
            addCommentButton.addActionListener(e -> showAddCommentDialog());

            // Enable/disable buttons based on selection
            ticketsTable.getSelectionModel().addListSelectionListener(e -> {
                boolean hasSelection = ticketsTable.getSelectedRow() != -1;
                changeStatusButton.setEnabled(hasSelection);
                addCommentButton.setEnabled(hasSelection);
            });

            actionPanel.add(changeStatusButton);
            actionPanel.add(addCommentButton);

            tablePanel.add(actionPanel, BorderLayout.SOUTH);
        }

        return tablePanel;
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
        ticketsTable.setRowHeight(40);
        ticketsTable.setIntercellSpacing(new Dimension(10, 5));
        ticketsTable.setShowGrid(false);
        ticketsTable.setGridColor(new Color(240, 240, 240));

        // Style the header
        JTableHeader header = ticketsTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(245, 245, 245));
        header.setForeground(TEXT_COLOR);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(218, 218, 218)));
        header.setPreferredSize(new Dimension(0, 40));

        // Configure columns
        TableColumnModel columnModel = ticketsTable.getColumnModel();

        // Set column widths
        columnModel.getColumn(0).setPreferredWidth(60);   // ID
        columnModel.getColumn(1).setPreferredWidth(250);  // Title
        columnModel.getColumn(2).setPreferredWidth(100);  // Priority
        columnModel.getColumn(3).setPreferredWidth(100);  // Category
        columnModel.getColumn(4).setPreferredWidth(120);  // Status
        columnModel.getColumn(5).setPreferredWidth(140);  // Created Date
        columnModel.getColumn(6).setPreferredWidth(140);  // Last Updated

        // Set custom renderers
        // Priority Column
        columnModel.getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof Priority priority && !isSelected) {
                    setHorizontalAlignment(CENTER);
                    JPanel panel = new JPanel(new BorderLayout());
                    panel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

                    JLabel label = new JLabel(priority.toString());
                    label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    label.setHorizontalAlignment(SwingConstants.CENTER);

                    switch (priority) {
                        case HIGH -> {
                            panel.setBackground(new Color(255, 236, 236));
                            label.setForeground(new Color(220, 53, 69));
                        }
                        case MEDIUM -> {
                            panel.setBackground(new Color(255, 248, 230));
                            label.setForeground(new Color(255, 153, 0));
                        }
                        case LOW -> {
                            panel.setBackground(new Color(232, 250, 240));
                            label.setForeground(new Color(40, 167, 69));
                        }
                    }

                    panel.add(label, BorderLayout.CENTER);
                    return panel;
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
                    JPanel panel = new JPanel(new BorderLayout());
                    panel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

                    JLabel label = new JLabel(status.toString().replace("_", " "));
                    label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    label.setHorizontalAlignment(SwingConstants.CENTER);

                    switch (status) {
                        case NEW -> {
                            panel.setBackground(new Color(232, 244, 253));
                            label.setForeground(new Color(23, 162, 184));
                        }
                        case IN_PROGRESS -> {
                            panel.setBackground(new Color(255, 243, 205));
                            label.setForeground(new Color(255, 153, 0));
                        }
                        case RESOLVED -> {
                            panel.setBackground(new Color(212, 237, 218));
                            label.setForeground(new Color(40, 167, 69));
                        }
                    }

                    panel.add(label, BorderLayout.CENTER);
                    return panel;
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

        // Add alternating row colors
        ticketsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 249, 249));
                }
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
                        BorderFactory.createEmptyBorder(0, 10, 0, 10)
                ));
                return c;
            }
        });
    }

    private void showTicketDetailsDialog(TicketDTO ticket) {
        JDialog dialog = new JDialog(this, "Ticket Details", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        // Create header panel with ticket ID and title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel idLabel = new JLabel("#" + ticket.getId());
        idLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        idLabel.setForeground(Color.WHITE);

        JLabel titleLabel = new JLabel(ticket.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        headerPanel.add(idLabel, BorderLayout.NORTH);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Create content panel
        JPanel contentPanel = new JPanel(new MigLayout("fillx, insets 25", "[120, right][grow]", "[][][][][][]"));
        contentPanel.setBackground(Color.WHITE);

        // Description
        contentPanel.add(createBoldLabel("Description:"), "cell 0 0, alignx right, aligny top");

        JTextArea descArea = new JTextArea(ticket.getDescription());
        descArea.setEditable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setFont(REGULAR_FONT);
        descArea.setBackground(new Color(248, 249, 250));
        descArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 226, 230)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setPreferredSize(new Dimension(300, 100));
        descScroll.setBorder(BorderFactory.createEmptyBorder());

        contentPanel.add(descScroll, "cell 1 0, growx, wrap");

        // Priority with styled label
        contentPanel.add(createBoldLabel("Priority:"), "cell 0 1");
        JLabel priorityLabel = createStyledLabel(ticket.getPriority().toString(), getPriorityColor(ticket.getPriority()));
        contentPanel.add(priorityLabel, "cell 1 1, wrap");

        // Category
        contentPanel.add(createBoldLabel("Category:"), "cell 0 2");
        contentPanel.add(new JLabel(ticket.getCategory().toString()), "cell 1 2, wrap");

        // Status with styled label
        contentPanel.add(createBoldLabel("Status:"), "cell 0 3");
        JLabel statusLabel = createStyledLabel(ticket.getStatus().toString(), getStatusColor(ticket.getStatus()));
        contentPanel.add(statusLabel, "cell 1 3, wrap");

        // Created date and by
        contentPanel.add(createBoldLabel("Created:"), "cell 0 4");

        String createdDateStr = ticket.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        // Get creator username
        String creatorUsername = "Unknown";
        try {
            UserDTO creator = apiClient.getUser(ticket.getCreatedById());
            if (creator != null) {
                creatorUsername = creator.getUsername();
            }
        } catch (Exception e) {
            // Handle silently
        }

        contentPanel.add(new JLabel(createdDateStr + " by " + creatorUsername), "cell 1 4, wrap");

        // Last updated
        if (ticket.getLastUpdated() != null) {
            contentPanel.add(createBoldLabel("Last Updated:"), "cell 0 5");
            String lastUpdatedStr = ticket.getLastUpdated().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            contentPanel.add(new JLabel(lastUpdatedStr), "cell 1 5, wrap");
        }

        // Comments section
        JPanel commentsPanel = new JPanel(new BorderLayout());
        commentsPanel.setBackground(Color.WHITE);
        commentsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(222, 226, 230)),
                "Comments",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                TEXT_COLOR
        ));

        JPanel commentsContentPanel = new JPanel();
        commentsContentPanel.setLayout(new BoxLayout(commentsContentPanel, BoxLayout.Y_AXIS));
        commentsContentPanel.setBackground(Color.WHITE);

        if (ticket.getComments() != null && !ticket.getComments().isEmpty()) {
            for (CommentDTO comment : ticket.getComments()) {
                // Create comment panel
                JPanel commentPanel = createCommentPanel(comment);
                commentsContentPanel.add(commentPanel);
                commentsContentPanel.add(Box.createVerticalStrut(10)); // Add spacing
            }
        } else {
            JLabel noCommentsLabel = new JLabel("No comments yet");
            noCommentsLabel.setFont(REGULAR_FONT);
            noCommentsLabel.setForeground(new Color(108, 117, 125));
            noCommentsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            noCommentsLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

            commentsContentPanel.add(Box.createVerticalGlue());
            commentsContentPanel.add(noCommentsLabel);
            commentsContentPanel.add(Box.createVerticalGlue());
        }

        JScrollPane commentsScroll = new JScrollPane(commentsContentPanel);
        commentsScroll.setBorder(BorderFactory.createEmptyBorder());
        commentsScroll.setPreferredSize(new Dimension(400, 150));

        commentsPanel.add(commentsScroll, BorderLayout.CENTER);

        // Create buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        buttonPanel.setBackground(new Color(248, 249, 250));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(222, 226, 230)));

        // Show action buttons only for IT support users
        if (currentUser.isItSupport()) {
            JButton addCommentBtn = createPrimaryButton("Add Comment");
            JButton changeStatusBtn = createSecondaryButton("Change Status");

            addCommentBtn.setPreferredSize(new Dimension(150, 40));
            changeStatusBtn.setPreferredSize(new Dimension(150, 40));

            addCommentBtn.addActionListener(e -> {
                dialog.dispose();
                showAddCommentDialog();
            });

            changeStatusBtn.addActionListener(e -> {
                dialog.dispose();
                showChangeStatusDialog();
            });

            buttonPanel.add(changeStatusBtn);
            buttonPanel.add(addCommentBtn);
        }

        JButton closeButton = new JButton("Close");
        closeButton.setFont(BUTTON_FONT);
        closeButton.setPreferredSize(new Dimension(100, 40));
        closeButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeButton);

        // Add all panels to dialog
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(commentsPanel, BorderLayout.SOUTH);

        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Configure dialog
        dialog.setSize(600, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JPanel createCommentPanel(CommentDTO comment) {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 226, 230)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        // Get username
        String username = "Unknown";
        try {
            UserDTO commentUser = apiClient.getUser(comment.getCreatedById());
            if (commentUser != null) {
                username = commentUser.getUsername();
            }
        } catch (Exception e) {
            // Handle silently
        }

        // Header with username and date
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(248, 249, 250));

        JLabel nameLabel = new JLabel(username);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(TEXT_COLOR);

        String dateStr = comment.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        JLabel dateLabel = new JLabel(dateStr);
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLabel.setForeground(new Color(108, 117, 125));

        headerPanel.add(nameLabel, BorderLayout.WEST);
        headerPanel.add(dateLabel, BorderLayout.EAST);

        // Comment content
        JTextArea contentArea = new JTextArea(comment.getContent());
        contentArea.setFont(REGULAR_FONT);
        contentArea.setEditable(false);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setBackground(new Color(248, 249, 250));
        contentArea.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(contentArea, BorderLayout.CENTER);

        return panel;
    }

    private JLabel createBoldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(TEXT_COLOR);
        return label;
    }

    private JLabel createStyledLabel(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(Color.WHITE);
        label.setOpaque(true);
        label.setBackground(color);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        return label;
    }

    private void showChangeStatusDialog() {
        int selectedRow = ticketsTable.getSelectedRow();
        if (selectedRow == -1) {
            showErrorDialog(this, "Error", "Please select a ticket first");
            return;
        }

        selectedRow = ticketsTable.convertRowIndexToModel(selectedRow);
        TicketDTO ticket = ticketTableModel.getTicketAt(selectedRow);

        JDialog dialog = new JDialog(this, "Change Ticket Status", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JLabel titleLabel = new JLabel("Update Ticket #" + ticket.getId() + " Status");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Content panel
        JPanel contentPanel = new JPanel(new MigLayout("fillx, insets 25", "[right][grow]", "[][]"));
        contentPanel.setBackground(Color.WHITE);

        JLabel currentStatusLabel = new JLabel("Current Status:");
        currentStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel statusValueLabel = createStyledLabel(ticket.getStatus().toString(), getStatusColor(ticket.getStatus()));

        JLabel newStatusLabel = new JLabel("New Status:");
        newStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JComboBox<Status> statusCombo = new JComboBox<>(Status.values());
        statusCombo.setSelectedItem(ticket.getStatus());
        statusCombo.setFont(REGULAR_FONT);
        statusCombo.setPreferredSize(new Dimension(200, 35));

        contentPanel.add(currentStatusLabel, "cell 0 0");
        contentPanel.add(statusValueLabel, "cell 1 0");
        contentPanel.add(newStatusLabel, "cell 0 1");
        contentPanel.add(statusCombo, "cell 1 1");

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        buttonPanel.setBackground(new Color(248, 249, 250));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(222, 226, 230)));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(BUTTON_FONT);
        cancelButton.setPreferredSize(new Dimension(100, 40));
        cancelButton.addActionListener(e -> dialog.dispose());

        JButton updateButton = createPrimaryButton("Update");
        updateButton.setPreferredSize(new Dimension(100, 40));

        updateButton.addActionListener(e -> {
            try {
                Status newStatus = (Status) statusCombo.getSelectedItem();
                if (newStatus == ticket.getStatus()) {
                    showErrorDialog(dialog, "Validation Error", "Please select a different status");
                    return;
                }

                apiClient.updateTicketStatus(ticket.getId(), newStatus, currentUser.getId());
                showNotification("Status updated successfully", ACCENT_COLOR);
                dialog.dispose();
                refreshTickets();
            } catch (Exception ex) {
                showErrorDialog(dialog, "Error", "Failed to update status: " + ex.getMessage());
            }
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(updateButton);

        // Add panels to dialog
        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showAddCommentDialog() {
        int selectedRow = ticketsTable.getSelectedRow();
        if (selectedRow == -1) {
            showErrorDialog(this, "Error", "Please select a ticket first");
            return;
        }

        selectedRow = ticketsTable.convertRowIndexToModel(selectedRow);
        TicketDTO ticket = ticketTableModel.getTicketAt(selectedRow);

        JDialog dialog = new JDialog(this, "Add Comment", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JLabel titleLabel = new JLabel("Add Comment to Ticket #" + ticket.getId());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Content panel
        JPanel contentPanel = new JPanel(new MigLayout("fillx, insets 25", "[grow]", "[][]"));
        contentPanel.setBackground(Color.WHITE);

        JLabel commentLabel = new JLabel("Your Comment:");
        commentLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JTextArea commentArea = new JTextArea(6, 30);
        commentArea.setFont(REGULAR_FONT);
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        commentArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 226, 230)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        JScrollPane commentScroll = new JScrollPane(commentArea);
        commentScroll.setBorder(BorderFactory.createEmptyBorder());

        contentPanel.add(commentLabel, "wrap");
        contentPanel.add(commentScroll, "grow");

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        buttonPanel.setBackground(new Color(248, 249, 250));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(222, 226, 230)));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(BUTTON_FONT);
        cancelButton.setPreferredSize(new Dimension(100, 40));
        cancelButton.addActionListener(e -> dialog.dispose());

        JButton submitButton = createPrimaryButton("Submit");
        submitButton.setPreferredSize(new Dimension(100, 40));

        submitButton.addActionListener(e -> {
            try {
                String comment = commentArea.getText().trim();
                if (comment.isEmpty()) {
                    showErrorDialog(dialog, "Validation Error", "Please enter a comment");
                    return;
                }

                apiClient.addComment(ticket.getId(), comment, currentUser.getId());
                showNotification("Comment added successfully", ACCENT_COLOR);
                dialog.dispose();
                refreshTickets();
            } catch (Exception ex) {
                showErrorDialog(dialog, "Error", "Failed to add comment: " + ex.getMessage());
            }
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(submitButton);

        // Add panels to dialog
        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setSize(500, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showAuditLogDialog() {
        JDialog dialog = new JDialog(this, "Audit Log", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JLabel titleLabel = new JLabel("System Audit Log");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Filter panel
        JPanel filterPanel = new JPanel(new MigLayout("", "[][]", "[]"));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(222, 226, 230)),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JTextField searchField = createStyledTextField("Filter by ticket ID or user", 20);
        searchField.setPreferredSize(new Dimension(250, 35));

        JButton searchButton = new JButton("Filter");
        searchButton.setFont(BUTTON_FONT);
        searchButton.setBackground(SECONDARY_COLOR);
        searchButton.setForeground(Color.WHITE);
        searchButton.setBorderPainted(false);
        searchButton.setFocusPainted(false);
        searchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        filterPanel.add(new JLabel("Search:"), "gapright 10");
        filterPanel.add(searchField, "width 250!");
        filterPanel.add(searchButton, "gapleft 10");

        // Create table model for audit log
        String[] columns = {"Ticket ID", "Action", "Old Value", "New Value", "Performed By", "Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable auditTable = new JTable(model);
        styleTable(auditTable);

        // Configure column widths
        TableColumnModel columnModel = auditTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(70);  // Ticket ID
        columnModel.getColumn(1).setPreferredWidth(120); // Action
        columnModel.getColumn(2).setPreferredWidth(100); // Old Value
        columnModel.getColumn(3).setPreferredWidth(100); // New Value
        columnModel.getColumn(4).setPreferredWidth(100); // Performed By
        columnModel.getColumn(5).setPreferredWidth(150); // Date

        JScrollPane tableScroll = new JScrollPane(auditTable);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        tableScroll.getViewport().setBackground(Color.WHITE);

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
            showErrorDialog(dialog, "Error", "Failed to load audit log: " + e.getMessage());
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

        // Enter key in search field should trigger search
        searchField.addActionListener(e -> searchButton.doClick());

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        buttonPanel.setBackground(new Color(248, 249, 250));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(222, 226, 230)));

        JButton closeButton = new JButton("Close");
        closeButton.setFont(BUTTON_FONT);
        closeButton.setPreferredSize(new Dimension(100, 40));
        closeButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(closeButton);

        // Add components to dialog
        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.add(filterPanel, BorderLayout.CENTER);
        dialog.add(tableScroll, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void styleTable(JTable table) {
        // Configure basic table properties
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.getTableHeader().setReorderingAllowed(true);
        table.setRowHeight(35);
        table.setIntercellSpacing(new Dimension(10, 5));
        table.setShowGrid(false);
        table.setGridColor(new Color(240, 240, 240));

        // Style the header
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(245, 245, 245));
        header.setForeground(TEXT_COLOR);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(218, 218, 218)));
        header.setPreferredSize(new Dimension(0, 40));

        // Add alternating row colors
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 249, 249));
                }
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
                        BorderFactory.createEmptyBorder(0, 10, 0, 10)
                ));
                return c;
            }
        });
    }

    private void refreshTickets() {
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            List<TicketDTO> tickets;
            if (currentUser.isItSupport()) {
                tickets = apiClient.getAllTickets(currentUser.getId());
            } else {
                tickets = apiClient.getUserTickets(currentUser.getId());
            }
            ticketTableModel.setTickets(tickets);

            // Show success notification
            if (tickets != null) {
                showNotification("Loaded " + tickets.size() + " tickets", SECONDARY_COLOR);
            }
        } catch (Exception ex) {
            showErrorDialog(this, "Error", "Failed to refresh tickets: " + ex.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
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

    // Helper methods for colors
    private Color getPriorityColor(Priority priority) {
        return switch (priority) {
            case HIGH -> new Color(220, 53, 69);
            case MEDIUM -> new Color(255, 153, 0);
            case LOW -> new Color(40, 167, 69);
        };
    }

    private Color getStatusColor(Status status) {
        return switch (status) {
            case NEW -> new Color(23, 162, 184);
            case IN_PROGRESS -> new Color(255, 153, 0);
            case RESOLVED -> new Color(40, 167, 69);
        };
    }
    private void showCreateTicketDialog() {
        JDialog dialog = new JDialog(this, "Create New Ticket", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel titleLabel = new JLabel("Create New Ticket");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Form panel
        JPanel formPanel = new JPanel(new MigLayout("fillx, insets 20", "[100, right][grow]", "[][][][]"));
        formPanel.setBackground(Color.WHITE);

        JTextField titleField = createStyledTextField("Enter ticket title", 30);

        JTextArea descriptionArea = new JTextArea(5, 30);
        descriptionArea.setFont(REGULAR_FONT);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 226, 230)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setBorder(BorderFactory.createEmptyBorder());

        JComboBox<Priority> priorityCombo = new JComboBox<>(Priority.values());
        priorityCombo.setFont(REGULAR_FONT);
        priorityCombo.setPreferredSize(new Dimension(200, 35));

        JComboBox<Category> categoryCombo = new JComboBox<>(Category.values());
        categoryCombo.setFont(REGULAR_FONT);
        categoryCombo.setPreferredSize(new Dimension(200, 35));

        // Add form elements
        formPanel.add(createBoldLabel("Title:"), "cell 0 0, alignx right");
        formPanel.add(titleField, "cell 1 0, growx, wrap");

        formPanel.add(createBoldLabel("Description:"), "cell 0 1, alignx right, aligny top");
        formPanel.add(descScroll, "cell 1 1, growx, wrap");

        formPanel.add(createBoldLabel("Priority:"), "cell 0 2, alignx right");
        formPanel.add(priorityCombo, "cell 1 2, wrap");

        formPanel.add(createBoldLabel("Category:"), "cell 0 3, alignx right");
        formPanel.add(categoryCombo, "cell 1 3, wrap");

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        buttonPanel.setBackground(new Color(248, 249, 250));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(222, 226, 230)));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(BUTTON_FONT);
        cancelButton.setPreferredSize(new Dimension(100, 40));
        cancelButton.addActionListener(e -> dialog.dispose());

        JButton submitButton = createPrimaryButton("Submit");
        submitButton.setPreferredSize(new Dimension(100, 40));

        submitButton.addActionListener(e -> {
            try {
                if (titleField.getText().trim().isEmpty()) {
                    showErrorDialog(dialog, "Validation Error", "Please enter a title for the ticket");
                    return;
                }

                TicketDTO ticket = new TicketDTO();
                ticket.setTitle(titleField.getText().trim());
                ticket.setDescription(descriptionArea.getText().trim());
                ticket.setPriority((Priority) priorityCombo.getSelectedItem());
                ticket.setCategory((Category) categoryCombo.getSelectedItem());

                apiClient.createTicket(ticket, currentUser.getId());
                showNotification("Ticket created successfully", ACCENT_COLOR);
                dialog.dispose();
                refreshTickets();
            } catch (Exception ex) {
                showErrorDialog(dialog, "Error", "Failed to create ticket: " + ex.getMessage());
            }
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(submitButton);

        // Add all panels to dialog
        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JPanel createEnhancedSearchPanel() {
        // Use only standard MigLayout constraints - no 'auto'
        JPanel searchPanel = new JPanel(new MigLayout("fillx, insets 0", "[grow][]", "[]"));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 218, 218), 1),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        // Create combo box for search type
        String[] searchOptions = {"ID", "Title", "Status", "Priority", "Category"};
        JComboBox<String> searchTypeCombo = new JComboBox<>(searchOptions);
        searchTypeCombo.setFont(REGULAR_FONT);
        searchTypeCombo.setPreferredSize(new Dimension(120, 40));

        // Simple search field
        JTextField searchField = createStyledTextField("Search tickets...", 20);
        searchField.setPreferredSize(new Dimension(0, 40));

        // Search button
        JButton searchButton = new JButton("Search");
        searchButton.setFont(BUTTON_FONT);
        searchButton.setBackground(PRIMARY_COLOR);
        searchButton.setForeground(Color.WHITE);
        searchButton.setBorderPainted(false);
        searchButton.setFocusPainted(false);
        searchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        searchButton.setPreferredSize(new Dimension(100, 40));

        // Create search action
        ActionListener searchAction = e -> {
            String searchText = searchField.getText().trim();
            String searchType = (String) searchTypeCombo.getSelectedItem();

            if (searchText.isEmpty()) {
                ticketsTable.setRowSorter(null);
                return;
            }

            TableRowSorter<TicketTableModel> sorter = new TableRowSorter<>(ticketTableModel);

            switch (searchType) {
                case "ID":
                    try {
                        Long id = Long.parseLong(searchText);
                        sorter.setRowFilter(RowFilter.numberFilter(
                                RowFilter.ComparisonType.EQUAL, id, 0));
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null,
                                "Please enter a valid ID number",
                                "Search Error",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    break;
                case "Title":
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(searchText), 1));
                    break;
                case "Status":
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(searchText), 4));
                    break;
                case "Priority":
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(searchText), 2));
                    break;
                case "Category":
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(searchText), 3));
                    break;
            }

            ticketsTable.setRowSorter(sorter);

            // Show search results count
            int filteredRowCount = ticketsTable.getRowCount();
            showNotification("Found " + filteredRowCount + " matching tickets", SECONDARY_COLOR);
        };

        // Add action listeners
        searchButton.addActionListener(searchAction);
        searchField.addActionListener(searchAction); // Trigger search on Enter key

        // Add elements to search panel with simple layout
        JPanel searchFieldPanel = new JPanel(new BorderLayout());
        searchFieldPanel.setBackground(Color.WHITE);
        searchFieldPanel.add(searchField, BorderLayout.CENTER);

        JPanel topRow = new JPanel(new MigLayout("insets 0", "[][grow][]", "[]"));
        topRow.setBackground(Color.WHITE);
        topRow.add(searchTypeCombo, "cell 0 0");
        topRow.add(searchFieldPanel, "cell 1 0, growx");
        topRow.add(searchButton, "cell 2 0");

        searchPanel.add(topRow, "cell 0 0, growx, span 2");

        return searchPanel;
    }
    private void setupFrame() {
        setTitle("IT Support Ticket System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLocationRelativeTo(null);

        // Try to set icon if available, otherwise use default
        try {
            setIconImage(createImageIcon("/icons/ticket_icon.png", "App Icon").getImage());
        } catch (Exception e) {
            // Use a default icon if the image can't be loaded
            BufferedImage defaultIcon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = defaultIcon.createGraphics();
            g.setColor(PRIMARY_COLOR);
            g.fillRect(0, 0, 16, 16);
            g.dispose();
            setIconImage(defaultIcon);
        }
    }
}