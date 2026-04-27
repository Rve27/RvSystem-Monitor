# Project-specific ProGuard rules for RvSystem-Monitor

# 1. JNI: Keep classes that interface with the Rust library
# These classes are called by the native C/Rust code using exact class/method names.
-keep class com.rve.systemmonitor.utils.CpuUtils { *; }
-keep class com.rve.systemmonitor.utils.MemoryUtils { *; }

# 2. Backdrop: Keep library classes for shaders and Liquid Glass effects
-keep class io.github.kyant0.backdrop.** { *; }
# Prevent warnings for optional dependencies the library might reference
-dontwarn io.github.kyant0.backdrop.**

# 3. Compose: Keep custom Modifier nodes (used by many UI libraries)
-keep public class * extends androidx.compose.ui.node.ModifierNodeElement

# 4. Standard Android/Compose attributes
-keepattributes Signature,InnerClasses,EnclosingMethod,AnnotationDefault,*Annotation*
