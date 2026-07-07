package com.aakash.batteryutils;

import android.content.Context;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.time.Duration;

/**
 * Estimation helpers for charging time, discharge time, and charging speed.
 *
 * <p>Two categories of methods live here, and the Javadoc on each is explicit
 * about which is which:</p>
 * <ul>
 *     <li><b>Platform-reported estimates</b> — thin wrappers around a real
 *     Android API (e.g. {@link #getChargeTimeRemainingMillis(Context)},
 *     {@link #getDischargePrediction(Context)}). These return exactly what the
 *     OS reports, nothing invented.</li>
 *     <li><b>Heuristic estimates</b> — approximations this library computes
 *     itself (e.g. {@link #classifyChargingSpeed(Context)},
 *     {@link #estimateDischargeTimeMillis}) because no public platform API
 *     provides the value directly. These are clearly documented as
 *     approximations and should be labeled as "estimated" in any UI.</li>
 * </ul>
 */
public final class BatteryEstimator {

    /** Default current-draw threshold (in mA) below which charging is classified as SLOW. */
    public static final int DEFAULT_SLOW_THRESHOLD_MA = 500;

    /** Default current-draw threshold (in mA) above which charging is classified as FAST. */
    public static final int DEFAULT_FAST_THRESHOLD_MA = 1500;

    private BatteryEstimator() {
    }

    // ---------------------------------------------------------------------
    // Platform-reported estimates
    // ---------------------------------------------------------------------

    /**
     * Wraps {@code BatteryManager.computeChargeTimeRemaining()} (API 28+).
     *
     * @return milliseconds until full charge, or {@link BatteryInfo#UNAVAILABLE_LONG}
     * if unsupported, not charging, or unknown.
     */
    public static long getChargeTimeRemainingMillis(@NonNull Context context) {
        return BatteryUtils.getChargeTimeRemainingMillis(context);
    }

    /**
     * Wraps {@code PowerManager.getBatteryDischargePrediction()} (API 31+), the
     * same estimate shown in system Settings &gt; Battery.
     *
     * @return a {@link DischargePrediction}, or {@code null} if unsupported
     * (API &lt; 31), the device is powered/charging, or the platform could not
     * produce an estimate.
     */
    @Nullable
    public static DischargePrediction getDischargePrediction(@NonNull Context context) {
        if (!BatteryCapabilities.isDischargePredictionSupported()) {
            return null;
        }
        PowerManager pm = (PowerManager) context.getApplicationContext()
                .getSystemService(Context.POWER_SERVICE);
        if (pm == null) {
            return null;
        }
        return getDischargePredictionCompat(pm);
    }

    @RequiresApi(31)
    private static DischargePrediction getDischargePredictionCompat(PowerManager pm) {
        Duration remaining = pm.getBatteryDischargePrediction();
        if (remaining == null) {
            return null;
        }
        boolean personalized = pm.isBatteryDischargePredictionPersonalized();
        return new DischargePrediction(remaining.toMillis(), personalized);
    }

    /** Result of {@link #getDischargePrediction(Context)}. */
    public static final class DischargePrediction {
        private final long remainingMillis;
        private final boolean personalized;

        DischargePrediction(long remainingMillis, boolean personalized) {
            this.remainingMillis = remainingMillis;
            this.personalized = personalized;
        }

        public long getRemainingMillis() {
            return remainingMillis;
        }

        /** Whether the estimate is personalized to this device's usage history. */
        public boolean isPersonalized() {
            return personalized;
        }
    }

    // ---------------------------------------------------------------------
    // Heuristic estimates
    // ---------------------------------------------------------------------

    /**
     * <b>Heuristic.</b> Classifies current charging speed from instantaneous
     * current draw, using the default thresholds. Not a platform-reported value
     * — see class Javadoc.
     */
    @NonNull
    public static ChargingSpeed classifyChargingSpeed(@NonNull Context context) {
        return classifyChargingSpeed(context, DEFAULT_SLOW_THRESHOLD_MA, DEFAULT_FAST_THRESHOLD_MA);
    }

    /**
     * <b>Heuristic.</b> Classifies current charging speed from instantaneous
     * current draw using caller-supplied thresholds (in mA).
     */
    @NonNull
    public static ChargingSpeed classifyChargingSpeed(@NonNull Context context,
                                                        int slowThresholdMa, int fastThresholdMa) {
        BatteryInfo info = BatteryUtils.getBatteryInfo(context);
        if (!info.isCharging()) {
            return ChargingSpeed.NOT_CHARGING;
        }
        if (!info.isCurrentNowAvailable()) {
            return ChargingSpeed.UNKNOWN;
        }
        float mA = Math.abs(info.getCurrentNowMicroAmps() / 1000f);
        if (mA < slowThresholdMa) {
            return ChargingSpeed.SLOW;
        }
        if (mA > fastThresholdMa) {
            return ChargingSpeed.FAST;
        }
        return ChargingSpeed.NORMAL;
    }

    /**
     * <b>Heuristic.</b> Estimates remaining discharge time in milliseconds from
     * two percentage samples taken over a known time window (e.g. from your own
     * periodic {@link BatteryMonitor} callbacks). This is a simple linear
     * extrapolation, not a platform estimate — prefer
     * {@link #getDischargePrediction(Context)} on API 31+ where available, which
     * uses the system's own (usage-history-aware) model.
     *
     * @param currentPercentage    current battery percentage
     * @param earlierPercentage    an earlier percentage sample
     * @param elapsedMillisBetween milliseconds between the two samples
     * @return estimated milliseconds remaining until 0%, or -1 if not
     * discharging, the samples are invalid, or the trend is flat/charging
     */
    public static long estimateDischargeTimeMillis(int currentPercentage, int earlierPercentage,
                                                      long elapsedMillisBetween) {
        if (elapsedMillisBetween <= 0 || currentPercentage >= earlierPercentage) {
            return -1;
        }
        float dropPerMs = (earlierPercentage - currentPercentage) / (float) elapsedMillisBetween;
        if (dropPerMs <= 0) {
            return -1;
        }
        return (long) (currentPercentage / dropPerMs);
    }
}
