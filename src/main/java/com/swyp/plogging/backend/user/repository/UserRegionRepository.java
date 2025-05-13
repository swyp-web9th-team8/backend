package com.swyp.plogging.backend.user.repository;

import com.swyp.plogging.backend.user.domain.UserRegion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRegionRepository extends JpaRepository<UserRegion, Long> {

    List<UserRegion> findByUserId(Long userId);

    @Query("SELECT ur FROM UserRegion ur WHERE ur.user.id = :userId AND ur.isPrimary = true")
    Optional<UserRegion> findPrimaryRegionByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM UserRegion ur WHERE ur.user.id = :userId AND ur.region.id = :regionId")
    void deleteByUserIdAndRegionId(@Param("userId") Long userId, @Param("regionId") Long regionId);
}