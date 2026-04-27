# RvSystem Monitor - Rust Backend

This directory contains the Rust implementation of the system monitoring backend for the RvSystem Monitor Android application.

## Overview

The Rust component is responsible for gathering low-level system metrics (CPU, Memory, and ZRAM data) by interacting with the Linux kernel via `/proc` and `/sys` filesystems. It exposes these metrics to the Android app through **JNI (Java Native Interface)**.

The project structure is intentionally organized to mirror the **Linux Kernel** organization.

## Project Structure

- `src/lib.rs`: Entry point for the JNI bridge. Contains the native functions called by Kotlin.
- `src/kernel/`: Core system logic (matches Linux kernel's `kernel/` directory).
    - `cpu.rs`: Logic for detecting core count, reading frequencies (Current, Min, Max), and scaling governors.
- `src/mm/`: Memory Management module (matches Linux kernel's `mm/` directory).
    - `memory.rs`: Logic for parsing `/proc/meminfo` and calculating RAM/ZRAM statistics.

## Building

This project is typically built as a dynamic library (`.so`) for Android using `cargo-ndk`.

### Prerequisites

1.  **Rust**: Install via [rustup.rs](https://rustup.rs/).
2.  **Android NDK**: Ensure the NDK is installed via Android Studio.
3.  **cargo-ndk**: Install it using:
    ```bash
    cargo install cargo-ndk
    ```
4.  **Target Architectures**: Add the necessary Rust targets for Android:
    ```bash
    rustup target add aarch64-linux-android armv7-linux-androideabi i686-linux-android x86_64-linux-android
    ```

### Build Command

To build for the primary Android architectures:

```bash
cargo ndk -t arm64-v8a -t armeabi-v7a build --release
```

The resulting libraries will be located in `target/` and are automatically managed by the Gradle task `:app:buildRustLibraries`.

## JNI Integration

The Rust functions are mapped to the corresponding Kotlin utility objects.

### Memory Utilities (`MemoryUtils`)

| Rust Function | Kotlin Native Method |
| :--- | :--- |
| `Java_..._getRamDataNative` | `getRamDataNative()` |
| `Java_..._getZramDataNative` | `getZramDataNative()` |

### CPU Utilities (`CpuUtils`)

| Rust Function | Kotlin Native Method |
| :--- | :--- |
| `Java_..._getCoreCountNative` | `getCoreCountNative()` |
| `Java_..._getCoreFrequencyNative` | `getCoreFrequencyNative(coreId, type)` |
| `Java_..._getCoreGovernorNative` | `getCoreGovernorNative(coreId)` |

## Documentation

You can generate the HTML documentation for this crate by running:

```bash
cargo doc --open
```
