package com.swyp.plogging.backend.participation.repository;

import com.swyp.plogging.backend.participation.domain.Participation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParticipationRepository extends JpaRepository<Participation, Long>, ParticipationRepositoryCustom {

}
