package com.studymanager.controller.user;

import com.studymanager.dto.request.LoginHistoryRequest;
import com.studymanager.dto.response.LoginHistoryResponse;
import com.studymanager.service.user.LoginHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Login History", description = "Admin — user login history management")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('ADMIN')")
@RestController
@RequestMapping("/api/admin/login-history")
public class LoginHistoryController {

    private final LoginHistoryService loginHistoryService;

    public LoginHistoryController(LoginHistoryService loginHistoryService) {
        this.loginHistoryService = loginHistoryService;
    }

    @Operation(summary = "Get login history of a user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LoginHistoryResponse>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(loginHistoryService.getMyHistory(userId));
    }

    @Operation(summary = "Get single login record by ID")
    @GetMapping("/{id}")
    public ResponseEntity<LoginHistoryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(loginHistoryService.getByIdAdmin(id));
    }

    @Operation(summary = "Update login record timestamp")
    @PutMapping("/{id}")
    public ResponseEntity<LoginHistoryResponse> update(
            @PathVariable Long id,
            @RequestBody LoginHistoryRequest request) {
        return ResponseEntity.ok(loginHistoryService.updateAdmin(id, request));
    }

    @Operation(summary = "Delete single login record")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        loginHistoryService.deleteAdmin(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete all login history of a user")
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteAll(@PathVariable Long userId) {
        loginHistoryService.deleteAll(userId);
        return ResponseEntity.noContent().build();
    }
}
