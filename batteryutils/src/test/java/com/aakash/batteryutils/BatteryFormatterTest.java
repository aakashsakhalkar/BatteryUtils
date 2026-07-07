package com.aakash.batteryutils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BatteryFormatterTest {

    @Test
    public void celsiusToFahrenheit_freezingAndBoiling() {
        assertEquals(32f, BatteryFormatter.celsiusToFahrenheit(0f), 0.01f);
        assertEquals(212f, BatteryFormatter.celsiusToFahrenheit(100f), 0.01f);
    }

    @Test
    public void celsiusToKelvin_absoluteZeroOffset() {
        assertEquals(273.15f, BatteryFormatter.celsiusToKelvin(0f), 0.01f);
    }

    @Test
    public void formatDurationMillis_underAnHour() {
        assertEquals("45m", BatteryFormatter.formatDurationMillis(45 * 60 * 1000L));
    }

    @Test
    public void formatDurationMillis_overAnHour() {
        assertEquals("2h 15m", BatteryFormatter.formatDurationMillis((2 * 60 + 15) * 60 * 1000L));
    }

    @Test
    public void formatDurationMillis_negative_returnsNA() {
        assertEquals("N/A", BatteryFormatter.formatDurationMillis(-1));
    }

    @Test
    public void formatPercentage_appendsPercentSign() {
        assertEquals("42%", BatteryFormatter.formatPercentage(42));
    }

    @Test
    public void formatCurrentMicroAmps_unavailable_returnsNA() {
        assertEquals("N/A", BatteryFormatter.formatCurrentMicroAmps(BatteryInfo.UNAVAILABLE_INT));
    }
}
