package com.swyp.plogging.backend.common.util.dto;

public interface Address {
    String getFullName();
    boolean isRoadAddress();
    String getSi();
    String getDistrict();
    String getNeighborhood();
    String getGil();
    Integer getGilNum();
}
