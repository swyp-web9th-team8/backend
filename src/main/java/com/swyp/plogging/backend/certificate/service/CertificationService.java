package com.swyp.plogging.backend.certificate.service;

import com.swyp.plogging.backend.certificate.domain.Certification;
import com.swyp.plogging.backend.certificate.repository.CertificateRepository;
import com.swyp.plogging.backend.common.exception.CertificationException;
import com.swyp.plogging.backend.common.exception.UnauthorizedUserException;
import com.swyp.plogging.backend.common.service.FileService;
import com.swyp.plogging.backend.participation.domain.Participation;
import com.swyp.plogging.backend.participation.service.ParticipationService;
import com.swyp.plogging.backend.post.controller.dto.PostInfoResponse;
import com.swyp.plogging.backend.post.domain.Post;
import com.swyp.plogging.backend.post.sevice.PostService;
import com.swyp.plogging.backend.user.domain.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;


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
        if(!myPost.isWriter(user)){
            throw new UnauthorizedUserException("올바른 접근이 아닙니다.");
        }

        // certificate 가져오기(없으면 생성)
        Certification certification = getOrNewByPost(myPost);

        List<String> imageUrls = new ArrayList<>();
        for(MultipartFile file : files) {
            // 이미지 저장후 고아로 남기지 않기 위함
            String imageUrl = fileService.uploadImageAndGetFileName(file);
            imageUrl = "/images/" + imageUrl;
            certification.addImageUrl(imageUrl);
            imageUrls.add(imageUrl);
        }

        return imageUrls;
    }

    @Transactional
    public PostInfoResponse certificate(Long postId, AppUser user, List<Long> userIds) {
        Post myPost = postService.findById(postId);
        if(!myPost.isWriter(user)){
            throw new UnauthorizedUserException("올바른 접근이 아닙니다.");
        }

        // 모임 참여후 participation.join = true, 참여 기록이 없다면 예외
        userIds.forEach(id -> {
            for(Participation participation : myPost.getParticipations()){
                if(participation.getUser().getId().equals(id)){
                    participation.joined();
                    return;
                }
            }
            throw CertificationException.notParticipated();
        });


        Certification certification = getOrNewByPost(myPost);
        // 최소 1개 이상의 이미지 필요
        if(!certification.getImageUrls().isEmpty()){
            certification.certificate();
        }else{
            throw CertificationException.needMinOneImage();
        }


        return new PostInfoResponse(myPost, certification);
    }

    /*
    용량 관계상 인증을 취소할 경우 인증과정 중 업로드한 이미지 삭제
     */
    @Transactional
    public void cancelCertificate(Long postId,AppUser user){
        Post myPost = postService.findById(postId);
        if(!myPost.isWriter(user)){
            throw new UnauthorizedUserException("올바른 접근이 아닙니다.");
        }

        List<String> urls = myPost.getCertification().getImageUrls();
        for(String url : urls){
            fileService.deleteSavedFileWithUrl(url);
        }

        repository.delete(myPost.getCertification());
    }

    private Certification getOrNewByPost(Post post) {
        return repository.findByPostId(post.getId())
                .orElseGet(() -> repository.save(Certification.newInstance(post)));
    }
}
