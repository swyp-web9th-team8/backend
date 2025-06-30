package com.swyp.plogging.backend.user.user.repository;

import com.swyp.plogging.backend.user.user.domain.AppUser;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
    Optional<AppUser> findByNickname(String nickname);
}
