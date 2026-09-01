package com.studymanager.dto.response;

import com.studymanager.entity.goal.GoalStatus;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

public class GoalResponse {

    private Long id;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double targetHours;
    private GoalStatus status;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private String createdByUsername;
    private List<MilestoneResponse> milestones;
    private int milestoneCount;
    private int completedMilestoneCount;

    public GoalResponse() {}

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public Double getTargetHours() { return targetHours; }
    public GoalStatus getStatus() { return status; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public String getCreatedByUsername() { return createdByUsername; }
    public List<MilestoneResponse> getMilestones() { return milestones; }
    public int getMilestoneCount() { return milestoneCount; }
    public int getCompletedMilestoneCount() { return completedMilestoneCount; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public void setTargetHours(Double targetHours) { this.targetHours = targetHours; }
    public void setStatus(GoalStatus status) { this.status = status; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setCreatedByUsername(String createdByUsername) { this.createdByUsername = createdByUsername; }
    public void setMilestones(List<MilestoneResponse> milestones) { this.milestones = milestones; }
    public void setMilestoneCount(int milestoneCount) { this.milestoneCount = milestoneCount; }
    public void setCompletedMilestoneCount(int completedMilestoneCount) { this.completedMilestoneCount = completedMilestoneCount; }
}
