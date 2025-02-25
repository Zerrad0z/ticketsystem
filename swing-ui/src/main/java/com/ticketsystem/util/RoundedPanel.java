package com.ticketsystem.util;

import javax.swing.*;
import java.awt.*;

class RoundedPanel extends JPanel {
    private final int cornerRadius;
    private boolean drawShadow;
    private int shadowSize = 5;

    public RoundedPanel(int radius, boolean shadow) {
        super();
        this.cornerRadius = radius;
        this.drawShadow = shadow;
        setOpaque(false);
    }

    public RoundedPanel(int radius, boolean shadow, LayoutManager layout) {
        super(layout);
        this.cornerRadius = radius;
        this.drawShadow = shadow;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw shadow
        if (drawShadow) {
            g2.setColor(new Color(0, 0, 0, 50));
            for (int i = 0; i < shadowSize; i++) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
                g2.fillRoundRect(i, i, getWidth() - i*2, getHeight() - i*2, cornerRadius, cornerRadius);
            }
        }

        // Draw panel background
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

        g2.dispose();
    }

    public void setShadowSize(int size) {
        this.shadowSize = size;
        repaint();
    }

    public void setDrawShadow(boolean draw) {
        this.drawShadow = draw;
        repaint();
    }
}