package com.studymanager.service.user;

import com.studymanager.entity.user.Role;
import com.studymanager.repository.user.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
    }

    public Role createRole(String name) {
        String roleName = name.toUpperCase();

        if (roleRepository.existsByName(roleName)) {
            throw new RuntimeException("Role already exists");
        }

        Role role = new Role();
        role.setName(roleName);

        return roleRepository.save(role);
    }

    public void deleteRole(Long roleId) {
        Role role = getRoleById(roleId);

        if (Role.ADMIN.equals(role.getName())) {
            throw new RuntimeException("ADMIN role cannot be deleted");
        }

        roleRepository.delete(role);
    }
}
