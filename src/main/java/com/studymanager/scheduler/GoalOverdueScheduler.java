package com.studymanager.scheduler;

import com.studymanager.service.goal.GoalOverdueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class GoalOverdueScheduler {

    private static final Logger log = LoggerFactory.getLogger(GoalOverdueScheduler.class);

    private final GoalOverdueService goalOverdueService;

    public GoalOverdueScheduler(GoalOverdueService goalOverdueService) {
        this.goalOverdueService = goalOverdueService;
    }

    /** Runs every day at 01:00 — catches goals even when users do not log in. */
    @Scheduled(cron = "${app.goals.overdue-cron:0 0 1 * * *}")
    public void markOverdueGoalsDaily() {
        int updated = goalOverdueService.syncOverdueGoals();
        if (updated > 0) {
            log.info("Marked {} goal(s) as OVERDUE", updated);
        }
    }
}
