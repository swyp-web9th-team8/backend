package com.swyp.plogging.backend.common.domain;

import java.util.Arrays;

public enum ImageExtension {
    JPG, JPEG, PNG, TIFF;

    public static boolean isAllowed(String extension) {
        return Arrays.stream(values())
            .anyMatch(e -> e.name().equalsIgnoreCase(extension));
    }
}
