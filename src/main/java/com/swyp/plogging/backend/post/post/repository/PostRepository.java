package com.swyp.plogging.backend.post.post.repository;

import com.swyp.plogging.backend.post.post.domain.Post;
import com.swyp.plogging.backend.user.user.domain.AppUser;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    List<Post> findAllByMeetingDtBeforeAndCompletedFalse(LocalDateTime now);

    List<Post> findByWriterIdAndCompletedTrue(Long writerId);

    Long countByWriterAndCompleted(AppUser user, boolean completed);

    List<Post> findAllByRegionIdIsNull();
    List<Post> findTop100ByRegionIdIsNull();

    @Query("select p from Post p where p.id = :postId")
    @EntityGraph(attributePaths = {"participations", "participations.user"})
    Optional<Post> findByIdWithGraph(Long postId);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("select p from Post p where p.id = :postId")
    Optional<Post> findByIdWithOptimisticLock(Long postId);
}
