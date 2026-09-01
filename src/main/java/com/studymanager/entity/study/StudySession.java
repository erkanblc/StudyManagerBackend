package com.studymanager.entity.study;

import com.studymanager.entity.user.User;
import jakarta.persistence.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Entity
@Table(name = "study_sessions")
public class StudySession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private ZonedDateTime startTime;

    @Column
    private ZonedDateTime endTime;

    @Column
    private Long durationSeconds;

    @Column
    private Long goalId;

    @Column
    private String subject;

    @Column(length = 1000)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    @Column
    private ZonedDateTime lastHeartbeatAt;

    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt = ZonedDateTime.now(ZoneOffset.UTC);

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public StudySession() {}

    public Long getId() { return id; }
    public ZonedDateTime getStartTime() { return startTime; }
    public ZonedDateTime getEndTime() { return endTime; }
    public Long getDurationSeconds() { return durationSeconds; }
    public Long getGoalId() { return goalId; }
    public String getSubject() { return subject; }
    public String getNotes() { return notes; }
    public SessionStatus getStatus() { return status; }
    public ZonedDateTime getLastHeartbeatAt() { return lastHeartbeatAt; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public User getUser() { return user; }

    public void setId(Long id) { this.id = id; }
    public void setStartTime(ZonedDateTime startTime) { this.startTime = startTime; }
    public void setEndTime(ZonedDateTime endTime) { this.endTime = endTime; }
    public void setDurationSeconds(Long durationSeconds) { this.durationSeconds = durationSeconds; }
    public void setGoalId(Long goalId) { this.goalId = goalId; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setStatus(SessionStatus status) { this.status = status; }
    public void setLastHeartbeatAt(ZonedDateTime lastHeartbeatAt) { this.lastHeartbeatAt = lastHeartbeatAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
    public void setUser(User user) { this.user = user; }
}
