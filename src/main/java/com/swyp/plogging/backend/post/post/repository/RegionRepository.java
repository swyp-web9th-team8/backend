package com.swyp.plogging.backend.post.post.repository;

import com.swyp.plogging.backend.region.domain.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {

    List<Region> findByCity(String city);

    List<Region> findByCityAndDistrict(String city, String district);

    // 특정 도시의 모든 구 목록 가져오기 (중복 제거)
    @Query("SELECT DISTINCT r.district FROM Region r WHERE r.city = ?1 ORDER BY r.district")
    List<String> findDistinctDistrictsByCity(String city);

    // 특정 구의 동 목록 가져오기 (동 이름이 비어있지 않은 경우만)
    @Query("SELECT r.neighborhood FROM Region r WHERE r.city = ?1 AND r.district = ?2 AND r.neighborhood <> '' ORDER BY r.neighborhood")
    List<String> findNeighborhoodsByCityAndDistrict(String city, String district);

    // 도시, 구, 동으로 지역 찾기
    Optional<Region> findByCityAndDistrictAndNeighborhood(String city, String district, String neighborhood);
}