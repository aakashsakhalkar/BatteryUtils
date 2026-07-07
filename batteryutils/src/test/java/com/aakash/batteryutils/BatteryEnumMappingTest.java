package com.aakash.batteryutils;

import static org.junit.Assert.assertEquals;

import android.os.BatteryManager;

import org.junit.Test;

public class BatteryEnumMappingTest {

    @Test
    public void batteryStatus_mapsKnownValues() {
        assertEquals(BatteryStatus.CHARGING,
                BatteryStatus.fromPlatformValue(BatteryManager.BATTERY_STATUS_CHARGING));
        assertEquals(BatteryStatus.DISCHARGING,
                BatteryStatus.fromPlatformValue(BatteryManager.BATTERY_STATUS_DISCHARGING));
        assertEquals(BatteryStatus.FULL,
                BatteryStatus.fromPlatformValue(BatteryManager.BATTERY_STATUS_FULL));
    }

    @Test
    public void batteryStatus_unknownRawValue_mapsToUnknown() {
        assertEquals(BatteryStatus.UNKNOWN, BatteryStatus.fromPlatformValue(9999));
    }

    @Test
    public void batteryHealth_mapsKnownValues() {
        assertEquals(BatteryHealth.GOOD,
                BatteryHealth.fromPlatformValue(BatteryManager.BATTERY_HEALTH_GOOD));
        assertEquals(BatteryHealth.OVERHEAT,
                BatteryHealth.fromPlatformValue(BatteryManager.BATTERY_HEALTH_OVERHEAT));
        assertEquals(BatteryHealth.DEAD,
                BatteryHealth.fromPlatformValue(BatteryManager.BATTERY_HEALTH_DEAD));
    }

    @Test
    public void chargingType_mapsKnownValues() {
        assertEquals(ChargingType.NONE, ChargingType.fromPlatformValue(0));
        assertEquals(ChargingType.AC,
                ChargingType.fromPlatformValue(BatteryManager.BATTERY_PLUGGED_AC));
        assertEquals(ChargingType.USB,
                ChargingType.fromPlatformValue(BatteryManager.BATTERY_PLUGGED_USB));
        assertEquals(ChargingType.WIRELESS,
                ChargingType.fromPlatformValue(BatteryManager.BATTERY_PLUGGED_WIRELESS));
    }

    @Test
    public void chargingType_none_isNotPluggedIn() {
        assertEquals(false, ChargingType.NONE.isPluggedIn());
    }

    @Test
    public void chargingType_ac_isPluggedIn() {
        assertEquals(true, ChargingType.AC.isPluggedIn());
    }
}
