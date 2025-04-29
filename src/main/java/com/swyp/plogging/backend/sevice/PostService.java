package com.swyp.plogging.backend.sevice;

import com.swyp.plogging.backend.controller.DTO.PostDetailResponse;
import com.swyp.plogging.backend.domain.Post;
import com.swyp.plogging.backend.repository.PostRepository;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PostService {
    private final PostRepository postRepository;

    public PostService(PostRepository postRepository){
        this.postRepository = postRepository;
    }


    public PostDetailResponse createPost(String title, String content,
                                         LocalDateTime meetingTime, String placeId,
                                         String placeName, String address,
                                         Integer maxParticipants, String openChatUrl,
                                         @Nullable Integer deadLine) {

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
        post.createDeadLine(deadLine);
        post = postRepository.save(post);

        return post.toDetailResponse();
    }
}
