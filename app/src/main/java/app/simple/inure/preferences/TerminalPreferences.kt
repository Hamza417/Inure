package app.simple.inure.preferences

import android.annotation.SuppressLint
import androidx.annotation.IntRange
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences

object TerminalPreferences {

    const val fontSize = "terminal_font_size"

    /* ---------------------------------------------------------------------------------------------- */

    fun getFontSize(): Int {
        return getSharedPreferences().getInt(fontSize, 10)
    }

    @SuppressLint("ApplySharedPref")
    fun setFontSize(@IntRange(from = 0, to = 288) value: Int): Boolean {
        return getSharedPreferences().edit().putInt(fontSize, value).commit()
    }

}