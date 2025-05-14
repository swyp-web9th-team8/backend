package com.swyp.plogging.backend.user.domain;

import com.swyp.plogging.backend.domain.Region;
import com.swyp.plogging.backend.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_regions")
@Getter
@NoArgsConstructor
public class UserRegion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    @Column(name = "is_primary")
    private boolean isPrimary = false;

    // 생성자
    public UserRegion(AppUser user, Region region, boolean isPrimary) {
        this.user = user;
        this.region = region;
        this.isPrimary = isPrimary;
    }

    // 편의 메서드
    public void setAsPrimary() {
        this.isPrimary = true;
    }

    public void unsetPrimary() {
        this.isPrimary = false;
    }
}