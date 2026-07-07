package com.aakash.batteryutils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Map;

/**
 * Uses Robolectric because {@link BatteryInfo#toBundle()} and
 * {@link BatteryInfo#toJson()} rely on real (non-stubbed) Android/JSON
 * framework classes to actually execute, rather than throwing "Stub!".
 */
@RunWith(RobolectricTestRunner.class)
public class BatteryInfoTest {

    private BatteryInfo buildSample() {
        return new BatteryInfo.Builder()
                .level(75)
                .scale(100)
                .percentage(75)
                .status(BatteryStatus.CHARGING)
                .health(BatteryHealth.GOOD)
                .chargingType(ChargingType.USB)
                .charging(true)
                .present(true)
                .technology("Li-ion")
                .temperatureCelsius(28.5f)
                .voltageMillivolts(4200)
                .currentNowMicroAmps(500_000)
                .build();
    }

    @Test
    public void builder_populatesAllFields() {
        BatteryInfo info = buildSample();
        assertEquals(75, info.getPercentage());
        assertEquals(BatteryStatus.CHARGING, info.getStatus());
        assertEquals(BatteryHealth.GOOD, info.getHealth());
        assertEquals(ChargingType.USB, info.getChargingType());
        assertTrue(info.isCharging());
        assertEquals("Li-ion", info.getTechnology());
        assertEquals(28.5f, info.getTemperatureCelsius(), 0.01f);
    }

    @Test
    public void unavailableSentinels_defaultCorrectly() {
        BatteryInfo info = new BatteryInfo.Builder().build();
        assertFalse(info.isChargeCounterAvailable());
        assertFalse(info.isEnergyCounterAvailable());
        assertFalse(info.isCycleCountAvailable());
        assertFalse(info.isChargeTimeRemainingAvailable());
    }

    @Test
    public void toBundle_roundTripsCoreFields() {
        Bundle bundle = buildSample().toBundle();
        assertEquals(75, bundle.getInt("percentage"));
        assertEquals("CHARGING", bundle.getString("status"));
        assertEquals("USB", bundle.getString("chargingType"));
        assertTrue(bundle.getBoolean("charging"));
    }

    @Test
    public void toMap_roundTripsCoreFields() {
        Map<String, Object> map = buildSample().toMap();
        assertEquals(75, map.get("percentage"));
        assertEquals("CHARGING", map.get("status"));
        assertEquals(true, map.get("charging"));
    }

    @Test
    public void toJson_roundTripsCoreFields() throws JSONException {
        JSONObject json = buildSample().toJson();
        assertEquals(75, json.getInt("percentage"));
        assertEquals("CHARGING", json.getString("status"));
        assertEquals("USB", json.getString("chargingType"));
        assertTrue(json.getBoolean("charging"));
    }

    @Test
    public void getPowerWatts_computesFromVoltageAndCurrent() {
        BatteryInfo info = buildSample();
        // 4200 mV * 0.5 A = 2.1 W
        assertEquals(2.1f, info.getPowerWatts(), 0.01f);
    }
}
