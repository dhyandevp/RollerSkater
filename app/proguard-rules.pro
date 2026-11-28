
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

-keepattributes Signature, InnerClasses, EnclosingMethod, AnnotationDefault, *Annotation*

-keep class com.kyant.taglib.** { *; }

-keep class org.jaudiotagger.tag.** { *; }
-dontwarn org.jaudiotagger.**

-dontwarn java.awt.**
-dontwarn javax.imageio.**
-dontwarn javax.sound.sampled.**
-dontwarn javax.swing.filechooser.FileFilter
-dontwarn javax.lang.model.**

-keep class androidx.media3.decoder.ffmpeg.** { *; }
-keep class androidx.media3.exoplayer.ffmpeg.** { *; }

-keepclassmembers class com.rollerskater.app.data.model.** { *; }

-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

-keep class com.rollerskater.app.data.preferences.PreferenceBackupEntry { *; }
-keep class com.rollerskater.app.data.backup.model.** { *; }
-keep class com.rollerskater.app.data.backup.module.** { *; }
-keep class com.rollerskater.app.data.database.FavoritesEntity { *; }
-keep class com.rollerskater.app.data.database.SongEngagementEntity { *; }
-keep class com.rollerskater.app.data.database.LyricsEntity { *; }
-keep class com.rollerskater.app.data.database.SearchHistoryEntity { *; }
-keep class com.rollerskater.app.data.database.TransitionRuleEntity { *; }
-keep class com.rollerskater.app.data.database.AudioBookmarkEntity { *; }

-dontwarn kotlinx.coroutines.**
-dontwarn org.slf4j.**
-dontwarn java.lang.management.**


-keep class com.atilika.kuromoji.** { *; }
-keepnames class com.atilika.kuromoji.** { *; }
-dontwarn com.atilika.kuromoji.**

-keep class net.sourceforge.pinyin4j.** { *; }
-keepclassmembers class net.sourceforge.pinyin4j.** { *; }
-dontwarn net.sourceforge.pinyin4j.**

-keep class * extends androidx.glance.appwidget.action.ActionCallback { <init>(); }
-keep class * extends androidx.glance.appwidget.GlanceAppWidget { <init>(); }
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver { <init>(); }
-keep class com.rollerskater.app.ui.glancewidget.** { *; }

# Room Database
-keep class * extends androidx.room.RoomDatabase
-keep class com.rollerskater.app.data.database.** { *; }
-dontwarn androidx.room.paging.**

# Media3 Session & Services
-keep class androidx.media3.session.** { *; }
-keep class com.rollerskater.app.data.service.** { *; }

# Native methods (JNI)
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

# Image loading (Coil)
-keep class coil.** { *; }
-dontwarn coil.**
-keep class coil3.** { *; }
-dontwarn coil3.**

# Vorbis-Java
-keep class org.gagravarr.** { *; }
-dontwarn org.gagravarr.**

# Kotlinx Serialization
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keepclassmembers class * {
    companion object;
}
-keepclassmembers class * extends kotlinx.serialization.KSerializer {
    <fields>;
    <methods>;
}

-assumenosideeffects class timber.log.Timber {
    public static void v(...);
    public static void d(...);
    public static void i(...);
    public static timber.log.Timber$Tree tag(java.lang.String);
}
-assumenosideeffects class timber.log.Timber$Tree {
    public void v(...);
    public void d(...);
    public void i(...);
}
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
