package com.studymanager.repository.goal;

import com.studymanager.entity.goal.Milestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MilestoneRepository extends JpaRepository<Milestone, Long> {

    List<Milestone> findByGoalIdOrderByCreatedAtAsc(Long goalId);

    long countByGoal_Id(Long goalId);

    void deleteByGoal_Id(Long goalId);

    List<Milestone> findByUserIsNull();

    @Query("""
            SELECT m FROM Milestone m
            LEFT JOIN m.goal g
            LEFT JOIN g.createdBy owner
            WHERE m.user.id = :userId
               OR (m.user IS NULL AND owner.id = :userId)
            """)
    List<Milestone> findAllForUser(@Param("userId") Long userId);

    @Query("""
            SELECT m FROM Milestone m
            LEFT JOIN m.goal g
            LEFT JOIN g.createdBy owner
            WHERE m.id = :id
              AND (m.user.id = :userId OR (m.user IS NULL AND owner.id = :userId))
            """)
    Optional<Milestone> findByIdForUser(@Param("id") Long id, @Param("userId") Long userId);
}
