package com.studymanager.dto.request;

import java.time.ZonedDateTime;

public class PlanSessionRequest {

    private String title;
    private Long goalId;
    private String goalTitle;
    private String type;
    private ZonedDateTime plannedDate;
    private Integer plannedDurationMinutes;
    private String notes;

    public PlanSessionRequest() {}

    public String getTitle() { return title; }
    public Long getGoalId() { return goalId; }
    public String getGoalTitle() { return goalTitle; }
    public String getType() { return type; }
    public ZonedDateTime getPlannedDate() { return plannedDate; }
    public Integer getPlannedDurationMinutes() { return plannedDurationMinutes; }
    public String getNotes() { return notes; }

    public void setTitle(String title) { this.title = title; }
    public void setGoalId(Long goalId) { this.goalId = goalId; }
    public void setGoalTitle(String goalTitle) { this.goalTitle = goalTitle; }
    public void setType(String type) { this.type = type; }
    public void setPlannedDate(ZonedDateTime plannedDate) { this.plannedDate = plannedDate; }
    public void setPlannedDurationMinutes(Integer plannedDurationMinutes) { this.plannedDurationMinutes = plannedDurationMinutes; }
    public void setNotes(String notes) { this.notes = notes; }
}
