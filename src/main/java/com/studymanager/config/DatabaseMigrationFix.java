package com.studymanager.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseMigrationFix {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseMigrationFix(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void fixLegacyData() {
        try {
            jdbcTemplate.update(
                    "UPDATE users SET created_at = NOW() " +
                    "WHERE created_at IS NULL OR created_at = '0000-00-00 00:00:00'"
            );
            jdbcTemplate.update(
                    "UPDATE users SET admin_approval_status = 'NONE' WHERE admin_approval_status IS NULL"
            );
            jdbcTemplate.update(
                    "UPDATE users u SET u.admin_approval_status = 'APPROVED' " +
                    "WHERE u.admin_approval_status = 'NONE' AND u.active = 1 " +
                    "AND EXISTS (" +
                    "  SELECT 1 FROM user_roles ur " +
                    "  JOIN roles r ON ur.role_id = r.id " +
                    "  WHERE ur.user_id = u.id AND r.name = 'ADMIN'" +
                    ")"
            );
        } catch (Exception ignored) {
            // Safe to ignore on first boot before schema exists
        }

        // Hibernate ddl-auto=update does not widen existing columns; PAUSED/OVERDUE/CANCELLED
        // need more than a legacy tiny status column (Data truncated for column 'status').
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE goals MODIFY COLUMN status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE'"
            );
        } catch (Exception ignored) {
            // Table may not exist yet on first boot
        }
    }
}