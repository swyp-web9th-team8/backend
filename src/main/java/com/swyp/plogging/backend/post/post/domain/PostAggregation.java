package com.swyp.plogging.backend.post.post.domain;

import com.swyp.plogging.backend.common.domain.base.BaseTimeEntity;
import com.swyp.plogging.backend.user.user.domain.AppUser;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 유저별 모임 및 참여 집계테이블<br>
 * 매일 자정 갱신, 유저 랭킹과 뱃지에 사용.
 */
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostAggregation extends BaseTimeEntity {
    @Id
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId(value = "id")
    private AppUser user;
    private Long totalPostCount;
    private Long totalParticipationCount;

    public void updateCounts(Long totalPostCount, Long totalParticipationCount){
        this.totalPostCount = totalPostCount;
        this.totalParticipationCount = totalParticipationCount;
    }
}
