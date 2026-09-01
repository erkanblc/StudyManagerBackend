package com.studymanager.dto.response;

public class RegisterResponse {

    private String email;
    private String username;
    private String message;
    private boolean pendingAdminApproval;

    public RegisterResponse() {}

    public RegisterResponse(String email, String username, String message, boolean pendingAdminApproval) {
        this.email = email;
        this.username = username;
        this.message = message;
        this.pendingAdminApproval = pendingAdminApproval;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isPendingAdminApproval() { return pendingAdminApproval; }
    public void setPendingAdminApproval(boolean pendingAdminApproval) {
        this.pendingAdminApproval = pendingAdminApproval;
    }
}
