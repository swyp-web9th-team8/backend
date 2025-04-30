package com.swyp.plogging.backend.service;


import com.swyp.plogging.backend.controller.DTO.PostDetailResponse;
import com.swyp.plogging.backend.domain.Post;
import com.swyp.plogging.backend.repository.PostRepository;
import com.swyp.plogging.backend.sevice.PostService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class PostServiceTest {
    @InjectMocks
    PostService postService;

    @Mock
    PostRepository postRepository;

    @Test
    @DisplayName("모임 생성 테스트")
    public void createPostTest(){
        //given
        Post given = Post.builder()
                .id(1L)
                // todo 로그인 구현 후 수정 필요
//                .writer()
                .title("생성 시험")
                .content("생성 시험 내용")
                .meetingDt(LocalDateTime.parse("2025-04-29T10:40:32"))
                .placeId("1")
                .placeName("서울시청")
                .address("서울특별시 중구 세종대로 126")
                .maxParticipants(10)
                .openChatUrl("https://open.kakao.com/몰라")
                .build();
        given.setUpDeadLine(null);
        when(postRepository.save(any(Post.class))).thenReturn(given);

        //when
        PostDetailResponse dto = postService.createPost(
                "생성 시험",
                "생성 시험 내용",
                LocalDateTime.parse("2025-04-29T10:40:32"),
                "1",
                "서울시청",
                "서울특별시 중구 세종대로 126",
                10,
                "https://open.kakao.com/몰라",
                null);
        //then
        Assertions.assertEquals(dto.getId(),1L);
        Assertions.assertEquals(dto.getTitle(), "생성 시험");
        Assertions.assertEquals(dto.getDeadLine(), given.getMeetingDt().minusMinutes(30));
    }

    @Test
    @DisplayName("모임 생성 테스트")
    public void modifyPostTest(){
        //given
        Post given = Post.builder()
                .id(1L)
                // todo 로그인 구현 후 수정 필요
//                .writer()
                .title("생성 시험")
                .content("생성 시험 내용")
                .meetingDt(LocalDateTime.parse("2025-04-29T10:40:32"))
                .placeId("1")
                .placeName("서울시청")
                .address("서울특별시 중구 세종대로 126")
                .maxParticipants(10)
                .openChatUrl("https://open.kakao.com/몰라")
                .build();
        given.setUpDeadLine(null);
        when(postRepository.findById(1L)).thenReturn(Optional.of(given));

        //when
        PostDetailResponse dto = postService.modifyPost(
                1L,
                "생성 시험2",
                "생성 시험 내용2",
                null,
                null,
                null,
                null,
                null,
                null,
                60);
        //then
        Assertions.assertEquals(dto.getId(),1L);
        Assertions.assertEquals(dto.getTitle(), "생성 시험2");
        Assertions.assertEquals(dto.getDeadLine(), given.getMeetingDt().minusMinutes(60));
    }

}
