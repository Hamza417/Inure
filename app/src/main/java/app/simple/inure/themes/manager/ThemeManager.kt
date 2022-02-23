package app.simple.inure.themes.manager

import app.simple.inure.themes.interfaces.ThemeChangedListener

object ThemeManager {

    private val listeners = mutableSetOf<ThemeChangedListener>()

    var theme = Theme.LIGHT
        set(value) {
            if (field != value) {
                field = value
                listeners.forEach { listener -> listener.onThemeChanged(value) }
            }
        }

    fun addListener(listener: ThemeChangedListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: ThemeChangedListener) {
        listeners.remove(listener)
    }

}