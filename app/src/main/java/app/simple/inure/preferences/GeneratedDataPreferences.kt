package app.simple.inure.preferences

object GeneratedDataPreferences {

    const val generatedDataType = "generated_data_type"

    const val name = "generated_name"
    const val packageName = "generated_package_name"
    private const val version = "generated_version"
    private const val installDate = "generated_install_date"
    private const val updateDate = "generated_update_date"
    private const val play_store = "generated_play_store"
    private const val fdroid = "generated_fdroid"

    const val TXT = "txt"
    const val XML = "xml"
    const val MD = "md"
    const val HTML = "html"
    const val CSV = "csv"
    const val JSON = "json"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setGeneratedDataType(type: String) {
        SharedPreferences.getSharedPreferences().edit().putString(generatedDataType, type).apply()
    }

    fun getGeneratedDataType(): String {
        return SharedPreferences.getSharedPreferences().getString(generatedDataType, TXT)!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setGeneratedName(type: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(name, type).apply()
    }

    fun isGeneratedName(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(name, true)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setGeneratedPackageName(type: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(packageName, type).apply()
    }

    fun isGeneratedPackageName(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(packageName, true)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setGeneratedVersion(type: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(version, type).apply()
    }

    fun isGeneratedVersion(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(version, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setGeneratedInstallDate(type: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(installDate, type).apply()
    }

    fun isGeneratedInstallDate(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(installDate, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setGeneratedUpdateDate(type: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(updateDate, type).apply()
    }

    fun isGeneratedUpdateDate(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(updateDate, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setGeneratedPlayStore(type: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(play_store, type).apply()
    }

    fun isGeneratedPlayStore(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(play_store, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setGeneratedFdroid(type: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(fdroid, type).apply()
    }

    fun isGeneratedFdroid(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(fdroid, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun reset() {
        setGeneratedDataType(TXT)
        setGeneratedName(false)
        setGeneratedPackageName(false)
        setGeneratedVersion(false)
        setGeneratedInstallDate(false)
        setGeneratedUpdateDate(false)
        setGeneratedPlayStore(false)
        setGeneratedFdroid(false)
    }
}