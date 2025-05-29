package com.swyp.plogging.backend.user.service;

import com.swyp.plogging.backend.common.exception.UnsupportedUpdateRequestException;
import com.swyp.plogging.backend.common.exception.UserNotFoundException;
import com.swyp.plogging.backend.common.service.FileService;
import com.swyp.plogging.backend.common.util.DateUtils;
import com.swyp.plogging.backend.domain.Region;
import com.swyp.plogging.backend.post.repository.RegionRepository;
import com.swyp.plogging.backend.rank.controller.dto.RankingResponse;
import com.swyp.plogging.backend.user.controller.dto.*;
import com.swyp.plogging.backend.user.domain.AppUser;
import com.swyp.plogging.backend.user.domain.UserBadge;
import com.swyp.plogging.backend.user.domain.UserRegion;
import com.swyp.plogging.backend.user.repository.UserBadgeRepository;
import com.swyp.plogging.backend.user.repository.UserRegionRepository;
import com.swyp.plogging.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RegionRepository regionRepository;
    private final UserRegionRepository userRegionRepository;
    private final FileService fileService;
    private final UserBadgeRepository userBadgeRepository;

    public ProfileResponse getProfile(Long userId) {
        return userRepository.findProfileByUserId(userId);
    }

    public EditableProfileResponse getEditableProfile(Long userId) {
        AppUser appUser = getUser(userId);
        return EditableProfileResponse.from(appUser);
    }

    private AppUser getUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(String.format("Could not find user with ID: %d", userId)));
    }

    @Transactional
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        AppUser user = getUser(userId);

        if (request instanceof UpdateNicknameRequest nicknameRequest) {
            user.updateNickname(nicknameRequest.getNickname());
        } else if (request instanceof UpdateRegionRequest regionRequest) {
            // Region ID가 제공된 경우
            if (regionRequest.getRegionId() != null) {
                regionRepository.findById(regionRequest.getRegionId())
                    .ifPresent(region -> {
                        updateUserRegion(user, region);
                    });
            }
            // 문자열 지역이 제공된 경우
            else if (regionRequest.getRegion() != null) {
                // 기존 방식 - 문자열로 저장
                user.updateRegion(regionRequest.getRegion());

                // 새 방식 - Region 객체로 변환하여 저장 (가능한 경우)
                findRegionFromString(regionRequest.getRegion())
                    .ifPresent(region -> {
                        updateUserRegion(user, region);
                    });
            }
        } else if (request instanceof UpdatePhoneNumRequest phoneRequest) {
            user.updatePhoneNum(phoneRequest.getPhoneNum());
        } else if (request instanceof UpdatePushEnabledRequest pushRequest) {
            user.updatePushEnabled(pushRequest.getPushEnabled());
        } else {
            throw new UnsupportedUpdateRequestException();
        }
    }

    private void updateUserRegion(AppUser user, Region region) {
        // 기존 기본 지역 확인 및 해제
        userRegionRepository.findPrimaryRegionByUserId(user.getId())
            .ifPresent(existingPrimary -> {
                existingPrimary.unsetPrimary();
                userRegionRepository.save(existingPrimary);
            });

        // 새 UserRegion 생성 및 저장
        UserRegion userRegion = new UserRegion(user, region, true);
        userRegionRepository.save(userRegion);

        // user.userRegions에 추가
        user.getUserRegions().add(userRegion);

        // 문자열 region 필드도 업데이트 (호환성)
        String regionStr = region.getCity() + " " + region.getDistrict();
        if (region.getNeighborhood() != null && !region.getNeighborhood().isEmpty()) {
            regionStr += " " + region.getNeighborhood();
        }
        user.updateRegion(regionStr);
    }

    /**
     * 문자열 형태의 지역 정보를 기반으로 Region 엔티티를 찾습니다. 형식: "서울특별시 강남구" 또는 "서울특별시 강남구 역삼동"
     *
     * @param regionString 지역 문자열
     * @return 찾은 Region 객체 (없으면 빈 Optional)
     */
    public Optional<Region> findRegionFromString(String regionString) {
        if (regionString == null || regionString.isEmpty()) {
            return Optional.empty();
        }

        String[] parts = regionString.split(" ");
        if (parts.length < 2) {
            return Optional.empty();
        }

        String city = parts[0];
        String district = parts[1];
        String neighborhood = parts.length > 2 ? parts[2] : "";

        if (neighborhood.isEmpty()) {
            List<Region> regions = regionRepository.findByCityAndDistrict(city, district);
            return regions.isEmpty() ? Optional.empty() : Optional.of(regions.get(0));
        } else {
            return regionRepository.findByCityAndDistrictAndNeighborhood(city, district, neighborhood);
        }
    }

    @Transactional
    public String uploadProfileImage(Long userId, MultipartFile file) {
        String filename = fileService.uploadImageAndGetFileName(file);

        AppUser user = getUser(userId);
        String publicPath = "/images/" + filename;
        user.updateProfileImageUrl(publicPath);

        return publicPath;
    }

    public UserBadgesResponse getUserBadges(Long userId) {
        AppUser user = getUser(userId);
        List<UserBadge> userBadges = userBadgeRepository.findByUser(user);
        List<UserBadgeResponse> userBadgeResponses = userBadges.stream()
            .map(UserBadgeResponse::from)
            .collect(Collectors.toList());

        UserBadge latestUserBadge = userBadgeRepository.findTopByUserIdOrderByCreatedDtDesc(userId)
            .orElse(null);
        int remainingActionsForNextBadge = calculateRemainingActionsForNextBadge(user, latestUserBadge);

        return new UserBadgesResponse(remainingActionsForNextBadge, userBadgeResponses);
    }

    // 다음 뱃지까지 남은 수 (게이지 바 빈 곳 수) 계산
    private int calculateRemainingActionsForNextBadge(AppUser user, UserBadge latestUserBadge) {
        int highestBadgeRequiredActivities = 40;
        int necessaryActivitiesForNextBadge = 10;
        int totalMeeting = user.getCertificatedMeeting();

        if (latestUserBadge == null) {
            return totalMeeting - necessaryActivitiesForNextBadge;
        }

        int latestBadgeRequiredActivities = latestUserBadge.getBadge().getRequiredActivitiesForBadge();
        int nextBadgeNum = latestBadgeRequiredActivities + necessaryActivitiesForNextBadge;

        if (totalMeeting >= highestBadgeRequiredActivities) {
            return 0;
        }

        return totalMeeting - nextBadgeNum;
    }

    public List<RankingResponse> getWeeklyRankings() {
        LocalDateTime startOfCurrentWeek = DateUtils.getStartOfCurrentWeek();
        List<RankingResponse> rankingResponses = userRepository.findWeeklyRanking(startOfCurrentWeek);
        assignRanks(rankingResponses);

        return rankingResponses;
    }

    private static void assignRanks(List<RankingResponse> rankingResponses) {
        rankingResponses.sort(Comparator.comparingInt(RankingResponse::getTotalMeet).reversed());

        for (int i = 0; i < rankingResponses.size(); i++) {
            rankingResponses.get(i).setRank(i + 1);
        }
    }

    public List<RankingResponse> getAllTimeRankings() {
        List<RankingResponse> rankingResponses = userRepository.findAllTimeRankings();
        assignRanks(rankingResponses);

        return rankingResponses;
    }

    // 사용자의 모든 지역 가져오기
    public List<UserRegion> getUserRegions(Long userId) {
        return userRegionRepository.findByUserId(userId);
    }

    // 사용자의 기본 지역 가져오기
    public Optional<UserRegion> getPrimaryUserRegion(Long userId) {
        return userRegionRepository.findPrimaryRegionByUserId(userId);
    }

    // 지역 추가하기
    @Transactional
    public void addUserRegion(Long userId, Long regionId, boolean isPrimary) {
        AppUser user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        Region region = regionRepository.findById(regionId)
            .orElseThrow(() -> new IllegalArgumentException("지역을 찾을 수 없습니다."));

        // 기본 지역인 경우 기존 기본 지역 해제
        if (isPrimary) {
            userRegionRepository.findPrimaryRegionByUserId(userId)
                .ifPresent(primaryRegion -> {
                    primaryRegion.unsetPrimary();
                    userRegionRepository.save(primaryRegion);
                });
        }

        // 새 지역 추가
        UserRegion userRegion = new UserRegion(user, region, isPrimary);
        userRegionRepository.save(userRegion);

        // 기본 지역인 경우 사용자의 region 필드 업데이트 (호환성)
        if (isPrimary) {
            String regionStr = region.getCity() + " " + region.getDistrict();
            if (region.getNeighborhood() != null && !region.getNeighborhood().isEmpty()) {
                regionStr += " " + region.getNeighborhood();
            }
            user.updateRegion(regionStr);
        }
    }

    // 지역 삭제하기
    @Transactional
    public void removeUserRegion(Long userId, Long regionId) {
        userRegionRepository.deleteByUserIdAndRegionId(userId, regionId);
    }

    // 기본 지역 변경하기
    @Transactional
    public void updatePrimaryRegion(Long userId, Long regionId) {
        AppUser user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        // 기존 기본 지역 해제
        userRegionRepository.findPrimaryRegionByUserId(userId)
            .ifPresent(primaryRegion -> {
                primaryRegion.unsetPrimary();
                userRegionRepository.save(primaryRegion);
            });

        // 새 기본 지역 설정
        UserRegion newPrimaryRegion = userRegionRepository.findById(regionId)
            .orElseThrow(() -> new IllegalArgumentException("지역을 찾을 수 없습니다."));

        newPrimaryRegion.setAsPrimary();
        userRegionRepository.save(newPrimaryRegion);

        // 사용자의 region 필드 업데이트 (호환성)
        Region region = newPrimaryRegion.getRegion();
        String regionStr = region.getCity() + " " + region.getDistrict();
        if (region.getNeighborhood() != null && !region.getNeighborhood().isEmpty()) {
            regionStr += " " + region.getNeighborhood();
        }
        user.updateRegion(regionStr);
    }

    @Transactional
    public void setFCMToken(Long userId, String token) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
        user.setFcmToken(token);
    }
}