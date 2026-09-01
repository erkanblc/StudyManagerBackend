package com.studymanager.controller.config;

import com.studymanager.dto.request.AppSettingRequest;
import com.studymanager.dto.response.AppSettingResponse;
import com.studymanager.entity.user.User;
import com.studymanager.service.config.AppSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin – Settings", description = "Application-wide configuration. Admin only.")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('ADMIN')")
@RestController
@RequestMapping("/api/admin/settings")
public class AdminSettingController {

    private final AppSettingService settingService;

    public AdminSettingController(AppSettingService settingService) {
        this.settingService = settingService;
    }

    @Operation(
        summary = "List all settings",
        description = "Returns every app_settings row."
    )
    @GetMapping
    public ResponseEntity<List<AppSettingResponse>> getAll() {
        return ResponseEntity.ok(settingService.getAll());
    }

    @Operation(
        summary = "Get setting by key",
        description = "Example key: `max.session.hours`"
    )
    @GetMapping("/{key}")
    public ResponseEntity<AppSettingResponse> getByKey(@PathVariable String key) {
        return ResponseEntity.ok(settingService.getByKey(key));
    }

    @Operation(
        summary = "Update a setting",
        description = """
            Updates the setting identified by `key`.
            Stores `updatedBy` as the current admin username (null if unavailable).

            **`max.session.hours`** rules:
            - Value must be a whole number (no decimals).
            - Accepted range: **6 – 24** hours.
            - Values < 6 are clamped to **6**.
            - Values > 24 are clamped to **24**.
            - Decimal / non-numeric values fall back to the minimum (**6**).
            """
    )
    @PutMapping("/{key}")
    public ResponseEntity<AppSettingResponse> update(
            @PathVariable String key,
            @AuthenticationPrincipal User currentUser,
            @RequestBody AppSettingRequest request) {
        return ResponseEntity.ok(settingService.update(key, request, currentUser));
    }
}
