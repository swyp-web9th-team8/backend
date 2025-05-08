package com.swyp.plogging.backend.user.domain;

import com.swyp.plogging.backend.domain.Participation;
import com.swyp.plogging.backend.domain.Post;
import com.swyp.plogging.backend.domain.UserBadge;
import com.swyp.plogging.backend.domain.base.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppUser extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String region;

    @Column
    private String phoneNum;

    private boolean pushEnabled;

    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider;

    private boolean registered = false;

    private String profileImageUrl;

    private int score;

    @OneToMany(mappedBy = "writer")
    private List<Post> writtenPosts = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Participation> participations = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserBadge> userBadges = new ArrayList<>();

    public static AppUser newInstance(String email, String nickname, String region, AuthProvider authProvider) {
        AppUser user = new AppUser();
        user.email = email;
        user.nickname = nickname;
        user.region = region;
        user.authProvider = authProvider;
        user.registered = false;

        return user;
    }

    public void updateProfile(String nickname, String region, String profileImageUrl, String phoneNum, Boolean pushEnabled) {
        if (nickname != null && !nickname.isEmpty()) {
            this.nickname = nickname;
        }

        if (region != null && !region.isEmpty()) {
            this.region = region;
        }

        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            this.profileImageUrl = profileImageUrl;
        }

        if (phoneNum != null && !phoneNum.isEmpty()) {
            this.phoneNum = phoneNum;
        }

        if (pushEnabled != null) {
            this.pushEnabled = pushEnabled;  // pushEnabled는 기본값이 필요 없다면 null 체크를 생략할 수 있음
        }
    }
}
