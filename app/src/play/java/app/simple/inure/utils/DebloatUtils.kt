package app.simple.inure.utils

import android.content.pm.PackageInfo

object DebloatUtils {

    fun PackageInfo.isPackageBloat(): Boolean {
        return false
    }

    fun initBloatAppsSet() {
        /* no-op */
    }
}
