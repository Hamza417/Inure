package app.simple.inure.preferences

import app.simple.inure.preferences.SharedPreferences.getSharedPreferences

object ShellPreferences {

    private const val COMMAND_LINE = "shell_command_line"
    private const val INITIAL_COMMAND = "shell_initial_command"
    private const val TERMINAL_TYPE = "shell_terminal_type"
    private const val MOUSE_EVENT = "shell_mouse_event"
    private const val CLOSE_WINDOW = "shell_close_window_on_exit"
    private const val VERIFY_PATH_ENTRIES = "shell_verify_path_entries"
    private const val ALLOW_PATH_EXTENSIONS = "shell_allow_path_extensions"
    private const val ALLOW_PATH_PREPEND = "shell_allow_path_prepend"
    private const val HOME_PATH = "home_path"

    const val USE_RISH = "use_rish"

    /* ---------------------------------------------------------------------------------------------- */

    fun getCommandLine(): String? {
        return getSharedPreferences().getString(COMMAND_LINE, "/system/bin/sh -")
    }

    fun setCommandLine(value: String): Boolean {
        return getSharedPreferences().edit().putString(COMMAND_LINE, value).commit()
    }

    /* ---------------------------------------------------------------------------------------------- */

    fun getInitialCommand(): String {
        return getSharedPreferences().getString(INITIAL_COMMAND, "")!!
    }

    fun setInitialCommand(value: String): Boolean {
        return getSharedPreferences().edit().putString(INITIAL_COMMAND, value).commit()
    }

    /* ---------------------------------------------------------------------------------------------- */

    fun getTerminalType(): String? {
        return getSharedPreferences().getString(TERMINAL_TYPE, "screen")
    }

    fun setTerminalType(value: String): Boolean {
        return getSharedPreferences().edit().putString(TERMINAL_TYPE, value).commit()
    }

    /* ---------------------------------------------------------------------------------------------- */

    fun getMouseEventState(): Boolean {
        return getSharedPreferences().getBoolean(MOUSE_EVENT, false)
    }

    fun setMouseEventState(value: Boolean): Boolean {
        return getSharedPreferences().edit().putBoolean(MOUSE_EVENT, value).commit()
    }

    /* ---------------------------------------------------------------------------------------------- */

    fun getCloseWindowOnExitState(): Boolean {
        return getSharedPreferences().getBoolean(CLOSE_WINDOW, true)
    }

    fun setCloseWindowOnExitState(value: Boolean): Boolean {
        return getSharedPreferences().edit().putBoolean(CLOSE_WINDOW, value).commit()
    }

    /* ---------------------------------------------------------------------------------------------- */

    fun getVerifyPathEntriesState(): Boolean {
        return getSharedPreferences().getBoolean(VERIFY_PATH_ENTRIES, true)
    }

    fun setVerifyPathEntriesState(value: Boolean): Boolean {
        return getSharedPreferences().edit().putBoolean(VERIFY_PATH_ENTRIES, value).commit()
    }

    /* ---------------------------------------------------------------------------------------------- */

    fun getAllowPathExtensionsState(): Boolean {
        return getSharedPreferences().getBoolean(ALLOW_PATH_EXTENSIONS, true)
    }

    fun setAllowPathExtensionsState(value: Boolean): Boolean {
        return getSharedPreferences().edit().putBoolean(ALLOW_PATH_EXTENSIONS, value).commit()
    }

    /* ---------------------------------------------------------------------------------------------- */

    fun getAllowPathPrependState(): Boolean {
        return getSharedPreferences().getBoolean(ALLOW_PATH_PREPEND, true)
    }

    fun setAllowPathPrependState(value: Boolean): Boolean {
        return getSharedPreferences().edit().putBoolean(ALLOW_PATH_PREPEND, value).commit()
    }

    /* ---------------------------------------------------------------------------------------------- */

    fun getHomePath(): String? {
        return getSharedPreferences().getString(HOME_PATH, "")
    }

    fun getHomePath(defValue: String): String? {
        return getSharedPreferences().getString(HOME_PATH, defValue)
    }

    // The home_path default is set dynamically in TermService.onCreate()
    fun setHomePath(value: String): Boolean {
        return getSharedPreferences().edit().putString(HOME_PATH, value).commit()
    }

    /* ---------------------------------------------------------------------------------------------- */

    fun isUsingRISH(): Boolean {
        return getSharedPreferences().getBoolean(USE_RISH, false)
    }

    fun setUseRISH(value: Boolean): Boolean {
        return getSharedPreferences().edit().putBoolean(USE_RISH, value).commit()
    }
}
