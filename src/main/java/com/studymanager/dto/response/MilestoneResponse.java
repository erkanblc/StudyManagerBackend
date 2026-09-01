package com.studymanager.dto.response;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public class MilestoneResponse {

    private Long id;
    private Long userId;
    private Long goalId;
    private String title;
    private String description;
    private LocalDate dueDate;
    private String type;
    private boolean completed;
    private ZonedDateTime completedAt;
    private ZonedDateTime createdAt;

    public MilestoneResponse() {}

    public MilestoneResponse(Long id, Long userId, Long goalId, String title, String description,
                             LocalDate dueDate, String type, boolean completed,
                             ZonedDateTime completedAt, ZonedDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.goalId = goalId;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.type = type;
        this.completed = completed;
        this.completedAt = completedAt;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getGoalId() { return goalId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDate getDueDate() { return dueDate; }
    public String getType() { return type; }
    public boolean isCompleted() { return completed; }
    public ZonedDateTime getCompletedAt() { return completedAt; }
    public ZonedDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setGoalId(Long goalId) { this.goalId = goalId; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setType(String type) { this.type = type; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public void setCompletedAt(ZonedDateTime completedAt) { this.completedAt = completedAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
}
