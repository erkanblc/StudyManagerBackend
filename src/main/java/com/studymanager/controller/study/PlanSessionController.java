package com.studymanager.controller.study;

import com.studymanager.dto.request.PlanSessionRequest;
import com.studymanager.dto.response.PlanSessionResponse;
import com.studymanager.entity.user.User;
import com.studymanager.service.study.PlanSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Plan Sessions", description = "Ders planlama — kullanıcıya özel plan oluşturma ve yönetimi")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('STUDENT')")
@RestController
@RequestMapping("/api/plan-sessions")
public class PlanSessionController {

    private final PlanSessionService planSessionService;

    public PlanSessionController(PlanSessionService planSessionService) {
        this.planSessionService = planSessionService;
    }

    @Operation(summary = "Tüm planlarımı listele")
    @GetMapping
    public ResponseEntity<List<PlanSessionResponse>> getAll(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(planSessionService.getAllPlans(currentUser.getId()));
    }

    @Operation(summary = "Bugünkü planlarım", description = "Planlanan tarihi bugün olan tüm planlar")
    @GetMapping("/today")
    public ResponseEntity<List<PlanSessionResponse>> getToday(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(planSessionService.getTodayPlans(currentUser.getId()));
    }

    @Operation(summary = "ID ile plan getir")
    @GetMapping("/{id}")
    public ResponseEntity<PlanSessionResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(planSessionService.getPlanById(id, currentUser.getId()));
    }

    @Operation(summary = "Yeni plan oluştur", description = "status otomatik PLANNED olarak set edilir")
    @PostMapping
    public ResponseEntity<PlanSessionResponse> create(
            @AuthenticationPrincipal User currentUser,
            @RequestBody PlanSessionRequest request) {
        return ResponseEntity.ok(planSessionService.createPlan(request, currentUser.getId()));
    }

    @Operation(summary = "Plan güncelle", description = "Null gönderilen alanlar değiştirilmez")
    @PutMapping("/{id}")
    public ResponseEntity<PlanSessionResponse> update(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser,
            @RequestBody PlanSessionRequest request) {
        return ResponseEntity.ok(planSessionService.updatePlan(id, currentUser.getId(), request));
    }

    @Operation(summary = "Planı tamamlandı işaretle", description = "status → COMPLETED")
    @PatchMapping("/{id}/complete")
    public ResponseEntity<PlanSessionResponse> complete(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(planSessionService.markCompleted(id, currentUser.getId()));
    }

    @Operation(summary = "Plan sil")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        planSessionService.deletePlan(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
