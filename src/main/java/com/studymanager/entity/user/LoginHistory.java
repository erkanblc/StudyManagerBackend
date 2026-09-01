package com.studymanager.entity.user;

import jakarta.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "login_history")
public class LoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private ZonedDateTime loginAt;

    public LoginHistory() {}

    public LoginHistory(User user, ZonedDateTime loginAt) {
        this.user = user;
        this.loginAt = loginAt;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public ZonedDateTime getLoginAt() { return loginAt; }

    public void setId(Long id) { this.id = id; }
    public void setUser(User user) { this.user = user; }
    public void setLoginAt(ZonedDateTime loginAt) { this.loginAt = loginAt; }
}
