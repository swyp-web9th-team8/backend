package com.swyp.plogging.backend.service;

import com.swyp.plogging.backend.post.domain.Participation;
import com.swyp.plogging.backend.post.domain.Post;
import com.swyp.plogging.backend.post.repository.ParticipationRepository;
import com.swyp.plogging.backend.post.sevice.ParticipationService;
import com.swyp.plogging.backend.post.sevice.PostService;
import com.swyp.plogging.backend.user.domain.AppUser;
import com.swyp.plogging.backend.user.domain.AuthProvider;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class ParticipationServiceTest {

    @InjectMocks
    ParticipationService participationService;

    @Mock
    PostService postService;
    @Mock
    ParticipationRepository participationRepository;


    private static Post data;
    private static Participation participation;
    private static AppUser user;

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
        user = AppUser.newInstance("user@user.com", "user1", "Soeul", AuthProvider.valueOf("GOOGLE"));
    }

    @Test
    @DisplayName("모임 참여 기능 구현")
    public void participationToPost(TestInfo testInfo) {
        log.info(() -> testInfo.getDisplayName() + " 시작");

        //given
        Long postId = 1L;
        when(postService.findById(postId)).thenReturn(data);


        //when
        participationService.participateToPost(postId, user);

        //then
        verify(participationRepository, times(1)).save(any(Participation.class));
        Assertions.assertEquals(1, data.getParticipations().size());
        log.info(() -> testInfo.getDisplayName() + " 완료");
    }

    @Test
    @DisplayName("모임 나가기 기능 구현")
    public void leaveFromPost(TestInfo testInfo) {
        log.info(() -> testInfo.getDisplayName() + " 시작");

        //given
        Long postId = 1L;
        data.addParticipation(Participation.newInstance(data, user));
        when(postService.findById(postId)).thenReturn(data);

        //when
        participationService.leaveFromPost(postId, user);

        //then
        verify(participationRepository, times(1)).delete(any(Participation.class));
        Assertions.assertEquals(0, data.getParticipations().size());
        log.info(() -> testInfo.getDisplayName() + " 완료");
    }

    @Test
    @DisplayName("모임 나가기 기능 예외 점검")
    public void exceptionOfLeaveFromPost(TestInfo testInfo) {
        log.info(() -> testInfo.getDisplayName() + " 시작");

        //given
        Long postId = 1L;
        data.addParticipation(Participation.newInstance(data, user));
        when(postService.findById(postId)).thenReturn(data);

        //when
        Exception e = Assertions.assertThrows(
                IllegalArgumentException.class,
                () ->
                        participationService
                                .leaveFromPost(postId, AppUser.newInstance("user2@user.com", "user2", "Seoul",AuthProvider.KAKAO)));

        //then
        Assertions.assertEquals("참가하지 않은 모임입니다.", e.getMessage());
        Assertions.assertEquals(1, data.getParticipations().size());
        log.info(() -> testInfo.getDisplayName() + " 완료");
    }
}
