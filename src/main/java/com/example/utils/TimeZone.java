package com.example.utils;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

public class TimeZone {

    // Custom mapping from IATA city codes to time zone IDs
    private static final Map<String, String> cityCodeToZoneMap = new HashMap<>();

    static {
        cityCodeToZoneMap.put("TLV", "Asia/Jerusalem");
        cityCodeToZoneMap.put("VVO", "Asia/Vladivostok");
        cityCodeToZoneMap.put("JFK", "America/New_York");
        cityCodeToZoneMap.put("NRT", "Asia/Tokyo");
        // Add more mappings as needed
    }

    public static String getGmtOffset(String cityCode) {
        String zoneIdString = cityCodeToZoneMap.get(cityCode.toUpperCase());
        if (zoneIdString == null) {
            throw new IllegalArgumentException("City code not recognized: " + cityCode);
        }

        ZoneId zoneId = ZoneId.of(zoneIdString);
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZoneOffset offset = now.getOffset();

        // Convert offset to +3, -5, etc.
        int totalSeconds = offset.getTotalSeconds();
        int hours = totalSeconds / 3600;

        return (hours >= 0 ? "+" : "") + hours;
    }
}
