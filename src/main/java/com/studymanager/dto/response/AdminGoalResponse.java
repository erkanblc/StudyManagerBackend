package com.studymanager.dto.response;

import com.studymanager.entity.goal.GoalStatus;

import java.time.ZonedDateTime;

public class AdminGoalResponse {

    private Long id;
    private String title;
    private String description;
    private GoalStatus status;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private Long userId;
    private String userEmail;
    private String userFullName;
    private String createdByUsername;

    public AdminGoalResponse() {}

    public AdminGoalResponse(Long id, String title, String description, GoalStatus status,
                             ZonedDateTime createdAt, ZonedDateTime updatedAt,
                             Long userId, String userEmail, String userFullName,
                             String createdByUsername) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.userId = userId;
        this.userEmail = userEmail;
        this.userFullName = userFullName;
        this.createdByUsername = createdByUsername;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public GoalStatus getStatus() { return status; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public Long getUserId() { return userId; }
    public String getUserEmail() { return userEmail; }
    public String getUserFullName() { return userFullName; }
    public String getCreatedByUsername() { return createdByUsername; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(GoalStatus status) { this.status = status; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public void setUserFullName(String userFullName) { this.userFullName = userFullName; }
    public void setCreatedByUsername(String createdByUsername) { this.createdByUsername = createdByUsername; }
}
