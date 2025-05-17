package com.swyp.plogging.backend.announce.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.swyp.plogging.backend.announce.domain.Announce;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AnnounceResponse {

    private Long id;
    private String title;
    private String content;
    private boolean active;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastModifiedDt;

    public AnnounceResponse(Long id, String title, String content, boolean active, LocalDateTime lastModifiedDt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.active = active;
        this.lastModifiedDt = lastModifiedDt;
    }

    public static AnnounceResponse from(Announce announce) {
        return new AnnounceResponse(announce.getId(), announce.getTitle(), announce.getContent(), announce.isActive(),
            announce.getLastModifiedDt());
    }
}
