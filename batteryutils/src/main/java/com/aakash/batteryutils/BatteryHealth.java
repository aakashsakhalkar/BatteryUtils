package com.aakash.batteryutils;

import android.os.BatteryManager;

/**
 * Represents the physical health of the battery, as reported by the platform
 * via {@link BatteryManager#EXTRA_HEALTH}.
 *
 * <p>This is a direct mapping of the platform's {@code BATTERY_HEALTH_*} integer
 * constants. Android does not expose a numeric "health score" or "health grade" —
 * only this coarse enumeration. Any score/grade derived from it (see
 * {@link BatteryAnalyzer}) is an approximation built on top of this value and
 * other signals, not a platform-provided metric.</p>
 */
public enum BatteryHealth {

    UNKNOWN(BatteryManager.BATTERY_HEALTH_UNKNOWN),
    GOOD(BatteryManager.BATTERY_HEALTH_GOOD),
    OVERHEAT(BatteryManager.BATTERY_HEALTH_OVERHEAT),
    DEAD(BatteryManager.BATTERY_HEALTH_DEAD),
    OVER_VOLTAGE(BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE),
    UNSPECIFIED_FAILURE(BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE),
    COLD(BatteryManager.BATTERY_HEALTH_COLD);

    private final int platformValue;

    BatteryHealth(int platformValue) {
        this.platformValue = platformValue;
    }

    public int getPlatformValue() {
        return platformValue;
    }

    /**
     * Maps a raw platform integer (as returned in {@code EXTRA_HEALTH}) to this enum.
     *
     * @param platformValue the raw value from the platform
     * @return the matching {@link BatteryHealth}, or {@link #UNKNOWN} if unrecognized
     */
    public static BatteryHealth fromPlatformValue(int platformValue) {
        for (BatteryHealth health : values()) {
            if (health.platformValue == platformValue) {
                return health;
            }
        }
        return UNKNOWN;
    }

    /**
     * @return true if this health state indicates a problem the user should know about.
     */
    public boolean isProblematic() {
        return this == OVERHEAT || this == DEAD || this == OVER_VOLTAGE
                || this == UNSPECIFIED_FAILURE || this == COLD;
    }
}
