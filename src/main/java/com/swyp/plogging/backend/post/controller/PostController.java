package com.swyp.plogging.backend.post.controller;

import com.swyp.plogging.backend.badge.event.CompletePostEvent;
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
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/post")
public class PostController {

    private final PostService postService;
    private final ParticipationService participationService;
    private final CertificationService certificateService;
    private final ApplicationEventPublisher eventPublisher;

    // 기존 API 엔드포인트 유지
    @Operation(
        summary = "모임을 생성하기 위한 API",
        description = "모임 생성을 위한 API. 인증이 필요함."
    )
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
                        request.getMaxParticipants(), request.getOpenChatUrl(), request.getDeadline());

                return ApiResponse.ok(response, "Successfully fetched the post details.");
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @Operation(
        summary = "모임을 수정하기 위한 API",
        description = "모임 수정을 위한 API. 인증이 필요함."
    )
    @Parameter(name = "postId", description = "수정시에는 postId가 필수이다.", required = true)
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
    @Operation(
        summary = "현재 위치를 기반으로 모임을 검색",
        description = "현재 위치를 기준으로 반경 Km이내의 모임을 검색"
    )
    @Parameters({
        @Parameter(name = "latitude", description = "위도"),
        @Parameter(name = "longitude", description = "경도"),
        @Parameter(name = "radiusKm", description = "반경 Km"),
        @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", in = ParameterIn.QUERY, example = "0"),
        @Parameter(name = "size", description = "한 페이지 크기", in = ParameterIn.QUERY, example = "10"),
        @Parameter(name = "sort", description = "정렬 기준 필드,asc|desc (예: meetingDt,desc)", in = ParameterIn.QUERY, example = "meetingDt,desc")
    }
    )
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
    @Operation(
        summary = "모임 삭제를 위한 API",
        description = "모임 삭제를 위한 API"
    )
    @Parameter(name = "postId", required = true)
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

    @Operation(
        summary = "모임 상세조회를 위한 API",
        description = "모임 상세조회를 위한 API. 모임 정보에 대한 상세한 데이터를 전부 반환"
    )
    @Parameter(name = "postId", required = true)
    @GetMapping("/{postId}")
    public ApiResponse<PostDetailResponse> postDetail(@PathVariable(name = "postId") Long postId,
        @AuthenticationPrincipal OAuth2User principal) {
        try {
            Long currentUserId = SecurityUtils.getUserId(principal);
            PostDetailResponse response = postService.getPostDetails(postId, currentUserId);
            return ApiResponse.ok(response, "Successfully fetched the post details.");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
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
        @RequestParam(required = true, name = "pos") String position,
        @RequestParam(required = false, name = "v") String value,
        @PageableDefault(size = 10, sort = "meetingDt", direction = Sort.Direction.DESC) Pageable pageable,
        @AuthenticationPrincipal OAuth2User user) {
        try {
            Page<PostInfoResponse> response = postService.getListOfPostInfo(pageable, position, value, SecurityUtils.getUserOrThrow(user));

            return ApiPagedResponse.ok(response, "Successfully fetched the list.");
        } catch (Exception e) {
            return ApiPagedResponse.error(e.getMessage());
        }
    }

    // 현재 모집완료된 모임목록 조회
    @Operation(summary = "현재 모집만 완료된 모임목록 조회", description = "현재 모집만 완료된 모임의 목록을 요약된 정보로 페이지네이션하여 조회합니다.")
    @Parameters({
        @Parameter(name = "pos", description = "서울특별시 OO구 OO동", in = ParameterIn.QUERY, required = true),
        @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", in = ParameterIn.QUERY, example = "0"),
        @Parameter(name = "size", description = "한 페이지 크기", in = ParameterIn.QUERY, example = "10"),
        @Parameter(name = "sort", description = "정렬 기준 필드,asc|desc (예: meetingDt,desc)", in = ParameterIn.QUERY, example = "meetingDt,desc")
    })
    @GetMapping("/list/rec")
    public ApiPagedResponse<PostInfoResponse> getListOfInactivePosts(
        @RequestParam(required = true, name = "pos") String position,
        @PageableDefault(size = 10, sort = "meetingDt", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            Page<PostInfoResponse> response = postService.getListOfCompletePostInfo(pageable, position,true, false);

            return ApiPagedResponse.ok(response, "Successfully fetched the list.");
        } catch (Exception e) {
            return ApiPagedResponse.error(e.getMessage());
        }
    }

    @Operation(summary = "현재 모임이 완료된 모임목록 조회", description = "현재 모임이 완료된 모임의 목록을 요약된 정보로 페이지네이션하여 조회합니다.")
    @Parameters({
            @Parameter(name = "pos", description = "서울특별시 OO구 OO동", in = ParameterIn.QUERY, required = false),
        @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", in = ParameterIn.QUERY, example = "0"),
        @Parameter(name = "size", description = "한 페이지 크기", in = ParameterIn.QUERY, example = "10"),
        @Parameter(name = "sort", description = "정렬 기준 필드,asc|desc (예: meetingDt,desc)", in = ParameterIn.QUERY, example = "meetingDt,desc")
    })
    @GetMapping("/list/com")
    public ApiPagedResponse<PostInfoResponse> getListOfCompletePosts(
            @RequestParam(name = "pos", defaultValue = "") String position,
        @PageableDefault(size = 10, sort = "meetingDt", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            Page<PostInfoResponse> response = postService.getListOfCompletePostInfo(pageable, position, false, true);

            return ApiPagedResponse.ok(response, "Successfully fetched the list.");
        } catch (Exception e) {
            return ApiPagedResponse.error(e.getMessage());
        }
    }

    @Operation(
        summary = "모임에 참석하기 위한 API",
        description = "모임에 참석하기 위한 API로 인증이 완료된 상태여야 한다."
    )
    @Parameter(name = "postId", description = "참석하려는 모임의 id")
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

    @Operation(
        summary = "모임에서 나가기 위한 API",
        description = "모임에서 나가기 위한 API로 인증이 완료된 상태여야 한다."
    )
    @Parameter(name = "postId", description = "나가려는 모임의 id")
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

    @Operation(
        summary = "모임의 리뷰를 작성하기 위한 API",
        description = "모임완료 후 리뷰를 작성하기 위한 API.\n리뷰작성을 위한 이미지는 '/{postId}/image'를 통해서 업로드해야한다.\n이미지를 다 업로드 하고 나서 참석한 사람들의 user.id를 body에 포함하여 요청"
    )
    @Parameters(
        @Parameter(name = "postId", description = "참석하려는 모임의 id")
    )
    @PostMapping("/{postId}/certificate")
    public ApiResponse<Object> certificatePost(@PathVariable(name = "postId") Long postId,
        @AuthenticationPrincipal OAuth2User user,
        @RequestBody CertificationRequest request) {
        try {
            PostInfoResponse response = certificateService.certificate(postId, SecurityUtils.getUserOrThrow(user), request.getUserIds());
            eventPublisher.publishEvent(new CompletePostEvent(SecurityUtils.getUserOrThrow(user)));
            return ApiResponse.ok(null, "Successfully certificate the post.");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @Operation(
        summary = "모임 리뷰 작성중 취소할 경우 서버에 업로드한 이미지를 처리하기 위한 API",
        description = "모임 리뷰 작성 중 화면을 나가거나, 취소 등 리뷰작성을 취소할 경우 해당 API를 호출할 수 있도록 구현 요청.\n" +
            "리뷰작성을 위해 업로드된 이미지가 유기되어 서버 용량을 차지하지 않도록 하기 위한 API.\n" +
            "리뷰작성 시 업로드한 이미지를 삭제하도록 구현되어 있음."
    )
    @Parameter(name = "postId", description = "리뷰를 작성중인 모임의 id")
    @DeleteMapping("/{postId}/certificate")
    public ApiResponse<Object> cancelCertificationOfPost(@PathVariable(name = "postId") Long postId,
        @AuthenticationPrincipal OAuth2User user) {
        try {
            certificateService.cancelCertificate(postId, SecurityUtils.getUserOrThrow(user));
            return ApiResponse.ok(null, "Successfully cancel certification.");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @Operation(
        summary = "모임의 리뷰를 작성하기 위한 이미지 업로드 API",
        description = "모임 리뷰를 작성하기 위한 이미지 API로 이미지 파일을 받아 해당 모임 데이터와 연관지어 이미지를 저장.\n" +
            "이미지는 바이너리로 전달. 여러 이미지를 한번에 처리할 수 있도록 개선(수정)"
    )
    @PostMapping("/{postId}/image")
    public ApiResponse<List<String>> uploadImageToPost(
        @Parameter(description = "리뷰를 작성중인 모임의 id")
        @PathVariable(name = "postId") Long postId,

        @AuthenticationPrincipal OAuth2User user,

        @Parameter(description = "업로드할 이미지 파일(binary) 배열", required = true, content = @Content(
            mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
            array = @ArraySchema(schema = @Schema(type = "string", format = "binary"))
        ))
        @RequestParam("files") List<MultipartFile> files) {
        try {
            List<String> imageUrls = certificateService.uploadImageToPost(postId, SecurityUtils.getUserOrThrow(user), files);
            return ApiResponse.ok(imageUrls, "Successfully upload the image to post.");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }


}