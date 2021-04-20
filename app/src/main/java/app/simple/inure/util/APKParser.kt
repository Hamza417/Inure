package app.simple.inure.util

import android.content.pm.ApplicationInfo
import com.jaredrummler.apkparser.ApkParser
import com.jaredrummler.apkparser.model.AndroidComponent
import com.jaredrummler.apkparser.model.CertificateMeta

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

    /**
     * Fetch the list of providers from an APK file
     */
    fun ApplicationInfo.getProviders(): MutableList<AndroidComponent>? {
        ApkParser.create(this).use {
            return it.androidManifest.providers
        }
    }

    /**
     * Fetch the list of permissions from an APK file
     */
    fun ApplicationInfo.getPermissions(): MutableList<String> {
        ApkParser.create(this).use {
            return it.androidManifest.apkMeta.usesPermissions
        }
    }

    /**
     * Fetch the certificate data from an APK file
     */
    fun ApplicationInfo.getCertificates(): CertificateMeta {
        ApkParser.create(this).use {
            return it.certificateMeta
        }
    }

    fun ApplicationInfo.getBroadcasts(): MutableList<AndroidComponent>? {
        ApkParser.create(this).use {
            return it.androidManifest.receivers
        }
    }
}
