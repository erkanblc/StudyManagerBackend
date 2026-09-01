package com.studymanager.service.study;

import com.studymanager.dto.request.HeartbeatSessionRequest;
import com.studymanager.dto.request.ManualSessionRequest;
import com.studymanager.dto.request.ResolveSessionRequest;
import com.studymanager.dto.request.StartSessionRequest;
import com.studymanager.dto.request.UpdateSessionRequest;
import com.studymanager.dto.response.StudySessionResponse;
import com.studymanager.entity.study.SessionStatus;
import com.studymanager.entity.study.StudySession;
import com.studymanager.entity.user.User;
import com.studymanager.repository.study.StudySessionRepository;
import com.studymanager.repository.user.UserRepository;
import com.studymanager.service.config.AppSettingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StudySessionService {

    private final StudySessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final AppSettingService appSettingService;

    public StudySessionService(StudySessionRepository sessionRepository,
                               UserRepository userRepository,
                               AppSettingService appSettingService) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.appSettingService = appSettingService;
    }

    // ── CREATE (manual) ───────────────────────────────────────────────────────

    @Transactional
    public StudySessionResponse createManualSession(Long userId, ManualSessionRequest req) {
        User user = findUser(userId);

        ZonedDateTime start = req.getStartTime();
        ZonedDateTime end   = req.getEndTime();
        Long durationSec    = req.getDuration();

        if (start != null && end != null) {
            if (end.isBefore(start)) {
                throw new IllegalArgumentException("endTime cannot be before startTime");
            }
            durationSec = ChronoUnit.SECONDS.between(start, end);
        } else if (durationSec != null && durationSec > 0) {
            end   = ZonedDateTime.now(ZoneOffset.UTC);
            start = end.minusSeconds(durationSec);
        } else {
            throw new IllegalArgumentException(
                    "Provide either startTime+endTime or duration (seconds)");
        }

        assertManualDurationBounds(durationSec);

        StudySession session = new StudySession();
        session.setUser(user);
        session.setStartTime(start);
        session.setEndTime(end);
        session.setDurationSeconds(durationSec);
        session.setGoalId(req.getGoalId());
        session.setSubject(req.getSubject());
        session.setNotes(req.getNotes());
        session.setStatus(SessionStatus.MANUAL);

        return toResponse(sessionRepository.save(session));
    }

    // ── START (stopwatch) ─────────────────────────────────────────────────────

    @Transactional
    public StudySessionResponse startSession(Long userId, StartSessionRequest req) {
        User user = findUser(userId);

        sessionRepository.findTopByUser_IdAndStatusOrderByStartTimeDesc(userId, SessionStatus.ACTIVE)
                .ifPresent(s -> {
                    throw new IllegalStateException("An active session already exists");
                });

        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

        StudySession session = new StudySession();
        session.setUser(user);
        session.setStartTime(now);
        session.setDurationSeconds(0L);
        session.setLastHeartbeatAt(now);
        session.setStatus(SessionStatus.ACTIVE);

        if (req != null) {
            session.setGoalId(req.getGoalId());
            session.setSubject(req.getSubject());
            session.setNotes(req.getNotes());
        }

        return toResponse(sessionRepository.save(session));
    }

    // ── STOP (legacy stopwatch) ───────────────────────────────────────────────

    @Transactional
    public StudySessionResponse stopSession(Long userId) {
        StudySession session = sessionRepository
                .findTopByUser_IdAndStatusOrderByStartTimeDesc(userId, SessionStatus.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("No active session found"));

        ZonedDateTime end = ZonedDateTime.now(ZoneOffset.UTC);
        long durationSec  = ChronoUnit.SECONDS.between(session.getStartTime(), end);
        assertDurationWithinLimit(durationSec);

        session.setEndTime(end);
        session.setDurationSeconds(durationSec);
        session.setLastHeartbeatAt(end);
        session.setStatus(SessionStatus.COMPLETED);

        return toResponse(sessionRepository.save(session));
    }

    // ── ACTIVE SESSION ────────────────────────────────────────────────────────

    public Optional<StudySessionResponse> getActiveSession(Long userId) {
        return sessionRepository
                .findTopByUser_IdAndStatusOrderByStartTimeDesc(userId, SessionStatus.ACTIVE)
                .map(this::toResponse);
    }

    // ── HEARTBEAT ─────────────────────────────────────────────────────────────

    @Transactional
    public StudySessionResponse heartbeat(Long sessionId, Long userId,
                                          HeartbeatSessionRequest req) {
        if (req.getDuration() == null) {
            throw new IllegalArgumentException("duration is required for heartbeat");
        }

        StudySession session = findSession(sessionId, userId);

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new IllegalStateException("Session is not ACTIVE");
        }

        session.setDurationSeconds(req.getDuration());
        assertDurationWithinLimit(req.getDuration());
        session.setLastHeartbeatAt(ZonedDateTime.now(ZoneOffset.UTC));

        if (req.getGoalId()  != null) session.setGoalId(req.getGoalId());
        if (req.getSubject() != null) session.setSubject(req.getSubject());
        if (req.getNotes()   != null) session.setNotes(req.getNotes());

        return toResponse(sessionRepository.save(session));
    }

    // ── RESOLVE ───────────────────────────────────────────────────────────────

    @Transactional
    public StudySessionResponse resolveSession(Long sessionId, Long userId,
                                               ResolveSessionRequest req) {
        StudySession session = findSession(sessionId, userId);

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new IllegalStateException("Session is not ACTIVE");
        }

        if (req.getAction() == null) {
            throw new IllegalArgumentException("action is required");
        }

        switch (req.getAction().toUpperCase()) {

            case "SAVE_AT_HEARTBEAT": {
                ZonedDateTime heartbeat = session.getLastHeartbeatAt();
                if (heartbeat == null) heartbeat = ZonedDateTime.now(ZoneOffset.UTC);
                long dur = ChronoUnit.SECONDS.between(session.getStartTime(), heartbeat);
                session.setEndTime(heartbeat);
                session.setDurationSeconds(Math.max(dur, 0));
                session.setStatus(SessionStatus.COMPLETED);
                break;
            }

            case "CONTINUE": {
                session.setLastHeartbeatAt(ZonedDateTime.now(ZoneOffset.UTC));
                break;
            }

            case "MANUAL": {
                ZonedDateTime start = req.getStartTime();
                ZonedDateTime end   = req.getEndTime();
                Long dur            = req.getDuration();

                if (start != null && end != null) {
                    if (end.isBefore(start)) {
                        throw new IllegalArgumentException("endTime cannot be before startTime");
                    }
                    dur = ChronoUnit.SECONDS.between(start, end);
                    session.setStartTime(start);
                    session.setEndTime(end);
                } else if (dur != null && dur > 0) {
                    ZonedDateTime resolvedEnd = ZonedDateTime.now(ZoneOffset.UTC);
                    session.setEndTime(resolvedEnd);
                    session.setStartTime(resolvedEnd.minusSeconds(dur));
                } else {
                    throw new IllegalArgumentException(
                            "MANUAL action requires startTime+endTime or duration");
                }
                assertManualDurationBounds(dur);
                session.setDurationSeconds(dur);
                session.setStatus(SessionStatus.COMPLETED);
                break;
            }

            default:
                throw new IllegalArgumentException(
                        "Unknown action: " + req.getAction() +
                        ". Use SAVE_AT_HEARTBEAT, CONTINUE or MANUAL");
        }

        return toResponse(sessionRepository.save(session));
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    public List<StudySessionResponse> getAllSessions(Long userId) {
        return sessionRepository.findByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Optional<StudySessionResponse> getLastSession(Long userId) {
        return sessionRepository.findTopByUser_IdOrderByCreatedAtDesc(userId)
                .map(this::toResponse);
    }

    public long getTotalSeconds(Long userId) {
        return sessionRepository.findByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .filter(s -> s.getDurationSeconds() != null)
                .mapToLong(StudySession::getDurationSeconds)
                .sum();
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    @Transactional
    public StudySessionResponse updateSession(Long sessionId, Long userId,
                                              UpdateSessionRequest req) {
        StudySession session = findSession(sessionId, userId);

        if (req.getSubject()  != null) session.setSubject(req.getSubject());
        if (req.getNotes()    != null) session.setNotes(req.getNotes());
        if (req.getGoalId()   != null) session.setGoalId(req.getGoalId());

        boolean timeChanged = false;
        if (req.getStartTime() != null) {
            session.setStartTime(req.getStartTime());
            timeChanged = true;
        }
        if (req.getDuration() != null && req.getDuration() > 0) {
            assertManualDurationBounds(req.getDuration());
            session.setDurationSeconds(req.getDuration());
            timeChanged = true;
        }
        if (timeChanged && session.getStartTime() != null && session.getDurationSeconds() != null) {
            session.setEndTime(session.getStartTime().plusSeconds(session.getDurationSeconds()));
        }

        return toResponse(sessionRepository.save(session));
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @Transactional
    public void deleteSession(Long sessionId, Long userId) {
        StudySession session = findSession(sessionId, userId);
        sessionRepository.delete(session);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static final long MANUAL_MIN_DURATION_SECONDS = 3L;
    private static final long MANUAL_MAX_DURATION_SECONDS = 24L * 3600L;

    /** Timer / stop / heartbeat — admin setting (e.g. 6–24h from DB). */
    private void assertDurationWithinLimit(long durationSeconds) {
        long maxSeconds = (long) appSettingService.getMaxSessionHours() * 3600;
        if (durationSeconds > maxSeconds) {
            int maxHours = appSettingService.getMaxSessionHours();
            throw new IllegalArgumentException(
                    "Session duration exceeds the maximum allowed limit of " + maxHours + " hour(s).");
        }
    }

    /** Manual create / edit / resolve — fixed bounds: 3 seconds … 24 hours. */
    private void assertManualDurationBounds(long durationSeconds) {
        if (durationSeconds < MANUAL_MIN_DURATION_SECONDS) {
            throw new IllegalArgumentException(
                    "Session duration must be at least " + MANUAL_MIN_DURATION_SECONDS + " second(s).");
        }
        if (durationSeconds > MANUAL_MAX_DURATION_SECONDS) {
            throw new IllegalArgumentException(
                    "Session duration exceeds the maximum allowed limit of 24 hour(s).");
        }
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }

    private StudySession findSession(Long sessionId, Long userId) {
        StudySession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));
        if (!session.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied for session: " + sessionId);
        }
        return session;
    }

    private StudySessionResponse toResponse(StudySession s) {
        return new StudySessionResponse(
                s.getId(),
                s.getUser() != null ? s.getUser().getId() : null,
                s.getStartTime(),
                s.getEndTime(),
                s.getDurationSeconds(),
                s.getGoalId(),
                s.getSubject(),
                s.getNotes(),
                s.getStatus(),
                s.getLastHeartbeatAt(),
                s.getCreatedAt()
        );
    }
}
