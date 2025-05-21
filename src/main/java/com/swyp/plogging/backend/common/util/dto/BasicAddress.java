package com.swyp.plogging.backend.common.util.dto;

public class BasicAddress implements Address {
    private String si;
    private String district;
    private String neighborhood;
    private String gil;
    private Integer gilNum;

    BasicAddress(String si, String district, String neighborhood, String gil, Integer gilNum) {
        this.si = si;
        this.district = district;
        this.neighborhood = neighborhood;
        this.gil = gil;
        this.gilNum = gilNum;
    }

    @Override
    public String getFullName() {
        if (!isRoadAddress()) {
            return String.format("%s %s %s", si, district, neighborhood);
        }
        return String.format("%s %s %s %d", si, district, gil, gilNum);
    }

    @Override
    public boolean isRoadAddress() {
        return gil != null && gilNum != null;
    }

    @Override
    public String getSi() {
        return si;
    }

    @Override
    public String getDistrict() {
        return district;
    }

    @Override
    public String getNeighborhood() {
        return neighborhood;
    }

    @Override
    public String getGil(){
        return gil;
    }

    @Override
    public Integer getGilNum() {
        return gilNum;
    }
}
