package com.studymanager.service.goal;

import com.studymanager.dto.request.GoalRequest;
import com.studymanager.dto.request.MilestoneRequest;
import com.studymanager.dto.response.AdminGoalResponse;
import com.studymanager.dto.response.GoalResponse;
import com.studymanager.dto.response.MilestoneResponse;
import com.studymanager.entity.goal.Goal;
import com.studymanager.entity.goal.GoalStatus;
import com.studymanager.entity.goal.Milestone;
import com.studymanager.entity.user.User;
import com.studymanager.repository.goal.GoalRepository;
import com.studymanager.repository.goal.MilestoneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GoalService {

    private final GoalRepository goalRepository;
    private final MilestoneRepository milestoneRepository;
    private final GoalOverdueService goalOverdueService;

    public GoalService(GoalRepository goalRepository,
                       MilestoneRepository milestoneRepository,
                       GoalOverdueService goalOverdueService) {
        this.goalRepository = goalRepository;
        this.milestoneRepository = milestoneRepository;
        this.goalOverdueService = goalOverdueService;
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    public List<GoalResponse> getMyGoals(Long userId) {
        goalOverdueService.syncOverdueGoalsForUser(userId);
        return goalRepository.findByCreatedByIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<GoalResponse> getMyActiveGoals(Long userId) {
        goalOverdueService.syncOverdueGoalsForUser(userId);
        return goalRepository.findByCreatedByIdAndStatusOrderByCreatedAtDesc(userId, GoalStatus.ACTIVE)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public GoalResponse getGoalById(Long id, Long userId) {
        goalOverdueService.syncOverdueGoalsForUser(userId);
        Goal goal = findGoalForUser(id, userId);
        return toResponse(goal);
    }

    // ── ADMIN READ ─────────────────────────────────────────────────────────────

    @Transactional
    public List<AdminGoalResponse> getAllGoalsAdmin() {
        goalOverdueService.syncOverdueGoals();
        return goalRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toAdminResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<AdminGoalResponse> getGoalsByUserAdmin(Long userId) {
        goalOverdueService.syncOverdueGoalsForUser(userId);
        return goalRepository.findByCreatedByIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toAdminResponse)
                .collect(Collectors.toList());
    }

    // ── CREATE ─────────────────────────────────────────────────────────────────

    @Transactional
    public GoalResponse createGoal(GoalRequest request, User user) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("Goal title cannot be empty");
        }
        validateTargetHours(request.getTargetHours());

        Goal goal = new Goal();
        goal.setTitle(request.getTitle().trim());
        goal.setDescription(request.getDescription());
        goal.setStartDate(request.getStartDate());
        goal.setEndDate(request.getEndDate());
        goal.setTargetHours(request.getTargetHours());
        goal.setCreatedBy(user);

        if (request.getStatus() != null) {
            goal.setStatus(GoalStatus.valueOf(request.getStatus().toUpperCase()));
        }

        return toResponse(goalRepository.save(goal));
    }

    // ── UPDATE ─────────────────────────────────────────────────────────────────

    @Transactional
    public GoalResponse updateGoal(Long id, Long userId, GoalRequest request) {
        Goal goal = findGoalForUser(id, userId);

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            goal.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            goal.setDescription(request.getDescription());
        }
        if (request.getStartDate() != null) {
            goal.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            goal.setEndDate(request.getEndDate());
        }
        if (request.getTargetHours() != null) {
            validateTargetHours(request.getTargetHours());
            goal.setTargetHours(request.getTargetHours());
        }

        // Apply status last so an explicit choice is never overwritten by date side-effects.
        // (Edit form always sends endDate; the old logic forced OVERDUE → ACTIVE whenever end was still future.)
        boolean statusProvided = request.getStatus() != null && !request.getStatus().isBlank();
        if (statusProvided) {
            goal.setStatus(GoalStatus.valueOf(request.getStatus().trim().toUpperCase()));
        } else if (goal.getStatus() == GoalStatus.OVERDUE
                && goal.getEndDate() != null
                && !goal.getEndDate().isBefore(LocalDate.now(ZoneOffset.UTC))) {
            goal.setStatus(GoalStatus.ACTIVE);
        }

        goal.setUpdatedAt(ZonedDateTime.now(ZoneOffset.UTC));

        return toResponse(goalRepository.save(goal));
    }

    private void validateTargetHours(Double targetHours) {
        if (targetHours != null && targetHours < 0) {
            throw new IllegalArgumentException("Target hours cannot be negative");
        }
    }

    // ── DELETE ─────────────────────────────────────────────────────────────────

    @Transactional
    public void deleteGoal(Long id, Long userId) {
        Goal goal = findGoalForUser(id, userId);
        // orphanRemoval clears linked milestones with the goal
        goal.getMilestones().clear();
        goalRepository.delete(goal);
    }

    // ── MILESTONE CRUD ─────────────────────────────────────────────────────────

    @Transactional
    public GoalResponse addMilestone(Long goalId, Long userId, MilestoneRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("Milestone title cannot be empty");
        }
        Goal goal = findGoalForUser(goalId, userId);
        assertUnderMilestoneLimit(goal.getId());

        Milestone milestone = new Milestone();
        milestone.setTitle(request.getTitle().trim());
        milestone.setDescription(request.getDescription() != null && !request.getDescription().isBlank()
                ? request.getDescription().trim() : null);
        milestone.setDueDate(request.getDueDate());
        milestone.setType(request.getType() != null && !request.getType().isBlank()
                ? request.getType().trim() : null);
        milestone.setGoal(goal);
        milestone.setUser(goal.getCreatedBy());
        goal.getMilestones().add(milestone);

        goal.setUpdatedAt(ZonedDateTime.now(ZoneOffset.UTC));
        return toResponse(goalRepository.save(goal));
    }

    @Transactional
    public GoalResponse toggleMilestone(Long goalId, Long milestoneId, Long userId) {
        findGoalForUser(goalId, userId);

        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new RuntimeException("Milestone not found: " + milestoneId));

        if (milestone.getGoal() == null || !milestone.getGoal().getId().equals(goalId)) {
            throw new RuntimeException("Milestone does not belong to this goal");
        }

        boolean nowCompleted = !milestone.isCompleted();
        milestone.setCompleted(nowCompleted);
        milestone.setCompletedAt(nowCompleted ? ZonedDateTime.now(ZoneOffset.UTC) : null);
        if (milestone.getUser() == null) {
            milestone.setUser(milestone.getGoal().getCreatedBy());
        }
        milestoneRepository.save(milestone);

        Goal goal = milestone.getGoal();
        goal.setUpdatedAt(ZonedDateTime.now(ZoneOffset.UTC));
        return toResponse(goalRepository.save(goal));
    }

    @Transactional
    public GoalResponse deleteMilestone(Long goalId, Long milestoneId, Long userId) {
        Goal goal = findGoalForUser(goalId, userId);

        boolean removed = goal.getMilestones().removeIf(m -> m.getId().equals(milestoneId));
        if (!removed) {
            throw new RuntimeException("Milestone not found: " + milestoneId);
        }

        goal.setUpdatedAt(ZonedDateTime.now(ZoneOffset.UTC));
        return toResponse(goalRepository.save(goal));
    }

    // ── HELPERS ────────────────────────────────────────────────────────────────

    private Goal findGoalForUser(Long goalId, Long userId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found: " + goalId));
        if (!goal.getCreatedBy().getId().equals(userId)) {
            throw new RuntimeException("Access denied: this goal belongs to another user");
        }
        return goal;
    }

    private void assertUnderMilestoneLimit(Long goalId) {
        MilestoneLimits.assertUnderLimit(milestoneRepository.countByGoal_Id(goalId));
    }

    private GoalResponse toResponse(Goal g) {
        List<MilestoneResponse> milestoneResponses = g.getMilestones().stream()
                .map(m -> new MilestoneResponse(
                        m.getId(),
                        m.getUser() != null
                                ? m.getUser().getId()
                                : (g.getCreatedBy() != null ? g.getCreatedBy().getId() : null),
                        g.getId(),
                        m.getTitle(),
                        m.getDescription(),
                        m.getDueDate(),
                        m.getType(),
                        m.isCompleted(),
                        m.getCompletedAt(),
                        m.getCreatedAt()
                ))
                .collect(Collectors.toList());

        int total = milestoneResponses.size();
        int completed = (int) milestoneResponses.stream().filter(MilestoneResponse::isCompleted).count();

        GoalResponse response = new GoalResponse();
        response.setId(g.getId());
        response.setTitle(g.getTitle());
        response.setDescription(g.getDescription());
        response.setStartDate(g.getStartDate());
        response.setEndDate(g.getEndDate());
        response.setTargetHours(g.getTargetHours());
        response.setStatus(g.getStatus());
        response.setCreatedAt(g.getCreatedAt());
        response.setUpdatedAt(g.getUpdatedAt());
        response.setCreatedByUsername(g.getCreatedBy() != null ? g.getCreatedBy().getUsername() : null);
        response.setMilestones(milestoneResponses);
        response.setMilestoneCount(total);
        response.setCompletedMilestoneCount(completed);
        return response;
    }

    private AdminGoalResponse toAdminResponse(Goal g) {
        User owner = g.getCreatedBy();
        return new AdminGoalResponse(
                g.getId(),
                g.getTitle(),
                g.getDescription(),
                g.getStatus(),
                g.getCreatedAt(),
                g.getUpdatedAt(),
                owner != null ? owner.getId() : null,
                owner != null ? owner.getEmail() : null,
                owner != null ? owner.getFullName() : null,
                owner != null ? owner.getUsername() : null
        );
    }
}
