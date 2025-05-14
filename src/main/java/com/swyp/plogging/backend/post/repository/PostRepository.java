package com.swyp.plogging.backend.post.repository;

import com.swyp.plogging.backend.post.domain.Post;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    List<Post> findAllByMeetingDtBeforeAndCompletedFalse(LocalDateTime now);

    List<Post> findByWriterIdAndCompletedTrue(Long writerId);
}
