package com.example.utils;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

public class TimeUtils {

    private static final Map<String, String> cityCodeToZoneMap = new HashMap<>();

    static {
        cityCodeToZoneMap.put("TLV", "Asia/Jerusalem");
        cityCodeToZoneMap.put("VVO", "Asia/Vladivostok");
    }

    public static Duration getTimeZoneCorrection(String origin, String destination) {
        int offsetDif = getGmtOffset(origin) - getGmtOffset(destination);
        return Duration.ZERO.plusHours(offsetDif);
    }

    private static int getGmtOffset(String cityCode) {
        String zoneIdString = cityCodeToZoneMap.get(cityCode.toUpperCase());
        if (zoneIdString == null) {
            throw new IllegalArgumentException("City code not recognized: " + cityCode);
        }

        ZoneId zoneId = ZoneId.of(zoneIdString);
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZoneOffset offset = now.getOffset();

        // Convert offset to +3, -5, etc.
        int totalSeconds = offset.getTotalSeconds();

        // Return hours
        return totalSeconds / 3600;
    }
}
