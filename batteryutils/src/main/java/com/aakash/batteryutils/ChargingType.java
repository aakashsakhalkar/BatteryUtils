package com.aakash.batteryutils;

import android.os.BatteryManager;
import android.os.Build;

/**
 * Represents the physical power source the device is currently connected to,
 * as reported by the platform via {@link BatteryManager#EXTRA_PLUGGED}.
 *
 * <p><b>Platform limitation — reverse charging:</b> Stock Android (AOSP) does not
 * expose any public API to detect "reverse charging" (a device charging another
 * device via USB-OTG or reverse wireless charging). Some OEMs (e.g. Samsung, via
 * {@code com.samsung.android.hardware.power}-style hidden broadcasts) implement
 * this privately, but there is no public, standard API for it. This library does
 * not fabricate a REVERSE value backed by a real API; if you need OEM-specific
 * reverse-charging detection, it must be implemented separately against that
 * OEM's private SDK, outside the scope of a public-API-only library.</p>
 */
public enum ChargingType {

    /** Not connected to any power source. */
    NONE(0),

    /** Connected to a standard AC wall charger. */
    AC(BatteryManager.BATTERY_PLUGGED_AC),

    /** Connected via USB (to a PC, hub, or USB power adapter without AC negotiation). */
    USB(BatteryManager.BATTERY_PLUGGED_USB),

    /** Charging wirelessly (Qi or similar). */
    WIRELESS(BatteryManager.BATTERY_PLUGGED_WIRELESS),

    /**
     * Connected via a dock. Only reported on API 33+ ({@code BATTERY_PLUGGED_DOCK});
     * on lower API levels this value is never returned by {@link #fromPlatformValue(int)}.
     */
    DOCK(Build.VERSION.SDK_INT >= 33 ? BatteryManager.BATTERY_PLUGGED_DOCK : -1),

    /** Plugged in, but the source could not be identified. */
    UNKNOWN(-1);

    private final int platformValue;

    ChargingType(int platformValue) {
        this.platformValue = platformValue;
    }

    public int getPlatformValue() {
        return platformValue;
    }

    /**
     * Maps a raw {@code EXTRA_PLUGGED} value to a {@link ChargingType}.
     *
     * @param platformValue 0 if unplugged, otherwise a {@code BATTERY_PLUGGED_*} bitmask value
     * @return the matching charging type
     */
    public static ChargingType fromPlatformValue(int platformValue) {
        if (platformValue == 0) {
            return NONE;
        }
        if (platformValue == BatteryManager.BATTERY_PLUGGED_AC) {
            return AC;
        }
        if (platformValue == BatteryManager.BATTERY_PLUGGED_USB) {
            return USB;
        }
        if (platformValue == BatteryManager.BATTERY_PLUGGED_WIRELESS) {
            return WIRELESS;
        }
        if (Build.VERSION.SDK_INT >= 33 && platformValue == BatteryManager.BATTERY_PLUGGED_DOCK) {
            return DOCK;
        }
        return UNKNOWN;
    }

    public boolean isPluggedIn() {
        return this != NONE;
    }
}
