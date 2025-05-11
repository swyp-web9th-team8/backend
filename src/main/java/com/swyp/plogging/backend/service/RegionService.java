package com.swyp.plogging.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp.plogging.backend.domain.Region;
import com.swyp.plogging.backend.repository.RegionRepository;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegionService {

    private final RegionRepository regionRepository;
    private final RestTemplate restTemplate;

    // 기존에 JsonConfiguration에서 objectMapper를 주입받아 사용
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${vworld.api.key}")
    private String apiKey;

    // 애플리케이션 시작 시 데이터 초기화
    @PostConstruct
    @Transactional
    public void initRegionData() {
        long count = regionRepository.count();
        log.info("지역 데이터 초기화 시작. 현재 데이터 수: {}", count);

        if (count == 0) {
            log.info("지역 데이터가 없어 초기화를 시작합니다...");
            try {
                // 서울시 시군구(구) 데이터 가져오기
                List<Region> districts = fetchSeoulDistricts();
                regionRepository.saveAll(districts);
                log.info("서울시 구 데이터 로드 완료: {} 개", districts.size());

                // 각 구별 읍면동(동) 데이터 가져오기
                List<Region> allNeighborhoods = new ArrayList<>();

                for (Region district : districts) {
                    String districtName = district.getDistrict();
                    List<Region> neighborhoods = fetchNeighborhoods(districtName);
                    allNeighborhoods.addAll(neighborhoods);
                    log.info("{} 동 데이터 로드 완료: {} 개", districtName, neighborhoods.size());

                    // API 호출 간격 조절 (너무 빠른 요청은 차단될 수 있음)
                    Thread.sleep(100);
                }

                regionRepository.saveAll(allNeighborhoods);
                log.info("서울시 지역 데이터 로드 완료: 총 {} 개 지역이 저장되었습니다.", regionRepository.count());
            } catch (Exception e) {
                log.error("지역 데이터 로드 중 오류 발생: {}", e.getMessage(), e);
                throw new RuntimeException("지역 데이터를 로드할 수 없습니다.", e);
            }
        } else {
            log.info("지역 데이터가 이미 존재합니다. 총 {} 개 지역이 저장되어 있습니다.", count);
        }
    }

    // V-World API에서 서울시 구 데이터 가져오기 - 수정됨
    private List<Region> fetchSeoulDistricts() throws Exception {
        String url = UriComponentsBuilder.fromHttpUrl("https://api.vworld.kr/req/data")
            .queryParam("service", "data")
            .queryParam("request", "GetFeature")
            .queryParam("data", "LT_C_ADSIGG_INFO")
            .queryParam("key", apiKey)
            .queryParam("domain", "localhost") // 포트 제거
            .queryParam("attrFilter", "full_nm:like:서울특별시") // 속성명 변경
            .queryParam("geometry", "false")
            .queryParam("size", "50")
            .queryParam("geomFilter", "")
            .queryParam("format", "json")
            .build()
            .toUriString();

        log.info("구 데이터 요청 URL: {}", url);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        log.info("구 데이터 응답 상태: {}", response.getStatusCode());

        JsonNode root = objectMapper.readTree(response.getBody());
        JsonNode response_node = root.get("response");

        if (response_node == null || !response_node.get("status").asText().equals("OK")) {
            log.error("API 응답 오류: {}", response.getBody());
            throw new RuntimeException("V-World API 응답 오류");
        }

        JsonNode result = response_node.get("result");
        JsonNode featureCollection = result.get("featureCollection");
        JsonNode features = featureCollection.get("features");

        List<Region> districts = new ArrayList<>();
        Set<String> addedDistricts = new HashSet<>();

        for (JsonNode feature : features) {
            JsonNode properties = feature.get("properties");

            String code = properties.get("sig_cd").asText();
            String fullName = properties.get("full_nm").asText(); // 예: "서울특별시 강남구"
            String[] parts = fullName.split(" ");

            if (parts.length >= 2) {
                String city = parts[0]; // 서울특별시
                String district = parts[1]; // 강남구

                // 중복 방지
                if (!addedDistricts.contains(district)) {
                    Region region = new Region();
                    region.setCity(city);
                    region.setDistrict(district);
                    region.setNeighborhood(""); // 구 레벨에서는 동 정보 없음
                    region.setCode(code);

                    districts.add(region);
                    addedDistricts.add(district);

                    log.info("구 추가: {} (코드: {})", district, code);
                }
            }
        }

        return districts;
    }

    // V-World API에서 특정 구의 동 데이터 가져오기 - 수정됨
    private List<Region> fetchNeighborhoods(String district) throws Exception {
        String url = UriComponentsBuilder.fromHttpUrl("https://api.vworld.kr/req/data")
            .queryParam("service", "data")
            .queryParam("request", "GetFeature")
            .queryParam("data", "LT_C_ADEMD_INFO")
            .queryParam("key", apiKey)
            .queryParam("domain", "localhost") // 포트 제거
            .queryParam("attrFilter", "full_nm:like:서울특별시 " + district) // 이 부분은 기존과 동일
            .queryParam("geometry", "false")
            .queryParam("size", "1000")
            .queryParam("geomFilter", "")
            .queryParam("format", "json")
            .build()
            .toUriString();

        log.info("동 데이터 요청 URL: {}", url);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        log.info("동 데이터 응답 상태: {}", response.getStatusCode());

        JsonNode root = objectMapper.readTree(response.getBody());
        JsonNode response_node = root.get("response");

        if (response_node == null || !response_node.get("status").asText().equals("OK")) {
            log.error("API 응답 오류: {}", response.getBody());
            throw new RuntimeException("V-World API 응답 오류");
        }

        JsonNode result = response_node.get("result");
        JsonNode featureCollection = result.get("featureCollection");
        JsonNode features = featureCollection.get("features");

        List<Region> neighborhoods = new ArrayList<>();
        Set<String> addedNeighborhoods = new HashSet<>();

        for (JsonNode feature : features) {
            JsonNode properties = feature.get("properties");

            String code = properties.get("emd_cd").asText();
            String fullName = properties.get("full_nm").asText(); // 예: "서울특별시 강남구 역삼동"
            String[] parts = fullName.split(" ");

            if (parts.length >= 3) {
                String city = parts[0]; // 서울특별시
                String districtName = parts[1]; // 강남구
                String neighborhood = parts[2]; // 역삼동

                // 중복 방지 및 동일한 구 확인
                if (districtName.equals(district) && !addedNeighborhoods.contains(neighborhood)) {
                    Region region = new Region();
                    region.setCity(city);
                    region.setDistrict(districtName);
                    region.setNeighborhood(neighborhood);
                    region.setCode(code);

                    neighborhoods.add(region);
                    addedNeighborhoods.add(neighborhood);

                    log.debug("동 추가: {} (코드: {})", neighborhood, code);
                }
            }
        }

        return neighborhoods;
    }

    // 구 목록 가져오기
    public List<String> getDistricts() {
        log.info("구 목록 조회");
        return regionRepository.findDistinctDistrictsByCity("서울특별시");
    }

    // 특정 구의 동 목록 가져오기
    public List<String> getNeighborhoods(String district) {
        log.info("동 목록 조회 - 구: {}", district);
        return regionRepository.findNeighborhoodsByCityAndDistrict("서울특별시", district);
    }
}