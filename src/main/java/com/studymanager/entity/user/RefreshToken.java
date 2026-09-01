package com.studymanager.entity.user;

import jakarta.persistence.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_token", columnList = "token", unique = true)
})
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private ZonedDateTime expiresAt;

    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt = ZonedDateTime.now(ZoneOffset.UTC);

    @Column(nullable = false)
    private boolean revoked = false;

    public RefreshToken() {}

    public RefreshToken(String token, User user, ZonedDateTime expiresAt) {
        this.token = token;
        this.user = user;
        this.expiresAt = expiresAt;
    }

    public Long getId() { return id; }
    public String getToken() { return token; }
    public User getUser() { return user; }
    public ZonedDateTime getExpiresAt() { return expiresAt; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public boolean isRevoked() { return revoked; }

    public void setId(Long id) { this.id = id; }
    public void setToken(String token) { this.token = token; }
    public void setUser(User user) { this.user = user; }
    public void setExpiresAt(ZonedDateTime expiresAt) { this.expiresAt = expiresAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }

    public boolean isExpired() {
        return expiresAt == null || ZonedDateTime.now(ZoneOffset.UTC).isAfter(expiresAt);
    }
}
