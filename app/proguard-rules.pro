# Project-specific ProGuard rules for RvSystem-Monitor

# 1. JNI: Keep classes and their native methods for Rust linkage
-keep class com.rve.systemmonitor.utils.CpuUtils {
    native <methods>;
}
-keep class com.rve.systemmonitor.utils.MemoryUtils {
    native <methods>;
}

# 2. Battery: Keep reflection targets for internal PowerProfile
-keep class com.android.internal.os.PowerProfile {
    public <init>(android.content.Context);
    public double getBatteryCapacity();
}

# 3. Backdrop: Consumer rules are bundled with the library.
# We keep only the dontwarn for optional dependencies.
-dontwarn io.github.kyant0.backdrop.**

# 4. Standard Android/Compose attributes
-keepattributes Signature,InnerClasses,EnclosingMethod,AnnotationDefault,*Annotation*
