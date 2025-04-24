package com.swyp.plogging.backend.controller;

import com.swyp.plogging.backend.controller.DTO.UserInfoUpdateRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @GetMapping("/me")
    public String me(){
        return "유저 정보";
    }

    @GetMapping("/{userId}/rankings")
    public String fetchRankingOfUser(@PathVariable(name = "userId")Long userId){
        return "Successfully retrieved rankings and badges.";
    }

    @PatchMapping("/{userId}")
    public String updateUserInfo(@PathVariable(name = "userId")Long userId,
                                 @RequestBody UserInfoUpdateRequest request){
        return "User information updated success.";
    }

    @GetMapping("/{userId}/participated-posts")
    public String fetchParticipatedPostsOfUser(@PathVariable(name = "userId")Long userId){
        return "Successfully retrieved participated events.";
    }

    @GetMapping("/{userId}/created-posts")
    public String fetchCreatedPsotsOfUser(@PathVariable(name = "userId")Long userId){
        return "Successfully retrieved created events.";
    }
}
