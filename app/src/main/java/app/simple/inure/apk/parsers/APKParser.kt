package app.simple.inure.apk.parsers

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import app.simple.inure.R
import app.simple.inure.exceptions.ApkParserException
import app.simple.inure.exceptions.CertificateParseException
import app.simple.inure.exceptions.DexClassesNotFoundException
import app.simple.inure.models.UsesFeatures
import app.simple.inure.preferences.ExtrasPreferences
import app.simple.inure.preferences.GraphicsPreferences
import app.simple.inure.util.StringUtils.capitalizeFirstLetter
import com.jaredrummler.apkparser.ApkParser
import com.jaredrummler.apkparser.model.AndroidComponent
import com.jaredrummler.apkparser.model.ApkMeta
import com.jaredrummler.apkparser.model.CertificateMeta
import com.jaredrummler.apkparser.model.DexInfo
import java.io.IOException
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

object APKParser {

    const val ARMEABI = "“generic” 32-bit ARM"
    const val ARM64 = "arm64-v8a"
    const val ARMv7 = "armeabi-v7a"
    const val x86 = "x86"
    const val x86_64 = "x86_64"

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
            ApkManifestFetcher.getManifestXmlFromFilePath(sourceDir)
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

    fun PackageInfo.getNativeLibraries(context: Context): StringBuilder {
        val stringBuilder = StringBuilder()
        var zipFile: ZipFile? = null
        try {
            zipFile = ZipFile(applicationInfo.sourceDir)
            val entries: Enumeration<out ZipEntry?> = zipFile.entries()
            while (entries.hasMoreElements()) {
                val entry: ZipEntry? = entries.nextElement()
                val name: String = entry!!.name
                if (name.contains("lib") || name.contains("libs")) {
                    if (name.endsWith(".so")) {
                        if (stringBuilder.isNotEmpty()) {
                            stringBuilder.append("\n")
                        }

                        stringBuilder.append(name)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            if (stringBuilder.isBlank()) {
                stringBuilder.append(context.getString(R.string.error))
            }
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        if (stringBuilder.isBlank()) {
            stringBuilder.append(context.getString(R.string.none))
        }

        return stringBuilder
    }

    fun PackageInfo.getApkArchitecture(context: Context): StringBuilder {
        var zipFile: ZipFile? = null
        val stringBuilder = StringBuilder()

        try {
            zipFile = ZipFile(applicationInfo.sourceDir)
            val entries: Enumeration<out ZipEntry?> = zipFile.entries()
            while (entries.hasMoreElements()) {
                val entry: ZipEntry? = entries.nextElement()
                val name: String = entry!!.name
                if (name.contains("lib")) {
                    if (name.contains(ARMEABI)) {
                        if (!stringBuilder.contains(ARMEABI)) {
                            if (stringBuilder.isNotBlank()) {
                                stringBuilder.append(" | ")
                            }

                            stringBuilder.append(ARMEABI)
                        }
                    }

                    if (name.contains(ARM64)) {
                        if (!stringBuilder.contains(ARM64)) {
                            if (stringBuilder.isNotBlank()) {
                                stringBuilder.append(" | ")
                            }

                            stringBuilder.append(ARM64)
                        }
                    }

                    if (name.contains(ARMv7)) {
                        if (!stringBuilder.contains(ARMv7)) {
                            if (stringBuilder.isNotBlank()) {
                                stringBuilder.append(" | ")
                            }

                            stringBuilder.append(ARMv7)
                        }
                    }

                    if (name.contains(x86)) {
                        if (!stringBuilder.contains(x86)) {
                            if (stringBuilder.isNotBlank()) {
                                stringBuilder.append(" | ")
                            }

                            stringBuilder.append(x86)
                        }
                    }

                    if (name.contains(x86_64)) {
                        if (!stringBuilder.contains(x86_64)) {
                            if (stringBuilder.isNotBlank()) {
                                stringBuilder.append(" | ")
                            }

                            stringBuilder.append(x86_64)
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            if (stringBuilder.isBlank()) {
                stringBuilder.append(context.getString(R.string.error))
            }
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        if (stringBuilder.isBlank()) {
            stringBuilder.append(context.getString(R.string.unspecified))
        }

        return stringBuilder
    }

    /**
     * Fetch the list of broadcast receivers from
     * an APK file
     */
    fun ApplicationInfo.getApkMeta(): ApkMeta {
        kotlin.runCatching {
            ApkParser.create(this).use {
                return it.androidManifest.apkMeta
            }
        }.getOrElse {
            throw ApkParserException("Couldn't parse app info due to error : ${it.message}")
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
     * Get list of all xml files within an APK file
     */
    fun getXmlFiles(path: String?, keyword: String): MutableList<String> {
        val xmlFiles: MutableList<String> = ArrayList()
        var zipFile: ZipFile? = null
        try {
            zipFile = ZipFile(path)
            val entries: Enumeration<out ZipEntry?> = zipFile.entries()
            while (entries.hasMoreElements()) {
                val entry: ZipEntry? = entries.nextElement()
                val name: String = entry!!.name
                if (name.endsWith(".xml") && name != "AndroidManifest.xml") {
                    if (name.contains(keyword)) xmlFiles.add(name)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        xmlFiles.sortBy {
            it.lowercase()
        }
        return xmlFiles
    }

    /**
     * Get list of all raster image files within an APK file
     */
    fun getGraphicsFiles(path: String?, keyword: String): MutableList<String> {

        val png = GraphicsPreferences.isFilterAllowed(GraphicsPreferences.png)
        val jpg = GraphicsPreferences.isFilterAllowed(GraphicsPreferences.jpg)
        val jpeg = GraphicsPreferences.isFilterAllowed(GraphicsPreferences.jpeg)
        val gif = GraphicsPreferences.isFilterAllowed(GraphicsPreferences.gif)
        val webp = GraphicsPreferences.isFilterAllowed(GraphicsPreferences.webp)
        val svg = GraphicsPreferences.isFilterAllowed(GraphicsPreferences.svg)

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
                        name.endsWith(".png") -> if (png) graphicsFiles.add(name)
                        name.endsWith(".jpg") -> if (jpg) graphicsFiles.add(name)
                        name.endsWith(".jpeg") -> if (jpeg) graphicsFiles.add(name)
                        name.endsWith(".gif") -> if (gif) graphicsFiles.add(name)
                        name.endsWith(".webp") -> if (webp) graphicsFiles.add(name)
                        name.endsWith(".svg") -> if (svg) graphicsFiles.add(name)
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
        // val version = ExtrasPreferences.isFilterAllowed(ExtrasPreferences.version)

        val extraFiles: MutableList<String> = ArrayList()
        var zipFile: ZipFile? = null

        try {
            zipFile = ZipFile(path)
            val entries: Enumeration<out ZipEntry?> = zipFile.entries()
            while (entries.hasMoreElements()) {
                val entry: ZipEntry? = entries.nextElement()
                val name: String = entry!!.name

                if (name.lowercase().contains(keyword.lowercase())) {
                    when {
                        name.endsWith(".json") -> if (json) extraFiles.add(name)
                        name.endsWith(".css") -> if (css) extraFiles.add(name)
                        name.endsWith(".html") -> if (html) extraFiles.add(name)
                        name.endsWith(".properties") -> if (properties) extraFiles.add(name)
                        name.endsWith(".js") -> if (js) extraFiles.add(name)
                        name.endsWith(".tsv") -> if (tsv) extraFiles.add(name)
                        name.endsWith(".txt") -> if (txt) extraFiles.add(name)
                        name.endsWith(".proto") -> if (proto) extraFiles.add(name)
                        name.endsWith(".java") -> if (java) extraFiles.add(name)
                        name.endsWith(".bin") -> if (bin) extraFiles.add(name)
                        name.endsWith(".ttf") -> if (ttf) extraFiles.add(name)
                        name.endsWith(".md") -> if (md) extraFiles.add(name)
                        name.endsWith(".ini") -> if (ini) extraFiles.add(name)
                        // name.endsWith(".version") -> if (version) extraFiles.add(name)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            kotlin.runCatching {
                zipFile?.close()
            }
        }

        extraFiles.sortBy {
            it.lowercase()
        }

        return extraFiles
    }
}