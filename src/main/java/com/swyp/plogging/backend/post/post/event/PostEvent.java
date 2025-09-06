package com.swyp.plogging.backend.post.post.event;

import com.swyp.plogging.backend.post.post.domain.Post;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PostEvent extends ApplicationEvent {

    private PostEventType eventType;

    public PostEvent(Post source) {
        super(source);
    }
    public PostEvent(Post source, PostEventType eventType) {
        super(source);
        this.eventType = eventType;
    }
}
