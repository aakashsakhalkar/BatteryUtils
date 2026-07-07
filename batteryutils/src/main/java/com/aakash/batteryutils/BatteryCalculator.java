package com.aakash.batteryutils;

/**
 * Stateless numeric calculations built on top of raw platform values. Every
 * method here is a pure function of its inputs — no hidden APIs, no invented
 * constants — so results can be reasoned about and unit-tested directly.
 */
public final class BatteryCalculator {

    private BatteryCalculator() {
    }

    /** Computes percentage (0-100) from raw level/scale, matching platform convention. */
    public static int calculatePercentage(int level, int scale) {
        if (scale <= 0) {
            return -1;
        }
        return Math.round(level * 100f / scale);
    }

    /**
     * Computes instantaneous power in watts from voltage (mV) and current (µA).
     * Returns {@link Float#NaN} if either input is a sentinel/invalid value.
     */
    public static float calculatePowerWatts(int voltageMillivolts, int currentMicroAmps) {
        if (voltageMillivolts <= 0 || currentMicroAmps == BatteryInfo.UNAVAILABLE_INT) {
            return Float.NaN;
        }
        float volts = voltageMillivolts / 1000f;
        float amps = currentMicroAmps / 1_000_000f;
        return Math.abs(volts * amps);
    }

    /**
     * Converts a charge counter value in microamp-hours to milliamp-hours, the
     * more commonly displayed unit.
     */
    public static float microAhToMilliAh(int microAh) {
        if (microAh == BatteryInfo.UNAVAILABLE_INT) {
            return Float.NaN;
        }
        return microAh / 1000f;
    }

    /**
     * Converts an energy counter value in nanowatt-hours to watt-hours.
     */
    public static float nanoWattHoursToWattHours(long nanoWattHours) {
        if (nanoWattHours == BatteryInfo.UNAVAILABLE_LONG) {
            return Float.NaN;
        }
        return nanoWattHours / 1_000_000_000f;
    }

    /**
     * Given two percentage samples and the elapsed time between them, estimates
     * the percentage change per hour. Positive while charging, negative while
     * discharging. Returns {@link Float#NaN} if the elapsed time is not positive.
     */
    public static float calculateRatePercentPerHour(int fromPercentage, int toPercentage,
                                                      long elapsedMillis) {
        if (elapsedMillis <= 0) {
            return Float.NaN;
        }
        float deltaPercent = toPercentage - fromPercentage;
        float hours = elapsedMillis / 3_600_000f;
        return deltaPercent / hours;
    }
}
