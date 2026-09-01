package com.studymanager.dto.request;

import java.time.ZonedDateTime;

public class UpdateSessionRequest {

    private Long goalId;
    private String subject;
    private String notes;
    private Long duration;
    private ZonedDateTime startTime;

    public UpdateSessionRequest() {}

    public Long getGoalId() { return goalId; }
    public String getSubject() { return subject; }
    public String getNotes() { return notes; }
    public Long getDuration() { return duration; }
    public ZonedDateTime getStartTime() { return startTime; }

    public void setGoalId(Long goalId) { this.goalId = goalId; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setDuration(Long duration) { this.duration = duration; }
    public void setStartTime(ZonedDateTime startTime) { this.startTime = startTime; }
}
