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

# Keep Firestore data model classes for reflection-based deserialization
-keepclassmembers class dev.sagi.monotask.data.model.** { *; }
-keep class dev.sagi.monotask.data.model.** { *; }

# Keep navigation route class names. Compose Navigation uses the compile-time serial name
# for destination.route, which must match route::class.qualifiedName in NavUtils.TAB_ORDER.
# R8 class merging collapses empty data objects to one class, breaking isForward() in release.
-keepnames class dev.sagi.monotask.ui.navigation.*Route
