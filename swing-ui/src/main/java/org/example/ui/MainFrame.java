package org.example.ui;

import org.example.model.*;
import org.example.service.ApiClient;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final LoginPanel loginPanel;
    private final TicketListPanel ticketListPanel;
    private final CreateTicketPanel createTicketPanel;
    private User currentUser;

    public MainFrame() {
        super("IT Support Ticket System");

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        loginPanel = new LoginPanel(this::onLogin);
        ticketListPanel = new TicketListPanel(this);
        createTicketPanel = new CreateTicketPanel(this);

        mainPanel.add(loginPanel, "LOGIN");
        mainPanel.add(ticketListPanel, "TICKETS");
        mainPanel.add(createTicketPanel, "CREATE");

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

        showLoginPanel();
    }

    public void showLoginPanel() {
        cardLayout.show(mainPanel, "LOGIN");
    }

    public void showTicketListPanel() {
        ticketListPanel.refreshTickets();
        cardLayout.show(mainPanel, "TICKETS");
    }

    public void showCreateTicketPanel() {
        cardLayout.show(mainPanel, "CREATE");
    }

    private void onLogin(UserDTO userDTO) {
        // Set the current user ID for API requests
        ApiClient.setCurrentUserId(String.valueOf(userDTO.getId()));

        // Create a User object for the UI
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setRole(userDTO.isItSupport() ? Role.IT_SUPPORT : Role.EMPLOYEE);
        this.currentUser = user;

        // Show the ticket list
        showTicketListPanel();
    }

    public User getCurrentUser() {
        return currentUser;
    }
}
