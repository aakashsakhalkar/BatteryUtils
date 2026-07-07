package com.aakash.batteryutils;

import android.util.Log;

import androidx.annotation.Nullable;

/**
 * Minimal internal logging wrapper so the library has a single place to control
 * log output. Disabled by default; enable via {@link #setEnabled(boolean)} (e.g.
 * only in debug builds of your app).
 */
public final class BatteryLogger {

    private static volatile boolean enabled = false;

    private BatteryLogger() {
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void d(String tag, String message) {
        if (enabled) {
            Log.d(tag, message);
        }
    }

    public static void w(String tag, String message) {
        if (enabled) {
            Log.w(tag, message);
        }
    }

    public static void e(String tag, String message, @Nullable Throwable t) {
        if (enabled) {
            if (t != null) {
                Log.e(tag, message, t);
            } else {
                Log.e(tag, message);
            }
        }
    }
}
