package com.ticketsystem.util;

import javax.swing.*;
import java.awt.*;

public class CustomComponents {
    public static JButton createPrimaryButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(0, 0, UIConstants.PRIMARY_COLOR,
                        0, getHeight(), UIConstants.SECONDARY_COLOR);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(),
                        UIConstants.BORDER_RADIUS, UIConstants.BORDER_RADIUS);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        button.setForeground(Color.WHITE);
        button.setFont(UIConstants.NORMAL_FONT);
        button.setPreferredSize(UIConstants.BUTTON_SIZE);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    public static JTextField createStyledTextField() {
        JTextField textField = new JTextField();
        textField.setPreferredSize(UIConstants.INPUT_SIZE);
        textField.setFont(UIConstants.NORMAL_FONT);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return textField;
    }

    public static JPanel createPanelWithShadow() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw shadow
                g2.setColor(new Color(0, 0, 0, 20));
                g2.fillRoundRect(3, 3, getWidth() - 6, getHeight() - 6,
                        UIConstants.BORDER_RADIUS, UIConstants.BORDER_RADIUS);

                // Draw panel background
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 3,
                        UIConstants.BORDER_RADIUS, UIConstants.BORDER_RADIUS);

                g2.dispose();
            }
        };
        panel.setOpaque(false);
        return panel;
    }
}