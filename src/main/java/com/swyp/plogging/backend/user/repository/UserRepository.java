package com.swyp.plogging.backend.user.repository;

import com.swyp.plogging.backend.user.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long>, UserRepositoryCustom {

}
