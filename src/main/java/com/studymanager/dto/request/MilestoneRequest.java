package com.studymanager.dto.request;

import java.time.LocalDate;

public class MilestoneRequest {

    private String title;
    private String description;
    private LocalDate dueDate;
    private String type;
    /** Optional; null or omitted = leave unchanged on update; send without goal for standalone create. */
    private Long goalId;
    private Boolean clearGoal;

    public MilestoneRequest() {}

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDate getDueDate() { return dueDate; }
    public String getType() { return type; }
    public Long getGoalId() { return goalId; }
    public Boolean getClearGoal() { return clearGoal; }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setType(String type) { this.type = type; }
    public void setGoalId(Long goalId) { this.goalId = goalId; }
    public void setClearGoal(Boolean clearGoal) { this.clearGoal = clearGoal; }
}
