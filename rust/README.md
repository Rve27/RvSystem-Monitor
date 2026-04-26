# RvSystem Monitor - Rust Backend

This directory contains the Rust implementation of the system monitoring backend for the RvSystem Monitor Android application.

## Overview

The Rust component is responsible for gathering low-level system metrics (currently memory and ZRAM data) by interacting with the Linux kernel via `/proc` filesystem. It exposes these metrics to the Android app through **JNI (Java Native Interface)**.

## Project Structure

- `src/lib.rs`: Entry point for the JNI bridge. Contains the native functions called by Kotlin.
- `src/mm/`: Memory Management module.
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

To build for all supported Android architectures:

```bash
cargo ndk -t aarch64-linux-android -t armv7-linux-androideabi -t i686-linux-android -t x86_64-linux-android build --release
```

The resulting libraries will be located in `target/` and should be moved or linked to `app/src/main/jniLibs/`.

## JNI Integration

The Rust functions are mapped to the Kotlin `com.rve.systemmonitor.utils.MemoryUtils` object.

| Rust Function | Kotlin Native Method |
| :--- | :--- |
| `Java_..._getRamDataNative` | `getRamDataNative()` |
| `Java_..._getZramDataNative` | `getZramDataNative()` |

## Documentation

You can generate the HTML documentation for this crate by running:

```bash
cargo doc --open
```
