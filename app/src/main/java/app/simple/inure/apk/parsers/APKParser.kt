package app.simple.inure.apk.parsers

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.constants.Extensions.isExtrasFile
import app.simple.inure.constants.Extensions.isImageFile
import app.simple.inure.exceptions.ApkParserException
import app.simple.inure.exceptions.DexClassesNotFoundException
import app.simple.inure.models.Extra
import app.simple.inure.models.Graphic
import app.simple.inure.preferences.SearchPreferences
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.FileUtils
import com.android.apksig.apk.ApkFormatException
import com.android.apksig.apk.ApkUtils
import com.android.apksig.apk.ApkUtils.ZipSections
import com.android.apksig.internal.zip.CentralDirectoryRecord
import com.android.apksig.internal.zip.LocalFileRecord
import com.android.apksig.util.DataSource
import com.android.apksig.util.DataSources
import com.android.apksig.zip.ZipFormatException
import net.dongliu.apk.parser.ApkFile
import net.dongliu.apk.parser.bean.ApkMeta
import net.dongliu.apk.parser.bean.DexClass
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Enumeration
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

@Suppress("ConstPropertyName", "ObjectPrivatePropertyName")
object APKParser {

    private const val ARMEABI = "armeabi \"generic\" 32-bit ARM"
    private const val ARM64 = "arm64-v8a"
    private const val ARMv7 = "armeabi-v7a"
    private const val MIPS = "mips"
    private const val x86 = "x86"
    private const val x86_64 = "x86_64"

    private const val __32Bit = "32-bit"
    private const val __64Bit = "64-bit"

    private val _32Bit = listOf(
            ARMEABI,
            ARMv7,
            MIPS,
            x86
    )

    private val _64Bit = listOf(
            ARM64,
            x86_64
    )

    const val ANDROID_MANIFEST = "AndroidManifest.xml"

    /**
     * Fetch the install location of an APK file
     */
    fun File.getGlEsVersion(): String {
        kotlin.runCatching {
            ApkFile(this).use {
                return it.apkMeta.glEsVersion.toString()
            }
        }.getOrElse {
            throw ApkParserException("Couldn't fetch GLES version due to error : ${it.message}")
        }
    }

    fun File.getMinSDK(): String {
        kotlin.runCatching {
            ApkFile(this).use {
                return it.apkMeta.minSdkVersion.toString()
            }
        }.getOrElse {
            throw ApkParserException("Couldn't fetch min SDK version due to error : ${it.message}")
        }
    }

    @kotlin.jvm.Throws(IOException::class, NullPointerException::class)
    fun PackageInfo.getNativeLibraries(context: Context): String {
        val files = mutableListOf<String>()
        files.add(safeApplicationInfo.sourceDir)

        safeApplicationInfo.splitSourceDirs?.let {
            files.addAll(it)
        }

        val libraries = files.mapNotNull { filePath ->
            File(filePath).takeIf {
                it.exists()
            }?.getNativeLibraries()
        }.filter {
            it.isNotBlank()
        }

        return if (libraries.isEmpty()) {
            context.getString(R.string.none)
        } else {
            libraries.joinToString("\n")
        }
    }

    fun File.getNativeLibraries(): String {
        return ZipFile(this).use { zipFile ->
            zipFile.entries().asSequence()
                .mapNotNull { it?.name }
                .filter { it.contains("lib") && it.endsWith(".so") }
                .joinToString("\n")
        }
    }

    fun PackageInfo.getArchitecture(context: Context): String {
        val files = mutableListOf<String>()
        files.add(safeApplicationInfo.sourceDir)

        safeApplicationInfo.splitSourceDirs?.let {
            files.addAll(it)
        }

        val architectures: List<String> = files.mapNotNull { filePath ->
            File(filePath).takeIf {
                it.exists()
            }?.getArchitecture(context)
        }.filter {
            it.isNotBlank()
        }

        return if (architectures.isEmpty()) {
            context.getString(R.string.unspecified)
        } else {
            architectures.joinToString(" | ")
                .split(" | ")
                .distinct()
                .joinToString(" | ")
        }
    }

