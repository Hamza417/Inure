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

    /**
     * If you're experiencing longer loading times, you can always disable startup loading from Preferences -> Behavior
     */
    const val LONG_LOADING_TIME = "0x011: If you're experiencing longer loading times, you can always disable startup loading from Preferences -> Behavior"

    /**
     * Data contains an unsupported type value
     *
     * Replace $ with the unsupported type value
     */
    const val UNSUPPORTED_TYPE_VALUE = "0x012: data contains an unsupported type value: $"

    /**
     * Using development features may cause unexpected behaviors in the app
     */
    const val DEVELOPMENT_FEATURES_WARNING = "0x013: Using development features may cause unexpected behaviors in the app, use these toggles at your own discretion. " +
            "These features are experimental and may also be removed from the app in future."

    /**
     * ETIP ID is null, this shouldn't have happened. Please report this issue to the developer
     */
    const val ETIP_ID_NULL = "0x014: ETIP ID is not available."

    const val UNIDENTIFIED_ERROR = "0x000788: unidentified error occurred"

    /**
     * Internet connection is not available
     */
    const val NO_INTERNET_CONNECTION = "0x015: Internet connection is not available. Please check your network settings and try again."

    /**
     * Development mode enabled
     */
    const val DEVELOPMENT_MODE_ENABLED = "0x016: Development mode enabled. This mode is intended for developers and may cause unexpected behaviors in the app. Use at your own risk."

    /**
     * Wait till loading is complete
     */
    const val WAIT_FOR_LOADING = "0x017: Please wait until the loading is complete before performing any actions. This may take a few seconds depending on your device and the amount of data being loaded."

    /**
     * Invalid file and application data state. Please check if the file is valid and the application data is correct.
     */
    const val INVALID_FILE_AND_APP_DATA = "0x018: Invalid file and application data state. Please check if the file is valid and the application data is correct."
}
