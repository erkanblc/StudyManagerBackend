package com.studymanager.service.goal;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class MilestoneLimits {

    public static final int MAX_PER_GOAL = 5;

    private MilestoneLimits() {}

    public static void assertUnderLimit(long currentCount) {
        if (currentCount >= MAX_PER_GOAL) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A goal can have at most " + MAX_PER_GOAL + " milestones");
        }
    }
}
