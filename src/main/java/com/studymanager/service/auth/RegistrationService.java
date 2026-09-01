package com.studymanager.service.auth;

import com.studymanager.dto.request.RegisterRequest;
import com.studymanager.dto.response.RegisterResponse;
import com.studymanager.entity.user.AdminApprovalStatus;
import com.studymanager.entity.user.Role;
import com.studymanager.entity.user.User;
import com.studymanager.repository.user.RoleRepository;
import com.studymanager.repository.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(UserRepository userRepository,
                               RoleRepository roleRepository,
                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        validate(request);

        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();
        String fullName = request.getFullName().trim();
        String password = request.getPassword();

        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists.");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPassword(passwordEncoder.encode(password));

        if (request.isRegisterAsAdmin()) {
            user.setActive(false);
            user.setAdminApprovalStatus(AdminApprovalStatus.PENDING);
            user.setRoles(new HashSet<>());
            userRepository.save(user);
            return new RegisterResponse(
                    email,
                    username,
                    "Admin registration submitted. An existing administrator must approve your account before you can sign in.",
                    true
            );
        }

        Role studentRole = roleRepository.findByName(Role.STUDENT)
                .orElseThrow(() -> new RuntimeException("Student role not found."));
        user.setActive(true);
        user.setAdminApprovalStatus(AdminApprovalStatus.NONE);
        Set<Role> roles = new HashSet<>();
        roles.add(studentRole);
        user.setRoles(roles);
        userRepository.save(user);

        return new RegisterResponse(
                email,
                username,
                "Registration successful. You can sign in now.",
                false
        );
    }

    private void validate(RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().length() < 3) {
            throw new RuntimeException("Username must be at least 3 characters.");
        }
        if (request.getFullName() == null || request.getFullName().trim().isBlank()) {
            throw new RuntimeException("Full name is required.");
        }
        if (request.getEmail() == null || !request.getEmail().contains("@")) {
            throw new RuntimeException("A valid email is required.");
        }
        if (request.getPassword() == null || request.getPassword().length() < 4) {
            throw new RuntimeException("Password must be at least 4 characters.");
        }
    }
}
