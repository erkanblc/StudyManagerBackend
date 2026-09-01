package com.studymanager.config;

import com.studymanager.service.goal.MilestoneService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class MilestoneUserBackfillRunner implements ApplicationRunner {

    private final MilestoneService milestoneService;

    public MilestoneUserBackfillRunner(MilestoneService milestoneService) {
        this.milestoneService = milestoneService;
    }

    @Override
    public void run(ApplicationArguments args) {
        milestoneService.backfillUserIds();
    }
}
