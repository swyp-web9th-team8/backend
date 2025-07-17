package com.swyp.plogging.backend.post.post.domain;

import com.swyp.plogging.backend.common.domain.base.BaseTimeEntity;
import com.swyp.plogging.backend.user.user.domain.AppUser;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostAggregation extends BaseTimeEntity {
    @Id
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    private AppUser user;
    private Long totalPostCount;
    private Long totalParticipationCount;

    public void updateCounts(Long totalPostCount, Long totalParticipationCount){
        this.totalPostCount = totalPostCount;
        this.totalParticipationCount = totalParticipationCount;
    }
}
