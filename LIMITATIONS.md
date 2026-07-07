# Platform Limitations

BatteryUtils only uses **public, documented Android APIs**. No hidden
framework fields, no reflection into non-SDK interfaces, no OEM-private
broadcasts. That means a few commonly-requested "battery" features from the
original spec have **no real implementation on stock Android**, and rather
than fake them, this library documents them explicitly here and on the
relevant class/method Javadoc.

| Requested feature | Status | Why |
|---|---|---|
| Reverse charging detection | **Not implemented** | No public API on any Android version. Some OEMs (e.g. Samsung) expose this privately, but there's no AOSP-standard broadcast or property for it. See `ChargingType`. |
| Design capacity / Full-charge capacity (mAh) | **Not implemented** | These exist only as hidden fuel-gauge fields, not part of the public `BatteryManager` API, and are blocked by non-SDK interface restrictions on modern API levels. |
| Battery "wear" as a measured percentage | **Not implemented** | Follows directly from the capacity limitation above — wear is normally computed as full-charge-capacity ÷ design-capacity, and neither value is public. |
| Adaptive Battery global on/off query | **Not implemented** | No public API reports the device-wide toggle. The closest related public API, `UsageStatsManager.getAppStandbyBucket()`, reports your own app's standby bucket, not a global flag — a different thing. |
| "Fast charging" / "slow charging" as a boolean | **Heuristic only** | No public boolean/enum exists. `BatteryEstimator.classifyChargingSpeed()` approximates this from `BATTERY_PROPERTY_CURRENT_NOW` against configurable thresholds. Clearly labeled as heuristic in Javadoc. |
| Battery cycle count | **Supported, API 34+ only** | `EXTRA_CYCLE_COUNT` was added in Android 14. On lower API levels, `BatteryInfo.getCycleCount()` returns the `UNAVAILABLE_INT` sentinel. |
| Charging time remaining | **Supported, API 28+ only** | Wraps `BatteryManager.computeChargeTimeRemaining()` directly. |
| Discharge time prediction | **Supported, API 31+ only** | Wraps `PowerManager.getBatteryDischargePrediction()` / `isBatteryDischargePredictionPersonalized()` — the same model used by system Settings. On lower API levels, `BatteryEstimator.estimateDischargeTimeMillis()` offers a simple linear-extrapolation heuristic instead, clearly labeled as such. |
| "Battery score" / "Battery grade" | **Heuristic only** | Android has no such metric. `BatteryAnalyzer` builds an illustrative composite from `BatteryHealth` + temperature, intended for demo/dashboard UI, not as a real diagnostic. |
| Dock as a distinct charging source | **Supported, API 33+ only** | `BATTERY_PLUGGED_DOCK` was added in Android 13. On lower API levels it's indistinguishable from other sources. |

Every method above is annotated in Javadoc as either a direct platform wrapper
or an explicitly-labeled heuristic, and `BatteryCapabilities` lets calling code
check support at runtime before showing UI for a given feature.
