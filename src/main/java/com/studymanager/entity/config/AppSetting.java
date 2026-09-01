package com.studymanager.entity.config;

import jakarta.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "app_settings")
public class AppSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String settingKey;

    @Column(nullable = false)
    private String settingValue;

    @Column(length = 500)
    private String description;

    @Column
    private ZonedDateTime updatedAt;

    /** Username of the admin who last changed this setting; null if never updated / unknown. */
    @Column
    private String updatedBy;

    public AppSetting() {}

    public AppSetting(String settingKey, String settingValue, String description) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.description = description;
    }

    public Long getId() { return id; }
    public String getSettingKey() { return settingKey; }
    public String getSettingValue() { return settingValue; }
    public String getDescription() { return description; }
    public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public String getUpdatedBy() { return updatedBy; }

    public void setId(Long id) { this.id = id; }
    public void setSettingKey(String settingKey) { this.settingKey = settingKey; }
    public void setSettingValue(String settingValue) { this.settingValue = settingValue; }
    public void setDescription(String description) { this.description = description; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