    fun File.getArchitecture(context: Context): String {
        val architectures = _64Bit + _32Bit

        return try {
            ZipFile(this).use { zipFile ->
                val entries = zipFile.entries().asSequence()
                    .mapNotNull { it?.name }
                    .toList() // Convert the sequence to a list to allow multiple iterations

                val detectedArchitectures = entries
                    .filter { it.contains("lib") }
                    .mapNotNull { name -> architectures.find { name.contains(it) } }
                    .distinct()

                val result = buildString {
                    if (detectedArchitectures.any { _32Bit.contains(it) }) {
                        append(__32Bit)
                        append(" | ")
                    }
                    if (detectedArchitectures.any { _64Bit.contains(it) }) {
                        append(__64Bit)
                        append(" | ")
                    }

                    append(detectedArchitectures.joinToString(" | "))
                }

                result
            }
        } catch (e: IOException) {
            e.printStackTrace()
            context.getString(R.string.error)
        }
    }

    /**
     * Fetch the list of broadcast receivers from
     * an APK file
     */
    fun ApplicationInfo.getApkMeta(): ApkMeta {
        kotlin.runCatching {
            ApkFile(this.sourceDir).use {
                return it.apkMeta
            }
        }.getOrElse {
            throw ApkParserException("Couldn't parse app info due to error : ${it.message}")
        }
    }

    /**
     * Fetch APK's dex data
     */
    fun File.getDexData(): Array<DexClass> {
        kotlin.runCatching {
            ApkFile(this).use {
                return it.dexClasses
            }
        }.getOrElse {
            throw DexClassesNotFoundException("This apk does not contain any recognizable dex classes data.")
        }
    }

