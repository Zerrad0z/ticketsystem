package org.example.ui;

import net.miginfocom.swing.MigLayout;
import org.example.model.*;
import org.example.service.ApiClient;
import org.example.service.TicketService;

import javax.swing.*;
import java.awt.*;

public class CreateTicketPanel extends JPanel {
    private final MainFrame mainFrame;
    private final JTextField titleField;
    private final JTextArea descriptionArea;
    private final JComboBox<Priority> priorityCombo;
    private final JComboBox<Category> categoryCombo;

    public CreateTicketPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new MigLayout("fill, insets 20", "[grow]", "[]20[][grow]20[]"));

        // Header
        JLabel headerLabel = new JLabel("Create New Ticket");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        add(headerLabel, "cell 0 0");

        // Form Panel
        JPanel formPanel = new JPanel(new MigLayout("", "[][grow]", "[][][][][]"));
        formPanel.setBorder(BorderFactory.createEtchedBorder());

        titleField = new JTextField(30);
        descriptionArea = new JTextArea(5, 30);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        priorityCombo = new JComboBox<>(Priority.values());
        categoryCombo = new JComboBox<>(Category.values());

        formPanel.add(new JLabel("Title:"), "cell 0 0");
        formPanel.add(titleField, "cell 1 0, growx");
        formPanel.add(new JLabel("Description:"), "cell 0 1");
        formPanel.add(new JScrollPane(descriptionArea), "cell 1 1, growx");
        formPanel.add(new JLabel("Priority:"), "cell 0 2");
        formPanel.add(priorityCombo, "cell 1 2");
        formPanel.add(new JLabel("Category:"), "cell 0 3");
        formPanel.add(categoryCombo, "cell 1 3");

        add(formPanel, "cell 0 1, grow");

        // Buttons Panel
        JPanel buttonsPanel = new JPanel(new MigLayout("", "push[][]", "[]"));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> mainFrame.showTicketListPanel());

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> submitTicket());

        buttonsPanel.add(cancelButton);
        buttonsPanel.add(submitButton);

        add(buttonsPanel, "cell 0 2, growx");
    }

    private void submitTicket() {
        if (titleField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a title");
            return;
        }

        if (descriptionArea.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a description");
            return;
        }

        // Get input data from the form
        String title = titleField.getText();
        String description = descriptionArea.getText();
        Priority priority = (Priority) priorityCombo.getSelectedItem();
        Category category = (Category) categoryCombo.getSelectedItem();

        // Call TicketService to create the ticket
        TicketService ticketService = new TicketService(new ApiClient());  // Assuming you have an ApiClient instance
        Ticket createdTicket = ticketService.createTicket(title, description, priority, category);

        if (createdTicket != null) {
            JOptionPane.showMessageDialog(this, "Ticket created successfully!");
            clearForm();
            mainFrame.showTicketListPanel();  // Switch back to the ticket list panel
        } else {
            JOptionPane.showMessageDialog(this, "Error creating ticket. Please try again.");
        }
    }


    private void clearForm() {
        titleField.setText("");
        descriptionArea.setText("");
        priorityCombo.setSelectedIndex(0);
        categoryCombo.setSelectedIndex(0);
    }
}