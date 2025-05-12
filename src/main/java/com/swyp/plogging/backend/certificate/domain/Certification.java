package com.swyp.plogging.backend.certificate.domain;

import com.swyp.plogging.backend.common.exception.PostNotFoundException;
import com.swyp.plogging.backend.domain.base.BaseTimeEntity;
import com.swyp.plogging.backend.post.domain.Post;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Certification extends BaseTimeEntity {

    @Id
    @GeneratedValue
    private Long id;

    @ElementCollection
    @CollectionTable(name = "certification_image_urls", joinColumns = @JoinColumn(name = "certification_id"))
    @Column(nullable = false)
    private List<String> imageUrls;

    @OneToOne
    @JoinColumn(name = "post_id")
    private Post post;

    private boolean certificated;

    public static Certification newInstance(Post post){
        Certification instance = new Certification();
        instance.post = post;
        instance.imageUrls = new ArrayList<>();
        instance.certificated = false;
        return instance;
    }

    public void addImageUrl(String imageUrl) {
        imageUrls.add(imageUrl);
    }

    public void certificate() {
        if(post.isCompleted()){
            certificated = true;
        }else{
            throw new PostNotFoundException("완료되지 않은 모임입니다.");
        }
    }
}
