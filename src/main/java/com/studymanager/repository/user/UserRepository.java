package com.studymanager.repository.user;

import com.studymanager.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import com.studymanager.entity.user.AdminApprovalStatus;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    List<User> findByAdminApprovalStatusOrderByIdDesc(AdminApprovalStatus status);

    long countByAdminApprovalStatus(AdminApprovalStatus status);
}
