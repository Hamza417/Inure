package app.simple.inure.util

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import app.simple.inure.constants.Extensions
import java.io.*
import java.nio.file.InvalidPathException
import java.nio.file.Paths
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object FileUtils {

    private const val BUFFER = 1024

    fun openFolder(context: Context, location: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        val myDir: Uri = FileProvider.getUriForFile(context, context.applicationContext.packageName + ".provider", File(location))
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.setDataAndType(myDir, DocumentsContract.Document.MIME_TYPE_DIR)

        if (intent.resolveActivityInfo(context.packageManager, 0) != null) {
            context.startActivity(intent)
        }
    }

    /**
     * Copy the given file to the destination [File]
     */
    fun String.copyTo(destination: File) {
        FileInputStream(File(this)).use { `in` ->
            FileOutputStream(destination).use { out ->
                val buf = ByteArray(BUFFER)
                var len: Int
                while (`in`.read(buf).also { len = it } > 0) {
                    out.write(buf, 0, len)
                }
            }
        }
    }

    fun ArrayList<File>.getLength(): Long {
        var total = 0L
        for (file in this) {
            total += file.length()
        }
        return total
    }

    fun ArrayList<File>.findFile(fileName: String): File? {
        for (file in this) {
            if (file.isFile && file.path.endsWith(fileName)) {
                return file
            }
        }

        return null
    }

    /**
     * Converts the given input stream to the given output file
     */
    fun copyStreamToFile(inputStream: InputStream, outputFile: File) {
        inputStream.use { input ->
            FileOutputStream(outputFile).use { output ->
                val buffer = ByteArray(1024) // buffer size
                while (true) {
                    val byteCount = input.read(buffer)
                    if (byteCount < 0) break
                    output.write(buffer, 0, byteCount)
                }
                output.flush()
            }
        }
    }

    @Throws(IOException::class, NullPointerException::class, ArrayIndexOutOfBoundsException::class)
    fun createZip(_files: Array<String>, zipFileName: File?) {
        var origin: BufferedInputStream? = null
        var out: ZipOutputStream? = null

        try {
            out = ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFileName)))
            val data = ByteArray(BUFFER)
            for (i in _files.indices) {
                Log.v("Compress", "Adding: " + _files[i])
                val fi = FileInputStream(_files[i])
                origin = BufferedInputStream(fi, BUFFER)
                val entry = ZipEntry(_files[i].substring(_files[i].lastIndexOf("/") + 1))
                out.putNextEntry(entry)
                var count: Int
                while (origin.read(data, 0, BUFFER).also { count = it } != -1) {
                    out.write(data, 0, count)
                }
                origin.close()
            }
            out.close()
        } finally {
            origin!!.close()
            out!!.close()
        }
    }

    fun String.toFile(): File {
        return File(this)
    }

    fun Uri.getMimeType(context: Context): String? {
        return if (ContentResolver.SCHEME_CONTENT == scheme) {
            context.contentResolver.getType(this)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(this.toString())
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.lowercase(Locale.ROOT))
        }
    }

    /**
     * <pre>
     * Checks if a string is a valid path.
     * Null safe.
     *
     * Calling examples:
     * isValidPath("c:/test");      //returns true
     * isValidPath("c:/te:t");      //returns false
     * isValidPath("c:/te?t");      //returns false
     * isValidPath("c/te*t");       //returns false
     * isValidPath("good.txt");     //returns true
     * isValidPath("not|good.txt"); //returns false
     * isValidPath("not:good.txt"); //returns false
     * </pre>
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun isValidPath(path: String?): Boolean {
        try {
            Paths.get(path)
        } catch (ex: InvalidPathException) {
            return false
        } catch (ex: java.lang.NullPointerException) {
            return false
        }
        return true
    }

    fun Uri.isSVG(context: Context): Boolean {
        return getMimeType(context) == "image/svg+xml"
    }

    fun convertToByteArray(file: File): ByteArray? {
        val fileBytes = ByteArray(file.length().toInt())
        return try {
            FileInputStream(file).use { inputStream -> inputStream.read(fileBytes) }
            fileBytes
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    fun String.isImageFile(): Boolean {
        return Extensions.imageExtensions.contains(this.substring(this.lastIndexOf(".") + 1))
    }

    fun String.makePathBashCompatible(): String {
        return this.replace(" ", "\\ ")
    }

    /**
     * This is a terrible way to check if a file is a .nomedia file or directory
     */
    fun File.isNomediaFileOrDirectory(): Boolean {
        return if (isFile) {
            if (absolutePath.split("/").any { it.startsWith(".") }) {
                return true
            } else {
                if (name == ".nomedia") {
                    return true
                } else {
                    absolutePath.split("/").forEach { it ->
                        return it.toFile().listFiles()?.any {
                            it.name == ".nomedia"
                        } ?: false
                    }

                    return false
                }
            }
        } else if (isDirectory) {
            if (absolutePath.split("/").any { it.startsWith(".") }) {
                return true
            } else {
                listFiles()?.forEach {
                    if (it.name == ".nomedia") {
                        return true
                    }
                }
            }
            false
        } else {
            false
        }
    }
}