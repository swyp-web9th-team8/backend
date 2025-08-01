package com.swyp.plogging.backend.user.user.repository;

import com.swyp.plogging.backend.user.user.domain.AppUser;
import com.swyp.plogging.backend.user.user.domain.UserBadge;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {

    List<UserBadge> findByUser(AppUser user);

    Optional<UserBadge> findTopByUserIdOrderByCreatedDtDesc(Long userId);
}
