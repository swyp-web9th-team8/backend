package com.swyp.plogging.backend.post.controller;

import com.swyp.plogging.backend.certificate.dto.CertificationRequest;
import com.swyp.plogging.backend.certificate.service.CertificationService;
import com.swyp.plogging.backend.common.dto.ApiPagedResponse;
import com.swyp.plogging.backend.common.dto.ApiResponse;
import com.swyp.plogging.backend.common.util.SecurityUtils;
import com.swyp.plogging.backend.participation.service.ParticipationService;
import com.swyp.plogging.backend.post.controller.dto.CreatePostRequest;
import com.swyp.plogging.backend.post.controller.dto.PostDetailResponse;
import com.swyp.plogging.backend.post.controller.dto.PostInfoResponse;
import com.swyp.plogging.backend.post.sevice.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/post")
public class PostController {

    private final PostService postService;
    private final ParticipationService participationService;
    private final CertificationService certificateService;

    // 기존 API 엔드포인트 유지
    @PostMapping("create")
    public ApiResponse<PostDetailResponse> createPost(@RequestBody CreatePostRequest request,
                                                      @AuthenticationPrincipal OAuth2User user) {
        // 회원 확인용
        SecurityUtils.getUserOrThrow(user);

        try {
            // 위치 정보가 있는 경우, 새로운 메서드로 호출
            if (request.getLatitude() != null && request.getLongitude() != null) {
                PostDetailResponse response = postService.createPost(request, SecurityUtils.getUserOrThrow(user));
                return ApiResponse.ok(response, "Successfully created the post with location.");
            } else {
                // 기존 로직 유지
                PostDetailResponse response = postService.createPost(SecurityUtils.getUserOrThrow(user), request.getTitle(),
                        request.getContent(),
                        request.getMeetingTime(), request.getPlaceId(),
                        request.getPlaceName(), request.getAddress(),
                        request.getMaxParticipants(), request.getOpenChatUrl(), null);

                return ApiResponse.ok(response, "Successfully fetched the post details.");
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PatchMapping("/{postId}/modify")
    public ApiResponse<PostDetailResponse> modifyPost(@PathVariable(name = "postId") Long postId,
                                                      @RequestBody CreatePostRequest request,
                                                      @AuthenticationPrincipal OAuth2User user) {
        if (!postId.equals(request.getId())) {
            throw new IllegalArgumentException("요청한 모임이 다릅니다.");
        }

        try {
            // 위치 정보가 있는 경우, 새로운 메서드로 호출
            if (request.getLatitude() != null && request.getLongitude() != null) {
                PostDetailResponse response = postService.modifyPost(
                        SecurityUtils.getUserOrThrow(user),
                        postId,
                        request
                );
                return ApiResponse.ok(response, "Successfully modified the post with location.");
            } else {
                // 기존 로직 유지
                PostDetailResponse response = postService.modifyPost(SecurityUtils.getUserOrThrow(user),
                        postId,
                        request.getTitle(), request.getContent(),
                        request.getMeetingTime(), request.getPlaceId(),
                        request.getPlaceName(), request.getAddress(),
                        request.getMaxParticipants(), request.getOpenChatUrl(), null
                );

                return ApiResponse.ok(response, "Successfully fetched the post details.");
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // 위치 기반 주변 모임 검색 API 추가
    @GetMapping("/nearby")
    public ApiPagedResponse<PostInfoResponse> getNearbyPosts(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5") Double radiusKm,
            @PageableDefault(size = 10, sort = "meetingTime", direction = Sort.Direction.ASC) Pageable pageable) {

        try {
            log.info("주변 모임 검색 요청: 위치=({}, {}), 반경={}km", latitude, longitude, radiusKm);
            Page<PostInfoResponse> response = postService.findNearbyPosts(latitude, longitude, radiusKm, pageable);
            return ApiPagedResponse.ok(response, "주변 모임 검색 결과입니다.");
        } catch (Exception e) {
            log.error("주변 모임 검색 중 오류 발생", e);
            return ApiPagedResponse.error(e.getMessage());
        }
    }

    // 기존 메서드들 유지...
    @DeleteMapping("/{postId}/delete")
    public ApiResponse<Object> deletePost(@PathVariable(name = "postId") Long postId,
                                          @AuthenticationPrincipal OAuth2User user) {
        try {
            postService.deletePost(postId, SecurityUtils.getUserOrThrow(user));

            return ApiResponse.ok(null, "Successfully deleted the post.");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{postId}")
    public ApiResponse<PostDetailResponse> postDetail(@PathVariable(name = "postId") Long postId) {
        try {
            PostDetailResponse response = postService.getPostDetails(postId);
            return ApiResponse.ok(response, "Successfully fetched the post details.");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // 초기 모임목록 조회
    @Operation(summary = "초기 모임목록 조회", description = "초기 API로 현재는 미사용")
    @GetMapping("/list")
    public ApiPagedResponse<PostInfoResponse> getListOfPosts(
            @PageableDefault(size = 10, sort = "meetingTime", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(name = "recruitmentCompleted", defaultValue = "false", required = false) Boolean recruitmentCompleted,
            @RequestParam(name = "completed", defaultValue = "false", required = false) Boolean completed) {
        try {
            Page<PostInfoResponse> response = postService.getListOfPostInfo(pageable,null,null);

            return ApiPagedResponse.ok(response, "Successfully fetched the list.");
        } catch (Exception e) {
            return ApiPagedResponse.error(e.getMessage());
        }
    }

    // 현재 모집중인 모임목록 조회
    @Operation(summary = "현재 모집 중인 모임목록 조회", description = "현재 모집중인 모임의 목록을 요약된 정보로 페이지네이션하여 조회합니다.")
    @Parameters({
            @Parameter(name = "pos", description = "도로명 주소", in = ParameterIn.QUERY, required = true),
            @Parameter(name = "v", description = "검색어", in = ParameterIn.QUERY, required = false),
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", in = ParameterIn.QUERY, example = "0"),
            @Parameter(name = "size", description = "한 페이지 크기", in = ParameterIn.QUERY, example = "10"),
            @Parameter(name = "sort", description = "정렬 기준 필드,asc|desc (예: meetingDt,desc)", in = ParameterIn.QUERY, example = "meetingDt,desc")
    })
    @GetMapping("/list/ing")
    public ApiPagedResponse<PostInfoResponse> getListOfActivePosts(
            @RequestParam(required = true, name = "pos")String position,
            @RequestParam(name = "v")String value,
            @PageableDefault(size = 10, sort = "meetingDt", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            Page<PostInfoResponse> response = postService.getListOfPostInfo(pageable,position, value);

            return ApiPagedResponse.ok(response, "Successfully fetched the list.");
        } catch (Exception e) {
            return ApiPagedResponse.error(e.getMessage());
        }
    }

    // 현재 모집완료된 모임목록 조회
    @Operation(summary = "현재 모집만 완료된 모임목록 조회", description = "현재 모집만 완료된 모임의 목록을 요약된 정보로 페이지네이션하여 조회합니다.")
    @Parameters({
            @Parameter(name = "pos", description = "도로명 주소", in = ParameterIn.QUERY, required = true),
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", in = ParameterIn.QUERY, example = "0"),
            @Parameter(name = "size", description = "한 페이지 크기", in = ParameterIn.QUERY, example = "10"),
            @Parameter(name = "sort", description = "정렬 기준 필드,asc|desc (예: meetingDt,desc)", in = ParameterIn.QUERY, example = "meetingDt,desc")
    })
    @GetMapping("/list/rec")
    public ApiPagedResponse<PostInfoResponse> getListOfInactivePosts(
            @RequestParam(required = true, name = "pos")String position,
            @PageableDefault(size = 10, sort = "meetingDt", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            Page<PostInfoResponse> response = postService.getListOfCompletePostInfo(pageable,true, false);

            return ApiPagedResponse.ok(response, "Successfully fetched the list.");
        } catch (Exception e) {
            return ApiPagedResponse.error(e.getMessage());
        }
    }

    @Operation(summary = "현재 모집만 완료된 모임목록 조회", description = "현재 모집만 완료된 모임의 목록을 요약된 정보로 페이지네이션하여 조회합니다.")
    @Parameters({
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", in = ParameterIn.QUERY, example = "0"),
            @Parameter(name = "size", description = "한 페이지 크기", in = ParameterIn.QUERY, example = "10"),
            @Parameter(name = "sort", description = "정렬 기준 필드,asc|desc (예: meetingDt,desc)", in = ParameterIn.QUERY, example = "meetingDt,desc")
    })
    @GetMapping("/list/com")
    public ApiPagedResponse<PostInfoResponse> getListOfCompletePosts(
            @PageableDefault(size = 10, sort = "meetingDt", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            Page<PostInfoResponse> response = postService.getListOfCompletePostInfo(pageable, false, true);

            return ApiPagedResponse.ok(response, "Successfully fetched the list.");
        } catch (Exception e) {
            return ApiPagedResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{postId}/participate")
    public ApiResponse<Object> participatePost(@PathVariable(name = "postId") Long postId,
                                               @AuthenticationPrincipal OAuth2User user) {
        try {
            participationService.participateToPost(postId, SecurityUtils.getUserOrThrow(user));
            return ApiResponse.ok(null, "Successfully joined the group.");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{postId}/leave")
    public ApiResponse<Object> leavePost(@PathVariable(name = "postId") Long postId,
                                         @AuthenticationPrincipal OAuth2User user) {
        try {
            participationService.leaveFromPost(postId, SecurityUtils.getUserOrThrow(user));
            return ApiResponse.ok(null, "Successfully left the group.");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{postId}/certificate")
    public ApiResponse<Object> certificatePost(@PathVariable(name = "postId") Long postId,
                                               @AuthenticationPrincipal OAuth2User user,
                                               @RequestBody CertificationRequest request){
        try {
            PostInfoResponse response = certificateService.certificate(postId, SecurityUtils.getUserOrThrow(user), request.getUserIds());
            return ApiResponse.ok(null, "Successfully certificate the post.");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{postId}/certificate")
    public ApiResponse<Object> cancelCertificationOfPost(@PathVariable(name = "postId") Long postId,
                                               @AuthenticationPrincipal OAuth2User user){
        try {
            certificateService.cancelCertificate(postId, SecurityUtils.getUserOrThrow(user));
            return ApiResponse.ok(null, "Successfully cancel certification.");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{postId}/image")
    public ApiResponse<String> uploadImageToPost(@PathVariable(name = "postId") Long postId,
                                                 @AuthenticationPrincipal OAuth2User user,
                                                 @RequestParam("file")MultipartFile file){
        try {
            String imageUrl = certificateService.uploadImageToPost(postId, SecurityUtils.getUserOrThrow(user), file);
            return ApiResponse.ok(imageUrl, "Successfully upload the image to post.");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }


}