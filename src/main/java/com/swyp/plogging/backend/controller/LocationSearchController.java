package com.swyp.plogging.backend.common.controller;

import com.swyp.plogging.backend.common.dto.ApiResponse;
import com.swyp.plogging.backend.common.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/location")
public class LocationSearchController {

    private final LocationService locationService;

    /**
     * 키워드로 장소 검색 (장소명, 상호명 등)
     */
    @GetMapping("/search")
    public ApiResponse<List<Map<String, Object>>> searchPlaces(@RequestParam String keyword) {
        log.info("장소 검색 요청: keyword={}", keyword);
        try {
            List<Map<String, Object>> results = locationService.searchPlacesByKeyword(keyword);
            return ApiResponse.ok(results, "장소 검색 결과입니다.");
        } catch (Exception e) {
            log.error("장소 검색 중 오류 발생", e);
            return ApiResponse.error("장소 검색 중 오류가 발생했습니다.");
        }
    }

    /**
     * 주소로 좌표 검색
     */
    @GetMapping("/geocode")
    public ApiResponse<List<Map<String, Object>>> geocodeAddress(@RequestParam String address) {
        log.info("주소 검색 요청: address={}", address);
        try {
            List<Map<String, Object>> results = locationService.searchCoordinatesByAddress(address);
            return ApiResponse.ok(results, "주소 검색 결과입니다.");
        } catch (Exception e) {
            log.error("주소 검색 중 오류 발생", e);
            return ApiResponse.error("주소 검색 중 오류가 발생했습니다.");
        }
    }

    /**
     * 좌표로 주소 검색 (역지오코딩)
     */
    @GetMapping("/reverse-geocode")
    public ApiResponse<Map<String, Object>> reverseGeocode(
            @RequestParam Double longitude,
            @RequestParam Double latitude) {
        log.info("역지오코딩 요청: longitude={}, latitude={}", longitude, latitude);
        try {
            Map<String, Object> result = locationService.reverseGeocode(longitude, latitude);
            return ApiResponse.ok(result, "좌표의 주소 정보입니다.");
        } catch (Exception e) {
            log.error("역지오코딩 중 오류 발생", e);
            return ApiResponse.error("역지오코딩 중 오류가 발생했습니다.");
        }
    }
}