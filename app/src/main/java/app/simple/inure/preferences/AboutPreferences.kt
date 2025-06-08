package app.simple.inure.preferences

import android.annotation.SuppressLint
import androidx.core.content.edit

object AboutPreferences {

    private const val SHARE_MESSAGE = "last_share_message"
    const val IS_DEVELOPMENT_MODE = "is_development_mode"

    // ---------------------------------------------------------------------------------------------------------- //

    fun setShareMessage(value: String) {
        SharedPreferences.getSharedPreferences().edit { putString(SHARE_MESSAGE, value) }
    }

    fun getShareMessage(): String {
        return SharedPreferences.getSharedPreferences()
            .getString(SHARE_MESSAGE,
                       "Let me recommend you this really good looking applications manager app.")!!
    }

    // ---------------------------------------------------------------------------------------------------------- //

    @SuppressLint("UseKtx")
    fun setDevelopmentMode(value: Boolean): Boolean {
        return SharedPreferences.getSharedPreferences().edit().putBoolean(IS_DEVELOPMENT_MODE, value).commit()
    }

    fun isDevelopmentMode(): Boolean {
        return SharedPreferences.getSharedPreferences()
            .getBoolean(IS_DEVELOPMENT_MODE, false)
    }
}
