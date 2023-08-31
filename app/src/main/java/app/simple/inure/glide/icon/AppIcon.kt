package app.simple.inure.glide.icon

import android.content.Context
import java.io.File

class AppIcon {

    val context: Context
    val packageName: String
    val enabled: Boolean
    val file: File?

    @Suppress("ConvertSecondaryConstructorToPrimary")
    constructor(context: Context, packageName: String, enabled: Boolean, file: File? = null) {
        this.context = context
        this.packageName = packageName
        this.enabled = enabled
        this.file = file
    }

    override fun hashCode(): Int {
        var result = context.hashCode()
        result = 31 * result + packageName.hashCode()
        result = 31 * result + enabled.hashCode()
        result = 31 * result + (file?.hashCode() ?: 0)
        return result
    }

    override fun equals(other: Any?): Boolean {
        return other is AppIcon && other.packageName == packageName
    }

    override fun toString(): String {
        return "AppIcon(context=$context, packageName='$packageName', enabled=$enabled, file=$file)"
    }

    companion object {
        fun from(context: Context, packageName: String, enabled: Boolean, file: File? = null): AppIcon {
            return AppIcon(context, packageName, enabled, file)
        }
    }
}