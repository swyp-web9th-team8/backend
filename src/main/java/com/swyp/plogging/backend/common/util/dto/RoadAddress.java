package com.swyp.plogging.backend.common.util.dto;

public class RoadAddress extends BasicAddress {
    boolean underground;

    public RoadAddress(String si, String district, String gil, boolean underground, Integer gilNum) {
        super(si, district, null, gil, gilNum);
        this.underground = underground;

    }

    public boolean isUnderground() {
        return underground;
    }
}
