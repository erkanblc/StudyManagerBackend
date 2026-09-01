package com.studymanager.controller.admin;

import com.studymanager.dto.request.CreateRoleRequest;
import com.studymanager.entity.user.Role;
import com.studymanager.service.user.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Roles", description = "Rol yönetimi")
@RestController
@RequestMapping("/api/admin/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @Operation(summary = "Tüm rolleri listele")
    @GetMapping
    public List<Role> getAllRoles() {
        return roleService.getAllRoles();
    }

    @Operation(summary = "Yeni rol oluştur")
    @PostMapping
    public Role createRole(@RequestBody CreateRoleRequest request) {
        return roleService.createRole(request.getName());
    }

    @Operation(summary = "Rol sil")
    @DeleteMapping("/{roleId}")
    public void deleteRole(@PathVariable Long roleId) {
        roleService.deleteRole(roleId);
    }
}
