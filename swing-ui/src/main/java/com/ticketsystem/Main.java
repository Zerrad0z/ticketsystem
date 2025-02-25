package com.ticketsystem;

import com.ticketsystem.ui.TicketSystemClient;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        // Set up the look and feel for a consistent appearance across platforms
        try {
            // Try to use the Nimbus look and feel for a modern appearance
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());

                    // Customize some Nimbus default colors
                    UIManager.put("nimbusBase", new Color(41, 128, 185));
                    UIManager.put("nimbusBlueGrey", new Color(52, 73, 94));
                    UIManager.put("control", new Color(245, 245, 245));

                    break;
                }
            }

            // If Nimbus is not available, fall back to the system look and feel
            if (UIManager.getLookAndFeel().getName().equals("Metal")) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        } catch (Exception e) {
            System.out.println("Could not set look and feel: " + e.getMessage());
        }

        // Set some global UI properties for better appearance
        UIManager.put("Button.arc", 8);
        UIManager.put("ToolTip.background", new Color(52, 73, 94));
        UIManager.put("ToolTip.foreground", Color.WHITE);
        UIManager.put("ToolTip.border", BorderFactory.createEmptyBorder(5, 8, 5, 8));

        // Launch the application on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                TicketSystemClient client = new TicketSystemClient();
                client.setVisible(true);

                // Optional: Display a splash screen or welcome message
                JOptionPane.showMessageDialog(client,
                        "Welcome to the IT Support Ticket System!\n\n" +
                                "This application allows you to manage IT support tickets efficiently.\n" +
                                "Please log in to access the system.",
                        "IT Support Ticket System",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Failed to start the application: " + e.getMessage(),
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
