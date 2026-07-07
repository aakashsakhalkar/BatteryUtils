package com.aakash.batteryutils;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * <b>Heuristic analysis only.</b> Android exposes no platform "battery score",
 * "battery grade", or numeric wear percentage — {@link BatteryHealth} is the
 * only health signal the OS provides. Everything in this class is a composite
 * built from that coarse signal plus temperature and voltage, meant for
 * illustrative dashboard UI (e.g. a demo app's "Battery Score" screen), not as
 * a substitute for a real diagnostic tool. Document this clearly wherever these
 * values are surfaced to end users.
 */
public final class BatteryAnalyzer {

    private BatteryAnalyzer() {
    }

    /**
     * A 0-100 heuristic "score", where 100 means {@link BatteryHealth#GOOD} at a
     * safe temperature, and lower scores reflect problematic health or
     * temperature extremes. This is not measured battery wear/degradation —
     * Android provides no public API for that — it is a synthetic composite of
     * the signals this library can actually read.
     */
    public static int calculateHeuristicScore(@NonNull BatteryInfo info) {
        int score = 100;

        switch (info.getHealth()) {
            case GOOD:
                break;
            case COLD:
            case OVERHEAT:
                score -= 30;
                break;
            case OVER_VOLTAGE:
            case UNSPECIFIED_FAILURE:
                score -= 50;
                break;
            case DEAD:
                score = 0;
                break;
            case UNKNOWN:
            default:
                score -= 10;
                break;
        }

        float temp = info.getTemperatureCelsius();
        if (!Float.isNaN(temp)) {
            if (temp > 45f) {
                score -= 20;
            } else if (temp > 40f) {
                score -= 10;
            } else if (temp < 0f) {
                score -= 20;
            }
        }

        return Math.max(0, Math.min(100, score));
    }

    /** Maps {@link #calculateHeuristicScore(BatteryInfo)} to a letter grade. */
    @NonNull
    public static String calculateHeuristicGrade(@NonNull BatteryInfo info) {
        int score = calculateHeuristicScore(info);
        if (score >= 90) return "A";
        if (score >= 75) return "B";
        if (score >= 60) return "C";
        if (score >= 40) return "D";
        return "F";
    }

    /**
     * A simple up/down/flat trend over a list of percentage samples taken in
     * chronological order (e.g. collected from {@link BatteryMonitor}
     * callbacks by the caller). Purely descriptive — not a prediction.
     */
    public enum Trend { RISING, FALLING, FLAT, INSUFFICIENT_DATA }

    @NonNull
    public static Trend calculateTrend(@NonNull List<Integer> chronologicalPercentages) {
        if (chronologicalPercentages.size() < 2) {
            return Trend.INSUFFICIENT_DATA;
        }
        int first = chronologicalPercentages.get(0);
        int last = chronologicalPercentages.get(chronologicalPercentages.size() - 1);
        if (last > first) return Trend.RISING;
        if (last < first) return Trend.FALLING;
        return Trend.FLAT;
    }
}
