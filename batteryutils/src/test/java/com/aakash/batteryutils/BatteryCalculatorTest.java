package com.aakash.batteryutils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BatteryCalculatorTest {

    @Test
    public void calculatePercentage_standardScale() {
        assertEquals(50, BatteryCalculator.calculatePercentage(50, 100));
        assertEquals(100, BatteryCalculator.calculatePercentage(100, 100));
        assertEquals(0, BatteryCalculator.calculatePercentage(0, 100));
    }

    @Test
    public void calculatePercentage_nonStandardScale() {
        // Some devices report scale != 100.
        assertEquals(50, BatteryCalculator.calculatePercentage(25, 50));
    }

    @Test
    public void calculatePercentage_invalidScale_returnsNegativeOne() {
        assertEquals(-1, BatteryCalculator.calculatePercentage(50, 0));
        assertEquals(-1, BatteryCalculator.calculatePercentage(50, -10));
    }

    @Test
    public void calculatePowerWatts_validInputs() {
        // 5000 mV * 1,000,000 uA (1A) = 5.0 W
        float watts = BatteryCalculator.calculatePowerWatts(5000, 1_000_000);
        assertEquals(5.0f, watts, 0.001f);
    }

    @Test
    public void calculatePowerWatts_negativeCurrent_returnsAbsoluteValue() {
        float watts = BatteryCalculator.calculatePowerWatts(5000, -1_000_000);
        assertEquals(5.0f, watts, 0.001f);
    }

    @Test
    public void calculatePowerWatts_unavailableCurrent_returnsNaN() {
        float watts = BatteryCalculator.calculatePowerWatts(5000, BatteryInfo.UNAVAILABLE_INT);
        assertTrue(Float.isNaN(watts));
    }

    @Test
    public void calculateRatePercentPerHour_charging() {
        // From 50% to 60% over 30 minutes = +20%/hour
        float rate = BatteryCalculator.calculateRatePercentPerHour(50, 60, 30 * 60 * 1000L);
        assertEquals(20f, rate, 0.01f);
    }

    @Test
    public void calculateRatePercentPerHour_discharging() {
        float rate = BatteryCalculator.calculateRatePercentPerHour(60, 50, 30 * 60 * 1000L);
        assertEquals(-20f, rate, 0.01f);
    }

    @Test
    public void calculateRatePercentPerHour_invalidElapsed_returnsNaN() {
        assertTrue(Float.isNaN(BatteryCalculator.calculateRatePercentPerHour(50, 60, 0)));
    }
}
