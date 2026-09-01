package com.studymanager.controller.goal;

import com.studymanager.dto.request.GoalRequest;
import com.studymanager.dto.request.MilestoneRequest;
import com.studymanager.dto.response.GoalResponse;
import com.studymanager.entity.user.User;
import com.studymanager.service.goal.GoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Goals", description = "Personal learning goal management")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    // ── GOAL CRUD ──────────────────────────────────────────────────────────────

    @Operation(summary = "List all my goals")
    @GetMapping
    public ResponseEntity<List<GoalResponse>> getAll(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(goalService.getMyGoals(currentUser.getId()));
    }

    @Operation(summary = "List my active goals", description = "For timer dropdown — only ACTIVE goals")
    @GetMapping("/active")
    public ResponseEntity<List<GoalResponse>> getActive(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(goalService.getMyActiveGoals(currentUser.getId()));
    }

    @Operation(summary = "Create a new goal")
    @PostMapping
    public ResponseEntity<GoalResponse> create(
            @AuthenticationPrincipal User currentUser,
            @RequestBody GoalRequest request) {
        return ResponseEntity.ok(goalService.createGoal(request, currentUser));
    }

    @Operation(summary = "Update goal", description = "title, description, status, startDate, endDate, targetHours can be changed")
    @PutMapping("/{id}")
    public ResponseEntity<GoalResponse> update(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser,
            @RequestBody GoalRequest request) {
        return ResponseEntity.ok(goalService.updateGoal(id, currentUser.getId(), request));
    }

    @Operation(summary = "Delete goal")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        goalService.deleteGoal(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    // ── MILESTONE CRUD ─────────────────────────────────────────────────────────

    @Operation(summary = "Add a milestone to a goal")
    @PostMapping("/{goalId}/milestones")
    public ResponseEntity<GoalResponse> addMilestone(
            @PathVariable Long goalId,
            @AuthenticationPrincipal User currentUser,
            @RequestBody MilestoneRequest request) {
        return ResponseEntity.ok(goalService.addMilestone(goalId, currentUser.getId(), request));
    }

    @Operation(summary = "Toggle milestone completion", description = "Marks as complete or incomplete. Returns updated goal with progress.")
    @PatchMapping("/{goalId}/milestones/{milestoneId}/toggle")
    public ResponseEntity<GoalResponse> toggleMilestone(
            @PathVariable Long goalId,
            @PathVariable Long milestoneId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(goalService.toggleMilestone(goalId, milestoneId, currentUser.getId()));
    }

    @Operation(summary = "Delete a milestone")
    @DeleteMapping("/{goalId}/milestones/{milestoneId}")
    public ResponseEntity<GoalResponse> deleteMilestone(
            @PathVariable Long goalId,
            @PathVariable Long milestoneId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(goalService.deleteMilestone(goalId, milestoneId, currentUser.getId()));
    }
}
