package com.studymanager.entity.goal;

import com.studymanager.entity.user.User;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "goals")
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    @Column
    private Double targetHours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private GoalStatus status = GoalStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt = ZonedDateTime.now(ZoneOffset.UTC);

    @Column
    private ZonedDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    /**
     * Linked milestones (goal_id set). Standalone milestones use goal = null.
     * orphanRemoval ensures removing from this list actually deletes the row
     * (plain CascadeType.ALL alone re-persists deleted children on goal save).
     */
    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<Milestone> milestones = new ArrayList<>();

    public Goal() {}

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public Double getTargetHours() { return targetHours; }
    public GoalStatus getStatus() { return status; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public User getCreatedBy() { return createdBy; }
    public List<Milestone> getMilestones() { return milestones; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public void setTargetHours(Double targetHours) { this.targetHours = targetHours; }
    public void setStatus(GoalStatus status) { this.status = status; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public void setMilestones(List<Milestone> milestones) { this.milestones = milestones; }
}
