package com.ticketsystem.util;

import javax.swing.*;
import java.awt.*;

class CustomListCellRenderer extends DefaultListCellRenderer {
    private final Font font;
    private final Color textColor;
    private final Color backgroundColor;
    private final Color selectionColor;

    public CustomListCellRenderer(Font font, Color textColor, Color backgroundColor, Color selectionColor) {
        this.font = font;
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
        this.selectionColor = selectionColor;
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        label.setFont(font);

        if (isSelected) {
            label.setBackground(selectionColor);
            label.setForeground(Color.WHITE);
        } else {
            label.setBackground(backgroundColor);
            label.setForeground(textColor);
        }

        label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        return label;
    }
}