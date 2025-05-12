package com.swyp.plogging.backend.certificate.repository;

import com.swyp.plogging.backend.certificate.domain.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certification, Long> {
    Optional<Certification> findByPostId(Long postId);
}
