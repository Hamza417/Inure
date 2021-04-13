package app.simple.inure.util

import android.content.pm.ApplicationInfo
import com.jaredrummler.apkparser.ApkParser
import com.jaredrummler.apkparser.model.AndroidComponent

object APKParser {
    /**
     * Fetch the decompiled manifest from an APK file
     */
    fun ApplicationInfo.extractManifest(): String? {
        ApkParser.create(this).use {
            return it.manifestXml
        }
    }

    /**
     * Fetch the list of service from an APK file
     */
    fun ApplicationInfo.getServices(): MutableList<AndroidComponent>? {
        ApkParser.create(this).use {
            return it.androidManifest.services
        }
    }

    /**
     * Fetch the list of activities from an APK file
     */
    fun ApplicationInfo.getActivities(): MutableList<AndroidComponent>? {
        ApkParser.create(this).use {
            return it.androidManifest.activities
        }
    }
}
