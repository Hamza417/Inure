# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}

# Uncomment this to preserve the line number information for
# debugging stack traces.
# -keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
# -renamesourcefileattribute SourceFile

# -dontobfuscate
# -optimizationpasses 5
# -dontusemixedcaseclassnames
# -dontskipnonpubliclibraryclasses
# -dontpreverify

-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/allocation/variable

-keep class app.simple.inure.play.activities.alias.TerminalAlias
-keep class app.simple.inure.play.terminal.TermInternal

-keep class app.simple.inure.activities.alias.TerminalAlias
-keep class app.simple.inure.terminal.TermInternal
-keep class app.simple.inure.activities.alias.TermHere
-keep class app.simple.inure.play.activities.alias.TermHere

-keep class app.simple.inure.play.database.** { *; }
-keep class app.simple.inure.database.** { *; }

# This is generated automatically by the Android Gradle plugin.
-dontwarn javax.annotation.Nonnull
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.concurrent.GuardedBy
-dontwarn org.bouncycastle.cert.X509CertificateHolder
-dontwarn org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
-dontwarn org.bouncycastle.cms.CMSException
-dontwarn org.bouncycastle.cms.CMSSignedData
-dontwarn org.bouncycastle.cms.SignerId
-dontwarn org.bouncycastle.cms.SignerInformation
-dontwarn org.bouncycastle.cms.SignerInformationStore
-dontwarn org.bouncycastle.jce.provider.BouncyCastleProvider
-dontwarn org.bouncycastle.util.Selector
-dontwarn org.bouncycastle.util.Store
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.OpenSSLProvider

# Gson uses generic type information stored in a class file when working with
# fields. Proguard removes such information by default, keep it.
-keepattributes Signature

# This is also needed for R8 in compat mode since multiple
# optimizations will remove the generic signature such as class
# merging and argument removal. See:
# https://r8.googlesource.com/r8/+/refs/heads/main/compatibility-faq.md#troubleshooting-gson-gson
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Optional. For using GSON @Expose annotation
-keepattributes AnnotationDefault,RuntimeVisibleAnnotations
