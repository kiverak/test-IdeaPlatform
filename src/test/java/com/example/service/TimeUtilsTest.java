package com.example.service;

import com.example.utils.TimeUtils;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class TimeUtilsTest {

    @Test
    void correction_winter_TLV_plus2_VVO_plus10() {
        // VVO(UTC+10) -> TLV(UTC+2) : 7 hours
        assertEquals(Duration.ofHours(7),
                TimeUtils.getTimeZoneCorrection("VVO", "TLV"));

        // TLV(UTC+2) -> VVO(UTC+10): -7 hours
        assertEquals(Duration.ofHours(-7),
                TimeUtils.getTimeZoneCorrection("TLV", "VVO"));

        // Same zone => 0
        assertEquals(Duration.ZERO,
                TimeUtils.getTimeZoneCorrection("TLV", "TLV"));
    }

    @Test
    void unknown_cityCode_throws() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> TimeUtils.getTimeZoneCorrection("XXX", "TLV")
        );
        assertTrue(ex.getMessage().contains("City code not recognized"));
    }
}