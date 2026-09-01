package com.studymanager.dto.response;

import java.time.ZonedDateTime;

public class LoginHistoryResponse {

    private Long id;
    private ZonedDateTime loginAt;

    public LoginHistoryResponse() {}

    public LoginHistoryResponse(Long id, ZonedDateTime loginAt) {
        this.id = id;
        this.loginAt = loginAt;
    }

    public Long getId() { return id; }
    public ZonedDateTime getLoginAt() { return loginAt; }

    public void setId(Long id) { this.id = id; }
    public void setLoginAt(ZonedDateTime loginAt) { this.loginAt = loginAt; }
}
