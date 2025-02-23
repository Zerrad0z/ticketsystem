package org.example.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TicketDTO {
    private Long id;
    private String title;
    private String description;
    private Priority priority;
    private Category category;
    private Status status;
    private LocalDateTime createdDate;
    private Long createdById;
    private List<CommentDTO> comments = new ArrayList<>();

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public Long getCreatedById() { return createdById; }
    public void setCreatedById(Long createdById) { this.createdById = createdById; }

    public List<CommentDTO> getComments() { return comments; }
    public void setComments(List<CommentDTO> comments) { this.comments = comments; }
}

