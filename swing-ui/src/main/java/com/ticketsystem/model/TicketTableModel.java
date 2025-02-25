package com.ticketsystem.model;

import javax.swing.table.AbstractTableModel;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TicketTableModel extends AbstractTableModel {
    private final String[] columnNames = {"ID", "Title", "Priority", "Category", "Status", "Created Date", "Last Updated"};
    private final Class<?>[] columnTypes = {Long.class, String.class, Priority.class, Category.class, Status.class, LocalDateTime.class, LocalDateTime.class};    private List<TicketDTO> tickets = new ArrayList<>();
    public void setTickets(List<TicketDTO> tickets) {
        this.tickets = tickets != null ? tickets : new ArrayList<>();
        fireTableDataChanged();
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
        return columnTypes[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex >= tickets.size()) {
            return null;
        }

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

    public TicketDTO getTicketAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < tickets.size()) {
            return tickets.get(rowIndex);
        }
        return null;
    }
}