package com.studymanager.dto.request;

import java.time.ZonedDateTime;

public class LoginHistoryRequest {

    private ZonedDateTime loginAt;

    public LoginHistoryRequest() {}

    public ZonedDateTime getLoginAt() { return loginAt; }
    public void setLoginAt(ZonedDateTime loginAt) { this.loginAt = loginAt; }
}
