package com.studymanager.service.goal;

import com.studymanager.dto.request.MilestoneRequest;
import com.studymanager.dto.response.MilestoneResponse;
import com.studymanager.entity.goal.Goal;
import com.studymanager.entity.goal.Milestone;
import com.studymanager.entity.user.User;
import com.studymanager.repository.goal.GoalRepository;
import com.studymanager.repository.goal.MilestoneRepository;
import com.studymanager.repository.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MilestoneService {

    private final MilestoneRepository milestoneRepository;
    private final GoalRepository goalRepository;
    private final UserRepository userRepository;

    public MilestoneService(MilestoneRepository milestoneRepository,
                            GoalRepository goalRepository,
                            UserRepository userRepository) {
        this.milestoneRepository = milestoneRepository;
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void backfillUserIds() {
        for (Milestone m : milestoneRepository.findByUserIsNull()) {
            if (m.getGoal() != null && m.getGoal().getCreatedBy() != null) {
                m.setUser(m.getGoal().getCreatedBy());
                milestoneRepository.save(m);
            }
        }
    }

    public List<MilestoneResponse> getMyMilestones(Long userId) {
        return milestoneRepository.findAllForUser(userId).stream()
                .sorted(Comparator
                        .comparing(Milestone::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Milestone::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public MilestoneResponse getById(Long id, Long userId) {
        return toResponse(findForUser(id, userId));
    }

    @Transactional
    public MilestoneResponse create(Long userId, MilestoneRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("Milestone title cannot be empty");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        Milestone milestone = new Milestone();
        milestone.setUser(user);
        milestone.setTitle(request.getTitle().trim());
        milestone.setDescription(blankToNull(request.getDescription()));
        milestone.setDueDate(request.getDueDate());
        milestone.setType(blankToNull(request.getType()));
        Goal goal = resolveGoal(request.getGoalId(), userId);
        if (goal != null) {
            assertUnderMilestoneLimit(goal.getId());
        }
        milestone.setGoal(goal);

        return toResponse(milestoneRepository.save(milestone));
    }

    @Transactional
    public MilestoneResponse update(Long id, Long userId, MilestoneRequest request) {
        Milestone milestone = findForUser(id, userId);

        if (request.getTitle() != null) {
            if (request.getTitle().isBlank()) {
                throw new IllegalArgumentException("Milestone title cannot be empty");
            }
            milestone.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            milestone.setDescription(blankToNull(request.getDescription()));
        }
        if (request.getDueDate() != null) {
            milestone.setDueDate(request.getDueDate());
        }
        if (request.getType() != null) {
            milestone.setType(blankToNull(request.getType()));
        }

        if (Boolean.TRUE.equals(request.getClearGoal())) {
            milestone.setGoal(null);
        } else if (request.getGoalId() != null) {
            Goal newGoal = resolveGoal(request.getGoalId(), userId);
            Long currentGoalId = milestone.getGoal() != null ? milestone.getGoal().getId() : null;
            if (newGoal != null && (currentGoalId == null || !currentGoalId.equals(newGoal.getId()))) {
                assertUnderMilestoneLimit(newGoal.getId());
            }
            milestone.setGoal(newGoal);
        }

        if (milestone.getUser() == null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));
            milestone.setUser(user);
        }

        return toResponse(milestoneRepository.save(milestone));
    }

    @Transactional
    public MilestoneResponse toggle(Long id, Long userId) {
        Milestone milestone = findForUser(id, userId);
        boolean nowCompleted = !milestone.isCompleted();
        milestone.setCompleted(nowCompleted);
        milestone.setCompletedAt(nowCompleted ? ZonedDateTime.now(ZoneOffset.UTC) : null);

        if (milestone.getUser() == null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));
            milestone.setUser(user);
        }

        Goal goal = milestone.getGoal();
        if (goal != null) {
            goal.setUpdatedAt(ZonedDateTime.now(ZoneOffset.UTC));
            goalRepository.save(goal);
        }

        return toResponse(milestoneRepository.save(milestone));
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Milestone milestone = findForUser(id, userId);
        Goal goal = milestone.getGoal();
        if (goal != null) {
            goal.getMilestones().removeIf(m -> m.getId().equals(id));
            goal.setUpdatedAt(ZonedDateTime.now(ZoneOffset.UTC));
            goalRepository.save(goal);
        } else {
            milestoneRepository.delete(milestone);
        }
    }

    private Milestone findForUser(Long id, Long userId) {
        return milestoneRepository.findByIdForUser(id, userId)
                .orElseThrow(() -> new RuntimeException("Milestone not found or access denied: " + id));
    }

    private Goal resolveGoal(Long goalId, Long userId) {
        if (goalId == null) return null;
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found: " + goalId));
        if (goal.getCreatedBy() == null || !goal.getCreatedBy().getId().equals(userId)) {
            throw new RuntimeException("Access denied: this goal belongs to another user");
        }
        return goal;
    }

    private void assertUnderMilestoneLimit(Long goalId) {
        MilestoneLimits.assertUnderLimit(milestoneRepository.countByGoal_Id(goalId));
    }

    private String blankToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public MilestoneResponse toResponse(Milestone m) {
        Long userId = m.getUser() != null
                ? m.getUser().getId()
                : (m.getGoal() != null && m.getGoal().getCreatedBy() != null
                        ? m.getGoal().getCreatedBy().getId()
                        : null);
        Long goalId = m.getGoal() != null ? m.getGoal().getId() : null;
        return new MilestoneResponse(
                m.getId(),
                userId,
                goalId,
                m.getTitle(),
                m.getDescription(),
                m.getDueDate(),
                m.getType(),
                m.isCompleted(),
                m.getCompletedAt(),
                m.getCreatedAt()
        );
    }
}
