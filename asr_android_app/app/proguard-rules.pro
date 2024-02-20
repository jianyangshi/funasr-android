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
# Add project specific ProGuard rules here.
 
-dontobfuscate
 
# Preserve the line number information for debugging stack traces.
-keepattributes SourceFile,LineNumberTable
 
# Library specific rules
-dontnote android.net.http.*
-dontnote org.apache.commons.codec.**
-dontnote org.apache.http.**
-dontnote com.squareup.moshi.**
-dontnote com.github.amlcurran.showcaseview.**
-dontnote de.cketti.safecontentresolver.**
-dontnote com.tokenautocomplete.**
 
-dontwarn okio.**
-dontwarn com.squareup.moshi.**
 
# Project specific rules
-dontnote com.fsck.k9.ui.messageview.**
-dontnote com.fsck.k9.view.**
 
-keep public class org.openintents.openpgp.**
 
-keepclassmembers class * extends androidx.appcompat.widget.SearchView {
    public <init>(android.content.Context);
}
 
# okhttp rules
# see: https://github.com/square/okhttp/blob/master/okhttp/src/main/resources/META-INF/proguard/okhttp3.pro
 
# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**
 
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
 
# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*
 
# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform
