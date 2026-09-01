package com.studymanager.entity.study;

import com.studymanager.entity.user.User;
import jakarta.persistence.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Entity
@Table(name = "plan_sessions")
public class PlanSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column
    private Long goalId;

    @Column
    private String goalTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanSessionType type;

    @Column(nullable = false)
    private ZonedDateTime plannedDate;

    @Column(nullable = false)
    private Integer plannedDurationMinutes;

    @Column(length = 1000)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanSessionStatus status = PlanSessionStatus.PLANNED;

    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt = ZonedDateTime.now(ZoneOffset.UTC);

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public PlanSession() {}

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
    public User getUser() { return user; }

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
    public void setUser(User user) { this.user = user; }
}
