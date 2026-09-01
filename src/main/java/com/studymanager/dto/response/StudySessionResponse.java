package com.studymanager.dto.response;

import com.studymanager.entity.study.SessionStatus;

import java.time.ZonedDateTime;

public class StudySessionResponse {

    private Long id;
    private Long userId;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
    private Long duration;
    private Long goalId;
    private String subject;
    private String notes;
    private SessionStatus status;
    private ZonedDateTime lastHeartbeatAt;
    private ZonedDateTime date;

    public StudySessionResponse() {}

    public StudySessionResponse(Long id, Long userId, ZonedDateTime startTime, ZonedDateTime endTime,
                                Long duration, Long goalId, String subject,
                                String notes, SessionStatus status,
                                ZonedDateTime lastHeartbeatAt, ZonedDateTime date) {
        this.id = id;
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        this.goalId = goalId;
        this.subject = subject;
        this.notes = notes;
        this.status = status;
        this.lastHeartbeatAt = lastHeartbeatAt;
        this.date = date;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public ZonedDateTime getStartTime() { return startTime; }
    public ZonedDateTime getEndTime() { return endTime; }
    public Long getDuration() { return duration; }
    public Long getGoalId() { return goalId; }
    public String getSubject() { return subject; }
    public String getNotes() { return notes; }
    public SessionStatus getStatus() { return status; }
    public ZonedDateTime getLastHeartbeatAt() { return lastHeartbeatAt; }
    public ZonedDateTime getDate() { return date; }

    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setStartTime(ZonedDateTime startTime) { this.startTime = startTime; }
    public void setEndTime(ZonedDateTime endTime) { this.endTime = endTime; }
    public void setDuration(Long duration) { this.duration = duration; }
    public void setGoalId(Long goalId) { this.goalId = goalId; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setStatus(SessionStatus status) { this.status = status; }
    public void setLastHeartbeatAt(ZonedDateTime lastHeartbeatAt) { this.lastHeartbeatAt = lastHeartbeatAt; }
    public void setDate(ZonedDateTime date) { this.date = date; }
}
