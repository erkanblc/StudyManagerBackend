package com.studymanager.repository.study;

import com.studymanager.entity.study.PlanSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;
import java.util.List;

public interface PlanSessionRepository extends JpaRepository<PlanSession, Long> {

    List<PlanSession> findByUserIdOrderByPlannedDateAsc(Long userId);

    List<PlanSession> findByUserIdAndPlannedDateBetweenOrderByPlannedDateAsc(
            Long userId, ZonedDateTime start, ZonedDateTime end);
}
