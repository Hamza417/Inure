package app.simple.inure.util

import app.simple.inure.BuildConfig

@Suppress("KotlinConstantConditions")
object AppUtils {

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

    /**
     * Returns true if the flavor is beta
     */
    fun isBetaFlavor(): Boolean {
        return BuildConfig.FLAVOR == "beta"
    }

    /**
     * Returns true if DEBUG
     */
    fun isDebug(): Boolean {
        return BuildConfig.DEBUG
    }
}