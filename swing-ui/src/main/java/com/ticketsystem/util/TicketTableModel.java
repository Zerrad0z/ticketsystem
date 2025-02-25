package com.ticketsystem.util;

import com.ticketsystem.model.*;

import javax.swing.table.AbstractTableModel;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Inner class for ticket table model
public class TicketTableModel extends AbstractTableModel {
    private final String[] columnNames = {
            "ID", "Title", "Priority", "Category", "Status", "Created Date", "Last Updated"
    };

    private List<TicketDTO> tickets;

    public TicketTableModel() {
        this.tickets = new ArrayList<>();
    }

    public void setTickets(List<TicketDTO> tickets) {
        this.tickets = tickets == null ? new ArrayList<>() : tickets;
        fireTableDataChanged();
    }

    public TicketDTO getTicketAt(int row) {
        return tickets.get(row);
    }

    @Override
    public int getRowCount() {
        return tickets.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return Long.class;
        } else if (columnIndex == 2) {
            return Priority.class;
        } else if (columnIndex == 3) {
            return Category.class;
        } else if (columnIndex == 4) {
            return Status.class;
        } else if (columnIndex == 5 || columnIndex == 6) {
            return LocalDateTime.class;
        }
        return String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        TicketDTO ticket = tickets.get(rowIndex);

        return switch (columnIndex) {
            case 0 -> ticket.getId();
            case 1 -> ticket.getTitle();
            case 2 -> ticket.getPriority();
            case 3 -> ticket.getCategory();
            case 4 -> ticket.getStatus();
            case 5 -> ticket.getCreatedDate();
            case 6 -> ticket.getLastUpdated();
            default -> null;
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}