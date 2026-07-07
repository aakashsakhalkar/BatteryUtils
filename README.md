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

## Why BatteryUtils?

Reading battery state on Android means juggling sticky broadcasts,
`BatteryManager` properties, and API-level landmines. BatteryUtils wraps all
of it into one clean, type-safe, well-documented API — **no reflection, no
hidden framework fields, no OEM-private hooks.** Every value is backed by a
real, public Android API, and every gap in what Android exposes is documented
honestly instead of faked. See [`LIMITATIONS.md`](LIMITATIONS.md).

## Features

| | |
|---|---|
| ✅ Battery percentage, level, scale | ✅ Live monitoring with `BatteryMonitor` |
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

## Quick start

```java
// One-shot read
BatteryInfo info = BatteryUtils.getBatteryInfo(context);
int percentage = info.getPercentage();
boolean charging = info.isCharging();
ChargingType source = info.getChargingType();
```

```java
// Live monitoring
BatteryMonitor monitor = new BatteryMonitor.Builder(context)
        .setBatteryChangeListener(info -> updateUi(info))
        .build();

monitor.start(); // e.g. in onStart()
monitor.stop();  // e.g. in onStop()
```

Full API reference and more examples: [`batteryutils/README.md`](batteryutils/README.md).

## Project structure

```
BatteryUtils/
├── batteryutils/        # the library module
│   └── src/main/java/com/aakash/batteryutils/
├── app/                 # demo app (Material 3)
├── LIMITATIONS.md        # honest platform-limitation notes
├── CHANGELOG.md
└── LICENSE
```

## Contributing

Contributions are welcome — see [`CONTRIBUTING.md`](CONTRIBUTING.md) for
guidelines before opening a PR.

## License

Released under the [MIT License](LICENSE).

---

<div align="center">
Built by <a href="https://aakash-sakhalkar.web.app">Aakash Sakhalkar</a>
</div>
