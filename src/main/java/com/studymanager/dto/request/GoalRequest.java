package com.studymanager.dto.request;

import java.time.LocalDate;

public class GoalRequest {

    private String title;
    private String description;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double targetHours;

    public GoalRequest() {}

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public Double getTargetHours() { return targetHours; }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(String status) { this.status = status; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public void setTargetHours(Double targetHours) { this.targetHours = targetHours; }
}
