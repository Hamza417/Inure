package app.simple.inure.apk.parsers

import android.content.pm.ApplicationInfo
import app.simple.inure.exceptions.ApkParserException
import app.simple.inure.exceptions.CertificateParseException
import app.simple.inure.model.UsesFeatures
import app.simple.inure.util.StringUtils.capitalizeFirstLetter
import com.jaredrummler.apkparser.ApkParser
import com.jaredrummler.apkparser.model.AndroidComponent
import com.jaredrummler.apkparser.model.CertificateMeta
import com.jaredrummler.apkparser.model.DexInfo
import net.dongliu.apk.parser.ApkFile
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
        kotlin.runCatching {
            ApkParser.create(sourceDir).use {
                return it.manifestXml
            }
        }.onFailure { throwable ->
            throwable.printStackTrace()
            ApkFile(sourceDir).use {
                return it.manifestXml
            }
        }.getOrElse {
            throw ApkParserException("Couldn't parse manifest file due to error : ${it.message}")
        }
    }

    /**
     * Fetch the list of service from an APK file
     */
    fun ApplicationInfo.getServices(): MutableList<AndroidComponent>? {
        kotlin.runCatching {
            ApkParser.create(this).use {
                return it.androidManifest.services
            }
        }.getOrElse {
            throw ApkParserException("Couldn't fetch services due to error : ${it.message}")
        }
    }

    /**
     * Fetch the list of activities from an APK file
     */
    fun ApplicationInfo.getActivities(): MutableList<AndroidComponent>? {
        kotlin.runCatching {
            ApkParser.create(this).use {
                return it.androidManifest.activities
            }
        }.getOrElse {
            throw ApkParserException("Couldn't fetch activities due to error : ${it.message}")
        }
    }

    /**
     * Fetch the list of providers from an APK file
     */
    fun ApplicationInfo.getProviders(): MutableList<AndroidComponent>? {
        kotlin.runCatching {
            ApkParser.create(this).use {
                return it.androidManifest.providers
            }
        }.getOrElse {
            throw ApkParserException("Couldn't fetch providers due to error : ${it.message}")
        }
    }

    /**
     * Fetch the list of permissions from an APK file
     *
     * Warning - This function does not handle any error
     */
    fun ApplicationInfo.getPermissions(): MutableList<String> {
        kotlin.runCatching {
            ApkFile(sourceDir).use {
                return it.apkMeta.usesPermissions
            }
        }.onFailure { throwable ->
            throwable.printStackTrace()

            ApkParser.create(sourceDir).use {
                return it.apkMeta.usesPermissions
            }
        }.getOrElse {
            throw ApkParserException("Couldn't fetch permissions due to error : ${it.message}")
        }
    }

    /**
     * Fetch the list of features from an APK file
     */
    fun ApplicationInfo.getFeatures(): MutableList<UsesFeatures> {
        kotlin.runCatching {
            ApkParser.create(this).use {
                val list = mutableListOf<UsesFeatures>()
                for (i in it.androidManifest.apkMeta.usesFeatures) {
                    list.add(UsesFeatures(i.name, i.required))
                }
                return list
            }
        }.onFailure {
            ApkFile(sourceDir).use {
                val list = mutableListOf<UsesFeatures>()
                for (i in it.apkMeta.usesFeatures) {
                    list.add(UsesFeatures(i.name, i.isRequired))
                }
                return list
            }
        }.getOrElse {
            throw ApkParserException("Couldn't fetch features due to error : ${it.message}")
        }
    }

    /**
     * Fetch the install location of an APK file
     */
    fun ApplicationInfo.getInstallLocation(): String {
        ApkParser.create(this).use {
            return it.androidManifest.apkMeta
                    .installLocation.capitalizeFirstLetter()
        }
    }

    /**
     * Fetch the install location of an APK file
     */
    fun ApplicationInfo.getGlEsVersion(): String {
        kotlin.runCatching {
            ApkParser.create(this).use {
                return it.androidManifest.apkMeta
                        .glEsVersion.toString()
            }
        }.onFailure {
            ApkFile(sourceDir).use {
                return it.apkMeta.glEsVersion.toString()
            }
        }.getOrElse {
            throw ApkParserException("Couldn't fetch GLES version due to error : ${it.message}")
        }
    }

    /**
     * Fetch the certificate data from an APK file
     */
    fun ApplicationInfo.getCertificates(): CertificateMeta {
        kotlin.runCatching {
            ApkParser.create(this).use {
                return it.certificateMeta
            }
        }.getOrElse {
            throw CertificateParseException(it.message!!)
        }
    }

    /**
     * Fetch the list of broadcast receivers from
     * an APK file
     */
    fun ApplicationInfo.getReceivers(): MutableList<AndroidComponent>? {
        kotlin.runCatching {
            ApkParser.create(this).use {
                return it.androidManifest.receivers
            }
        }.getOrElse {
            throw ApkParserException("Couldn't fetch receivers due to error : ${it.message}")
        }
    }

    /**
     * Fetch the list of broadcast receivers from
     * an APK file
     */
    fun ApplicationInfo.getApkMeta(): Any? {
        kotlin.runCatching {
            ApkParser.create(this).use {
                return it.androidManifest.apkMeta
            }
        }.onFailure {
            ApkFile(sourceDir).use {
                return it.apkMeta
            }
        }.getOrElse {
            throw ApkParserException("Couldn't parse app info due to error : ${it.message}")
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
     *
     */
    fun ApplicationInfo.getDexData(): MutableList<DexInfo>? {
        ApkParser.create(this).use {
            return it.dexInfos
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

    /**
     * Get list of all raster image files within an APK file
     */
    fun getGraphicsFiles(path: String?): MutableList<String> {
        val graphicsFiles: MutableList<String> = ArrayList()
        var zipFile: ZipFile? = null
        try {
            zipFile = ZipFile(path)
            val entries: Enumeration<out ZipEntry?> = zipFile.entries()
            while (entries.hasMoreElements()) {
                val entry: ZipEntry? = entries.nextElement()
                val name: String = entry!!.name
                if (name.endsWith(".png")
                    || name.endsWith(".jpg")
                    || name.endsWith(".jpeg")
                    || name.endsWith(".gif")
                    || name.endsWith(".webp")
                    || name.endsWith(".svg")) {
                    graphicsFiles.add(name)
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
        graphicsFiles.sort()
        return graphicsFiles
    }

    /**
     * Get list of all raster image files within an APK file
     */
    fun getExtraFiles(path: String?): MutableList<String> {
        val graphicsFiles: MutableList<String> = ArrayList()
        var zipFile: ZipFile? = null
        try {
            zipFile = ZipFile(path)
            val entries: Enumeration<out ZipEntry?> = zipFile.entries()
            while (entries.hasMoreElements()) {
                val entry: ZipEntry? = entries.nextElement()
                val name: String = entry!!.name
                if (name.endsWith(".json")
                    || name.endsWith(".css")
                    || name.endsWith(".html")
                    || name.endsWith(".properties")
                    || name.endsWith(".js")
                    || name.endsWith(".tsv")
                    || name.endsWith(".txt")
                    || name.endsWith(".proto")
                    || name.endsWith(".java")
                    || name.endsWith(".bin")
                    || name.endsWith(".ttf")) {
                    graphicsFiles.add(name)
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
        graphicsFiles.sort()
        return graphicsFiles
    }
}
