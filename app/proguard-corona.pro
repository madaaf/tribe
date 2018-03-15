-keep public class android.net.http.SslError
-keep public class android.webkit.WebViewClient
-dontwarn android.webkit.WebView
-dontwarn android.net.http.SslError
-dontwarn android.webkit.WebViewClient

-libraryjars libs
# Define the plugin class so that it doesn't get stripped out
-keep public class plugin.library.LuaLoader

# keep all the classes from Corona's jars
-keep class com.ansca.corona.** { *; }
-keep class com.naef.jnlua.** { *; }
-keep class network.**

-dontwarn org.apache.http.**
-dontwarn android.net.**
-keep class org.apache.** {*;}
-keep class org.apache.http.** { *; }

# Libraries reference classes not in the Android runtime - block those warnings
# references by math lib
-dontwarn java.awt.**
# referenced by jnlua.script
-dontwarn javax.script.**
# jnlua script itself
-dontwarn com.naef.jnlua.script.**
