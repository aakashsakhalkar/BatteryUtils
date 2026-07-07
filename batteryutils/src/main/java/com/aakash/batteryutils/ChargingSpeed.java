package com.aakash.batteryutils;

/**
 * A heuristic classification of charging speed based on instantaneous current
 * draw. Android has <b>no public API</b> that directly reports "fast charging"
 * or "slow charging" as a boolean/enum — this is a best-effort approximation
 * built solely from {@code BatteryManager.BATTERY_PROPERTY_CURRENT_NOW}, not a
 * platform-reported fact. Thresholds are configurable via {@link BatteryEstimator}
 * overloads.
 */
public enum ChargingSpeed {

    /** Not currently charging, or current draw unavailable on this device. */
    NOT_CHARGING,

    /** Charging at a low rate (below the "normal" threshold). */
    SLOW,

    /** Charging at a typical rate. */
    NORMAL,

    /** Charging at a high rate (above the "fast" threshold). */
    FAST,

    /** Charging, but current draw could not be read to classify speed. */
    UNKNOWN
}