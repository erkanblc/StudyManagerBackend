package com.studymanager.repository.user;

import com.studymanager.entity.user.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    Optional<LoginHistory> findTopByUserIdOrderByLoginAtDesc(Long userId);

    List<LoginHistory> findByUserIdOrderByLoginAtDesc(Long userId);
}
