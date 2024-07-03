package app.simple.inure.util

import android.annotation.SuppressLint
import android.os.Build
import app.simple.inure.preferences.FormattingPreferences
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.text.CharacterIterator
import java.text.StringCharacterIterator
import java.util.LinkedList
import kotlin.math.abs

object FileSizeHelper {

    fun String?.toSize(): String {
        return try {
            File(this!!).length().toSize()
        } catch (e: NullPointerException) {
            0L.toSize()
        }
    }

    fun String.getDirectoryLength(): Long {
        return File(this).length()
    }

    fun String?.toLength(): Long {
        return try {
            File(this!!).length()
        } catch (e: NullPointerException) {
            0L
        }
    }

    fun Array<String>.getDirectorySize(): Long {
        var total = 0L

        for (i in this.indices) {
            total += File(this[i]).length()
        }

        return total
    }

    fun String.getDirectorySize(): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Files.walk(Paths.get(this))
                    .filter { p -> p.toFile().isFile }
                    .mapToLong { p -> p.toFile().length() }
                    .sum().toSize()
            } else {
                val file = File(this)
                var result: Long = 0
                val dirs: MutableList<File> = LinkedList()

                if (!file.exists()) {
                    return 0L.toSize()
                }

                if (!file.isDirectory) {
                    return file.length().toSize()
                }

                dirs.add(file)

                while (dirs.isNotEmpty()) {
                    val dir = dirs.removeAt(0)
                    if (!dir.exists()) continue
                    val listFiles = dir.listFiles()
                    if (listFiles == null || listFiles.isEmpty()) continue
                    for (child in listFiles) {
                        result += child.length()
                        if (child.isDirectory) dirs.add(child)
                    }
                }

                result.toSize()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            0L.toSize()
        }
    }

    fun String.getNumberOfFile(): Int {
        return File(this).list()!!.size
    }

    fun Int.toBytes(): Int {
        return when (FormattingPreferences.getSizeType()) {
            "si" -> {
                this * 1000
            }
            "binary" -> {
                this * 1024
            }
            else -> {
                this * 1000
            }
        }
    }

    fun Int.toSize(): String {
        return toLong().toSize()
    }

    fun Long.toSize(): String {
        return when (FormattingPreferences.getSizeType()) {
            "si" -> {
                this.humanReadableByteCountSI()
            }
            "binary" -> {
                this.humanReadableByteCountBinary()
            }
            else -> {
                this.humanReadableByteCountSI()
            }
        }
    }

    @SuppressLint("DefaultLocale")
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

    @SuppressLint("DefaultLocale")
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
