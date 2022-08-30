package app.simple.inure.apk.utils

import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.exception.ZipException
import java.io.File

object ZipUtils {
    fun zip(zip: String?, files: List<File?>?) {
        try {
            ZipFile(zip).addFiles(files)
        } catch (e: ZipException) {
            e.printStackTrace()
        }
    }
}