package app.simple.inure.constants

@Suppress("unused")
object Warnings {

    /**
     * InureWarning01: Cannot establish a root connection with the main shell.
     */
    fun getNoRootConnectionWarning(): String = "0x001: Could not establish a root connection with the main shell."

    /**
     * InureWarning02: Failed to load the bitmap
     */
    fun getFailedToLoadFileWarning(path: String, type: String): String = "0x002: Failed to load type: $type from path: $path"

    /**
     * InureWarning03: Unknown app state detected!
     */
    fun gtUnknownAppStateWarning(): String = "0x003: Unknown app state detected!"

    /**
     * Invalid unlocker detected
     */
    fun getInvalidUnlockerWarning(): String = "0x004: Invalid unlocker package detected or unlocker integrity has been compromised!"

    /**
     * InureWarning05: App integrity failed
     */
    fun getAppIntegrityFailedWarning(): String = "0x005: App integrity has been compromised"

    /**
     * Failed to initialize the Shizuku.
     */
    fun getShizukuFailedWarning(): String = "0x006: Failed to initialize Shizuku."

    /**
     * No file explorer app installed on your device
     */
    fun getNoFileExplorerWarning(): String = "0x007: No file explorer app installed on your device"

    /**
     * Unable to verify the integrity of the unlocker package
     */
    fun getUnableToVerifyUnlockerWarning(): String = "0x008: Unable to verify the integrity of the unlocker package"

    /**
     * Root service is taking too long to respond
     */
    fun getRootServiceTimeoutWarning(): String = "0x009: Root service is taking too long to respond, try restarting the app."

    /**
     * Activity not found or not supported by your device
     */
    fun getActivityNotFoundWarning(): String = "0x00A: Activity not found or not supported by your device"

    /**
     * Shizuku binder is not ready
     */
    const val SHIZUKU_BINDER_NOT_READY = "0x00B: Shizuku binder is not ready, please check if Shizuku is running properly."

    /**
     * Shizuku permission denied, please open the Shizuku app and grant the permission
     */
    const val SHIZUKU_PERMISSION_DENIED = "0x00C: Shizuku permission denied, please open the Shizuku app and grant the permission manually."

    /**
     * Android System dex classes are not supported
     */
    const val ANDROID_SYSTEM_DEX_CLASSES = "0x00D: Android System dex classes are not supported"

    /**
     * Empty flags
     */
    const val EMPTY_FLAGS = "0x00E: Empty flags"

    /**
     * Usage stats access blocked
     */
    const val USAGE_STATS_ACCESS_BLOCKED = "0x00F: Usage stats access blocked"

    /**
     * Root or shizuku required to change the state
     */
    const val ROOT_OR_SHIZUKU_REQUIRED = "0x010: Root or Shizuku required to change the state"
}
