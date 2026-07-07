package com.aakash.batteryutils;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * An immutable snapshot of every battery-related property this library was able
 * to read from the platform at {@link #getTimestampMillis()}.
 *
 * <p>Fields that the platform did not report, or that require an API level higher
 * than the device is running, use documented sentinel values ({@link #UNAVAILABLE_INT},
 * {@link #UNAVAILABLE_LONG}, {@link Float#NaN}) rather than 0 or -1, so that "not
 * reported" is never confused with a real reading of zero. Always check
 * {@code isXAvailable()} (or compare to the sentinel) before trusting a value that
 * may not be supported on a given device/API level. See {@link BatteryCapabilities}
 * for a way to check support up front.</p>
 *
 * <p>Build instances via {@link Builder}, or obtain a fully populated instance
 * from {@link BatteryUtils#getBatteryInfo(android.content.Context)}.</p>
 */
public final class BatteryInfo {

    /** Sentinel for an unavailable {@code int} field. */
    public static final int UNAVAILABLE_INT = Integer.MIN_VALUE;

    /** Sentinel for an unavailable {@code long} field. */
    public static final long UNAVAILABLE_LONG = Long.MIN_VALUE;

    private final long timestampMillis;

    private final int level;
    private final int scale;
    private final int percentage;

    private final BatteryStatus status;
    private final BatteryHealth health;
    private final ChargingType chargingType;
    private final boolean charging;
    private final boolean present;
    private final boolean batteryLow;

    private final String technology;

    private final float temperatureCelsius;
    private final int voltageMillivolts;

    private final int chargeCounterMicroAh;
    private final int currentNowMicroAmps;
    private final int currentAverageMicroAmps;
    private final long energyCounterNanoWattHours;
    private final int cycleCount;

    private final long chargeTimeRemainingMillis;

    private BatteryInfo(Builder b) {
        this.timestampMillis = b.timestampMillis;
        this.level = b.level;
        this.scale = b.scale;
        this.percentage = b.percentage;
        this.status = b.status;
        this.health = b.health;
        this.chargingType = b.chargingType;
        this.charging = b.charging;
        this.present = b.present;
        this.batteryLow = b.batteryLow;
        this.technology = b.technology;
        this.temperatureCelsius = b.temperatureCelsius;
        this.voltageMillivolts = b.voltageMillivolts;
        this.chargeCounterMicroAh = b.chargeCounterMicroAh;
        this.currentNowMicroAmps = b.currentNowMicroAmps;
        this.currentAverageMicroAmps = b.currentAverageMicroAmps;
        this.energyCounterNanoWattHours = b.energyCounterNanoWattHours;
        this.cycleCount = b.cycleCount;
        this.chargeTimeRemainingMillis = b.chargeTimeRemainingMillis;
    }

    // ---------------------------------------------------------------------
    // Core
    // ---------------------------------------------------------------------

    public long getTimestampMillis() {
        return timestampMillis;
    }

    /** Raw battery level, out of {@link #getScale()}. Prefer {@link #getPercentage()}. */
    public int getLevel() {
        return level;
    }

    /** Raw battery scale (typically 100, but not guaranteed by the platform contract). */
    public int getScale() {
        return scale;
    }

    /** Battery percentage, 0-100, computed as {@code level * 100 / scale}. */
    public int getPercentage() {
        return percentage;
    }

    @NonNull
    public BatteryStatus getStatus() {
        return status;
    }

    @NonNull
    public BatteryHealth getHealth() {
        return health;
    }

    @NonNull
    public ChargingType getChargingType() {
        return chargingType;
    }

    public boolean isCharging() {
        return charging;
    }

    public boolean isPresent() {
        return present;
    }

    /**
     * Whether the system considers the battery "low" ({@code EXTRA_BATTERY_LOW}).
     * Always {@code false} on API &lt; 28 (value simply not broadcast pre-P).
     */
    public boolean isBatteryLow() {
        return batteryLow;
    }

    @Nullable
    public String getTechnology() {
        return technology;
    }

    // ---------------------------------------------------------------------
    // Temperature / Voltage
    // ---------------------------------------------------------------------

    public float getTemperatureCelsius() {
        return temperatureCelsius;
    }

    public float getTemperatureFahrenheit() {
        return BatteryFormatter.celsiusToFahrenheit(temperatureCelsius);
    }

    public float getTemperatureKelvin() {
        return BatteryFormatter.celsiusToKelvin(temperatureCelsius);
    }

    /** Voltage in millivolts, as reported by {@code EXTRA_VOLTAGE}. */
    public int getVoltageMillivolts() {
        return voltageMillivolts;
    }

    public float getVoltageVolts() {
        return voltageMillivolts / 1000f;
    }

    // ---------------------------------------------------------------------
    // Charge / current / energy
    // ---------------------------------------------------------------------

    /**
     * Battery charge counter in microamp-hours. {@link #UNAVAILABLE_INT} if the
     * device's fuel gauge does not report it via
     * {@code BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER}.
     */
    public int getChargeCounterMicroAh() {
        return chargeCounterMicroAh;
    }

    /**
     * Instantaneous current draw in microamps (negative while discharging on most
     * devices, positive while charging — sign convention is OEM-dependent).
     * {@link #UNAVAILABLE_INT} if unsupported.
     */
    public int getCurrentNowMicroAmps() {
        return currentNowMicroAmps;
    }

    /** Average current in microamps over the last period. {@link #UNAVAILABLE_INT} if unsupported. */
    public int getCurrentAverageMicroAmps() {
        return currentAverageMicroAmps;
    }

    /** Remaining energy in nanowatt-hours. {@link #UNAVAILABLE_LONG} if unsupported. */
    public long getEnergyCounterNanoWattHours() {
        return energyCounterNanoWattHours;
    }

    /**
     * Battery charge cycle count, from {@code EXTRA_CYCLE_COUNT}. Requires API 34+;
     * {@link #UNAVAILABLE_INT} on lower API levels or if the OEM does not populate it.
     */
    public int getCycleCount() {
        return cycleCount;
    }

    /**
     * Estimated milliseconds until the battery is fully charged, from
     * {@code BatteryManager.computeChargeTimeRemaining()}. Requires API 28+.
     * {@link #UNAVAILABLE_LONG} if unsupported, not charging, or unknown
     * (the platform method itself returns -1 in those cases).
     */
    public long getChargeTimeRemainingMillis() {
        return chargeTimeRemainingMillis;
    }

    // ---------------------------------------------------------------------
    // Derived / convenience
    // ---------------------------------------------------------------------

    /**
     * Instantaneous power draw in watts, computed as voltage(V) &times; current(A).
     * Returns {@link Float#NaN} if either input is unavailable.
     */
    public float getPowerWatts() {
        if (currentNowMicroAmps == UNAVAILABLE_INT || voltageMillivolts <= 0) {
            return Float.NaN;
        }
        float amps = currentNowMicroAmps / 1_000_000f;
        float volts = voltageMillivolts / 1000f;
        return Math.abs(amps * volts);
    }

    public boolean isChargeCounterAvailable() {
        return chargeCounterMicroAh != UNAVAILABLE_INT;
    }

    public boolean isCurrentNowAvailable() {
        return currentNowMicroAmps != UNAVAILABLE_INT;
    }

    public boolean isCurrentAverageAvailable() {
        return currentAverageMicroAmps != UNAVAILABLE_INT;
    }

    public boolean isEnergyCounterAvailable() {
        return energyCounterNanoWattHours != UNAVAILABLE_LONG;
    }

    public boolean isCycleCountAvailable() {
        return cycleCount != UNAVAILABLE_INT;
    }

    public boolean isChargeTimeRemainingAvailable() {
        return chargeTimeRemainingMillis != UNAVAILABLE_LONG;
    }

    // ---------------------------------------------------------------------
    // Export
    // ---------------------------------------------------------------------

    /** Exports this snapshot as a {@link Bundle}, e.g. for passing between Activities/Fragments. */
    @NonNull
    public Bundle toBundle() {
        Bundle b = new Bundle();
        b.putLong("timestampMillis", timestampMillis);
        b.putInt("level", level);
        b.putInt("scale", scale);
        b.putInt("percentage", percentage);
        b.putString("status", status.name());
        b.putString("health", health.name());
        b.putString("chargingType", chargingType.name());
        b.putBoolean("charging", charging);
        b.putBoolean("present", present);
        b.putBoolean("batteryLow", batteryLow);
        b.putString("technology", technology);
        b.putFloat("temperatureCelsius", temperatureCelsius);
        b.putInt("voltageMillivolts", voltageMillivolts);
        b.putInt("chargeCounterMicroAh", chargeCounterMicroAh);
        b.putInt("currentNowMicroAmps", currentNowMicroAmps);
        b.putInt("currentAverageMicroAmps", currentAverageMicroAmps);
        b.putLong("energyCounterNanoWattHours", energyCounterNanoWattHours);
        b.putInt("cycleCount", cycleCount);
        b.putLong("chargeTimeRemainingMillis", chargeTimeRemainingMillis);
        return b;
    }

    /** Exports this snapshot as a {@link Map}, keyed identically to {@link #toBundle()}. */
    @NonNull
    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("timestampMillis", timestampMillis);
        m.put("level", level);
        m.put("scale", scale);
        m.put("percentage", percentage);
        m.put("status", status.name());
        m.put("health", health.name());
        m.put("chargingType", chargingType.name());
        m.put("charging", charging);
        m.put("present", present);
        m.put("batteryLow", batteryLow);
        m.put("technology", technology);
        m.put("temperatureCelsius", temperatureCelsius);
        m.put("voltageMillivolts", voltageMillivolts);
        m.put("chargeCounterMicroAh", chargeCounterMicroAh);
        m.put("currentNowMicroAmps", currentNowMicroAmps);
        m.put("currentAverageMicroAmps", currentAverageMicroAmps);
        m.put("energyCounterNanoWattHours", energyCounterNanoWattHours);
        m.put("cycleCount", cycleCount);
        m.put("chargeTimeRemainingMillis", chargeTimeRemainingMillis);
        return m;
    }

    /** Exports this snapshot as a {@link JSONObject}. */
    @NonNull
    public JSONObject toJson() {
        JSONObject o = new JSONObject();
        try {
            o.put("timestampMillis", timestampMillis);
            o.put("level", level);
            o.put("scale", scale);
            o.put("percentage", percentage);
            o.put("status", status.name());
            o.put("health", health.name());
            o.put("chargingType", chargingType.name());
            o.put("charging", charging);
            o.put("present", present);
            o.put("batteryLow", batteryLow);
            o.put("technology", technology);
            o.put("temperatureCelsius", temperatureCelsius);
            o.put("voltageMillivolts", voltageMillivolts);
            o.put("chargeCounterMicroAh", chargeCounterMicroAh);
            o.put("currentNowMicroAmps", currentNowMicroAmps);
            o.put("currentAverageMicroAmps", currentAverageMicroAmps);
            o.put("energyCounterNanoWattHours", energyCounterNanoWattHours);
            o.put("cycleCount", cycleCount);
            o.put("chargeTimeRemainingMillis", chargeTimeRemainingMillis);
        } catch (JSONException e) {
            // JSONException here would only be thrown for a null key, which never happens
            // above, so this branch is unreachable in practice.
            BatteryLogger.e("BatteryInfo", "Failed to build JSON", e);
        }
        return o;
    }

    @NonNull
    @Override
    public String toString() {
        return "BatteryInfo{" +
                "percentage=" + percentage +
                ", status=" + status +
                ", health=" + health +
                ", chargingType=" + chargingType +
                ", charging=" + charging +
                ", temperatureCelsius=" + temperatureCelsius +
                ", voltageMillivolts=" + voltageMillivolts +
                '}';
    }

    // ---------------------------------------------------------------------
    // Builder
    // ---------------------------------------------------------------------

    public static final class Builder {
        private long timestampMillis = System.currentTimeMillis();
        private int level;
        private int scale = 100;
        private int percentage;
        private BatteryStatus status = BatteryStatus.UNKNOWN;
        private BatteryHealth health = BatteryHealth.UNKNOWN;
        private ChargingType chargingType = ChargingType.NONE;
        private boolean charging;
        private boolean present = true;
        private boolean batteryLow;
        private String technology;
        private float temperatureCelsius = Float.NaN;
        private int voltageMillivolts;
        private int chargeCounterMicroAh = UNAVAILABLE_INT;
        private int currentNowMicroAmps = UNAVAILABLE_INT;
        private int currentAverageMicroAmps = UNAVAILABLE_INT;
        private long energyCounterNanoWattHours = UNAVAILABLE_LONG;
        private int cycleCount = UNAVAILABLE_INT;
        private long chargeTimeRemainingMillis = UNAVAILABLE_LONG;

        public Builder timestampMillis(long v) { this.timestampMillis = v; return this; }
        public Builder level(int v) { this.level = v; return this; }
        public Builder scale(int v) { this.scale = v; return this; }
        public Builder percentage(int v) { this.percentage = v; return this; }
        public Builder status(BatteryStatus v) { this.status = v; return this; }
        public Builder health(BatteryHealth v) { this.health = v; return this; }
        public Builder chargingType(ChargingType v) { this.chargingType = v; return this; }
        public Builder charging(boolean v) { this.charging = v; return this; }
        public Builder present(boolean v) { this.present = v; return this; }
        public Builder batteryLow(boolean v) { this.batteryLow = v; return this; }
        public Builder technology(String v) { this.technology = v; return this; }
        public Builder temperatureCelsius(float v) { this.temperatureCelsius = v; return this; }
        public Builder voltageMillivolts(int v) { this.voltageMillivolts = v; return this; }
        public Builder chargeCounterMicroAh(int v) { this.chargeCounterMicroAh = v; return this; }
        public Builder currentNowMicroAmps(int v) { this.currentNowMicroAmps = v; return this; }
        public Builder currentAverageMicroAmps(int v) { this.currentAverageMicroAmps = v; return this; }
        public Builder energyCounterNanoWattHours(long v) { this.energyCounterNanoWattHours = v; return this; }
        public Builder cycleCount(int v) { this.cycleCount = v; return this; }
        public Builder chargeTimeRemainingMillis(long v) { this.chargeTimeRemainingMillis = v; return this; }

        @NonNull
        public BatteryInfo build() {
            return new BatteryInfo(this);
        }
    }
}