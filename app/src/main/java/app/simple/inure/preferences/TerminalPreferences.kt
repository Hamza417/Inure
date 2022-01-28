package app.simple.inure.preferences

import androidx.annotation.IntRange
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences

object TerminalPreferences {

    private const val fontSize = "terminal_font_size"
    private const val color = "terminal_color"
    private const val utf8 = "terminal_default_utf_8"
    private const val backButtonAction = "terminal_back_button_action"
    private const val controlKey = "terminal_control_key"
    private const val fnKey = "terminal_fn_key"
    private const val altKeyEscape = "terminal_alt_key_escape"
    private const val useKeyboardShortcuts = "terminal_use_keyboard_shortcuts"

    /* ---------------------------------------------------------------------------------------------- */

    // Font size
    fun getFontSize(): Int {
        return getSharedPreferences().getInt(fontSize, 10)
    }

    fun setFontSize(@IntRange(from = 0, to = 288) value: Int): Boolean {
        return getSharedPreferences().edit().putInt(fontSize, value).commit()
    }

    /* ---------------------------------------------------------------------------------------------- */

    fun getColor(): Int {
        return getSharedPreferences().getInt(color, 0)
    }

    fun setColor(value: Int): Boolean {
        return getSharedPreferences().edit().putInt(color, value).commit()
    }

    /* ---------------------------------------------------------------------------------------------- */

    fun getUTF8State(): Boolean {
        return getSharedPreferences().getBoolean(utf8, false)
    }

    fun setUTF8State(value: Boolean): Boolean {
        return getSharedPreferences().edit().putBoolean(utf8, value).commit()
    }

    /* ---------------------------------------------------------------------------------------------- */

    fun getBackButtonAction(): Int {
        return getSharedPreferences().getInt(backButtonAction, 2)
    }

    fun setBackButtonAction(value: Int): Boolean {
        return getSharedPreferences().edit().putInt(backButtonAction, value).commit()
    }

    /* ---------------------------------------------------------------------------------------------- */

    fun getControlKey(): Int {
        return getSharedPreferences().getInt(controlKey, 5)
    }

    fun setControlKey(value: Int): Boolean {
        return getSharedPreferences().edit().putInt(controlKey, value).commit()
    }

    /* ---------------------------------------------------------------------------------------------- */

    fun getFnKey(): Int {
        return getSharedPreferences().getInt(fnKey, 4)
    }

    fun setFnKey(value: Int): Boolean {
        return getSharedPreferences().edit().putInt(fnKey, value).commit()
    }

    /* ---------------------------------------------------------------------------------------------- */

    fun getAltKeyEscapeState(): Boolean {
        return getSharedPreferences().getBoolean(altKeyEscape, false)
    }

    fun setAltKeyEscapeState(value: Boolean): Boolean {
        return getSharedPreferences().edit().putBoolean(altKeyEscape, value).commit()
    }

    /* ---------------------------------------------------------------------------------------------- */

    fun getKeyboardShortcutState(): Boolean {
        return getSharedPreferences().getBoolean(useKeyboardShortcuts, true)
    }

    fun setKeyboardShortcutState(value: Boolean): Boolean {
        return getSharedPreferences().edit().putBoolean(useKeyboardShortcuts, value).commit()
    }
}