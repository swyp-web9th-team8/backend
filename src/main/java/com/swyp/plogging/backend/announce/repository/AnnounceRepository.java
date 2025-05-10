package com.swyp.plogging.backend.announce.repository;

import com.swyp.plogging.backend.announce.domain.Announce;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnnounceRepository extends JpaRepository<Announce, Long> {

    List<Announce> findByActiveTrue();

    Optional<Announce> findByIdAndActiveTrue(Long id);
}
