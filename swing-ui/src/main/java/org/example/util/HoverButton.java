package org.example.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

class HoverButton extends JButton {
    private Color normalColor;
    private Color hoverColor;
    private Color pressedColor;
    private boolean isRounded = true;
    private int cornerRadius = 8;

    public HoverButton(String text) {
        super(text);
        setupButton();
    }

    public HoverButton(String text, Color normalColor, Color hoverColor, Color pressedColor) {
        super(text);
        this.normalColor = normalColor;
        this.hoverColor = hoverColor;
        this.pressedColor = pressedColor;
        setupButton();
    }

    private void setupButton() {
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);

        if (normalColor == null) {
            normalColor = new Color(41, 128, 185);
        }

        if (hoverColor == null) {
            hoverColor = new Color(52, 152, 219);
        }

        if (pressedColor == null) {
            pressedColor = new Color(36, 113, 163);
        }

        setBackground(normalColor);
        setForeground(Color.WHITE);

        // Add mouse listeners for hover effects
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    setBackground(hoverColor);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (isEnabled()) {
                    setBackground(normalColor);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()) {
                    setBackground(pressedColor);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isEnabled()) {
                    setBackground(getModel().isRollover() ? hoverColor : normalColor);
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (isRounded) {
            // Draw rounded background
            if (getModel().isPressed()) {
                g2.setColor(pressedColor);
            } else if (getModel().isRollover()) {
                g2.setColor(hoverColor);
            } else {
                g2.setColor(getBackground());
            }

            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
        } else {
            // Draw regular background
            if (getModel().isPressed()) {
                g2.setColor(pressedColor);
            } else if (getModel().isRollover()) {
                g2.setColor(hoverColor);
            } else {
                g2.setColor(getBackground());
            }

            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        g2.dispose();
        super.paintComponent(g);
    }

    public void setRounded(boolean rounded) {
        this.isRounded = rounded;
        repaint();
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    public void setButtonColors(Color normal, Color hover, Color pressed) {
        this.normalColor = normal;
        this.hoverColor = hover;
        this.pressedColor = pressed;
        setBackground(normalColor);
        repaint();
    }
}