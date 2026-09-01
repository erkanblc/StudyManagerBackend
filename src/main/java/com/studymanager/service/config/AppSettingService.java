package com.studymanager.service.config;

import com.studymanager.dto.request.AppSettingRequest;
import com.studymanager.dto.response.AppSettingResponse;
import com.studymanager.entity.config.AppSetting;
import com.studymanager.entity.user.User;
import com.studymanager.repository.config.AppSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppSettingService {

    public static final String KEY_MAX_SESSION_HOURS = "max.session.hours";
    public static final int MIN_ALLOWED = 6;
    public static final int MAX_ALLOWED = 24;

    private final AppSettingRepository repository;

    public AppSettingService(AppSettingRepository repository) {
        this.repository = repository;
    }

    public int getMaxSessionHours() {
        return repository.findBySettingKey(KEY_MAX_SESSION_HOURS)
                .map(s -> clamp(parseIntSafe(s.getSettingValue())))
                .orElse(MIN_ALLOWED);
    }

    public List<AppSettingResponse> getAll() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public AppSettingResponse getByKey(String key) {
        AppSetting setting = repository.findBySettingKey(key)
                .orElseThrow(() -> new RuntimeException("Setting not found: " + key));
        return toResponse(setting);
    }

    @Transactional
    public AppSettingResponse update(String key, AppSettingRequest request, User admin) {
        AppSetting setting = repository.findBySettingKey(key)
                .orElseThrow(() -> new RuntimeException("Setting not found: " + key));

        if (key.equals(KEY_MAX_SESSION_HOURS)) {
            int clamped = clamp(parseIntSafe(request.getValue()));
            setting.setSettingValue(String.valueOf(clamped));
        } else {
            setting.setSettingValue(request.getValue());
        }

        setting.setUpdatedAt(ZonedDateTime.now(ZoneOffset.UTC));
        setting.setUpdatedBy(resolveAdminDisplayName(admin));
        return toResponse(repository.save(setting));
    }

    public void initDefaultIfAbsent(String key, String defaultValue, String description) {
        if (repository.findBySettingKey(key).isEmpty()) {
            repository.save(new AppSetting(key, defaultValue, description));
        }
    }

    private AppSettingResponse toResponse(AppSetting s) {
        return new AppSettingResponse(
                s.getId(),
                s.getSettingKey(),
                s.getSettingValue(),
                s.getDescription(),
                s.getUpdatedAt(),
                s.getUpdatedBy()
        );
    }

    /** Prefer username; fallback fullName; otherwise null (empty on UI). */
    private static String resolveAdminDisplayName(User admin) {
        if (admin == null) {
            return null;
        }
        if (admin.getUsername() != null && !admin.getUsername().isBlank()) {
            return admin.getUsername();
        }
        if (admin.getFullName() != null && !admin.getFullName().isBlank()) {
            return admin.getFullName();
        }
        return null;
    }

    static int clamp(int value) {
        if (value < MIN_ALLOWED) return MIN_ALLOWED;
        if (value > MAX_ALLOWED) return MAX_ALLOWED;
        return value;
    }

    static int parseIntSafe(String value) {
        if (value == null || value.isBlank()) return MIN_ALLOWED;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return MIN_ALLOWED;
        }
    }
}
