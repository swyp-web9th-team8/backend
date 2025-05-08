package com.swyp.plogging.backend.service;


import com.swyp.plogging.backend.controller.dto.PostDetailResponse;
import com.swyp.plogging.backend.controller.dto.PostInfoResponse;
import com.swyp.plogging.backend.post.domain.Post;
import com.swyp.plogging.backend.post.repository.PostRepository;
import com.swyp.plogging.backend.post.sevice.PostService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

    @InjectMocks
    PostService postService;

    @Mock
    PostRepository postRepository;

    private static Post data;

    private static final Logger log = LoggerFactory.getLogger(PostServiceTest.class);

    @BeforeEach
    public void createData() {
        data = Post.builder()
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
        data.setUpDeadLine(null);
    }

    @Test
    @DisplayName("모임 생성 테스트")
    public void createPostTest(TestInfo testInfo) {
        log.info(() -> testInfo.getDisplayName() + " 시작");
        //given
        Post expected = data;
        when(postRepository.save(any(Post.class))).thenReturn(expected);

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
        Assertions.assertEquals(dto.getId(), 1L);
        Assertions.assertEquals(dto.getTitle(), "생성 시험");
        Assertions.assertEquals(dto.getDeadLine(), expected.getMeetingDt().minusMinutes(30));
        log.info(() -> testInfo.getDisplayName() + " 완료");
    }

    @Test
    @DisplayName("모임 수정 테스트")
    public void modifyPostTest(TestInfo testInfo) {
        log.info(() -> testInfo.getDisplayName() + " 시작");
        //given
        Post given = data;
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
        Assertions.assertEquals(dto.getId(), 1L);
        Assertions.assertEquals(dto.getTitle(), "생성 시험2");
        Assertions.assertEquals(dto.getMeetingTime(), given.getMeetingDt());
        Assertions.assertEquals(dto.getDeadLine(), given.getMeetingDt().minusMinutes(60));

        log.info(() -> testInfo.getDisplayName() + " 완료");
    }

    @Test
    @DisplayName("모임 삭제 테스트")
    public void deletePostTest(TestInfo testInfo) {
        log.info(() -> testInfo.getDisplayName() + " 시작");
        //given
        Post given = data;
        // todo 로그인 구현 후 작성자 확인 필요
        // when(postService.validateWriter()).thenReturn(true);

        //when
        postService.deletePost(1L);

        //verify
        verify(postRepository, times(1)).deleteById(1L);
        log.info(() -> testInfo.getDisplayName() + " 완료");
    }

    @Test
    @DisplayName("모임 상세정보 조회 테스트")
    public void getPostDetailsTest(TestInfo testInfo) {
        log.info(() -> testInfo.getDisplayName() + " 시작");
        //given
        Post given = data;
        when(postRepository.findById(1L)).thenReturn(Optional.of(given));

        //when
        PostDetailResponse dto = postService.getPostDetails(1L);

        //then
        Assertions.assertEquals(dto.getId(), 1L);
        Assertions.assertEquals(dto.getTitle(), "생성 시험");
        Assertions.assertEquals(dto.getMeetingTime(), given.getMeetingDt());

        log.info(() -> testInfo.getDisplayName() + " 완료");
    }

    @Test
    @DisplayName("모임 정보목록 조회 테스트")
    public void getPostListTest(TestInfo testInfo) {
        log.info(() -> testInfo.getDisplayName() + " 시작");

        List<Post> givenList = new ArrayList<>();

        //given
        for (int i = 0; i < 20; i++) {
            Post post = Post.builder()
                .id((long) i)
                // todo 로그인 구현 후 수정 필요
//                .writer()
                .title("생성 시험" + i)
                .content("생성 시험 내용" + i)
                .meetingDt(LocalDateTime.now().minusMinutes(100 - i))
                .placeId("" + i)
                .placeName("서울시청")
                .address("서울특별시 중구 세종대로 126")
                .maxParticipants(10)
                .openChatUrl("https://open.kakao.com/몰라")
                .build();
            givenList.add(post);
        }
        Pageable pageable = PageRequest.of(0, 10, Sort.by("meetingTime").descending());
        Boolean recruitmentCompleted = false;
        Boolean completed = false;
        when(postRepository.findPostByCondition(pageable, recruitmentCompleted, completed)).thenReturn(
            new PageImpl<>(givenList, pageable, givenList.size()));

        //when
        Page<PostInfoResponse> dto = postService.getListOfPostInfo(pageable, recruitmentCompleted, completed);

        //then
        Assertions.assertEquals(dto.getNumber(), pageable.getPageNumber());
        Assertions.assertEquals(dto.getTotalElements(), givenList.size());

        log.info(() -> testInfo.getDisplayName() + " 완료");
    }

}
