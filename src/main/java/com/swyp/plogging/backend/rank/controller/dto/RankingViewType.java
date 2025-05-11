package com.swyp.plogging.backend.rank.controller.dto;

import com.swyp.plogging.backend.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public enum RankingViewType {

    ALL, WEEKLY;

    public static RankingViewType from(String value) {
        if (value == null || value.isBlank()) {
            return ALL;
        }

        try {
            return RankingViewType.valueOf(value.toUpperCase());
        } catch (CustomException e) {
            throw new CustomException("Unsupported request type : " + value, HttpStatus.BAD_REQUEST);
        }
    }
}
