package com.swyp.plogging.backend.controller;

import com.swyp.plogging.backend.service.RegionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/regions")
@RequiredArgsConstructor
public class RegionController {

    private final RegionService regionService;

    @GetMapping("/districts")
    public ResponseEntity<?> getDistricts() {
        List<String> districts = regionService.getDistricts();
        return ResponseEntity.ok(Map.of("success", true, "districts", districts));
    }

    @GetMapping("/neighborhoods")
    public ResponseEntity<?> getNeighborhoods(@RequestParam String district) {
        List<String> neighborhoods = regionService.getNeighborhoods(district);
        return ResponseEntity.ok(Map.of("success", true, "neighborhoods", neighborhoods));
    }
}