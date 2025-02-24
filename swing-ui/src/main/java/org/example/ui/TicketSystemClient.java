/**
 * Ticket System Client Application
 * Main class for the IT Support Ticket System GUI
 */
package org.example.ui;

// Import statements
import javax.swing.*;
import net.miginfocom.swing.MigLayout;
import org.example.model.*;
import org.example.service.APIClient;
import org.example.util.CustomComponents;
import org.example.util.UIConstants;
import org.jdesktop.swingx.JXDatePicker;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public class TicketSystemClient extends JFrame {
    // Class Fields
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
    private JButton viewAuditLogButton;

    /**
     * Constructor: Initializes the main application window
     */
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

    /**
     * Sets up the main application frame
     */
    private void setupFrame() {
        setTitle("IT Support Ticket System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
    }

    /**
     * Creates and configures the login panel
     */
    private void createLoginPanel() {
        loginPanel = new JPanel(new MigLayout("fill, insets 40", "[grow]", "[]30[][]30[]"));

        // Create title with custom font and size
        JLabel titleLabel = new JLabel("IT Support Ticket System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(44, 62, 80));

        // Create and style input fields
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        usernameField.setPreferredSize(new Dimension(250, 35));
        passwordField.setPreferredSize(new Dimension(250, 35));

        Font inputFont = new Font("Arial", Font.PLAIN, 14);
        usernameField.setFont(inputFont);
        passwordField.setFont(inputFont);
        usernameField.putClientProperty("JTextField.placeholderText", "Enter username");
        passwordField.putClientProperty("JTextField.placeholderText", "Enter password");

        // Create and style buttons
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Create Account");

        loginButton.setPreferredSize(new Dimension(250, 40));
        registerButton.setPreferredSize(new Dimension(250, 35));
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        registerButton.setFont(new Font("Arial", Font.PLAIN, 12));

        // Style login button
        loginButton.setBackground(new Color(52, 152, 219));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setOpaque(true);

        // Style register button as link
        registerButton.setBorderPainted(false);
        registerButton.setContentAreaFilled(false);
        registerButton.setForeground(new Color(52, 152, 219));
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Layout components
        loginPanel.add(titleLabel, "cell 0 0, center");
        loginPanel.add(usernameField, "cell 0 1, center");
        loginPanel.add(passwordField, "cell 0 2, center");
        loginPanel.add(loginButton, "cell 0 3, center");
        loginPanel.add(registerButton, "cell 0 4, center");

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
    }

    /**
     * Handles the login process
     */
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
            UserDTO user = apiClient.login(loginRequest);

            if (user != null) {
                currentUser = user;
                usernameField.setText("");
                passwordField.setText("");

                if (mainPanel != null) {
                    contentPanel.remove(mainPanel);
                }

                createMainPanel();
                contentPanel.add(mainPanel, "main");
                cardLayout.show(contentPanel, "main");
                contentPanel.revalidate();
                contentPanel.repaint();
                refreshTickets();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid credentials",
                        "Login Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Login failed: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    /**
     * Shows the registration dialog for new user creation
     */
    private void showRegisterDialog() {
        JDialog dialog = new JDialog(this, "Create New Account", true);
        dialog.setLayout(new MigLayout("fillx, insets 30", "[right][grow]", "[]15[]15[]25[]"));

        // Create form fields
        JTextField regUsernameField = new JTextField(20);
        JPasswordField regPasswordField = new JPasswordField(20);
        JComboBox<Role> roleComboBox = new JComboBox<>(Role.values());

        // Style components
        Font labelFont = new Font("Arial", Font.BOLD, 12);
        regUsernameField.setPreferredSize(new Dimension(200, 30));
        regPasswordField.setPreferredSize(new Dimension(200, 30));
        roleComboBox.setPreferredSize(new Dimension(200, 30));

        // Create and style labels
        JLabel titleLabel = new JLabel("Create Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));

        JLabel usernameLabel = new JLabel("Username:");
        JLabel passwordLabel = new JLabel("Password:");
        JLabel roleLabel = new JLabel("Role:");
        usernameLabel.setFont(labelFont);
        passwordLabel.setFont(labelFont);
        roleLabel.setFont(labelFont);

        // Create submit button
        JButton submitButton = new JButton("Register");
        submitButton.setPreferredSize(new Dimension(200, 35));
        submitButton.setBackground(new Color(52, 152, 219));
        submitButton.setForeground(Color.WHITE);
        submitButton.setFont(new Font("Arial", Font.BOLD, 14));
        submitButton.setFocusPainted(false);
        submitButton.setBorderPainted(false);
        submitButton.setOpaque(true);

        // Add registration action
        submitButton.addActionListener(e -> {
            try {
                String username = regUsernameField.getText();
                String password = new String(regPasswordField.getPassword());
                Role role = (Role) roleComboBox.getSelectedItem();

                RegisterRequest request = new RegisterRequest(username, password, role);
                apiClient.register(request);
                JOptionPane.showMessageDialog(dialog,
                        "Registration successful! Please login.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Registration failed: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Layout components
        dialog.add(titleLabel, "span 2, center, gapbottom 20");
        dialog.add(usernameLabel, "cell 0 1");
        dialog.add(regUsernameField, "cell 1 1, growx");
        dialog.add(passwordLabel, "cell 0 2");
        dialog.add(regPasswordField, "cell 1 2, growx");
        dialog.add(roleLabel, "cell 0 3");
        dialog.add(roleComboBox, "cell 1 3, growx");
        dialog.add(submitButton, "span 2, center, gaptop 20");

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Creates the main application panel after successful login
     */
    private void createMainPanel() {
        mainPanel = new JPanel(new MigLayout("fill, insets 0", "[grow]", "[][][][grow]"));
        mainPanel.setBackground(UIConstants.BACKGROUND_COLOR);

        createHeader();
        createToolsPanel();
        createTablePanel();
    }

    /**
     * Creates the header section of the main panel
     */
    private void createHeader() {
        JPanel headerPanel = new JPanel(new MigLayout("fill, insets 15", "[grow][]", "[]")) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, UIConstants.PRIMARY_COLOR, w, h, UIConstants.SECONDARY_COLOR);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
                g2d.dispose();
            }
        };
        headerPanel.setPreferredSize(new Dimension(900, 70));

        // Create title section
        JPanel titlePanel = new JPanel(new MigLayout("", "[]", "[][]"));
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("IT Support Ticket System");
        titleLabel.setFont(UIConstants.TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);
        JLabel roleLabel = new JLabel(currentUser.getRole());
        roleLabel.setFont(UIConstants.NORMAL_FONT);
        roleLabel.setForeground(new Color(255, 255, 255, 200));
        titlePanel.add(titleLabel, "wrap");
        titlePanel.add(roleLabel);

        // Create user section
        JPanel userPanel = new JPanel(new MigLayout("", "[][]", "[]"));
        userPanel.setOpaque(false);

        // Create user avatar
        JLabel avatarLabel = new JLabel(currentUser.getUsername().substring(0, 1).toUpperCase()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatarLabel.setPreferredSize(new Dimension(35, 35));
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        avatarLabel.setForeground(UIConstants.PRIMARY_COLOR);
        avatarLabel.setFont(UIConstants.HEADER_FONT);

        // Create logout button
        JButton logoutButton = new JButton("Logout");
        styleHeaderButton(logoutButton);
        logoutButton.addActionListener(e -> handleLogout());

        userPanel.add(avatarLabel);
        userPanel.add(logoutButton, "gapleft 10");

        headerPanel.add(titlePanel, "left");
        headerPanel.add(userPanel, "right");
        mainPanel.add(headerPanel, "north, growx");
    }
}