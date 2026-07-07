package com.aakash.batteryutilui;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.aakash.batteryutilui.databinding.ActivityMainBinding;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private final List<long[]> percentageSamples = new ArrayList<>();
    private static final int MAX_SAMPLES = 20;

    private ActivityMainBinding binding;
    private BatteryMonitor batteryMonitor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_main);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        // Show a one-shot reading immediately, before live monitoring kicks in.
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        renderBatteryInfo(BatteryUtils.getBatteryInfo(this));
        renderCapabilities();

        binding.btnExportJson.setOnClickListener(v -> exportJson());
        binding.btnExportBundle.setOnClickListener(v -> exportBundle());
        binding.btnExportMap.setOnClickListener(v -> exportMap());

        batteryMonitor = new BatteryMonitor.Builder(this)
                .setBatteryChangeListener(this::renderBatteryInfo)
                .listenForPowerSaveModeChanges(true)
                .setPowerSaveModeChangeListener(this::renderBatterySaver)
                .build();
    }@Override
    public void onStart() {
        super.onStart();
        if (batteryMonitor != null) {
            batteryMonitor.start();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (batteryMonitor != null) {
            batteryMonitor.stop();
        }
    }

    private void renderBatteryInfo(@NonNull BatteryInfo info) {
        if (binding == null) {
            return;
        }

        recordSample(info.getPercentage());

        binding.tvLastUpdated.setText(getString(R.string.last_updated_format,
                DateFormat.format("HH:mm:ss", new Date(info.getTimestampMillis()))));

        // ----- Core -----
        binding.tvPercentage.setText(getString(R.string.field_percentage,
                BatteryFormatter.formatPercentage(info.getPercentage())));
        binding.tvLevelScale.setText(getString(R.string.field_level_scale,
                info.getLevel(), info.getScale()));
        binding.tvStatus.setText(getString(R.string.field_status, info.getStatus().name()));
        binding.tvHealth.setText(getString(R.string.field_health, info.getHealth().name()));
        binding.tvChargingType.setText(getString(R.string.field_charging_type,
                info.getChargingType().name()));
        binding.tvIsCharging.setText(getString(R.string.field_is_charging, info.isCharging()));
        binding.tvPresent.setText(getString(R.string.field_present, info.isPresent()));
        binding.tvBatteryLow.setText(getString(R.string.field_battery_low, info.isBatteryLow()));
        binding.tvTechnology.setText(getString(R.string.field_technology,
                info.getTechnology() != null ? info.getTechnology() : "unknown"));

        // ----- Temperature / voltage -----
        binding.tvTempC.setText(BatteryFormatter.formatTemperatureCelsius(info.getTemperatureCelsius()));
        binding.tvTempF.setText(BatteryFormatter.formatTemperatureFahrenheit(info.getTemperatureCelsius()));
        binding.tvTempK.setText(BatteryFormatter.formatTemperatureKelvin(info.getTemperatureCelsius()));
        binding.tvVoltage.setText(BatteryFormatter.formatVoltage(info.getVoltageMillivolts()));

        // ----- Current / power / energy -----
        binding.tvCurrentNow.setText(getString(R.string.field_current_now,
                BatteryFormatter.formatCurrentMicroAmps(info.getCurrentNowMicroAmps())));
        binding.tvCurrentAvg.setText(getString(R.string.field_current_avg,
                BatteryFormatter.formatCurrentMicroAmps(info.getCurrentAverageMicroAmps())));
        binding.tvPower.setText(getString(R.string.field_power,
                BatteryFormatter.formatPowerWatts(info.getPowerWatts())));
        binding.tvChargeCounter.setText(getString(R.string.field_charge_counter,
                info.isChargeCounterAvailable()
                        ? String.format("%.1f mAh", info.getChargeCounterMicroAh() / 1000f)
                        : "N/A"));
        binding.tvEnergyCounter.setText(getString(R.string.field_energy_counter,
                info.isEnergyCounterAvailable()
                        ? String.format("%.1f Wh", info.getEnergyCounterNanoWattHours() / 1_000_000_000f)
                        : "N/A"));
        binding.tvCycleCount.setText(getString(R.string.field_cycle_count,
                info.isCycleCountAvailable()
                        ? String.valueOf(info.getCycleCount())
                        : "N/A (needs API 34+)"));

        // ----- Time estimates -----
        binding.tvChargeTimeRemaining.setText(getString(R.string.field_charge_time_remaining,
                BatteryCapabilities.isChargeTimeRemainingSupported()
                        ? BatteryFormatter.formatDurationMillis(info.getChargeTimeRemainingMillis())
                        : "N/A (needs API 28+)"));

        BatteryEstimator.DischargePrediction prediction =
                BatteryEstimator.getDischargePrediction(this);
        if (prediction != null) {
            binding.tvDischargePrediction.setText(getString(R.string.field_discharge_prediction,
                    BatteryFormatter.formatDurationMillis(prediction.getRemainingMillis())
                            + (prediction.isPersonalized() ? " (personalized)" : "")));
        } else {
            binding.tvDischargePrediction.setText(getString(R.string.field_discharge_prediction,
                    "N/A (needs API 31+, or currently charging)"));
        }

        binding.tvDischargeHeuristic.setText(getString(R.string.field_discharge_heuristic,
                BatteryFormatter.formatDurationMillis(estimateHeuristicDischargeMillis())));

        binding.tvChargingSpeed.setText(getString(R.string.field_charging_speed,
                BatteryEstimator.classifyChargingSpeed(this).name()));

        // ----- System state -----
        binding.tvBatterySaver.setText(getString(R.string.field_battery_saver,
                BatteryUtils.isBatterySaverOn(this) ? "on" : "off"));
        binding.tvDeviceIdle.setText(getString(R.string.field_device_idle,
                BatteryUtils.isDeviceIdle(this)));

        // ----- Heuristic analysis -----
        int score = BatteryAnalyzer.calculateHeuristicScore(info);
        String grade = BatteryAnalyzer.calculateHeuristicGrade(info);
        binding.tvScore.setText(getString(R.string.field_score, score));
        binding.tvGrade.setText(getString(R.string.field_grade, grade));
        binding.tvTrend.setText(getString(R.string.field_trend, calculateTrend().name()));
    }

    private void renderBatterySaver(boolean isOn) {
        if (binding == null) {
            return;
        }
        binding.tvBatterySaver.setText(getString(R.string.field_battery_saver, isOn ? "on" : "off"));
    }

    private void renderCapabilities() {
        binding.tvCapabilities.setText(BatteryCapabilities.describeCapabilities(this));
    }

    // ---------------------------------------------------------------------
    // Trend / heuristic discharge estimate helpers
    // ---------------------------------------------------------------------

    private void recordSample(int percentage) {
        percentageSamples.add(new long[]{System.currentTimeMillis(), percentage});
        while (percentageSamples.size() > MAX_SAMPLES) {
            percentageSamples.remove(0);
        }
    }

    @NonNull
    private BatteryAnalyzer.Trend calculateTrend() {
        List<Integer> chronological = new ArrayList<>();
        for (long[] sample : percentageSamples) {
            chronological.add((int) sample[1]);
        }
        return BatteryAnalyzer.calculateTrend(chronological);
    }

    private long estimateHeuristicDischargeMillis() {
        if (percentageSamples.size() < 2) {
            return -1;
        }
        long[] earliest = percentageSamples.get(0);
        long[] latest = percentageSamples.get(percentageSamples.size() - 1);
        long elapsed = latest[0] - earliest[0];
        return BatteryEstimator.estimateDischargeTimeMillis(
                (int) latest[1], (int) earliest[1], elapsed);
    }

    // ---------------------------------------------------------------------
    // Export demos
    // ---------------------------------------------------------------------

    private void exportJson() {
        JSONObject json = BatteryUtils.getBatteryInfo(this).toJson();
        String text = json.toString();
        binding.tvExportOutput.setText(text);
        Toast.makeText(this, "Exported as JSON", Toast.LENGTH_SHORT).show();
    }

    private void exportBundle() {
        Bundle bundle = BatteryUtils.getBatteryInfo(this).toBundle();
        StringBuilder sb = new StringBuilder();
        for (String key : bundle.keySet()) {
            sb.append(key).append("=").append(bundle.get(key)).append("\n");
        }
        binding.tvExportOutput.setText(sb.toString().trim());
        Toast.makeText(this, "Exported as Bundle", Toast.LENGTH_SHORT).show();
    }

    private void exportMap() {
        Map<String, Object> map = BatteryUtils.getBatteryInfo(this).toMap();
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }
        binding.tvExportOutput.setText(sb.toString().trim());
        Toast.makeText(this, "Exported as Map", Toast.LENGTH_SHORT).show();
    }
}