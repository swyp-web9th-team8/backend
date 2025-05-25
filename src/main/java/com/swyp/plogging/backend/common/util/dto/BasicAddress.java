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
        String siName = si;
        if(siName == null){
            siName = "";
        }
        String districtName = district;
        if(districtName == null){
            districtName = "";
        }
        String neighborhoodName = neighborhood;
        if(neighborhoodName == null){
            neighborhoodName = "";
        }
        String gilName = gil;
        if(gilName == null){
            gilName = "";
        }
        Integer gilNumName = gilNum;
        if(gilNumName == null){
            gilNumName = 0;
        }

        if (!isRoadAddress()) {
            return String.format("%s %s %s", siName, districtName, neighborhoodName).strip().trim();
        }
        return String.format("%s %s %s %d", siName, districtName, gilName, gilNumName).strip().trim();
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
