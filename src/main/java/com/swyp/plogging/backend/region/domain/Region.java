package com.swyp.plogging.backend.region.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.MultiPolygon;

@Entity
@Table(name = "regions")
@Getter
@Setter
@NoArgsConstructor
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String city; // 시도 (서울특별시)

    @Column(nullable = false)
    private String district; // 시군구 (강남구)

    @Column
    private String neighborhood; // 읍면동 (역삼동)

    @Column
    private String code; // 행정구역 코드

    // 지역단위 검색을 위한 지역 폴리곤
    @Column(columnDefinition = "geometry(MULTIPOLYGON, 4326)")
    private MultiPolygon polygons;

    // 엔티티 생성자
    public Region(String city, String district, String neighborhood, String code) {
        this.city = city;
        this.district = district;
        this.neighborhood = neighborhood;
        this.code = code;
    }

    public boolean hasPolygons(){
        return polygons != null;
    }
}