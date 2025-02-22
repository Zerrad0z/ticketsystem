package org.example.ui;

import net.miginfocom.swing.MigLayout;
import org.example.model.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.ObjectMapper;

public class LoginPanel extends JPanel {
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final Consumer<UserDTO> onLoginCallback;

    public LoginPanel(Consumer<UserDTO> onLoginCallback) {
        this.onLoginCallback = onLoginCallback;
        setLayout(new MigLayout("fill, insets 20", "[grow]", "[]20[][]20[]"));

        // Header
        JLabel headerLabel = new JLabel("IT Support Ticket System");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(headerLabel, "cell 0 0, center");

        // Login form
        JPanel formPanel = new JPanel(new MigLayout("", "[][grow]", "[][]"));
        formPanel.setBorder(BorderFactory.createTitledBorder("Login"));

        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);

        formPanel.add(new JLabel("Username:"), "cell 0 0");
        formPanel.add(usernameField, "cell 1 0, growx");
        formPanel.add(new JLabel("Password:"), "cell 0 1");
        formPanel.add(passwordField, "cell 1 1, growx");

        add(formPanel, "cell 0 1, growx");

        // Login button
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> performLogin());
        add(loginButton, "cell 0 2, center");
    }

    private void performLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password");
            return;
        }

        try {
            // Send login request to backend
            URL url = new URL("http://localhost:8080/api/login");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Create request JSON body
            String jsonInputString = String.format("{\"username\": \"%s\", \"password\": \"%s\"}", username, password);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Get response
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                String responseLine;
                StringBuilder response = new StringBuilder();
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                // Parse response to UserDTO
                ObjectMapper objectMapper = new ObjectMapper();
                UserDTO userDTO = objectMapper.readValue(response.toString(), UserDTO.class);
                onLoginCallback.accept(userDTO);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Login failed: " + e.getMessage());
        }
    }
}
