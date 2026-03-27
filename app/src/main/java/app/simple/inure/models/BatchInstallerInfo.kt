package app.simple.inure.models

import java.io.File

/**
 * Represents a single APK entry in the batch installer list.
 *
 * @property file The original [File] on disk (may be a bundle like `.apks`, `.xapk`, `.apkm`, or a plain `.apk`).
 * @property apkFiles The resolved list of individual `.apk` files to install. For a plain APK this
 *   is a single-element list containing [file]; for a split-APK bundle it contains every `.apk`
 *   extracted from the archive.
 * @property appName Human-readable application label extracted from the base APK manifest.
 * @property packageName The package name declared in the base APK manifest.
 * @property installState Current installation state for this entry.
 * @property failureReason Optional message populated when [installState] is [InstallState.FAILED].
 *
 * @author Hamza417
 */
data class BatchInstallerInfo(
        val file: File,
        val apkFiles: ArrayList<File> = arrayListOf(),
        val appName: String,
        val packageName: String,
        var installState: InstallState = InstallState.PENDING,
        var failureReason: String? = null
) {
    /**
     * Represents the possible states for an APK installation in the batch queue.
     */
    enum class InstallState {
        /** Waiting in queue, not yet started. */
        PENDING,

        /** Currently being installed. */
        INSTALLING,

        /** Installation completed successfully. */
        INSTALLED,

        /** Installation failed; see [BatchInstallerInfo.failureReason] for details. */
        FAILED
    }
}

