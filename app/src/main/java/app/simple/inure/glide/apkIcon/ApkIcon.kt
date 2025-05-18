package app.simple.inure.glide.apkIcon

import android.content.Context
import java.io.File

class ApkIcon(val context: Context, val file: File) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ApkIcon

        if (context != other.context) return false
        if (file != other.file) return false

        return true
    }

    override fun hashCode(): Int {
        return file.hashCode()
    }
}
