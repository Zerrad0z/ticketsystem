package com.ticketsystem.ui;

import javax.swing.*;
import com.ticketsystem.model.*;
import com.ticketsystem.util.TicketTableModel;
import net.miginfocom.swing.MigLayout;
import com.ticketsystem.service.APIClient;

import javax.swing.border.Border;
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
    // Main panels
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JPanel loginPanel;
    private JPanel mainPanel;

    // Login components
    private JTextField usernameField;
    private JPasswordField passwordField;

    // Service components
    private APIClient apiClient;
    private UserDTO currentUser;

    // Table components
    private TicketTableModel ticketTableModel;
    private JTable ticketsTable;

    // Action buttons
    private JButton changeStatusButton;
    private JButton addCommentButton;
    private JButton viewAuditLogButton;

    // Color scheme - Modern flat design palette
    private final Color PRIMARY_COLOR = new Color(25, 118, 210);      // Material Blue
    private final Color SECONDARY_COLOR = new Color(66, 165, 245);    // Lighter blue
    private final Color ACCENT_COLOR = new Color(76, 175, 80);        // Material Green
    private final Color BACKGROUND_COLOR = new Color(250, 250, 250);  // Off-white
    private final Color TEXT_COLOR = new Color(33, 33, 33);           // Dark gray
    private final Color ERROR_COLOR = new Color(211, 47, 47);         // Material Red
    private final Color SURFACE_COLOR = Color.WHITE;                  // Pure white for content areas

    // Typography
    private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font BUTTON_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 12);

    public TicketSystemClient() {
        // Initialize API client
        apiClient = new APIClient("http://localhost:8080/api");

        // Set up the application frame
        setupFrame();

        // Initialize card layout for navigation
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BACKGROUND_COLOR);

        // Create login panel
        createLoginPanel();
        contentPanel.add(loginPanel, "login");
        add(contentPanel);
        cardLayout.show(contentPanel, "login");

        // Apply modern look and feel
        applyLookAndFeel();
    }

    private void setupFrame() {
        setTitle("IT Support Ticket System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);

        try {
            setIconImage(createImageIcon("/icons/ticket_icon.png", "App Icon").getImage());
        } catch (Exception e) {
            setIconImage(createDefaultIcon(16, 16, PRIMARY_COLOR));
        }
    }

    private void applyLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("FlatLaf".equals(info.getName()) || "Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }

            // Set some UI manager properties for consistent styling
            UIManager.put("ToolTip.background", SURFACE_COLOR);
            UIManager.put("ToolTip.foreground", TEXT_COLOR);
            UIManager.put("ToolTip.border", BorderFactory.createLineBorder(SECONDARY_COLOR, 1));

            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            System.out.println("Modern look and feel not available, using default");
        }
    }

    private void createLoginPanel() {
        loginPanel = new JPanel(new MigLayout("fill, insets 0", "[grow]", "[grow]"));
        loginPanel.setBackground(BACKGROUND_COLOR);

        // Create login card with shadow effect
        JPanel loginCard = new JPanel(new MigLayout("fill, insets 40", "[grow]", "[]30[][]30[]"));
        loginCard.setBackground(SURFACE_COLOR);
        loginCard.setBorder(createCardBorder());

        // Create logo and branding
        ImageIcon logoIcon = createImageIcon("/icons/support_logo.png", "IT Support Logo");
        JLabel logoLabel = new JLabel(logoIcon);
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Create title
        JLabel titleLabel = new JLabel("IT Support Ticket System");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Create input fields with enhanced styling
        usernameField = createStyledTextField("Username", 20);
        passwordField = createStyledPasswordField("Password", 20);

        // Add icon to text fields
        JPanel usernamePanel = createIconTextField(usernameField, "/icons/user_icon.png", "User");
        JPanel passwordPanel = createIconTextField(passwordField, "/icons/lock_icon.png", "Lock");

        // Create buttons
        JButton loginButton = createPrimaryButton("Login");
        JButton registerButton = createLinkButton("Create Account");

        // Add action listeners
        loginButton.addActionListener(e -> handleLogin());
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

        // Add components to login card
        loginCard.add(logoLabel, "cell 0 0, center");
        loginCard.add(titleLabel, "cell 0 1, center");
        loginCard.add(usernamePanel, "cell 0 2, center, growx, width 300!");
        loginCard.add(passwordPanel, "cell 0 3, center, growx, width 300!");
        loginCard.add(loginButton, "cell 0 4, center, width 300!");
        loginCard.add(registerButton, "cell 0 5, center, gaptop 15");

        // Add login card to panel with centering
        loginPanel.add(loginCard, "cell 0 0, width 450!, center, height 550!, center");
    }

    private JPanel createIconTextField(JTextField field, String iconPath, String iconDesc) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SURFACE_COLOR);

        JLabel iconLabel = new JLabel(createImageIcon(iconPath, iconDesc));
        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

        panel.add(iconLabel, BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);

        return panel;
    }

    private Border createCardBorder() {
        // Create a compound border with subtle shadow effect
        return BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(5, 5, 5, 5),
                        BorderFactory.createLineBorder(new Color(0, 0, 0, 20), 1)
                ),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        );
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

        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(
                        Math.min(PRIMARY_COLOR.getRed() + 20, 255),
                        Math.min(PRIMARY_COLOR.getGreen() + 20, 255),
                        Math.min(PRIMARY_COLOR.getBlue() + 20, 255)
                ));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(PRIMARY_COLOR);
            }
        });

        return button;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(SURFACE_COLOR);
        button.setForeground(PRIMARY_COLOR);
        button.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 2));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(250, 45));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);

        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(240, 245, 255));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(SURFACE_COLOR);
            }
        });

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

        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(PRIMARY_COLOR);
                button.setText("<html><u>" + text + "</u></html>");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(SECONDARY_COLOR);
                button.setText(text);
            }
        });

        return button;
    }

    private void showRegisterDialog() {
        JDialog dialog = new JDialog(this, "Create New Account", true);
        dialog.setLayout(new MigLayout("fillx, insets 30", "[right][grow]", "[]25[]15[]15[]25[]"));
        dialog.getContentPane().setBackground(SURFACE_COLOR);

        // Create styled components
        JTextField regUsernameField = createStyledTextField("Username", 20);
        JPasswordField regPasswordField = createStyledPasswordField("Password", 20);
        JComboBox<Role> roleComboBox = new JComboBox<>(Role.values());
        roleComboBox.setPreferredSize(new Dimension(250, 40));
        roleComboBox.setFont(REGULAR_FONT);

        // Create title and labels
        JLabel titleLabel = new JLabel("Create Account");
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);

        JLabel usernameLabel = new JLabel("Username:");
        JLabel passwordLabel = new JLabel("Password:");
        JLabel roleLabel = new JLabel("Role:");
        usernameLabel.setFont(REGULAR_FONT);
        passwordLabel.setFont(REGULAR_FONT);
        roleLabel.setFont(REGULAR_FONT);

        // Create buttons
        JButton submitButton = createPrimaryButton("Register");
        JButton cancelButton = createSecondaryButton("Cancel");

        // Add action listeners
        cancelButton.addActionListener(e -> dialog.dispose());
        submitButton.addActionListener(e -> {
            try {
                String username = regUsernameField.getText();
                String password = new String(regPasswordField.getPassword());
                Role role = (Role) roleComboBox.getSelectedItem();

                if (username.isEmpty() || password.isEmpty()) {
                    showErrorDialog(dialog, "Registration Error", "Please enter both username and password");
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
        buttonPanel.setBackground(SURFACE_COLOR);
        buttonPanel.add(cancelButton, "cell 0 0, growx");
        buttonPanel.add(submitButton, "cell 1 0, growx");
        dialog.add(buttonPanel, "span 2, growx, gaptop 20");

        // Configure dialog
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
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            // Display the specific error message from the APIClient
            showErrorDialog(this, "Login Error", ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            showErrorDialog(this, "Login Error", "An unexpected error occurred. Please try again.");
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
        errorDialog.getContentPane().setBackground(SURFACE_COLOR);

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
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBackground(PRIMARY_COLOR);

        // Logo panel at the top
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setBackground(new Color(13, 71, 161)); // Darker blue
        logoPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        JLabel appTitle = new JLabel("IT Support Desk");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        appTitle.setForeground(Color.WHITE);
        logoPanel.add(appTitle, BorderLayout.CENTER);

        // User info panel
        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setBackground(new Color(25, 118, 210)); // Primary color
        userPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel userLabel = new JLabel(currentUser.getUsername());
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        userLabel.setForeground(Color.WHITE);

        JLabel roleLabel = new JLabel(currentUser.getRole().toString());
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleLabel.setForeground(new Color(236, 240, 241));

        JPanel userInfoPanel = new JPanel(new MigLayout("insets 0", "[]", "[]0[]"));
        userInfoPanel.setBackground(new Color(25, 118, 210));
        userInfoPanel.add(userLabel, "wrap");
        userInfoPanel.add(roleLabel);
        userPanel.add(userInfoPanel, BorderLayout.CENTER);

        // Menu items
        JPanel menuPanel = new JPanel(new MigLayout("fillx, insets 0", "[grow]", "[]0[]0[]"));
        menuPanel.setBackground(PRIMARY_COLOR);

        JButton dashboardButton = createSidebarButton("Dashboard", "/icons/dashboard.png");
        JButton ticketsButton = createSidebarButton("Tickets", "/icons/ticket.png");

        // Set tickets button as active
        ticketsButton.setBackground(new Color(13, 71, 161)); // Darker blue
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
        button.setIconTextGap(12);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(220, 48));

        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.getBackground().equals(PRIMARY_COLOR)) {
                    button.setBackground(new Color(13, 71, 161)); // Darker blue
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!text.equals("Tickets")) { // Keep Tickets button highlighted
                    button.setBackground(PRIMARY_COLOR);
                }
            }
        });

        return button;
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(SURFACE_COLOR);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 218, 218), 1),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        // Title with icon
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setBackground(SURFACE_COLOR);

        JLabel iconLabel = new JLabel(createImageIcon("/icons/ticket_management.png", "Tickets"));
        JLabel titleLabel = new JLabel("Ticket Management");
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(TEXT_COLOR);

        titlePanel.add(iconLabel);
        titlePanel.add(titleLabel);

        // Action panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setBackground(SURFACE_COLOR);

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

            // Add hover effect
            createTicketButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    createTicketButton.setBackground(new Color(67, 160, 71)); // Darker green
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    createTicketButton.setBackground(ACCENT_COLOR);
                }
            });

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

            // Add hover effect
            refreshButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    refreshButton.setBackground(new Color(30, 136, 229)); // Darker blue
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    refreshButton.setBackground(SECONDARY_COLOR);
                }
            });

            refreshButton.addActionListener(e -> refreshTickets());
            actionPanel.add(refreshButton);
        }

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(actionPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createFilterSearchPanel() {
        JPanel filterSearchPanel = new JPanel(new MigLayout("fillx, insets 0", "[grow]", "[]"));
        filterSearchPanel.setBackground(BACKGROUND_COLOR);

        // Enhanced search panel that includes both search and filters
        JPanel enhancedSearchPanel = createEnhancedSearchPanel();

        // Add to filter search panel
        filterSearchPanel.add(enhancedSearchPanel, "cell 0 0, growx");

        return filterSearchPanel;
    }

    private JPanel createEnhancedSearchPanel() {
        JPanel searchPanel = new JPanel(new MigLayout("fillx, insets 0", "[grow][]", "[][]"));
        searchPanel.setBackground(SURFACE_COLOR);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 218, 218), 1),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        // Create search bar
        JPanel searchBarPanel = new JPanel(new MigLayout("fillx, insets 0", "[100][grow][100]", "[]"));
        searchBarPanel.setBackground(SURFACE_COLOR);

        // Search type combo box with modern styling
        String[] searchOptions = {"ID", "Title"};
        JComboBox<String> searchTypeCombo = new JComboBox<>(searchOptions);
        searchTypeCombo.setFont(REGULAR_FONT);
        searchTypeCombo.setBackground(SURFACE_COLOR);
        searchTypeCombo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 218, 218), 1),
                BorderFactory.createEmptyBorder(0, 5, 0, 5)
        ));
        searchTypeCombo.setPreferredSize(new Dimension(120, 40));

        // Search field with icon
        JTextField searchField = createStyledTextField("Search tickets...", 20);
        searchField.setPreferredSize(new Dimension(0, 40));

        JPanel searchFieldPanel = new JPanel(new BorderLayout());
        searchFieldPanel.setBackground(SURFACE_COLOR);

        JLabel searchIcon = new JLabel(createImageIcon("/icons/search.png", "Search"));
        searchIcon.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        searchFieldPanel.add(searchIcon, BorderLayout.WEST);
        searchFieldPanel.add(searchField, BorderLayout.CENTER);

        // Search button
        JButton searchButton = new JButton("Search");
        searchButton.setFont(BUTTON_FONT);
        searchButton.setBackground(PRIMARY_COLOR);
        searchButton.setForeground(Color.WHITE);
        searchButton.setBorderPainted(false);
        searchButton.setFocusPainted(false);
        searchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        searchButton.setPreferredSize(new Dimension(100, 40));

        // Add hover effect
        searchButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                searchButton.setBackground(new Color(13, 71, 161)); // Darker blue
            }

            @Override
            public void mouseExited(MouseEvent e) {
                searchButton.setBackground(PRIMARY_COLOR);
            }
        });

        // Add search components to panel
        searchBarPanel.add(searchTypeCombo, "cell 0 0");
        searchBarPanel.add(searchFieldPanel, "cell 1 0, growx");
        searchBarPanel.add(searchButton, "cell 2 0");

        // Add filter toggles in a separate row
        JPanel filterTogglePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterTogglePanel.setBackground(SURFACE_COLOR);

        JLabel filterLabel = new JLabel("Quick Filters:");
        filterLabel.setFont(REGULAR_FONT);
        filterTogglePanel.add(filterLabel);

        // Status filter pills
        String[] statuses = {"NEW", "IN_PROGRESS", "RESOLVED"};
        Color[] statusColors = {new Color(33, 150, 243), new Color(255, 152, 0), new Color(76, 175, 80)};

        for (int i = 0; i < statuses.length; i++) {
            JButton statusFilter = createFilterPill(statuses[i], statusColors[i]);
            filterTogglePanel.add(statusFilter);
        }

        // Add separator
        filterTogglePanel.add(new JSeparator(SwingConstants.VERTICAL));

        // Priority filter pills
        String[] priorities = {"HIGH", "MEDIUM", "LOW"};
        Color[] priorityColors = {new Color(244, 67, 54), new Color(255, 152, 0), new Color(76, 175, 80)};

        for (int i = 0; i < priorities.length; i++) {
            JButton priorityFilter = createFilterPill(priorities[i], priorityColors[i]);
            filterTogglePanel.add(priorityFilter);
        }

        // Create search action to be used by button and Enter key
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
        searchField.addActionListener(searchAction);

        // Add components to main search panel
        searchPanel.add(searchBarPanel, "cell 0 0, growx, span 2");
        searchPanel.add(filterTogglePanel, "cell 0 1, growx, span 2");

        return searchPanel;
    }

    private JButton createFilterPill(String text, Color color) {
        JButton pill = new JButton(text);
        pill.setFont(SMALL_FONT);
        pill.setForeground(SURFACE_COLOR);
        pill.setBackground(color);
        pill.setBorderPainted(false);
        pill.setFocusPainted(false);
        pill.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pill.setPreferredSize(new Dimension(90, 28));

        // Add hover and click effects
        pill.addMouseListener(new MouseAdapter() {
            private boolean isActive = false;

            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isActive) {
                    pill.setBackground(new Color(
                            Math.max(color.getRed() - 20, 0),
                            Math.max(color.getGreen() - 20, 0),
                            Math.max(color.getBlue() - 20, 0)
                    ));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!isActive) {
                    pill.setBackground(color);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                isActive = !isActive;

                if (isActive) {
                    pill.setBorder(BorderFactory.createLineBorder(SURFACE_COLOR, 2));

                    // Apply filter
                    String filterText = pill.getText();
                    TableRowSorter<TicketTableModel> sorter = new TableRowSorter<>(ticketTableModel);

                    if (filterText.equals("HIGH") || filterText.equals("MEDIUM") || filterText.equals("LOW")) {
                        sorter.setRowFilter(RowFilter.regexFilter("^" + filterText + "$", 2));
                    } else {
                        sorter.setRowFilter(RowFilter.regexFilter("^" + filterText + "$", 4));
                    }

                    ticketsTable.setRowSorter(sorter);
                } else {
                    pill.setBorder(BorderFactory.createEmptyBorder());
                    ticketsTable.setRowSorter(null);
                }
            }
        });

        return pill;
    }

    private JPanel createTicketTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(SURFACE_COLOR);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 218, 218), 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        // Configure and create table
        configureTicketTable();

        // Create a scrollable view for the table
        JScrollPane scrollPane = new JScrollPane(ticketsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(SURFACE_COLOR);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Add action panel at the bottom if IT Support
        if (currentUser.isItSupport()) {
            JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            actionPanel.setBackground(SURFACE_COLOR);
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

            // Add hover effects
            changeStatusButton.addMouseListener(new ButtonHoverAdapter(changeStatusButton, SECONDARY_COLOR));
            addCommentButton.addMouseListener(new ButtonHoverAdapter(addCommentButton, ACCENT_COLOR));

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

    // Helper class for button hover effects
    private class ButtonHoverAdapter extends MouseAdapter {
        private final JButton button;
        private final Color originalColor;

        public ButtonHoverAdapter(JButton button, Color originalColor) {
            this.button = button;
            this.originalColor = originalColor;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if (button.isEnabled()) {
                button.setBackground(new Color(
                        Math.max(originalColor.getRed() - 20, 0),
                        Math.max(originalColor.getGreen() - 20, 0),
                        Math.max(originalColor.getBlue() - 20, 0)
                ));
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (button.isEnabled()) {
                button.setBackground(originalColor);
            }
        }
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
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
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
                            label.setForeground(new Color(211, 47, 47));
                        }
                        case MEDIUM -> {
                            panel.setBackground(new Color(255, 248, 230));
                            label.setForeground(new Color(255, 153, 0));
                        }
                        case LOW -> {
                            panel.setBackground(new Color(232, 250, 240));
                            label.setForeground(new Color(46, 125, 50));
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
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
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
                            label.setForeground(new Color(21, 101, 192));
                        }
                        case IN_PROGRESS -> {
                            panel.setBackground(new Color(255, 243, 205));
                            label.setForeground(new Color(245, 124, 0));
                        }
                        case RESOLVED -> {
                            panel.setBackground(new Color(212, 237, 218));
                            label.setForeground(new Color(46, 125, 50));
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
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
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
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value != null) {
                    setToolTipText(value.toString());
                }
                return c;
            }
        });

        // Add double-click listener for ticket details
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
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? SURFACE_COLOR : new Color(249, 249, 249));
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
        dialog.getContentPane().setBackground(SURFACE_COLOR);

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
        contentPanel.setBackground(SURFACE_COLOR);

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
        commentsPanel.setBackground(SURFACE_COLOR);
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
        commentsContentPanel.setBackground(SURFACE_COLOR);

        if (ticket.getTicketComments() != null && !ticket.getTicketComments().isEmpty()) {
            for (CommentDTO comment : ticket.getTicketComments()) {
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
        dialog.getContentPane().setBackground(SURFACE_COLOR);

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
        contentPanel.setBackground(SURFACE_COLOR);

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
        dialog.getContentPane().setBackground(SURFACE_COLOR);

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
        contentPanel.setBackground(SURFACE_COLOR);

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
        dialog.getContentPane().setBackground(SURFACE_COLOR);

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
        filterPanel.setBackground(SURFACE_COLOR);
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
        columnModel.getColumn(0).setPreferredWidth(70);   // Ticket ID
        columnModel.getColumn(1).setPreferredWidth(120);  // Action
        columnModel.getColumn(2).setPreferredWidth(100);  // Old Value
        columnModel.getColumn(3).setPreferredWidth(100);  // New Value
        columnModel.getColumn(4).setPreferredWidth(100);  // Performed By
        columnModel.getColumn(5).setPreferredWidth(150);  // Date

        JScrollPane tableScroll = new JScrollPane(auditTable);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        tableScroll.getViewport().setBackground(SURFACE_COLOR);

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
        dialog.add(filterPanel, BorderLayout.PAGE_START);
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
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? SURFACE_COLOR : new Color(249, 249, 249));
                }

                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
                        BorderFactory.createEmptyBorder(0, 10, 0, 10)
                ));

                return c;
            }
        });
    }

    private void showCreateTicketDialog() {
        JDialog dialog = new JDialog(this, "Create New Ticket", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(SURFACE_COLOR);

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
        formPanel.setBackground(SURFACE_COLOR);

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
             //   refreshTickets();
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

    private void applyFilters(Category category, Priority priority, Status status, Date fromDate, Date toDate) {
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
            case HIGH -> new Color(211, 47, 47);     // Material Red
            case MEDIUM -> new Color(255, 152, 0);   // Material Orange
            case LOW -> new Color(46, 125, 50);      // Material Green
        };
    }

    private Color getStatusColor(Status status) {
        return switch (status) {
            case NEW -> new Color(21, 101, 192);     // Material Blue
            case IN_PROGRESS -> new Color(255, 152, 0); // Material Orange
            case RESOLVED -> new Color(46, 125, 50); // Material Green
        };
    }

    private ImageIcon createImageIcon(String path, String description) {
        java.net.URL imgURL = getClass().getResource(path);
        if (imgURL != null) {
            ImageIcon icon = new ImageIcon(imgURL, description);
            // Resize the icon to appropriate dimensions
            Image img = icon.getImage();
            // Determine appropriate size based on icon type
            int width = 24;
            int height = 24;

            if (path.contains("user") || path.contains("lock")) {
                width = height = 16;
            } else if (path.contains("support_logo")) {
                width = height = 64; // Larger for the main logo
            }

            Image resizedImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(resizedImg, description);
        } else {
            System.err.println("Warning: Could not find image: " + path);
            return createFallbackIcon(path, description);
        }
    }

    private ImageIcon createFallbackIcon(String path, String description) {
        // Create a simple colored square as fallback
        int size = 16;
        Color iconColor = PRIMARY_COLOR;

        // Use different colors based on icon type
        if (path.contains("user")) {
            iconColor = new Color(66, 165, 245);  // Light blue
            size = 20;
        } else if (path.contains("lock")) {
            iconColor = new Color(211, 47, 47);   // Red
            size = 20;
        } else if (path.contains("search")) {
            iconColor = new Color(149, 165, 166); // Gray
        } else if (path.contains("clear")) {
            iconColor = new Color(189, 195, 199); // Light gray
        } else if (path.contains("add")) {
            iconColor = ACCENT_COLOR;
        }

        return new ImageIcon(createDefaultIcon(size, size, iconColor), description);
    }

    private Image createDefaultIcon(int width, int height, Color color) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();
        return img;
    }
}