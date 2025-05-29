package com.swyp.plogging.backend.user.domain;

import com.swyp.plogging.backend.domain.Region;
import com.swyp.plogging.backend.domain.base.BaseEntity;
import com.swyp.plogging.backend.participation.domain.Participation;
import com.swyp.plogging.backend.post.domain.Post;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

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

    // 이전 호환성을 위해 region 필드 유지
    @Column(nullable = false)
    private String region;

    @Column
    private String phoneNum;

    @Setter
    @Column
    private String gender;

    private boolean pushEnabled;

    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider;

    private boolean registered = false;

    private String profileImageUrl;

    private int score;
    // 정지여부 필드
    private boolean activeUser = true;
    //fcm token
    private String fcmToken;

    @OneToMany(mappedBy = "writer")
    private List<Post> writtenPosts = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Participation> participations = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserBadge> userBadges = new ArrayList<>();

    // UserRegion 관계 추가
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserRegion> userRegions = new ArrayList<>();

    // 기존 메서드 - 문자열 region만 설정
    public static AppUser newInstance(String email, String nickname, String region, AuthProvider authProvider, String profileImageUrl) {
        AppUser user = new AppUser();
        user.email = email;
        user.nickname = nickname;
        user.region = region;  // 호환성을 위해 문자열 region 설정
        user.authProvider = authProvider;
        user.profileImageUrl = profileImageUrl;
        user.registered = false;

        return user;
    }

    // 신규 메서드 - Region 엔티티를 직접 사용
    public static AppUser newInstanceWithRegion(String email, String nickname, Region region, AuthProvider authProvider,
        String profileImageUrl) {
        // 먼저 문자열 region을 포함한 사용자 생성
        AppUser user = newInstance(
            email,
            nickname,
            formatRegionString(region),
            authProvider,
            profileImageUrl
        );

        // UserRegion 엔티티 생성 및 연결
        UserRegion userRegion = new UserRegion(user, region, true);
        user.getUserRegions().add(userRegion);

        return user;
    }

    // Region 엔티티를 문자열로 변환
    private static String formatRegionString(Region region) {
        String regionStr = region.getCity() + " " + region.getDistrict();
        if (region.getNeighborhood() != null && !region.getNeighborhood().isEmpty()) {
            regionStr += " " + region.getNeighborhood();
        }
        return regionStr;
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

    public int getCertificatedMeeting() {
        int certificatedWrittenPostCount = (int) writtenPosts.stream()
            .filter(Post::isCertified)
            .count();
        int certificatedParticipationPostCount = (int) participations.stream()
            .filter(participation -> participation.getPost().isCertified())
            .count();

        return certificatedWrittenPostCount + certificatedParticipationPostCount;
    }

    // 사용자 정보 업데이트 메서드
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

    // 새로운 메서드: 지역 추가
    public void addRegion(Region region, boolean isPrimary) {
        UserRegion userRegion = new UserRegion(this, region, isPrimary);
        userRegions.add(userRegion);

        // 기본 지역인 경우 문자열 region 필드도 업데이트
        if (isPrimary) {
            // 다른 모든 지역을 기본이 아닌 것으로 설정
            userRegions.stream()
                .filter(ur -> ur.getRegion() != region)
                .forEach(UserRegion::unsetPrimary);

            // region 문자열 필드 업데이트 (호환성)
            this.region = formatRegionString(region);
        }
    }

    // 새로운 메서드: 기본 지역 가져오기
    public UserRegion getPrimaryRegion() {
        return userRegions.stream()
            .filter(UserRegion::isPrimary)
            .findFirst()
            .orElse(null);
    }

    // 기존 updateRegion 메서드 오버로드 - Region 객체 사용
    public void updateRegion(Region region) {
        // 기존 기본 지역 찾기
        UserRegion primaryRegion = getPrimaryRegion();

        if (primaryRegion != null) {
            // 기존 기본 지역 제거
            userRegions.remove(primaryRegion);
        }

        // 새 기본 지역 추가
        addRegion(region, true);
    }

    // Region 엔티티와 함께 update 메서드 오버로드
    public void update(String nickname, Region region, String profileImageUrl) {
        this.nickname = nickname;
        this.region = formatRegionString(region);
        if (profileImageUrl != null) {
            this.profileImageUrl = profileImageUrl;
        }

        // 기존 기본 지역을 비활성화
        UserRegion currentPrimary = getPrimaryRegion();
        if (currentPrimary != null) {
            currentPrimary.unsetPrimary();
        }

        // 새 지역 추가
        addRegion(region, true);
    }

    public void inActive() {
        this.activeUser = false;
    }

    public void setFcmToken(String token){
        this.fcmToken = token;
    }
}