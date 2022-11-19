package app.simple.inure.util

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Build
import app.simple.inure.R
import app.simple.inure.apk.utils.MetaUtils
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.util.FileSizeHelper.toSize

object PackageListUtils {
    fun TypeFaceTextView.setAppInfo(apps: PackageInfo) {
        val stringBuilder = StringBuilder()

        if ((apps.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0) {
            stringBuilder.append(context.getString(R.string.user))
        } else {
            stringBuilder.append(context.getString(R.string.system))
        }

        stringBuilder.append(" | ")
        stringBuilder.append(apps.applicationInfo.sourceDir.toSize())
        stringBuilder.append(" | ")

        if (apps.applicationInfo.enabled) {
            stringBuilder.append(context.getString(R.string.enabled))
        } else {
            stringBuilder.append(context.getString(R.string.disabled))
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stringBuilder.append(" | ")
            stringBuilder.append(MetaUtils.getCategory(apps.applicationInfo.category, context))
        }

        text = stringBuilder
    }
}