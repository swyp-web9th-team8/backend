package com.swyp.plogging.backend.badge.repository;

import com.swyp.plogging.backend.badge.domain.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {
    List<Badge> findByRequiredActivitiesForBadgeLessThanEqualOrderByRequiredActivitiesForBadgeAsc(int count);
}
