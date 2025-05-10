package com.swyp.plogging.backend.announce.controller.dto;

import com.swyp.plogging.backend.announce.domain.Announce;
import lombok.Data;

@Data
public class AnnounceResponse {

    private Long id;
    private String title;
    private String content;
    private boolean active;

    public AnnounceResponse(Long id, String title, String content, boolean active) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.active = active;
    }

    public static AnnounceResponse from(Announce announce) {
        return new AnnounceResponse(announce.getId(), announce.getTitle(), announce.getContent(), announce.isActive());
    }
}
