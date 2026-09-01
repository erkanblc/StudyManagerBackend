package com.studymanager.service.goal;

import com.studymanager.entity.goal.Goal;
import com.studymanager.entity.goal.GoalStatus;
import com.studymanager.repository.goal.GoalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.List;

@Service
public class GoalOverdueService {

    private static final EnumSet<GoalStatus> OVERDUE_ELIGIBLE = EnumSet.of(
            GoalStatus.ACTIVE,
            GoalStatus.PAUSED
    );

    private final GoalRepository goalRepository;

    public GoalOverdueService(GoalRepository goalRepository) {
        this.goalRepository = goalRepository;
    }

    @Transactional
    public int syncOverdueGoals() {
        return markOverdue(
                goalRepository.findByStatusInAndEndDateBefore(OVERDUE_ELIGIBLE, LocalDate.now(ZoneOffset.UTC))
        );
    }

    @Transactional
    public int syncOverdueGoalsForUser(Long userId) {
        return markOverdue(
                goalRepository.findByCreatedByIdAndStatusInAndEndDateBefore(
                        userId,
                        OVERDUE_ELIGIBLE,
                        LocalDate.now(ZoneOffset.UTC)
                )
        );
    }

    private int markOverdue(List<Goal> goals) {
        if (goals.isEmpty()) {
            return 0;
        }

        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        for (Goal goal : goals) {
            goal.setStatus(GoalStatus.OVERDUE);
            goal.setUpdatedAt(now);
        }
        goalRepository.saveAll(goals);
        return goals.size();
    }
}
