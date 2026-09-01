package com.studymanager.dto.response;

import com.studymanager.entity.study.PlanSessionStatus;
import com.studymanager.entity.study.PlanSessionType;

import java.time.ZonedDateTime;

public class PlanSessionResponse {

    private Long id;
    private String title;
    private Long goalId;
    private String goalTitle;
    private PlanSessionType type;
    private ZonedDateTime plannedDate;
    private Integer plannedDurationMinutes;
    private String notes;
    private PlanSessionStatus status;
    private ZonedDateTime createdAt;

    public PlanSessionResponse() {}

    public PlanSessionResponse(Long id, String title, Long goalId, String goalTitle,
                               PlanSessionType type, ZonedDateTime plannedDate,
                               Integer plannedDurationMinutes, String notes,
                               PlanSessionStatus status, ZonedDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.goalId = goalId;
        this.goalTitle = goalTitle;
        this.type = type;
        this.plannedDate = plannedDate;
        this.plannedDurationMinutes = plannedDurationMinutes;
        this.notes = notes;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public Long getGoalId() { return goalId; }
    public String getGoalTitle() { return goalTitle; }
    public PlanSessionType getType() { return type; }
    public ZonedDateTime getPlannedDate() { return plannedDate; }
    public Integer getPlannedDurationMinutes() { return plannedDurationMinutes; }
    public String getNotes() { return notes; }
    public PlanSessionStatus getStatus() { return status; }
    public ZonedDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setGoalId(Long goalId) { this.goalId = goalId; }
    public void setGoalTitle(String goalTitle) { this.goalTitle = goalTitle; }
    public void setType(PlanSessionType type) { this.type = type; }
    public void setPlannedDate(ZonedDateTime plannedDate) { this.plannedDate = plannedDate; }
    public void setPlannedDurationMinutes(Integer plannedDurationMinutes) { this.plannedDurationMinutes = plannedDurationMinutes; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setStatus(PlanSessionStatus status) { this.status = status; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
}
