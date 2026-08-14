# Changelog

## [0.9-beta] - 2026-08-12

### Feat
- **ui**: Add detailed rust library information section
- **ui**: Add navigation to cpu screen from processor card
- **cpu**: Add ABI and bitness info to CPU overview card
- **cpu**: Detect detailed ARM architecture from cpuinfo
- **about**: Add chenlongapps to contributors list
- **ui**: Show all supported refresh rates in display card
- **hardware**: Gpu temp fallback via shizuku
- **cpu**: Fallback temp via shizuku
- **settings**: Add language selection feature and support for multiple languages
- **about**: Add sponsor card and funding integration
- **overlay**: Add auto-toggle for selected apps
- **cpu**: Add overall cpu temperature badge to overview card
- **gpu**: Extract and display various vulkan limits and properties
- **gpu**: Add opengl and vulkan extensions bottom sheet

### Fix
- **cpu**: Keep load delta across io threads
- **ui**: Hide per-core temp and governor when absent
- **cpu**: Stop faking per-core temp from shizuku
- **ui**: Update ToggleButton shapes for material3 alpha25
- **ui**: Always display frequency in GHz format
- **cpu**: Prevent selection of invalid thermal zones and support decidegree units
- **cpu**: Add fallback paths for cpu frequency sysfs reading

### Perf
- **cpu**: Flatten nested if-let in thermal parsing
- **cpu**: Avoid Vec alloc in freq fallback path
- **cpu**: Inline /proc/stat parsing — no collect()
- **cpu**: Reuse Vec buffers in calculate_cpu_load
- **cpu**: Add get_all_core_temperatures() — single Mutex lock

### Refactor
- **settings**: Replace language icon with translate
- **settings**: Remove current language indicator
- **build**: Remove unnecessary ProGuard rules and Kotlin compiler parameters
- **about**: Remove total contributions from about screen and models
- **cpu**: Split cpu.rs into cpu+thermal modules, add cached file descriptors, add JNI benchmark
- **gpu**: Remove memory information and its unused resources
- **gpu**: Extract vulkan limits into new card

### Style
- **code**: Format codebase and optimize imports
- **ui**: Rename "CPU Core" to "Core" in cpu screen
- **ui**: Remove icon from cpu core cards
- **ui**: Append px suffix to vulkan image limits

## [0.8-beta] - 2026-07-24

### Feat
- **device**: Display market name and use brand instead of manufacturer
- **ui**: Add chromatic aberration to bottom navigation bar
- **os**: Display HyperOS version and refine OS card layout
- **ui**: Animate bottom nav bar item content color
- **ui**: Add option to disable ripple effect in hapticClickable
- **os**: Add Android 17 (Cinnamon Bun) dessert name mapping
- **cpu**: Add tsens fallback for legacy Snapdragon formats
- **cpu**: Support MediaTek Dimensity 8500 thermal zone format
- **cpu**: Support MediaTek G99 thermal zone format
- **cpu**: Support Snapdragon 845 thermal zone format
- **cpu**: Support Qualcomm cpu-C-N-S thermal zone format
- **setup**: Add skip option for overlay permission
- **ui**: add new contributors and bundle offline avatars
- **settings**: add toggle to disable transition background blur
- **backup**: add missing overlay settings to backup
- **ui**: New screen transition animations
- **overlay**: add customizable overlay position feature
- **fps**: calculate real render fps using totalFrames delta

### Fix
- **cpu**: Add fallback to cpufreq/policy paths for cpu metrics
- **ui**: Make bottom nav items responsive on small screens
- **navigation**: Remove color animations in bottom nav bar
- **cpu**: Fix temperature and governor detection on MediaTek/Samsung SoCs
- **overlay**: use TypedValue for sp conversion
- **battery**: synchronize graph updates with power draw metric
- **rust**: replace deprecated JNI get_string method
- **overlay**: replace TextView with SurfaceView to prevent refresh rate boost
- **i18n**: remove unused and duplicate translations
- rename translation files to strings.xml

### Refactor
- **ui**: Remove cpu temperature from cpu overview card

### Docs
- **readme**: update badges and support links
- Remove version numbers from Tech Stack
- Remove Key Features section

### Style
- **ui**: Redesign bottom nav bar
- **ui**: Adjust bottom nav selected item background contrast
- **ui**: Remove ripple effect from BottomNavBar items
- apply spotless formatting

