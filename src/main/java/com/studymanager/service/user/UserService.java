package com.studymanager.service.user;

import com.studymanager.entity.user.Role;
import com.studymanager.entity.user.User;
import com.studymanager.repository.user.RoleRepository;
import com.studymanager.repository.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User createUser(String username, String fullName, String email,
                           String rawPassword, Set<String> roleNames, boolean active) {
        if (username == null || username.isBlank()) {
            throw new RuntimeException("Username is required");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new RuntimeException("Full name is required");
        }
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email is required");
        }
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new RuntimeException("Password is required");
        }
        if (roleNames == null || roleNames.isEmpty()) {
            throw new RuntimeException("At least one role is required");
        }
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists: " + username);
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists: " + email);
        }

        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName.toUpperCase())
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
            roles.add(role);
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setFullName(fullName.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setActive(active);
        user.setAdminApprovalStatus(com.studymanager.entity.user.AdminApprovalStatus.NONE);
        user.setRoles(roles);

        return userRepository.save(user);
    }

    public User updateUserRoles(Long userId, Set<String> roleNames) {
        User user = getUserById(userId);

        Set<Role> roles = new HashSet<>();

        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName.toUpperCase())
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

            roles.add(role);
        }

        user.setRoles(roles);

        return userRepository.save(user);
    }

    public User updateUserStatus(Long userId, boolean active) {
        User user = getUserById(userId);

        boolean isAdmin = user.getRoles()
                .stream()
                .anyMatch(role -> Role.ADMIN.equals(role.getName()));

        if (isAdmin) {
            throw new RuntimeException("Admin users cannot be activated or deactivated");
        }

        user.setActive(active);

        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        User user = getUserById(userId);

        boolean isAdmin = user.getRoles()
                .stream()
                .anyMatch(role -> Role.ADMIN.equals(role.getName()));

        if (isAdmin) {
            throw new RuntimeException("Admin users cannot be deleted");
        }

        userRepository.delete(user);
    }
}
