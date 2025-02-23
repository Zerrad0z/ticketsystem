package org.example.model;


import java.time.LocalDateTime;



public class AuditLogDTO {
    private Long id;
    private Long ticketId;
    private String action;
    private String oldValue;
    private String newValue;
    private Long performedById;
    private LocalDateTime createdDate;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public Long getPerformedById() { return performedById; }
    public void setPerformedById(Long performedById) { this.performedById = performedById; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
}