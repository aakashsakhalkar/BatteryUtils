package com.aakash.batteryutils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * Main entry point for one-shot (non-live) battery queries.
 *
 * <p>Internally this reads the sticky {@link Intent#ACTION_BATTERY_CHANGED}
 * broadcast (via {@code registerReceiver(null, filter)}, which returns the
 * last broadcast immediately without registering a live receiver) combined
 * with {@link BatteryManager} system-service properties for values not present
 * on the intent.</p>
 *
 * <p>For continuous/live updates, use {@link BatteryMonitor} instead of polling
 * this class in a loop.</p>
 */
public final class BatteryUtils {

    private static final String TAG = "BatteryUtils";

    private BatteryUtils() {
    }

    /**
     * Builds a full {@link BatteryInfo} snapshot from the current system state.
     *
     * @param context any context; applicationContext is used internally
     * @return a populated, immutable snapshot
     */
    @NonNull
    public static BatteryInfo getBatteryInfo(@NonNull Context context) {
        Context appContext = context.getApplicationContext();
        Intent batteryIntent = getStickyBatteryIntent(appContext);
        BatteryManager batteryManager =
                (BatteryManager) appContext.getSystemService(Context.BATTERY_SERVICE);

        BatteryInfo.Builder builder = new BatteryInfo.Builder();

        int level = -1;
        int scale = -1;
        if (batteryIntent != null) {
            level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            int statusRaw = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS,
                    BatteryManager.BATTERY_STATUS_UNKNOWN);
            int healthRaw = batteryIntent.getIntExtra(BatteryManager.EXTRA_HEALTH,
                    BatteryManager.BATTERY_HEALTH_UNKNOWN);
            int pluggedRaw = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
            boolean present = batteryIntent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, true);
            String technology = batteryIntent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
            int temperatureTenths = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
            int voltage = batteryIntent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
            BatteryStatus status = BatteryStatus.fromPlatformValue(statusRaw);
            ChargingType chargingType = ChargingType.fromPlatformValue(pluggedRaw);

            builder.status(status)
                    .health(BatteryHealth.fromPlatformValue(healthRaw))
                    .chargingType(chargingType)
                    .charging(status.isCharging() || chargingType.isPluggedIn())
                    .present(present)
                    .technology(technology)
                    .temperatureCelsius(temperatureTenths / 10f)
                    .voltageMillivolts(voltage);

            if (BatteryCapabilities.isBatteryLowFlagSupported()) {
                builder.batteryLow(batteryIntent.getBooleanExtra(
                        BatteryManager.EXTRA_BATTERY_LOW, false));
            }

            if (BatteryCapabilities.isCycleCountSupported()) {
                int cycles = batteryIntent.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT,
                        BatteryInfo.UNAVAILABLE_INT);
                builder.cycleCount(cycles);
            }
        }

        if (scale > 0 && level >= 0) {
            builder.level(level).scale(scale).percentage(Math.round(level * 100f / scale));
        } else if (batteryManager != null) {
            // Fallback: BATTERY_PROPERTY_CAPACITY directly reports 0-100.
            int capacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            builder.percentage(capacity).level(capacity).scale(100);
        }

        if (batteryManager != null) {
            builder.chargeCounterMicroAh(safeIntProperty(batteryManager,
                    BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER));
            builder.currentNowMicroAmps(safeIntProperty(batteryManager,
                    BatteryManager.BATTERY_PROPERTY_CURRENT_NOW));
            builder.currentAverageMicroAmps(safeIntProperty(batteryManager,
                    BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE));

            long energy = batteryManager.getLongProperty(
                    BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER);
            builder.energyCounterNanoWattHours(energy == Long.MIN_VALUE
                    ? BatteryInfo.UNAVAILABLE_LONG : energy);

            if (BatteryCapabilities.isChargeTimeRemainingSupported()) {
                builder.chargeTimeRemainingMillis(getChargeTimeRemaining(batteryManager));
            }
        }

        return builder.build();
    }

    /** Convenience: battery percentage 0-100. */
    public static int getBatteryPercentage(@NonNull Context context) {
        return getBatteryInfo(context).getPercentage();
    }

    /** Convenience: whether the battery is currently charging (any source). */
    public static boolean isCharging(@NonNull Context context) {
        Context appContext = context.getApplicationContext();
        if (BatteryCapabilities.isChargingQuerySupported()) {
            BatteryManager bm = (BatteryManager) appContext.getSystemService(Context.BATTERY_SERVICE);
            if (bm != null) {
                return bm.isCharging();
            }
        }
        return getBatteryInfo(context).isCharging();
    }

    /** Convenience: current charging source (AC/USB/WIRELESS/DOCK/NONE/UNKNOWN). */
    @NonNull
    public static ChargingType getChargingType(@NonNull Context context) {
        return getBatteryInfo(context).getChargingType();
    }

    /** Convenience: current battery health. */
    @NonNull
    public static BatteryHealth getBatteryHealth(@NonNull Context context) {
        return getBatteryInfo(context).getHealth();
    }

    /** Convenience: current battery status. */
    @NonNull
    public static BatteryStatus getBatteryStatus(@NonNull Context context) {
        return getBatteryInfo(context).getStatus();
    }

    /** Whether Battery Saver mode is currently on. */
    public static boolean isBatterySaverOn(@NonNull Context context) {
        PowerManager pm = (PowerManager) context.getApplicationContext()
                .getSystemService(Context.POWER_SERVICE);
        return pm != null && pm.isPowerSaveMode();
    }

    /**
     * Whether the device is currently in Doze (idle) mode. Requires API 23+;
     * returns {@code false} on lower API levels since Doze does not exist there.
     */
    public static boolean isDeviceIdle(@NonNull Context context) {
        if (!BatteryCapabilities.isDeviceIdleQuerySupported()) {
            return false;
        }
        PowerManager pm = (PowerManager) context.getApplicationContext()
                .getSystemService(Context.POWER_SERVICE);
        return pm != null && isDeviceIdleModeCompat(pm);
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private static boolean isDeviceIdleModeCompat(PowerManager pm) {
        return pm.isDeviceIdleMode();
    }

    /**
     * Estimated milliseconds until fully charged, or {@link BatteryInfo#UNAVAILABLE_LONG}
     * if unsupported (API &lt; 28), not charging, or unknown.
     */
    public static long getChargeTimeRemainingMillis(@NonNull Context context) {
        if (!BatteryCapabilities.isChargeTimeRemainingSupported()) {
            return BatteryInfo.UNAVAILABLE_LONG;
        }
        BatteryManager bm = (BatteryManager) context.getApplicationContext()
                .getSystemService(Context.BATTERY_SERVICE);
        if (bm == null) {
            return BatteryInfo.UNAVAILABLE_LONG;
        }
        return getChargeTimeRemaining(bm);
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private static long getChargeTimeRemaining(BatteryManager bm) {
        long value = bm.computeChargeTimeRemaining();
        return value < 0 ? BatteryInfo.UNAVAILABLE_LONG : value;
    }

    @Nullable
    private static Intent getStickyBatteryIntent(Context appContext) {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        // Passing a null receiver just reads the last sticky broadcast without
        // registering a live listener — this is the documented pattern for a
        // one-shot battery read.
        return appContext.registerReceiver(null, filter);
    }

    private static int safeIntProperty(BatteryManager bm, int propertyId) {
        int value = bm.getIntProperty(propertyId);
        // BatteryManager returns Integer.MIN_VALUE when a property isn't supported,
        // which conveniently matches our UNAVAILABLE_INT sentinel already.
        return value;
    }
}