package app.simple.inure.preferences

import androidx.core.content.edit

object VirusTotalPreferences {

    private const val VIRUS_TOTAL_API_KEY = "virus_total_api_key"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setVirusTotalApiKey(value: String) {
        SharedPreferences.getEncryptedSharedPreferences().edit { putString(VIRUS_TOTAL_API_KEY, value) }
    }

    fun getVirusTotalApiKey(): String {
        return SharedPreferences.getEncryptedSharedPreferences().getString(VIRUS_TOTAL_API_KEY, "") ?: ""
    }
}