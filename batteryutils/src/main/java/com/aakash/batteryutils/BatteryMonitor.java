package com.aakash.batteryutils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Provides continuous, live battery monitoring by registering a
 * {@link BroadcastReceiver} for {@link Intent#ACTION_BATTERY_CHANGED} (and,
 * optionally, Battery Saver toggle events). All callbacks are delivered on the
 * main thread.
 *
 * <p>This class is thread-safe with respect to {@link #start()}/{@link #stop()}
 * being called from different threads, though in practice Android's
 * broadcast-receiver APIs are typically used from the main thread.</p>
 *
 * <p>Always call {@link #stop()} (e.g. in {@code onDestroy()}/{@code onStop()})
 * to avoid leaking the registered receiver.</p>
 *
 * <pre>{@code
 * BatteryMonitor monitor = new BatteryMonitor.Builder(context)
 *         .setBatteryChangeListener(info -> updateUi(info))
 *         .listenForPowerSaveModeChanges(true)
 *         .setPowerSaveModeChangeListener(on -> updateSaverBadge(on))
 *         .build();
 * monitor.start();
 * // ...
 * monitor.stop();
 * }</pre>
 */
public final class BatteryMonitor {

    private final Context appContext;
    private final OnBatteryChangeListener batteryChangeListener;
    private final OnPowerSaveModeChangeListener powerSaveModeChangeListener;
    private final boolean listenForPowerSaveModeChanges;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final AtomicBoolean running = new AtomicBoolean(false);

    @Nullable
    private BroadcastReceiver batteryReceiver;
    @Nullable
    private BroadcastReceiver powerSaveReceiver;

    private BatteryMonitor(Builder builder) {
        this.appContext = builder.context.getApplicationContext();
        this.batteryChangeListener = builder.batteryChangeListener;
        this.powerSaveModeChangeListener = builder.powerSaveModeChangeListener;
        this.listenForPowerSaveModeChanges = builder.listenForPowerSaveModeChanges;
    }

    /**
     * Begins live monitoring. Delivers an immediate callback with the current
     * state, then one callback per subsequent system broadcast. Safe to call
     * more than once; subsequent calls while already running are no-ops.
     */
    @MainThread
    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }

        batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                dispatchBatteryInfo();
            }
        };
        IntentFilter batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent sticky = appContext.registerReceiver(batteryReceiver, batteryFilter);
        if (sticky != null) {
            dispatchBatteryInfo();
        }

        if (listenForPowerSaveModeChanges && powerSaveModeChangeListener != null) {
            powerSaveReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    dispatchPowerSaveMode();
                }
            };
            IntentFilter saverFilter = new IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
            appContext.registerReceiver(powerSaveReceiver, saverFilter);
            dispatchPowerSaveMode();
        }

        BatteryLogger.d("BatteryMonitor", "Started");
    }

    /**
     * Stops live monitoring and unregisters all receivers. Safe to call more
     * than once, and safe to call even if {@link #start()} was never called.
     */
    @MainThread
    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }
        if (batteryReceiver != null) {
            safeUnregister(batteryReceiver);
            batteryReceiver = null;
        }
        if (powerSaveReceiver != null) {
            safeUnregister(powerSaveReceiver);
            powerSaveReceiver = null;
        }
        BatteryLogger.d("BatteryMonitor", "Stopped");
    }

    public boolean isRunning() {
        return running.get();
    }

    private void safeUnregister(BroadcastReceiver receiver) {
        try {
            appContext.unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
            // Already unregistered (e.g. by the system) - safe to ignore.
            BatteryLogger.w("BatteryMonitor", "Receiver already unregistered");
        }
    }

    private void dispatchBatteryInfo() {
        BatteryInfo info = BatteryUtils.getBatteryInfo(appContext);
        mainHandler.post(() -> {
            if (batteryChangeListener != null) {
                batteryChangeListener.onBatteryChanged(info);
            }
        });
    }

    private void dispatchPowerSaveMode() {
        boolean on = BatteryUtils.isBatterySaverOn(appContext);
        mainHandler.post(() -> {
            if (powerSaveModeChangeListener != null) {
                powerSaveModeChangeListener.onPowerSaveModeChanged(on);
            }
        });
    }

    // ---------------------------------------------------------------------
    // Builder
    // ---------------------------------------------------------------------

    public static final class Builder {
        private final Context context;
        private OnBatteryChangeListener batteryChangeListener;
        private OnPowerSaveModeChangeListener powerSaveModeChangeListener;
        private boolean listenForPowerSaveModeChanges = false;

        public Builder(@NonNull Context context) {
            this.context = context;
        }

        @NonNull
        public Builder setBatteryChangeListener(@NonNull OnBatteryChangeListener listener) {
            this.batteryChangeListener = listener;
            return this;
        }

        @NonNull
        public Builder setPowerSaveModeChangeListener(@NonNull OnPowerSaveModeChangeListener listener) {
            this.powerSaveModeChangeListener = listener;
            return this;
        }

        @NonNull
        public Builder listenForPowerSaveModeChanges(boolean enabled) {
            this.listenForPowerSaveModeChanges = enabled;
            return this;
        }

        @NonNull
        public BatteryMonitor build() {
            return new BatteryMonitor(this);
        }
    }
}
