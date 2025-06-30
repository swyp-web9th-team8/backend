package com.swyp.plogging.backend.user.user.repository;

import com.swyp.plogging.backend.user.user.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long>, UserRepositoryCustom {

}
