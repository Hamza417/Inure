package app.simple.inure.util

import android.content.pm.ApplicationInfo
import com.jaredrummler.apkparser.ApkParser
import com.jaredrummler.apkparser.model.AndroidComponent
import java.io.IOException

object APKParser {
    /**
     * Fetch the decompiled manifest from an APK file
     */
    fun ApplicationInfo.extractManifest(): String? {
        return try {
            val apkParser = ApkParser.create(this)
            val xml = apkParser.manifestXml
            apkParser.close()
            xml
        } catch (e: IOException) {
            "null"
        }
    }

    /**
     * Fetch the list of service from an APK file
     */
    fun ApplicationInfo.getServices(): MutableList<AndroidComponent>? {
        return try {
            val apkParser = ApkParser.create(this)
            val xml = apkParser.androidManifest.services
            apkParser.close()
            xml
        } catch (e: IOException) {
            null
        }
    }
}