### Build
- **deps**: bump com.android.application from 9.3.0 to 9.3.1
- **deps**: bump libc from 0.2.188 to 0.2.189 in /rust
- **deps**: bump libc from 0.2.186 to 0.2.188 in /rust
- **deps**: bump com.android.application from 9.2.1 to 9.3.0
- **deps**: bump org.jetbrains.kotlinx:kotlinx-collections-immutable
- **deps**: bump com.google.devtools.ksp from 2.3.9 to 2.3.10
- **deps**: bump hilt from 2.60 to 2.60.1
- **deps**: bump kotlin from 2.4.0 to 2.4.10
- **deps**: bump androidx.compose.material3:material3
- **deps**: bump androidx.hilt:hilt-navigation-compose
- **deps**: bump com.diffplug.spotless from 8.7.0 to 8.8.0
- **deps**: Add errorprone annotations dependency
- **deps**: bump androidx.compose:compose-bom
- **deps**: bump hilt from 2.59.2 to 2.60
- **deps**: bump gradle-wrapper from 9.6.0 to 9.6.1
- **deps**: bump gradle-wrapper from 9.5.1 to 9.6.0
- **deps**: bump log from 0.4.32 to 0.4.33 in /rust

### Other Changes
- Revert "fix(cpu): Fix temperature and governor detection on MediaTek/Samsung SoCs"
- Fix: resolve inconsistent overlay update intervals and freezing
- Fix(fastlane): trim short description to stay under 80 characters limit
- UK and RU translations

## [0.7.1-alpha] - 2026-06-22

### Feat
- **ui**: move design capacity to overview card

### Fix
- **about**: resolve link crash and remove github api dependency
- **battery**: fix current detection on OnePlus and BBK devices

### Perf
- **ui**: optimize UI string allocations in CoreDetail
- **rust**: optimize rust calculate_cpu_load allocations
- **rust**: optimize JNI string parsing
- **shizuku**: optimize Shizuku command execution

### Docs
- **readme**: update tech stack versions
- **metadata**: update build instructions and metadata descriptions

### Build
- **config**: make release signing optional for F-Droid
- **config**: remove foojay, secure wrapper, and adjust sdk 37
- **rust**: pin toolchain to 1.96.0 for reproducible builds
- **manifest**: restrict REQUEST_INSTALL_PACKAGES to github variant
- **deps**: bump coil from 3.4.0 to 3.5.0
- **deps**: bump com.diffplug.spotless from 8.6.0 to 8.7.0
- **deps**: bump androidx.compose:compose-bom
- **deps**: bump lifecycleRuntimeKtx from 2.10.0 to 2.11.0
- **deps**: bump androidx.compose.material3:material3

## [0.6-beta] - 2026-06-12

### Feat
- **updater**: strictly require 'app-github-release.apk' for updates
- **ui**: add backup restore step to setup flow
- **dist**: add distribution flavors and toggleable updater
- **metadata**: add icon for fastlane
- **settings**: implement backup and restore functionality
- **ui**: update navigation transitions to MD3 Expressive

### Fix
- **overlay**: fix metrics not updating in floating overlay
- **gpu**: implement reactive polling for temperature updates
- **ui**: ensure card color consistency across screens

### Perf
- **flow**: optimize flow collection and threading

### Refactor
- **core**: optimize memory data mapping and cleanup UI
- **update**: remove variant-specific apk check
- **dead code**: remove dead code and unused resources

### Style
- **ui**: improve selected nav item glass effect with BlendMode.Hue

### Build
- **deps**: bump okhttp from 5.3.2 to 5.4.0
- **deps**: bump org.jetbrains.kotlinx:kotlinx-collections-immutable
- **kotlin**: fix build errors after kotlin 2.4.0 update
- **deps**: bump log from 0.4.30 to 0.4.32 in /rust
- **deps**: bump androidx.core:core-ktx from 1.18.0 to 1.19.0
- **deps**: bump androidx.compose.material3:material3
- **deps**: bump kotlin from 2.3.21 to 2.4.0
- **deps**: bump io.github.kyant0:backdrop from 2.0.0-rc01 to 2.0.0
- **deps**: bump com.diffplug.spotless from 8.5.1 to 8.6.0
- **deps**: bump com.google.devtools.ksp from 2.3.8 to 2.3.9

### Other Changes
- Release v0.6-beta

## [0.5-beta] - 2026-05-26

### Feat
- **overlay**: migrate to shizuku-based surfaceflinger fps monitoring
- **cpu**: add shizuku integration and cpu load monitoring
- **ui**: add detailed gpu hardware capabilities and reorganise layout
- **rust**: add rust library specifications screen
- **gpu**: display temperature as badge chip in overview
- **gpu**: add vulkan driver version reporting
- **gpu**: add detailed opengl es version reporting
- **gpu**: add gpu temperature monitoring and details screen
- **ui**: add theme selection step to setup flow
- **ui**: add exit animation to setup completion
- **ui**: overhaul SetupScreen with modern M3 Expressive design
- **ui**: redesign setup screen with dotLottie animations

### Fix
- **battery**: fix battery status string populating
- **ui**: migrate deprecated `rememberModalBottomSheetState` to `rememberBottomSheetState`
- **vulkan**: increase buffer size for `VkPhysicalDeviceProperties`
- **utils**: specify `Locale.US` in `CpuUtils` frequency formatting
- **ui**: enforce `Locale.US` in `CPUScreen` string formatting
- **domain**: specify `Locale` in `CPU` frequency formatting

