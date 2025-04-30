package com.swyp.plogging.backend.controller;

import com.swyp.plogging.backend.controller.DTO.APIResponse;
import com.swyp.plogging.backend.controller.DTO.CreatePostRequest;
import com.swyp.plogging.backend.controller.DTO.PostDetailResponse;
import com.swyp.plogging.backend.sevice.PostService;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.awt.print.Pageable;

@RestController
@RequestMapping("/api/post")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService){
        this.postService = postService;
    }

    @PostMapping("create")
    public APIResponse<PostDetailResponse> createPost(@RequestBody CreatePostRequest request){
        APIResponse<PostDetailResponse> APIResponse = new APIResponse<>();
        try {
            PostDetailResponse response = postService.createPost(request.getTitle(), request.getContent(),
                    request.getMeetingTime(), request.getPlaceId(),
                    request.getPlaceName(), request.getAddress(),
                    request.getMaxParticipants(), request.getOpenChatUrl(), null);

            return APIResponse.ok(response, "Successfully fetched the post details.");
        }catch(Exception e){
            return APIResponse.error(e.getMessage());
        }
    }

    @PatchMapping("/{postId}/modify")
    public APIResponse<PostDetailResponse> modifyPost(@PathVariable(name = "postId")Long postId,
                             @RequestBody CreatePostRequest request){
        APIResponse<PostDetailResponse> APIresponse = new APIResponse<>();
        try {
            PostDetailResponse response = postService.modifyPost(
                    request.getId(),
                    request.getTitle(), request.getContent(),
                    request.getMeetingTime(), request.getPlaceId(),
                    request.getPlaceName(), request.getAddress(),
                    request.getMaxParticipants(), request.getOpenChatUrl(), null
            );

            return APIresponse.ok(response, "Successfully fetched the post details.");
        }catch (Exception e){
            return APIresponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{postId}/delete")
    public APIResponse<Object> deletePost(@PathVariable(name = "postId")Long postId){
        APIResponse<Object> APIResponse = new APIResponse<>();

        try{
            postService.deletePost(postId);

            return APIResponse.ok(null, "Successfully deleted the post.");
        }catch(Exception e){
            return APIResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{postId}")
    public APIResponse<PostDetailResponse> postDetail(@PathVariable(name = "postId")Long postId){
        APIResponse<PostDetailResponse> APIResponse = new APIResponse<>();

        try{
            PostDetailResponse response = postService.getPostDetails(postId);
            return APIResponse.ok(response, "Successfully fetched the post details.");
        }catch(Exception e){
            return APIResponse.error(e.getMessage());
        }
    }

    @GetMapping("/list")
    public String getListOfPosts(@PageableDefault(size = 10, sort = "meetingTime",  direction = Sort.Direction.DESC)Pageable pageable,
                                 @RequestParam(name = "recruitmentCompleted", defaultValue = "false", required = false) Boolean recruitmentCompleted,
                                 @RequestParam(name = "completed", defaultValue = "false", required = false)Boolean completed){
        return "Successfully fetched the list.";
    }

    @PostMapping("/{postId}/participate")
    public String participatePost(@PathVariable(name = "postId")Long postId){
        return "Successfully joined the group.";

    }
    @PostMapping("/{postId}/leave")
    public String leavePost(@PathVariable(name = "postId")Long postId){
        return "Successfully left the group.";
    }

}
