package com.aakash.batteryutils;

import android.content.Context;
import android.os.BatteryManager;
import android.os.Build;
import android.os.PowerManager;

import androidx.annotation.NonNull;

/**
 * Reports which battery-related features are actually available on the current
 * device and API level, so callers can gracefully hide/disable UI for anything
 * unsupported instead of showing a fabricated or default value.
 *
 * <p>A feature reported as unsupported here means Android's public API genuinely
 * does not expose it on this API level — it is not a bug in this library. Where
 * a whole category of the original feature request has no public API on any
 * Android version (e.g. reverse-charging detection, or reading design/full-charge
 * capacity), that is documented on the relevant method and is intentionally
 * absent from the library rather than implemented via reflection on hidden
 * framework APIs, which would be unreliable and blocked by the platform's
 * non-SDK interface restrictions on modern API levels.</p>
 */
public final class BatteryCapabilities {

    private BatteryCapabilities() {
    }

    /** {@code BatteryManager.isCharging()} — requires API 23+. */
    public static boolean isChargingQuerySupported() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /** {@code BatteryManager.computeChargeTimeRemaining()} — requires API 28+. */
    public static boolean isChargeTimeRemainingSupported() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;
    }

    /** {@code EXTRA_BATTERY_LOW} on the sticky broadcast — requires API 28+. */
    public static boolean isBatteryLowFlagSupported() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;
    }

    /** {@code EXTRA_CYCLE_COUNT} on the sticky broadcast — requires API 34+. */
    public static boolean isCycleCountSupported() {
        return Build.VERSION.SDK_INT >= 34;
    }

    /** {@code BATTERY_PLUGGED_DOCK} as a distinct charging source — requires API 33+. */
    public static boolean isDockChargingSourceSupported() {
        return Build.VERSION.SDK_INT >= 33;
    }

    /** {@code PowerManager.isDeviceIdleMode()} (Doze) — requires API 23+. */
    public static boolean isDeviceIdleQuerySupported() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * {@code PowerManager.getBatteryDischargePrediction()} /
     * {@code isBatteryDischargePredictionPersonalized()} — requires API 31+.
     */
    public static boolean isDischargePredictionSupported() {
        return Build.VERSION.SDK_INT >= 31;
    }

    /**
     * Whether {@code BatteryManager.BATTERY_PROPERTY_CURRENT_NOW} can be queried
     * on this API level. The property itself has existed since API 21, but
     * individual devices may still return {@link Integer#MIN_VALUE} if their
     * fuel gauge driver doesn't implement it — check the returned
     * {@link BatteryInfo#isCurrentNowAvailable()} for the actual per-device result.
     */
    public static boolean isCurrentNowApiSupported() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    /**
     * Checks battery presence via the sticky broadcast. Always "supported" (the
     * extra has existed since API 1); provided for symmetry/completeness.
     */
    public static boolean isBatteryPresenceQuerySupported() {
        return true;
    }

    /**
     * <b>Not available on any public Android API.</b> There is no standard,
     * public API to read design capacity or full-charge capacity in mAh — these
     * exist only as hidden/OEM-private fuel gauge fields not exposed through
     * {@link BatteryManager}. Always returns {@code false}; kept as an explicit,
     * documented method rather than silently omitting the capability so callers
     * know this was considered, not missed.
     */
    public static boolean isDesignOrFullChargeCapacitySupported() {
        return false;
    }

    /**
     * <b>Not available on any public Android API.</b> Stock Android exposes no
     * public API for reverse charging (device-to-device power sharing) detection.
     * Always returns {@code false}. See {@link ChargingType} for details.
     */
    public static boolean isReverseChargingDetectionSupported() {
        return false;
    }

    /**
     * <b>No direct system-wide public API.</b> Android does not expose a single
     * public call to check whether Adaptive Battery is globally enabled. The
     * closest public signal is {@code UsageStatsManager.getAppStandbyBucket()},
     * which reports your own app's standby bucket (a consequence of Adaptive
     * Battery/App Standby), not a global on/off flag. Always returns
     * {@code false} here since there is no direct equivalent.
     */
    public static boolean isAdaptiveBatteryGlobalQuerySupported() {
        return false;
    }

    /** {@code PowerManager.isPowerSaveMode()} — has existed since API 21. */
    public static boolean isBatterySaverQuerySupported() {
        return true;
    }

    /**
     * Returns a short, human-readable capability report, useful for a
     * "Capabilities" screen in a demo app or for bug reports.
     */
    @NonNull
    public static String describeCapabilities(@NonNull Context context) {
        StringBuilder sb = new StringBuilder();
        sb.append("API level: ").append(Build.VERSION.SDK_INT).append('\n');
        sb.append("isCharging(): ").append(isChargingQuerySupported()).append('\n');
        sb.append("chargeTimeRemaining(): ").append(isChargeTimeRemainingSupported()).append('\n');
        sb.append("batteryLowFlag: ").append(isBatteryLowFlagSupported()).append('\n');
        sb.append("cycleCount: ").append(isCycleCountSupported()).append('\n');
        sb.append("dockChargingSource: ").append(isDockChargingSourceSupported()).append('\n');
        sb.append("deviceIdleQuery: ").append(isDeviceIdleQuerySupported()).append('\n');
        sb.append("dischargePrediction: ").append(isDischargePredictionSupported()).append('\n');
        sb.append("designOrFullChargeCapacity: ").append(isDesignOrFullChargeCapacitySupported()).append('\n');
        sb.append("reverseChargingDetection: ").append(isReverseChargingDetectionSupported()).append('\n');
        sb.append("adaptiveBatteryGlobalQuery: ").append(isAdaptiveBatteryGlobalQuerySupported());
        return sb.toString();
    }
}
