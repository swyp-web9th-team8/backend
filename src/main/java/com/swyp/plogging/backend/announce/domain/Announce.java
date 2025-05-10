package com.swyp.plogging.backend.announce.domain;

import com.swyp.plogging.backend.domain.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Announce extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column
    private boolean active;

    public static Announce newInstance(String title, String content, boolean active) {
        Announce announce = new Announce();
        announce.title = title;
        announce.content = content;
        announce.active = active;
        return announce;
    }

    public void inactivation() {
        active = false;
    }
}
