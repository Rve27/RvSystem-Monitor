# Changelog

## [0.2-beta] - 2026-05-03

### Features
- **battery:** Add animation to status text.
- **battery:** Make battery graph history duration reactive.
- **display:** Determine DPI badge color depending on HDR support.
- **display:** Add HDR capabilities detection.
- **vulkan:** Add Vulkan version detection and display.
- **settings:** Implement adjustable vibration intensity.
- **haptics:** Implement system-wide haptic feedback support.
- **ui:** Implement data source help bottom sheet in HomeScreen.
- **ui:** Implement visual customization options in Overlay Settings.

### Performance
- Persist battery history across screen sessions while pausing updates when inactive.
- Ensure battery data streams pause when the screen is inactive to save resources.
- Optimize battery screen performance and fix graph scaling issues.
- Optimize JNI data bridge and Compose recomposition for smoother UI.

### UI/UX Enhancements
- Set minimum 1000mA scale for battery speed graph for better readability.
- Change battery status animation to a smoother horizontal slide.
- **BatteryScreen:** Implement real-time charging speed graph.
- **BatteryScreen:** Animate Power Source text transitions.
- **BatteryScreen:** Implement dynamic charging speeds and animations.
- **Theme:** Adopt Material 3 motion scheme for color animations.
- **Settings:** Update snap animations and overlay transitions to use standard motion specs.
- **CPUScreen:** Redesign CoreDetailCard for better information density.

### Bug Fixes
- **ui:** Fix BottomNavBar colors in dark mode.
- **ui:** Fix color accent bugs on certain Custom ROMs.
- **ui:** Eliminate white blink effect on Layout cards in Overlay Settings.

### Refactoring & Cleanup
- Extract reusable UI components and reorganize haptic package.
- Update JNI array manipulation methods for better safety.
- Use `EnvUnowned` for more robust JNI handling in Rust.
- Reorganize generic UI components into a shared directory.
- Optimize imports and format native code.
- Standardize typography by removing monospace font from charging speed display.

### Build & CI
- Bump `jni` crate from 0.21.1 to 0.22.4.
- Configure Rust release profile and add necessary dependencies.
- Add Cargo ecosystem support to Dependabot.
- Configure Gradle variants for side-by-side installation of debug and release builds.

### Documentation
- Comprehensive update of project documentation.
- Add KDoc documentation to all major UI components.
- Update README with latest features and build instructions.
