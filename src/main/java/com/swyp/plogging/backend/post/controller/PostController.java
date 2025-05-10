package com.swyp.plogging.backend.post.controller;

import com.swyp.plogging.backend.common.dto.ApiPagedResponse;
import com.swyp.plogging.backend.common.dto.ApiResponse;
import com.swyp.plogging.backend.common.util.SecurityUtils;
import com.swyp.plogging.backend.participation.service.ParticipationService;
import com.swyp.plogging.backend.post.controller.dto.CreatePostRequest;
import com.swyp.plogging.backend.post.controller.dto.PostDetailResponse;
import com.swyp.plogging.backend.post.controller.dto.PostInfoResponse;
import com.swyp.plogging.backend.post.sevice.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/post")
public class PostController {

    private final PostService postService;
    private final ParticipationService participationService;

    @PostMapping("create")
    public ApiResponse<PostDetailResponse> createPost(@RequestBody CreatePostRequest request,
        @AuthenticationPrincipal OAuth2User user) {
        // 회원 확인용
        SecurityUtils.getUserOrThrow(user);

        try {
            PostDetailResponse response = postService.createPost(SecurityUtils.getUserOrThrow(user), request.getTitle(),
                request.getContent(),
                request.getMeetingTime(), request.getPlaceId(),
                request.getPlaceName(), request.getAddress(),
                request.getMaxParticipants(), request.getOpenChatUrl(), null);

            return ApiResponse.ok(response, "Successfully fetched the post details.");
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
            PostDetailResponse response = postService.modifyPost(SecurityUtils.getUserOrThrow(user),
                postId,
                request.getTitle(), request.getContent(),
                request.getMeetingTime(), request.getPlaceId(),
                request.getPlaceName(), request.getAddress(),
                request.getMaxParticipants(), request.getOpenChatUrl(), null
            );

            return ApiResponse.ok(response, "Successfully fetched the post details.");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

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

    @GetMapping("/list")
    public ApiPagedResponse<PostInfoResponse> getListOfPosts(
        @PageableDefault(size = 10, sort = "meetingTime", direction = Sort.Direction.DESC) Pageable pageable,
        @RequestParam(name = "recruitmentCompleted", defaultValue = "false", required = false) Boolean recruitmentCompleted,
        @RequestParam(name = "completed", defaultValue = "false", required = false) Boolean completed) {
        try {
            Page<PostInfoResponse> response = postService.getListOfPostInfo(pageable, recruitmentCompleted, completed);

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
}
