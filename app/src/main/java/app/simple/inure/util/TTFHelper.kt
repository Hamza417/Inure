package app.simple.inure.util

import android.content.Context
import android.content.pm.PackageInfo
import android.graphics.Typeface
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.util.FileUtils.copyStreamToFile
import java.io.File
import java.io.InputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

object TTFHelper {
    fun getTTFFile(path: String, packageInfo: PackageInfo, context: Context): Typeface? {
        kotlin.runCatching {
            ZipFile(packageInfo.safeApplicationInfo.sourceDir).use {
                val entries: Enumeration<out ZipEntry?> = it.entries()
                while (entries.hasMoreElements()) {
                    val entry: ZipEntry? = entries.nextElement()
                    val name: String = entry!!.name
                    if (name == path) {
                        File(context.getExternalFilesDir(null)!!.path + "/font_cache/").mkdir()
                        val file = File(context.getExternalFilesDir(null)?.path + "/font_cache/" + name.substring(name.lastIndexOf("/")))
                        copyStreamToFile(ZipFile(packageInfo.safeApplicationInfo.sourceDir).getInputStream(entry), file)
                        return Typeface.createFromFile(file)
                    }
                }
            }
        }.getOrElse {
            it.printStackTrace()
        }

        return null
    }

    fun getTTFFile(inputStream: InputStream, context: Context, name: String): Typeface? {
        kotlin.runCatching {
            File(context.getExternalFilesDir(null)!!.path + "/font_cache/").mkdir()
            val file = File(context.getExternalFilesDir(null)?.path + "/font_cache/" + name)
            copyStreamToFile(inputStream, file)
            return Typeface.createFromFile(file)
        }.getOrElse {
            it.printStackTrace()
        }

        return null
    }
}
