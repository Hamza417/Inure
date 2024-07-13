package app.simple.inure.preferences

object ConfigurationPreferences {

    private const val KEEP_SCREEN_ON = "keep_screen_on"
    private const val SHOW_USERS_LIST = "show_users_list"
    private const val APP_PATH = "app_path"

    const val IS_USING_ROOT = "is_using_root"
    const val IS_USING_SHIZUKU = "is_using_shizuku"
    const val LANGUAGE = "language_of_app"
    const val IS_EXTERNAL_STORAGE = "is_external_storage"

    fun setKeepScreenOn(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(KEEP_SCREEN_ON, value).apply()
    }

    fun isKeepScreenOn(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(KEEP_SCREEN_ON, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setUsingRoot(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(IS_USING_ROOT, value).apply()
    }

    fun isUsingRoot(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(IS_USING_ROOT, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAppLanguage(value: String) {
        SharedPreferences.getSharedPreferences().edit().putString(LANGUAGE, value).apply()
    }

    fun getAppLanguage(): String? {
        return SharedPreferences.getSharedPreferences().getString(LANGUAGE, "default")
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setUsingShizuku(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(IS_USING_SHIZUKU, value).apply()
    }

    fun isUsingShizuku(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(IS_USING_SHIZUKU, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAppPath(value: String) {
        SharedPreferences.getSharedPreferences().edit().putString(APP_PATH, value).apply()
    }

    fun getAppPath(): String {
        return SharedPreferences.getSharedPreferences().getString(APP_PATH, "Inure App Manager") ?: "Inure App Manager"
    }

    fun defaultAppPath() {
        SharedPreferences.getSharedPreferences().edit().putString(APP_PATH, "Inure App Manager").apply()
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setExternalStorage(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(IS_EXTERNAL_STORAGE, value).apply()
    }

    fun isExternalStorage(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(IS_EXTERNAL_STORAGE, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setShowUsersList(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(SHOW_USERS_LIST, value).apply()
    }

    fun isShowUsersList(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(SHOW_USERS_LIST, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun isRootOrShizuku(): Boolean {
        return isUsingRoot() || isUsingShizuku()
    }
}
