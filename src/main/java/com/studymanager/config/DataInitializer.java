package com.studymanager.config;

import com.studymanager.entity.user.Role;
import com.studymanager.entity.user.User;
import com.studymanager.repository.user.RoleRepository;
import com.studymanager.repository.user.UserRepository;
import com.studymanager.service.config.AppSettingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppSettingService appSettingService;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           AppSettingService appSettingService) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.appSettingService = appSettingService;
    }

    @Override
    public void run(String... args) {
        Role adminRole   = createRoleIfNotExists(Role.ADMIN);
        Role studentRole = createRoleIfNotExists(Role.STUDENT);

        createUserIfNotExists("admin",    "Admin",       "admin@example.com",    "admin",    adminRole);
        createUserIfNotExists("student1", "Student One", "student1@example.com", "student1", studentRole);
        createUserIfNotExists("erkan",    "Erkan",       "erkan@erkan.com",       "12345",    adminRole);

        appSettingService.initDefaultIfAbsent(
                AppSettingService.KEY_MAX_SESSION_HOURS,
                "6",
                "Maximum allowed study session duration in hours. Min: 6, Max: 24."
        );
    }

    private void createUserIfNotExists(String username, String fullName, String email,
                                       String rawPassword, Role role) {
        if (userRepository.existsByUsername(username)) {
            return;
        }
        User user = new User();
        user.setUsername(username);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setActive(true);
        user.getRoles().add(role);
        userRepository.save(user);
    }

    private Role createRoleIfNotExists(String name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> {
                    Role role = new Role(name);
                    return roleRepository.save(role);
                });
    }
}
