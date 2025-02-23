package org.example.model;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

public class CommentDTO {
    private Long id;
    private String content;
    private LocalDateTime createdDate;
    private Long createdById;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public Long getCreatedById() { return createdById; }
    public void setCreatedById(Long createdById) { this.createdById = createdById; }
}