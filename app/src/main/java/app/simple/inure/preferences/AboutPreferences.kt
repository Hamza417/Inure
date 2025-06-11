package app.simple.inure.preferences

import androidx.core.content.edit

object AboutPreferences {

    private const val SHARE_MESSAGE = "last_share_message"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setShareMessage(value: String) {
        SharedPreferences.getSharedPreferences().edit { putString(SHARE_MESSAGE, value) }
    }

    fun getShareMessage(): String {
        return SharedPreferences.getSharedPreferences()
            .getString(SHARE_MESSAGE,
                       "Let me recommend you this really good looking applications manager app.")!!
    }
}
