package com.studymanager.controller.admin;

import com.studymanager.dto.response.PendingAdminResponse;
import com.studymanager.service.user.AdminApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Admin Approvals", description = "Admin — pending admin registration approvals")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('ADMIN')")
@RestController
@RequestMapping("/api/admin/approvals")
public class AdminApprovalController {

    private final AdminApprovalService adminApprovalService;

    public AdminApprovalController(AdminApprovalService adminApprovalService) {
        this.adminApprovalService = adminApprovalService;
    }

    @Operation(summary = "List pending admin registrations")
    @GetMapping("/pending")
    public ResponseEntity<List<PendingAdminResponse>> getPending() {
        return ResponseEntity.ok(adminApprovalService.getPendingAdmins());
    }

    @Operation(summary = "Count pending admin registrations")
    @GetMapping("/pending/count")
    public ResponseEntity<Map<String, Long>> countPending() {
        return ResponseEntity.ok(Map.of("count", adminApprovalService.countPendingAdmins()));
    }

    @Operation(summary = "Approve admin registration")
    @PostMapping("/{userId}/approve")
    public ResponseEntity<PendingAdminResponse> approve(@PathVariable Long userId) {
        return ResponseEntity.ok(adminApprovalService.approve(userId));
    }

    @Operation(summary = "Reject admin registration")
    @PostMapping("/{userId}/reject")
    public ResponseEntity<PendingAdminResponse> reject(@PathVariable Long userId) {
        return ResponseEntity.ok(adminApprovalService.reject(userId));
    }
}
