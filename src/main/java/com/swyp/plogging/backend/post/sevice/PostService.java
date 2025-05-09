package com.swyp.plogging.backend.post.sevice;

import com.swyp.plogging.backend.common.exception.PostNotFoundException;
import com.swyp.plogging.backend.common.exception.UnauthorizedUserException;
import com.swyp.plogging.backend.post.controller.dto.PostDetailResponse;
import com.swyp.plogging.backend.post.controller.dto.PostInfoResponse;
import com.swyp.plogging.backend.post.domain.Post;
import com.swyp.plogging.backend.post.repository.PostRepository;
import com.swyp.plogging.backend.user.domain.AppUser;
import jakarta.annotation.Nullable;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }


    @Transactional
    public PostDetailResponse createPost(AppUser user, String title, String content,
                                         LocalDateTime meetingTime, String placeId,
                                         String placeName, String address,
                                         @NonNull Integer maxParticipants, String openChatUrl,
                                         @Nullable Integer deadLine) {

        if (maxParticipants <= 0) {
            throw new IllegalArgumentException("최대인원 설정이 잘못되었습니다.");
        }

        Post post = Post.builder()
            .writer(user)
            .title(title)
            .content(content)
            .meetingDt(meetingTime)
            .placeId(placeId)
            .placeName(placeName)
            .address(address)
            .completed(false)
            .maxParticipants(maxParticipants)
            .openChatUrl(openChatUrl)
            .build();

        // null일 경우 30분전 세팅
        post.setUpDeadLine(deadLine);
        post = postRepository.save(post);

        return post.toDetailResponse();
    }

    @Transactional
    public PostDetailResponse modifyPost(AppUser user, Long id, String title,
        String content, LocalDateTime meetingTime,
        String placeId, String placeName, String address,
        Integer maxParticipants, String openChatUrl, Integer deadLine) {

        Post post = findById(id);
        post.isWriter(user);
        post.modify(title, content, meetingTime, placeId, placeName, address, maxParticipants, openChatUrl, deadLine);

        return post.toDetailResponse();
    }

    public Post findById(Long id) {
        return postRepository.findById(id).orElseThrow(PostNotFoundException::new);
    }

    @Transactional
    public void deletePost(Long postId, AppUser user) {
        Post post = findById(postId);

        // 작성자인지 확인
        if(!post.isWriter(user)){
            throw new UnauthorizedUserException("작성자가 아닙니다.");
        }

        postRepository.delete(post);
    }

    public PostDetailResponse getPostDetails(Long postId) {
        return findById(postId).toDetailResponse();
    }

    public Page<PostInfoResponse> getListOfPostInfo(Pageable pageable, Boolean recruitmentCompleted, Boolean completed) {
        // 데이터 DTO로 정제
        Page<Post> data = postRepository.findPostByCondition(pageable, recruitmentCompleted, completed);
        List<PostInfoResponse> content = data.getContent().stream().map(PostInfoResponse::new).toList();

        return new PageImpl<>(content, data.getPageable(), data.getTotalElements());
    }
}
