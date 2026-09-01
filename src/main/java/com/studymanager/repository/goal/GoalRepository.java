package com.studymanager.repository.goal;

import com.studymanager.entity.goal.Goal;
import com.studymanager.entity.goal.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface GoalRepository extends JpaRepository<Goal, Long> {

    List<Goal> findAllByOrderByCreatedAtDesc();

    List<Goal> findByStatusOrderByCreatedAtDesc(GoalStatus status);

    List<Goal> findByCreatedByIdOrderByCreatedAtDesc(Long userId);

    List<Goal> findByCreatedByIdAndStatusOrderByCreatedAtDesc(Long userId, GoalStatus status);

    List<Goal> findByStatusInAndEndDateBefore(Collection<GoalStatus> statuses, LocalDate date);

    List<Goal> findByCreatedByIdAndStatusInAndEndDateBefore(
            Long userId,
            Collection<GoalStatus> statuses,
            LocalDate date
    );
}
