# BatteryUtils

A production-ready, Java-only Android library for reading battery state:
percentage, status, health, charging source, temperature, voltage, current,
energy, charge/discharge estimates, and more — built entirely on public
Android APIs.

- **Java only**, AndroidX, `minSdk 21`
- No reflection, no hidden APIs, no OEM-private hooks
- Thread-safe, immutable data model
- One-shot queries (`BatteryUtils`) and live monitoring (`BatteryMonitor`)
- Export to JSON / `Bundle` / `Map`

See [`LIMITATIONS.md`](../LIMITATIONS.md) for an honest account of what
Android's public API does *not* expose, and how this library handles those
gaps (either gracefully unsupported, or clearly labeled as heuristic).

## Install (JitPack)

```gradle
// settings.gradle
dependencyResolutionManagement {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

```gradle
// app/build.gradle
dependencies {
    implementation 'com.github.aakashsakhalkar:BatteryUtils:1.0.0'
}
```

## Quick start

### One-shot read

```java
BatteryInfo info = BatteryUtils.getBatteryInfo(context);
int percentage = info.getPercentage();
boolean charging = info.isCharging();
BatteryHealth health = info.getHealth();
ChargingType source = info.getChargingType();
float tempC = info.getTemperatureCelsius();
```

### Live monitoring

```java
BatteryMonitor monitor = new BatteryMonitor.Builder(context)
        .setBatteryChangeListener(info -> {
            percentageText.setText(BatteryFormatter.formatPercentage(info.getPercentage()));
        })
        .listenForPowerSaveModeChanges(true)
        .setPowerSaveModeChangeListener(isOn -> saverBadge.setVisibility(isOn ? VISIBLE : GONE))
        .build();

// e.g. in onStart()/onStop()
monitor.start();
monitor.stop();
```

### Capability checks

```java
if (BatteryCapabilities.isChargeTimeRemainingSupported()) {
    long millis = BatteryEstimator.getChargeTimeRemainingMillis(context);
    label.setText(BatteryFormatter.formatDurationMillis(millis));
} else {
    label.setText("Not available on this API level");
}
```

### Export

```java
JSONObject json = info.toJson();
Bundle bundle = info.toBundle();
Map<String, Object> map = info.toMap();
```

## Classes

| Class | Purpose |
|---|---|
| `BatteryInfo` | Immutable snapshot of all readable battery properties |
| `BatteryUtils` | One-shot facade queries |
| `BatteryMonitor` | Live monitoring via `BroadcastReceiver`, main-thread callbacks |
| `BatteryStatus` / `BatteryHealth` / `ChargingType` / `ChargingSpeed` | Type-safe enums over platform constants |
| `BatteryCapabilities` | Runtime feature/API-level support checks |
| `BatteryFormatter` | Presentation formatting (temperature units, durations, etc.) |
| `BatteryCalculator` | Pure numeric calculations (power, rate-of-change, unit conversion) |
| `BatteryEstimator` | Charge-time / discharge-prediction wrappers + heuristic estimates |
| `BatteryAnalyzer` | Heuristic score/grade/trend for dashboard-style UI |
| `BatteryLogger` | Minimal internal logging toggle |

## Testing

```
./gradlew :batteryutils:test
```

Pure-Java logic (`BatteryCalculator`, `BatteryFormatter`, enum mappings) is
tested with plain JUnit; anything touching `Bundle`/JSON uses Robolectric.
