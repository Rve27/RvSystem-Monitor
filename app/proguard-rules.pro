# Project-specific ProGuard rules for RvSystem-Monitor

# 1. JNI: Keep classes and their native methods for linkage
-keep class com.rve.systemmonitor.utils.CpuUtils { native <methods>; }
-keep class com.rve.systemmonitor.utils.DeviceUtils { native <methods>; }
-keep class com.rve.systemmonitor.utils.GpuUtils { native <methods>; }
-keep class com.rve.systemmonitor.utils.MemoryUtils { native <methods>; }

# 5. Standard Android/Compose attributes
-keepattributes Signature,InnerClasses,EnclosingMethod,AnnotationDefault,*Annotation*

# 6. Shizuku: Keep service class for name-based instantiation and AIDL bridge
-keep class com.rve.systemmonitor.shizuku.CommandRunnerService {
    <init>(android.content.Context);
}
-keep class com.rve.systemmonitor.shizuku.ICommandRunner { *; }
-keep class com.rve.systemmonitor.shizuku.ICommandRunner$Stub { *; }
-keep class com.rve.systemmonitor.shizuku.ICommandRunner$Stub$Proxy { *; }

