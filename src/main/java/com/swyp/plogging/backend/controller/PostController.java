package com.swyp.plogging.backend.controller;

import com.swyp.plogging.backend.controller.DTO.CreatePostRequest;
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
    public String createPost(@RequestBody CreatePostRequest request){
        return "Successfully fetched the post details.";
    }

    @PatchMapping("/{postId}/modify")
    public String modifyPost(@PathVariable(name = "postId")Long postId,
                             @RequestBody CreatePostRequest request){
        return "Successfully fetched the post details.";
    }

    @DeleteMapping("/{postId}/delete")
    public String deletePost(@PathVariable(name = "postId")Long postId){
        return "Successfully deleted the post.";
    }

    @GetMapping("/{postId}")
    public String postDetail(@PathVariable(name = "postId")Long postId){
        return "Successfully fetched the post details.";
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
