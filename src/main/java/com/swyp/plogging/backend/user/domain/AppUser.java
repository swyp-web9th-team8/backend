package com.swyp.plogging.backend.user.domain;

import com.swyp.plogging.backend.domain.base.BaseEntity;
import com.swyp.plogging.backend.participation.domain.Participation;
import com.swyp.plogging.backend.post.domain.Post;
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
import lombok.Setter;

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

    @Setter
    @Column
    private String gender; // 성별 필드

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

    public static AppUser newInstance(String email, String nickname, String region, AuthProvider authProvider, String profileImageUrl) {
        AppUser user = new AppUser();
        user.email = email;
        user.nickname = nickname;
        user.region = region;
        user.authProvider = authProvider;
        user.profileImageUrl = profileImageUrl;
        user.registered = false;

        return user;
    }

    public void updateProfileImageUrl(String profileImageUrl) {
        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            this.profileImageUrl = profileImageUrl;
        }
    }

    public void updateNickname(String nickname) {
        if (nickname != null && !nickname.isEmpty()) {
            this.nickname = nickname;
        }
    }

    public void updateRegion(String region) {
        if (region != null && !region.isEmpty()) {
            this.region = region;
        }
    }

    public void updatePhoneNum(String phoneNum) {
        if (phoneNum != null && !phoneNum.isEmpty()) {
            this.phoneNum = phoneNum;
        }
    }

    public void updatePushEnabled(Boolean pushEnabled) {
        if (pushEnabled != null) {
            this.pushEnabled = pushEnabled;
        }
    }

    public int getTotalMeeting() {
        return writtenPosts.size() + participations.size();
    }

    // 사용자 정보 업데이트 메서드 추가
    public void update(String nickname, String region, String profileImageUrl) {
        this.nickname = nickname;
        this.region = region;
        if (profileImageUrl != null) {
            this.profileImageUrl = profileImageUrl;
        }
    }

    // 등록 완료 메서드
    public void completeRegistration() {
        this.registered = true;
    }
}
