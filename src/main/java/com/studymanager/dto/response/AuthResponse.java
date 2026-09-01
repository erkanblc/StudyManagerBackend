package com.studymanager.dto.response;

import java.time.ZonedDateTime;
import java.util.List;

public class AuthResponse {

    private Long id;
    /** Access JWT (short-lived). */
    private String token;
    /** Opaque refresh token (long-lived, stored server-side). */
    private String refreshToken;
    /** Access token lifetime in milliseconds. */
    private long expiresIn;
    private String email;
    private String username;
    private List<String> roles;
    private ZonedDateTime lastLoginAt;

    public AuthResponse() {}

    public AuthResponse(Long id, String token, String refreshToken, long expiresIn,
                        String email, String username, List<String> roles,
                        ZonedDateTime lastLoginAt) {
        this.id = id;
        this.token = token;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.email = email;
        this.username = username;
        this.roles = roles;
        this.lastLoginAt = lastLoginAt;
    }

    public Long getId() { return id; }
    public String getToken() { return token; }
    public String getRefreshToken() { return refreshToken; }
    public long getExpiresIn() { return expiresIn; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public List<String> getRoles() { return roles; }
    public ZonedDateTime getLastLoginAt() { return lastLoginAt; }

    public void setId(Long id) { this.id = id; }
    public void setToken(String token) { this.token = token; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }
    public void setEmail(String email) { this.email = email; }
    public void setUsername(String username) { this.username = username; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    public void setLastLoginAt(ZonedDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
}
