package com.studymanager.service.user;

import com.studymanager.dto.response.PendingAdminResponse;
import com.studymanager.entity.user.AdminApprovalStatus;
import com.studymanager.entity.user.Role;
import com.studymanager.entity.user.User;
import com.studymanager.repository.user.RoleRepository;
import com.studymanager.repository.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminApprovalService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public AdminApprovalService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public List<PendingAdminResponse> getPendingAdmins() {
        return userRepository.findByAdminApprovalStatusOrderByIdDesc(AdminApprovalStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public long countPendingAdmins() {
        return userRepository.countByAdminApprovalStatus(AdminApprovalStatus.PENDING);
    }

    @Transactional
    public PendingAdminResponse approve(Long userId) {
        User user = getPendingUser(userId);
        Role adminRole = roleRepository.findByName(Role.ADMIN)
                .orElseThrow(() -> new RuntimeException("Admin role not found."));
        user.getRoles().clear();
        user.getRoles().add(adminRole);
        user.setActive(true);
        user.setAdminApprovalStatus(AdminApprovalStatus.APPROVED);
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public PendingAdminResponse reject(Long userId) {
        User user = getPendingUser(userId);
        user.setActive(false);
        user.setAdminApprovalStatus(AdminApprovalStatus.REJECTED);
        return toResponse(userRepository.save(user));
    }

    private User getPendingUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        if (user.getAdminApprovalStatus() != AdminApprovalStatus.PENDING) {
            throw new RuntimeException("User is not pending admin approval.");
        }
        return user;
    }

    private PendingAdminResponse toResponse(User user) {
        return new PendingAdminResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }
}
