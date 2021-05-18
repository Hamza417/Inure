package app.simple.inure.util

import android.os.Build
import androidx.annotation.RequiresApi
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.preferences.MainPreferences
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.text.CharacterIterator
import java.text.StringCharacterIterator
import kotlin.math.abs

object FileSizeHelper {

    fun String.getFileSize(): String {
        return File(this).length().getFileSize()
    }

    fun String.getDirectoryLength(): Long {
        return File(this).length()
    }

    fun Array<String>.getDirectorySize(): String {
        var total = 0L

        for (i in this.indices) {
            total = File(this[i]).length()
        }

        return total.getFileSize()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun String.getDirectorySize(): String {
        return try {
            Files.walk(Paths.get(this))
                    .filter { p -> p.toFile().isFile }
                    .mapToLong { p -> p.toFile().length() }
                    .sum().getFileSize()
        } catch (e: IOException) {
            e.printStackTrace()
            0L.getFileSize()
        }
    }

    fun String.getNumberOfFile(): Int {
        return File(this).list()!!.size
    }

    fun Long.getFileSize(): String {
        return when {
            ConfigurationPreferences.getSizeType() == "si" -> {
                this.humanReadableByteCountSI()
            }
            ConfigurationPreferences.getSizeType() == "binary" -> {
                this.humanReadableByteCountBinary()
            }
            else -> {
                this.humanReadableByteCountSI()
            }
        }
    }

    private fun Long.humanReadableByteCountBinary(): String {
        val absB = if (this == Long.MIN_VALUE) Long.MAX_VALUE else abs(this)
        if (absB < 1024) {
            return "$this B"
        }
        var value = absB
        val ci: CharacterIterator = StringCharacterIterator("KMGTPE")
        var i = 40
        while (i >= 0 && absB > 0xfffccccccccccccL shr i) {
            value = value shr 10
            ci.next()
            i -= 10
        }
        value *= java.lang.Long.signum(this).toLong()
        return String.format("%.1f %ciB", value / 1024.0, ci.current())
    }

    private fun Long.humanReadableByteCountSI(): String {
        var bytes = this
        if (-1000 < bytes && bytes < 1000) {
            return "$bytes B"
        }
        val ci: CharacterIterator = StringCharacterIterator("kMGTPE")
        while (bytes <= -999950 || bytes >= 999950) {
            bytes /= 1000
            ci.next()
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current())
    }
}