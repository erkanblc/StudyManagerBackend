package com.studymanager.controller.goal;

import com.studymanager.dto.request.MilestoneRequest;
import com.studymanager.dto.response.MilestoneResponse;
import com.studymanager.entity.user.User;
import com.studymanager.service.goal.MilestoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Milestones", description = "User-scoped milestones — standalone or linked to a goal")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/milestones")
public class MilestoneController {

    private final MilestoneService milestoneService;

    public MilestoneController(MilestoneService milestoneService) {
        this.milestoneService = milestoneService;
    }

    @Operation(summary = "List my milestones")
    @GetMapping
    public ResponseEntity<List<MilestoneResponse>> getAll(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(milestoneService.getMyMilestones(currentUser.getId()));
    }

    @Operation(summary = "Get one milestone")
    @GetMapping("/{id}")
    public ResponseEntity<MilestoneResponse> getOne(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(milestoneService.getById(id, currentUser.getId()));
    }

    @Operation(summary = "Create milestone (goalId optional for standalone)")
    @PostMapping
    public ResponseEntity<MilestoneResponse> create(
            @AuthenticationPrincipal User currentUser,
            @RequestBody MilestoneRequest request) {
        return ResponseEntity.ok(milestoneService.create(currentUser.getId(), request));
    }

    @Operation(summary = "Update milestone")
    @PutMapping("/{id}")
    public ResponseEntity<MilestoneResponse> update(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser,
            @RequestBody MilestoneRequest request) {
        return ResponseEntity.ok(milestoneService.update(id, currentUser.getId(), request));
    }

    @Operation(summary = "Toggle completed")
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<MilestoneResponse> toggle(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(milestoneService.toggle(id, currentUser.getId()));
    }

    @Operation(summary = "Delete milestone")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        milestoneService.delete(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
