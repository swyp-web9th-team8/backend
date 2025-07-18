package com.swyp.plogging.backend.certificate.service;

import com.swyp.plogging.backend.post.certificate.domain.Certification;
import com.swyp.plogging.backend.post.certificate.repository.CertificateRepository;
import com.swyp.plogging.backend.common.exception.CertificationException;
import com.swyp.plogging.backend.common.service.FileService;
import com.swyp.plogging.backend.post.certificate.service.CertificationService;
import com.swyp.plogging.backend.post.participation.domain.Participation;
import com.swyp.plogging.backend.post.participation.service.ParticipationService;
import com.swyp.plogging.backend.post.post.domain.Post;
import com.swyp.plogging.backend.post.post.sevice.PostService;
import com.swyp.plogging.backend.post.sevice.PostServiceTest;
import com.swyp.plogging.backend.user.user.domain.AppUser;
import com.swyp.plogging.backend.user.user.domain.AuthProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class CertificateServiceTest {
    @InjectMocks
    CertificationService service;

    @Mock
    private FileService fileService;
    @Mock
    private PostService postService;
    @Mock
    private ParticipationService participationService;
    @Mock
    private CertificateRepository repository;

    private static final Logger log = LoggerFactory.getLogger(PostServiceTest.class);

    @Test
    @DisplayName("이미지 업로드 테스트")
    public void uploadImage(TestInfo testInfo) {
        log.info(() -> testInfo.getDisplayName() + " 시작");
        //given
        AppUser user = AppUser.newInstance(
                "user@user.com",
                "user",
                "Seoul",
                AuthProvider.GOOGLE,
                null
        );
        List<MultipartFile> files = List.of(new MockMultipartFile(
                "file",
                "profileImage.png",
                MediaType.IMAGE_PNG_VALUE,
                "testFileData".getBytes()
        ));
        Post post = Post.builder()
                .id(1L)
                .writer(user)
                .title("생성 시험")
                .content("생성 시험 내용")
                .meetingDt(LocalDateTime.parse("2025-04-29T10:40:32"))
                .placeId("1")
                .placeName("서울시청")
                .address("서울특별시 중구 세종대로 126")
                .maxParticipants(10)
                .openChatUrl("https://open.kakao.com/몰라")
                .build();
        String randomName = UUID.randomUUID() + ".png";
        List<String> expect = List.of("/images/" + randomName);
        Certification certification = Certification.newInstance(post,"/images/" + randomName);

        when(postService.findById(1L)).thenReturn(post);
        when(repository.save(any(Certification.class))).thenReturn(certification);
        when(fileService.uploadImageAndGetFileName(any(MultipartFile.class))).thenReturn(randomName);

        //when
        List<String> actual = service.uploadImageToPost(1L, user, files);

        //then
        Assertions.assertEquals(expect, actual);
        Assertions.assertTrue(certification.getImageUrls().contains(expect.get(0)));

        log.info(() -> testInfo.getDisplayName() + " 완료");
    }

    @Test
    @DisplayName("certificate 테스트")
    public void certificate(TestInfo testInfo) throws Exception {
        log.info(() -> testInfo.getDisplayName() + " 시작");
        //given
        AppUser user = AppUser.newInstance(
                "user@user.com",
                "user",
                "Seoul",
                AuthProvider.GOOGLE,
                null
        );
        List<MultipartFile> files = List.of(new MockMultipartFile(
                "file",
                "profileImage.png",
                MediaType.IMAGE_PNG_VALUE,
                "testFileData".getBytes()
        ));
        Post post = Post.builder()
                .id(1L)
                .writer(user)
                .title("생성 시험")
                .content("생성 시험 내용")
                .meetingDt(LocalDateTime.parse("2025-04-29T10:40:32"))
                .placeId("1")
                .placeName("서울시청")
                .address("서울특별시 중구 세종대로 126")
                .maxParticipants(10)
                .openChatUrl("https://open.kakao.com/몰라")
                .completed(true)
                .build();
        for (long i = 2L; i < 7; i++) {
            AppUser userInstance = AppUser.newInstance(
                    "user" + i + "@user.com",
                    "user" + i,
                    "Seoul",
                    AuthProvider.GOOGLE,
                    null
            );
            setEntityId(userInstance, i);

            post.addParticipation(
                    Participation.newInstance(post, userInstance)
            );
        }

        String randomName = UUID.randomUUID() + ".png";
        String expect = "/images/" + randomName;
        Certification certification = Certification.newInstance(post, "/images/" + randomName);

        when(postService.findById(1L)).thenReturn(post);
        when(repository.save(any(Certification.class))).thenReturn(certification);
        when(fileService.uploadImageAndGetFileName(any(MultipartFile.class))).thenReturn(randomName);


        //when
        List<String> actual = service.uploadImageToPost(post.getId(), user, files);
        service.certificate(post.getId(), user, List.of(2L, 3L, 5L));

        //then
        //참석 확인
        for (Participation participation : post.getParticipations()) {
            if (List.of(4L, 6L).contains(participation.getUser().getId())) {
                Assertions.assertFalse(participation.isJoined());
            } else {
                Assertions.assertTrue(participation.isJoined());
            }
        }
        // 이미지가 포함되어 있는지 확인
        Assertions.assertTrue(certification.getImageUrls().contains(expect));

        log.info(() -> testInfo.getDisplayName() + " 완료");
    }

    @Test
    @DisplayName("certificate 테스트 - 참석하지 않은 user 포함 예외")
    public void certificateFromRequestContainingUserNotParticipating(TestInfo testInfo) throws Exception {
        log.info(() -> testInfo.getDisplayName() + " 시작");
        //given
        AppUser user = AppUser.newInstance(
                "user@user.com",
                "user",
                "Seoul",
                AuthProvider.GOOGLE,
                null
        );
        Post post = Post.builder()
                .id(1L)
                .writer(user)
                .title("생성 시험")
                .content("생성 시험 내용")
                .meetingDt(LocalDateTime.parse("2025-04-29T10:40:32"))
                .placeId("1")
                .placeName("서울시청")
                .address("서울특별시 중구 세종대로 126")
                .maxParticipants(10)
                .openChatUrl("https://open.kakao.com/몰라")
                .completed(true)
                .build();
        for (long i = 2L; i < 7; i++) {
            AppUser userInstance = AppUser.newInstance(
                    "user" + i + "@user.com",
                    "user" + i,
                    "Seoul",
                    AuthProvider.GOOGLE,
                    null
            );
            setEntityId(userInstance, i);

            post.addParticipation(
                    Participation.newInstance(post, userInstance)
            );
        }
        when(postService.findById(1L)).thenReturn(post);


        //when
        Exception exception = Assertions.assertThrows(CertificationException.class,
                () -> service.certificate(post.getId(), user, List.of(2L, 3L, 10L))
        );

        //then
        // 참가하지 않은 사람이 있는 경우 예외
        Assertions.assertEquals("참가하지 않은 모임입니다.", exception.getMessage());

        log.info(() -> testInfo.getDisplayName() + " 완료");
    }

    @Test
    @DisplayName("certificate 테스트 - 이미지 없는 인증")
    public void certificateWithNoImage(TestInfo testInfo) throws Exception {
        log.info(() -> testInfo.getDisplayName() + " 시작");
        //given
        AppUser user = AppUser.newInstance(
                "user@user.com",
                "user",
                "Seoul",
                AuthProvider.GOOGLE,
                null
        );
        Post post = Post.builder()
                .id(1L)
                .writer(user)
                .title("생성 시험")
                .content("생성 시험 내용")
                .meetingDt(LocalDateTime.parse("2025-04-29T10:40:32"))
                .placeId("1")
                .placeName("서울시청")
                .address("서울특별시 중구 세종대로 126")
                .maxParticipants(10)
                .openChatUrl("https://open.kakao.com/몰라")
                .completed(true)
                .build();
        for (long i = 2L; i < 7; i++) {
            AppUser userInstance = AppUser.newInstance(
                    "user" + i + "@user.com",
                    "user" + i,
                    "Seoul",
                    AuthProvider.GOOGLE,
                    null
            );
            setEntityId(userInstance, i);

            post.addParticipation(
                    Participation.newInstance(post, userInstance)
            );
        }

        Certification certification = Certification.newInstance(post);

        when(postService.findById(1L)).thenReturn(post);
//        when(repository.save(any(Certification.class))).thenReturn(certification);

        //when
        Exception exception = Assertions.assertThrows(CertificationException.class,
                () -> service.certificate(post.getId(), user, List.of(2L, 3L, 5L))
        );

        //then
        // 이미지가 최소 1개 이상 필요
        Assertions.assertEquals("최소 1개 이상의 이미지가 필요합니다.", exception.getMessage());

        log.info(() -> testInfo.getDisplayName() + " 완료");
    }

    // id 강제 주입
    private void setEntityId(Object entity, Long id) throws Exception {
        Field idField = entity.getClass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
    }

}
