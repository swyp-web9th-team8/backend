package com.swyp.plogging.backend.common.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.WKTReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${naver.map.client-id:}")
    private String mapClientId;

    @Value("${naver.map.client-secret:}")
    private String mapClientSecret;

    @Value("${naver.search.client-id:}")
    private String searchClientId;

    @Value("${naver.search.client-secret:}")
    private String searchClientSecret;

    private HttpHeaders mapHeaders;

    @PostConstruct
    private void init() {
        // 네이버 클라우드 플랫폼 Maps API용 헤더
        mapHeaders = new HttpHeaders();
        mapHeaders.set("x-ncp-apigw-api-key-id", mapClientId);
        mapHeaders.set("x-ncp-apigw-api-key", mapClientSecret);

        log.info("네이버 지도 API 헤더 설정 완료: Client ID={}, Secret={}",
                (mapClientId != null && mapClientId.length() > 4) ? mapClientId.substring(0, 4) + "..." : mapClientId,
                (mapClientSecret != null && mapClientSecret.length() > 4) ? mapClientSecret.substring(0, 4) + "..." : mapClientSecret);
    }

    /**
     * 키워드로 장소 검색 (네이버 검색 API의 지역 검색 사용)
     */
    public List<Map<String, Object>> searchPlacesByKeyword(String keyword) {
        try {
            // 네이버 검색 API 사용 (지역 검색)
            String url = UriComponentsBuilder.fromHttpUrl("https://openapi.naver.com/v1/search/local.json")
                    .queryParam("query", keyword)
                    .queryParam("display", 10)
                    .build()
                    .toUriString();

            log.info("장소 검색 요청 URL: {}", url);

            // 네이버 검색 API용 헤더 설정
            HttpHeaders searchHeaders = new HttpHeaders();
            searchHeaders.set("X-Naver-Client-Id", searchClientId);
            searchHeaders.set("X-Naver-Client-Secret", searchClientSecret);

            HttpEntity<String> entity = new HttpEntity<>(searchHeaders);
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class);

            log.info("장소 검색 응답 상태: {}", response.getStatusCode());

            Map<String, Object> result = objectMapper.readValue(response.getBody(), Map.class);
            List<Map<String, Object>> items = (List<Map<String, Object>>) result.get("items");

            if (items == null) {
                return new ArrayList<>();
            }

            // 응답 형식 변환 및 정리 (네이버 검색 API 응답 형식에 맞춤)
            List<Map<String, Object>> formattedPlaces = new ArrayList<>();
            for (Map<String, Object> item : items) {
                Map<String, Object> formattedPlace = new HashMap<>();
                formattedPlace.put("id", item.get("title").toString().replaceAll("<[^>]*>", ""));  // HTML 태그 제거
                formattedPlace.put("name", item.get("title").toString().replaceAll("<[^>]*>", "")); // HTML 태그 제거
                formattedPlace.put("address", item.get("address"));
                formattedPlace.put("roadAddress", item.get("roadAddress"));

                // 좌표 정보가 있는 경우 (mapx, mapy)
                if (item.containsKey("mapx") && item.containsKey("mapy")) {
                    try {
                        // 네이버 검색 API는 카텍 좌표계를 사용하므로 변환이 필요할 수 있음
                        // 여기서는 간단히 처리
                        double longitude = Double.parseDouble(item.get("mapx").toString());
                        double latitude = Double.parseDouble(item.get("mapy").toString());
                        formattedPlace.put("longitude", longitude);
                        formattedPlace.put("latitude", latitude);
                    } catch (Exception e) {
                        log.error("좌표 변환 중 오류 발생", e);
                    }
                }

                formattedPlaces.add(formattedPlace);
            }

            return formattedPlaces;
        } catch (Exception e) {
            log.error("장소 검색 중 오류 발생", e);
            return new ArrayList<>();
        }
    }

    /**
     * 주소로 좌표 검색 (네이버 지도 주소 -> 좌표 변환 API)
     */
    public List<Map<String, Object>> searchCoordinatesByAddress(String address) {
        try {
            // 공식 문서에 맞게 URL 수정
            String url = UriComponentsBuilder.fromHttpUrl("https://maps.apigw.ntruss.com/map-geocode/v2/geocode")
                    .queryParam("query", address)
                    .build()
                    .toUriString();

            log.info("주소 검색 요청 URL: {}", url);

            HttpEntity<String> entity = new HttpEntity<>(mapHeaders);
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class);

            log.info("주소 검색 응답 상태: {}", response.getStatusCode());

            Map<String, Object> result = objectMapper.readValue(response.getBody(), Map.class);
            List<Map<String, Object>> addresses = (List<Map<String, Object>>) result.get("addresses");

            if (addresses == null) {
                return new ArrayList<>();
            }

            // 응답 형식 변환 및 정리
            List<Map<String, Object>> formattedAddresses = new ArrayList<>();
            for (Map<String, Object> addr : addresses) {
                Map<String, Object> formattedAddr = new HashMap<>();
                formattedAddr.put("roadAddress", addr.get("roadAddress"));
                formattedAddr.put("jibunAddress", addr.get("jibunAddress"));
                formattedAddr.put("latitude", Double.parseDouble((String) addr.get("y")));
                formattedAddr.put("longitude", Double.parseDouble((String) addr.get("x")));
                formattedAddresses.add(formattedAddr);
            }

            return formattedAddresses;
        } catch (Exception e) {
            log.error("주소 검색 중 오류 발생", e);
            return new ArrayList<>();
        }
    }

    /**
     * 좌표로 주소 검색 (역지오코딩)
     */
    public Map<String, Object> reverseGeocode(Double longitude, Double latitude) {
        try {
            String coords = longitude + "," + latitude;
            // 공식 문서에 맞게 URL 수정
            String url = UriComponentsBuilder.fromHttpUrl("https://maps.apigw.ntruss.com/map-reversegeocode/v2/gc")
                    .queryParam("coords", coords)
                    .queryParam("output", "json")
                    .build()
                    .toUriString();

            log.info("역지오코딩 요청 URL: {}", url);

            HttpEntity<String> entity = new HttpEntity<>(mapHeaders);
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class);

            log.info("역지오코딩 응답 상태: {}", response.getStatusCode());

            Map<String, Object> result = objectMapper.readValue(response.getBody(), Map.class);
            List<Map<String, Object>> results = (List<Map<String, Object>>) result.get("results");

            Map<String, Object> addressInfo = new HashMap<>();

            if (results != null && !results.isEmpty()) {
                Map<String, Object> firstResult = results.get(0);

                // 지번 주소 정보
                Map<String, Object> region = (Map<String, Object>) firstResult.get("region");
                if (region != null) {
                    StringBuilder fullAddress = new StringBuilder();

                    // 시도
                    Map<String, Object> area1 = (Map<String, Object>) region.get("area1");
                    if (area1 != null) {
                        String city = (String) area1.get("name");
                        addressInfo.put("city", city);
                        fullAddress.append(city).append(" ");
                    }

                    // 구군
                    Map<String, Object> area2 = (Map<String, Object>) region.get("area2");
                    if (area2 != null) {
                        String district = (String) area2.get("name");
                        addressInfo.put("district", district);
                        fullAddress.append(district).append(" ");
                    }

                    // 동읍면
                    Map<String, Object> area3 = (Map<String, Object>) region.get("area3");
                    if (area3 != null) {
                        String neighborhood = (String) area3.get("name");
                        addressInfo.put("neighborhood", neighborhood);
                        fullAddress.append(neighborhood).append(" ");
                    }

                    // 번지
                    Map<String, Object> land = (Map<String, Object>) firstResult.get("land");
                    if (land != null) {
                        String number1 = (String) land.get("number1");
                        String number2 = (String) land.get("number2");

                        if (number1 != null && !number1.isEmpty()) {
                            fullAddress.append(number1);
                            if (number2 != null && !number2.isEmpty()) {
                                fullAddress.append("-").append(number2);
                            }
                        }
                    }

                    addressInfo.put("fullAddress", fullAddress.toString());
                }

                // 도로명 주소
                if (firstResult.get("land") != null) {
                    Map<String, Object> land = (Map<String, Object>) firstResult.get("land");
                    String addition0 = (String) land.get("addition0");
                    if (addition0 != null && !addition0.isEmpty()) {
                        addressInfo.put("roadAddress", addition0);
                    }
                }
            }

            // 좌표 추가
            addressInfo.put("latitude", latitude);
            addressInfo.put("longitude", longitude);

            return addressInfo;
        } catch (Exception e) {
            log.error("역지오코딩 중 오류 발생", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", e.getMessage());
            return errorResult;
        }
    }

    /**
     * 좌표를 PostGIS Point 객체로 변환
     */
    public Point createPoint(Double longitude, Double latitude) {
        if (longitude == null || latitude == null) {
            return null;
        }

        try {
            // WKT(Well-Known Text) 형식으로 Point 생성
            String pointWKT = String.format("POINT(%f %f)", longitude, latitude);
            WKTReader wktReader = new WKTReader();
            Point point = (Point) wktReader.read(pointWKT);
            point.setSRID(4326); // WGS84 좌표계
            return point;
        } catch (Exception e) {
            log.error("Point 객체 생성 중 오류 발생", e);
            return null;
        }
    }
}