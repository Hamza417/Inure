package app.simple.inure.util

import app.simple.inure.BuildConfig

@Suppress("KotlinConstantConditions")
object AppUtils {

    const val appVersion = BuildConfig.VERSION_NAME
    const val appVersionCode = BuildConfig.VERSION_CODE
    const val appPackageName = BuildConfig.APPLICATION_ID
    const val appBuildType = BuildConfig.BUILD_TYPE
    const val appFlavor = BuildConfig.FLAVOR
    const val unlockerPackageName = "app.simple.inureunlocker"

    /**
     * Returns true if the flavor is play store
     */
    fun isPlayFlavor(): Boolean {
        return BuildConfig.FLAVOR == "play"
    }

    /**
     * Returns true if the flavor is fdroid or github
     */
    fun isGithubFlavor(): Boolean {
        return BuildConfig.FLAVOR == "github"
    }
}