package com.aakash.batteryutils;

/**
 * Callback for Battery Saver (power save mode) toggling, delivered by
 * {@link BatteryMonitor} when configured with
 * {@link BatteryMonitor.Builder#listenForPowerSaveModeChanges(boolean)}.
 */
public interface OnPowerSaveModeChangeListener {

    /**
     * @param isPowerSaveModeOn the new Battery Saver state
     */
    void onPowerSaveModeChanged(boolean isPowerSaveModeOn);
}
