package app.simple.inure.util

import app.simple.inure.BuildConfig

@Suppress("KotlinConstantConditions")
object BuildUtils {

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