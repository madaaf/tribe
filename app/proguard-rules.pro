# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/tiago/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interfaces
# class:
#-keepclassmembers class fqcn.of.javascript.interfaces.for.webview {
#   public *;
#}
-dontwarn
-keep public class * implements com.bumptech.glide.module.GlideModule

-dontoptimize
-keepattributes SourceFile,LineNumberTable
-keep class com.tribe.app.** { *; }
-keepclassmembers class ** {
    public void onEvent*(**);
}