# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

##---------------Begin: proguard configuration for Retrofit  ---------------------------------------

-dontwarn okio.**
-dontwarn javax.annotation.**

# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions

##---------------End: proguard configuration for Retrofit  -----------------------------------------


##---------------Begin: proguard configuration for Gson  -------------------------------------------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
#-keep class com.epic.pos.adapter.** { *; }
#-keep class com.epic.pos.common.** { *; }
#-keep class com.epic.pos.config.** { *; }
#-keep class com.epic.pos.crypto.** { *; }
#-keep class com.epic.pos.dagger.** { *; }
#-keep class com.epic.pos.data.** { *; }
#-keep class com.epic.pos.databinding.** { *; }
#-keep class com.epic.pos.domain.** { *; }
#-keep class com.epic.pos.helper.** { *; }
#-keep class com.epic.pos.iso.** { *; }
#-keep class com.epic.pos.receipt.** { *; }
#-keep class com.epic.pos.service.** { *; }
#-keep class com.epic.pos.tlv.** { *; }
#-keep class com.epic.pos.util.** { *; }
#-keep class com.epic.pos.view.** { *; }
#-keep class com.epic.pos.x990.** { *; }
-keep class com.epic.pos.** { *; }
-keep class lk.epic.eatmca.** { *; }
-keep class com.vfi.smartpos.** { *; }
-keep class org.jpos.iso.** { *; }
-keep class org.apache.crimson.parser.** { *; }

# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

##---------------End: proguard configuration for Gson  ---------------------------------------------


##---------------Begin: proguard configuration for RxJava  -----------------------------------------
-keep class rx.schedulers.Schedulers {
    public static <methods>;
}
-keep class rx.schedulers.ImmediateScheduler {
    public <methods>;
}
-keep class rx.schedulers.TestScheduler {
    public <methods>;
}
-keep class rx.schedulers.Schedulers {
    public static ** test();
}
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    long producerNode;
    long consumerNode;
}

##---------------End: proguard configuration for RxJava  -------------------------------------------

##---------------Begin: proguard configuration for BottomNavigationView DisableShiftMode  ----------
-keepclassmembers class android.support.design.internal.BottomNavigationMenuView {
    boolean mShiftingMode;
}
##---------------End: proguard configuration for BottomNavigationView DisableShiftMode  ------------
