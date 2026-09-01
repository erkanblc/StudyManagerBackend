package com.studymanager.dto.response;

import java.time.ZonedDateTime;

public class AppSettingResponse {

    private Long id;
    private String key;
    private String value;
    private String description;
    private ZonedDateTime updatedAt;
    private String updatedBy;

    public AppSettingResponse() {}

    public AppSettingResponse(Long id, String key, String value,
                               String description, ZonedDateTime updatedAt, String updatedBy) {
        this.id = id;
        this.key = key;
        this.value = value;
        this.description = description;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public Long getId() { return id; }
    public String getKey() { return key; }
    public String getValue() { return value; }
    public String getDescription() { return description; }
    public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public String getUpdatedBy() { return updatedBy; }

    public void setId(Long id) { this.id = id; }
    public void setKey(String key) { this.key = key; }
    public void setValue(String value) { this.value = value; }
    public void setDescription(String description) { this.description = description; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
