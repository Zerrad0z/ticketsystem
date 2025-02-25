package com.ticketsystem.util;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

class CustomTableCellRenderer extends DefaultTableCellRenderer {
    private final Font font;
    private final Color textColor;
    private final boolean centerText;

    public CustomTableCellRenderer(Font font, Color textColor, boolean centerText) {
        this.font = font;
        this.textColor = textColor;
        this.centerText = centerText;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (c instanceof JLabel label) {
            label.setFont(font);

            if (!isSelected) {
                label.setForeground(textColor);
                label.setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 249, 249));
            }

            if (centerText) {
                label.setHorizontalAlignment(SwingConstants.CENTER);
            }

            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
                    BorderFactory.createEmptyBorder(0, 10, 0, 10)
            ));
        }

        return c;
    }
}