package com.aakash.batteryutils;

import androidx.annotation.NonNull;

/**
 * Callback for live battery updates delivered by {@link BatteryMonitor}.
 */
public interface OnBatteryChangeListener {

    /**
     * Called whenever the system broadcasts a battery change
     * ({@link android.content.Intent#ACTION_BATTERY_CHANGED}), and also once
     * immediately upon calling {@link BatteryMonitor#start()} with the current
     * sticky state.
     *
     * @param info the latest full battery snapshot
     */
    void onBatteryChanged(@NonNull BatteryInfo info);
}
