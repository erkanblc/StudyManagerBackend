package com.studymanager.dto.request;

import java.time.ZonedDateTime;

public class ManualSessionRequest {

    private Long goalId;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
    private Long duration;
    private String subject;
    private String notes;

    public ManualSessionRequest() {}

    public Long getGoalId() { return goalId; }
    public ZonedDateTime getStartTime() { return startTime; }
    public ZonedDateTime getEndTime() { return endTime; }
    public Long getDuration() { return duration; }
    public String getSubject() { return subject; }
    public String getNotes() { return notes; }

    public void setGoalId(Long goalId) { this.goalId = goalId; }
    public void setStartTime(ZonedDateTime startTime) { this.startTime = startTime; }
    public void setEndTime(ZonedDateTime endTime) { this.endTime = endTime; }
    public void setDuration(Long duration) { this.duration = duration; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setNotes(String notes) { this.notes = notes; }
}
