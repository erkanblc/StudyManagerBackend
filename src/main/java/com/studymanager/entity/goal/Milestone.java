package com.studymanager.entity.goal;

import com.studymanager.entity.user.User;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Entity
@Table(name = "milestones")
public class Milestone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column
    private LocalDate dueDate;

    @Column(length = 50)
    private String type;

    @Column(nullable = false)
    private boolean completed = false;

    @Column
    private ZonedDateTime completedAt;

    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt = ZonedDateTime.now(ZoneOffset.UTC);

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id")
    private Goal goal;

    public Milestone() {}

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDate getDueDate() { return dueDate; }
    public String getType() { return type; }
    public boolean isCompleted() { return completed; }
    public ZonedDateTime getCompletedAt() { return completedAt; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public User getUser() { return user; }
    public Goal getGoal() { return goal; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setType(String type) { this.type = type; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public void setCompletedAt(ZonedDateTime completedAt) { this.completedAt = completedAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
    public void setUser(User user) { this.user = user; }
    public void setGoal(Goal goal) { this.goal = goal; }
}
