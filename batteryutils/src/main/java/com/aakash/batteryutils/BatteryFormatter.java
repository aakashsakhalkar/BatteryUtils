package com.aakash.batteryutils;

import androidx.annotation.NonNull;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Stateless formatting helpers for presenting battery values to users.
 * All methods are pure functions with no Android context dependency, so they
 * are trivially unit-testable.
 */
public final class BatteryFormatter {

    private BatteryFormatter() {
    }

    // ---------------------------------------------------------------------
    // Temperature
    // ---------------------------------------------------------------------

    public static float celsiusToFahrenheit(float celsius) {
        return (celsius * 9f / 5f) + 32f;
    }

    public static float celsiusToKelvin(float celsius) {
        return celsius + 273.15f;
    }

    @NonNull
    public static String formatTemperatureCelsius(float celsius) {
        return String.format(Locale.getDefault(), "%.1f\u00B0C", celsius);
    }

    @NonNull
    public static String formatTemperatureFahrenheit(float celsius) {
        return String.format(Locale.getDefault(), "%.1f\u00B0F", celsiusToFahrenheit(celsius));
    }

    @NonNull
    public static String formatTemperatureKelvin(float celsius) {
        return String.format(Locale.getDefault(), "%.1fK", celsiusToKelvin(celsius));
    }

    // ---------------------------------------------------------------------
    // Voltage / current / power
    // ---------------------------------------------------------------------

    @NonNull
    public static String formatVoltage(int millivolts) {
        return String.format(Locale.getDefault(), "%.2f V", millivolts / 1000f);
    }

    @NonNull
    public static String formatCurrentMicroAmps(int microAmps) {
        if (microAmps == BatteryInfo.UNAVAILABLE_INT) {
            return "N/A";
        }
        return String.format(Locale.getDefault(), "%.0f mA", microAmps / 1000f);
    }

    @NonNull
    public static String formatPowerWatts(float watts) {
        if (Float.isNaN(watts)) {
            return "N/A";
        }
        return String.format(Locale.getDefault(), "%.2f W", watts);
    }

    // ---------------------------------------------------------------------
    // Time
    // ---------------------------------------------------------------------

    /**
     * Formats a millisecond duration as {@code "Hh Mm"} (or {@code "Mm"} if under an hour).
     * Returns {@code "N/A"} for negative/sentinel values.
     */
    @NonNull
    public static String formatDurationMillis(long millis) {
        if (millis < 0) {
            return "N/A";
        }
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        if (hours > 0) {
            return String.format(Locale.getDefault(), "%dh %dm", hours, minutes);
        }
        return String.format(Locale.getDefault(), "%dm", minutes);
    }

    @NonNull
    public static String formatPercentage(int percentage) {
        return percentage + "%";
    }
}
