# RvSystem Monitor

RvSystem Monitor is a powerful, high-performance Android system monitoring application built with *
*Jetpack Compose** and a **Rust** backend. It provides real-time insights into your device's
hardware, processor performance, and memory usage.

## 🚀 Key Features

- **Detailed CPU Monitoring**: Real-time per-core frequencies (Current, Min, Max) and scaling
  governors.
- **Memory Insights**: Live tracking of RAM and ZRAM usage with precise GB and percentage metrics.
- **Hardware Overview**: Comprehensive information about SOC Manufacturer, Model, OS Version, and
  GPU (Vendor/Renderer).
- **Performance Optimized**: Native Rust backend for efficient system file parsing with minimal JVM
  overhead.
- **Lifecycle Aware**: Intelligent data streaming that automatically stops when the app is in the
  background to save battery.
- **Modern UI**: Clean, expressive Material 3 design with animated progress indicators and
  responsive layouts.

## 🛠️ Architecture

The project follows a modern, modular architecture:

- **Frontend (Kotlin/Compose)**:
    - Uses **Clean Architecture** patterns with Domain, Data, and UI layers.
    - **Dagger Hilt** for dependency injection.
    - **StateFlow** and **Coroutines** for reactive, lifecycle-aware data handling.
- **Backend (Rust)**:
    - Mirroring **Linux Kernel** organization (`kernel/`, `mm/`).
    - High-performance sysfs and procfs parsing.
    - Interfaced via **JNI (Java Native Interface)** for maximum efficiency.

## 📁 Project Structure

```text
RvSystem-Monitor/
├── app/                  # Android application module (Kotlin/Compose)
├── rust/                 # Native system monitoring backend (Rust)
│   ├── src/kernel/       # Core system logic (CPU, etc.)
│   └── src/mm/           # Memory management logic (RAM, ZRAM)
├── build.gradle.kts      # Project-wide Gradle configuration
└── README.md             # This file
```

## 🛠️ Getting Started

### Prerequisites

1. **Android Studio** (Ladybug or newer recommended).
2. **Rust Toolchain**: Install via [rustup.rs](https://rustup.rs/).
3. **Android NDK**: Required for Rust compilation.
4. **cargo-ndk**: `cargo install cargo-ndk`.

### Building the Project

1. **Clone the repository**:
   ```bash
   git clone https://github.com/Rve27/RvSystem-Monitor.git
   cd RvSystem-Monitor
   ```

2. **Build Rust Libraries**:
   The project is configured to build Rust libraries automatically during the Gradle build process.
   You can also trigger it manually:
   ```bash
   ./gradlew :app:buildRustLibraries
   ```

3. **Build and Run**:
    - **Using Android Studio**: Open the project in Android Studio and click **Run**.
    - **Using CLI**:
        - **Build Debug APK**:
          ```bash
          ./gradlew assembleDebug
          ```
        - **Build Release APK**:
          ```bash
          ./gradlew assembleRelease
          ```
        - **Install and Run on Device**:
          ```bash
          ./gradlew installDebug
          ```

          The APK will be located at `app/build/outputs/apk/debug/app-debug.apk`.

## 📝 Debugging

The application includes detailed lifecycle logging in `debug` variants. You can monitor the status
of CPU and Memory data streams in **Logcat** by filtering for the `SystemInfoRepository` tag.
