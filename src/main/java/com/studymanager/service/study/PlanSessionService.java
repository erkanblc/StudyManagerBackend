package com.studymanager.service.study;

import com.studymanager.dto.request.PlanSessionRequest;
import com.studymanager.dto.response.PlanSessionResponse;
import com.studymanager.entity.study.PlanSession;
import com.studymanager.entity.study.PlanSessionStatus;
import com.studymanager.entity.study.PlanSessionType;
import com.studymanager.entity.user.User;
import com.studymanager.repository.study.PlanSessionRepository;
import com.studymanager.repository.user.UserRepository;
import com.studymanager.service.config.AppSettingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlanSessionService {

    private final PlanSessionRepository planSessionRepository;
    private final UserRepository userRepository;
    private final AppSettingService appSettingService;

    public PlanSessionService(PlanSessionRepository planSessionRepository,
                              UserRepository userRepository,
                              AppSettingService appSettingService) {
        this.planSessionRepository = planSessionRepository;
        this.userRepository = userRepository;
        this.appSettingService = appSettingService;
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    public List<PlanSessionResponse> getAllPlans(Long userId) {
        return planSessionRepository.findByUserIdOrderByPlannedDateAsc(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<PlanSessionResponse> getTodayPlans(Long userId) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        ZonedDateTime startOfDay = today.atStartOfDay(ZoneOffset.UTC);
        ZonedDateTime endOfDay   = today.plusDays(1).atStartOfDay(ZoneOffset.UTC).minusNanos(1);
        return planSessionRepository
                .findByUserIdAndPlannedDateBetweenOrderByPlannedDateAsc(userId, startOfDay, endOfDay)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public PlanSessionResponse getPlanById(Long id, Long userId) {
        return toResponse(findPlanForUser(id, userId));
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Transactional
    public PlanSessionResponse createPlan(PlanSessionRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        validateRequest(request);

        PlanSession plan = new PlanSession();
        plan.setUser(user);
        plan.setTitle(request.getTitle().trim());
        plan.setGoalId(request.getGoalId());
        plan.setGoalTitle(request.getGoalTitle());
        plan.setType(PlanSessionType.valueOf(request.getType().toUpperCase()));
        plan.setPlannedDate(request.getPlannedDate());
        plan.setPlannedDurationMinutes(request.getPlannedDurationMinutes());
        plan.setNotes(request.getNotes());
        plan.setStatus(PlanSessionStatus.PLANNED);

        return toResponse(planSessionRepository.save(plan));
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    @Transactional
    public PlanSessionResponse updatePlan(Long id, Long userId, PlanSessionRequest request) {
        PlanSession plan = findPlanForUser(id, userId);

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            plan.setTitle(request.getTitle().trim());
        }
        if (request.getGoalId() != null)     plan.setGoalId(request.getGoalId());
        if (request.getGoalTitle() != null)  plan.setGoalTitle(request.getGoalTitle());
        if (request.getType() != null)       plan.setType(PlanSessionType.valueOf(request.getType().toUpperCase()));
        if (request.getPlannedDate() != null) plan.setPlannedDate(request.getPlannedDate());
        if (request.getPlannedDurationMinutes() != null) {
            int maxMinutes = appSettingService.getMaxSessionHours() * 60;
            if (request.getPlannedDurationMinutes() <= 0) {
                throw new IllegalArgumentException("Planned duration must be greater than 0");
            }
            if (request.getPlannedDurationMinutes() > maxMinutes) {
                throw new IllegalArgumentException(
                        "Planned duration exceeds the maximum allowed limit of "
                        + appSettingService.getMaxSessionHours() + " hour(s).");
            }
            plan.setPlannedDurationMinutes(request.getPlannedDurationMinutes());
        }
        if (request.getNotes() != null)      plan.setNotes(request.getNotes());

        return toResponse(planSessionRepository.save(plan));
    }

    // ── PATCH: complete ───────────────────────────────────────────────────────

    @Transactional
    public PlanSessionResponse markCompleted(Long id, Long userId) {
        PlanSession plan = findPlanForUser(id, userId);
        plan.setStatus(PlanSessionStatus.COMPLETED);
        return toResponse(planSessionRepository.save(plan));
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @Transactional
    public void deletePlan(Long id, Long userId) {
        PlanSession plan = findPlanForUser(id, userId);
        planSessionRepository.delete(plan);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private PlanSession findPlanForUser(Long planId, Long userId) {
        PlanSession plan = planSessionRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("PlanSession not found: " + planId));
        if (!plan.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied: this plan belongs to another user");
        }
        return plan;
    }

    private void validateRequest(PlanSessionRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (request.getType() == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }
        if (request.getPlannedDate() == null) {
            throw new IllegalArgumentException("Planned date cannot be null");
        }
        if (request.getPlannedDurationMinutes() == null || request.getPlannedDurationMinutes() <= 0) {
            throw new IllegalArgumentException("Planned duration must be greater than 0");
        }
        int maxMinutes = appSettingService.getMaxSessionHours() * 60;
        if (request.getPlannedDurationMinutes() > maxMinutes) {
            throw new IllegalArgumentException(
                    "Planned duration exceeds the maximum allowed limit of "
                    + appSettingService.getMaxSessionHours() + " hour(s).");
        }
    }

    private PlanSessionResponse toResponse(PlanSession p) {
        return new PlanSessionResponse(
                p.getId(),
                p.getTitle(),
                p.getGoalId(),
                p.getGoalTitle(),
                p.getType(),
                p.getPlannedDate(),
                p.getPlannedDurationMinutes(),
                p.getNotes(),
                p.getStatus(),
                p.getCreatedAt()
        );
    }
}
