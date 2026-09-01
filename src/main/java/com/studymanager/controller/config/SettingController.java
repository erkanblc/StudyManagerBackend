package com.studymanager.controller.config;

import com.studymanager.service.config.AppSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Settings", description = "Public/authenticated app settings (read-only)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/settings")
public class SettingController {

    private final AppSettingService settingService;

    public SettingController(AppSettingService settingService) {
        this.settingService = settingService;
    }

    @Operation(summary = "Get maximum study session duration (hours)")
    @GetMapping("/max-session-hours")
    public ResponseEntity<Map<String, Object>> getMaxSessionHours() {
        int hours = settingService.getMaxSessionHours();
        return ResponseEntity.ok(Map.of(
                "key", AppSettingService.KEY_MAX_SESSION_HOURS,
                "value", String.valueOf(hours),
                "maxHours", hours,
                "maxMinutes", hours * 60,
                "maxSeconds", hours * 3600L
        ));
    }
}
