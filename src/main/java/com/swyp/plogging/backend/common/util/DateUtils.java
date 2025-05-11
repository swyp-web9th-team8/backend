package com.swyp.plogging.backend.common.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DateUtils {

    private DateUtils() {
        throw new UnsupportedOperationException("Utility class... do not instantiate");
    }

    public static LocalDateTime getStartOfCurrentWeek() {
        LocalDate today = LocalDate.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();

        int minusDays = dayOfWeek == DayOfWeek.SUNDAY ? 0 : dayOfWeek.getValue();
        return today.minusDays(minusDays).atStartOfDay();
    }
}
