package com.studymanager.dto.request;

public class StartSessionRequest {

    private Long goalId;
    private String subject;
    private String notes;

    public StartSessionRequest() {}

    public Long getGoalId() { return goalId; }
    public String getSubject() { return subject; }
    public String getNotes() { return notes; }

    public void setGoalId(Long goalId) { this.goalId = goalId; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setNotes(String notes) { this.notes = notes; }
}
