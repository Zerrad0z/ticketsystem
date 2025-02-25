package com.ticketsystem.service;

import com.ticketsystem.model.Priority;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class PriorityCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {

        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (value instanceof Priority priority) {
            setText(priority.toString());
            setHorizontalAlignment(CENTER);

            if (!isSelected) {
                switch (priority) {
                    case HIGH -> setBackground(new Color(255, 200, 200));
                    case MEDIUM -> setBackground(new Color(255, 255, 200));
                    case LOW -> setBackground(new Color(200, 255, 200));
                    default -> setBackground(table.getBackground());
                }
            }
        }
        return c;
    }
}