### Perf
- **ui**: optimize compose stability and state reactivity
- **jni**: batch cpu static info retrieval and switch to decimal units
- **data**: cache github contributors in memory
- **data**: implement multi-tiered caching for performance optimization
- **ui**: optimize compose stability across models and screens
- **ui**: optimize viewmodel initialization and navigation smoothness
- **ui**: optimize lottie animation in `SetupScreen`
- **ui**: optimize jetpack compose stability and skippability

### Refactor
- **overlay**: improve reactivity and structural organization of `SystemOverlayService`
- **data**: improve flow and channel emission safety
- **ui**: externalize hardcoded strings to `strings.xml`
- **imports**: replace fully qualified names with explicit imports
- **rust**: consolidate native monitoring logic and reduce JNI boilerplate
- **ui**: consolidate settings components and improve accessibility
- **ui**: separate state collection from stateless content in screens
- **ui**: use `mutableIntStateOf` for `selectedOption` in `UpdateDialog`

### Build
- **deps**: bump log, material3, compose-bom, and spotless
- **config**: remove redundant backdrop and jna proguard rules
- **deps**: migrate from dotlottie to lottie-compose

### Other Changes
- Release v0.5-beta
- Update screenshots from v0.4-beta

## [0.4-beta] - 2026-05-16

### Feat
- **ui:** add custom LShape and update Appearance hero section
- **perf:** reactive overlay settings and performance optimizations
- **overlay:** rename to floating overlay and add master toggle
- **ui:** refactor appearance settings into grouped cards
- **ui:** update appearance hero description text
- **ui:** add interactive hero section to appearance settings
- **navigation:** add isTestFlow parameter to Setup route
- **ui:** add amoled mode for pure black backgrounds
- **ui:** add pause updates feature to update dialog
- **update:** pause redundant update checks when update UI is active
- **ui:** update VersionPill colors in UpdateDialog
- **ui:** update dialog icon and colors

### Fix
- **update:** remove intrusive update failure error message
- **ui:** resolve compilation errors and update icons in UpdateDialog

### Perf
- **overlay:** optimize SystemOverlayService

### Refactor
- **kernel:** flatten thermal reading logic in cpu.rs
- **ui:** update settings screen with grouped list styling
- **ui:** extract reusable compose components and utilities
- **core:** reduce boilerplate across kotlin and rust layers

### Docs
- **readme:** update features, tech stack, and rust documentation

### Style
- **ui:** animate color selection border width
- **ui:** simplify layout option card visuals
- **ui:** update settings menu item arrow tint to primary
- **ui:** apply spotless formatting to AppearanceSettingsScreen
- **setup:** move next button to bottom in SetupScreen
- **setup:** use default button shapes in SetupScreen
- **ui:** set primary color for switch check icons

### Build
- **deps:** bump com.diffplug.spotless from 8.4.0 to 8.5.0
- **deps:** bump com.google.devtools.ksp from 2.3.7 to 2.3.8
- **deps:** bump gradle-wrapper from 9.5.0 to 9.5.1
- **config:** optimize gradle performance settings

### Chore
- chore: remove unused imports and optimize codebase

## [0.3-beta] - 2026-05-09

### Feat
- **ui:** Add testing section to app settings for setup flow.
- **ui:** Refactor setup screen into a multi-step onboarding flow.
- **update:** Enhance update feature with expressive UI and changelogs.
- **ui:** Add app settings screen and auto-update toggle.
- **update:** Implement in-app "Check for Updates" feature.
- **Floating Overlay:** Implement CPU and Battery temperature monitoring.
- **battery:** Reset min/max stats and graph on charging status change.
- **cpu:** Add real-time temperature monitoring for CPU and individual cores.
- **battery:** Display negative values for discharging speed.
- **Floating Overlay:** Support 0.5s increments for update interval.

### Fix
- **ui:** Resolve peak frequency showing 0.0 GHz on CPU screen.
- **display:** Modernize APIs and fix context compatibility.

### Perf
- **kernel:** Optimize cpu.rs sysfs polling pipeline.
- **cpu:** Optimize hardware polling with JNI batching and thermal caching.

### Refactor
- **rust:** Optimize memory info parsing using pattern matching.
- **ui:** Modularize generic components and cleanup dead code.
- **display:** Use modern Android 14+ APIs for display and HDR capabilities.

### Docs
- Overhaul README and add contributing guidelines.
- Add initial 0.2-beta changelog.

### Build
- Bump AGP to 9.2.1 and core dependencies.

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
- **ui:** Implement visual customization options in Floating Overlay Settings.

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
- **Settings:** Update snap animations and Floating Overlay transitions to use standard motion specs.
- **CPUScreen:** Redesign CoreDetailCard for better information density.

### Bug Fixes
- **ui:** Fix BottomNavBar colors in dark mode.
- **ui:** Fix color accent bugs on certain Custom ROMs.
- **ui:** Eliminate white blink effect on Layout cards in Floating Overlay Settings.

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
