package com.swyp.plogging.backend.certificate.service;

import com.swyp.plogging.backend.certificate.domain.Certification;
import com.swyp.plogging.backend.certificate.repository.CertificateRepository;
import com.swyp.plogging.backend.common.exception.UnauthorizedUserException;
import com.swyp.plogging.backend.common.service.FileService;
import com.swyp.plogging.backend.participation.service.ParticipationService;
import com.swyp.plogging.backend.post.controller.dto.PostInfoResponse;
import com.swyp.plogging.backend.post.domain.Post;
import com.swyp.plogging.backend.post.sevice.PostService;
import com.swyp.plogging.backend.user.domain.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Service
@RequiredArgsConstructor
public class CertificationService {
    private final FileService fileService;
    private final PostService postService;
    private final ParticipationService participationService;
    private final CertificateRepository repository;


    @Transactional
    public String uploadImageToPost(Long postId, AppUser user, MultipartFile file) {
        Post myPost = postService.findById(postId);
        if(!myPost.isWriter(user)){
            throw new UnauthorizedUserException("올바른 접근이 아닙니다.");
        }

        // certificate 가져오기(없으면 생성)
        Certification certification = getOrNewByPost(myPost);

        String imageUrl = fileService.uploadImageAndGetFileName(file);
        imageUrl = "/images/" + imageUrl;
        certification.addImageUrl(imageUrl);

        return imageUrl;
    }

    @Transactional
    public PostInfoResponse certificate(Long postId, AppUser user, List<Long> userIds) {
        Post myPost = postService.findById(postId);
        if(!myPost.isWriter(user)){
            throw new UnauthorizedUserException("올바른 접근이 아닙니다.");
        }

        // 모임 참여후 인증목록에 있을 때, participation.join = true
        myPost.getParticipations().forEach(participation -> {
            if(userIds.contains(participation.getUser().getId())){
                participation.joined();
            }
        });

        myPost.getCertification().certificate();

        return new PostInfoResponse(myPost);
    }

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
