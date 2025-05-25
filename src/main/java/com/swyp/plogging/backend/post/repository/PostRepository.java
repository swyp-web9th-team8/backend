package com.swyp.plogging.backend.post.repository;

import com.swyp.plogging.backend.post.domain.Post;
import com.swyp.plogging.backend.user.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    List<Post> findAllByMeetingDtBeforeAndCompletedFalse(LocalDateTime now);

    List<Post> findByWriterIdAndCompletedTrue(Long writerId);

    Long countByWriterAndCompleted(AppUser user, boolean completed);
}
