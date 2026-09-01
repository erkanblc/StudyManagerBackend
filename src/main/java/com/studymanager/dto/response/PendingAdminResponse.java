package com.studymanager.dto.response;

import java.time.LocalDateTime;

public class PendingAdminResponse {

    private Long id;
    private String username;
    private String fullName;
    private String email;
    private LocalDateTime registeredAt;

    public PendingAdminResponse() {}

    public PendingAdminResponse(Long id, String username, String fullName, String email,
                              LocalDateTime registeredAt) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.registeredAt = registeredAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }
}
