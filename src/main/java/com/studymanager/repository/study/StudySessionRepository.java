package com.studymanager.repository.study;

import com.studymanager.entity.study.SessionStatus;
import com.studymanager.entity.study.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudySessionRepository extends JpaRepository<StudySession, Long> {

    List<StudySession> findByUser_IdOrderByCreatedAtDesc(Long userId);

    Optional<StudySession> findTopByUser_IdAndStatusOrderByStartTimeDesc(Long userId,
            SessionStatus status);

    Optional<StudySession> findTopByUser_IdOrderByCreatedAtDesc(Long userId);
}
