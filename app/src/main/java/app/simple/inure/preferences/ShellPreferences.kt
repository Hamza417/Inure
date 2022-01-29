package app.simple.inure.preferences

import app.simple.inure.preferences.SharedPreferences.getSharedPreferences

object ShellPreferences {

    private const val commandLine = "shell_command_line"
    private const val initialCommand = "shell_initial_command"
    private const val terminalType = "shell_terminal_type"
    private const val mouseEvent = "shell_mouse_event"
    private const val closeWindow = "shell_close_window_on_exit"
    private const val verifyPathEntries = "shell_verify_path_entries"
    private const val allowPathExtensions = "shell_allow_path_extensions"
    private const val allowPathPrepend = "shell_allow_path_prepend"
    private const val homePath = "home_path"

    /* ---------------------------------------------------------------------------------------------- */

    fun getCommandLine(): String? {
        return getSharedPreferences().getString(commandLine, "/system/bin/sh -")
    }

    fun setCommandLine(value: String): Boolean {
        return getSharedPreferences().edit().putString(commandLine, value).commit()
    }

    /* ---------------------------------------------------------------------------------------------- */

    fun getInitialCommand(): String {
        return getSharedPreferences().getString(initialCommand, "")!!
    }

    fun setInitialCommand(value: String): Boolean {
        return getSharedPreferences().edit().putString(initialCommand, value).commit()
    }

    /* ---------------------------------------------------------------------------------------------- */

    fun getTerminalType(): String? {
        return getSharedPreferences().getString(terminalType, "screen")
    }

    fun setTerminalType(value: String): Boolean {
        return getSharedPreferences().edit().putString(terminalType, value).commit()
    }

    /* ---------------------------------------------------------------------------------------------- */

    fun getMouseEventState(): Boolean {
        return getSharedPreferences().getBoolean(mouseEvent, false)
    }

    fun setMouseEventState(value: Boolean): Boolean {
        return getSharedPreferences().edit().putBoolean(mouseEvent, value).commit()
    }

    /* ---------------------------------------------------------------------------------------------- */

    fun getCloseWindowOnExitState(): Boolean {
        return getSharedPreferences().getBoolean(closeWindow, true)
    }

    fun setCloseWindowOnExitState(value: Boolean): Boolean {
        return getSharedPreferences().edit().putBoolean(closeWindow, value).commit()
    }

    /* ---------------------------------------------------------------------------------------------- */

    fun getVerifyPathEntriesState(): Boolean {
        return getSharedPreferences().getBoolean(verifyPathEntries, true)
    }

    fun setVerifyPathEntriesState(value: Boolean): Boolean {
        return getSharedPreferences().edit().putBoolean(verifyPathEntries, value).commit()
    }

    /* ---------------------------------------------------------------------------------------------- */

    fun getAllowPathExtensionsState(): Boolean {
        return getSharedPreferences().getBoolean(allowPathExtensions, true)
    }

    fun setAllowPathExtensionsState(value: Boolean): Boolean {
        return getSharedPreferences().edit().putBoolean(allowPathExtensions, value).commit()
    }

    /* ---------------------------------------------------------------------------------------------- */

    fun getAllowPathPrependState(): Boolean {
        return getSharedPreferences().getBoolean(allowPathPrepend, true)
    }

    fun setAllowPathPrependState(value: Boolean): Boolean {
        return getSharedPreferences().edit().putBoolean(allowPathPrepend, value).commit()
    }

    /* ---------------------------------------------------------------------------------------------- */

    fun getHomePath(): String? {
        return getSharedPreferences().getString(homePath, "")
    }

    fun getHomePath(defValue: String): String? {
        return getSharedPreferences().getString(homePath, defValue)
    }

    // The home_path default is set dynamically in TermService.onCreate()
    fun setHomePath(value: String): Boolean {
        return getSharedPreferences().edit().putString(homePath, value).commit()
    }
}