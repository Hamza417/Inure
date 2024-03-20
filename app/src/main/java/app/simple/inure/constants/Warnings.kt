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
    const val SHIZUKU_BINDER_NOT_READY = "0x00B: Shizuku binder is not ready"
}
