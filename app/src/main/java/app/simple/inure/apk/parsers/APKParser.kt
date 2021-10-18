package app.simple.inure.apk.parsers

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import app.simple.inure.exceptions.ApkParserException
import app.simple.inure.exceptions.CertificateParseException
import app.simple.inure.exceptions.DexClassesNotFoundException
import app.simple.inure.exceptions.InureXmlParserException
import app.simple.inure.model.UsesFeatures
import app.simple.inure.preferences.ExtrasPreferences
import app.simple.inure.util.StringUtils.capitalizeFirstLetter
import com.jaredrummler.apkparser.ApkParser
import com.jaredrummler.apkparser.model.AndroidComponent
import com.jaredrummler.apkparser.model.CertificateMeta
import com.jaredrummler.apkparser.model.DexInfo
import net.dongliu.apk.parser.ApkFile
import net.dongliu.apk.parser.bean.DexClass
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
    fun PackageInfo.getActivities(): MutableList<AndroidComponent>? {
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
        kotlin.runCatching {
            ApkParser.create(this).use {
                return it.androidManifest.apkMeta
                        .installLocation.capitalizeFirstLetter()
            }
        }.onFailure {
            ApkFile(sourceDir).use {
                return it.apkMeta.installLocation.capitalizeFirstLetter()
            }
        }.getOrElse {
            throw Exception()
        }
    }

    /**
     * Fetch the install location of an APK file
     */
    fun PackageInfo.getGlEsVersion(): String {
        kotlin.runCatching {
            ApkParser.create(this).use {
                return it.androidManifest.apkMeta
                        .glEsVersion.toString()
            }
        }.onFailure {
            ApkFile(this.applicationInfo.sourceDir).use {
                return it.apkMeta.glEsVersion.toString()
            }
        }.getOrElse {
            throw ApkParserException("Couldn't fetch GLES version due to error : ${it.message}")
        }
    }

    /**
     * Fetch the certificate data from an APK file
     */
    fun PackageInfo.getCertificates(): CertificateMeta {
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
    fun PackageInfo.getReceivers(): MutableList<AndroidComponent>? {
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
        kotlin.runCatching {
            ApkParser.create(this).use {
                return it.transBinaryXml(path)
            }
        }.onFailure {
            ApkFile(sourceDir).use {
                return it.transBinaryXml(path)
            }
        }.getOrElse {
            throw InureXmlParserException("Couldn't parse XML file for package $packageName")
        }
    }

    /**
     * Fetch APK's dex data
     */
    fun ApplicationInfo.getDexData(): MutableList<DexInfo>? {
        kotlin.runCatching {
            ApkParser.create(this).use {
                return it.dexInfos
            }
        }.getOrElse {
            throw DexClassesNotFoundException("This apk does not contain any recognizable dex classes data.")
        }
    }

    /**
     * Get list of all dex classes
     */
    fun PackageInfo.getDexClasses(): ArrayList<DexClass> {
        ApkFile(this.applicationInfo.sourceDir).use {
            return it.dexClasses!!.toList() as ArrayList<DexClass>
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
    fun getGraphicsFiles(path: String?, keyword: String): MutableList<String> {
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

                    if (name.lowercase().contains(keyword.lowercase())) {
                        graphicsFiles.add(name)
                    }
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
    fun getExtraFiles(path: String?, keyword: String): MutableList<String> {

        val json = ExtrasPreferences.isFilterAllowed(ExtrasPreferences.json)
        val css = ExtrasPreferences.isFilterAllowed(ExtrasPreferences.css)
        val html = ExtrasPreferences.isFilterAllowed(ExtrasPreferences.html)
        val properties = ExtrasPreferences.isFilterAllowed(ExtrasPreferences.properties)
        val js = ExtrasPreferences.isFilterAllowed(ExtrasPreferences.js)
        val tsv = ExtrasPreferences.isFilterAllowed(ExtrasPreferences.tsv)
        val txt = ExtrasPreferences.isFilterAllowed(ExtrasPreferences.txt)
        val proto = ExtrasPreferences.isFilterAllowed(ExtrasPreferences.proto)
        val java = ExtrasPreferences.isFilterAllowed(ExtrasPreferences.java)
        val bin = ExtrasPreferences.isFilterAllowed(ExtrasPreferences.bin)
        val ttf = ExtrasPreferences.isFilterAllowed(ExtrasPreferences.ttf)
        val md = ExtrasPreferences.isFilterAllowed(ExtrasPreferences.md)
        val ini = ExtrasPreferences.isFilterAllowed(ExtrasPreferences.ini)

        val graphicsFiles: MutableList<String> = ArrayList()
        var zipFile: ZipFile? = null

        try {
            zipFile = ZipFile(path)
            val entries: Enumeration<out ZipEntry?> = zipFile.entries()
            while (entries.hasMoreElements()) {
                val entry: ZipEntry? = entries.nextElement()
                val name: String = entry!!.name

                if (name.lowercase().contains(keyword.lowercase())) {
                    when {
                        name.endsWith(".json") -> if (json) graphicsFiles.add(name)
                        name.endsWith(".css") -> if (css) graphicsFiles.add(name)
                        name.endsWith(".html") -> if (html) graphicsFiles.add(name)
                        name.endsWith(".properties") -> if (properties) graphicsFiles.add(name)
                        name.endsWith(".js") -> if (js) graphicsFiles.add(name)
                        name.endsWith(".tsv") -> if (tsv) graphicsFiles.add(name)
                        name.endsWith(".txt") -> if (txt) graphicsFiles.add(name)
                        name.endsWith(".proto") -> if (proto) graphicsFiles.add(name)
                        name.endsWith(".java") -> if (java) graphicsFiles.add(name)
                        name.endsWith(".bin") -> if (bin) graphicsFiles.add(name)
                        name.endsWith(".ttf") -> if (ttf) graphicsFiles.add(name)
                        name.endsWith(".md") -> if (md) graphicsFiles.add(name)
                        name.endsWith(".ini") -> if (ini) graphicsFiles.add(name)
                    }
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
