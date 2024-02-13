package app.simple.inure.preferences

object ConfigurationPreferences {

    private const val keepScreenOn = "keep_screen_on"
    private const val showUsersList = "show_users_list"
    const val isUsingRoot = "is_using_root"
    const val isUsingShizuku = "is_using_shizuku"
    const val language = "language_of_app"
    const val appPath = "app_path"
    const val isExternalStorage = "is_external_storage"

    fun setKeepScreenOn(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(keepScreenOn, value).apply()
    }

    fun isKeepScreenOn(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(keepScreenOn, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setUsingRoot(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(isUsingRoot, value).apply()
    }

    fun isUsingRoot(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(isUsingRoot, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAppLanguage(value: String) {
        SharedPreferences.getSharedPreferences().edit().putString(language, value).apply()
    }

    fun getAppLanguage(): String? {
        return SharedPreferences.getSharedPreferences().getString(language, "default")
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setUsingShizuku(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(isUsingShizuku, value).apply()
    }

    fun isUsingShizuku(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(isUsingShizuku, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAppPath(value: String) {
        SharedPreferences.getSharedPreferences().edit().putString(appPath, value).apply()
    }

    fun getAppPath(): String {
        return SharedPreferences.getSharedPreferences().getString(appPath, "Inure App Manager") ?: "Inure App Manager"
    }

    fun defaultAppPath() {
        SharedPreferences.getSharedPreferences().edit().putString(appPath, "Inure App Manager").apply()
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setExternalStorage(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(isExternalStorage, value).apply()
    }

    fun isExternalStorage(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(isExternalStorage, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setShowUsersList(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(showUsersList, value).apply()
    }

    fun isShowUsersList(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(showUsersList, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun isRootOrShizuku(): Boolean {
        return isUsingRoot() || isUsingShizuku()
    }
}