    /**
     * Get list of all xml files within an APK file
     */
    fun getXmlFiles(path: String?, keyword: String, ignoreCase: Boolean = true): MutableList<String> {
        val xmlFiles: MutableList<String> = ArrayList()
        var zipFile: ZipFile? = null
        try {
            zipFile = ZipFile(path)
            val entries: Enumeration<out ZipEntry?> = zipFile.entries()
            while (entries.hasMoreElements()) {
                val entry: ZipEntry? = entries.nextElement()
                val name: String = entry!!.name
                if (name.lowercase().endsWith(".xml") && name != "AndroidManifest.xml") {
                    if (name.contains(keyword, ignoreCase)) {
                        xmlFiles.add(name)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
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

    fun PackageInfo.getMatchedResourcesSize(keyword: String, case: Boolean = SearchPreferences.isCasingIgnored()): Int {
        return getXmlFiles(safeApplicationInfo.sourceDir, keyword, case).size
    }

    /**
     * Get list of all raster image files within an APK file
     */
    fun getGraphicsFiles(path: String?, keyword: String): MutableList<Graphic> {
        val graphicsFiles: MutableList<Graphic> = ArrayList()
        var zipFile: ZipFile? = null
        try {
            zipFile = ZipFile(path)
            val entries: Enumeration<out ZipEntry?> = zipFile.entries()
            while (entries.hasMoreElements()) {

                val entry: ZipEntry? = entries.nextElement()
                val entryPath: String = entry!!.name // is a path
                val graphic = Graphic()

                when {
                    keyword.lowercase().startsWith("$") -> {
                        if (entryPath.lowercase().endsWith(keyword.lowercase().replace("$", ""))) {
                            if (entryPath.lowercase().isImageFile()) {
                                graphic.path = entryPath
                                graphic.name = entryPath.substringAfterLast("/")
                                graphic.size = entry.size
                                graphicsFiles.add(graphic)
                            }
                        }
                    }
                    keyword.lowercase().endsWith("$") -> {
                        if (entryPath.lowercase().startsWith(keyword.lowercase().replace("$", ""))) {
                            if (entryPath.lowercase().isImageFile()) {
                                graphic.path = entryPath
                                graphic.name = entryPath.substringAfterLast("/")
                                graphic.size = entry.size
                                graphicsFiles.add(graphic)
                            }
                        }
                    }
                    else -> {
                        if (entryPath.lowercase().contains(keyword.lowercase())) {
                            if (entryPath.lowercase().isImageFile()) {
                                graphic.path = entryPath
                                graphic.name = entryPath.substringAfterLast("/")
                                graphic.size = entry.size
                                graphicsFiles.add(graphic)
                            }
                        }
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

        return graphicsFiles
    }

    /**
     * Get list of all raster image files within an APK file
     */
    fun getExtraFiles(path: String?, keyword: String): MutableList<Extra> {

        val extraFiles: MutableList<Extra> = ArrayList()
        var zipFile: ZipFile? = null

        try {
            zipFile = ZipFile(path)
            val entries: Enumeration<out ZipEntry?> = zipFile.entries()
            while (entries.hasMoreElements()) {
                val entry: ZipEntry? = entries.nextElement()
                val name: String = entry!!.name // is a path
                val extra = Extra()

                if (keyword.lowercase().startsWith("$")) {
                    if (name.lowercase().endsWith(keyword.lowercase().replace("$", ""))) {
                        if (name.isExtrasFile()) {
                            extra.path = name
                            extra.name = name.substringAfterLast("/")
                            extra.size = entry.size
                            extraFiles.add(extra)
                        }
                    }
                } else if (keyword.lowercase().endsWith("$")) {
                    if (name.lowercase().startsWith(keyword.lowercase().replace("$", ""))) {
                        if (name.isExtrasFile()) {
                            extra.path = name
                            extra.name = name.substringAfterLast("/")
                            extra.size = entry.size
                            extraFiles.add(extra)
                        }
                    }
                } else {
                    if (name.lowercase().contains(keyword.lowercase())) {
                        if (name.isExtrasFile()) {
                            extra.path = name
                            extra.name = name.substringAfterLast("/")
                            extra.size = entry.size
                            extraFiles.add(extra)
                        }
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

        return extraFiles
    }

    @Throws(ApkParserException::class, IOException::class)
    fun getManifestByteBuffer(file: File): ByteBuffer {
        RandomAccessFile(file, FileUtils.FILE_MODE_READ).use { randomAccessFile ->
            val source: DataSource = DataSources.asDataSource(randomAccessFile)
            val zipSections: ZipSections = ApkUtils.findZipSections(source)
            val centralDirectoryRecords: List<CentralDirectoryRecord> = parseZipCentralDirectory(source, zipSections)
            val slicedSource = source.slice(0, zipSections.zipCentralDirectoryOffset)
            return extractAndroidManifest(centralDirectoryRecords, slicedSource)
        }
    }

    @Throws(IOException::class, ApkFormatException::class)
    fun parseZipCentralDirectory(apk: DataSource, sections: ZipSections): List<CentralDirectoryRecord> {
        val sizeBytes: Long = sections.zipCentralDirectorySizeBytes.checkSizeOrThis()
        val offset = sections.zipCentralDirectoryOffset
        val buffer: ByteBuffer = apk.getByteBuffer(offset, sizeBytes.toInt())
            .order(ByteOrder.LITTLE_ENDIAN)
        val expectedCdRecordCount = sections.zipCentralDirectoryRecordCount
        val records: MutableList<CentralDirectoryRecord> = ArrayList(expectedCdRecordCount)

        for (i in 0 until expectedCdRecordCount) {
            /**
             * ZIP entry ending with '/' is a directory entry.
             */
            CentralDirectoryRecord.getRecord(buffer).let {
                if (it.name.endsWith("/").invert()) {
                    records.add(it)
                }
            }
        }



        return records
    }

    @Throws(IOException::class, ApkFormatException::class, ZipFormatException::class)
    private fun extractAndroidManifest(records: List<CentralDirectoryRecord>, logicalHeaderFileSection: DataSource): ByteBuffer {
        val androidManifestRecord = records.find { it.name == ANDROID_MANIFEST }
            ?: throw ApkFormatException("$ANDROID_MANIFEST not found in APK's Central Directory")

        val uncompressedData = LocalFileRecord.getUncompressedData(
                logicalHeaderFileSection, androidManifestRecord, logicalHeaderFileSection.size()
        )

        return ByteBuffer.wrap(uncompressedData)
    }

    private fun Long.checkSizeOrThis(): Long {
        when {
            this > Int.MAX_VALUE -> {
                throw ApkFormatException("ZIP Central Directory too large: $this bytes")
            }
            this < 0 -> {
                throw ApkFormatException("ZIP Central Directory negative size: $this bytes")
            }
            else -> {
                return this
            }
        }
    }
}
