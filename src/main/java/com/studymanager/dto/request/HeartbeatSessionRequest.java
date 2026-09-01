package com.studymanager.dto.request;

public class HeartbeatSessionRequest {

    private Long duration;    // required — frontend'in o ana kadar saydığı saniye
    private Long goalId;
    private String subject;
    private String notes;
    private Boolean isRunning;

    public HeartbeatSessionRequest() {}

    public Long getDuration() { return duration; }
    public Long getGoalId() { return goalId; }
    public String getSubject() { return subject; }
    public String getNotes() { return notes; }
    public Boolean getIsRunning() { return isRunning; }

    public void setDuration(Long duration) { this.duration = duration; }
    public void setGoalId(Long goalId) { this.goalId = goalId; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setIsRunning(Boolean isRunning) { this.isRunning = isRunning; }
}
