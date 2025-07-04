package com.swyp.plogging.backend.post.participation.domain;

import com.swyp.plogging.backend.post.post.domain.Post;
import com.swyp.plogging.backend.common.domain.base.BaseTimeEntity;
import com.swyp.plogging.backend.user.user.domain.AppUser;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Participation extends BaseTimeEntity {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    private boolean joined;

    public static Participation newInstance(Post target, AppUser user){
        Participation participation = new Participation();
        participation.post = target;
        participation.user = user;
        participation.joined = false;
        return participation;
    }

    public void joined() {
        joined = true;
    }
}
