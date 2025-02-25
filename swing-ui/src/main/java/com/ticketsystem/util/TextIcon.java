package com.ticketsystem.util;

import javax.swing.*;
import java.awt.*;

public class TextIcon implements Icon {
    private final String text;
    private final Color background;
    private final Color foreground;
    private final int width;
    private final int height;

    public TextIcon(String text, Color background, Color foreground, int width, int height) {
        this.text = text;
        this.background = background;
        this.foreground = foreground;
        this.width = width;
        this.height = height;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw background
        g2.setColor(background);
        g2.fillRoundRect(x, y, width, height, 8, 8);

        // Draw text
        g2.setColor(foreground);
        g2.setFont(new Font("Arial", Font.BOLD, height * 3/5));

        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();

        g2.drawString(text,
                x + (width - textWidth) / 2,
                y + (height - textHeight) / 2 + fm.getAscent());

        g2.dispose();
    }

    @Override
    public int getIconWidth() {
        return width;
    }

    @Override
    public int getIconHeight() {
        return height;
    }
}