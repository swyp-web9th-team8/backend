package com.swyp.plogging.backend.post.certificate.service;

import com.swyp.plogging.backend.post.certificate.domain.Certification;
import com.swyp.plogging.backend.post.certificate.repository.CertificateRepository;
import com.swyp.plogging.backend.common.exception.CertificationException;
import com.swyp.plogging.backend.common.exception.UnauthorizedUserException;
import com.swyp.plogging.backend.common.service.FileService;
import com.swyp.plogging.backend.post.participation.domain.Participation;
import com.swyp.plogging.backend.post.participation.service.ParticipationService;
import com.swyp.plogging.backend.post.post.controller.dto.PostInfoResponse;
import com.swyp.plogging.backend.post.post.domain.Post;
import com.swyp.plogging.backend.post.post.sevice.PostService;
import com.swyp.plogging.backend.user.user.domain.AppUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificationService {

    private final FileService fileService;
    private final PostService postService;
    private final ParticipationService participationService;
    private final CertificateRepository repository;

    @Transactional
    public List<String> uploadImageToPost(Long postId, AppUser user, List<MultipartFile> files) {
        Post myPost = postService.findById(postId);
        if (!myPost.isWriter(user)) {
            throw new UnauthorizedUserException("올바른 접근이 아닙니다.");
        }

        String imageUrl;
        Certification certification = myPost.getCertification();
        for (MultipartFile file : files) {
            // 이미지 저장후 고아로 남기지 않기 위함
            imageUrl = fileService.uploadImageAndGetFileName(file);
            imageUrl = "/images/" + imageUrl;

            // certification 없으면 생성
            if (certification == null) {
                certification = Certification.newInstance(myPost, imageUrl);
                certification = repository.save(certification);
                myPost.setCertification(certification);
            } else {
                certification.getImageUrls().add(imageUrl);
            }
        }

        // 파일이 없고 Certification도 없으면 빈 리스트 반환
        if(certification == null){
            return new ArrayList<>();
        }

        return certification.getImageUrls();
    }

    @Transactional
    public PostInfoResponse certificate(Long postId, AppUser user, List<Long> userIds) {
        Post myPost = postService.findById(postId);
        if (!myPost.isWriter(user)) {
            throw new UnauthorizedUserException("올바른 접근이 아닙니다.");
        }

        // 모임 참여후 participation.join = true, 참여 기록이 없다면 예외
        userIds.forEach(id -> {
            for (Participation participation : myPost.getParticipations()) {
                if (participation.getUser().getId().equals(id)) {
                    participation.joined();
                    return;
                }
            }
            throw CertificationException.notParticipated();
        });

        Certification certification = myPost.getCertification();
        // 최소 1개 이상의 이미지 필요
        if (certification != null && !certification.getImageUrls().isEmpty()) {
            certification.certificate();
        } else {
            throw CertificationException.needMinOneImage();
        }

        myPost.complete();
        log.info("---인증완료 {} : postId = {}",user.getId(), myPost.getId());
        return new PostInfoResponse(myPost, certification);
    }

    /*
    용량 관계상 인증을 취소할 경우 인증과정 중 업로드한 이미지 삭제
     */
    @Transactional
    public void cancelCertificate(Long postId, AppUser user) {
        Post myPost = postService.findById(postId);
        if (!myPost.isWriter(user)) {
            throw new UnauthorizedUserException("올바른 접근이 아닙니다.");
        }

        List<String> urls = myPost.getCertification().getImageUrls();
        for (String url : urls) {
            fileService.deleteSavedFileWithUrl(url);
        }

        repository.delete(myPost.getCertification());
    }
}
