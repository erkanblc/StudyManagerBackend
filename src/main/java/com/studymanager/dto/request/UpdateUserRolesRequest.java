package com.studymanager.dto.request;

import java.util.Set;

public class UpdateUserRolesRequest {

    private Set<String> roleNames;

    public UpdateUserRolesRequest() {
    }

    public Set<String> getRoleNames() {
        return roleNames;
    }

    public void setRoleNames(Set<String> roleNames) {
        this.roleNames = roleNames;
    }
}
