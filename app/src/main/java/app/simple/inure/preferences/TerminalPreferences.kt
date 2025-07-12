package app.simple.inure.preferences

import androidx.annotation.IntRange
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences

object TerminalPreferences {

    private const val FONT_SIZE = "terminal_font_size"
    private const val COLOR = "terminal_color"
    private const val CURSOR_BLINK = "terminal_cursor_blink"
    private const val UTF8 = "terminal_default_utf_8"
    private const val BACK_BUTTON_ACTION = "terminal_back_button_action"
    private const val ALT_KEY_ESCAPE = "terminal_alt_key_escape"
    private const val USE_KEYBOARD_SHORTCUTS = "terminal_use_keyboard_shortcuts"

    const val INPUT_METHOD = "input_method"
    const val CONTROL_KEY = "terminal_control_key"
    const val FN_KEY = "terminal_fn_key"

    /* ---------------------------------------------------------------------------------------------- */

    // Font size
    fun getFontSize(): Int {
        return getSharedPreferences().getInt(FONT_SIZE, 10)
    }

    fun setFontSize(@IntRange(from = 0, to = 288) value: Int): Boolean {
        return getSharedPreferences().edit().putInt(FONT_SIZE, value).commit()
    }

    /* ---------------------------------------------------------------------------------------------- */

    fun getColor(): Int {
        return getSharedPreferences().getInt(COLOR, 0)
    }

    fun setColor(value: Int): Boolean {
        return getSharedPreferences().edit().putInt(COLOR, value).commit()
    }

    /* ---------------------------------------------------------------------------------------------- */

    fun getCursorBlinkState(): Boolean {
        return getSharedPreferences().getBoolean(CURSOR_BLINK, false)
    }

    fun setCursorBlinkState(value: Boolean): Boolean {
        return getSharedPreferences().edit().putBoolean(CURSOR_BLINK, value).commit()
    }

    /* ---------------------------------------------------------------------------------------------- */

    fun getUTF8State(): Boolean {
        return getSharedPreferences().getBoolean(UTF8, false)
    }

    fun setUTF8State(value: Boolean): Boolean {
        return getSharedPreferences().edit().putBoolean(UTF8, value).commit()
    }

    /* ---------------------------------------------------------------------------------------------- */

    fun getBackButtonAction(): Int {
        return getSharedPreferences().getInt(BACK_BUTTON_ACTION, 2)
    }

    fun setBackButtonAction(value: Int): Boolean {
        return getSharedPreferences().edit().putInt(BACK_BUTTON_ACTION, value).commit()
    }

    /* ---------------------------------------------------------------------------------------------- */

    fun getControlKey(): Int {
        return getSharedPreferences().getInt(CONTROL_KEY, 5)
    }

    fun setControlKey(value: Int): Boolean {
        return getSharedPreferences().edit().putInt(CONTROL_KEY, value).commit()
    }

    /* ---------------------------------------------------------------------------------------------- */

    fun getFnKey(): Int {
        return getSharedPreferences().getInt(FN_KEY, 4)
    }

    fun setFnKey(value: Int): Boolean {
        return getSharedPreferences().edit().putInt(FN_KEY, value).commit()
    }

    /* ---------------------------------------------------------------------------------------------- */

    fun getAltKeyEscapeState(): Boolean {
        return getSharedPreferences().getBoolean(ALT_KEY_ESCAPE, false)
    }

    fun setAltKeyEscapeState(value: Boolean): Boolean {
        return getSharedPreferences().edit().putBoolean(ALT_KEY_ESCAPE, value).commit()
    }

    /* ---------------------------------------------------------------------------------------------- */

    fun getKeyboardShortcutState(): Boolean {
        return getSharedPreferences().getBoolean(USE_KEYBOARD_SHORTCUTS, true)
    }

    fun setKeyboardShortcutState(value: Boolean): Boolean {
        return getSharedPreferences().edit().putBoolean(USE_KEYBOARD_SHORTCUTS, value).commit()
    }

    /* ---------------------------------------------------------------------------------------------- */

    fun getInputMethod(): Int {
        return getSharedPreferences().getInt(INPUT_METHOD, 0)
    }

    fun setInputMethod(@IntRange(from = 0, to = 1) value: Int): Boolean {
        return getSharedPreferences().edit().putInt(INPUT_METHOD, value).commit()
    }
}
