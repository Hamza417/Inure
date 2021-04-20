package app.simple.inure.util

import android.content.pm.ApplicationInfo
import com.jaredrummler.apkparser.ApkParser
import com.jaredrummler.apkparser.model.AndroidComponent
import com.jaredrummler.apkparser.model.CertificateMeta
import java.io.IOException
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import kotlin.collections.ArrayList

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

    /**
     * Fetch the list of broadcast receivers from
     * an APK file
     */
    fun ApplicationInfo.getBroadcasts(): MutableList<AndroidComponent>? {
        ApkParser.create(this).use {
            return it.androidManifest.receivers
        }
    }

    /**
     * Fetch the list of broadcast receivers from
     * an APK file
     */
    fun ApplicationInfo.getTransBinaryXml(path: String): String {
        ApkParser.create(this).use {
            return it.transBinaryXml(path)
        }
    }

    /**
     * Get list of all xml files within an APK file
     */
    fun getXmlFiles(path: String?): MutableList<String> {
        val xmlFiles: MutableList<String> = ArrayList()
        var zipFile: ZipFile? = null
        try {
            zipFile = ZipFile(path)
            val entries: Enumeration<out ZipEntry?> = zipFile.entries()
            while (entries.hasMoreElements()) {
                val entry: ZipEntry? = entries.nextElement()
                val name: String = entry!!.name
                if (name.endsWith(".xml") && name != "AndroidManifest.xml") {
                    xmlFiles.add(name)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close()
                } catch (ignored: IOException) {
                }
            }
        }
        xmlFiles.sort()
        return xmlFiles
    }
}
