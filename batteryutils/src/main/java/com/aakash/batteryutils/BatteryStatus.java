package com.aakash.batteryutils;

import android.os.BatteryManager;

/**
 * Represents the charging status of the battery, as reported by the platform
 * via {@link BatteryManager#EXTRA_STATUS} on the
 * {@link android.content.Intent#ACTION_BATTERY_CHANGED} sticky broadcast.
 *
 * <p>This is a direct, lossless mapping of the platform's {@code BATTERY_STATUS_*}
 * integer constants into a type-safe enum. No values are invented or guessed.</p>
 */
public enum BatteryStatus {

    /** Status could not be determined by the platform. */
    UNKNOWN(BatteryManager.BATTERY_STATUS_UNKNOWN),

    /** Battery is actively charging (via any source). */
    CHARGING(BatteryManager.BATTERY_STATUS_CHARGING),

    /** Battery is discharging (not connected to a power source, or connected but not charging). */
    DISCHARGING(BatteryManager.BATTERY_STATUS_DISCHARGING),

    /** Connected to a power source but not currently charging (e.g. paused due to temperature). */
    NOT_CHARGING(BatteryManager.BATTERY_STATUS_NOT_CHARGING),

    /** Battery is full. */
    FULL(BatteryManager.BATTERY_STATUS_FULL);

    private final int platformValue;

    BatteryStatus(int platformValue) {
        this.platformValue = platformValue;
    }

    /**
     * @return the raw platform integer constant this value represents.
     */
    public int getPlatformValue() {
        return platformValue;
    }

    /**
     * Maps a raw platform integer (as returned in {@code EXTRA_STATUS}) to this enum.
     *
     * @param platformValue the raw value from the platform
     * @return the matching {@link BatteryStatus}, or {@link #UNKNOWN} if unrecognized
     */
    public static BatteryStatus fromPlatformValue(int platformValue) {
        for (BatteryStatus status : values()) {
            if (status.platformValue == platformValue) {
                return status;
            }
        }
        return UNKNOWN;
    }

    /**
     * Convenience check for whether this status represents an actively charging battery.
     */
    public boolean isCharging() {
        return this == CHARGING;
    }
}
