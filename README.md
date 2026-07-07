<div align="center">

# 🔋 BatteryUtils

**The most complete battery utility library for Android — built entirely on public APIs.**

Percentage · Health · Charging source · Temperature · Voltage · Current · Energy · Cycle count · Live monitoring · Export

[![](https://jitpack.io/v/aakashsakhalkar/BatteryUtils.svg)](https://jitpack.io/#aakashsakhalkar/BatteryUtils)
![Min SDK](https://img.shields.io/badge/minSdk-21-brightgreen)
![Language](https://img.shields.io/badge/language-Java-orange)
![License](https://img.shields.io/badge/license-MIT-blue)

</div>

---

## Table of Contents

- [Why BatteryUtils?](#why-batteryutils)
- [Features](#features)
- [Install](#install)
- [Quick Start](#quick-start)
- [Full API Guide](#full-api-guide)
  - [BatteryInfo](#batteryinfo)
  - [BatteryUtils (one-shot facade)](#batteryutils-one-shot-facade)
  - [BatteryMonitor (live updates)](#batterymonitor-live-updates)
  - [Enums](#enums)
  - [BatteryCapabilities](#batterycapabilities)
  - [BatteryFormatter](#batteryformatter)
  - [BatteryCalculator](#batterycalculator)
  - [BatteryEstimator](#batteryestimator)
  - [BatteryAnalyzer](#batteryanalyzer)
  - [Export](#export)
- [Platform Limitations (read this)](#platform-limitations-read-this)
- [Project Structure](#project-structure)
- [Testing](#testing)
- [Contributing](#contributing)
- [License](#license)

---

## Why BatteryUtils?

Reading battery state on Android means juggling sticky broadcasts, `BatteryManager`
properties, and API-level landmines. BatteryUtils wraps all of it into one clean,
type-safe, well-documented API.

- **Java only** — no Kotlin, no unnecessary transitive dependencies
- **AndroidX**, `minSdk 21`
- **No reflection, no hidden framework fields, no OEM-private hooks** — every
  value is backed by a real, public Android API
- Every gap in what Android exposes is **documented honestly** instead of faked
  (see [Platform Limitations](#platform-limitations-read-this))
- Thread-safe, immutable data model
- Unit tested (JUnit + Robolectric)

## Features

| | |
|---|---|
| ✅ Battery percentage, level, scale | ✅ Live monitoring via `BatteryMonitor` |
| ✅ Charging status & health | ✅ Battery Saver / Doze detection |
| ✅ Charging source (AC / USB / Wireless / Dock) | ✅ Charge time remaining (API 28+) |
| ✅ Temperature in °C / °F / K | ✅ Discharge prediction (API 31+) |
| ✅ Voltage, current now/avg, power draw | ✅ Cycle count (API 34+) |
| ✅ Charge & energy counters | ✅ Export to JSON / Bundle / Map |
| ✅ Heuristic charging-speed classification | ✅ Heuristic battery score & grade |

## Install

Add JitPack to your root `settings.gradle`:

```gradle
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

Add the dependency to your module `build.gradle`:

```gradle
dependencies {
    implementation 'com.github.aakashsakhalkar:BatteryUtils:1.0.0'
}
```

## Quick Start

```java
// One-shot read
BatteryInfo info = BatteryUtils.getBatteryInfo(context);
int percentage = info.getPercentage();
boolean charging = info.isCharging();
ChargingType source = info.getChargingType();
BatteryHealth health = info.getHealth();
float tempC = info.getTemperatureCelsius();
```

```java
// Live monitoring
BatteryMonitor monitor = new BatteryMonitor.Builder(context)
        .setBatteryChangeListener(info -> updateUi(info))
        .listenForPowerSaveModeChanges(true)
        .setPowerSaveModeChangeListener(isOn -> updateSaverBadge(isOn))
        .build();

monitor.start(); // e.g. in onStart()
monitor.stop();  // e.g. in onStop()
```

---

## Full API Guide

### BatteryInfo

An immutable snapshot of every battery property this library could read at a
point in time. Obtain one from `BatteryUtils.getBatteryInfo(context)` or a
`BatteryMonitor` callback.

```java
BatteryInfo info = BatteryUtils.getBatteryInfo(context);

info.getLevel();                    // raw level
info.getScale();                    // raw scale (usually 100)
info.getPercentage();                // 0-100
info.getStatus();                    // BatteryStatus
info.getHealth();                    // BatteryHealth
info.getChargingType();               // ChargingType
info.isCharging();
info.isPresent();
info.isBatteryLow();                 // API 28+, false otherwise
info.getTechnology();                 // e.g. "Li-ion"

info.getTemperatureCelsius();
info.getTemperatureFahrenheit();
info.getTemperatureKelvin();
info.getVoltageMillivolts();
info.getVoltageVolts();

info.getChargeCounterMicroAh();
info.getCurrentNowMicroAmps();
info.getCurrentAverageMicroAmps();
info.getEnergyCounterNanoWattHours();
info.getCycleCount();                 // API 34+, sentinel otherwise
info.getPowerWatts();                 // derived from voltage * current
info.getChargeTimeRemainingMillis();   // API 28+

// Availability checks - always check before trusting a value that
// may not be supported on this device/API level
info.isChargeCounterAvailable();
info.isCurrentNowAvailable();
info.isCurrentAverageAvailable();
info.isEnergyCounterAvailable();
info.isCycleCountAvailable();
info.isChargeTimeRemainingAvailable();
```

Fields the platform didn't report use documented sentinel values
(`BatteryInfo.UNAVAILABLE_INT`, `BatteryInfo.UNAVAILABLE_LONG`, `Float.NaN`)
rather than 0 or -1, so "not reported" is never confused with a real reading
of zero.

### BatteryUtils (one-shot facade)

```java
BatteryUtils.getBatteryInfo(context);          // full snapshot
BatteryUtils.getBatteryPercentage(context);
BatteryUtils.isCharging(context);
BatteryUtils.getChargingType(context);
BatteryUtils.getBatteryHealth(context);
BatteryUtils.getBatteryStatus(context);
BatteryUtils.isBatterySaverOn(context);
BatteryUtils.isDeviceIdle(context);            // Doze, API 23+
BatteryUtils.getChargeTimeRemainingMillis(context); // API 28+
```

### BatteryMonitor (live updates)

Registers a `BroadcastReceiver` for `ACTION_BATTERY_CHANGED` (and, optionally,
Battery Saver toggles), delivering callbacks on the main thread.

```java
BatteryMonitor monitor = new BatteryMonitor.Builder(context)
        .setBatteryChangeListener(info -> { /* ... */ })
        .listenForPowerSaveModeChanges(true)
        .setPowerSaveModeChangeListener(isOn -> { /* ... */ })
        .build();

monitor.start();
monitor.isRunning();
monitor.stop(); // always call this (onStop/onDestroy) to avoid leaking the receiver
```

### Enums

| Enum | Values |
|---|---|
| `BatteryStatus` | `UNKNOWN`, `CHARGING`, `DISCHARGING`, `NOT_CHARGING`, `FULL` |
| `BatteryHealth` | `UNKNOWN`, `GOOD`, `OVERHEAT`, `DEAD`, `OVER_VOLTAGE`, `UNSPECIFIED_FAILURE`, `COLD` |
| `ChargingType` | `NONE`, `AC`, `USB`, `WIRELESS`, `DOCK` (API 33+), `UNKNOWN` |
| `ChargingSpeed` | `NOT_CHARGING`, `SLOW`, `NORMAL`, `FAST`, `UNKNOWN` (heuristic, see below) |

Each has a `fromPlatformValue(int)` factory and maps directly to real
`BatteryManager` constants — no invented values.

### BatteryCapabilities

Checks what's actually supported on the current device/API level, so you can
hide UI for unsupported features instead of showing a fabricated value:

```java
BatteryCapabilities.isChargingQuerySupported();          // API 23+
BatteryCapabilities.isChargeTimeRemainingSupported();     // API 28+
BatteryCapabilities.isBatteryLowFlagSupported();          // API 28+
BatteryCapabilities.isCycleCountSupported();              // API 34+
BatteryCapabilities.isDockChargingSourceSupported();      // API 33+
BatteryCapabilities.isDeviceIdleQuerySupported();         // API 23+
BatteryCapabilities.isDischargePredictionSupported();     // API 31+
BatteryCapabilities.isCurrentNowApiSupported();
BatteryCapabilities.isBatteryPresenceQuerySupported();
BatteryCapabilities.isBatterySaverQuerySupported();

// Always false - no public API exists for these (see Limitations)
BatteryCapabilities.isDesignOrFullChargeCapacitySupported();
BatteryCapabilities.isReverseChargingDetectionSupported();
BatteryCapabilities.isAdaptiveBatteryGlobalQuerySupported();

BatteryCapabilities.describeCapabilities(context); // human-readable dump
```

### BatteryFormatter

Pure, stateless, unit-testable formatting helpers:

```java
BatteryFormatter.celsiusToFahrenheit(28.5f);
BatteryFormatter.celsiusToKelvin(28.5f);
BatteryFormatter.formatTemperatureCelsius(28.5f);     // "28.5°C"
BatteryFormatter.formatTemperatureFahrenheit(28.5f);  // "83.3°F"
BatteryFormatter.formatTemperatureKelvin(28.5f);      // "301.7K"
BatteryFormatter.formatVoltage(4200);                 // "4.20 V"
BatteryFormatter.formatCurrentMicroAmps(500000);       // "500 mA"
BatteryFormatter.formatPowerWatts(2.1f);               // "2.10 W"
BatteryFormatter.formatDurationMillis(5400000);        // "1h 30m"
BatteryFormatter.formatPercentage(82);                 // "82%"
```

### BatteryCalculator

Pure numeric calculations with no Android dependency:

```java
BatteryCalculator.calculatePercentage(level, scale);
BatteryCalculator.calculatePowerWatts(voltageMillivolts, currentMicroAmps);
BatteryCalculator.microAhToMilliAh(microAh);
BatteryCalculator.nanoWattHoursToWattHours(nanoWattHours);
BatteryCalculator.calculateRatePercentPerHour(fromPercent, toPercent, elapsedMillis);
```

### BatteryEstimator

Two categories, clearly separated:

**Platform-reported** (thin wrappers around a real Android API):
```java
BatteryEstimator.getChargeTimeRemainingMillis(context);  // API 28+
BatteryEstimator.getDischargePrediction(context);        // API 31+, same model as Settings > Battery
```

**Heuristic** (approximated by this library, clearly labeled — no public API exists):
```java
BatteryEstimator.classifyChargingSpeed(context);                 // ChargingSpeed
BatteryEstimator.classifyChargingSpeed(context, slowMa, fastMa); // custom thresholds
BatteryEstimator.estimateDischargeTimeMillis(currentPct, earlierPct, elapsedMillis);
```

### BatteryAnalyzer

**Heuristic only** — Android has no platform "battery score" or "grade".
Useful for illustrative dashboard UI:

```java
BatteryAnalyzer.calculateHeuristicScore(info);   // 0-100
BatteryAnalyzer.calculateHeuristicGrade(info);   // "A".."F"
BatteryAnalyzer.calculateTrend(percentageSamples); // RISING / FALLING / FLAT / INSUFFICIENT_DATA
```

### Export

```java
JSONObject json = info.toJson();
Bundle bundle = info.toBundle();
Map<String, Object> map = info.toMap();
```

---

## Platform Limitations (read this)

BatteryUtils only uses **public, documented Android APIs**. No hidden
framework fields, no reflection into non-SDK interfaces, no OEM-private
broadcasts. A few commonly-requested "battery" features have **no real
implementation on stock Android** — rather than fake them, here's the honest
account:

| Requested feature | Status | Why |
|---|---|---|
| Reverse charging detection | **Not implemented** | No public API on any Android version. Some OEMs (e.g. Samsung) expose this privately, but there's no AOSP-standard broadcast or property for it. |
| Design capacity / Full-charge capacity (mAh) | **Not implemented** | Exist only as hidden fuel-gauge fields, not part of the public `BatteryManager` API, blocked by non-SDK interface restrictions on modern API levels. |
| Battery "wear" as a measured percentage | **Not implemented** | Follows from the capacity limitation above — wear is normally full-charge-capacity ÷ design-capacity, and neither value is public. |
| Adaptive Battery global on/off query | **Not implemented** | No public API reports the device-wide toggle. The closest related API, `UsageStatsManager.getAppStandbyBucket()`, reports your own app's standby bucket, not a global flag. |
| Max charging current / voltage | **Not implemented** | `EXTRA_MAX_CHARGING_CURRENT`/`EXTRA_MAX_CHARGING_VOLTAGE` exist in AOSP source but are marked `@hide` — not part of the public SDK. |
| "Fast charging" / "slow charging" as a boolean | **Heuristic only** | No public boolean/enum exists. `BatteryEstimator.classifyChargingSpeed()` approximates this from `BATTERY_PROPERTY_CURRENT_NOW` against configurable thresholds. |
| Battery cycle count | **Supported, API 34+ only** | `EXTRA_CYCLE_COUNT` was added in Android 14. |
| Charging time remaining | **Supported, API 28+ only** | Wraps `BatteryManager.computeChargeTimeRemaining()` directly. |
| Discharge time prediction | **Supported, API 31+ only** | Wraps `PowerManager.getBatteryDischargePrediction()` — the same model used by system Settings. Below API 31, `BatteryEstimator.estimateDischargeTimeMillis()` offers a linear-extrapolation heuristic instead. |
| "Battery score" / "Battery grade" | **Heuristic only** | No such platform metric exists. `BatteryAnalyzer` builds an illustrative composite from `BatteryHealth` + temperature, meant for demo/dashboard UI, not real diagnostics. |
| Dock as a distinct charging source | **Supported, API 33+ only** | `BATTERY_PLUGGED_DOCK` was added in Android 13. |

Every method above is annotated in Javadoc as either a direct platform
wrapper or an explicitly-labeled heuristic. `BatteryCapabilities` lets calling
code check support at runtime before showing UI for a given feature.

## Project Structure

```
BatteryUtils/
├── batteryutils/                 # the library module
│   └── src/main/java/com/aakash/batteryutils/
│       ├── BatteryInfo.java
│       ├── BatteryUtils.java
│       ├── BatteryMonitor.java
│       ├── BatteryStatus.java
│       ├── BatteryHealth.java
│       ├── ChargingType.java
│       ├── ChargingSpeed.java
│       ├── BatteryCapabilities.java
│       ├── BatteryFormatter.java
│       ├── BatteryCalculator.java
│       ├── BatteryEstimator.java
│       ├── BatteryAnalyzer.java
│       ├── BatteryLogger.java
│       ├── OnBatteryChangeListener.java
│       └── OnPowerSaveModeChangeListener.java
├── app/                          # demo app (Material 3)
└── build.gradle / settings.gradle
```

## Testing

```bash
./gradlew :batteryutils:test
```

Pure-Java logic (`BatteryCalculator`, `BatteryFormatter`, enum mappings) is
tested with plain JUnit; anything touching `Bundle`/JSON uses Robolectric.

## Contributing

Issues and PRs are welcome. Please keep contributions consistent with the
project's core principle: **public APIs only, no reflection, no hidden
fields** — if a feature can't be built that way, document the limitation
instead of working around it.

## License

Released under the [MIT License](LICENSE).

---

<div align="center">
Built by <a href="https://aakash-sakhalkar.web.app">Aakash Sakhalkar</a>
</div>
