package com.swyp.plogging.backend.service;

import com.swyp.plogging.backend.controller.dto.PostDetailResponse;
import com.swyp.plogging.backend.controller.dto.PostInfoResponse;
import com.swyp.plogging.backend.controller.dto.PostListResponse;
import com.swyp.plogging.backend.domain.Post;
import com.swyp.plogging.backend.repository.PostRepository;
import jakarta.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.List;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }


    @Transactional
    public PostDetailResponse createPost(String title, String content,
        LocalDateTime meetingTime, String placeId,
        String placeName, String address,
        @NonNull Integer maxParticipants, String openChatUrl,
        @Nullable Integer deadLine) {

        if (maxParticipants <= 0) {
            throw new IllegalArgumentException("최대인원 설정이 잘못되었습니다.");
        }

        Post post = Post.builder()
//                .writer() // todo 로그인 구현 완료 후 작성자 수정
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
    public PostDetailResponse modifyPost(Long id, String title,
        String content, LocalDateTime meetingTime,
        String placeId, String placeName, String address,
        Integer maxParticipants, String openChatUrl, Integer deadLine) {

        Post post = findById(id);
        post.modify(title, content, meetingTime, placeId, placeName, address, maxParticipants, openChatUrl, deadLine);

        return post.toDetailResponse();
    }

    public Post findById(Long id) {
        return postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("잘못된 PostId입니다."));
    }

    @Transactional
    public void deletePost(Long postId) {
        // todo 글의 주인 또는 관리자인지 확인 필요

        postRepository.deleteById(postId);
    }

    public PostDetailResponse getPostDetails(Long postId) {
        return findById(postId).toDetailResponse();
    }

    public PostListResponse<PostInfoResponse> getListOfPostInfo(Pageable pageable, Boolean recruitmentCompleted, Boolean completed) {
        // 데이터 DTO로 정제
        Page<Post> data = postRepository.findPostByCondition(pageable, recruitmentCompleted, completed);
        List<PostInfoResponse> content = data.getContent().stream().map(PostInfoResponse::new).toList();

        return PostListResponse.of(content, data.getPageable(), data.getTotalElements());
    }
}
