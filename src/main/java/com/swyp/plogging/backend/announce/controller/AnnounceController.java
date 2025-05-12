package com.swyp.plogging.backend.announce.controller;

import com.swyp.plogging.backend.announce.controller.dto.AnnounceRequest;
import com.swyp.plogging.backend.announce.controller.dto.AnnounceResponse;
import com.swyp.plogging.backend.announce.service.AnnounceService;
import com.swyp.plogging.backend.common.dto.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/announces")
public class AnnounceController {

    private final AnnounceService announceService;

    @GetMapping
    public ApiResponse<List<AnnounceResponse>> getAll() {
        List<AnnounceResponse> announces = announceService.getAll();
        return ApiResponse.ok(announces, "Announces fetched successfully!");
    }

    @GetMapping("/{id}")
    public ApiResponse<AnnounceResponse> getAnnounce(@PathVariable Long id) {
        AnnounceResponse announce = announceService.getAnnounce(id);
        return ApiResponse.ok(announce, "Announce fetched successfully!");
    }

    @PostMapping
    public ApiResponse<AnnounceResponse> createAnnounce(@RequestBody AnnounceRequest request) {
        AnnounceResponse announce = announceService.createAnnounce(request.getTitle(), request.getContent());
        return ApiResponse.ok(announce, "Announce created successfully!");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Long> deleteAnnounce(@PathVariable Long id) {
        announceService.deleteAnnounce(id);
        return ApiResponse.ok(id, "Announce deleted successfully!");
    }
}
