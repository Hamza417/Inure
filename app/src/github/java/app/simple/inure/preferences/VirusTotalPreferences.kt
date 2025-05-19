package app.simple.inure.preferences

import androidx.core.content.edit

object VirusTotalPreferences {

    private const val VIRUS_TOTAL_API_KEY = "virus_total_api_key"

    const val LOADER_TYPE = "loader_type"

    const val LOADER_TYPE_POLICY = 0
    const val LOADER_TYPE_SECURITY = 1
    const val LOADER_TYPE_FIND_IN_PAGE = 2
    const val LOADER_TYPE_SEARCH = 3
    const val LOADER_TYPE_FINGERPRINT = 4

    // ---------------------------------------------------------------------------------------------------------- //

    fun setVirusTotalApiKey(value: String) {
        SharedPreferences.getEncryptedSharedPreferences().edit { putString(VIRUS_TOTAL_API_KEY, value) }
    }

    fun getVirusTotalApiKey(): String {
        return SharedPreferences.getEncryptedSharedPreferences().getString(VIRUS_TOTAL_API_KEY, "") ?: ""
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setLoaderType(value: Int) {
        SharedPreferences.getSharedPreferences().edit { putInt(LOADER_TYPE, value) }
    }

    fun getLoaderType(): Int {
        return SharedPreferences.getSharedPreferences().getInt(LOADER_TYPE, LOADER_TYPE_POLICY)
    }
}