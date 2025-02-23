package org.example.service;

import org.example.model.Status;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class StatusCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {

        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (value instanceof Status status) {
            setText(status.toString());
            setHorizontalAlignment(CENTER);

            if (!isSelected) {
                switch (status) {
                    case NEW -> setBackground(new Color(200, 200, 255));
                    case IN_PROGRESS -> setBackground(new Color(255, 255, 200));
                    case RESOLVED -> setBackground(new Color(200, 255, 200));
                    default -> setBackground(table.getBackground());
                }
            }
        }
        return c;
    }
}